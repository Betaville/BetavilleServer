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
package edu.poly.bxmc.betaville.server.xml;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import edu.poly.bxmc.betaville.model.IUser.UserType;
import edu.poly.bxmc.betaville.server.util.Preferences;
import edu.poly.bxmc.betaville.xml.PreferenceWriter;

/**
 * Lists all of Betaville's default preferences and sets them
 * up if they are not currently available.
 * @author Skye Book
 *
 */
public class DefaultPreferenceWriter {
	private static Logger logger = Logger.getLogger(DefaultPreferenceWriter.class);
	
	public static void writeDefaultPreferences() throws IOException{
		writeDefaultPreferences((new File("config.xml")));
	}
	

	/**
	 * @throws IOException 
	 * 
	 */
	public static void writeDefaultPreferences(File file) throws IOException {
		logger.info("Writing default preferences");
		if(System.getProperty(Preferences.LOG_REPORT_INTERVAL)==null) System.setProperty(Preferences.LOG_REPORT_INTERVAL, "600000");
		if(System.getProperty(Preferences.NETWORK_DEFAULT_PORT)==null) System.setProperty(Preferences.NETWORK_DEFAULT_PORT, "14500");
		if(System.getProperty(Preferences.NETWORK_SSL_PORT)==null) System.setProperty(Preferences.NETWORK_SSL_PORT, "14501");
		if(System.getProperty(Preferences.NETWORK_USE_SSL)==null) System.setProperty(Preferences.NETWORK_USE_SSL, "false");
		if(System.getProperty(Preferences.MYSQL_HOST)==null) System.setProperty(Preferences.MYSQL_HOST, "localhost");
		if(System.getProperty(Preferences.MYSQL_PORT)==null) System.setProperty(Preferences.MYSQL_PORT, "3306");
		if(System.getProperty(Preferences.MYSQL_USER)==null) System.setProperty(Preferences.MYSQL_USER, "root");
		if(System.getProperty(Preferences.MYSQL_PASS)==null) System.setProperty(Preferences.MYSQL_PASS, "root");
		if(System.getProperty(Preferences.MYSQL_DATABASE)==null) System.setProperty(Preferences.MYSQL_DATABASE, "betaville");
		if(System.getProperty(Preferences.MAIL_ENABLED)==null) System.setProperty(Preferences.MAIL_ENABLED, "false");
		if(System.getProperty(Preferences.MAIL_HOST)==null) System.setProperty(Preferences.MAIL_HOST, "smtpserver");
		if(System.getProperty(Preferences.MAIL_USER)==null) System.setProperty(Preferences.MAIL_USER, "smtpuser");
		if(System.getProperty(Preferences.MAIL_PASS)==null) System.setProperty(Preferences.MAIL_PASS, "smtppass");
		if(System.getProperty(Preferences.MAIL_PORT)==null) System.setProperty(Preferences.MAIL_PORT, "smtpport");
		if(System.getProperty(Preferences.MAIL_COMMENT_NOTIFICATION)==null) System.setProperty(Preferences.MAIL_COMMENT_NOTIFICATION, "MailAssets/comment_notification.html");
		if(System.getProperty(Preferences.STORAGE_MEDIA)==null) System.setProperty(Preferences.STORAGE_MEDIA, "storage/");
		if(System.getProperty(Preferences.STORAGE_SESSIONS)==null) System.setProperty(Preferences.STORAGE_SESSIONS, "sessions/");
		if(System.getProperty(Preferences.MINIMUM_BASE_CREATE)==null) System.setProperty(Preferences.MINIMUM_BASE_CREATE, UserType.BASE_COMMITTER.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_BASE_MODIFY)==null) System.setProperty(Preferences.MINIMUM_BASE_MODIFY, UserType.BASE_COMMITTER.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_BASE_DELETE)==null) System.setProperty(Preferences.MINIMUM_BASE_DELETE, UserType.BASE_COMMITTER.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_PROPOSAL_CREATE)==null) System.setProperty(Preferences.MINIMUM_PROPOSAL_CREATE, UserType.MEMBER.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_PROPOSAL_MODIFY)==null) System.setProperty(Preferences.MINIMUM_PROPOSAL_MODIFY, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_PROPOSAL_DELETE)==null) System.setProperty(Preferences.MINIMUM_PROPOSAL_DELETE, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_WORMHOLE_CREATE)==null) System.setProperty(Preferences.MINIMUM_WORMHOLE_CREATE, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_WORMHOLE_MODIFY)==null) System.setProperty(Preferences.MINIMUM_WORMHOLE_MODIFY, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.MINIMUM_WORMHOLE_DELETE)==null) System.setProperty(Preferences.MINIMUM_WORMHOLE_DELETE, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.USER_ELEVATE_ADMIN)==null) System.setProperty(Preferences.USER_ELEVATE_ADMIN, UserType.ADMIN.name().toUpperCase());
		if(System.getProperty(Preferences.USER_ELEVATE_MODERATOR)==null) System.setProperty(Preferences.USER_ELEVATE_MODERATOR, UserType.ADMIN.name().toUpperCase());
		if(System.getProperty(Preferences.USER_ELEVATE_BASE_COMMITTER)==null) System.setProperty(Preferences.USER_ELEVATE_BASE_COMMITTER, UserType.MODERATOR.name().toUpperCase());
		if(System.getProperty(Preferences.USER_ELEVATE_DATA_SEARCHER)==null) System.setProperty(Preferences.USER_ELEVATE_DATA_SEARCHER, UserType.MODERATOR.name().toUpperCase());
		
		PreferenceWriter pr = new PreferenceWriter(new File("config.xml"));
		pr.writeData();
	}
	
	public static void main(String[] args) throws IOException{
		// Generates preferences
		File file = new File("config.xml");
		DefaultPreferenceWriter.writeDefaultPreferences(file);
		System.out.println("Default preferences written to " + file.getAbsolutePath());
	}

}