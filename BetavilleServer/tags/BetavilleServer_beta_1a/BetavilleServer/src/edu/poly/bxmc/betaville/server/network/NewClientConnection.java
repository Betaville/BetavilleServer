/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.server.network;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

import edu.poly.bxmc.betaville.database.NewDatabaseManager;
import edu.poly.bxmc.betaville.model.Design;
import edu.poly.bxmc.betaville.model.UTMCoordinate;
import edu.poly.bxmc.betaville.net.PhysicalFileTransporter;
import edu.poly.bxmc.betaville.server.Client;

/**
 * @author Skye Book (Re-Write and Completion)
 * @author Caroline Bouchat - Laid out and structured
 */
public class NewClientConnection extends Thread {
	/**
	 * Attribute <DBManager> - Database manager
	 */
	private NewDatabaseManager dbManager;
	/**
	 * Attribute <input> - Input of the socket
	 */
	private ObjectInputStream input;
	/**
	 * Attribute <output> - Output of the socket
	 */
	private ObjectOutputStream output;
	/**
	 * Attribute <client> - The client
	 */
	private Client client;

	boolean clientIsSafe = true;
	
	String modelBinLocation = "bin/";

	/**
	 * 
	 */
	public NewClientConnection(Client client) {
		this.client = client;
		dbManager = new NewDatabaseManager();
	}

