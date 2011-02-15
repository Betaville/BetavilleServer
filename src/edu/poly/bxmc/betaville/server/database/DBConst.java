/** Copyright (c) 2008-2010, Brooklyn eXperimental Media Center
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

/**
 * Names of each of the tables and columns in the database.
 * @author Skye Book
 *
 */
public class DBConst {
	
	public final static String DESIGN_TABLE = "design";
	public final static String DESIGN_ID = "designID";
	public final static String DESIGN_NAME = "name";
	public final static String DESIGN_FILE = "filepath";
	public final static String DESIGN_CITY = "cityID";
	public final static String DESIGN_ADDRESS = "address";
	public final static String DESIGN_USER = "user";
	public final static String DESIGN_COORDINATE = "coordinateID";
	public final static String DESIGN_DATE = "date";
	public final static String DESIGN_LAST_MODIFIED = "lastModified";
	public final static String DESIGN_PRIVACY = "publicViewing";
	public final static String DESIGN_DESCRIPTION = "description";
	public final static String DESIGN_URL = "designURL";
	public final static String DESIGN_TYPE = "designType";
	public final static String DESIGN_TYPE_AUDIO = "audio";
	public final static String DESIGN_TYPE_VIDEO = "video";
	public final static String DESIGN_TYPE_MODEL = "model";
	public final static String DESIGN_TYPE_SKETCH = "sketch";
	public final static String DESIGN_TYPE_EMPTY = "empty";
	public final static String DESIGN_IS_ALIVE = "isAlive";
	public final static String DESIGN_FAVE_LIST= "favelist";
	
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
	public final static String MODEL_ROTATION_X = "rotX";
	public final static String MODEL_ROTATION_Y = "rotY";
	public final static String MODEL_ROTATION_Z = "rotZ";
	public final static String MODEL_LENGTH = "length";
	public final static String MODEL_WIDTH = "width";
	public final static String MODEL_HEIGHT = "height";
	public final static String MODEL_TEX = "textured";
	
	public final static String EMPTY_DESIGN_TABLE = "emptydesign";
	public final static String EMPTY_DESIGN_ID = "designid";
	public final static String EMPTY_DESIGN_LENGTH = "length";
	public final static String EMPTY_DESIGN_WIDTH = "width";
	
	public final static String CITY_TABLE = "city";
	public final static String CITY_ID = "cityID";
	public final static String CITY_NAME = "cityName";
	public final static String CITY_STATE = "state";
	public final static String CITY_COUNTRY = "country";
	
	public final static String USER_TABLE = "user";
	public final static String USER_NAME = "userName";
	public final static String USER_DISPLAY_NAME = "displayName";
	public final static String USER_PASS = "userPass";
	public final static String USER_STRONG_PASS = "strongpass";
	public final static String USER_STRONG_SALT = "strongsalt";
	public final static String USER_TWITTER = "twitterName";
	public final static String USER_EMAIL = "email";
	public final static String USER_EMAIL_VISIBLE = "showEmail";
	public final static String USER_ACTIVATED = "activated";
	public final static String USER_BIO = "bio";
	public final static String USER_WEBSITE = "website";
	public final static String USER_TYPE = "type";
	public final static String USER_TYPE_MEMBER = "member";
	public final static String USER_TYPE_BASE_COMMITTER = "base_committer";
	public final static String USER_TYPE_DATA_SEARCHER = "data_searcher";
	public final static String USER_TYPE_MODERATOR = "moderator";
	public final static String USER_TYPE_ADMIN = "admin";
	
	public final static String SESSION_TABLE = "session";
	public final static String SESSION_ID = "sessionID";
	public final static String SESSION_USER = "userName";
	public final static String SESSION_START = "timeEntered";
	public final static String SESSION_END = "timeLeft";
	
	public final static String COORD_TABLE = "coordinate";
	public final static String COORD_ID = "coordinateID";
	public final static String COORD_NORTHING = "northing";
	public final static String COORD_EASTING = "easting";
	public final static String COORD_LATZONE = "latZone";
	public final static String COORD_LONZONE = "lonZone";
	public final static String COORD_ALTITUDE = "altitude";
	
	public final static String COMMENT_TABLE = "comment";
	public final static String COMMENT_ID = "commentID";
	public final static String COMMENT_DESIGN = "designID";
	public final static String COMMENT_USER = "user";
	public final static String COMMENT_TEXT = "comment";
	public final static String COMMENT_DATE = "date";
	public final static String COMMENT_SPAMFLAG = "spamFlag";
	public final static String COMMENT_SPAMVERIFIED = "spamVerified";
	public final static String COMMENT_REPLIESTO = "repliesTo";
	
	public final static String PROPOSAL_TABLE = "proposal";
	public final static String PROPOSAL_ID = "proposalID";
	public final static String PROPOSAL_SOURCE = "sourceID";
	public final static String PROPOSAL_DEST = "destinationID";
	public final static String PROPOSAL_TYPE = "type";
	public final static String PROPOSAL_TYPE_PROPOSAL = "proposal";
	public final static String PROPOSAL_TYPE_VERSION = "version";
	public final static String PROPOSAL_TYPE_REMOVABLE_LIST = "removables";
	public final static String PROPOSAL_PERMISSIONS_LEVEL = "level";
	public final static String PROPOSAL_PERMISSIONS_LEVEL_CLOSED = "closed";
	public final static String PROPOSAL_PERMISSIONS_LEVEL_GROUP = "group";
	public final static String PROPOSAL_PERMISSIONS_LEVEL_ALL = "all";
	public final static String PROPOSAL_PERMISSIONS_GROUP_ARRAY = "group";
	
	public final static String WORMHOLE_TABLE = "wormhole";
	public final static String WORMHOLE_ID = "wormholeid";
	public final static String WORMHOLE_COORDINATE = "coordinateid";
	public final static String WORMHOLE_CITY = "cityid";
	public final static String WORMHOLE_NAME = "name";
	public final static String WORMHOLE_IS_ALIVE = "isAlive";
}