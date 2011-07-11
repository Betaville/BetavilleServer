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
package edu.poly.bxmc.betaville.server.mail;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

/**
 * @author Skye Book
 *
 */
public abstract class AbstractMailer implements Mailer{
	
	protected Session session;
	private ArrayList<Message> messageQueue;
	

	/**
	 * 
	 */
	public AbstractMailer(String host, String user, String pass, int port, boolean starttls, boolean requiresAuth) throws Exception{
		if(!validateAddress(user)) throw new Exception();
		
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", user);
		props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.starttls.enable", Boolean.toString(starttls));
		props.put("mail.smtp.auth", Boolean.toString(requiresAuth));
		
		session = Session.getDefaultInstance(props, null);
	}
	
	protected abstract boolean validateAddress(String address);
	
	/* (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.Mailer#sendMailNow(edu.poly.bxmc.betaville.server.mail.Message)
	 */
	@Override
	public void sendMailNow(Message message) throws MessagingException {
		sendMail(message);
	}

	/* (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.Mailer#addToMailQueue(edu.poly.bxmc.betaville.server.mail.Message)
	 */
	@Override
	public void addToMailQueue(Message message) {
		messageQueue.add(message);
	}
	
	/*
	 * (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.Mailer#flushMailQueue()
	 */
	@Override
	public void flushMailQueue() throws MessagingException{
		for(Message message : messageQueue){
			sendMail(message);
		}
	}
	
	private void sendMail(Message message) throws MessagingException{
		Transport transport = session.getTransport("smtp");
		transport.connect(session.getProperty("mail.smtp.host"), session.getProperty("mail.smtp.user"), session.getProperty("mail.smtp.password"));
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}
}
