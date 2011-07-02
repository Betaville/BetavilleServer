/** Copyright (c) 2008-2011, Brooklyn eXperimental Media Center
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Brooklyn eXperimental Media Center nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Brooklyn eXperimental Media Center BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.poly.bxmc.betaville.server;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import edu.poly.bxmc.betaville.server.network.ConnectionTracker;
import edu.poly.bxmc.betaville.server.network.SecureServerManager;
import edu.poly.bxmc.betaville.server.network.ServerManager;
import edu.poly.bxmc.betaville.server.session.availability.InMemorySessionTracker;
import edu.poly.bxmc.betaville.server.session.availability.SessionTracker;
import edu.poly.bxmc.betaville.server.util.Preferences;

/**
 * Class <Server> - Launcher 
 *
 * @author Caroline Bouchat
 */
public class ServerLauncher {
	
	private static Logger logger;
	
	private static long reportInterval = 600000;

	public static final ExecutorService managerPool = Executors.newCachedThreadPool();

	/**
	 * Method <Server> - Launch the server
	 *
	 * @param args Arguments
	 */
	public static void main(final String[] args) {
		
		// Is this a query? (i.e: help, versions, etc)
		if(args.length>0){
			// now we know that we have a command, let's see which it is
			if(isHelpArgument(args[0])){
				System.out.println("++++-You have reached Betaville Server-++++");
				System.out.println("--Don't you wish that we had put some information here?");
				System.out.println("--Thanks for stopping by, but you really want to be at http://betaville.net");
			}
			else if(isVersionArgument(args[0])){
				System.out.println("VERSION?!");
			}
		}
		
		
		// Set up preferences
		try {
			Preferences.initialize();
		} catch (IOException e) {
			System.err.println("A preferences file could not be created in the Betaville directory.  " +
					"Please ensure that you're home directory has write-permissions " +
					"enabled.  Betaville will run but your preferences will not be saved.");
		}
		
		// Set up logging
		try {
			DateFormat.getDateInstance().format(new Date());
			Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n"),
					Preferences.getSetting(Preferences.STORAGE_LOGGING)+DateFormat.getDateInstance().format(new Date())+".log"));
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n")));
			Logger.getRootLogger().setLevel(Level.INFO);
			logger = Logger.getLogger(ServerLauncher.class);
		} catch (IOException e) {
			System.err.println("Log file coult not be opened for writing!  Please check your user permissions.");
			e.printStackTrace();
		}
		
		// Set up session tracker
		try {
			Class<? extends SessionTracker> sessionTrackerType = (Class<? extends SessionTracker>) Class.forName(Preferences.getSetting(Preferences.SESSION_TRACKER));
			SessionTracker.registerTracker(sessionTrackerType.newInstance());
		} catch (ClassNotFoundException e) {
			logger.error("The "+SessionTracker.class.getSimpleName() + ", \""+Preferences.getSetting(Preferences.SESSION_TRACKER)+
					"\", specified in config.xml is not valid.  This may stem from a type or a classpath issue.  Defaulting to " +
					InMemorySessionTracker.class.getSimpleName());
			SessionTracker.registerTracker(new InMemorySessionTracker());
		} catch (ClassCastException e) {
			logger.error("The "+SessionTracker.class.getSimpleName() + ", \""+Preferences.getSetting(Preferences.SESSION_TRACKER)+
					"\", specified in config.xml is not of the type "+SessionTracker.class.getName()+".  Defaulting to " +
					InMemorySessionTracker.class.getSimpleName());
			SessionTracker.registerTracker(new InMemorySessionTracker());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Create insecure manager
		managerPool.submit(new Runnable(){
			@Override
			public void run() {
				new ServerManager(null, args);
			}});

		// Create SSL manager (only if SSL is enabled)
		if(Preferences.getBooleanSetting(Preferences.NETWORK_USE_SSL)){
			managerPool.submit(new Runnable(){
				@Override
				public void run() {
					new SecureServerManager(args);
					logger.info("SSL Connections Enabled");
				}});
		}

		// Create report timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				ConnectionTracker.generateLogReport();
			}
		}, reportInterval, reportInterval);
	}
	
	private static boolean isHelpArgument(String argument){
		return argument.toLowerCase().equals("-h") || argument.toLowerCase().equals("--help");
	}
	
	private static boolean isVersionArgument(String argument){
		return argument.toLowerCase().equals("-v") || argument.toLowerCase().equals("--version");
	}
}