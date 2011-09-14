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
package edu.poly.bxmc.betaville.server.session.availability;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.poly.bxmc.betaville.server.session.Session;
import edu.poly.bxmc.betaville.util.Crypto;

/**
 * @author Skye Book
 *
 */
public class InMemorySessionTracker extends SessionTracker{
	public static final Logger logger = Logger.getLogger(InMemorySessionTracker.class);
	public ArrayList<Session> sessions = new ArrayList<Session>();
	
	public InMemorySessionTracker(){}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#addSession(int, java.lang.String)
	 */
	public synchronized Session addSession(int sessionID, String user){
		String tokenCandidate = Crypto.createSessionToken();
		while(sessionTokenExists(tokenCandidate)){
			tokenCandidate = Crypto.createSessionToken();
		}
		Session session = new Session(user, sessionID, tokenCandidate);
		sessions.add(session);
		logger.info("New Session("+user+":"+session.getSessionID()+":"+session.getSessionToken()+")");
		return session;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#sessionTokenExists(java.lang.String)
	 */
	@Override
	public boolean sessionTokenExists(String tokenCandidate){
		for(Session session : sessions){
			if(session.getSessionToken()==tokenCandidate) return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#killSession(java.lang.String)
	 */
	public synchronized int killSession(String sessionToken){
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
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#getSession(java.lang.String)
	 */
	public Session getSession(String sessionToken){
		for(Session session : sessions){
			if(session.getSessionToken().equals(sessionToken)) return session;
		}
		return null;
	}
}
