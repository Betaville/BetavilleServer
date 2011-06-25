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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.Future;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import edu.poly.bxmc.betaville.server.Client;
import edu.poly.bxmc.betaville.server.ServerLauncher;

/**
 * @author Skye Book
 *
 */
public class SecureServerManager {
	private final int serverPort = 14501;
	
	private String pass=null;
	private char[] keyStorePass;
	private char[] trustStorePass;

	/**
	 * 
	 */
	public SecureServerManager(String[] startupArgs) {
		if(startupArgs.length==0){
			pass=null;
		}else{
			pass=startupArgs[0];
		}
		try {
			
			keyStorePass = "123456".toCharArray();
			trustStorePass = "123456".toCharArray();
			
			KeyStore keyStore = KeyStore.getInstance("JKS");
			KeyStore trustStore = KeyStore.getInstance("JKS");
			
			keyStore.load(new FileInputStream(new File("certs/server.keystore")), keyStorePass);
			trustStore.load(new FileInputStream(new File("certs/server.truststore")), trustStorePass);
			
			KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
			keyManager.init(keyStore, keyStorePass);
			TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
			trustManager.init(trustStore);
			
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
			
			
			SSLServerSocketFactory sslFactory = context.getServerSocketFactory();
			SSLServerSocket server = (SSLServerSocket)sslFactory.createServerSocket(serverPort);
			
			while (true) {
				// Waiting for a incoming client connection request
				Socket socketClient = server.accept();
				if (socketClient.isConnected()) {
					Client client = new Client(socketClient);NewClientConnection connection = new NewClientConnection(client, pass);
					Future future = ServerLauncher.managerPool.submit(connection);
					String futureKey = ConnectionTracker.addConnection(future);
					connection.setFutureKey(futureKey);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
