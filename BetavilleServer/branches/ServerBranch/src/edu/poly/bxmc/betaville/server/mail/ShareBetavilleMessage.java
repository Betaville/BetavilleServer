/**
 * 
 */
package edu.poly.bxmc.betaville.server.mail;

import java.io.IOException;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;

/**
 * @author Jarred Humphrey
 *
 */
public class ShareBetavilleMessage extends BetavilleNotification{
	private static final Logger logger = Logger.getLogger(ShareBetavilleMessage.class);
	
	/**
	 * @param session
	 * @throws IOException 
	 * @throws MessagingException 
	 */
	public ShareBetavilleMessage(Session session, String userName, String designID, String shareEmail, String comment) throws MessagingException, IOException{
		super(session);
		
		// load the variables
		variables.put(MailVariables.RECIPIENT, shareEmail);
		variables.put(MailVariables.COMMENT_CONTENT, comment);
		variables.put(MailVariables.USER_NAME, userName);
		variables.put(MailVariables.DESIGN_ID, designID);
		
		// Load the content specified by the configfile
		loadContent("MailAssets/comment_notification.html");
		// If the message supplied doesn't have the correct variables then use one that we know does.
		if(!verifyMessageValidity()) loadFallbackContent();
		
		// Fill the message
		setContent(createContent(userName, shareEmail, designID, comment), "text/html");
		setSubject(userName + " wants to share Betaville with you!");
		logger.info("Sending email to: " + shareEmail);
		setRecipient(RecipientType.TO, new InternetAddress(shareEmail));
		setFrom(new InternetAddress("notifications@betaville.net"));
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.BetavilleNotification#loadFallbackContent()
	 */
	protected void loadFallbackContent() throws IOException{
		loadContent("MailAssets/comment_notification.html");
	}
	
	public Session getSession(){
		return session;
	}
	
	private String createContent(String user, String userWhoMadeComment, String designName, String comment){
		// Run through each of the variables and replace them.
		for(Entry<String, String> set : variables.entrySet()){
			messageString = messageString.replace(set.getKey(), set.getValue());
		}
		return messageString;
	}
}