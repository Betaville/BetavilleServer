/**
 * 
 */
package edu.poly.bxmc.betaville.server.session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
			PrintWriter writer = new PrintWriter(directory+"/"+session.getSessionToken());
			writer.write(session.sessionID+"\n");
			writer.write(session.sessionToken+"\n");
			writer.write(session.user+"\n");
			writer.flush();
			writer.close();
		} else 
			throw new IOException("A directory must be provided for the session to be written to");
	}

	public static Session readSession(String token, File directory) throws IOException{
		File file = new File(directory+"/"+token);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int id = Integer.parseInt(reader.readLine());
		String sessionToken = reader.readLine();
		String user = reader.readLine();
		return new Session(user, id, sessionToken);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
