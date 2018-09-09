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
package edu.poly.bxmc.betaville.server.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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

	public static final String HTTP_STORAGE_ENABLED=SERVER_BASE+"http.storage.enabled";
	public static final String HTTP_STORAGE_LOCATION=SERVER_BASE+"http.storage.location";

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
	public static final String MAIL_STARTTLS=SERVER_BASE+"mailer.starttls";
	public static final String MAIL_REQUIRES_AUTH=SERVER_BASE+"mailer.requiresauth";
	public static final String MAIL_COMMENT_NOTIFICATION=SERVER_BASE+"mailer.message.commentnotification";

	public static final String STORAGE_MEDIA=SERVER_BASE+"storage.media";
	public static final String STORAGE_SESSIONS=SERVER_BASE+"storage.sessions";
	public static final String STORAGE_LOGGING=SERVER_BASE+"storage.logging";

	public static final String SESSION_TRACKER=SERVER_BASE+"session.tracker";

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

	public static int getIntegerSetting(String settingToGet){
		return Integer.parseInt(System.getProperty(settingToGet));
	}

	public static void setBooleanSetting(String preferenceToSet, boolean setting){
		System.setProperty(preferenceToSet, Boolean.toString(setting));
	}

	public static void setIntegerSetting(String preferenceToSet, int setting){
		System.setProperty(preferenceToSet, Integer.toString(setting));
	}

	public static void setSetting(String preferenceToSet, String setting){
		System.setProperty(preferenceToSet, setting);
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
		try {
			System.out.println("Reading preference file");
			PreferenceReader pr = new PreferenceReader(new File("config.xml"));
			pr.parse();
			preferencesLoadedOnInitialization=true;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("No preferences file could be found, creating one.");
			DefaultPreferenceWriter.writeDefaultPreferences();
		}
	}

	public static void overlayPreferencesFromEnvironment() {
		String[] allPreferences = {
				Preferences.HTTP_STORAGE_ENABLED,
				Preferences.HTTP_STORAGE_LOCATION,
				Preferences.NETWORK_DEFAULT_PORT,
				Preferences.NETWORK_SSL_PORT,
				Preferences.NETWORK_USE_SSL,
				Preferences.LOG_REPORT_INTERVAL,
				Preferences.MYSQL_USER,
				Preferences.MYSQL_PASS,
				Preferences.MYSQL_PORT,
				Preferences.MYSQL_HOST,
				Preferences.MYSQL_DATABASE,
				Preferences.MAIL_ENABLED,
				Preferences.MAIL_HOST,
				Preferences.MAIL_USER,
				Preferences.MAIL_PASS,
				Preferences.MAIL_PORT,
				Preferences.MAIL_STARTTLS,
				Preferences.MAIL_REQUIRES_AUTH,
				Preferences.MAIL_COMMENT_NOTIFICATION,
				Preferences.STORAGE_MEDIA,
				Preferences.STORAGE_SESSIONS,
				Preferences.STORAGE_LOGGING,
				Preferences.SESSION_TRACKER,
				Preferences.MINIMUM_BASE_CREATE,
				Preferences.MINIMUM_BASE_MODIFY,
				Preferences.MINIMUM_BASE_DELETE,
				Preferences.MINIMUM_PROPOSAL_CREATE,
				Preferences.MINIMUM_PROPOSAL_MODIFY,
				Preferences.MINIMUM_PROPOSAL_DELETE,
				Preferences.MINIMUM_WORMHOLE_CREATE,
				Preferences.MINIMUM_WORMHOLE_MODIFY,
				Preferences.MINIMUM_WORMHOLE_DELETE,
				Preferences.USER_ELEVATE_ADMIN,
				Preferences.USER_ELEVATE_MODERATOR,
				Preferences.USER_ELEVATE_BASE_COMMITTER,
				Preferences.USER_ELEVATE_DATA_SEARCHER
		};
		
		for (String propertyName : allPreferences) {
			String value = System.getenv(propertyName);
			if (value != null) {
				System.setProperty(propertyName, value);
			}
		}
	}
}