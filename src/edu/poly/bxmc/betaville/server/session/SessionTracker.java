/**
 * 
 */
package edu.poly.bxmc.betaville.server.session;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.poly.bxmc.betaville.util.Crypto;

/**
 * @author Skye Book
 *
 */
public class SessionTracker {
	private static final Logger logger = Logger.getLogger(SessionTracker.class);
	private static ArrayList<Session> sessions = new ArrayList<Session>();
	
	private SessionTracker(){}
	
	private static boolean sessionTokenExists(String tokenCandidate){
		for(Session session : sessions){
			if(session.getSessionToken()==tokenCandidate) return true;
		}
		return false;
	}
	
	/**
	 * Creates a session and adds it to the session tracker.
	 * @param sessionID The sessionID
	 * @param user Username
	 * @param pass This user's password - <em>TEMPORARY</em>
	 * @return The created session
	 */
	public static synchronized Session addSession(int sessionID, String user, String pass){
		String tokenCandidate = Crypto.createSessionToken();
		while(sessionTokenExists(tokenCandidate)){
			tokenCandidate = Crypto.createSessionToken();
		}
		Session session = new Session(user, sessionID, tokenCandidate, pass);
		sessions.add(session);
		logger.info("New Session("+user+":"+session.getSessionID()+":"+session.getSessionToken()+")");
		return session;
	}
	
	/**
	 * Removes a session from the tracker, thus disallowing any more actions with it.
	 * @param sessionToken The token of the session to destroy
	 * @return The sessionID or -2 if the session could not be found
	 */
	public static synchronized int killSession(String sessionToken){
		logger.info("Ending session with token: " + sessionToken);
		logger.info("There are " + sessions.size() + " sessions");
		for(Session session : sessions){
			logger.info("Examining " + session.getSessionToken());
			if(session.getSessionToken().equals(sessionToken)){
				int sessionID = session.getSessionID();
				sessions.remove(session);
				return sessionID;
			}
		}
		logger.warn("Session for the following token could not be found: " + sessionToken);
		return -2;
	}
	
	public static Session getSession(String sessionToken){
		for(Session session : sessions){
			if(session.getSessionToken().equals(sessionToken)) return session;
		}
		return null;
	}
}
