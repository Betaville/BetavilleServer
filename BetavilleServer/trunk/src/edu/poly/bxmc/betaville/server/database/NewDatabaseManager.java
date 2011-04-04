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
package edu.poly.bxmc.betaville.server.database;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import edu.poly.bxmc.betaville.jme.map.MapManager;
import edu.poly.bxmc.betaville.jme.map.UTMCoordinate;
import edu.poly.bxmc.betaville.jme.map.MapManager.SquareCorner;
import edu.poly.bxmc.betaville.model.AudibleDesign;
import edu.poly.bxmc.betaville.model.City;
import edu.poly.bxmc.betaville.model.Comment;
import edu.poly.bxmc.betaville.model.EmptyDesign;
import edu.poly.bxmc.betaville.model.ModeledDesign;
import edu.poly.bxmc.betaville.model.ProposalPermission;
import edu.poly.bxmc.betaville.model.ProposalUtils;
import edu.poly.bxmc.betaville.model.SketchedDesign;
import edu.poly.bxmc.betaville.model.VideoDesign;
import edu.poly.bxmc.betaville.model.Design;
import edu.poly.bxmc.betaville.model.Design.Classification;
import edu.poly.bxmc.betaville.model.IUser.UserType;
import edu.poly.bxmc.betaville.model.ProposalPermission.Type;
import edu.poly.bxmc.betaville.model.Wormhole;
import edu.poly.bxmc.betaville.server.authentication.IAuthenticator;
import edu.poly.bxmc.betaville.server.mail.AbstractMailer;
import edu.poly.bxmc.betaville.server.mail.CommentNotificationMessage;
import edu.poly.bxmc.betaville.server.session.SessionTracker;
import edu.poly.bxmc.betaville.server.util.UserArrayUtils;
import edu.poly.bxmc.betaville.sound.PerformanceStyle;
import edu.poly.bxmc.betaville.util.Crypto;

/**
 * Contains all of the queries that interact with the database.
 * @author Skye Book
 *
 */
public class NewDatabaseManager {
	private static final Logger logger = Logger.getLogger(NewDatabaseManager.class);
	/**
	 * Attribute <name> - Manager of the connection to the database
	 */
	private DataBaseConnection dbConnection;

	/**
	 * Authentication module setup for this server
	 */
	private IAuthenticator authenticator;
	
	private AbstractMailer mailer;
	
	
	// USER
	private PreparedStatement addUser;
	private PreparedStatement checkNameAvailability;
	private PreparedStatement changePassword;
	private PreparedStatement changeBio;
	private PreparedStatement getUserEmail;
	private PreparedStatement authenticaterUserAgainstUsername;
	/**
	 * 1)	hash	-	String<br>
	 * 2)	salt	-	String<br>
	 * 3)	user	-	String<br>
	 */
	private PreparedStatement updateUserToHardenedEncryption;
	private PreparedStatement checkUserLevel;
	
	// DESIGN
	/**
	 * 1)	name		-	String<br>
	 * 2)	design file	-	String<br>
	 * 3)	cityID		-	int<br>
	 * 4)	user		-	String<br>
	 * 5)	coordID		-	int<br>
	 * 6)	privacy		-	int<br>
	 * 7)	description	-	String<br>
	 * 8)	url			-	String<br>
	 * 9)	type		-	String<br>
	 * 10)	address		-	String<br>
	 */
	private PreparedStatement addDesign;
	private PreparedStatement getDesignUser;
	private PreparedStatement verifyDesignOwnership;
	private PreparedStatement verifyProposalMembership;
	private PreparedStatement changeDesignName;
	private PreparedStatement changeDesignFile;
	
	/**
	 * 1) newDescription	-	String<br>
	 * 2) designID			-	int<br>
	 */
	private PreparedStatement changeDesignDescription;
	
	/**
	 * 1) newAddress	-	String<br>
	 * 2) designID		-	int<br>
	 */
	private PreparedStatement changeDesignAddress;
	
	/**
	 * 1) newURL	-	String<br>
	 * 2) designID	-	int<br>
	 */
	private PreparedStatement changeDesignURL;
	private PreparedStatement getDesignType;
	private PreparedStatement removeDesign;
	private PreparedStatement findDesignByID;
	private PreparedStatement findDesignsByName;
	private PreparedStatement findDesignsByUser;
	private PreparedStatement findDesignsByCity;
	private PreparedStatement findTypeDesignsByCity;
	private PreparedStatement findModelDesignsByCity;
	private PreparedStatement findBaseModelDesignByID;
	private PreparedStatement findTerrainDesignsByCity;
	private PreparedStatement addProposal;
	
	
	//Added by Ram
	private PreparedStatement startSession;
	private PreparedStatement endSession;
	private PreparedStatement getUserLevel;
	private PreparedStatement changeModelTex;
	private PreparedStatement selectModelDesignCoordinates;
	private PreparedStatement changeModeledDesignLocation;
	private PreparedStatement getProposalPermissions;
	private PreparedStatement isProposal;
	private PreparedStatement addVersion;
	private PreparedStatement findAllProposals;
	private PreparedStatement findVersionsOfProposal;
	private PreparedStatement getProposalRemoveList;
	private PreparedStatement addCity;
	private PreparedStatement findAllCities;
	private PreparedStatement findCitiesByName;
	private PreparedStatement findCitiesByState;
	private PreparedStatement findCitiesByCountry;
	private PreparedStatement findCityByID;
	private PreparedStatement findCityByAll;
	private PreparedStatement addCoordinate;
	
	/**
	 * 1) easting	-	int<br>
	 * 2) northing	-	int<br>
	 * 3) latzone	-	char<br>
	 * 4) lonzone	-	int<br>
	 * 5) altitude	-	int<br>
	 * 6) id		-	int<br>
	 */
	private PreparedStatement changeCoordinate;
	private PreparedStatement retrieveCoordinate;
	private PreparedStatement reportSpamContent;
	private PreparedStatement getComments;
	
	/**
	 * 1) new favelist array "size:value;value;value;"	-	String<br>
	 * 2) designID										-	int<br>
	 */
	private PreparedStatement faveDesign;
	
	/**
	 * 1) designID	-	int<br>
	 */
	private PreparedStatement retrieveFaves;
	
	// COMMENT
	/**
	 * 1) designID	-	int<br>
	 * 2) user		-	String<br>
	 * 3) text		-	String<br>
	 * 4) repliesTo	-	int<br>
	 */
	private PreparedStatement addComment;
	
	private PreparedStatement deleteComment;
	
	// WORMHOLES
	/**
	 * 1) coordinateID	-	int<br>
	 * 2) name			-	string<br>
	 * 3) rotation X	-	int<br>
	 * 4) rotation Y	-	int<br>
	 * 5) rotation Z	-	int<br>
	 */
	private PreparedStatement addWormhole;
	/**
	 * 1) wormholeID	-	int<br>
	 */
	private PreparedStatement deleteWormhole;
	/**
	 * 1) name			-	String<br>
	 * 2) wormholeID	-	int<br>
	 */
	private PreparedStatement changeWormholeName;
	/**
	 * 1) easting		-	int<br>
	 * 2) northing		-	int<br>
	 * 3) latZone		-	char<br>
	 * 4) lonZone		-	int<br>
	 * 5) altitude		-	int<br>
	 * 6) wormholeID	-	int<br>
	 */
	private PreparedStatement changeWormholeLocation;
	
	/**
	 * 1) minLat		-	char<br>
	 * 2) maxLat		-	char<br>
	 * 3) minLon		-	int<br>
	 * 4) maxLon		-	int<br>
	 * 5) minNorthing	-	int<br>
	 * 6) maxNorthing	-	int<br>
	 * 7) minEasting	-	int<br>
	 * 8) maxEasting	-	int<br>
	 */
	private PreparedStatement getWormholesAtLocation;
	
