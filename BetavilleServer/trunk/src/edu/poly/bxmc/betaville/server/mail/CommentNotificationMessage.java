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

import java.io.IOException;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A message for notifying users about comments on their designs.
 * @author Skye Book
 * @see MimeMessage
 */
public class CommentNotificationMessage extends BetavilleNotification{
	
	/**
	 * @param session
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public CommentNotificationMessage(Session session, String user, String userWhoMadeComment, String designName, String comment, String userEmail) throws MessagingException, IOException{
		super(session);
		
		// load the variables
		variables.put(MailVariables.RECIPIENT, user);
		variables.put(MailVariables.COMMENT_CONTENT, comment);
		variables.put(MailVariables.COMMENT_USER, userWhoMadeComment);
		variables.put(MailVariables.DESIGN_NAME, designName);
		
		// Load the content specified by the configfile
		loadContent("MailAssets/comment_notification.html");
		// If the message supplied doesn't have the correct variables then use one that we know does.
		if(!verifyMessageValidity()) loadFallbackContent();
		
		// Fill the message
		setContent(createContent(user, userWhoMadeComment, designName, comment), "text/html");
		setSubject("New Betaville Comment");
		setRecipient(RecipientType.TO, new InternetAddress(userEmail));
		setFrom(new InternetAddress("notifications@betaville.net"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.BetavilleNotification#loadFallbackContent()
	 */
	protected void loadFallbackContent() throws IOException{
		loadContent("MailAssets/comment_notification.html");
	}
	
	private String createContent(String user, String userWhoMadeComment, String designName, String comment){
		// Run through each of the variables and replace them.
		for(Entry<String, String> set : variables.entrySet()){
			messageString = messageString.replace(set.getKey(), set.getValue());
		}
		return messageString;
	}
}
