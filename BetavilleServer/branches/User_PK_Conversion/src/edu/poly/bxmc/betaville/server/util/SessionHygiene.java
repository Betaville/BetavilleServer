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
package edu.poly.bxmc.betaville.server.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import edu.poly.bxmc.betaville.server.database.DBConst;
import edu.poly.bxmc.betaville.server.database.DataBaseConnection;



/**
 * Runs a cleanup for unfinished sessions by filling
 * in a best guess at their termination date.
 * @author Skye Book
 *
 */
public class SessionHygiene {

	private static DataBaseConnection dbc;

	private static PreparedStatement retrieveUsers;
	private static PreparedStatement retrieveUserSessions;
	private static PreparedStatement findNextSessionStart;
	private static PreparedStatement retrieveComments;
	private static PreparedStatement retrieveDesigns;
	private static PreparedStatement setSessionEndTime;

	/**
	 * @param args
	 */
	public static void main(String[] args){
		if(args.length!=2) dbc = new DataBaseConnection("root", "root");
		else dbc = new DataBaseConnection(args[0], args[1]);

		try {
			retrieveUsers = dbc.getConnection().prepareStatement("SELECT " + DBConst.USER_NAME + " FROM " + DBConst.USER_TABLE + " ORDER BY " + DBConst.USER_NAME);
			retrieveUserSessions = dbc.getConnection().prepareStatement("SELECT * FROM " + DBConst.SESSION_TABLE + " WHERE " + DBConst.SESSION_USER + "= ? ORDER BY " + DBConst.SESSION_START);
			findNextSessionStart = dbc.getConnection().prepareStatement("SELECT "+DBConst.SESSION_START+" FROM " + DBConst.SESSION_TABLE + " WHERE " + DBConst.SESSION_START + " > ? " + " LIMIT 1 ");
			retrieveComments = dbc.getConnection().prepareStatement("SELECT " + DBConst.COMMENT_DATE + " FROM " + DBConst.COMMENT_TABLE + " WHERE " + DBConst.COMMENT_DATE + " > ? AND " + DBConst.COMMENT_DATE + " < ? AND " + DBConst.COMMENT_USER + " = ? ORDER BY " + DBConst.COMMENT_DATE);
			retrieveDesigns = dbc.getConnection().prepareStatement("SELECT " + DBConst.DESIGN_DATE + " FROM " + DBConst.DESIGN_TABLE + " WHERE " + DBConst.DESIGN_DATE + " > ? AND " + DBConst.DESIGN_DATE + " < ? AND " + DBConst.DESIGN_USER + " = ? ORDER BY " + DBConst.DESIGN_DATE);
			setSessionEndTime = dbc.getConnection().prepareStatement("UPDATE " + DBConst.SESSION_TABLE + " SET " + DBConst.SESSION_END + " = ? WHERE " + DBConst.SESSION_ID + " = ?");

			int updateCounter=0;
			long start = System.currentTimeMillis();

			ResultSet users = retrieveUsers.executeQuery();
			while(users.next()){
				String user = users.getString(DBConst.USER_NAME);
				retrieveUserSessions.setString(1, user);
				ResultSet sessions = retrieveUserSessions.executeQuery();
				while(sessions.next()){
					if(sessions.getDate(DBConst.SESSION_END)==null){
						//System.out.println("null session found from " + user);
						Timestamp sessionStart = sessions.getTimestamp(DBConst.SESSION_START);
						findNextSessionStart.setTimestamp(1, sessionStart);
						ResultSet nextSession = findNextSessionStart.executeQuery();
						// if another sessions has been started since the previous one we can do the calculation
						if(nextSession.first()){
							Timestamp nextSessionStart = nextSession.getTimestamp(DBConst.SESSION_START);
							//System.out.println("bad session started " + sessionStart.getTime() + ", next session starts at " + nextSessionStart.getTime());
							retrieveComments.setTimestamp(1, sessionStart);
							retrieveComments.setTimestamp(2, nextSessionStart);
							retrieveComments.setString(3, user);
							retrieveDesigns.setTimestamp(1, sessionStart);
							retrieveDesigns.setTimestamp(2, nextSessionStart);
							retrieveDesigns.setString(3, user);
							ResultSet validComments = retrieveComments.executeQuery();
							//System.out.println(retrieveComments.toString());
							ResultSet validDesigns = retrieveDesigns.executeQuery();
							//System.out.println("queries executed");
							Timestamp latestDate = null;
							if(validComments.last()){
								latestDate = validComments.getTimestamp(DBConst.COMMENT_DATE);
								System.out.println("last comment at " + latestDate.toString());
							}
							if(validDesigns.last()){
								Timestamp designsDate =  validDesigns.getTimestamp(DBConst.DESIGN_DATE);
								System.out.println("last design at " + designsDate.toString());
								if(latestDate==null) latestDate = designsDate;
								else if(latestDate.before(designsDate)){
									latestDate = designsDate;
								}
							}
							
							// begin the procedure to update if we've found a useful date
							if(latestDate!=null){
								// increment the time by one second, we will use this as the session's end time
								int sessionID = sessions.getInt(DBConst.SESSION_ID);
								latestDate.setTime(latestDate.getTime()+1000);
								setSessionEndTime.setTimestamp(1, latestDate);
								setSessionEndTime.setInt(2, sessionID);
								setSessionEndTime.executeUpdate();
								updateCounter++;
								System.out.println("Session " + sessionID + " end time set to " + latestDate.toString());
							}
						}
					}
				}
			}
			
			System.out.println("Maintenance was performed on " + updateCounter + " records and took " + (System.currentTimeMillis()-start) + "ms");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
