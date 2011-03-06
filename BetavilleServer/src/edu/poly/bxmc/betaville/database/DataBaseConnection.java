/**
 * Copyright 2008-2010 Brooklyn eXperimental Media Center
 * Betaville Project by Brooklyn eXperimental Media Center at NYU-Poly
 * http://bxmc.poly.edu
 */
package edu.poly.bxmc.betaville.database;

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
		try {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				con = DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/betaville",
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
				System.out.println("last inserted " + last);
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
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