	private PreparedStatement getAllWormholes;
	private PreparedStatement getAllWormholesInCity;
	

	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager() {
		this("root");
	}
	
	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager(String password) {
		this("root", password);
	}

	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager(IAuthenticator authenticator) {
		dbConnection = new DataBaseConnection("root", "root");
		this.authenticator=authenticator;
		
		try {
			prepareStatements();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}
	
	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager(String user, String password) {
		dbConnection = new DataBaseConnection(user, password);
		try {
			//mailer=new GMailer(Preferences.getSetting(Preferences.MAIL_USER),Preferences.getSetting(Preferences.MAIL_PASS));
		} catch (Exception e1) {
			logger.error("Exception caught while initializing "+mailer.getClass().getName(), e1);
		}
		authenticator = new IAuthenticator(){
			
			// user result set
			private ResultSet rs;
			
			/*
			 * (non-Javadoc)
			 * @see edu.poly.bxmc.betaville.server.authentication.IAuthenticator#authenticateUser(java.lang.String, java.lang.String)
			 */
			public boolean authenticateUser(String user, String pass) {
				try {
					authenticaterUserAgainstUsername.setString(1, user);
					rs = authenticaterUserAgainstUsername.executeQuery();
					if(rs.first()){
						// first check to see if the hardened hash is available
						String hash = rs.getString(DBConst.USER_STRONG_PASS);
						if(hash!=null){
							if(hash.length()!=40) return authMD5(user, pass);
							logger.debug("Authenticating with strong hash");
							String salt = rs.getString(DBConst.USER_STRONG_SALT);
							rs.close();
							return Crypto.doSaltedEncryption(pass, salt).equals(hash);
						}
						else{
							return authMD5(user, pass);
						}
					}
					else
						logger.debug("user '"+user+"' not found");
						return false;
				} catch (SQLException e) {
					logger.error("SQL ERROR", e);
				}
				return false;
			}
			
			private boolean authMD5(String user, String pass) throws SQLException{
				// if its not available, do the regular authentication, *and* setup hardened encryption
				logger.debug("authenticating with legacy hash");
				String legacy = rs.getString(DBConst.USER_PASS);
				if(Crypto.doMD5(pass).equals(legacy)){
					String[] hardened = Crypto.createBetavilleHash(pass);
					updateUserToHardenedEncryption.setString(1, hardened[0]);
					updateUserToHardenedEncryption.setString(2, hardened[1]);
					updateUserToHardenedEncryption.setString(3, user);
					updateUserToHardenedEncryption.executeUpdate();
					rs.close();
					return true;
				}
				else return false;
			}
		};


		try {
			prepareStatements();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	private void prepareStatements() throws SQLException{
		addUser = dbConnection.getConnection().prepareStatement("INSERT INTO `"+DBConst.USER_TABLE+"` (`"+
				DBConst.USER_NAME+"`, `"+DBConst.USER_STRONG_PASS+"`, `"+DBConst.USER_STRONG_SALT+"`, `"+
				DBConst.USER_EMAIL+"`) VALUES" +
				"(?,?,?,?);");
		//Added by Ram
		startSession = dbConnection.getConnection().prepareStatement("INSERT INTO `"+DBConst.SESSION_TABLE+"` " +
				"(`"+DBConst.SESSION_USER+"`, `"+DBConst.SESSION_START+"`) VALUES (?, NOW());", PreparedStatement.RETURN_GENERATED_KEYS);
		endSession = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.SESSION_TABLE+" SET "+
				DBConst.SESSION_END+" = NOW() WHERE "+DBConst.SESSION_ID+ "= ?;");
		checkNameAvailability = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.USER_TABLE+" WHERE "+
				DBConst.USER_NAME+" = ?;");
		changePassword = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.USER_TABLE + " SET " + 
				DBConst.USER_PASS + " = MD5(?) WHERE "+DBConst.USER_NAME+" = ?;");
		changeBio = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.USER_TABLE + " SET " + 
				DBConst.USER_BIO + " = ? WHERE "+DBConst.USER_NAME+" = ?;");
		getUserEmail = dbConnection.getConnection().prepareStatement("SELECT " + DBConst.USER_EMAIL + " FROM " + 
				DBConst.USER_TABLE + " WHERE " + DBConst.USER_NAME + " = ?;");
		authenticaterUserAgainstUsername = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.USER_TABLE+
				" WHERE "+DBConst.USER_NAME+" = ?;");
		updateUserToHardenedEncryption = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.USER_TABLE + " SET "+
				DBConst.USER_STRONG_PASS + " = ?, " + DBConst.USER_STRONG_SALT + " = ? WHERE " + DBConst.USER_NAME + " = ?;");
		checkUserLevel = dbConnection.getConnection().prepareStatement("SELECT " + DBConst.USER_NAME + " FROM " + DBConst.USER_TABLE + 
				" WHERE " + DBConst.USER_TYPE + " = ? AND " + DBConst.USER_NAME + " = ?");
		getUserLevel = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.USER_TYPE+" FROM "+DBConst.USER_TABLE+
				" WHERE "+DBConst.USER_NAME+" = ?;");
		addDesign = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.DESIGN_TABLE+" (`"+DBConst.DESIGN_NAME+
				"`, `"+DBConst.DESIGN_FILE+"`, `"+DBConst.DESIGN_CITY+"`, `"+DBConst.DESIGN_USER+"`, `"+
				DBConst.DESIGN_COORDINATE+"`, `"+DBConst.DESIGN_DATE+"`, `"+DBConst.DESIGN_LAST_MODIFIED+"`, `"+DBConst.DESIGN_PRIVACY+"`, `"+
				DBConst.DESIGN_DESCRIPTION+"`, `"+DBConst.DESIGN_URL+"`, `"+DBConst.DESIGN_TYPE+"`, `"+
				DBConst.DESIGN_ADDRESS+"`) VALUES (?,?,?,?,?,NOW(),NOW(),?,?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		getDesignUser = dbConnection.getConnection().prepareStatement("SELECT " +DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_USER + ", "+DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_NAME+", "+DBConst.USER_TABLE+"."+DBConst.USER_EMAIL+" FROM " + DBConst.DESIGN_TABLE + " JOIN "+DBConst.USER_TABLE+" ON "+DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_USER+"="+DBConst.USER_TABLE+"."+DBConst.USER_NAME+" WHERE " + DBConst.DESIGN_ID  + " = ?;");
		verifyDesignOwnership = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.DESIGN_ID+" FROM "+DBConst.DESIGN_TABLE+
				" WHERE "+DBConst.DESIGN_USER+" = ? AND "+DBConst.DESIGN_ID+" = ?;");
		verifyProposalMembership = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.PROPOSAL_TABLE+"."+DBConst.PROPOSAL_PERMISSIONS_GROUP_ARRAY +" FROM "+DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_DEST+" = ? AND "+DBConst.PROPOSAL_PERMISSIONS_LEVEL +" LIKE '"+DBConst.PROPOSAL_PERMISSIONS_LEVEL_GROUP+"'");
		changeDesignName = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.DESIGN_TABLE + " SET " + 
				DBConst.DESIGN_NAME + " = ?, "+DBConst.DESIGN_LAST_MODIFIED+"=NOW() WHERE "+DBConst.DESIGN_ID+" = ?;");
		changeDesignFile = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.DESIGN_TABLE+" SET "+DBConst.DESIGN_FILE+
				" = ?, "+DBConst.DESIGN_LAST_MODIFIED+"=NOW() WHERE "+DBConst.DESIGN_ID+" = ?;");
		changeModelTex = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.MODEL_TABLE+" SET "+DBConst.MODEL_TEX+
				" = ? WHERE "+DBConst.MODEL_ID+" = ?;");
		selectModelDesignCoordinates = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.DESIGN_COORDINATE+
				" FROM "+DBConst.DESIGN_TABLE+" WHERE "+DBConst.DESIGN_ID+" = ?;");
		changeModeledDesignLocation = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.MODEL_TABLE+
				" SET "+DBConst.MODEL_ROTATION_Y+" = ? WHERE "+DBConst.MODEL_ID+" = ?;");
		changeDesignDescription = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.DESIGN_TABLE + " SET " + 
				DBConst.DESIGN_DESCRIPTION + " = ?, "+DBConst.DESIGN_LAST_MODIFIED+"=NOW() WHERE "+DBConst.DESIGN_ID+" = ?;");
		changeDesignAddress = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.DESIGN_TABLE + " SET " + 
				DBConst.DESIGN_ADDRESS + " = ?, "+DBConst.DESIGN_LAST_MODIFIED+"=NOW() WHERE "+DBConst.DESIGN_ID+" = ?;");
		changeDesignURL = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.DESIGN_TABLE + " SET " + 
				DBConst.DESIGN_URL + " = ?, "+DBConst.DESIGN_LAST_MODIFIED+"=NOW() WHERE "+DBConst.DESIGN_ID+" = ?;");
		getDesignType = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.DESIGN_TYPE+" FROM "+DBConst.DESIGN_TABLE+
				" WHERE "+DBConst.DESIGN_ID+" = ?;");
		removeDesign = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.DESIGN_TABLE + " SET " + 
				DBConst.DESIGN_IS_ALIVE +" = false WHERE " + DBConst.DESIGN_ID + " = ?;");
		findDesignByID = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_ID + " = ?;");
		findDesignsByName = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_NAME + " LIKE ? AND "+DBConst.DESIGN_NAME+" NOT LIKE '%$TERRAIN';;");
		findDesignsByUser = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_USER + " = ? AND "+DBConst.DESIGN_NAME+" NOT LIKE '%$TERRAIN';;");
		findDesignsByCity = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_CITY + " = ? AND "+DBConst.DESIGN_NAME+" NOT LIKE '%$TERRAIN';");
		// select * from design
		// join modeldesign on design.designid=modeldesign.designid
		// join coordinate on design.coordinateid=coordinate.coordinateid
		// left outer join proposal on design.designid=proposal.sourceid where design.isalive=1 and design.designtype='model' and design.cityid=2 and proposal.sourceid is null and design.name not like '%$TERRAIN%';
		findModelDesignsByCity = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" JOIN "+DBConst.MODEL_TABLE+" ON "+DBConst.DESIGN_ID+"="+DBConst.MODEL_ID+
				" JOIN "+DBConst.COORD_TABLE+" ON "+DBConst.DESIGN_COORDINATE+"="+DBConst.COORD_ID+
				" LEFT OUTER JOIN "+DBConst.PROPOSAL_TABLE+" ON "+DBConst.DESIGN_ID+"="+DBConst.PROPOSAL_SOURCE+
				" WHERE " + DBConst.DESIGN_CITY + " = ? AND "+DBConst.DESIGN_TYPE+"='"+DBConst.DESIGN_TYPE_MODEL+
				"' AND "+DBConst.DESIGN_IS_ALIVE+"=1 AND "+DBConst.PROPOSAL_SOURCE+" IS NULL AND "+DBConst.DESIGN_NAME+" NOT LIKE '%$TERRAIN';");
		findBaseModelDesignByID = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" JOIN "+DBConst.MODEL_TABLE+" ON "+DBConst.DESIGN_ID+"="+DBConst.MODEL_ID+
				" JOIN "+DBConst.COORD_TABLE+" ON "+DBConst.DESIGN_COORDINATE+"="+DBConst.COORD_ID+
				" LEFT OUTER JOIN "+DBConst.PROPOSAL_TABLE+" ON "+DBConst.DESIGN_ID+"="+DBConst.PROPOSAL_SOURCE+
				" WHERE " + DBConst.DESIGN_CITY + " = ? AND "+DBConst.DESIGN_TYPE+"='"+DBConst.DESIGN_TYPE_MODEL+
				"' AND "+DBConst.DESIGN_IS_ALIVE+"=1 AND "+DBConst.PROPOSAL_SOURCE+" IS NULL;");
		
		findTypeDesignsByCity = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_CITY + " = ? AND "+DBConst.DESIGN_NAME+" NOT LIKE '%$TERRAIN' AND WHERE "+DBConst.DESIGN_TYPE+" = ?;");
		findTerrainDesignsByCity = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.DESIGN_TABLE + 
				" WHERE " + DBConst.DESIGN_CITY + " = ? AND "+DBConst.DESIGN_NAME+" LIKE '%$TERRAIN';");
		addProposal = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.PROPOSAL_TABLE+
				" (`"+DBConst.PROPOSAL_SOURCE+"`, `"+DBConst.PROPOSAL_DEST+"`, `"+DBConst.PROPOSAL_TYPE+"`, `"+
				DBConst.PROPOSAL_TYPE_REMOVABLE_LIST+"`,`"+DBConst.PROPOSAL_PERMISSIONS_LEVEL+"`,`"+DBConst.PROPOSAL_PERMISSIONS_GROUP_ARRAY+"`) VALUES (?,?,'"+DBConst.PROPOSAL_TYPE_PROPOSAL+"', ?, ?, ?);");
		getProposalPermissions = dbConnection.getConnection().prepareStatement("SELECT * FROM "+
				DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_DEST+" = ?;");
		isProposal = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.PROPOSAL_TYPE+" FROM "+
				DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_DEST+" = ?;");
		addVersion = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.PROPOSAL_TABLE+
				" (`"+DBConst.PROPOSAL_SOURCE+"`, `"+DBConst.PROPOSAL_DEST+"`, `"+DBConst.PROPOSAL_TYPE+
				"`, `"+DBConst.PROPOSAL_TYPE_REMOVABLE_LIST+"`) VALUES (?,?,?,?);");
		findAllProposals = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.PROPOSAL_DEST+" FROM "+
				DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_SOURCE+" = ? AND "+DBConst.PROPOSAL_TYPE+" = ?;");
		findVersionsOfProposal = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.PROPOSAL_DEST+
				" FROM "+DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_SOURCE+" = ? AND "+DBConst.PROPOSAL_TYPE+
				" = '"+DBConst.PROPOSAL_TYPE_VERSION+"';");
		getProposalRemoveList = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.PROPOSAL_TYPE_REMOVABLE_LIST+
				" FROM "+DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_DEST+" = ?;");
		addCity = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.CITY_TABLE+
				" (`"+DBConst.CITY_NAME+"`, `"+DBConst.CITY_STATE+"`, `"+DBConst.CITY_COUNTRY+"`) VALUES (?,?,?);", 
				PreparedStatement.RETURN_GENERATED_KEYS);
		findAllCities = dbConnection.getConnection().prepareStatement("SELECT * FROM "+
				DBConst.CITY_TABLE+";");
		findCitiesByName = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.CITY_ID+" FROM "+
				DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_NAME+" LIKE ?;");
		findCitiesByState = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.CITY_ID+" FROM "+
				DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_STATE+" LIKE ?;");
		findCitiesByCountry = dbConnection.getConnection().prepareStatement("SELECT "+DBConst.CITY_ID+" FROM "+
				DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_COUNTRY+" LIKE ?;");
		findCityByID = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.CITY_TABLE+" WHERE "+
				DBConst.CITY_ID+" = ?;");
		findCityByAll = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.CITY_TABLE+
				" WHERE "+DBConst.CITY_NAME+" = ? AND "+DBConst.CITY_STATE+" = ? AND "+DBConst.CITY_COUNTRY+
				" = ?;");
		addCoordinate = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.COORD_TABLE+
				" (`"+DBConst.COORD_NORTHING+"`, `"+DBConst.COORD_EASTING+"`, `"+DBConst.COORD_LATZONE+"`, `"+
				DBConst.COORD_LONZONE+"`, `"+DBConst.COORD_ALTITUDE+"`) VALUES (?,?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
		changeCoordinate = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.COORD_TABLE+" SET "+
				DBConst.COORD_EASTING+" = ?, "+DBConst.COORD_NORTHING+" = ?, "+DBConst.COORD_LATZONE+" = ?, "+
				DBConst.COORD_LONZONE+" = ?, "+DBConst.COORD_ALTITUDE+" = ? WHERE "+DBConst.COORD_ID+" = ?;");
		retrieveCoordinate = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.COORD_TABLE+
				" WHERE "+DBConst.COORD_ID+" = ?;");
		faveDesign = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.DESIGN_TABLE+" SET " +DBConst.DESIGN_FAVE_LIST + " = ? WHERE " + DBConst.DESIGN_ID + " = ?;");
		retrieveFaves = dbConnection.getConnection().prepareStatement("SELECT + "+DBConst.DESIGN_FAVE_LIST+" FROM " +DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_ID + " = ?;");
		addComment = dbConnection.getConnection().prepareStatement("INSERT INTO "+DBConst.COMMENT_TABLE+" (`"+DBConst.COMMENT_DESIGN+"`, `"+DBConst.COMMENT_USER+"`, `"+DBConst.COMMENT_TEXT+"`, `"+DBConst.COMMENT_DATE+"`, `"+DBConst.COMMENT_REPLIESTO+"`) VALUES (?,?,?,NOW(),?);", PreparedStatement.RETURN_GENERATED_KEYS);
		deleteComment = dbConnection.getConnection().prepareStatement("DELETE FROM " + DBConst.COMMENT_TABLE + " WHERE " + 
				DBConst.COMMENT_ID + " = ?");
		reportSpamContent = dbConnection.getConnection().prepareStatement("UPDATE "+DBConst.COMMENT_TABLE+" SET "+
				DBConst.COMMENT_SPAMFLAG+" = 1 WHERE "+DBConst.COMMENT_ID+" = ?;");
		getComments = dbConnection.getConnection().prepareStatement("SELECT * FROM "+DBConst.COMMENT_TABLE+" WHERE "+
				DBConst.COMMENT_DESIGN+" = ?;");
		addWormhole = dbConnection.getConnection().prepareStatement("INSERT INTO " + DBConst.WORMHOLE_TABLE + " (`"+DBConst.WORMHOLE_COORDINATE+"`, `"+DBConst.WORMHOLE_NAME+"`, `"+DBConst.WORMHOLE_CITY+"`, `"+DBConst.WORMHOLE_IS_ALIVE+"`) VALUES (?, ?, ?, 1);", PreparedStatement.RETURN_GENERATED_KEYS);
		deleteWormhole = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.WORMHOLE_TABLE + " SET " + DBConst.WORMHOLE_IS_ALIVE + " = 0 WHERE " + DBConst.WORMHOLE_ID + " = ?");
		changeWormholeName = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.WORMHOLE_TABLE + " SET " + DBConst.WORMHOLE_NAME + " = ? WHERE " + DBConst.WORMHOLE_ID + " = ?");
		changeWormholeLocation = dbConnection.getConnection().prepareStatement("UPDATE " + DBConst.COORD_TABLE + " JOIN "+DBConst.WORMHOLE_TABLE+" ON "+DBConst.WORMHOLE_TABLE+"."+DBConst.WORMHOLE_COORDINATE+" = "+DBConst.COORD_TABLE+"."+DBConst.COORD_ID+"   SET " + DBConst.COORD_EASTING + " = ?, " + DBConst.COORD_NORTHING + " = ?, " + DBConst.COORD_LATZONE + " = ?, " + DBConst.COORD_LONZONE + " = ?, " + DBConst.COORD_ALTITUDE + " = ?, WHERE " + DBConst.WORMHOLE_ID + " = ?");
		getWormholesAtLocation = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.WORMHOLE_TABLE + " JOIN " +DBConst.COORD_TABLE + " ON "+DBConst.COORD_TABLE+"."+DBConst.COORD_ID+"="+DBConst.WORMHOLE_TABLE+"."+DBConst.WORMHOLE_COORDINATE+" WHERE "+DBConst.COORD_LATZONE+">=? AND "+DBConst.COORD_LATZONE+"<=? AND "+DBConst.COORD_LONZONE+">=? AND "+DBConst.COORD_LONZONE+"<=? AND "+DBConst.COORD_NORTHING+">=? AND "+DBConst.COORD_NORTHING+"<=? AND "+DBConst.COORD_EASTING+">=? AND "+DBConst.COORD_EASTING+"<=?");
		getAllWormholes = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.WORMHOLE_TABLE + " JOIN " +DBConst.COORD_TABLE + " ON "+DBConst.COORD_TABLE+"."+DBConst.COORD_ID+"="+DBConst.WORMHOLE_TABLE+"."+DBConst.WORMHOLE_COORDINATE);
		getAllWormholesInCity = dbConnection.getConnection().prepareStatement("SELECT * FROM " + DBConst.WORMHOLE_TABLE + " JOIN " +DBConst.COORD_TABLE + " ON "+DBConst.COORD_TABLE+"."+DBConst.COORD_ID+"="+DBConst.WORMHOLE_TABLE+"."+DBConst.WORMHOLE_COORDINATE+" WHERE "+DBConst.WORMHOLE_CITY+"=?");
	}

	public void closeConnection(){
		dbConnection.closeConnection();
	}
	
	/**
	 * Calls the authenticateUser method in the instance's authenticator.
	 * @param user
	 * @param pass
	 * @return true if the authentication was successful, false if not
	 * @see IAuthenticator
	 */
	public boolean authenticateUser(String user, String pass){
		return authenticator.authenticateUser(user, pass);
	}
	
	/**
	 * Calls the authenticateUser method in the instance's authenticator
	 * and starts a user session
	 * @param user
	 * @param pass
	 * @return -1 for an SQL exception, -3 for failed authentication, or
	 * the session ID (always greater than 0)
	 * @see IAuthenticator
	 */
	public int startSession(String user, String pass){
		if(!authenticator.authenticateUser(user, pass)){
			return -3;
		}
		else{
			try{
				startSession.setString(1, user);
				boolean executeStatus = startSession.execute();
				int sessionID = 0;
				if(!executeStatus){
					ResultSet resultSet = startSession.getGeneratedKeys();
					
					if((resultSet != null) && (resultSet.next())){
						sessionID = resultSet.getInt(1);
					}
				}
				//int sessionID = dbConnection.getLastKey();
				return sessionID;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
		}
	}

	/**
	 * Ends the specified session in the database.
	 * @param sessionID The ID of the session to end
	 * @return 0 for success, -1 for an SQL error
	 */
	public int endSession(int sessionID){
		try {
			logger.debug("Ending Session " + sessionID);
			endSession.setInt(1, sessionID);
			endSession.executeUpdate();

		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return -1;
		}
		return 0;
	}

	public boolean addUser(String user, String pass, String email, String twitter, String bio){
		try {
			String salt = Crypto.createSalt(12);
			String hash = Crypto.doSaltedEncryption(pass, salt);
			
			addUser.setString(1, user);
			addUser.setString(2, hash);
			addUser.setString(3, salt);
			addUser.setString(4, email);
			
			addUser.executeUpdate();
			
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return false;
		}
		return true;
	}

	/**
	 * Checks the availability of a username
	 * @param user Username to check
	 * @return if the name is available or not
	 */
	public boolean checkNameAvailability(String user){
		try {			
			checkNameAvailability.setString(1, user);
			ResultSet rs = checkNameAvailability.executeQuery();
			if(rs.first()){
				rs.close();
				logger.debug("name '"+user+"' unavailable");
				return false;
			}
			else{
				rs.close();
				logger.debug("name '"+user+"' unavailable");
				return true;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return true;
	}

	public boolean changePassword(String user, String pass, String newPass){
		if(authenticator.authenticateUser(user, pass)){
			
			try {
				changePassword.setString(1, newPass);
				changePassword.setString(2, user);
				changePassword.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
			return true;
		}
		else return false;
	}

	public boolean changeBio(String user, String pass, String newBio){
		if(authenticator.authenticateUser(user, pass)){

			try {
				changeBio.setString(1, newBio);
				changeBio.setString(2, user);
				changeBio.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
			return true;
		}
		else return false;
	}

	public String getUserEmail(String user){
		try {
			getUserEmail.setString(1, user);
			ResultSet rs = getUserEmail.executeQuery();
			if(rs.first()){
				String mail = rs.getString(DBConst.USER_EMAIL);
				rs.close();
				return mail;
			}
			else return null;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	
	public int checkUserLevel(String user, UserType userType){
		try {			
				checkUserLevel.setString(1, userType.toString().toLowerCase());
				checkUserLevel.setString(2, user);
				ResultSet rs = checkUserLevel.executeQuery();
			if(rs.first()){
				rs.close();
				return 1;
			}
			else{
				rs.close();
				return 0;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return -1;
		}
	}
	
	/**
	 * Retrieves the user's UserType from the database.
	 * @param user The user to check for.
	 * @return The permissions level of the user.  At least the level "member" will always be returned as a failsafe.
	 * @see UserType
	 */
	public UserType getUserLevel(String user){
		try {
			getUserLevel.setString(1, user);
			ResultSet rs = getUserLevel.executeQuery();
			if(rs.first()){
				String type = rs.getString(DBConst.USER_TYPE);
				rs.close();
				return UserType.valueOf(type.toUpperCase());
			}
			else{
				rs.close();
				return UserType.MEMBER;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return UserType.MEMBER;
		}
	}
	
	/**
	 * Adds a new design to the database
	 * @param design
	 * @param user
	 * @param pass
	 * @param removableString removables list for proposal
	 * @return The new design's ID, or -1 for SQL error, -2 for unsupported <code>Design</code>, or -3 for failed authentication
	 */
	public int addDesign(Design design, String user, String pass, String fileExtension){
		if(authenticator.authenticateUser(user, pass)){
			int privacy;
			if(design.isPublic()) privacy=1;
			else privacy=0;
			int coordinateID=addCoordinate(design.getCoordinate());
			int designID;
			try {

				// these are generic components of a design
				addDesign.setString(1, design.getName());
				addDesign.setString(2, design.getFilepath());
				addDesign.setInt(3, design.getCityID());
				addDesign.setString(4, design.getUser());
				addDesign.setInt(5, coordinateID);
				addDesign.setInt(6, privacy);
				addDesign.setString(7, design.getDescription());
				addDesign.setString(8, design.getURL());
				addDesign.setString(10, design.getAddress());

				if(design instanceof ModeledDesign){
					int texturedValue=0;
					if(((ModeledDesign)design).isTextured()) texturedValue=1;

					addDesign.setString(9, "model");
					addDesign.executeUpdate();

					ResultSet idSet = addDesign.getGeneratedKeys();
					if(idSet.next()){
						designID = idSet.getInt(1);
						dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_FILE + " = '"+designID+design.getFilepath().substring(design.getFilepath().lastIndexOf("."), design.getFilepath().length())+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
						dbConnection.sendUpdate("INSERT INTO "+DBConst.MODEL_TABLE+" (`"+DBConst.MODEL_ID+"`, `"+DBConst.MODEL_ROTATION_X+"`, `"+DBConst.MODEL_ROTATION_Y+"`, `"+DBConst.MODEL_ROTATION_Z+"`, `"+DBConst.MODEL_TEX+"`) VALUES ("+designID+","+((ModeledDesign)design).getRotationX()+", "+((ModeledDesign)design).getRotationY()+", "+((ModeledDesign)design).getRotationZ()+", "+texturedValue+");");
						return designID;
					}
				}
				else if(design instanceof SketchedDesign){
					addDesign.setString(9, "sketch");
					addDesign.executeUpdate();

					ResultSet idSet = addDesign.getGeneratedKeys();
					if(idSet.next()){
						designID = idSet.getInt(1);
						dbConnection.sendUpdate("INSERT INTO "+DBConst.SKETCH_TABLE+" (`"+DBConst.SKETCH_ID+"`, `"+DBConst.SKETCH_ROTATION+"`, `"+DBConst.SKETCH_UPPLANE+"`) VALUES ("+designID+","+((SketchedDesign)design).getRotation()+",'"+((SketchedDesign)design).getUpPlane()+"');");
						return designID;
					}
				}
				else if(design instanceof AudibleDesign){
					addDesign.setString(9, "audio");
					addDesign.executeUpdate();

					ResultSet idSet = addDesign.getGeneratedKeys();

					if(idSet.next()){
						designID = idSet.getInt(1);
						dbConnection.sendUpdate("INSERT INTO "+DBConst.AUDIO_TABLE+" (`"+DBConst.AUDIO_ID+"`, `"+DBConst.AUDIO_DIRECTIONX+"`, `"+DBConst.AUDIO_DIRECTIONY+"`, `"+DBConst.AUDIO_DIRECTIONZ+"`) VALUES ("+designID+","+((AudibleDesign)design).getDirectionX()+","+((AudibleDesign)design).getDirectionY()+","+((AudibleDesign)design).getDirectionZ()+");");
						return designID;
					}
				}
				else if(design instanceof VideoDesign){
					addDesign.setString(9, "video");
					addDesign.executeUpdate();

					ResultSet idSet = addDesign.getGeneratedKeys();

					if(idSet.next()){
						designID = idSet.getInt(1);
						dbConnection.sendUpdate("INSERT INTO "+DBConst.VIDEO_TABLE+" (`"+DBConst.VIDEO_ID+"`, `"+DBConst.VIDEO_DIRECTIONX+"`, `"+DBConst.VIDEO_DIRECTIONY+"`, `"+DBConst.VIDEO_DIRECTIONZ+"`) VALUES ("+designID+","+((VideoDesign)design).getDirectionX()+","+((VideoDesign)design).getDirectionY()+","+((VideoDesign)design).getDirectionZ()+");");
						changeDesignFilename(designID, fileExtension);
						return designID;
					}
				}
				else if(design instanceof EmptyDesign){
					addDesign.setString(9, "empty");
					addDesign.executeUpdate();

					ResultSet idSet = addDesign.getGeneratedKeys();

					if(idSet.next()){
						designID = idSet.getInt(1);
						dbConnection.sendUpdate("INSERT INTO "+DBConst.EMPTY_DESIGN_TABLE+" (`"+DBConst.EMPTY_DESIGN_ID+"`,  `"+DBConst.EMPTY_DESIGN_LENGTH+"`,  `"+DBConst.EMPTY_DESIGN_WIDTH+"`) VALUES ("+designID+","+((EmptyDesign)design).getLength()+","+((EmptyDesign)design).getWidth()+");");
						changeDesignFilename(designID, fileExtension);
						return designID;
					}
				}
				else return -2;

			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
		}
		return -3;
	}

	/**
	 * Checks for ownership of a design.  Includes user authentication as well.
	 * @param designID
	 * @param user
	 * @param pass
	 * @return true if the design is owned by the user
	 */
	private boolean verifyDesignOwnership(int designID, String user, String pass){
		try {
			if(authenticateUser(user, pass))
			{
				verifyDesignOwnership.setString(1, user);
				verifyDesignOwnership.setInt(2, designID);
				ResultSet stepTwo = verifyDesignOwnership.executeQuery();
				if(stepTwo.first()){
					stepTwo.close();
					return true;
				} else{
					stepTwo.close();
					return false;
				}
			} else return false;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return false;
		}
	}
	
	private boolean verifyMemberOfProposalGroup(int proposalDestID, String user, String pass){
		try {
			if(authenticateUser(user, pass))
			{
				verifyProposalMembership.setInt(1, proposalDestID);
				ResultSet rs = verifyProposalMembership.executeQuery();
				if(rs.first()){
					boolean answer = UserArrayUtils.checkArrayForUser(rs.getString(DBConst.PROPOSAL_PERMISSIONS_GROUP_ARRAY), user);
					rs.close();
					return answer;
				} else{
					rs.close();
					return false;
				}
			} else return false;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return false;
		}
	}

	//Need to check up on the sql for this one
	private void changeDesignFilename(int designID, String fileExtension){
		try {
			dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_FILE + " = '"+designID+"."+fileExtension+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	public boolean changeDesignName(int designID, String newName, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				changeDesignName.setString(1, newName);
				changeDesignName.setInt(2, designID);
				changeDesignName.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean changeDesignFile(int designID, String newFilename, String user, String pass, boolean textureOnOff){
		if(verifyDesignOwnership(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				changeDesignFile.setString(1, newFilename);
				changeDesignFile.setInt(2, designID);
				changeDesignFile.executeUpdate();
				
				changeModelTex.setInt(1, (textureOnOff?1:0));
				changeModelTex.setInt(2, designID);
				changeModelTex.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean changeDesignDescription(int designID, String newDescription, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass) || verifyMemberOfProposalGroup(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				changeDesignDescription.setString(1, newDescription);
				changeDesignDescription.setInt(2, designID);
				changeDesignDescription.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean changeDesignAddress(int designID, String newAddress, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass) || verifyMemberOfProposalGroup(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				changeDesignAddress.setString(1, newAddress);
				changeDesignAddress.setInt(2, designID);
				changeDesignAddress.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean changeDesignURL(int designID, String newURL, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				changeDesignURL.setString(1, newURL);
				changeDesignURL.setInt(2, designID);
				changeDesignURL.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean changeModeledDesignLocation(int designID, int rotY, UTMCoordinate newLocation, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)
				|| (authenticateUser(user, pass) && getUserLevel(user).compareTo(UserType.MODERATOR)>=0)){
			try {
				selectModelDesignCoordinates.setInt(1, designID);
				ResultSet rs = selectModelDesignCoordinates.executeQuery();
				if(rs.first()){
					int coordinateID = rs.getInt(DBConst.DESIGN_COORDINATE);
					changeCoordinate(coordinateID, newLocation);
					changeModeledDesignLocation.setInt(1, rotY);
					changeModeledDesignLocation.setInt(2, designID);
					changeModeledDesignLocation.executeUpdate();
					rs.close();
					return true;
				}else{
					rs.close();
					return false;
				}
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public String getDesignType(int designID){
		try {
			getDesignType.setInt(1, designID);
			ResultSet rs = getDesignType.executeQuery();
			if(rs.first()){
				String type = rs.getString(DBConst.DESIGN_TYPE);
				rs.close();
				return type;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return null;
	}
	
	
	
	private ModeledDesign modelDesignFromFull(ResultSet drs){
		ModeledDesign d=null;
		try {
			d = new ModeledDesign(drs.getString(DBConst.DESIGN_NAME),
					// --Create UTM coordinate here--
					new UTMCoordinate(drs.getInt(DBConst.COORD_EASTING),
							drs.getInt(DBConst.COORD_NORTHING),
							drs.getInt(DBConst.COORD_LONZONE),
							drs.getString(DBConst.COORD_LATZONE).charAt(0),
							drs.getInt(DBConst.COORD_ALTITUDE)),
					// --end of coordinate creation--
					drs.getString(DBConst.DESIGN_ADDRESS), drs.getInt(DBConst.DESIGN_CITY), drs.getString(DBConst.DESIGN_USER),
					drs.getString(DBConst.DESIGN_DESCRIPTION), drs.getString(DBConst.DESIGN_FILE), drs.getString(DBConst.DESIGN_URL),
					drs.getBoolean(DBConst.DESIGN_PRIVACY), drs.getInt(DBConst.MODEL_ROTATION_X), drs.getInt(DBConst.MODEL_ROTATION_Y),
					drs.getInt(DBConst.MODEL_ROTATION_Z), drs.getBoolean(DBConst.MODEL_TEX));
			if(drs.getInt(DBConst.PROPOSAL_SOURCE)!=0){
				
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
		
		return d;
	}

	private Design designFromResultSet(ResultSet drs){
		try {
			int id = drs.getInt(DBConst.DESIGN_ID);

			// Check to see if design is "deleted"
			if(!drs.getBoolean(DBConst.DESIGN_IS_ALIVE)){
				return null;
			}

			ResultSet proposalRS = dbConnection.sendQuery("SELECT * FROM "+DBConst.PROPOSAL_TABLE+" WHERE "+DBConst.PROPOSAL_DEST+"="+id+";");
			int sourceID=0;
			List<Integer> obstructionList = new ArrayList<Integer>();
			ProposalPermission proposalPermission = null;
			Classification classification=Classification.BASE;
			if(proposalRS.first()){
				sourceID=proposalRS.getInt(DBConst.PROPOSAL_SOURCE);
				if(proposalRS.getString(DBConst.PROPOSAL_TYPE).equals(DBConst.PROPOSAL_TYPE_PROPOSAL)){
					classification=Classification.PROPOSAL;
				}
				else{
					classification=Classification.VERSION;
				}
				
				obstructionList = ProposalUtils.interpretRemovablesString(getProposalRemoveList(id));
				
				// get proposal permission
				Type type = null;
				String level = proposalRS.getString(DBConst.PROPOSAL_PERMISSIONS_LEVEL);
				
				// check for the case where we have not yet been inserting proposal permissions
				if(level==null){
					proposalPermission =  new ProposalPermission(Type.CLOSED);
				}
				else{
					if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_CLOSED)){
						type = Type.CLOSED;
					}
					else if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_GROUP)){
						type = Type.GROUP;
					}
					if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_ALL)){
						type = Type.ALL;
					}
				}
				String group = proposalRS.getString(DBConst.PROPOSAL_PERMISSIONS_GROUP_ARRAY);
				List<String> users = UserArrayUtils.getArrayUsers(group);
				
				proposalPermission =  new ProposalPermission(type, users);
			}
			String type = drs.getString(DBConst.DESIGN_TYPE);
			UTMCoordinate utm = retrieveCoordinate(drs.getInt(DBConst.DESIGN_COORDINATE));
			List<String> favedBy = UserArrayUtils.getArrayUsers(drs.getString(DBConst.DESIGN_FAVE_LIST));
			String designName = drs.getString(DBConst.DESIGN_NAME);
			String designAddress = drs.getString(DBConst.DESIGN_ADDRESS);
			int designCity = drs.getInt(DBConst.DESIGN_CITY);
			String designUser = drs.getString(DBConst.DESIGN_USER);
			String designDescription = drs.getString(DBConst.DESIGN_DESCRIPTION);
			String designFile = drs.getString(DBConst.DESIGN_FILE);
			String designURL = drs.getString(DBConst.DESIGN_URL);
			boolean designPrivacy = drs.getBoolean(DBConst.DESIGN_PRIVACY);
			Design returnable=null;
			if(type.equals(DBConst.DESIGN_TYPE_MODEL)){
				ResultSet mrs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				mrs.first();
				int rotX = mrs.getInt(DBConst.MODEL_ROTATION_X);
				int rotY = mrs.getInt(DBConst.MODEL_ROTATION_Y);
				int rotZ = mrs.getInt(DBConst.MODEL_ROTATION_Z);
				boolean designIsTextured = mrs.getBoolean(DBConst.MODEL_TEX);
				ModeledDesign md =  new ModeledDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, rotX, rotY, rotZ, designIsTextured);
				returnable = md;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_SKETCH)){
				ResultSet srs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				srs.first();
				SketchedDesign sd = new SketchedDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, srs.getInt(DBConst.SKETCH_ROTATION), srs.getString(DBConst.SKETCH_UPPLANE).charAt(0));
				returnable = sd;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_AUDIO)){
				ResultSet ars = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				ars.first();
				//TODO: Chnage to a dynamically created sound style
				AudibleDesign ad = new AudibleDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, ars.getFloat(DBConst.AUDIO_DIRECTIONX),ars.getFloat(DBConst.AUDIO_DIRECTIONY),ars.getFloat(DBConst.AUDIO_DIRECTIONZ), ars.getInt(DBConst.AUDIO_VOLUME), new PerformanceStyle());
				returnable = ad;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_VIDEO)){
				ResultSet vrs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				vrs.first();
				VideoDesign vd = new VideoDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, vrs.getFloat(DBConst.VIDEO_DIRECTIONX),vrs.getFloat(DBConst.VIDEO_DIRECTIONY),vrs.getFloat(DBConst.VIDEO_DIRECTIONZ), vrs.getInt(DBConst.AUDIO_VOLUME));
				returnable = vd;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_EMPTY)){
				ResultSet ers = dbConnection.sendQuery("SELECT * FROM " + DBConst.EMPTY_DESIGN_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				ers.first();
				EmptyDesign vd = new EmptyDesign(utm, designAddress, designUser, designDescription, designURL, designPrivacy, ers.getInt(DBConst.EMPTY_DESIGN_LENGTH), ers.getInt(DBConst.EMPTY_DESIGN_WIDTH));
				returnable = vd;
			}
			returnable.setID(id);
			returnable.setSourceID(sourceID);
			returnable.setDateAdded(DateFormat.getDateInstance().format(drs.getDate(DBConst.DESIGN_DATE)));
			returnable.setClassification(classification);
			returnable.setDesignsToRemove(obstructionList);
			returnable.setFavedBy(favedBy);
			if(proposalPermission!=null) returnable.setProposalPermission(proposalPermission);
			return returnable;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	/**
	 * Removes a design from the database
	 * @param designID
	 * @param user
	 * @param pass
	 * @return 0 for success, -3 for failed authentication
	 */
	public int removeDesign(int designID, String user, String pass){
		if(authenticator.authenticateUser(user, pass) && // user & pass must be correct as well as:
				((getUserLevel(user).equals(UserType.ADMIN) ||  getUserLevel(user).equals(UserType.MODERATOR)) // must be an admin or moderator, OR
				|| verifyDesignOwnership(designID, user, pass))){ // the original creator of the design
			try {
				removeDesign.setInt(1, designID);
				removeDesign.executeUpdate();
				return 0;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
		}
		else return -3;
	}

	public Design findDesignByID(int id){
		try {
			findDesignByID.setInt(1, id);
			ResultSet drs = findDesignByID.executeQuery();
			if(drs.first()){
				Design d = designFromResultSet(drs);
				drs.close();
				return d;
			}
			else return null;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public ArrayList<Design> findMultipleDesignsByID(int[] id){
		String stmt = "SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE {" + DBConst.DESIGN_ID + " = " + id[0]+"}";
		for(int i=1; i<id.length; i++){
			stmt+= " OR {" + DBConst.DESIGN_ID + " = " + id[i] +"}";
		}
		stmt+=";";
		try {
			ResultSet rs = dbConnection.sendQuery(stmt);
			ArrayList<Design> designs = new ArrayList<Design>();
			while(rs.next()){
				designs.add(designFromResultSet(rs));
			}
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return null;
	}

	public Vector<Design> findDesignsByName(String name){
		try {
			findDesignsByName.setString(1, name);
			ResultSet drs = findDesignsByName.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				designs.add(designFromResultSet(drs));
			}
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public Vector<Design> findDesignsByUser(String user){
		try {
			logger.debug("Finding designs from "+user);
			findDesignsByUser.setString(1, user);
			ResultSet drs = findDesignsByUser.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				designs.add(designFromResultSet(drs));
			}
			drs.close();
			
			logger.debug(designs.size() + " designs found for user " + user);
			
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	/**
	 * DO NOT CALL
	 * TODO: findDesignsByDate
	 * @incomplete
	 */
	public Vector<Design> findDesignsByDate(long date){
		new java.sql.Date(date);
		return null;
	}
	
	public Vector<Design> findTypeDesiginsByCity(int cityID, String type){
		try{
			findTypeDesignsByCity.setInt(1, cityID);
			findTypeDesignsByCity.setString(2, type);
			ResultSet drs = findTypeDesignsByCity.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				designs.add(designFromResultSet(drs));
			}
			drs.close();
			return designs;
		}catch(SQLException e){
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public Vector<Design> findDesignsByCity(int cityID, boolean onlyBase){
		try {
			findDesignsByCity.setInt(1, cityID);
			ResultSet drs = findDesignsByCity.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				// if we're only looking for base designs then we need to check
				// that the sourceID is 0
				Design d = designFromResultSet(drs);
				if(d!=null){
					if(onlyBase){
						if(d.getSourceID()==0  && !d.getFilepath().endsWith(".")){
							designs.add(d);
						}
					}
					else{
						designs.add(d);
					}
				}
			}
			drs.close();
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	
	//TODO complete
	/*
	public Vector<Design> findModelDesignsByCity(int cityID, boolean onlyBase){
		try {
			findModelDesignsByCity.setInt(1, cityID);
			ResultSet drs = findDesignsByCity.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				// if we're only looking for base designs then we need to check
				// that the sourceID is 0
				if(onlyBase && drs.getInt(DBConst.des))
				Design d = new ModeledDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, rotX, rotY, rotZ, designIsTextured);
				if(d!=null){
					if(onlyBase){
						if(d.getSourceID()==0  && !d.getFilepath().endsWith(".")){
							designs.add(d);
						}
					}
					else{
						designs.add(d);
					}
				}
			}
			drs.close();
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	*/
	
	public Vector<Design> findTerrainDesignsByCity(int cityID){
		try {
			findTerrainDesignsByCity.setInt(1, cityID);
			ResultSet drs = findTerrainDesignsByCity.executeQuery();
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				Design d = designFromResultSet(drs);
				if(d!=null) designs.add(d);
			}
			drs.close();
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	
	/**
	 * Adds a proposal to the proposal table.
	 * @param source
	 * @param destination
	 */
	public void addProposal(int source, int destination, String removableString, ProposalPermission permission){
		try {
			String removableToInsert="NONE";
			if(removableString!=null) removableToInsert=removableString;
			addProposal.setInt(1, source);
			addProposal.setInt(2, destination);
			addProposal.setString(3, removableToInsert);
			addProposal.setString(4, permission.getType().name().toLowerCase());
			if(permission.getType().equals(Type.GROUP)) addProposal.setString(5, UserArrayUtils.createArrayFromUsers(permission.getUsers()));
			else addProposal.setString(5, "");
			addProposal.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	private boolean isProposal(int designID){
		try {
			isProposal.setInt(1, designID);
			ResultSet propRS = isProposal.executeQuery();
			if(propRS.first()){
				String type = propRS.getString(DBConst.PROPOSAL_TYPE);
				if(type!=null){
					if(type.toLowerCase().equals(DBConst.PROPOSAL_TYPE_PROPOSAL.toLowerCase())){
						return true;
					}
					else return false;
				}
				else return false;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return false;
	}

	/**
	 * Adds a version to the proposal table.
	 * @param source
	 * @param destination
	 */
	public void addVersion(int source, int destination, String removableString){
		try {
			String removableToInsert="NONE";
			if(ProposalUtils.validateRemovableList(removableString)) removableToInsert=removableString;
			addVersion.setInt(1, source);
			addVersion.setInt(2, destination);
			addVersion.setString(3, DBConst.PROPOSAL_TYPE_VERSION);
			addVersion.setString(4, removableToInsert);
			addVersion.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	/**
	 * Finds all proposals related to a design
	 * @param source The ID of the design for which to find all proposals
	 * @return ID of all proposals related to this source
	 */
	public int[] findAllProposals(int source){
		try {
			ArrayList<Integer> proposals = new ArrayList<Integer>();
			findAllProposals.setInt(1, source);
			findAllProposals.setString(2, DBConst.PROPOSAL_TYPE_PROPOSAL);
			ResultSet rs = findAllProposals.executeQuery();
			while(rs.next()){
				proposals.add(rs.getInt(DBConst.PROPOSAL_DEST));
			}
			int[] returnable = new int[proposals.size()];
			for(int i=0; i<proposals.size(); i++){
				returnable[i] = proposals.get(i);
			}
			rs.close();
			return returnable;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	/**
	 * Gets all of the proposals in a specific area
	 * @param coordinate
	 * @param meterRadius
	 * @return
	 */
	public ArrayList<Design> findAllProposalsInArea(UTMCoordinate coordinate, int meterRadius){
		ArrayList<Design> designs = new ArrayList<Design>();
		UTMCoordinate[] box = MapManager.createBox(meterRadius*2, meterRadius*2, SquareCorner.CENTER,coordinate);
		try {
			ResultSet coordResultSet = dbConnection.sendQuery("SELECT " + DBConst.COORD_ID + " FROM " + DBConst.COORD_TABLE + " WHERE " + DBConst.COORD_LATZONE + " = '" + box[0].getLatZone() +"' AND " + DBConst.COORD_LONZONE + " = " + box[0].getLonZone() + " AND " + DBConst.COORD_EASTING + " < " + box[1].getEasting()
					+ " AND " + DBConst.COORD_EASTING + " > " + box[0].getEasting() + " AND " + DBConst.COORD_NORTHING + " > " + box[3].getNorthing() + " AND "
					+ DBConst.COORD_NORTHING + " < " + box[0].getNorthing()+";");
			while(coordResultSet.next()){
				// Get the design information for each coordinate
				ResultSet designResultSet = dbConnection.sendQuery("SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_COORDINATE + " = " + coordResultSet.getInt(DBConst.COORD_ID) + " AND " + DBConst.DESIGN_IS_ALIVE + "=1;");
				if(designResultSet.first()){
					// check if its a proposal
					if(isProposal(designResultSet.getInt(DBConst.DESIGN_ID))){
						Design proposalDesign = designFromResultSet(designResultSet);
						proposalDesign.setDesignsToRemove(ProposalUtils.interpretRemovablesString(getProposalRemoveList(proposalDesign.getID())));
						designs.add(proposalDesign);
					}
				}
				designResultSet.close();
			}
			coordResultSet.close();
			return designs;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	
	public ProposalPermission getProposalPermissions(int designID){
		try {
			getProposalPermissions.setInt(1, designID);
			ResultSet rs = getProposalPermissions.executeQuery();
			if(rs.first()){
				Type type = null;
				String level = rs.getString(DBConst.PROPOSAL_PERMISSIONS_LEVEL);
				
				// check for the case where we have not yet been inserting proposal permissions
				if(level==null){
					return new ProposalPermission(Type.CLOSED);
				}
				if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_CLOSED)){
					type = Type.CLOSED;
				}
				else if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_GROUP)){
					type = Type.GROUP;
				}
				if(level.equals(DBConst.PROPOSAL_PERMISSIONS_LEVEL_ALL)){
					type = Type.ALL;
				}
				String group = rs.getString(DBConst.PROPOSAL_PERMISSIONS_GROUP_ARRAY);
				List<String> users = UserArrayUtils.getArrayUsers(group);
				
				return new ProposalPermission(type, users);
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return null;
	}

	/**
	 * Finds the versions of a given proposal.  This does not verify whether an
	 * ID is correctly representing a proposal and as such should only be called
	 * when we can be sure that the ID given is for a proposal.
	 * @param proposalDesignID The proposal whose versions we wish to find.
	 * @return An array of integers identifying the designs in order or null
	 * if no versions were found.
	 */
	public int[] findVersionsOfProposal(int proposalDesignID){
		ArrayList<Integer> proposalList = new ArrayList<Integer>();
		int[] versionTable = null;

		try {
			findVersionsOfProposal.setInt(1, proposalDesignID);
			ResultSet rs = findVersionsOfProposal.executeQuery();
			while(rs.next()){
				proposalList.add(rs.getInt(DBConst.PROPOSAL_DEST));
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}

		if(!proposalList.isEmpty()){
			versionTable = new int[proposalList.size()];
			for(int i=0; i<proposalList.size(); i++){
				versionTable[i] = proposalList.get(i);
			}
			return versionTable;
		}
		else return null;			
	}
	
	/**
	 * Gets the list of designs that need to be removed for a proposal
	 * to be displayed properly
	 * @param designID
	 * @return A string containing the ID's of what to remove in the
	 * following format: "idToRemove;" i.e: "5;4;1;8;"
	 */
	public String getProposalRemoveList(int designID){
		try {
			getProposalRemoveList.setInt(1, designID);
			ResultSet rs = getProposalRemoveList.executeQuery();
			if(rs.first()){
				String removeList = rs.getString(DBConst.PROPOSAL_TYPE_REMOVABLE_LIST);
				rs.close();
				return removeList;
			}
			else{
				rs.close();
				return "";
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return null;
	}

	/**
	 * Adds a new city to the database and checks for the pre-existence of the city
	 * @param name Name of the new city
	 * @param state State in which the city is located
	 * @param country Country in which the city is located
	 * @return The ID of the new city, or -1 for an SQL error, or -2 if the city already exists
	 */
	public int addCity(String name, String state, String country){
		try {
			if(findCityByAll(name, state, country)==null){
				addCity.setString(1, name);
				addCity.setString(2, state);
				addCity.setString(3, country);
				addCity.executeUpdate();
				ResultSet resultSet = addCity.getGeneratedKeys();
				if((resultSet != null) && (resultSet.next())){
					return resultSet.getInt(1);
				}
				return -1;
			}
			else return -2;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return -1;
		}
	}
	
	public List<City> findAllCities(){
		ArrayList<City> cities = new ArrayList<City>();
		try {
			ResultSet rs = findAllCities.executeQuery();
			while(rs.next()){
				int cityID = rs.getInt(DBConst.CITY_ID);
				String name = rs.getString(DBConst.CITY_NAME);
				String state = rs.getString(DBConst.CITY_STATE);
				String country = rs.getString(DBConst.CITY_COUNTRY);
				cities.add(new City(name, state, country, cityID));
			}
			return cities;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
		return cities;
	}

	public Vector<Integer> findCitiesByName(String cityName){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			findCitiesByName.setString(1, cityName);
			cityResult = findCitiesByName.executeQuery();
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			cityResult.close();
			return results;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public Vector<Integer> findCitiesByState(String state){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			findCitiesByState.setString(1, state);
			cityResult = findCitiesByState.executeQuery();
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			cityResult.close();
			return results;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public Vector<Integer> findCitiesByCountry(String country){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			findCitiesByCountry.setString(1, country);
			cityResult = findCitiesByCountry.executeQuery();
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			cityResult.close();
			return results;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public String[] findCityByID(int id){
		ResultSet cityResult;
		try {
			findCityByID.setInt(1, id);
			cityResult = findCityByID.executeQuery();
			if(cityResult.first()){
				String[] city = new String[]{cityResult.getString("cityName"),cityResult.getString("state"),cityResult.getString("country")};
				cityResult.close();
				return city;
			}
			cityResult.close();
			return null;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public String[] findCityByAll(String name, String state, String country){
		try {
			findCityByAll.setString(1, name);
			findCityByAll.setString(2, state);
			findCityByAll.setString(3, country);
			ResultSet rs = findCityByAll.executeQuery();
			if(rs.first()){
				rs.close();
				return new String[]{name, state, country};
			}
			else{
				rs.close();
				return null;
			}
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	/**
	 * 
	 * @param utm
	 * @return
	 * @see #addCoordinate(int, int, int, char, int)
	 */
	public int addCoordinate(UTMCoordinate utm){
		return addCoordinate(utm.getEasting(), utm.getNorthing(), utm.getLonZone(), utm.getLatZone(), utm.getAltitude());
	}

	/**
	 * 
	 * @param easting
	 * @param northing
	 * @param lonZone
	 * @param latZone
	 * @param altitude
	 * @return
	 */
	public int addCoordinate(int easting, int northing, int lonZone, char latZone, int altitude){
		try {
			addCoordinate.setInt(1, northing);
			addCoordinate.setInt(2, easting);
			addCoordinate.setString(3, ""+latZone);
			addCoordinate.setInt(4, lonZone);
			addCoordinate.setInt(5, altitude);
			
			addCoordinate.executeUpdate();
			
			ResultSet resultSet = addCoordinate.getGeneratedKeys();
			if((resultSet != null) && (resultSet.next())){
				int returned = resultSet.getInt(1);
				resultSet.close();
				return returned;
			}
			return -1;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return -1;
		}
	}

	public void changeCoordinate(int coordinateID, UTMCoordinate utm){
		changeCoordinate(coordinateID, utm.getEasting(), utm.getNorthing(), utm.getLonZone(), utm.getLatZone(), utm.getAltitude());
	}

	public void changeCoordinate(int coordinateID, int easting, int northing, int lonZone, char latZone, int altitude){
		try {
			changeCoordinate.setInt(1, easting);
			changeCoordinate.setInt(2, northing);
			changeCoordinate.setString(3, ""+latZone);
			changeCoordinate.setInt(4, lonZone);
			changeCoordinate.setInt(5, altitude);
			changeCoordinate.setInt(6, coordinateID);
			changeCoordinate.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	public UTMCoordinate retrieveCoordinate(int coordinateID){
		try {
			retrieveCoordinate.setInt(1, coordinateID);
			ResultSet rs = retrieveCoordinate.executeQuery();
			if(rs.first()){
				return new UTMCoordinate(rs.getInt(DBConst.COORD_EASTING), rs.getInt(DBConst.COORD_NORTHING), rs.getInt(DBConst.COORD_LONZONE), rs.getString(DBConst.COORD_LATZONE).charAt(0), rs.getInt(DBConst.COORD_ALTITUDE));
			}
			else return null;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}

	public boolean addComment(Comment comment, String pass){
		if(authenticator.authenticateUser(comment.getUser(),pass)){
			try {
				addComment.setInt(1, comment.getDesignID());
				addComment.setString(2, comment.getUser());
				addComment.setString(3, comment.getComment());
				addComment.setInt(4, comment.repliesTo());
				addComment.executeUpdate();
				if(mailer!=null){
					getDesignUser.setInt(1, comment.getDesignID());
					ResultSet rs = getDesignUser.executeQuery();
					if(rs.first()){
						try {
							CommentNotificationMessage message = new CommentNotificationMessage(mailer.getSession(), rs.getString(DBConst.DESIGN_USER), comment.getUser(), rs.getString(DBConst.DESIGN_NAME), comment.getComment(), rs.getString(DBConst.USER_EMAIL));
							mailer.sendMailNow(message);
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return false;
			}
		}
		else return false;
	}

	public boolean deleteComment(int commentID, String user, String password){
		if(authenticator.authenticateUser(user, password)){
			try {
				deleteComment.setInt(1, commentID);
				deleteComment.executeUpdate();
				return true;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
			}
		}
		return false;
	}

	public void reportSpamComment(int commentID){
		try {
			reportSpamContent.setInt(1, commentID);
			reportSpamContent.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
		}
	}

	public Vector<Comment> getComments(int designID){
		Vector<Comment> comments = new Vector<Comment>();
		try {
			getComments.setInt(1, designID);
			ResultSet rs = getComments.executeQuery();
			while(rs.next()){
				// Ignore any comments that are verified spam
				if(!rs.getBoolean(DBConst.COMMENT_SPAMVERIFIED)){
					comments.add(new Comment(rs.getInt(DBConst.COMMENT_ID), designID, rs.getString(DBConst.COMMENT_USER), rs.getString(DBConst.COMMENT_TEXT), rs.getInt(DBConst.COMMENT_REPLIESTO), rs.getDate(DBConst.COMMENT_DATE).toString()));
				}
			}
			rs.close();
		} catch (SQLException e){
			logger.error("SQL ERROR", e);
		}
		return comments;
	}
	
	/**
	 * @param user
	 * @param pass
	 * @param designID
	 * @return 0 for success, -1 for SQL error, -2 if its already faved, -3 for failed authentication
	 */
	public int faveDesign(String user, String pass, int designID){
		if(!authenticator.authenticateUser(user, pass)){
			return -3;
		}
		try {
			retrieveFaves.setInt(1, designID);
			ResultSet rs = retrieveFaves.executeQuery();
			
			if(rs.first()){
				String list = rs.getString(DBConst.DESIGN_FAVE_LIST);

				// if the list starts with 0 then we don't need to check if the user has already faved this
				if(list!=null){
					if(!UserArrayUtils.checkArrayForUser(list, user)){
						String newArray = (UserArrayUtils.getSizeOfFaveArray(list)+1)+":"+UserArrayUtils.getArrayNamesAsSemiDelim(list)+user+";";
						faveDesign.setString(1, newArray);
					}
					else return -2;
				}
				else{
					faveDesign.setString(1, "1:"+user+";");
				}
			}


			faveDesign.setInt(2, designID);
			faveDesign.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return -1;
		}
		return 0;
	}
	
	/**
	 * Creates a new wormhole
	 * @param coordinate
	 * @param name
	 * @param rotateX
	 * @param rotateY
	 * @param rotateZ
	 * @return the new wormhole's ID on success or a failure value less than zero
	 */
	public int addWormhole(UTMCoordinate coordinate, String name, int cityID, String sessionToken){
		// we're good to go if this is not -1
		// TODO: Work on standardized fail values
		logger.info("off to look for userType!");
		UserType userType = getUserLevel(SessionTracker.getSession(sessionToken).getUser());
		logger.info("Usertype is " + userType.name());
		if(userType.equals(UserType.MODERATOR) || userType.equals(UserType.ADMIN)){
			int createdCoordinate = addCoordinate(coordinate);
			if(createdCoordinate==-1) return createdCoordinate;

			try {
				addWormhole.setInt(1, createdCoordinate);
				addWormhole.setString(2, name);
				addWormhole.setInt(3, cityID);
				addWormhole.executeUpdate();

				ResultSet resultSet = addWormhole.getGeneratedKeys();
				if((resultSet != null) && (resultSet.next())){
					int returned = resultSet.getInt(1);
					logger.info("Wormhole created with ID: " + returned);
					resultSet.close();
					return returned;
				}
				else return -2;
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
		}
		else return -3;
	}

	/**
	 * 
	 * @param wormholeToDelete
	 * @param sessionToken
	 * @return 0 for success, -1 for SQL error, -3 for failed authentication
	 */
	public int deleteWormhole(int wormholeToDelete, String sessionToken){
		UserType userType = getUserLevel(SessionTracker.getSession(sessionToken).getUser());
		if(userType.equals(UserType.MODERATOR) || userType.equals(UserType.ADMIN)){
			try {
				deleteWormhole.setInt(1, wormholeToDelete);
				deleteWormhole.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
			return 0;
		}
		else return -3;
	}
	
	/**
	 * 
	 * @param newLocation
	 * @param wormholeID
	 * @param sessionToken
	 * @return 0 for success, -1 for SQL error, -3 for failed authentication
	 */
	public int changeWormholeLocation(UTMCoordinate newLocation, int wormholeID, String sessionToken){
		UserType userType = getUserLevel(SessionTracker.getSession(sessionToken).getUser());
		if(userType.equals(UserType.MODERATOR) || userType.equals(UserType.ADMIN)){
			try {
				changeWormholeLocation.setInt(1, newLocation.getEasting());
				changeWormholeLocation.setInt(2, newLocation.getNorthing());
				changeWormholeLocation.setString(3, ""+newLocation.getLatZone());
				changeWormholeLocation.setInt(4, newLocation.getLonZone());
				changeWormholeLocation.setInt(5, newLocation.getAltitude());
				changeWormholeLocation.setInt(6, wormholeID);
				changeWormholeLocation.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
			return 0;
		}
		else return -3;
	}
	
	/**
	 * 
	 * @param name
	 * @param wormholeID
	 * @param sessionToken
	 * @return 0 for success, -1 for SQL error, -3 for failed authentication
	 */
	public int changeWormholeName(String name, int wormholeID, String sessionToken){
		UserType userType = getUserLevel(SessionTracker.getSession(sessionToken).getUser());
		if(userType.equals(UserType.MODERATOR) || userType.equals(UserType.ADMIN)){
			try {
				changeWormholeName.setString(1, name);
				changeWormholeName.setInt(2, wormholeID);
				changeWormholeName.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQL ERROR", e);
				return -1;
			}
			return 0;
		}
		else return -3;
	}
	
	public ArrayList<Wormhole> getAllWormholes(){
		try {
			ResultSet rs = getAllWormholes.executeQuery();
			
			ArrayList<Wormhole> wormholes = new ArrayList<Wormhole>();
			
			while(rs.next()){
				wormholes.add(new Wormhole(new UTMCoordinate(rs.getInt(DBConst.COORD_EASTING), rs.getInt(DBConst.COORD_NORTHING), rs.getInt(DBConst.COORD_LONZONE), rs.getString(DBConst.COORD_LATZONE).charAt(0), 0), rs.getString(DBConst.WORMHOLE_NAME), rs.getInt(DBConst.WORMHOLE_CITY)));
			}
			rs.close();
			return wormholes;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return null;
		}
	}
	
	public ArrayList<Wormhole> getAllWormholesInCity(int cityID){
		logger.info("Getting all wormholes in city: " + cityID);
		try {
			getAllWormholesInCity.setInt(1, cityID);
			ResultSet rs = getAllWormholesInCity.executeQuery();
			
			ArrayList<Wormhole> wormholes = new ArrayList<Wormhole>();
			
			while(rs.next()){
				wormholes.add(new Wormhole(new UTMCoordinate(rs.getInt(DBConst.COORD_EASTING), rs.getInt(DBConst.COORD_NORTHING), rs.getInt(DBConst.COORD_LONZONE), rs.getString(DBConst.COORD_LATZONE).charAt(0), 0), rs.getString(DBConst.WORMHOLE_NAME), rs.getInt(DBConst.WORMHOLE_CITY)));
			}
			rs.close();
			return wormholes;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return new ArrayList<Wormhole>();
		}
	}
	
	/**
	 * 
	 * @param location
	 * @param extentNorth
	 * @param extentEast
	 * @return The found wormholes or null if there was an SQL error.
	 */
	public ArrayList<Wormhole> getWormholesWithin(UTMCoordinate location, int extentNorth, int extentEast){
		// order of returned coordinate array: nw, ne, sw, se
		UTMCoordinate[] box = MapManager.createBox(extentNorth, extentEast, SquareCorner.SW, location);
		try {
			getWormholesAtLocation.setString(1, ""+box[2].getLatZone());
			getWormholesAtLocation.setString(2, ""+box[0].getLatZone());
			getWormholesAtLocation.setInt(3, box[0].getLonZone());
			getWormholesAtLocation.setInt(4, box[1].getLonZone());
			getWormholesAtLocation.setInt(5, box[2].getNorthing());
			getWormholesAtLocation.setInt(6, box[0].getNorthing());
			getWormholesAtLocation.setInt(7, box[0].getEasting());
			getWormholesAtLocation.setInt(8, box[1].getEasting());
			ResultSet rs = getWormholesAtLocation.executeQuery();
			
			ArrayList<Wormhole> wormholes = new ArrayList<Wormhole>();
			
			while(rs.next()){
				wormholes.add(new Wormhole(new UTMCoordinate(rs.getInt(DBConst.COORD_EASTING), rs.getInt(DBConst.COORD_NORTHING), rs.getInt(DBConst.COORD_LONZONE), rs.getString(DBConst.COORD_LATZONE).charAt(0), 0), rs.getString(DBConst.WORMHOLE_NAME), rs.getInt(DBConst.WORMHOLE_CITY)));
			}
			rs.close();
			return wormholes;
		} catch (SQLException e) {
			logger.error("SQL ERROR", e);
			return new ArrayList<Wormhole>();
		}
	}
	
	/**
	 * Synchronizes client and server data 
	 * @param hashMap
	 * @return
	 */
	public List<Design> synchronizeData(HashMap<Integer, Integer> hashMap){
		ArrayList<Design> designs = new ArrayList<Design>();
		for(Entry<Integer, Integer> e : hashMap.entrySet()){
			Design onServer = findDesignByID(e.getKey());
			if(e.getValue()!=onServer.hashCode()) designs.add(onServer);
		}
		return designs;
	}
}
