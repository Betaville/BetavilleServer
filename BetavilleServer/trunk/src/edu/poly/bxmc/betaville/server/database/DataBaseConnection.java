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
package edu.poly.bxmc.betaville.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Class <CreateConnection> - Manages the connection to the database
 * 
 * @author Skye Book
 * @author Caroline Bouchat
 * @version 0.1 - Spring 2009
 */
public class DataBaseConnection {
	/**
	 * Attribute <con> - Connection to the database
	 */
	private Connection con = null;
	private Statement statement = null;
	
	
	/**
	 * Constructor - Creates (opens) the SQL connection
	 */
	public DataBaseConnection(String user, String pass) {
		this(user, pass, "betaville", "localhost", 3306);
	}
	
	
	/**
	 * Constructor - Creates (opens) the SQL connection
	 */
	public DataBaseConnection(String user, String pass, String host) {
		this(user, pass, "betaville", host, 3306);
	}
	
	/**
	 * Constructor - Creates (opens) the SQL connection
	 */
	public DataBaseConnection(String user, String pass, String dbName, String host, int port) {
		try {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				con = DriverManager.getConnection(
						"jdbc:mysql://"+host+":"+port+"/"+dbName,
						user,
						pass);
				
				statement = con.createStatement();
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException: " + e.getMessage());
			} catch (InstantiationException e) {
				System.err.println("InstantiationException: " + e.getMessage());
			} catch (IllegalAccessException e) {
				System.err.println("IllegalAccessException: " + e.getMessage());
			}
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
			System.err.println("SQLState: " + e.getSQLState());
			System.err.println("VendorError: " + e.getErrorCode());
		}
	}

	/**
	 * Sends the SQL query to the database
	 * 
	 * @param query
	 *            Query to send to the database
	 * @return The set of results obtained after the execution of the query
	 * @throws SQLException
	 */
	public ResultSet sendQuery(String query) throws SQLException {
		return con.createStatement().executeQuery(query);
	}
	
	public int sendUpdate(String update) throws SQLException{
		return statement.executeUpdate(update, Statement.RETURN_GENERATED_KEYS);
	}
	
	public int getLastKey(){
		try {
			ResultSet rs = statement.getGeneratedKeys();
			if(rs.next()){
				int last = rs.getInt(1);
				return last;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * Closes the connection with the database
	 */
	public void closeConnection() {
		try {
			if (con != null) {
				statement.close();
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(){
		return con;
	}
}
