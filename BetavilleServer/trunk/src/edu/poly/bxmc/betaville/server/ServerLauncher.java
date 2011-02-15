/** Copyright (c) 2008-2010, Brooklyn eXperimental Media Center
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
import edu.poly.bxmc.betaville.server.util.Preferences;

/**
 * Class <Server> - Launcher 
 *
 * @author Caroline Bouchat
 */
public class ServerLauncher {
	
	private static long reportInterval = 600000;
	
	public static final ExecutorService managerPool = Executors.newCachedThreadPool();

	/**
	 * Method <Server> - Launch the server
	 *
	 * @param args Arguments
	 */
	public static void main(final String[] args) {
		try {
			DateFormat.getDateInstance().format(new Date());
			Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n"), DateFormat.getDateInstance().format(new Date())+".log"));
			Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d [%t] %-5p %c %x - %m%n")));
			Logger.getRootLogger().setLevel(Level.INFO);
		} catch (IOException e) {
			System.err.println("Log file coult not be opened for writing!  Please check your user permissions.");
			e.printStackTrace();
		}
		
		// Set up preferences
		Preferences.initialize();
		
		// Create insecure manager
		managerPool.submit(new Runnable(){
			@Override
			public void run() {
				new ServerManager(null, args);
			}});
		
		// Create SSL manager
		managerPool.submit(new Runnable(){
			@Override
			public void run() {
				new SecureServerManager(args);
			}});
		
		// Create report timer
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				ConnectionTracker.generateLogReport();
			}
		}, reportInterval, reportInterval);
	}
}