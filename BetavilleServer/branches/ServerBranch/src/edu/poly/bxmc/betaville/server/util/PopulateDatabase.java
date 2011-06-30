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

import java.io.IOException;

import edu.poly.bxmc.betaville.server.database.NewDatabaseManager;
import edu.poly.bxmc.betaville.server.xml.DefaultPreferenceWriter;

/**
 * @author Skye Book
 *
 */
public class PopulateDatabase {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("+----------------------------------+");
		System.out.println("|Betaville Database Population Tool|");
		System.out.println("+----------------------------------+");

		if(args.length==0){
			System.out.println("|           Usage:                 |");
			System.out.println("|           FLAGS                  |");
			System.out.println("| -u databaseuser                  |");
			System.out.println("| -pdatabasepass                   |");
			System.out.println("| -adminuser newbvadmin            |");
			System.out.println("| -adminpassbvadminpass            |");
			System.out.println("| -adminmail bvadminemail          |");
			System.out.println("| -city newcitytocreate [quoted]   |");
			System.out.println("| -state statecityisin [quoted]    |");
			System.out.println("| -country countrycityisin [quoted]|");
			System.out.println("+----------------------------------+");

		}

		String dbUser = null;
		String dbPass = null;
		String adminUser = null;
		String adminPass = null;
		String adminMail = null;
		String city = "";
		String state = "";
		String country = "";

		for(int i=0; i<args.length; i++){

			// database user
			if(args[i].startsWith("-u")){
				dbUser=args[i+1];
				continue;
			}

			// database password
			if(args[i].startsWith("-p")){
				dbPass=args[i].substring(2);
				continue;
			}

			// Admin user to be created
			if(args[i].startsWith("-adminuser")){
				adminUser=args[i+1];
				continue;
			}

			// Password for admin user being created
			if(args[i].startsWith("-adminpass")){
				adminPass=args[i].substring(10);
				continue;
			}

			// Email address for the new administrator account
			if(args[i].startsWith("-adminmail")){
				adminMail=args[i+1];
				continue;
			}

			
			// city, state, and country should all be wrapped in quotes
			
			// city
			if(args[i].startsWith("-city")){
				for(int j=i+1; j<args.length; j++){
					if(args[j].endsWith("\"")){
						city+=(" "+args[j].substring(0, args[j].length()-1));
						if(city.startsWith(" \"")) city = city.substring(2);
						else if(city.startsWith("\"")) city = city.substring(1);
						break;
					}
					else{
						if(args[j].startsWith("\"")) city+=(args[j].substring(1));
						else city+=(" "+args[j]);
					}
				}
				continue;
			}

			// state
			if(args[i].startsWith("-state")){
				for(int j=i+1; j<args.length; j++){
					if(args[j].endsWith("\"")){
						state+=(" "+args[j].substring(0, args[j].length()-1));
						if(state.startsWith(" \"")) state = state.substring(2);
						else if(state.startsWith("\"")) state = state.substring(1);
						break;
					}
					else{
						if(args[j].startsWith("\"")) state+=(args[j].substring(1));
						else state+=(" "+args[j]);
					}
				}
				continue;
			}

			// country
			if(args[i].startsWith("-country")){
				for(int j=i+1; j<args.length; j++){
					if(args[j].endsWith("\"")){
						country+=(" "+args[j].substring(0, args[j].length()-1));
						if(country.startsWith(" \"")) country = country.substring(2);
						else if(country.startsWith("\"")) country = country.substring(1);
						break;
					}
					else{
						if(args[j].startsWith("\"")) country+=(args[j].substring(1));
						else country+=(" "+args[j]);
					}
				}
				continue;
			}
		}
		
		NewDatabaseManager db = new NewDatabaseManager(dbUser, dbPass, "betaville", "localhost", 3306);
		/*
		 * It's OK to bypass the username requirements here since this is being done locally (by someone who
		 * should know better.. and may, in fact, want to create a username like "admin")
		 */
		if(db.addUser(adminUser, adminPass, adminMail, "", "", true)) System.out.println("User '"+adminUser+"' created");
		else System.err.println("User could not be created");

		// user is set to admin in the script

		if(db.addCity(city, state, country)>-1) System.out.println("City '"+city+"' created");
		else System.err.println("City could not be created");
		
		
		// Now that we've created a server, let's setup some preferences:
		System.setProperty(Preferences.MYSQL_DATABASE, "betaville");
		System.setProperty(Preferences.MYSQL_USER, dbUser);
		System.setProperty(Preferences.MYSQL_PASS, dbPass);
		// The default port is 3306, so no need to set that (it will be written when we write the preferences)
		
		// now commit these preferences to a file
		try {
			DefaultPreferenceWriter.writeDefaultPreferences();
		} catch (IOException e) {
			System.out.println("ERROR: The preferences file could not be written.  Are you sure you have permission to " +
					"write to this directory?");
		}
	}

}
