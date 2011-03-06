/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import edu.poly.bxmc.betaville.database.NewDatabaseManager;
import edu.poly.bxmc.betaville.server.Client;
import edu.poly.bxmc.betaville.server.gui.ServerGUI;

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
	private final int serverPort = 14500;
	private static int numberOfClients = 0;

	/**
	 * Constructor - Create the server socket and connect to clients
	 * 
	 * @param manager
	 * @param gui
	 */
	public ServerManager(NewDatabaseManager manager, ServerGUI gui) {
		try {
			// Creation of the server socket
			ServerSocket server = new ServerSocket(serverPort);

			while (true) {
				// Waiting for a incoming client connection request
				Socket socketClient = server.accept();
				if (socketClient.isConnected()) {
					System.out.println("client is connected");
					Client client = new Client(socketClient);

					//gui.getListModel().addElement(client);

					NewClientConnection clientManager = new NewClientConnection(client);
					//ClientConnectionManager clientManager = new ClientConnectionManager(client, manager);
					clientManager.start();
					numberOfClients+=1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addClientToCounter(){
		numberOfClients+=1;
	}
	
	public static void removeClientFromCounter(){
		numberOfClients-=1;
	}
	
	public static int getNumberOfClients(){
		return numberOfClients;
	}
}
