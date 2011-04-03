/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.server;

import java.net.Socket;

public class Client {
	private String clientName;

	private Socket clientSocket;
	
	public Client(Socket socket) {
		this.clientName = "Anonymous";
		this.clientSocket = socket;
	}

	public String getClientName() {
		return clientName;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
}