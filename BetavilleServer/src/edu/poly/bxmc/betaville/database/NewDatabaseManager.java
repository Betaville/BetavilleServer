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
import edu.poly.bxmc.betaville.model.ModeledDesign;
import edu.poly.bxmc.betaville.model.SketchedDesign;
import edu.poly.bxmc.betaville.model.VideoDesign;
import edu.poly.bxmc.betaville.model.Design;
import edu.poly.bxmc.betaville.model.UTMCoordinate;

/**
 * 
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
			dbConnection.sendUpdate("INSERT INTO `"+DatabaseConst.USER_TABLE+"` (`"+DatabaseConst.USER_NAME+"`, `"+DatabaseConst.USER_PASS+"`, `"+DatabaseConst.USER_TWITTER+"`, `"+DatabaseConst.USER_EMAIL+"`, `"+DatabaseConst.USER_BIO+"`) VALUES" +
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
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DatabaseConst.USER_TABLE+" WHERE "+DatabaseConst.USER_NAME+" = '"+user+"';");
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
			ResultSet rs = dbConnection.sendQuery("SELECT" + DatabaseConst.USER_EMAIL + " FROM " + DatabaseConst.USER_TABLE + " WHERE " + DatabaseConst.USER_NAME + " = '"+user+"';");
			if(rs.first()){
				return rs.getString(DatabaseConst.USER_EMAIL);
			}
			else return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean authenticateUser(String user, String pass){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DatabaseConst.USER_TABLE+" WHERE "+DatabaseConst.USER_NAME+" = '"+user+"' AND "+DatabaseConst.USER_PASS+" = MD5('"+pass+"');");
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
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.DESIGN_TABLE+" (`"+DatabaseConst.DESIGN_NAME+"`, `"+DatabaseConst.DESIGN_FILE+"`, `"+DatabaseConst.DESIGN_CITY+"`, `"+DatabaseConst.DESIGN_USER+"`, `"+DatabaseConst.DESIGN_COORDINATE+"`, `"+DatabaseConst.DESIGN_DATE+"`, `"+DatabaseConst.DESIGN_PRIVACY+"`, `"+DatabaseConst.DESIGN_DESCRIPTION+"`, `"+DatabaseConst.DESIGN_URL+"`, `"+DatabaseConst.DESIGN_TYPE+"`, `"+DatabaseConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','model','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("UPDATE " + DatabaseConst.DESIGN_TABLE + " SET " + DatabaseConst.DESIGN_FILE + " = '"+designID+design.getFilepath().substring(design.getFilepath().lastIndexOf("."), design.getFilepath().length())+"' WHERE "+DatabaseConst.DESIGN_ID+" = "+designID+";");
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.MODEL_TABLE+" (`"+DatabaseConst.MODEL_ID+"`, `"+DatabaseConst.MODEL_ROTATION+"`, `"+DatabaseConst.MODEL_TEX+"`) VALUES ("+designID+","+((ModeledDesign)design).getRotation()+", "+texturedValue+");");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof SketchedDesign){
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.DESIGN_TABLE+" (`"+DatabaseConst.DESIGN_NAME+"`, `"+DatabaseConst.DESIGN_FILE+"`, `"+DatabaseConst.DESIGN_CITY+"`, `"+DatabaseConst.DESIGN_USER+"`, `"+DatabaseConst.DESIGN_COORDINATE+"`, `"+DatabaseConst.DESIGN_DATE+"`, `"+DatabaseConst.DESIGN_PRIVACY+"`, `"+DatabaseConst.DESIGN_DESCRIPTION+"`, `"+DatabaseConst.DESIGN_URL+"`, `"+DatabaseConst.DESIGN_TYPE+"`, `"+DatabaseConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','sketch','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.SKETCH_TABLE+" (`"+DatabaseConst.SKETCH_ID+"`, `"+DatabaseConst.SKETCH_ROTATION+"`, `"+DatabaseConst.SKETCH_UPPLANE+"`) VALUES ("+designID+","+((SketchedDesign)design).getRotation()+",'"+((SketchedDesign)design).getUpPlane()+"');");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof AudibleDesign){
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.DESIGN_TABLE+" (`"+DatabaseConst.DESIGN_NAME+"`, `"+DatabaseConst.DESIGN_FILE+"`, `"+DatabaseConst.DESIGN_CITY+"`, `"+DatabaseConst.DESIGN_USER+"`, `"+DatabaseConst.DESIGN_COORDINATE+"`, `"+DatabaseConst.DESIGN_DATE+"`, `"+DatabaseConst.DESIGN_PRIVACY+"`, `"+DatabaseConst.DESIGN_DESCRIPTION+"`, `"+DatabaseConst.DESIGN_URL+"`, `"+DatabaseConst.DESIGN_TYPE+"`, `"+DatabaseConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','audio','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.AUDIO_TABLE+" (`"+DatabaseConst.AUDIO_ID+"`, `"+DatabaseConst.AUDIO_DIRECTIONX+"`, `"+DatabaseConst.AUDIO_DIRECTIONY+"`, `"+DatabaseConst.AUDIO_DIRECTIONZ+"`) VALUES ("+designID+","+((AudibleDesign)design).getDirection().getX()+","+((AudibleDesign)design).getDirection().getY()+","+((AudibleDesign)design).getDirection().getZ()+");");
					if(sourceID!=0){
						addProposal(sourceID, designID);
					}
					return designID;
				}
				else if(design instanceof VideoDesign){
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.DESIGN_TABLE+" (`"+DatabaseConst.DESIGN_NAME+"`, `"+DatabaseConst.DESIGN_FILE+"`, `"+DatabaseConst.DESIGN_CITY+"`, `"+DatabaseConst.DESIGN_USER+"`, `"+DatabaseConst.DESIGN_COORDINATE+"`, `"+DatabaseConst.DESIGN_DATE+"`, `"+DatabaseConst.DESIGN_PRIVACY+"`, `"+DatabaseConst.DESIGN_DESCRIPTION+"`, `"+DatabaseConst.DESIGN_URL+"`, `"+DatabaseConst.DESIGN_TYPE+"`, `"+DatabaseConst.DESIGN_ADDRESS+"`) VALUES ('"+design.getName()+"','"+design.getFilepath()+"',"+design.getCityID()+",'"+design.getUser()+"',"+coordinateID+",NOW(),"+privacy+",'"+design.getDescription()+"','"+design.getURL()+"','video','"+design.getAddress()+"');");
					designID = dbConnection.getLastKey();
					dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.VIDEO_TABLE+" (`"+DatabaseConst.VIDEO_ID+"`, `"+DatabaseConst.VIDEO_DIRECTIONX+"`, `"+DatabaseConst.VIDEO_DIRECTIONY+"`, `"+DatabaseConst.VIDEO_DIRECTIONZ+"`) VALUES ("+designID+","+((VideoDesign)design).getDirection().getX()+","+((VideoDesign)design).getDirection().getY()+","+((VideoDesign)design).getDirection().getZ()+");");
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
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM "+DatabaseConst.USER_TABLE+" WHERE "+DatabaseConst.USER_NAME+" = '"+user+"' AND "+DatabaseConst.USER_PASS+" = MD5('"+pass+"');");
			if(rs.first()){
				ResultSet stepTwo = dbConnection.sendQuery("SELECT * FROM "+DatabaseConst.DESIGN_TABLE+" WHERE "+DatabaseConst.DESIGN_USER+" = '"+user+"' AND "+DatabaseConst.DESIGN_ID+" = "+designID+";");
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
			dbConnection.sendUpdate("UPDATE " + DatabaseConst.DESIGN_TABLE + " SET " + DatabaseConst.DESIGN_FILE + " = '"+designID+"."+fileExtension+"' WHERE "+DatabaseConst.DESIGN_ID+" = "+designID+";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean changeDesignName(int designID, String newName, String user, String pass){
		if(verifyDesignOwnership(designID, user, pass)){
			try {
				dbConnection.sendUpdate("UPDATE " + DatabaseConst.DESIGN_TABLE + " SET " + DatabaseConst.DESIGN_NAME + " = '"+newName+"' WHERE "+DatabaseConst.DESIGN_ID+" = "+designID+";");
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
				dbConnection.sendUpdate("UPDATE " + DatabaseConst.DESIGN_TABLE + " SET " + DatabaseConst.DESIGN_DESCRIPTION + " = '"+newDescription+"' WHERE "+DatabaseConst.DESIGN_ID+" = "+designID+";");
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
				ResultSet rs = dbConnection.sendQuery("SELECT " + DatabaseConst.DESIGN_COORDINATE + " FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_ID + " = " + designID + ";");
				System.out.println("Getting Coordinate for Design: " + designID);
				if(rs.first()){
					System.out.println("has a result");
					int coordinateID = rs.getInt(DatabaseConst.DESIGN_COORDINATE);
					System.out.println("found " + coordinateID);
					changeCoordinate(coordinateID, newLocation);
					dbConnection.sendUpdate("UPDATE " + DatabaseConst.MODEL_TABLE + " SET "
							+ DatabaseConst.MODEL_ROTATION + "="+rotY+
							" WHERE " + DatabaseConst.MODEL_ID + " = " + designID +";");
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
			ResultSet rs = dbConnection.sendQuery("SELECT " + DatabaseConst.DESIGN_TYPE + " FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_ID + "=" + designID);
			if(rs.first()){
				return rs.getString(DatabaseConst.DESIGN_TYPE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Design designFromResultSet(ResultSet drs){
		try {
			int id = drs.getInt(DatabaseConst.DESIGN_ID);
			String type = drs.getString(DatabaseConst.DESIGN_TYPE);
			UTMCoordinate utm = retrieveCoordinate(drs.getInt(DatabaseConst.DESIGN_COORDINATE));
			
			String designName = drs.getString(DatabaseConst.DESIGN_NAME);
			String designAddress = drs.getString(DatabaseConst.DESIGN_ADDRESS);
			int designCity = drs.getInt(DatabaseConst.DESIGN_CITY);
			String designUser = drs.getString(DatabaseConst.DESIGN_USER);
			String designDescription = drs.getString(DatabaseConst.DESIGN_DESCRIPTION);
			String designFile = drs.getString(DatabaseConst.DESIGN_FILE);
			String designURL = drs.getString(DatabaseConst.DESIGN_URL);
			boolean designPrivacy = drs.getBoolean(DatabaseConst.DESIGN_PRIVACY);
			
			if(type.equals(DatabaseConst.DESIGN_TYPE_MODEL)){
				ResultSet mrs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MODEL_TABLE + " WHERE " + DatabaseConst.MODEL_ID + " = " + id +";");
				mrs.first();
				int designRotation = mrs.getInt(DatabaseConst.MODEL_ROTATION);
				boolean designIsTextured = mrs.getBoolean(DatabaseConst.MODEL_TEX);
				ModeledDesign md =  new ModeledDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, designRotation, designIsTextured);
				md.setID(id);
				return md;
			}
			else if(type.equals(DatabaseConst.DESIGN_TYPE_SKETCH)){
				ResultSet srs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MODEL_TABLE + " WHERE " + DatabaseConst.MODEL_ID + " = " + id +";");
				srs.first();
				SketchedDesign sd = new SketchedDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, srs.getInt(DatabaseConst.SKETCH_ROTATION), srs.getString(DatabaseConst.SKETCH_UPPLANE).charAt(0));
				sd.setID(id);
				return sd;
			}
			else if(type.equals(DatabaseConst.DESIGN_TYPE_AUDIO)){
				ResultSet ars = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MODEL_TABLE + " WHERE " + DatabaseConst.MODEL_ID + " = " + id +";");
				ars.first();
				Vector3f direction = new Vector3f(ars.getFloat(DatabaseConst.AUDIO_DIRECTIONX),ars.getFloat(DatabaseConst.AUDIO_DIRECTIONY),ars.getFloat(DatabaseConst.AUDIO_DIRECTIONZ));
				AudibleDesign ad = new AudibleDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, direction, ars.getInt(DatabaseConst.AUDIO_VOLUME));
				ad.setID(id);
				return ad;
			}
			else if(type.equals(DatabaseConst.DESIGN_TYPE_VIDEO)){
				ResultSet vrs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MODEL_TABLE + " WHERE " + DatabaseConst.MODEL_ID + " = " + id +";");
				vrs.first();
				Vector3f direction = new Vector3f(vrs.getFloat(DatabaseConst.VIDEO_DIRECTIONX),vrs.getFloat(DatabaseConst.VIDEO_DIRECTIONY),vrs.getFloat(DatabaseConst.VIDEO_DIRECTIONZ));
				VideoDesign vd = new VideoDesign(designName, utm, designAddress, designCity, designUser, designDescription, designFile, designURL, designPrivacy, direction, vrs.getInt(DatabaseConst.AUDIO_VOLUME));
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
	 * DO NOT CALL
	 * TODO
	 * 
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
				if(designType.equals(DatabaseConst.DESIGN_TYPE_AUDIO)){
					dbConnection.sendUpdate("DELETE from " + DatabaseConst.DESIGN_TABLE +", "+ DatabaseConst.AUDIO_TABLE + " USING " + DatabaseConst.DESIGN_TABLE + " INNER JOIN " + DatabaseConst.AUDIO_TABLE + " ON " + DatabaseConst.DESIGN_TABLE+"."+DatabaseConst.DESIGN_ID +"="+ DatabaseConst.AUDIO_TABLE+"."+DatabaseConst.AUDIO_ID + " WHERE " + DatabaseConst.AUDIO_TABLE+"."+DatabaseConst.AUDIO_ID+"="+designID);
				}
				else if(designType.equals(DatabaseConst.DESIGN_TYPE_VIDEO)){
					dbConnection.sendUpdate("DELETE from " + DatabaseConst.DESIGN_TABLE +", "+ DatabaseConst.VIDEO_TABLE + " USING " + DatabaseConst.DESIGN_TABLE + " INNER JOIN " + DatabaseConst.VIDEO_TABLE + " ON " + DatabaseConst.DESIGN_TABLE+"."+DatabaseConst.DESIGN_ID +"="+ DatabaseConst.VIDEO_TABLE+"."+DatabaseConst.VIDEO_ID + " WHERE " + DatabaseConst.VIDEO_TABLE+"."+DatabaseConst.VIDEO_ID+"="+designID);
				}
				else if(designType.equals(DatabaseConst.DESIGN_TYPE_SKETCH)){
					dbConnection.sendUpdate("DELETE from " + DatabaseConst.DESIGN_TABLE +", "+ DatabaseConst.SKETCH_TABLE + " USING " + DatabaseConst.DESIGN_TABLE + " INNER JOIN " + DatabaseConst.SKETCH_TABLE + " ON " + DatabaseConst.DESIGN_TABLE+"."+DatabaseConst.DESIGN_ID +"="+ DatabaseConst.SKETCH_TABLE+"."+DatabaseConst.SKETCH_ID + " WHERE " + DatabaseConst.SKETCH_TABLE+"."+DatabaseConst.SKETCH_ID+"="+designID);
				}
				else if(designType.equals(DatabaseConst.DESIGN_TYPE_MODEL)){
					dbConnection.sendUpdate("DELETE from " + DatabaseConst.DESIGN_TABLE +", "+ DatabaseConst.MODEL_TABLE + " USING " + DatabaseConst.DESIGN_TABLE + " INNER JOIN " + DatabaseConst.MODEL_TABLE + " ON " + DatabaseConst.DESIGN_TABLE+"."+DatabaseConst.DESIGN_ID +"="+ DatabaseConst.MODEL_TABLE+"."+DatabaseConst.MODEL_ID + " WHERE " + DatabaseConst.MODEL_TABLE+"."+DatabaseConst.MODEL_ID+"="+designID);
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
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_ID + " = " + id +";");
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
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_NAME + " LIKE '" + name +"';");
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
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_USER + " = '" + user +"';");
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
			ResultSet drs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.DESIGN_TABLE + " WHERE " + DatabaseConst.DESIGN_CITY + " = " + cityID +";");
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
			dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.PROPOSAL_TABLE+" (`"+DatabaseConst.PROPOSE_SOURCE+"`, `"+DatabaseConst.PROPOSE_DEST+"`) VALUES ("+source+","+destination+");");
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
			ResultSet rs = dbConnection.sendQuery("SELECT " + DatabaseConst.PROPOSE_DEST + " FROM " + DatabaseConst.PROPOSAL_TABLE + " WHERE " + DatabaseConst.PROPOSE_SOURCE + " = " + source + ";");
			if(rs.first()){
				return rs.getInt(DatabaseConst.PROPOSE_DEST);
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
			ResultSet rs = dbConnection.sendQuery("SELECT " + DatabaseConst.PROPOSE_SOURCE + " FROM " + DatabaseConst.PROPOSAL_TABLE + " WHERE " + DatabaseConst.PROPOSE_DEST + " = " + destination + ";");
			if(rs.first()){
				return rs.getInt(DatabaseConst.PROPOSE_SOURCE);
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
				dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.CITY_TABLE+" (`"+DatabaseConst.CITY_NAME+"`, `"+DatabaseConst.CITY_STATE+"`, `"+DatabaseConst.CITY_COUNTRY+"`) VALUES ('"+name+"','"+state+"','"+country+"');");
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
			cityResult = dbConnection.sendQuery("SELECT "+DatabaseConst.CITY_ID+" FROM "+DatabaseConst.CITY_TABLE+" WHERE "+DatabaseConst.CITY_NAME+" LIKE '"+cityName+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DatabaseConst.CITY_ID));
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
			cityResult = dbConnection.sendQuery("SELECT "+DatabaseConst.CITY_ID+" FROM "+DatabaseConst.CITY_TABLE+" WHERE "+DatabaseConst.CITY_STATE+" LIKE '"+state+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DatabaseConst.CITY_ID));
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
			cityResult = dbConnection.sendQuery("SELECT "+DatabaseConst.CITY_ID+" FROM "+DatabaseConst.CITY_TABLE+" WHERE "+DatabaseConst.CITY_COUNTRY+" LIKE '"+country+"'");
			while(cityResult.next() == true){
				results.add(cityResult.getInt(DatabaseConst.CITY_ID));
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
			cityResult = dbConnection.sendQuery("SELECT * FROM "+DatabaseConst.CITY_TABLE+" WHERE "+DatabaseConst.CITY_ID+" = '"+id+"'");
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
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.CITY_TABLE +" WHERE " + DatabaseConst.CITY_NAME + " = '"+name+"'"
					+ " AND " + DatabaseConst.CITY_STATE + " = '"+state+"'"
					+ " AND " + DatabaseConst.CITY_COUNTRY + " = '"+country+"';");
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
			dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.COORD_TABLE+" (`"+DatabaseConst.COORD_NORTHING+"`, `"+DatabaseConst.COORD_EASTING+"`, `"+DatabaseConst.COORD_LATZONE+"`, `"+DatabaseConst.COORD_LONZONE+"`, `"+DatabaseConst.COORD_ALTITUDE+"`) VALUES ("+northing+","+easting+",'"+latZone+"',"+lonZone+","+altitude+");");
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
			dbConnection.sendUpdate("UPDATE " + DatabaseConst.COORD_TABLE + " SET "
					+ DatabaseConst.COORD_EASTING + "="+easting+","
					+ DatabaseConst.COORD_NORTHING + "="+northing+","
					+ DatabaseConst.COORD_LATZONE + "='"+latZone+"',"
					+ DatabaseConst.COORD_LONZONE + "="+lonZone+","
					+ DatabaseConst.COORD_ALTITUDE + "="+altitude+
					" WHERE " + DatabaseConst.COORD_ID + " = " + coordinateID +";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public UTMCoordinate retrieveCoordinate(int coordinateID){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.COORD_TABLE + " WHERE " + DatabaseConst.COORD_ID + " = " + coordinateID + ";");
			if(rs.first()){
				return new UTMCoordinate(rs.getInt(DatabaseConst.COORD_EASTING), rs.getInt(DatabaseConst.COORD_NORTHING), rs.getInt(DatabaseConst.COORD_LONZONE), rs.getString(DatabaseConst.COORD_LATZONE).charAt(0), rs.getInt(DatabaseConst.COORD_ALTITUDE));
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
			dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.VOTE_TABLE+" (`"+DatabaseConst.VOTE_VALUE+"`, `"+DatabaseConst.VOTE_USER+"`, `"+DatabaseConst.VOTE_DESIGN+"`) VALUES ("+vote+",'"+user+"',"+designID+");");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void changeVote(int designID, String user, String pass){
		if(authenticateUser(user, pass)){
			try {
				ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.VOTE_TABLE + " WHERE " + DatabaseConst.VOTE_DESIGN +" = " + designID + " AND " + DatabaseConst.VOTE_USER + " = '"+user+"';");
				if(!rs.first()) return;
				int voteID = rs.getInt(DatabaseConst.VOTE_ID);
				int newVote;
				if(rs.getBoolean(DatabaseConst.VOTE_VALUE)){
					newVote = 0;
				}
				else newVote=1;
				dbConnection.sendUpdate("Update "+DatabaseConst.VOTE_TABLE+ " SET " + DatabaseConst.VOTE_VALUE + " = " + newVote + " WHERE " + DatabaseConst.VOTE_ID +" = " + voteID+";");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean addComment(int designID, String user, String pass, String comment){
		if(authenticateUser(user,pass)){
			try {
				dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.COMMENT_TABLE+" (`"+DatabaseConst.COMMENT_DESIGN+"`, `"+DatabaseConst.COMMENT_USER+"`, `"+DatabaseConst.COMMENT_TEXT+"`, `"+DatabaseConst.COMMENT_DATE+"`) VALUES ("+designID+",'"+user+"','"+comment+"',NOW());");
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		else return false;
	}

	// TODO
	public boolean deleteComment(int commentID, String user, String password){
		return false;
	}

	public void reportSpamComment(int commentID){
		try {
			dbConnection.sendUpdate("UPDATE " + DatabaseConst.COMMENT_TABLE + " SET " + DatabaseConst.COMMENT_SPAMFLAG + " = 1 WHERE " + DatabaseConst.COMMENT_ID + " = " + commentID + ";");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean addModerator(String user){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.MOD_TABLE+" (`"+DatabaseConst.MOD_NAME+"`, `"+DatabaseConst.MOD_LEVEL+"`) VALUES ('"+user+"',2);");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean addAdministrator(String user){
		try {
			dbConnection.sendUpdate("INSERT INTO "+DatabaseConst.MOD_TABLE+" (`"+DatabaseConst.MOD_NAME+"`, `"+DatabaseConst.MOD_LEVEL+"`) VALUES ('"+user+"',1);");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean checkModeratorStatus(String user){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MOD_TABLE + " WHERE " + DatabaseConst.MOD_NAME + " = '"+user+"' AND " +
					DatabaseConst.MOD_LEVEL + " = 2" + ";");
			if(rs.first()) return true;
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean checkAdminStatus(String user){
		try {
			ResultSet rs = dbConnection.sendQuery("SELECT * FROM " + DatabaseConst.MOD_TABLE + " WHERE " + DatabaseConst.MOD_NAME + " = '"+user+"' AND " +
					DatabaseConst.MOD_LEVEL + " = 1" + ";");
			if(rs.first()) return true;
			else return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
