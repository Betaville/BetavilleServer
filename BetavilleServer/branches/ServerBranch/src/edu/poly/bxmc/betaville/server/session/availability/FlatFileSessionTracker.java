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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.poly.bxmc.betaville.server.session.Session;
import edu.poly.bxmc.betaville.server.session.SessionSerializer;
import edu.poly.bxmc.betaville.server.util.Preferences;
import edu.poly.bxmc.betaville.util.Crypto;

/**
 * @author Skye Book
 *
 */
public class FlatFileSessionTracker extends SessionTracker{
	private static final Logger logger = Logger.getLogger(FlatFileSessionTracker.class);

	private File sessionDirectory = new File(Preferences.getSetting(Preferences.STORAGE_SESSIONS));

	/**
	 * 
	 */
	public FlatFileSessionTracker() {
		// if the directory doesn't exist, create a new one
		if(!sessionDirectory.exists()) sessionDirectory.mkdirs();
		else{
			// if the directory does exist, delete any session files
			File[] files = sessionDirectory.listFiles();
			if(files!=null){
				for(File sessionFile : files){
					sessionFile.delete();
				}
			}
		}
		
		String tokenCandidate = Crypto.createSessionToken();
		while(sessionTokenExists(tokenCandidate)){
			tokenCandidate = Crypto.createSessionToken();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#addSession(int, java.lang.String)
	 */
	@Override
	public synchronized Session addSession(int sessionID, String user) {
		String tokenCandidate = Crypto.createSessionToken();
		while(sessionTokenExists(tokenCandidate)){
			tokenCandidate = Crypto.createSessionToken();
		}
		Session session = new Session(user, sessionID, tokenCandidate);
		try {
			SessionSerializer.writeSession(session, sessionDirectory);
		} catch (IOException e) {
			logger.fatal("Session file could not be written, ensure that "
					+ sessionDirectory.toString() + " is writable");
		}
		logger.info("New Session("+user+":"+session.getSessionID()+":"+session.getSessionToken()+")");
		return session;
	}

	private boolean sessionTokenExists(String tokenCandidate){
		return SessionSerializer.createSessionFile(tokenCandidate, sessionDirectory).exists();
	}

	/* (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#getSession(java.lang.String)
	 */
	@Override
	public Session getSession(String sessionToken) {
		try {
			return SessionSerializer.readSession(sessionToken, sessionDirectory);
		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				logger.info("Requested session not found, this could be an attack");
			}
			else{
				logger.fatal("Session file could not be read, ensure that "
						+ sessionDirectory.toString() + " is readable");
			}

			return null;
		}
	}

	/* (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.session.availability.SessionProvider#killSession(java.lang.String)
	 */
	@Override
	public int killSession(String sessionToken) {
		try {
			Session session = SessionSerializer.readSession(sessionToken, sessionDirectory);
			if(SessionSerializer.createSessionFile(sessionToken, sessionDirectory).delete()){
				if(SessionSerializer.createSessionFile(sessionToken, sessionDirectory).exists()){
					logger.info("Session file exists!");
				}
				logger.info("Session file deleted");
			}
			else logger.error("Session file was not deleted");
			return session.getSessionID();
		} catch (IOException e) {
			if(e instanceof FileNotFoundException){
				logger.info("Requested session not found, this could be an attack");
			}
			else{
				logger.fatal("Session file could not be read, ensure that "
						+ sessionDirectory.toString() + " is readable");
			}

		}
		return -2;
	}

}
