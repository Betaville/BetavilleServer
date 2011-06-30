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

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author Skye Book
 *
 */
public class GMailer extends AbstractMailer {

	/**
	 * @param host
	 * @param user
	 * @param pass
	 * @param port`
	 * @throws Exception
	 */
	public GMailer(String user, String pass)
	throws Exception {
		super("smtp.gmail.com", user, pass, 587, true, true);
	}

	/* (non-Javadoc)
	 * @see edu.poly.bxmc.betaville.server.mail.AbstractMailer#validateAddress(java.lang.String)
	 */
	protected boolean validateAddress(String address) {
		return address.contains("@");
	}

	public static void main(String[] args) throws Exception{
		// This is an example on how to use the mailer!
		GMailer mailer = new GMailer("notifications@betaville.net", "...");
		MimeMessage message = new CommentNotificationMessage(mailer.session, "Carl", "sbook", "Some Thing", "I really like this thing", "cskelton@poly.edu");
		message.setFrom(new InternetAddress("notifications@betaville.net"));
		mailer.sendMailNow(message);
	}

}
