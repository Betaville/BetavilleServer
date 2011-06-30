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
package edu.poly.bxmc.betaville.server.session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes or reads a session from a file.  XML is <em>not</em> used in order to improve speed.
 * @author Skye Book
 *
 */
public class SessionSerializer {

	public static void writeSession(Session session, File directory) throws IOException{
		if(directory.isDirectory()){
			PrintWriter writer = new PrintWriter(createSessionFile(session.getSessionToken(), directory));
			writer.write(session.sessionID+"\n");
			writer.write(session.sessionToken+"\n");
			writer.write(session.user+"\n");
			writer.flush();
			writer.close();
		} else 
			throw new IOException("A directory must be provided for the session to be written to");
	}

	public static Session readSession(String token, File directory) throws IOException{
		File file = createSessionFile(token, directory);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int id = Integer.parseInt(reader.readLine());
		String sessionToken = reader.readLine();
		String user = reader.readLine();
		reader.close();
		return new Session(user, id, sessionToken);
	}
	
	public static File createSessionFile(String token, File directory){
		return new File(directory+"/"+token);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
