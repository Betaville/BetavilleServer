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
package edu.poly.bxmc.betaville.server.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;

import edu.poly.bxmc.betaville.jme.map.UTMCoordinate;
import edu.poly.bxmc.betaville.model.Comment;
import edu.poly.bxmc.betaville.model.Design;
import edu.poly.bxmc.betaville.model.EmptyDesign;
import edu.poly.bxmc.betaville.model.ProposalPermission;
import edu.poly.bxmc.betaville.model.IUser.UserType;
import edu.poly.bxmc.betaville.net.ConnectionCodes;
import edu.poly.bxmc.betaville.net.PhysicalFileTransporter;
import edu.poly.bxmc.betaville.server.Client;
import edu.poly.bxmc.betaville.server.database.DBConst;
import edu.poly.bxmc.betaville.server.database.NewDatabaseManager;
import edu.poly.bxmc.betaville.server.mail.MailSystem;
import edu.poly.bxmc.betaville.server.mail.ShareBetavilleMessage;
import edu.poly.bxmc.betaville.server.session.availability.SessionTracker;
import edu.poly.bxmc.betaville.server.util.Preferences;
import edu.poly.bxmc.betaville.util.StringZipper;
import edu.poly.bxmc.betaville.xml.DataExporter;

/**
 * @author Skye Book (Re-Write and Completion)
 * @author Caroline Bouchat - Laid out and structured
 */
public class NewClientConnection implements Runnable {
	private static final Logger logger = Logger.getLogger(NewClientConnection.class);
	private static final String DELIMITER = "\t";
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

	AtomicBoolean clientIsSafe = new AtomicBoolean(true);

	private String modelBinLocation = Preferences.getSetting(Preferences.STORAGE_MEDIA);

	private long lastRequest = -1;

	private Timer keepAliveTimer = new Timer();
	private int keepAliveLimit = 4*60*1000;

	private boolean sessionStarter = false;

	private boolean sessionOpen = true;

	private String sessionToken = null;

	private String futureKey=null;

	private XMLOutputter xo = new XMLOutputter();

