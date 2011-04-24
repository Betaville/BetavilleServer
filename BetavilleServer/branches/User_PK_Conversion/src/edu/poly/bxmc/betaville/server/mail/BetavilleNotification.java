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
package edu.poly.bxmc.betaville.server.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * @author Skye Book
 *
 */
public abstract class BetavilleNotification extends MimeMessage {
	
	protected String messageString="";
	protected HashMap<String, String> variables;

	/**
	 * @param session
	 */
	public BetavilleNotification(Session session) {
		super(session);
		variables = new HashMap<String, String>();
	}
	
	/**
	 * Loads a supplied HTML file into the messageString field.
	 * @param fileString
	 * @throws IOException
	 */
	protected void loadContent(String fileString) throws IOException{
		File file = new File(fileString);
		byte[] buf = new byte[(int)file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(buf);
		messageString = new String(buf);
		fis.close();
	}
	
	protected abstract void loadFallbackContent() throws IOException;
	
	protected boolean verifyMessageValidity(){
		for(String variable : variables.keySet()){
			if(!messageString.contains(variable)) return false;
		}
		return true;
	}
}
