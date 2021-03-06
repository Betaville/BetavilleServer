/** Copyright (c) 2008-2012, Brooklyn eXperimental Media Center
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

import edu.poly.bxmc.betaville.server.mail.AbstractMailer;
import edu.poly.bxmc.betaville.server.mail.FullDetailMailer;
import edu.poly.bxmc.betaville.server.mail.MailSystem;
import edu.poly.bxmc.betaville.server.network.ConnectionTracker;
import edu.poly.bxmc.betaville.server.network.SecureServerManager;
import edu.poly.bxmc.betaville.server.network.ServerManager;
import edu.poly.bxmc.betaville.server.session.availability.InMemorySessionTracker;
import edu.poly.bxmc.betaville.server.session.availability.SessionTracker;
import edu.poly.bxmc.betaville.server.util.Preferences;
import edu.poly.bxmc.betaville.util.OS;

/**
 * Class <Server> - Launcher 
 *
 * @author Skye Book
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
		
		// Now that the defaults and config file are loaded, add anything set in the system env vars
		Preferences.overlayPreferencesFromEnvironment();

		// Set up logging
		try {
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n")));
			Logger.getRootLogger().setLevel(Level.INFO);

			logger = Logger.getLogger(ServerLauncher.class);

			DateFormat.getDateInstance().format(new Date());
			// check if the folder for the logs to go into exists;  Create it if it doesn't
			File loggingDir = new File(Preferences.getSetting(Preferences.STORAGE_LOGGING));
			if(!loggingDir.exists()){
				if(loggingDir.mkdirs()){
					logger.info("Logging directory created");
				}
				else logger.error("The logging directory could not be created, logs will not be written to the file system");
			}


			if(loggingDir.exists()){
				Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n"),
						Preferences.getSetting(Preferences.STORAGE_LOGGING)+DateFormat.getDateInstance().format(new Date())+".log"));
			}
		} catch (IOException e) {
			System.err.println("Log file coult not be opened for writing!  Please check your user permissions.");
			e.printStackTrace();
		}

		// Create lock file (only for Unix based systems)
		if(System.getProperty("pid")!=null && !OS.isWindows()){
			logger.info("Running on pid "+System.getProperty("pid"));
			try {
				PrintWriter writer = new PrintWriter(new File("BetavilleServer.lock"));
				writer.write(System.getProperty("pid"));
				writer.flush();
				writer.close();
			} catch (FileNotFoundException e) {
				logger.error("Could not write PID file, update scripts will not be able to be used", e);
			}

		}

		// Set up the mailer if it is enabled
		if(Preferences.getBooleanSetting(Preferences.MAIL_ENABLED)){
			try {
				AbstractMailer mailer = new FullDetailMailer(Preferences.getSetting(Preferences.MAIL_HOST),
						Preferences.getSetting(Preferences.MAIL_USER),
						Preferences.getSetting(Preferences.MAIL_PASS),
						Preferences.getIntegerSetting(Preferences.MAIL_PORT),
						Preferences.getBooleanSetting(Preferences.MAIL_STARTTLS),
						Preferences.getBooleanSetting(Preferences.MAIL_REQUIRES_AUTH));
				MailSystem.registerMailer(mailer);
			} catch (Exception e) {
				logger.error("The mailer could not be setup, disabling it for this session", e);
				Preferences.setBooleanSetting(Preferences.MAIL_ENABLED, false);
			}
		}

		// Set up session tracker
		try {
			@SuppressWarnings("unchecked")
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

		// check for the existence of storage folders

		String modelBinLocation = Preferences.getSetting(Preferences.STORAGE_MEDIA);
		File designMedia = new File(modelBinLocation+"designmedia/");
		if(!designMedia.exists()){
			logger.info("Design Media folder not found, creating it");
			designMedia.mkdirs();
		}
		File designThumbs = new File(modelBinLocation+"designthumbs/");
		if(!designThumbs.exists()){
			logger.info("Design Thumbnail folder not found, creating it");
			designThumbs.mkdirs();
		}
		File sourceMedia = new File(modelBinLocation+"sourcemedia/");
		if(!sourceMedia.exists()){
			logger.info("Source Media folder not found, creating it");
			sourceMedia.mkdirs();
		}

		// Create insecure manager
		managerPool.submit(new Runnable(){
			@Override
			public void run() {
				new ServerManager(args);
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