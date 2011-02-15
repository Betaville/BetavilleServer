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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Skye Book
 *
 */
public class UserArrayUtils {
	public static List<String> getArrayUsers(String array){
		ArrayList<String> users = new ArrayList<String>();
		if(array!=null){
			if(array.startsWith("0:")) return users;
			String semiDelimVals = array.substring(array.indexOf(":")+1);
			while(semiDelimVals.contains(";")){
				users.add(semiDelimVals.substring(0,semiDelimVals.indexOf(";")));
				semiDelimVals = semiDelimVals.substring(semiDelimVals.indexOf(";")+1);
			}
		}
		return users;
	}

	public static boolean checkArrayForUser(String array, String user){
		String semiDelimVals = array.substring(array.indexOf(":")+1);
		
		while(semiDelimVals.contains(";")){
			System.out.println(semiDelimVals.substring(0,semiDelimVals.indexOf(";")));
			if(user.equals(semiDelimVals.substring(0,semiDelimVals.indexOf(";")))) return true;
			semiDelimVals = semiDelimVals.substring(semiDelimVals.indexOf(";")+1);
		}
		
		return false;
	}
	
	public static String getArrayNamesAsSemiDelim(String array){
		return array.substring(array.indexOf(":")+1);
	}
	
	public static int getSizeOfFaveArray(String array){
		return Integer.parseInt(array.substring(0, array.indexOf(":")));
	}
	
	public static String createArrayFromUsers(List<String> userList){
		if(userList.size()==0) return "0:;";
		String array = ""+userList.size()+":";
		for(String user : userList){
			array+=(user+";");
		}
		return array;
	}
}
