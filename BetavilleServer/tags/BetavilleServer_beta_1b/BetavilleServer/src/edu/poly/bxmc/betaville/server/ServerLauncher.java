/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.server;

import edu.poly.bxmc.betaville.database.NewDatabaseManager;
import edu.poly.bxmc.betaville.server.network.ServerManager;

/**
 * Class <Server> - Launcher 
 *
 * @author Caroline Bouchat
 */
public class ServerLauncher {

	/**
	 * Method <Server> - Launch the server
	 *
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		NewDatabaseManager DBManager = null;
		if(args.length==0){
			DBManager = new NewDatabaseManager();
		}
		else if(args.length==1){
			DBManager = new NewDatabaseManager(args[0]);
		}
		new ServerManager(DBManager, null);
	}

}