	public NewClientConnection(Client client) {
		this.client = client;
		dbManager = new NewDatabaseManager();

		keepAliveTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if((System.currentTimeMillis()-lastRequest)>(keepAliveLimit)){
					// die
					logger.info("Connection should die now");
					ConnectionTracker.removeConnection(futureKey, false);
					keepAliveTimer.cancel();
				}
			}
		}, 60*1000, 60*1000);
	}

	public void run(){
		logger.info("Client connected from: " + client.getClientSocket().getInetAddress() + " ("+ConnectionTracker.getConnectionCount()+" current)");
		try {
			input = new ObjectInputStream(client.getClientSocket().getInputStream());
			output = new ObjectOutputStream(client.getClientSocket().getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (clientIsSafe.get()) {
			receive();
		}

		// we now deincrement the counter since the network's update loop is broken
		logger.info("Connection from " + client.getClientAdress() + " closing");
		ConnectionTracker.removeConnection(futureKey, true);
		keepAliveTimer.cancel();
	}

	private void closeConnectionFromError() throws IOException{
		if(sessionStarter){
			if(sessionOpen){
				/*
				 * If this is a connection that started the session and the
				 * client has disconnected, then it is safe to say the session
				 * has ended.
				 */
				int sessionID = SessionTracker.get().killSession(sessionToken);
				if(sessionID>0){
					logger.info("Ending Session " + sessionID + " due to the initiating connection being lost");
					dbManager.endSession(sessionID);
					sessionOpen=false;
				}
				else{
					logger.info("Session with token " + sessionToken + " needs to be ended but the sessionID could" +
							"not be found in the SessionTracker");
				}
			}
			else{
				logger.warn("Session with token " + sessionToken + " is trying to be closed again although it no longer" +
						"seems to be open!");
			}
		}
		input.close();
		client.getClientSocket().close();
		clientIsSafe.set(false);
		if(dbManager!=null) dbManager.closeConnection();
	}

	@SuppressWarnings("unchecked")
	private void receive(){
		try {
			Object[] inObject = null;
			try{
				Object in = input.readObject();
				lastRequest=System.currentTimeMillis();
				if(in instanceof Object[]){
					inObject = (Object[])in;
				}
				else if(in instanceof Integer){
					// Process connection codes
					if(((Integer)in)==ConnectionCodes.CLOSE){
						logger.info("Socket from " + client.getClientAdress() + " close requested");
						clientIsSafe.set(false);
						output.close();
						client.getClientSocket().close();
						if(dbManager!=null) dbManager.closeConnection();
						return;
					}
				}
			} catch (IOException e){
				logger.error("Exception Caught While Attempting Read From Client: " + e.getClass().getName(), e);
				closeConnectionFromError();
				return;
			} catch (ClassNotFoundException e) {
				logger.error("Exception Caught While Attempting Read From Client: " + e.getClass().getName(), e);
				closeConnectionFromError();
				return;
			}

			String section = (String)inObject[0];
			String request = ((String)inObject[1]);

			// USER FUNCTIONALITY
			if(section.equals("user")){
				if(request.equals("auth")){
					logger.info(client.getClientAdress()+DELIMITER+"user:auth");
					boolean response = dbManager.authenticateUser((String)inObject[2], (String)inObject[3]);
					output.writeObject(Boolean.toString(response));
				}
				if(request.equals("startsession")){
					logger.info(client.getClientAdress()+DELIMITER+"user:startsession");
					int response = dbManager.startSession((String)inObject[2], (String)inObject[3]);
					// only create a session if the response was valid
					String sessionToken = "";
					if(response>0)sessionToken = SessionTracker.get().addSession(response, (String)inObject[2]).getSessionToken();
					sessionStarter=true;
					this.sessionToken=sessionToken;
					sessionOpen=true;
					output.writeObject(new Object[]{Integer.toString(response),sessionToken});
				}
				if(request.equals("endsession")){
					logger.info(client.getClientAdress()+DELIMITER+"user:endsession");
					logger.info("Attempting end session");
					int sessionID = SessionTracker.get().killSession((String)inObject[2]);
					if(sessionID>0){
						int response = dbManager.endSession(sessionID);
						output.writeObject(Integer.toString(response));
					}
					else output.writeObject(Integer.toString(sessionID));
				}
				else if(request.equals("add")){
					logger.info(client.getClientAdress()+DELIMITER+"user:add");
					// Do not bypass the username requirements since this is a public registration process
					// TODO: Perhaps we should put in a token-authenticated add user method that allows an administrator to bypass these restrictions..
					boolean response = dbManager.addUser((String)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5], (String)inObject[6], false);
					output.writeObject(Boolean.toString(response));
				}
				else if(request.equals("available")){
					logger.info(client.getClientAdress()+DELIMITER+"user:available");
					boolean response = dbManager.checkNameAvailability((String)inObject[2]);
					output.writeObject(Boolean.toString(response));
				}
				else if(request.equals("changepass")){
					logger.info(client.getClientAdress()+DELIMITER+"user:changepass");
					output.writeObject(Boolean.toString(dbManager.changePassword((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(request.equals("changebio")){
					logger.info(client.getClientAdress()+DELIMITER+"user:changebio");
					output.writeObject(Boolean.toString(dbManager.changeBio((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(request.equals("getmail")){
					logger.info(client.getClientAdress()+DELIMITER+"user:getmail");
					output.writeObject(dbManager.getUserEmail((String)inObject[2]));
				}
				else if(request.equals("checklevel")){
					logger.info(client.getClientAdress()+DELIMITER+"user:checklevel");
					output.writeObject(Integer.toString(dbManager.checkUserLevel((String)inObject[2], (UserType)inObject[3])));
				}
				else if(request.equals("getlevel")){
					logger.info(client.getClientAdress()+DELIMITER+"user:getlevel");
					output.writeObject(dbManager.getUserLevel((String)inObject[2]));
				}
			}


			// DESIGN FUNCTIONALITY
			else if(section.equals("design")){
				if(request.equals("synchronizedata")){
					logger.info(client.getClientAdress()+DELIMITER+"design:synchronizedata");
					output.writeObject(dbManager.synchronizeData((HashMap<Integer, Integer>)inObject[2]));
				}
				if(request.equals("addempty")){
					logger.info(client.getClientAdress()+DELIMITER+"design:addempty");
					output.writeObject(Integer.toString(dbManager.addDesign((EmptyDesign)inObject[2], (String)inObject[3], (String)inObject[4], "none")));
				}
				else if(request.equals("addproposal")){
					logger.info(client.getClientAdress()+DELIMITER+"design:addproposal");
					Design design = (Design)inObject[2];
					ProposalPermission permission=(ProposalPermission)inObject[8];
					if(permission!=null) logger.debug("Permissions Received!");
					else logger.debug("Permissions failed!");
					// if the source is linked to an invalid location, we create an empty design
					if(design.getSourceID()==0){
						EmptyDesign ed = new EmptyDesign(design.getCoordinate(), "no address", design.getCityID(),  (String)inObject[3], "none", "none", true, 5, 5);
						int emptyDesignID = dbManager.addDesign(ed, (String)inObject[3], (String)inObject[4], "");
						design.setSourceID(emptyDesignID);
					}

					String extension = new String(design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length()));
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					dbManager.addProposal(design.getSourceID(), designID, (String)inObject[6], permission);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						try{
							if(inObject[7]!=null)((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
							if(inObject[9]!=null) ((PhysicalFileTransporter)inObject[9]).writeToFileSystem(new File(modelBinLocation+"sourcemedia/"+designID+".zip"));
						}catch(ArrayIndexOutOfBoundsException e){
							// the source object was not included
						}
					}
					output.writeObject(Integer.toString(designID));
				}
				else if(request.equals("addbase")){
					logger.info(client.getClientAdress()+DELIMITER+"design:addbase");
					Design design = (Design)inObject[2];
					String extension = new String(design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length()));
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						try{
							if(inObject[6]!=null) ((PhysicalFileTransporter)inObject[6]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
							if(inObject[7]!=null) ((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"sourcemedia/"+designID+".zip"));
						}catch(ArrayIndexOutOfBoundsException e){
							// the source object was not included
						}
					}
					output.writeObject(Integer.toString(designID));
				}
				else if(request.equals("setthumb")){
					logger.info(client.getClientAdress()+DELIMITER+"design:setthumb");
					// check that the supplied username and password is correct
					if(dbManager.authenticateUser((String)inObject[4], (String)inObject[5])){
						int designID = (Integer)inObject[2];
						// ensure that the user has control over the design
						if(dbManager.verifyDesignOwnership(designID, (String)inObject[4], (String)inObject[5]) || dbManager.getUserLevel((String)inObject[4]).compareTo(UserType.MODERATOR)>-1){
							PhysicalFileTransporter pft = (PhysicalFileTransporter)inObject[3];
							pft.writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
							output.writeObject(Integer.toString(0));
						}
						else{
							output.writeObject(Integer.toString(-1));
						}
					}
				}
				else if(request.equals("changename")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changename");
					output.writeObject(Boolean.toString(dbManager.changeDesignName((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(request.equals("changedescription")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changedescription");
					output.writeObject(Boolean.toString(dbManager.changeDesignDescription((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(request.equals("changeaddress")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changeaddress");
					output.writeObject(Boolean.toString(dbManager.changeDesignAddress((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(request.equals("changeurl")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changeurl");
					output.writeObject(Boolean.toString(dbManager.changeDesignURL((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(request.equals("changemodellocation")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changemodellocation");
					output.writeObject(Boolean.toString(dbManager.changeModeledDesignLocation((Integer)inObject[2], (Float)inObject[4], (UTMCoordinate)inObject[3], (String)inObject[5], (String)inObject[6])));
				}
				else if(request.equals("findbyid")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findbyid");
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					output.writeObject(design);
				}
				else if(request.equals("findbyname")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findbyname");
					output.writeObject(dbManager.findDesignsByName((String)inObject[2]));
				}
				else if(request.equals("findbyuser")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findbyuser");
					output.writeObject(dbManager.findDesignsByUser((String)inObject[2]));
				}
				else if(request.equals("findbydate")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findbydate");
					output.writeObject(dbManager.findDesignsByDate((Long)inObject[2]));
				}
				else if(request.equals("findbycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findbycity");
					output.writeObject(dbManager.findDesignsByCity((Integer)inObject[2], (Boolean)inObject[3]));
				}
				else if(request.equals("terrainbycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:terrainbycity");
					output.writeObject(dbManager.findTerrainDesignsByCity((Integer)inObject[2]));
				}
				else if(request.equals("findmodeledbycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findmodeledbycity");
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_MODEL));
				}
				else if(request.equals("findaudiobycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findaudiobycity");
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_AUDIO));
				}
				else if(request.equals("findimagebycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findimagebycity");
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_SKETCH));
				}
				else if(request.equals("findvideobycity")){
					logger.info(client.getClientAdress()+DELIMITER+"design:findvideobycity");
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_VIDEO));
				}
				else if(request.equals("allproposals")){
					logger.info(client.getClientAdress()+DELIMITER+"design:allproposals");
					output.writeObject(dbManager.findAllProposals((Integer)inObject[2]));
				}
				else if(request.equals("requestfile")){
					logger.info(client.getClientAdress()+DELIMITER+"design:requestfile");
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					// send out a PFT if http file storage is disabled
					if(Boolean.parseBoolean(System.getProperty(Preferences.HTTP_STORAGE_ENABLED))){
						URL fileURL = new URL(System.getProperty(Preferences.HTTP_STORAGE_LOCATION+design.getFilepath()));
						output.writeObject(fileURL);
					}
					else{
						output.writeObject(wrapFile(design));
					}
				}
				else if(request.equals("requestthumb")){
					logger.info(client.getClientAdress()+DELIMITER+"design:requestthumb");
					output.writeObject(wrapThumbnail((Integer)inObject[2]));
				}
				else if(request.equals("changefile")){
					logger.info(client.getClientAdress()+DELIMITER+"design:changefile");
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					String currentFile = new String(design.getFilepath().substring(0, design.getFilepath().lastIndexOf(".")));
					String newFilename=null;
					if(currentFile.contains("_")){
						int currentIteration = Integer.parseInt(new String(currentFile.substring(currentFile.lastIndexOf("_")+1, currentFile.length())));
						newFilename = design.getID()+"_"+(currentIteration+1)+".jme";
					}
					else{
						newFilename=design.getID()+"_"+1+".jme";
					}
					PhysicalFileTransporter pft = (PhysicalFileTransporter)inObject[6];
					pft.writeToFileSystem(new File(modelBinLocation+"designmedia/"+newFilename));
					output.writeObject(Boolean.toString(dbManager.changeDesignFile(design.getID(), newFilename, (String)inObject[3], (String)inObject[4], (Boolean)inObject[5])));


					try{
						if(inObject[7]!=null) ((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"sourcemedia/"+newFilename.replace(".jme", ".zip")));
					}catch(ArrayIndexOutOfBoundsException e){
						// the source object was not included
					}
				}
				else if(request.equals("reserve")){
					logger.info(client.getClientAdress()+DELIMITER+"design:reserve");
					Design design = (Design)inObject[2];
					design.setPublic(false);
					String extension = new String(design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length()));
					output.writeObject(dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension));
				}
				else if(request.equals("remove")){
					int designID = (Integer)inObject[2];
					String user = (String)inObject[3];
					String pass = (String)inObject[4];
					int response = dbManager.removeDesign(designID, user, pass);
					if(response==0) logger.info(client.getClientAdress()+DELIMITER+"design:remove"+DELIMITER+designID+DELIMITER+user);
					output.writeObject(Integer.toString(response));
				}
				else if(request.equals("synchronize")){
					logger.info(client.getClientAdress()+DELIMITER+"design:synchronize");
					int[] hashes = (int[])inObject[2];
					int[] idList = new int[hashes.length/2];
					for(int i=0; i<hashes.length; i+=2){
						if(i==0){
							idList[i]=hashes[i];
						}
						else{
							idList[i/2]=hashes[i];
						}
					}
					ArrayList<Design> returnable = new ArrayList<Design>();
					ArrayList<Design> designs = dbManager.findMultipleDesignsByID(idList);
					Iterator<Design> it = designs.iterator();
					int cycle=0;
					while(it.hasNext()){
						Design d = it.next();
						if(hashes[cycle]==d.getID()){
							cycle++;
							if(d.hashCode()!=hashes[cycle]){
								returnable.add(d);
							}
							cycle++;
						}
					}
					output.writeObject(returnable);
				}
			}

			// DESIGN FUNCTIONALITY
			else if(section.equals("design-android")){
				androidDesign(request, inObject);
			}

			// PROPOSAL FUNCTIONALITY
			else if(section.equals("proposal")){
				if(request.equals("findinradius")){
					logger.info(client.getClientAdress()+DELIMITER+"proposal:findinradius");
					output.writeObject(dbManager.findAllProposalsInArea((UTMCoordinate)inObject[2], (Integer)inObject[3]));
				}
				if(request.equals("getpermissions")){
					logger.info(client.getClientAdress()+DELIMITER+"proposal:getpermissions");
					output.writeObject(dbManager.getProposalPermissions((Integer)inObject[2]));
				}
				else if(request.equals("addversion")){
					logger.info(client.getClientAdress()+DELIMITER+"proposal:addversion");
					Design design = (Design)inObject[2];
					String extension = new String(design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length()));
					System.out.print("filepath being added: " + design.getFilepath());
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					dbManager.addVersion(design.getSourceID(), designID, (String)inObject[6]);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						try{
							if(inObject[7]!=null)((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
							if(inObject[8]!=null) ((PhysicalFileTransporter)inObject[8]).writeToFileSystem(new File(modelBinLocation+"sourcemedia/"+designID+".zip"));
						}catch(ArrayIndexOutOfBoundsException e){
							// the source object was not included
						}
					}
					output.writeObject(Integer.toString(designID));
				}
			}

			// VERSION FUNCTIONALITY
			else if(section.equals("version")){
				if(request.equals("versionsofproposal")){
					logger.info(client.getClientAdress()+DELIMITER+"version:versionsofproposal");
					output.writeObject(dbManager.findVersionsOfProposal((Integer)inObject[2]));
				}
			}

			// FAVE FUNCTIONALITY
			else if(section.equals("fave")){
				if(request.equals("add")){
					logger.info(client.getClientAdress()+DELIMITER+"fave:add");
					output.writeObject(Integer.toString(dbManager.faveDesign((String)inObject[2], (String)inObject[3], (Integer)inObject[4])));
				}
				else if(request.equals("remove")){
					logger.info(client.getClientAdress()+DELIMITER+"fave:remove");
					// TODO implement this
				}
			}

			// ACTIVITY FUNCTIONALITY
			else if(section.equals("activity")){
				if(request.equals("comments")){
					logger.info(client.getClientAdress()+DELIMITER+"activity:comments");
					output.writeObject(dbManager.retrieveRecentComments());
				}
				else if(request.equals("designs")){
					logger.info(client.getClientAdress()+DELIMITER+"activity:designs");
					output.writeObject(dbManager.retrieveRecentDesignIDs());
				}
				else if(request.equals("myactivity")){
					logger.info(client.getClientAdress()+DELIMITER+"activity:myactivity");
					output.writeObject(dbManager.retrieveCommentsOnMyActivity(SessionTracker.get().getSession((String)inObject[2])));

				}
			}


			//SHARE FUNCTIONALITY
			else if(section.equals("share")){
				logger.info(client.getClientAdress()+DELIMITER+"share");
				if (Preferences.getBooleanSetting(Preferences.MAIL_ENABLED)){
					logger.info("Share requested");
					// This is an example on how to use the mailer!
					try {
						MimeMessage message = new ShareBetavilleMessage(MailSystem.getMailer().getSession(), request, ((String)inObject[2]), ((String)inObject[3]), ((String)inObject[4]));
						message.setFrom(new InternetAddress("notifications@betaville.net"));
						MailSystem.getMailer().sendMailNow(message);
						output.writeObject(new Object[]{Integer.toString(1)});
					} catch (Exception e) {
						output.writeObject(new Object[]{Integer.toString(-1)});
						logger.error("Share Failed", e);
					}
				} else{
					logger.error("Mail is not enabled");
					output.writeObject(new Object[]{Integer.toString(-4)});
				}
			}


			// COMMENT FUNCTIONALITY
			else if(section.equals("comment")){
				if(request.equals("add")){
					logger.info(client.getClientAdress()+DELIMITER+"comment:add");
					output.writeObject(Boolean.toString(dbManager.addComment((Comment)inObject[2], (String)inObject[3])));
				}
				if(request.equals("delete")){
					logger.info(client.getClientAdress()+DELIMITER+"comment:delete");
					output.writeObject(Boolean.toString(dbManager.deleteComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				if(request.equals("reportspam")){
					logger.info(client.getClientAdress()+DELIMITER+"comment:reportspam");
					dbManager.reportSpamComment((Integer)inObject[2]);
				}
				if(request.equals("getforid")){
					logger.info(client.getClientAdress()+DELIMITER+"comment:getforid");
					output.writeObject(dbManager.getComments((Integer)inObject[2]));
				}
			}

			// ANDROID COMMENT FUNCTIONALITY
			else if(section.equals("comment-android")){
				androidComment(request, inObject);
			}

			// CITY FUNCTIONALITY
			else if(section.equals("city")){
				if(request.equals("add")){
					logger.info(client.getClientAdress()+DELIMITER+"city:add");
					output.writeObject(Integer.toString(dbManager.addCity((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(request.equals("findbyname")){
					logger.info(client.getClientAdress()+DELIMITER+"city:findbyname");
					output.writeObject(dbManager.findCitiesByName((String)inObject[2]));
				}
				else if(request.equals("findbystate")){
					logger.info(client.getClientAdress()+DELIMITER+"city:findbystate");
					output.writeObject(dbManager.findCitiesByState((String)inObject[2]));
				}
				else if(request.equals("findbycountry")){
					logger.info(client.getClientAdress()+DELIMITER+"city:findbycountry");
					output.writeObject(dbManager.findCitiesByCountry((String)inObject[2]));
				}
				else if(request.equals("findbyid")){
					logger.info(client.getClientAdress()+DELIMITER+"city:findbyid");
					output.writeObject(dbManager.findCityByID((Integer)inObject[2]));
				}
				else if(request.equals("findbyall")){
					logger.info(client.getClientAdress()+DELIMITER+"city:findbyall");
					output.writeObject(dbManager.findCityByAll((String)inObject[2], (String)inObject[3], (String)inObject[4]));
				}
				else if(request.equals("getall")){
					logger.info(client.getClientAdress()+DELIMITER+"city:getall");
					output.writeObject(dbManager.findAllCities());
				}
			}

			// WORMHOLES
			else if(section.equals("wormhole")){
				if(request.equals("add")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:add");
					logger.info("Adding Wormhole");
					output.writeObject(Integer.toString(dbManager.addWormhole((UTMCoordinate)inObject[2], (String)inObject[3], (Integer)inObject[4], (String)inObject[5])));
				}
				else if(request.equals("delete")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:delete");
					output.writeObject(Integer.toString(dbManager.deleteWormhole((Integer)inObject[2], (String)inObject[3])));
				}
				else if(request.equals("editname")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:editname");
					output.writeObject(Integer.toString(dbManager.changeWormholeName((String)inObject[2], (Integer)inObject[3], (String)inObject[4])));
				}
				else if(request.equals("editlocation")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:editlocation");
					output.writeObject(Integer.toString(dbManager.changeWormholeLocation((UTMCoordinate)inObject[2], (Integer)inObject[3], (String)inObject[4])));
				}
				else if(request.equals("getwithin")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:getwithin");
					output.writeObject(dbManager.getWormholesWithin((UTMCoordinate)inObject[2], (Integer)inObject[3], (Integer)inObject[4]));
				}
				else if(request.equals("getall")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:getall");
					output.writeObject(dbManager.getAllWormholes());
				}
				else if(request.equals("getallincity")){
					logger.info(client.getClientAdress()+DELIMITER+"wormhole:getallincity");
					output.writeObject(dbManager.getAllWormholesInCity((Integer)inObject[2]));
				}
			}

			// VERSION ENFORCEMENT
			else if(section.equals("softwareversion")){
				if(request.equals("getdesign")){
					logger.info(client.getClientAdress()+DELIMITER+"softwareversion:getdesign");
					output.writeObject(Long.toString(Design.serialVersionUID));
				}
			}

			// clear references held by the output stream
			output.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void androidDesign(String request, Object[] inObject) throws IOException{
		logger.info("Inside Android Design: " + (String)inObject[1]);
		if(request.equals("changename")){
			output.writeObject(Boolean.toString(dbManager.changeDesignName((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(request.equals("changedescription")){
			output.writeObject(Boolean.toString(dbManager.changeDesignDescription((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(request.equals("changeaddress")){
			output.writeObject(Boolean.toString(dbManager.changeDesignAddress((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(request.equals("changeurl")){
			output.writeObject(Boolean.toString(dbManager.changeDesignURL((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(request.equals("changemodellocation")){
			output.writeObject(Boolean.toString(dbManager.changeModeledDesignLocation((Integer)inObject[2], (Integer)inObject[4], (UTMCoordinate)inObject[3], (String)inObject[5], (String)inObject[6])));
		}
		else if(request.equals("findbyid")){
			logger.info("finding "+(Integer)inObject[2]);
			Design design = dbManager.findDesignByID((Integer)inObject[2]);
			logger.info("design " + design.getName());
			logger.info("\n"+xo.outputString(DataExporter.export(design)));
			output.writeObject(xo.outputString(DataExporter.export(design)));
		}
		else if(request.equals("findbyname")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByName((String)inObject[2]))));
		}
		else if(request.equals("findbyuser")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByUser((String)inObject[2]))));
		}
		else if(request.equals("findbydate")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByDate((Long)inObject[2]))));
		}
		else if(request.equals("findbycity")){
			List<Design> designs = dbManager.findDesignsByCity((Integer)inObject[2], (Boolean)inObject[3]);
			logger.info(designs.size()+" designs retrieved");
			String xmlResponse = xo.outputString(DataExporter.exportDesigns(designs));
			logger.info("Responding with: " + xmlResponse);
			output.writeObject(xmlResponse);
		}
		else if(request.equals("findbycitysetstartend")){
			List<Design> designs = dbManager.findDesignsByCitySetStartEnd((Integer)inObject[2], (Boolean)inObject[3],
					(Integer)inObject[4], (Integer)inObject[5]);
			logger.info(designs.size()+" designs retrieved");
			String xmlResponse = xo.outputString(DataExporter.exportDesigns(designs));
			logger.info("Responding with: " + xmlResponse);
			output.writeObject(StringZipper.compress(xmlResponse));
		}
		else if(request.equals("terrainbycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTerrainDesignsByCity((Integer)inObject[2]))));
		}
		else if(request.equals("findmodeledbycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_MODEL))));
		}
		else if(request.equals("findaudiobycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_AUDIO))));
		}
		else if(request.equals("findimagebycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_SKETCH))));
		}
		else if(request.equals("findvideobycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_VIDEO))));
		}
		else if(request.equals("allproposals")){
			output.writeObject(dbManager.findAllProposals((Integer)inObject[2]));
		}
		else if(request.equals("requestthumb")){
			output.writeObject(wrapThumbnail((Integer)inObject[2]));
		}
		else if(request.equals("remove")){
			int designID = (Integer)inObject[2];
			String user = (String)inObject[3];
			String pass = (String)inObject[4];
			output.writeObject(Integer.toString(dbManager.removeDesign(designID, user, pass)));
		}
	}

	private void androidComment(String request, Object[] inObject) throws IOException{
		if(request.equals("add")){// TODO: NOT ANDROID SAFE YET
			output.writeObject(Boolean.toString(dbManager.addComment((Comment)inObject[2], (String)inObject[3])));
		}
		if(request.equals("delete")){// TODO: NOT ANDROID SAFE YET
			output.writeObject(Boolean.toString(dbManager.deleteComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4])));
		}
		if(request.equals("reportspam")){// TODO: NOT ANDROID SAFE YET
			dbManager.reportSpamComment((Integer)inObject[2]);
		}
		if(request.equals("getforid")){
			output.writeObject(xo.outputString(DataExporter.exportComments(dbManager.getComments((Integer)inObject[2]))));
		}
	}

	public void setFutureKey(String key){
		futureKey=key;
	}

	public String getFutureKey(){
		return futureKey;
	}

	private PhysicalFileTransporter wrapFile(Design design){
		// empty designs don't have files associated with them
		if(design instanceof EmptyDesign) return null;

		File designFile = new File(modelBinLocation+"designmedia/"+design.getFilepath());
		FileInputStream fis;
		try {
			fis = new FileInputStream(designFile);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			PhysicalFileTransporter transport = new PhysicalFileTransporter(b);
			fis.close();
			return transport;
		} catch (FileNotFoundException e) {
			logger.debug("Error getting file: " + design.getFilepath());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PhysicalFileTransporter wrapThumbnail(int designID){
		File designFile = new File(modelBinLocation+"designthumbs/"+designID+".png");
		if(!designFile.exists()) return null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(designFile);
			byte[] b = new byte[fis.available()];
			fis.read(b);
			PhysicalFileTransporter transport = new PhysicalFileTransporter(b);
			fis.close();
			return transport;
		} catch (FileNotFoundException e) {
			logger.debug("Thumbnail does not exist for " + designID +" but Java reports that the file exists", e);
		} catch (IOException e) {
			logger.debug("Error reading thumbnail for file: " + designID, e);
		}
		return null;
	}
}
