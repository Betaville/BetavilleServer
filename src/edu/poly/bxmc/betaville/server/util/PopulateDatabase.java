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

import edu.poly.bxmc.betaville.server.database.NewDatabaseManager;

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
			System.out.println("| -city defaultcitytocreate        |");
			System.out.println("| -state statecityisin             |");
			System.out.println("| -country countrystateisin        |");
			System.out.println("+----------------------------------+");

		}

		String dbUser = null;
		String dbPass = null;
		String adminUser = null;
		String adminPass = null;
		String adminMail = null;
		String city = null;
		String state = null;
		String country = null;

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

			// database password
			if(args[i].startsWith("-adminuser")){
				adminUser=args[i+1];
				continue;
			}

			// database password
			if(args[i].startsWith("-adminpass")){
				adminPass=args[i].substring(10);
				continue;
			}

			// database password
			if(args[i].startsWith("-adminmail")){
				adminMail=args[i+1];
				continue;
			}

			// city
			if(args[i].startsWith("-city")){
				city=args[i+1];
				continue;
			}

			// state
			if(args[i].startsWith("-state")){
				state=args[i+1];
				continue;
			}

			// country
			if(args[i].startsWith("-country")){
				country=args[i+1];
				continue;
			}
		}

		System.out.println("dbuser: "+dbUser);
		System.out.println("dbpass: "+dbPass);
		NewDatabaseManager db = new NewDatabaseManager(dbUser, dbPass);
		if(db.addUser(adminUser, adminPass, adminMail, "", "")) System.out.println("User '"+adminUser+"' created");
		else System.err.println("User could not be created");

		// user is set to admin in the script

		if(db.addCity(city, state, country)>-1) System.out.println("City '"+city+"' created");
		else System.err.println("City could not be created");
	}

}
