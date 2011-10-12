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

		while (clientIsSafe) {
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
		clientIsSafe=false;
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
						clientIsSafe=false;
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



			// USER FUNCTIONALITY
			if(((String)inObject[0]).equals("user")){
				if(((String)inObject[1]).equals("auth")){
					boolean response = dbManager.authenticateUser((String)inObject[2], (String)inObject[3]);
					output.writeObject(Boolean.toString(response));
				}
				if(((String)inObject[1]).equals("startsession")){
					int response = dbManager.startSession((String)inObject[2], (String)inObject[3]);
					// only create a session if the response was valid
					String sessionToken = "";
					if(response>0)sessionToken = SessionTracker.get().addSession(response, (String)inObject[2]).getSessionToken();
					sessionStarter=true;
					this.sessionToken=sessionToken;
					sessionOpen=true;
					output.writeObject(new Object[]{Integer.toString(response),sessionToken});
				}
				if(((String)inObject[1]).equals("endsession")){
					logger.info("Attempting end session");
					int sessionID = SessionTracker.get().killSession((String)inObject[2]);
					if(sessionID>0){
						int response = dbManager.endSession(sessionID);
						output.writeObject(Integer.toString(response));
					}
					else output.writeObject(Integer.toString(sessionID));
				}
				else if(((String)inObject[1]).equals("add")){
					// Do not bypass the username requirements since this is a public registration process
					// TODO: Perhaps we should put in a token-authenticated add user method that allows an administrator to bypass these restrictions..
					boolean response = dbManager.addUser((String)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5], (String)inObject[6], false);
					output.writeObject(Boolean.toString(response));
				}
				else if(((String)inObject[1]).equals("available")){
					boolean response = dbManager.checkNameAvailability((String)inObject[2]);
					output.writeObject(Boolean.toString(response));
				}
				else if(((String)inObject[1]).equals("changepass")){
					output.writeObject(Boolean.toString(dbManager.changePassword((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("changebio")){
					output.writeObject(Boolean.toString(dbManager.changeBio((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("getmail")){
					output.writeObject(dbManager.getUserEmail((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("checklevel")){
					output.writeObject(Integer.toString(dbManager.checkUserLevel((String)inObject[2], (UserType)inObject[3])));
				}
				else if(((String)inObject[1]).equals("getlevel")){
					output.writeObject(dbManager.getUserLevel((String)inObject[2]));
				}
			}


			// DESIGN FUNCTIONALITY
			else if(((String)inObject[0]).equals("design")){
				if(((String)inObject[1]).equals("synchronizedata")){
					output.writeObject(dbManager.synchronizeData((HashMap<Integer, Integer>)inObject[2]));
				}
				if(((String)inObject[1]).equals("addempty")){
					output.writeObject(Integer.toString(dbManager.addDesign((EmptyDesign)inObject[2], (String)inObject[3], (String)inObject[4], "none")));
				}
				else if(((String)inObject[1]).equals("addproposal")){
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

					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					dbManager.addProposal(design.getSourceID(), designID, (String)inObject[6], permission);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						if(inObject[7]!=null)((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
					}
					output.writeObject(Integer.toString(designID));
				}
				else if(((String)inObject[1]).equals("addbase")){
					Design design = (Design)inObject[2];
					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						// see if a thumbnail has been packed as well
						if(inObject.length>6){
							((PhysicalFileTransporter)inObject[6]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
						}
					}
					output.writeObject(Integer.toString(designID));
				}
				else if(((String)inObject[1]).equals("changename")){
					output.writeObject(Boolean.toString(dbManager.changeDesignName((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(((String)inObject[1]).equals("changedescription")){
					output.writeObject(Boolean.toString(dbManager.changeDesignDescription((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(((String)inObject[1]).equals("changeaddress")){
					output.writeObject(Boolean.toString(dbManager.changeDesignAddress((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(((String)inObject[1]).equals("changeurl")){
					output.writeObject(Boolean.toString(dbManager.changeDesignURL((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
				}
				else if(((String)inObject[1]).equals("changemodellocation")){
					output.writeObject(Boolean.toString(dbManager.changeModeledDesignLocation((Integer)inObject[2], (Float)inObject[4], (UTMCoordinate)inObject[3], (String)inObject[5], (String)inObject[6])));
				}
				else if(((String)inObject[1]).equals("findbyid")){
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					output.writeObject(design);
				}
				else if(((String)inObject[1]).equals("findbyname")){
					output.writeObject(dbManager.findDesignsByName((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbyuser")){
					output.writeObject(dbManager.findDesignsByUser((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbydate")){
					output.writeObject(dbManager.findDesignsByDate((Long)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbycity")){
					output.writeObject(dbManager.findDesignsByCity((Integer)inObject[2], (Boolean)inObject[3]));
				}
				else if(((String)inObject[1]).equals("terrainbycity")){
					output.writeObject(dbManager.findTerrainDesignsByCity((Integer)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findmodeledbycity")){
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_MODEL));
				}
				else if(((String)inObject[1]).equals("findaudiobycity")){
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_AUDIO));
				}
				else if(((String)inObject[1]).equals("findimagebycity")){
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_SKETCH));
				}
				else if(((String)inObject[1]).equals("findvideobycity")){
					output.writeObject(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_VIDEO));
				}
				else if(((String)inObject[1]).equals("allproposals")){
					output.writeObject(dbManager.findAllProposals((Integer)inObject[2]));
				}
				else if(((String)inObject[1]).equals("requestfile")){
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
				else if(((String)inObject[1]).equals("requestthumb")){
					output.writeObject(wrapThumbnail((Integer)inObject[2]));
				}
				else if(((String)inObject[1]).equals("changefile")){
					Design design = dbManager.findDesignByID((Integer)inObject[2]);
					String currentFile = design.getFilepath().substring(0, design.getFilepath().lastIndexOf("."));
					String newFilename=null;
					if(currentFile.contains("_")){
						int currentIteration = Integer.parseInt(currentFile.substring(currentFile.lastIndexOf("_")+1, currentFile.length()));
						newFilename = design.getID()+"_"+(currentIteration+1)+".jme";
					}
					else{
						newFilename=design.getID()+"_"+1+".jme";
					}
					PhysicalFileTransporter pft = (PhysicalFileTransporter)inObject[6];
					pft.writeToFileSystem(new File(modelBinLocation+"designmedia/"+newFilename));
					output.writeObject(Boolean.toString(dbManager.changeDesignFile(design.getID(), newFilename, (String)inObject[3], (String)inObject[4], (Boolean)inObject[5])));
				}
				else if(((String)inObject[1]).equals("reserve")){
					Design design = (Design)inObject[2];
					design.setPublic(false);
					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					output.writeObject(dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension));
				}
				else if(((String)inObject[1]).equals("remove")){
					int designID = (Integer)inObject[2];
					String user = (String)inObject[3];
					String pass = (String)inObject[4];
					output.writeObject(Integer.toString(dbManager.removeDesign(designID, user, pass)));
				}
				else if(((String)inObject[1]).equals("synchronize")){
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
			else if(((String)inObject[0]).equals("design-android")){
				androidDesign(inObject);
			}

			// PROPOSAL FUNCTIONALITY
			else if(((String)inObject[0]).equals("proposal")){
				if(((String)inObject[1]).equals("findinradius")){
					output.writeObject(dbManager.findAllProposalsInArea((UTMCoordinate)inObject[2], (Integer)inObject[3]));
				}
				if(((String)inObject[1]).equals("getpermissions")){
					output.writeObject(dbManager.getProposalPermissions((Integer)inObject[2]));
				}
				else if(((String)inObject[1]).equals("addversion")){
					Design design = (Design)inObject[2];
					String extension = design.getFilepath().substring(design.getFilepath().lastIndexOf(".")+1, design.getFilepath().length());
					System.out.print("filepath being added: " + design.getFilepath());
					int designID = dbManager.addDesign(design, (String)inObject[3], (String)inObject[4], extension);
					dbManager.addVersion(design.getSourceID(), designID, (String)inObject[6]);
					if(designID>0){
						((PhysicalFileTransporter)inObject[5]).writeToFileSystem(new File(modelBinLocation+"designmedia/"+designID+"."+extension));
						if(inObject.length==8)if(inObject[7]!=null)((PhysicalFileTransporter)inObject[7]).writeToFileSystem(new File(modelBinLocation+"designthumbs/"+designID+".png"));
					}
					output.writeObject(Integer.toString(designID));
				}
			}

			// VERSION FUNCTIONALITY
			else if(((String)inObject[0]).equals("version")){
				if(((String)inObject[1]).equals("versionsofproposal")){
					output.writeObject(dbManager.findVersionsOfProposal((Integer)inObject[2]));
				}
			}

			// FAVE FUNCTIONALITY
			else if(((String)inObject[0]).equals("fave")){
				if(((String)inObject[1]).equals("add")){
					output.writeObject(Integer.toString(dbManager.faveDesign((String)inObject[2], (String)inObject[3], (Integer)inObject[4])));
				}
				else if(((String)inObject[1]).equals("remove")){
					// TODO implement this
				}
			}

			// ACTIVITY FUNCTIONALITY
			else if(((String)inObject[0]).equals("activity")){
				if(((String)inObject[1]).equals("comments")){
					output.writeObject(dbManager.retrieveRecentComments());
				}
				else if(((String)inObject[1]).equals("designs")){
					output.writeObject(dbManager.retrieveRecentDesignIDs());
				}
				else if(((String)inObject[1]).equals("myactivity")){
					output.writeObject(dbManager.retrieveCommentsOnMyActivity(SessionTracker.get().getSession((String)inObject[2])));

				}
			}


			//SHARE FUNCTIONALITY
			else if(((String)inObject[0]).equals("share")){
				if (Preferences.getBooleanSetting(Preferences.MAIL_ENABLED)){
					logger.info("Share requested");
					// This is an example on how to use the mailer!
					try {
						MimeMessage message = new ShareBetavilleMessage(MailSystem.getMailer().getSession(), ((String)inObject[1]), ((String)inObject[2]), ((String)inObject[3]), ((String)inObject[4]));
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
			else if(((String)inObject[0]).equals("comment")){
				if(((String)inObject[1]).equals("add")){
					output.writeObject(Boolean.toString(dbManager.addComment((Comment)inObject[2], (String)inObject[3])));
				}
				if(((String)inObject[1]).equals("delete")){
					output.writeObject(Boolean.toString(dbManager.deleteComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				if(((String)inObject[1]).equals("reportspam")){
					dbManager.reportSpamComment((Integer)inObject[2]);
				}
				if(((String)inObject[1]).equals("getforid")){
					output.writeObject(dbManager.getComments((Integer)inObject[2]));
				}
			}

			// ANDROID COMMENT FUNCTIONALITY
			else if(((String)inObject[0]).equals("comment-android")){
				androidComment(inObject);
			}

			// CITY FUNCTIONALITY
			else if(((String)inObject[0]).equals("city")){
				if(((String)inObject[1]).equals("add")){
					output.writeObject(Integer.toString(dbManager.addCity((String)inObject[2], (String)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("findbyname")){
					output.writeObject(dbManager.findCitiesByName((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbystate")){
					output.writeObject(dbManager.findCitiesByState((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbycountry")){
					output.writeObject(dbManager.findCitiesByCountry((String)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbyid")){
					output.writeObject(dbManager.findCityByID((Integer)inObject[2]));
				}
				else if(((String)inObject[1]).equals("findbyall")){
					output.writeObject(dbManager.findCityByAll((String)inObject[2], (String)inObject[3], (String)inObject[4]));
				}
				else if(((String)inObject[1]).equals("getall")){
					output.writeObject(dbManager.findAllCities());
				}
			}

			// WORMHOLES
			else if(((String)inObject[0]).equals("wormhole")){
				if(((String)inObject[1]).equals("add")){
					logger.info("Adding Wormhole");
					output.writeObject(Integer.toString(dbManager.addWormhole((UTMCoordinate)inObject[2], (String)inObject[3], (Integer)inObject[4], (String)inObject[5])));
				}
				else if(((String)inObject[1]).equals("delete")){
					output.writeObject(Integer.toString(dbManager.deleteWormhole((Integer)inObject[2], (String)inObject[3])));
				}
				else if(((String)inObject[1]).equals("editname")){
					output.writeObject(Integer.toString(dbManager.changeWormholeName((String)inObject[2], (Integer)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("editlocation")){
					output.writeObject(Integer.toString(dbManager.changeWormholeLocation((UTMCoordinate)inObject[2], (Integer)inObject[3], (String)inObject[4])));
				}
				else if(((String)inObject[1]).equals("getwithin")){
					output.writeObject(dbManager.getWormholesWithin((UTMCoordinate)inObject[2], (Integer)inObject[3], (Integer)inObject[4]));
				}
				else if(((String)inObject[1]).equals("getall")){
					output.writeObject(dbManager.getAllWormholes());
				}
				else if(((String)inObject[1]).equals("getallincity")){
					logger.info("getallincity command received");
					output.writeObject(dbManager.getAllWormholesInCity((Integer)inObject[2]));
				}
			}

			// VERSION ENFORCEMENT
			else if(((String)inObject[0]).equals("softwareversion")){
				if(((String)inObject[1]).equals("getdesign")){
					output.writeObject(Long.toString(Design.serialVersionUID));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void androidDesign(Object[] inObject) throws IOException{
		logger.info("Inside Android Design: " + (String)inObject[1]);
		if(((String)inObject[1]).equals("changename")){
			output.writeObject(Boolean.toString(dbManager.changeDesignName((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(((String)inObject[1]).equals("changedescription")){
			output.writeObject(Boolean.toString(dbManager.changeDesignDescription((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(((String)inObject[1]).equals("changeaddress")){
			output.writeObject(Boolean.toString(dbManager.changeDesignAddress((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(((String)inObject[1]).equals("changeurl")){
			output.writeObject(Boolean.toString(dbManager.changeDesignURL((Integer)inObject[2], (String)inObject[3], (String)inObject[4], (String)inObject[5])));
		}
		else if(((String)inObject[1]).equals("changemodellocation")){
			output.writeObject(Boolean.toString(dbManager.changeModeledDesignLocation((Integer)inObject[2], (Integer)inObject[4], (UTMCoordinate)inObject[3], (String)inObject[5], (String)inObject[6])));
		}
		else if(((String)inObject[1]).equals("findbyid")){
			logger.info("finding "+(Integer)inObject[2]);
			Design design = dbManager.findDesignByID((Integer)inObject[2]);
			logger.info("design " + design.getName());
			logger.info("\n"+xo.outputString(DataExporter.export(design)));
			output.writeObject(xo.outputString(DataExporter.export(design)));
		}
		else if(((String)inObject[1]).equals("findbyname")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByName((String)inObject[2]))));
		}
		else if(((String)inObject[1]).equals("findbyuser")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByUser((String)inObject[2]))));
		}
		else if(((String)inObject[1]).equals("findbydate")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findDesignsByDate((Long)inObject[2]))));
		}
		else if(((String)inObject[1]).equals("findbycity")){
			List<Design> designs = dbManager.findDesignsByCity((Integer)inObject[2], (Boolean)inObject[3]);
			logger.info(designs.size()+" designs retrieved");
			String xmlResponse = xo.outputString(DataExporter.exportDesigns(designs));
			logger.info("Responding with: " + xmlResponse);
			output.writeObject(xmlResponse);
		}
		else if(((String)inObject[1]).equals("findbycitysetstartend")){
			List<Design> designs = dbManager.findDesignsByCitySetStartEnd((Integer)inObject[2], (Boolean)inObject[3],
					(Integer)inObject[4], (Integer)inObject[5]);
			logger.info(designs.size()+" designs retrieved");
			String xmlResponse = xo.outputString(DataExporter.exportDesigns(designs));
			logger.info("Responding with: " + xmlResponse);
			output.writeObject(StringZipper.compress(xmlResponse));
		}
		else if(((String)inObject[1]).equals("terrainbycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTerrainDesignsByCity((Integer)inObject[2]))));
		}
		else if(((String)inObject[1]).equals("findmodeledbycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_MODEL))));
		}
		else if(((String)inObject[1]).equals("findaudiobycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_AUDIO))));
		}
		else if(((String)inObject[1]).equals("findimagebycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_SKETCH))));
		}
		else if(((String)inObject[1]).equals("findvideobycity")){
			output.writeObject(xo.outputString(DataExporter.exportDesigns(dbManager.findTypeDesiginsByCity((Integer)inObject[2], DBConst.DESIGN_TYPE_VIDEO))));
		}
		else if(((String)inObject[1]).equals("allproposals")){
			output.writeObject(dbManager.findAllProposals((Integer)inObject[2]));
		}
		else if(((String)inObject[1]).equals("requestthumb")){
			output.writeObject(wrapThumbnail((Integer)inObject[2]));
		}
		else if(((String)inObject[1]).equals("remove")){
			int designID = (Integer)inObject[2];
			String user = (String)inObject[3];
			String pass = (String)inObject[4];
			output.writeObject(Integer.toString(dbManager.removeDesign(designID, user, pass)));
		}
	}

	private void androidComment(Object[] inObject) throws IOException{
		if(((String)inObject[1]).equals("add")){// TODO: NOT ANDROID SAFE YET
			output.writeObject(Boolean.toString(dbManager.addComment((Comment)inObject[2], (String)inObject[3])));
		}
		if(((String)inObject[1]).equals("delete")){// TODO: NOT ANDROID SAFE YET
			output.writeObject(Boolean.toString(dbManager.deleteComment((Integer)inObject[2], (String)inObject[3], (String)inObject[4])));
		}
		if(((String)inObject[1]).equals("reportspam")){// TODO: NOT ANDROID SAFE YET
			dbManager.reportSpamComment((Integer)inObject[2]);
		}
		if(((String)inObject[1]).equals("getforid")){
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
