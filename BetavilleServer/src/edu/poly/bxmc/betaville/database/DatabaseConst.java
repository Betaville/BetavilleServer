/**
 * 
 */
package edu.poly.bxmc.betaville.database;

/**
 * Names of each of the tables and columns in the database.
 * @author Skye Book
 *
 */
public class DatabaseConst {
	
	public final static String DESIGN_TABLE = "design";
	public final static String DESIGN_ID = "designID";
	public final static String DESIGN_NAME = "name";
	public final static String DESIGN_FILE = "filepath";
	public final static String DESIGN_CITY = "cityID";
	public final static String DESIGN_ADDRESS = "address";
	public final static String DESIGN_USER = "user";
	public final static String DESIGN_COORDINATE = "coordinateID";
	public final static String DESIGN_DATE = "date";
	public final static String DESIGN_PRIVACY = "publicViewing";
	public final static String DESIGN_DESCRIPTION = "description";
	public final static String DESIGN_URL = "designURL";
	public final static String DESIGN_TYPE = "designType";
	public final static String DESIGN_TYPE_AUDIO = "audio";
	public final static String DESIGN_TYPE_VIDEO = "video";
	public final static String DESIGN_TYPE_MODEL = "model";
	public final static String DESIGN_TYPE_SKETCH = "sketch";
	
	
	public final static String AUDIO_TABLE = "audiodesign";
	public final static String AUDIO_ID = "designid";
	public final static String AUDIO_LENGTH = "length";
	public final static String AUDIO_VOLUME = "volume";
	public final static String AUDIO_DIRECTIONX = "directionX";
	public final static String AUDIO_DIRECTIONY = "directionY";
	public final static String AUDIO_DIRECTIONZ = "directionZ";
	
	public final static String VIDEO_TABLE = "videodesign";
	public final static String VIDEO_ID = "designid";
	public final static String VIDEO_LENGTH = "length";
	public final static String VIDEO_VOLUME = "volume";
	public final static String VIDEO_FORMAT = "format";
	public final static String VIDEO_DIRECTIONX = "directionX";
	public final static String VIDEO_DIRECTIONY = "directionY";
	public final static String VIDEO_DIRECTIONZ = "directionZ";
	
	public final static String SKETCH_TABLE = "sketchdesign";
	public final static String SKETCH_ID = "designid";
	public final static String SKETCH_ROTATION = "rotY";
	public final static String SKETCH_LENGTH = "length";
	public final static String SKETCH_WIDTH = "width";
	public final static String SKETCH_UPPLANE = "upPlane";
	
	public final static String MODEL_TABLE = "modeldesign";
	public final static String MODEL_ID = "designid";
	public final static String MODEL_ROTATION = "rotY";
	public final static String MODEL_LENGTH = "length";
	public final static String MODEL_WIDTH = "width";
	public final static String MODEL_HEIGHT = "height";
	public final static String MODEL_TEX = "textured";
	
	
	public final static String CITY_TABLE = "city";
	public final static String CITY_ID = "cityID";
	public final static String CITY_NAME = "cityName";
	public final static String CITY_STATE = "state";
	public final static String CITY_COUNTRY = "country";
	
	public final static String USER_TABLE = "user";
	public final static String USER_NAME = "userName";
	public final static String USER_PASS = "userPass";
	public final static String USER_TWITTER = "twitterName";
	public final static String USER_EMAIL = "email";
	public final static String USER_ACTIVATED = "activated";
	public final static String USER_BIO = "bio";
	
	
	//public final static String BAN_TABLE = "ban";
	
	public final static String COORD_TABLE = "coordinate";
	public final static String COORD_ID = "coordinateID";
	public final static String COORD_NORTHING = "northing";
	public final static String COORD_EASTING = "easting";
	public final static String COORD_LATZONE = "latZone";
	public final static String COORD_LONZONE = "lonZone";
	public final static String COORD_ALTITUDE = "altitude";
	
	
	public final static String VOTE_TABLE = "vote";
	public final static String VOTE_ID = "voteID";
	public final static String VOTE_VALUE = "voteUp";
	public final static String VOTE_USER = "user";
	public final static String VOTE_DESIGN = "designid";
	
	public final static String COMMENT_TABLE = "comment";
	public final static String COMMENT_ID = "commentID";
	public final static String COMMENT_DESIGN = "designID";
	public final static String COMMENT_USER = "user";
	public final static String COMMENT_TEXT = "comment";
	public final static String COMMENT_DATE = "date";
	public final static String COMMENT_SPAMFLAG = "spamFlag";
	public final static String COMMENT_SPAMVERIFIED = "spamVerified";
	
	public final static String PROPOSAL_TABLE = "proposal";
	public final static String PROPOSAL_ID = "proposalID";
	public final static String PROPOSE_SOURCE = "sourceID";
	public final static String PROPOSE_DEST = "destinationID";
	
	
	public final static String MOD_TABLE = "moderators";
	public final static String MOD_NAME = "user";
	public final static String MOD_LEVEL = "level";
}
