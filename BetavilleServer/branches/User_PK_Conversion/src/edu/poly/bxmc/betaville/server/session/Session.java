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
package edu.poly.bxmc.betaville.server.session;

/**
 * Simple data storage class for holding information about a session,
 * where some is volatile and some is not.<br><br>
 * <strong>WARNING:</strong> password storage in this class is
 * <em>temporary</em> and will disappear in the very near future. - 17 November 2010
 * @author Skye Book
 */
public class Session {
	String user;
	// TODO: PASSWORD IS TEMPORARY
	String pass;
	int sessionID;
	String sessionToken;

	/**
	 * This is a temporary constructor to load in passwords
	 */
	public Session(String user, int sessionID, String sessionToken, String pass){
		this(user, sessionID, sessionToken);
		this.pass = pass;
	}
	
	/**
	 * 
	 */
	public Session(String user, int sessionID, String sessionToken){
		this.user = user;
		this.sessionID = sessionID;
		this.sessionToken=sessionToken;
	}
	
	public void setSessionToken(String sessionToken){
		this.sessionToken = sessionToken;
	}
	
	public String getSessionToken(){
		return sessionToken;
	}
	
	public String getUser(){
		return user;
	}
	
	public String getPassword(){
		return pass;
	}
	
	public int getSessionID(){
		return sessionID;
	}
}
