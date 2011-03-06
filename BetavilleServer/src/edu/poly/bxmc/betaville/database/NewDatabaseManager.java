/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.jme.math.Vector3f;

import edu.poly.bxmc.betaville.model.AudibleDesign;
import edu.poly.bxmc.betaville.model.Comment;
import edu.poly.bxmc.betaville.model.ModeledDesign;
import edu.poly.bxmc.betaville.model.SketchedDesign;
import edu.poly.bxmc.betaville.model.VideoDesign;
import edu.poly.bxmc.betaville.model.Design;
import edu.poly.bxmc.betaville.model.UTMCoordinate;

/**
 * Contains all of the queries that interact with the database.
 * @author Skye Book
 *
 */
public class NewDatabaseManager {
	/**
	 * Attribute <name> - Manager of the connection to the database
	 */
	private DataBaseConnection dbConnection;

	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager() {
		dbConnection = new DataBaseConnection("root", "root");
	}
	
	/**
	 * Constructor - Create the manager of the DB
	 */
	public NewDatabaseManager(String password) {
		dbConnection = new DataBaseConnection("root", password);
	}
	
	public void closeConnection(){
		dbConnection.closeConnection();
	}
	
	public boolean addUser(String user, String pass, String email, String twitter, String bio){
		try {
			dbConnection.sendUpdate("INSERT INTO `"+DBConst.USER_TABLE+"` (`"+DBConst.USER_NAME+"`, `"+DBConst.USER_PASS+"`, `"+DBConst.USER_TWITTER+"`, `"+DBConst.USER_EMAIL+"`, `"+DBConst.USER_BIO+"`) VALUES" +
					"('"+user+"',MD5('"+pass+"'),'"+twitter+"','"+email+"','"+bio+"');");
		} catch (SQLException e) {
			e.printStackTrace();
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
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DBConst.USER_TABLE+" WHERE "+DBConst.USER_NAME+" = '"+user+"';");
			if(rs.first()){
				System.out.println("name unavailable");
				return false;
			}
			else{
				System.out.println("name available");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean changePassword(String user, String pass, String newPass){
		if(authenticateUser(user, pass)){
			// change password
			return true;
		}
		else return false;
	}

	public boolean changeBio(String user, String pass, String newBio){
		if(authenticateUser(user, pass)){
			// change bio
			return true;
		}
		else return false;
	}
	
	public String getUserEmail(String user){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT" + DBConst.USER_EMAIL + " FROM " + DBConst.USER_TABLE + " WHERE " + DBConst.USER_NAME + " = '"+user+"';");
			if(rs.first()){
				return rs.getString(DBConst.USER_EMAIL);
			}
			else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean authenticateUser(String user, String pass){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DBConst.USER_TABLE+" WHERE "+DBConst.USER_NAME+" = '"+user+"' AND "+DBConst.USER_PASS+" = MD5('"+pass+"');");
			if(rs.first()){
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Adds a new design to the database
	 * @param design
	 * @param user
	 * @param pass
	 * @param sourceID  The designID of the <code>Design</code> on which this new design is building.  Give 0 for designs
	 * with no predecessors.
	 * @return The new design's ID, or -1 for SQL error, -2 for unsupported <code>Design</code>, or -3 for failed authentication
	 */
	public int addDesign(Design design, String user, String pass, int sourceID, String fileExtension){
		if(authenticateUser(user, pass)){
			String designName = design.getName().replaceAll("'", "\'");
			String designDescription = design.getDescription().replaceAll("'", "\'");
			String designAddress = design.getAddress().replaceAll("'", "\'");
			int privacy;
			if(design.isPublic()) privacy=1;
			else privacy=0;
			int coordinateID=addCoordinate(design.getCoordinate());
			int designID;
			try {
				if(design instanceof ModeledDesign){
					int texturedValue=0;
					if(((ModeledDesign)design).isTextured()) texturedValue=1;
					System.out.println("User from design: " + design.getUser() + " | User from argument: "+user);
					dbConnection.sendUpdate("INSERT INTO "+DBConst.DESIGN_TABLE+" (`"+DBConst.DESIGN_NAME+"`, `"+DBConst.DESIGN_FILE+"`, `"+DBConst.DESIGN_CITY+"`, `"+DBConst.DESIGN_USER+"`, `"+DBConst.DESIGN_COORDINATE+"`, `"+DBConst.DESIGN_DATE+"`, `"+DBConst.DESIGN_PRIVACY+"`, `"+DBConst.DESIGN_DESCRIPTION+"`, `"+DBConst.DESIGN_URL+"`, `"+DBConst.DESIGN_TYPE+"`, `"+DBConst.DESIGN_ADDRESS+"`) VALUES ('"+designName+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+designDescription+"','"+design.getURL()+"','model','"+designAddress+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_FILE + " = '"+designID+design.getFilepath().substring(design.getFilepath().lastIndexOf("."), design.getFilepath().length())+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
					dbConnection.sendUpdate("INSERT INTO "+DBConst.MODEL_TABLE+" (`"+DBConst.MODEL_ID+"`, `"+DBConst.MODEL_ROTATION+"`, `"+DBConst.MODEL_TEX+"`) VALUES ("+designID+","+((ModeledDesign)design).getRotation()+", "+texturedValue+");");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof SketchedDesign){
					dbConnection.sendUpdate("INSERT INTO "+DBConst.DESIGN_TABLE+" (`"+DBConst.DESIGN_NAME+"`, `"+DBConst.DESIGN_FILE+"`, `"+DBConst.DESIGN_CITY+"`, `"+DBConst.DESIGN_USER+"`, `"+DBConst.DESIGN_COORDINATE+"`, `"+DBConst.DESIGN_DATE+"`, `"+DBConst.DESIGN_PRIVACY+"`, `"+DBConst.DESIGN_DESCRIPTION+"`, `"+DBConst.DESIGN_URL+"`, `"+DBConst.DESIGN_TYPE+"`, `"+DBConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','sketch','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DBConst.SKETCH_TABLE+" (`"+DBConst.SKETCH_ID+"`, `"+DBConst.SKETCH_ROTATION+"`, `"+DBConst.SKETCH_UPPLANE+"`) VALUES ("+designID+","+((SketchedDesign)design).getRotation()+",'"+((SketchedDesign)design).getUpPlane()+"');");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof AudibleDesign){
					dbConnection.sendUpdate("INSERT INTO "+DBConst.DESIGN_TABLE+" (`"+DBConst.DESIGN_NAME+"`, `"+DBConst.DESIGN_FILE+"`, `"+DBConst.DESIGN_CITY+"`, `"+DBConst.DESIGN_USER+"`, `"+DBConst.DESIGN_COORDINATE+"`, `"+DBConst.DESIGN_DATE+"`, `"+DBConst.DESIGN_PRIVACY+"`, `"+DBConst.DESIGN_DESCRIPTION+"`, `"+DBConst.DESIGN_URL+"`, `"+DBConst.DESIGN_TYPE+"`, `"+DBConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','audio','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DBConst.AUDIO_TABLE+" (`"+DBConst.AUDIO_ID+"`, `"+DBConst.AUDIO_DIRECTIONX+"`, `"+DBConst.AUDIO_DIRECTIONY+"`, `"+DBConst.AUDIO_DIRECTIONZ+"`) VALUES ("+designID+","+((AudibleDesign)design).getDirection().getX()+","+((AudibleDesign)design).getDirection().getY()+","+((AudibleDesign)design).getDirection().getZ()+");");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof VideoDesign){
					dbConnection.sendUpdate("INSERT INTO "+DBConst.DESIGN_TABLE+" (`"+DBConst.DESIGN_NAME+"`, `"+DBConst.DESIGN_FILE+"`, `"+DBConst.DESIGN_CITY+"`, `"+DBConst.DESIGN_USER+"`, `"+DBConst.DESIGN_COORDINATE+"`, `"+DBConst.DESIGN_DATE+"`, `"+DBConst.DESIGN_PRIVACY+"`, `"+DBConst.DESIGN_DESCRIPTION+"`, `"+DBConst.DESIGN_URL+"`, `"+DBConst.DESIGN_TYPE+"`, `"+DBConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','video','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DBConst.VIDEO_TABLE+" (`"+DBConst.VIDEO_ID+"`, `"+DBConst.VIDEO_DIRECTIONX+"`, `"+DBConst.VIDEO_DIRECTIONY+"`, `"+DBConst.VIDEO_DIRECTIONZ+"`) VALUES ("+designID+","+((VideoDesign)design).getDirection().getX()+","+((VideoDesign)design).getDirection().getY()+","+((VideoDesign)design).getDirection().getZ()+");");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					changeDesignFilename(designID, fileExtension);
					return designID;
				}
				return -2;
			} catch (SQLException e) {
				e.printStackTrace();
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
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DBConst.USER_TABLE+" WHERE "+DBConst.USER_NAME+" = '"+user+"' AND "+DBConst.USER_PASS+" = MD5('"+pass+"');");
			if(rs.first()){
				ResultSet stepTwo = dbConnection.sendQuery("SELECT * FROM "+DBConst.DESIGN_TABLE+" WHERE "+DBConst.DESIGN_USER+" = '"+user+"' AND "+DBConst.DESIGN_ID+" = "+designID+";");
				if(stepTwo.first()){
					return true;
				} else return false;
			} else return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void changeDesignFilename(int designID, String fileExtension){
		try {
			dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_FILE + " = '"+designID+"."+fileExtension+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean changeDesignName(int designID, String newName, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)){
			try {
				dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_NAME + " = '"+newName+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		else return false;
	}
	
	public boolean changeDesignDescription(int designID, String newDescription, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)){
			try {
				dbConnection.sendUpdate("UPDATE " + DBConst.DESIGN_TABLE + " SET " + DBConst.DESIGN_DESCRIPTION + " = '"+newDescription+"' WHERE "+DBConst.DESIGN_ID+" = "+designID+";");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		else return false;
	}
	
	public boolean changeModeledDesignLocation(int designID, int rotY, UTMCoordinate newLocation, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)){
			try {
				ResultSet rs = dbConnection.sendQuery("SELECT " + DBConst.DESIGN_COORDINATE + " FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_ID + " = " + designID + ";");
				System.out.println("Getting Coordinate for Design: " + designID);
				if(rs.first()){
					System.out.println("has a result");
					int coordinateID = rs.getInt(DBConst.DESIGN_COORDINATE);
					System.out.println("found " + coordinateID);
					changeCoordinate(coordinateID, newLocation);
					dbConnection.sendUpdate("UPDATE " + DBConst.MODEL_TABLE + " SET "
							+ DBConst.MODEL_ROTATION + "="+rotY+
							" WHERE " + DBConst.MODEL_ID + " = " + designID +";");
					return true;
				}else return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		else return false;
	}
	
	public String getDesignType(int designID){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT " + DBConst.DESIGN_TYPE + " FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_ID + "=" + designID);
			if(rs.first()){
				return rs.getString(DBConst.DESIGN_TYPE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Design designFromResultSet(ResultSet drs){
		try {
			int id = drs.getInt(DBConst.DESIGN_ID);
			String type = drs.getString(DBConst.DESIGN_TYPE);
			UTMCoordinate utm = retrieveCoordinate(drs.getInt(DBConst.DESIGN_COORDINATE));
			
			String designName = drs.getString(DBConst.DESIGN_NAME);
			String designAddress = drs.getString(DBConst.DESIGN_ADDRESS);
			int designCity = drs.getInt(DBConst.DESIGN_CITY);
			String designUser = drs.getString(DBConst.DESIGN_USER);
			String designDescription = drs.getString(DBConst.DESIGN_DESCRIPTION);
			String designFile = drs.getString(DBConst.DESIGN_FILE);
			String designURL = drs.getString(DBConst.DESIGN_URL);
			boolean designPrivacy = drs.getBoolean(DBConst.DESIGN_PRIVACY);
			
			if(type.equals(DBConst.DESIGN_TYPE_MODEL)){
				ResultSet mrs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				mrs.first();
				int designRotation = mrs.getInt(DBConst.MODEL_ROTATION);
				boolean designIsTextured = mrs.getBoolean(DBConst.MODEL_TEX);
				ModeledDesign md =  new ModeledDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, designRotation, designIsTextured);
				md.setID(id);
				return md;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_SKETCH)){
				ResultSet srs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				srs.first();
				SketchedDesign sd = new SketchedDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, srs.getInt(DBConst.SKETCH_ROTATION), srs.getString(DBConst.SKETCH_UPPLANE).charAt(0));
				sd.setID(id);
				return sd;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_AUDIO)){
				ResultSet ars = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				ars.first();
				Vector3f direction = new Vector3f(ars.getFloat(DBConst.AUDIO_DIRECTIONX),ars.getFloat(DBConst.AUDIO_DIRECTIONY),ars.getFloat(DBConst.AUDIO_DIRECTIONZ));
				AudibleDesign ad = new AudibleDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, direction, ars.getInt(DBConst.AUDIO_VOLUME));
				ad.setID(id);
				return ad;
			}
			else if(type.equals(DBConst.DESIGN_TYPE_VIDEO)){
				ResultSet vrs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MODEL_TABLE + " WHERE " + DBConst.MODEL_ID + " = " + id +";");
				vrs.first();
				Vector3f direction = new Vector3f(vrs.getFloat(DBConst.VIDEO_DIRECTIONX),vrs.getFloat(DBConst.VIDEO_DIRECTIONY),vrs.getFloat(DBConst.VIDEO_DIRECTIONZ));
				VideoDesign vd = new VideoDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, direction, vrs.getInt(DBConst.AUDIO_VOLUME));
				vd.setID(id);
				return vd;
			}
			else return null;
		} catch (SQLException e) {
			e.printStackTrace();
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
		if(verifyDesignOwnership(designID, user, pass)){
			try {
				String designType = getDesignType(designID);
				if(designType.equals(DBConst.DESIGN_TYPE_AUDIO)){
					dbConnection.sendUpdate("DELETE from " + DBConst.DESIGN_TABLE +", "+ DBConst.AUDIO_TABLE + " USING " + DBConst.DESIGN_TABLE + " INNER JOIN " + DBConst.AUDIO_TABLE + " ON " + DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_ID +"="+ DBConst.AUDIO_TABLE+"."+DBConst.AUDIO_ID + " WHERE " + DBConst.AUDIO_TABLE+"."+DBConst.AUDIO_ID+"="+designID);
				}
				else if(designType.equals(DBConst.DESIGN_TYPE_VIDEO)){
					dbConnection.sendUpdate("DELETE from " + DBConst.DESIGN_TABLE +", "+ DBConst.VIDEO_TABLE + " USING " + DBConst.DESIGN_TABLE + " INNER JOIN " + DBConst.VIDEO_TABLE + " ON " + DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_ID +"="+ DBConst.VIDEO_TABLE+"."+DBConst.VIDEO_ID + " WHERE " + DBConst.VIDEO_TABLE+"."+DBConst.VIDEO_ID+"="+designID);
				}
				else if(designType.equals(DBConst.DESIGN_TYPE_SKETCH)){
					dbConnection.sendUpdate("DELETE from " + DBConst.DESIGN_TABLE +", "+ DBConst.SKETCH_TABLE + " USING " + DBConst.DESIGN_TABLE + " INNER JOIN " + DBConst.SKETCH_TABLE + " ON " + DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_ID +"="+ DBConst.SKETCH_TABLE+"."+DBConst.SKETCH_ID + " WHERE " + DBConst.SKETCH_TABLE+"."+DBConst.SKETCH_ID+"="+designID);
				}
				else if(designType.equals(DBConst.DESIGN_TYPE_MODEL)){
					dbConnection.sendUpdate("DELETE from " + DBConst.DESIGN_TABLE +", "+ DBConst.MODEL_TABLE + " USING " + DBConst.DESIGN_TABLE + " INNER JOIN " + DBConst.MODEL_TABLE + " ON " + DBConst.DESIGN_TABLE+"."+DBConst.DESIGN_ID +"="+ DBConst.MODEL_TABLE+"."+DBConst.MODEL_ID + " WHERE " + DBConst.MODEL_TABLE+"."+DBConst.MODEL_ID+"="+designID);
				}
				return 0;
			} catch (SQLException e) {
				e.printStackTrace();
				return -1;
			}
		}
		else return -3;
	}
	
	public Design findDesignByID(int id){
		try {
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_ID + " = " + id +";");
			if(drs.first()){
				return designFromResultSet(drs);
			}
			else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Vector<Design> findDesignsByName(String name){
		try {
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_NAME + " LIKE '" + name +"';");
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				designs.add(designFromResultSet(drs));
			}
			return designs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Vector<Design> findDesignsByUser(String user){
		try {
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_USER + " = '" + user +"';");
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				System.out.println("Designs from user: " +designs.size());
				designs.add(designFromResultSet(drs));
			}
			return designs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// TODO: findDesignsByDate
	public Vector<Design> findDesignsByDate(String date){
		return null;
	}
	
	public Vector<Design> findDesignsByCity(int cityID){
		try {
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_CITY + " = " + cityID +";");
			Vector<Design> designs = new Vector<Design>();
			while(drs.next()){
				designs.add(designFromResultSet(drs));
			}
			return designs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addProposal(int source, int destination){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DBConst.PROPOSAL_TABLE+" (`"+DBConst.PROPOSE_SOURCE+"`, `"+DBConst.PROPOSE_DEST+"`) VALUES ("+source+","+destination+");");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Finds a design proposed on top of the specified design
	 * @param source
	 * @return ID of new proposal, or 0 if none are found, or -1 for an SQL error.
	 */
	public int findNewerProposal(int source){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT " + DBConst.PROPOSE_DEST + " FROM " + DBConst.PROPOSAL_TABLE + " WHERE " + DBConst.PROPOSE_SOURCE + " = " + source + ";");
			if(rs.first()){
				return rs.getInt(DBConst.PROPOSE_DEST);
			}
			else return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Finds the design that the supplied design was
	 * proposed on top of
	 * @param destination
	 * @return
	 */
	public int findOlderProposal(int destination){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT " + DBConst.PROPOSE_SOURCE + " FROM " + DBConst.PROPOSAL_TABLE + " WHERE " + DBConst.PROPOSE_DEST + " = " + destination + ";");
			if(rs.first()){
				return rs.getInt(DBConst.PROPOSE_SOURCE);
			}
			else return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
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
			System.out.println("checking for city");
			if(findCityByAll(name, state, country)==null){
				System.out.println("adding city");
				dbConnection.sendUpdate("INSERT INTO "+DBConst.CITY_TABLE+" (`"+DBConst.CITY_NAME+"`, `"+DBConst.CITY_STATE+"`, `"+DBConst.CITY_COUNTRY+"`) VALUES ('"+name+"','"+state+"','"+country+"');");
				return dbConnection.getLastKey();
			}
			else return -2;
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public Vector<Integer> findCitiesByName(String cityName){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			cityResult = dbConnection.sendQuery("SELECT "+DBConst.CITY_ID+" FROM "+DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_NAME+" LIKE '"+cityName+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			return results;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Vector<Integer> findCitiesByState(String state){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			cityResult = dbConnection.sendQuery("SELECT "+DBConst.CITY_ID+" FROM "+DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_STATE+" LIKE '"+state+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			return results;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Vector<Integer> findCitiesByCountry(String country){
		Vector<Integer> results = new Vector<Integer>();
		ResultSet cityResult;
		try {
			cityResult = dbConnection.sendQuery("SELECT "+DBConst.CITY_ID+" FROM "+DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_COUNTRY+" LIKE '"+country+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DBConst.CITY_ID));
			}
			return results;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String[] findCityByID(int id){
		ResultSet cityResult;
		try {
			cityResult = dbConnection.sendQuery("SELECT * FROM "+DBConst.CITY_TABLE+" WHERE "+DBConst.CITY_ID+" = '"+id+"'");
			if(cityResult.first()){
				return new String[]{cityResult.getString("cityName"),cityResult.getString("state"),cityResult.getString("country")};
			}
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String[] findCityByAll(String name, String state, String country){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.CITY_TABLE +" WHERE " + DBConst.CITY_NAME + " = '"+name+"'"
					+ " AND " + DBConst.CITY_STATE + " = '"+state+"'"
					+ " AND " + DBConst.CITY_COUNTRY + " = '"+country+"';");
			if(rs.first()){
				return new String[]{name, state, country};
			}
			else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int addCoordinate(UTMCoordinate utm){
		return addCoordinate(utm.getEasting(), utm.getNorthing(), utm.getLonZone(), utm.getLatZone(), utm.getAltitude());
	}
	
	public int addCoordinate(int easting, int northing, int lonZone, char latZone, int altitude){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DBConst.COORD_TABLE+" (`"+DBConst.COORD_NORTHING+"`, `"+DBConst.COORD_EASTING+"`, `"+DBConst.COORD_LATZONE+"`, `"+DBConst.COORD_LONZONE+"`, `"+DBConst.COORD_ALTITUDE+"`) VALUES ("+northing+","+easting+",'"+latZone+"',"+lonZone+","+altitude+");");
			return dbConnection.getLastKey();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void changeCoordinate(int coordinateID, UTMCoordinate utm){
		changeCoordinate(coordinateID, utm.getEasting(), utm.getNorthing(), utm.getLonZone(), utm.getLatZone(), utm.getAltitude());
	}
	
	public void changeCoordinate(int coordinateID, int easting, int northing, int lonZone, char latZone, int altitude){
		try {
			dbConnection.sendUpdate("UPDATE " + DBConst.COORD_TABLE + " SET "
					+ DBConst.COORD_EASTING + "="+easting+","
					+ DBConst.COORD_NORTHING + "="+northing+","
					+ DBConst.COORD_LATZONE + "='"+latZone+"',"
					+ DBConst.COORD_LONZONE + "="+lonZone+","
					+ DBConst.COORD_ALTITUDE + "="+altitude+
					" WHERE " + DBConst.COORD_ID + " = " + coordinateID +";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public UTMCoordinate retrieveCoordinate(int coordinateID){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.COORD_TABLE + " WHERE " + DBConst.COORD_ID + " = " + coordinateID + ";");
			if(rs.first()){
				return new UTMCoordinate(rs.getInt(DBConst.COORD_EASTING), rs.getInt(DBConst.COORD_NORTHING), rs.getInt(DBConst.COORD_LONZONE), rs.getString(DBConst.COORD_LATZONE).charAt(0), rs.getInt(DBConst.COORD_ALTITUDE));
			}
			else return null;
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	public void addVote(int designID, String user, String pass, boolean voteUp){
		try {
			int vote;
			if(voteUp) vote=1;
			else vote=0;
			dbConnection.sendUpdate("INSERT INTO "+DBConst.VOTE_TABLE+" (`"+DBConst.VOTE_VALUE+"`, `"+DBConst.VOTE_USER+"`, `"+DBConst.VOTE_DESIGN+"`) VALUES ("+vote+",'"+user+"',"+designID+");");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void changeVote(int designID, String user, String pass){
		if(authenticateUser(user, pass)){
			try {
				ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.VOTE_TABLE + " WHERE " + DBConst.VOTE_DESIGN +" = " + designID + " AND " + DBConst.VOTE_USER + " = '"+user+"';");
				if(!rs.first()) return;
				int voteID = rs.getInt(DBConst.VOTE_ID);
				int newVote;
				if(rs.getBoolean(DBConst.VOTE_VALUE)){
					newVote = 0;
				}
				else newVote=1;
				dbConnection.sendUpdate("Update "+DBConst.VOTE_TABLE+ " SET " + DBConst.VOTE_VALUE + " = " + newVote + " WHERE " + DBConst.VOTE_ID +" = " + voteID+";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean addComment(Comment comment, String pass){
		if(authenticateUser(comment.getUser(),pass)){
			try {
				dbConnection.sendUpdate("INSERT INTO "+DBConst.COMMENT_TABLE+" (`"+DBConst.COMMENT_DESIGN+"`, `"+DBConst.COMMENT_USER+"`, `"+DBConst.COMMENT_TEXT+"`, `"+DBConst.COMMENT_DATE+"`, `"+DBConst.COMMENT_REPLIESTO+"`) VALUES ("+comment.getDesignID()+",'"+comment.getUser()+"','"+comment.getComment()+"',NOW(),"+comment.repliesTo()+");");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		else return false;
	}

	// TODO Needs Testing
	public boolean deleteComment(int commentID, String user, String password){
		if(authenticateUser(user, password)){
			try {
				dbConnection.sendUpdate("DELETE FROM " + DBConst.COMMENT_TABLE + " WHERE " + DBConst.COMMENT_ID + " = " + commentID);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public void reportSpamComment(int commentID){
		try {
			dbConnection.sendUpdate("UPDATE " + DBConst.COMMENT_TABLE + " SET " + DBConst.COMMENT_SPAMFLAG + " = 1 WHERE " + DBConst.COMMENT_ID + " = " + commentID + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Vector<Comment> getComments(int designID){
		Vector<Comment> comments = new Vector<Comment>();
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.COMMENT_TABLE + " WHERE " + DBConst.COMMENT_DESIGN + "=" + designID);
			while(rs.next()){
				// Ignore any comments that are verified spam
				if(!rs.getBoolean(DBConst.COMMENT_SPAMVERIFIED)){
					comments.add(new Comment(rs.getInt(DBConst.COMMENT_ID), designID, rs.getString(DBConst.COMMENT_USER), rs.getString(DBConst.COMMENT_TEXT), rs.getInt(DBConst.COMMENT_REPLIESTO), rs.getDate(DBConst.COMMENT_DATE).toString()));
				}
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
		if(comments.isEmpty()){
			comments.add(new Comment(0,0,null, null));
		}
		return comments;
	}
	
	public boolean addModerator(String user){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DBConst.MOD_TABLE+" (`"+DBConst.MOD_NAME+"`, `"+DBConst.MOD_LEVEL+"`) VALUES ('"+user+"',2);");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean addAdministrator(String user){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DBConst.MOD_TABLE+" (`"+DBConst.MOD_NAME+"`, `"+DBConst.MOD_LEVEL+"`) VALUES ('"+user+"',1);");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean checkModeratorStatus(String user){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MOD_TABLE + " WHERE " + DBConst.MOD_NAME + " = '"+user+"' AND " +
					DBConst.MOD_LEVEL + " = 2" + ";");
			if(rs.first()) return true;
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean checkAdminStatus(String user){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DBConst.MOD_TABLE + " WHERE " + DBConst.MOD_NAME + " = '"+user+"' AND " +
					DBConst.MOD_LEVEL + " = 1" + ";");
			if(rs.first()) return true;
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
