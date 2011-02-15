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
package edu.poly.bxmc.betaville.server.network;

import org.apache.log4j.Logger;

/**
 * Keeps track of the number of clients currently connected.
 * @author Skye Book
 *
 */
public class ConnectionTracker {
	private static final Logger logger = Logger.getLogger(ConnectionTracker.class);
	
	// tracks the current number of connections
	private static int connectionCount = 0;
	
	// tracks the total number of connections during the lifetime of this server instance
	private static int totalConnectionCount = 0;

	private ConnectionTracker(){}
	
	public static void incrementConnectionCount(){
		totalConnectionCount++;
		connectionCount++;
	}
	
	public static void deincrementConnectionCount(){
		connectionCount--;
	}
	
	public static int getConnectionCount(){
		return connectionCount;
	}
	
	public static int getTotalConnectionCount(){
		return totalConnectionCount;
	}
	
	/**
	 * Prints to the logger the current amount of connections as well as the connections since startup
	 */
	public static synchronized void generateLogReport(){
		logger.info("Current Connections: " + connectionCount + " | Connections Since Start: " + totalConnectionCount);
	}
}
