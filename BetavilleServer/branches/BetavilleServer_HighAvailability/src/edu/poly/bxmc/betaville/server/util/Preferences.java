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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import edu.poly.bxmc.betaville.server.xml.DefaultPreferenceWriter;
import edu.poly.bxmc.betaville.xml.PreferenceReader;

/**
 * @author Skye Book
 *
 */
public class Preferences{
	private static final Logger logger = Logger.getLogger(Preferences.class);
	
	private static boolean preferencesLoadedOnInitialization=false;
	
	protected static final String SERVER_BASE = "betaville.server.";
	
	public static final String NETWORK_DEFAULT_PORT=SERVER_BASE+"network.default.port";
	public static final String NETWORK_SSL_PORT=SERVER_BASE+"network.ssl.port";
	public static final String NETWORK_USE_SSL=SERVER_BASE+"network.ssl.enabled";
	
	
	public static final String LOG_REPORT_INTERVAL=SERVER_BASE+"logging.report.interval";
	public static final String MYSQL_USER=SERVER_BASE+"mysql.user";
	public static final String MYSQL_PASS=SERVER_BASE+"mysql.pass";
	public static final String MYSQL_PORT=SERVER_BASE+"mysql.port";
	public static final String MYSQL_HOST=SERVER_BASE+"mysql.host";
	public static final String MYSQL_DATABASE=SERVER_BASE+"mysql.database";
	
	public static final String MAIL_ENABLED=SERVER_BASE+"mailer.enabled";
	public static final String MAIL_HOST=SERVER_BASE+"mailer.host";
	public static final String MAIL_USER=SERVER_BASE+"mailer.user";
	public static final String MAIL_PASS=SERVER_BASE+"mailer.pass";
	public static final String MAIL_PORT=SERVER_BASE+"mailer.port";
	public static final String MAIL_COMMENT_NOTIFICATION=SERVER_BASE+"mailer.message.commentnotification";
	
	public static final String MINIMUM_BASE_CREATE=SERVER_BASE+"permissions.base.create";
	public static final String MINIMUM_BASE_MODIFY=SERVER_BASE+"permissions.base.modify";
	public static final String MINIMUM_BASE_DELETE=SERVER_BASE+"permissions.base.delete";
	
	public static final String MINIMUM_PROPOSAL_CREATE=SERVER_BASE+"permissions.proposal.create";
	public static final String MINIMUM_PROPOSAL_MODIFY=SERVER_BASE+"permissions.proposal.modify";
	public static final String MINIMUM_PROPOSAL_DELETE=SERVER_BASE+"permissions.proposal.delete";
	
	public static final String MINIMUM_WORMHOLE_CREATE=SERVER_BASE+"permissions.wormhole.create";
	public static final String MINIMUM_WORMHOLE_MODIFY=SERVER_BASE+"permissions.wormhole.modify";
	public static final String MINIMUM_WORMHOLE_DELETE=SERVER_BASE+"permissions.wormhole.delete";
	
	public static final String USER_ELEVATE_ADMIN=SERVER_BASE+"permissions.elevate.admin";
	public static final String USER_ELEVATE_MODERATOR=SERVER_BASE+"permissions.elevate.moderator";
	public static final String USER_ELEVATE_BASE_COMMITTER=SERVER_BASE+"permissions.elevate.base_committer";
	public static final String USER_ELEVATE_DATA_SEARCHER=SERVER_BASE+"permissions.elevate.data_searcher";
	
	public static String getSetting(String settingToGet){
		return System.getProperty(settingToGet);
	}
	
	public static boolean getBooleanSetting(String settingToGet){
		return Boolean.parseBoolean(System.getProperty(settingToGet));
	}
	
	/**
	 * Check for whether the application preferences were generated on startup
	 * or loaded from a file.
	 * @return True if they were loaded from a file, false if they were generated.
	 */
	public static boolean arePreferencesLoadedFromFile(){
		return preferencesLoadedOnInitialization;
	}
	
	public static void initialize() throws IOException{
		PreferenceReader pr;
		try {
			logger.info("Reading preference file");
			pr = new PreferenceReader(new File("config.xml"));
			pr.parse();
			preferencesLoadedOnInitialization=true;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("No preferences file could be found, creating one.");
			DefaultPreferenceWriter.writeDefaultPreferences();
		}
	}
}