	public void run(){
		super.run();
		System.out.println("Client connected from: " + client.getClientSocket().getLocalAddress());

		try {
			input = new ObjectInputStream(client.getClientSocket().getInputStream());
			output = new ObjectOutputStream(client.getClientSocket().getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (clientIsSafe) {
			receive();
		}
	}

	private void receive(){
		try {
			Object[] inObject = null;
			try{
				inObject = (Object[])input.readObject();
			} catch (EOFException e){
				System.out.println("Client unexpectedly disconnected.  Closing socket");
				input.close();
				client.getClientSocket().close();
				ServerManager.removeClientFromCounter();
				dbManager.closeConnection();
				clientIsSafe=false;
				return;
			} catch (SocketException e){
				System.out.println("Client unexpectedly disconnected.  Closing socket");
				input.close();
				client.getClientSocket().close();
				ServerManager.removeClientFromCounter();
				dbManager.closeConnection();
				clientIsSafe=false;
				return;
			}
			
			// USER FUNCTIONALITY
			if(((String)inObject[0]).equals("user")){
				if(((String)inObject[1]).equals("add")){
					System.out.println("adding user");
					boolean response = dbManager.addUser((String)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5], (String)inObject[6]);
					output.writeObject(Boolean.toString(response));
				}
				else if(((String)inObject[1]).equals("available")){
					boolean response = dbManager.checkNameAvailability((String)inObject[2]);
					output.writeObject(Boolean.toString(response));
				}
				else if(((String)inObject[1]).equals("changepass")){
					System.out.println("changing password");
					output.writeObject(Boolean.toString(dbManager.changePassword((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("changebio")){
					System.out.println("changing bio");
					output.writeObject(Boolean.toString(dbManager.changeBio((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("getmail")){
					System.out.println("getting email");
					output.writeObject(dbManager.getUserEmail((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("auth")){
					System.out.println("authenticating user");
					output.writeObject(Boolean.toString(dbManager.authenticateUser((String)inObject[2], (String)inObject[3])));
				}
			}
			
			
			// DESIGN FUNCTIONALITY
			else if(((String)inObject[0]).equals("design")){
				if(((String)inObject[1]).equals("add")){
					Design design = (Design)inObject[2];
					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					System.out.print("filepath being added: " + design.getFilepath());
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], (Integer)inObject[5], extension);
					if(designID>0){
						((PhysicalFileTransporter)inObject[6]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
					}
					output.writeObject(Integer.toString(designID));
				}
				if(((String)inObject[1]).equals("changename")){
					output.writeObject(Boolean.toString(dbManager.changeDesignName((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				if(((String)inObject[1]).equals("changedescription")){
					output.writeObject(Boolean.toString(dbManager.changeDesignDescription((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				if(((String)inObject[1]).equals("changemodellocation")){
					output.writeObject(Boolean.toString(dbManager.changeModeledDesignLocation((Integer)inObject[2], (Integer)inObject[4], (UTMCoordinate)inObject[3], (String)inObject[5], (String)inObject[6])));
				}
				if(((String)inObject[1]).equals("findbyid")){
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					output.writeObject(new Object[]{design, wrapFile(design)});
				}
				if(((String)inObject[1]).equals("findbyname")){
					output.writeObject(dbManager.findDesignsByName((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbyuser")){
					output.writeObject(dbManager.findDesignsByUser((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbydate")){
					output.writeObject(dbManager.findDesignsByDate((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbycity")){
					output.writeObject(dbManager.findDesignsByCity((Integer)inObject[2]));
				}
				if(((String)inObject[1]).equals("newerproposal")){
					output.writeObject(Integer.toString(dbManager.findNewerProposal((Integer)inObject[2])));
				}
				if(((String)inObject[1]).equals("olderproposal")){
					output.writeObject(Integer.toString(dbManager.findOlderProposal((Integer)inObject[2])));
				}
				if(((String)inObject[1]).equals("requestfile")){
					System.out.println("looking for: " + (Integer)inObject[2]);
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					output.writeObject(wrapFile(design));
				}
				if(((String)inObject[1]).equals("reserve")){
					Design design = (Design)inObject[2];
					design.setPublic(false);
					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					output.writeObject(dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], (Integer)inObject[5], extension));
				}
				if(((String)inObject[1]).equals("remove")){
					int designID = (Integer)inObject[2];
					String user = (String)inObject[3];
					String pass = (String)inObject[4];
					output.writeObject(Integer.toString(dbManager.removeDesign(designID, user, pass)));
				}
			}
			
			
			// VOTE FUNCTIONALITY
			else if(((String)inObject[0]).equals("vote")){
				if(((String)inObject[1]).equals("add")){
					dbManager.addVote((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (Boolean)inObject[5]);
				}
				if(((String)inObject[1]).equals("change")){
					dbManager.changeVote((Integer)inObject[2], (String)inObject[3], (String)inObject[4]);
				}
			}
			
			
			// COMMENT FUNCTIONALITY
			else if(((String)inObject[0]).equals("comment")){
				if(((String)inObject[1]).equals("add")){
					dbManager.addComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5]);
				}
				if(((String)inObject[1]).equals("delete")){
					output.writeObject(Boolean.toString(dbManager.deleteComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				if(((String)inObject[1]).equals("reportspam")){
					dbManager.reportSpamComment((Integer)inObject[2]);
				}
			}
			
			
			// COORDINATE FUNCTIONALITY -- I'm not sure we need to have this accessible from the outside now that I'm thinking of it
			else if(((String)inObject[0]).equals("coordinate")){}
			
			
			// CITY FUNCTIONALITY
			else if(((String)inObject[0]).equals("city")){
				if(((String)inObject[1]).equals("add")){
					output.writeObject(Integer.toString(dbManager.addCity((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				if(((String)inObject[1]).equals("findbyname")){
					output.writeObject(dbManager.findCitiesByName((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbystate")){
					output.writeObject(dbManager.findCitiesByState((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbycountry")){
					output.writeObject(dbManager.findCitiesByCountry((String)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbyid")){
					output.writeObject(dbManager.findCityByID((Integer)inObject[2]));
				}
				if(((String)inObject[1]).equals("findbyall")){
					output.writeObject(dbManager.findCityByAll((String)inObject[2], (String)inObject[3], (String)inObject[4]));
				}
			}
			
			// MODERATION FUNCTIONALITY
			else if(((String)inObject[0]).equals("moderation")){
				if(((String)inObject[1]).equals("addadmin")){
					output.writeObject(Boolean.toString(dbManager.addAdministrator((String)inObject[2])));
				}
				if(((String)inObject[1]).equals("checkadmin")){
					output.writeObject(Boolean.toString(dbManager.checkAdminStatus((String)inObject[2])));
				}
				if(((String)inObject[1]).equals("addmod")){
					output.writeObject(Boolean.toString(dbManager.addModerator((String)inObject[2])));
				}
				if(((String)inObject[1]).equals("checkmod")){
					output.writeObject(Boolean.toString(dbManager.checkModeratorStatus((String)inObject[2])));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private PhysicalFileTransporter wrapFile(Design design){
		System.out.println("getting: " + design.getFilepath());
		File designFile = new File(modelBinLocation+"designmedia/"+design.getFilepath());
		FileInputStream fis;
		try {
			fis = new FileInputStream(designFile);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			PhysicalFileTransporter transport = new PhysicalFileTransporter(b);
			return transport;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
