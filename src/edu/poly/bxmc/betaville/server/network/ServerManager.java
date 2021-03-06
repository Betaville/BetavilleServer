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
package edu.poly.bxmc.betaville.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Future;

import edu.poly.bxmc.betaville.server.Client;
import edu.poly.bxmc.betaville.server.ServerLauncher;
import edu.poly.bxmc.betaville.server.util.Preferences;

/**
 * Class <ServerManager> - Manage the connection to the server. For each client
 * connected, creation of a thread which respond to the client's request.
 * 
 * @author Caroline Bouchat
 * @version 0.1 - Spring 2009
 */
public class ServerManager {
	/**
	 * Constant <serverPort> - Port of the server used
	 */
	private final int serverPort = Integer.parseInt(Preferences.getSetting(Preferences.NETWORK_DEFAULT_PORT));
	

	/**
	 * Constructor - Create the server socket and connect to clients
	 * 
	 * @param manager
	 * @param gui
	 */
	public ServerManager(String[] startupArgs) {
		try {
			// Creation of the server socket
			ServerSocket server = new ServerSocket(serverPort);

			while (true) {
				// Waiting for a incoming client connection request
				Socket socketClient = server.accept();
				if (socketClient.isConnected()) {
					Client client = new Client(socketClient);
					NewClientConnection connection = new NewClientConnection(client);
					Future future = ServerLauncher.managerPool.submit(connection);
					String futureKey = ConnectionTracker.addConnection(future);
					connection.setFutureKey(futureKey);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
