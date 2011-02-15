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
package edu.poly.bxmc.betaville.server.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import edu.poly.bxmc.betaville.server.Client;

public class ListServerRenderer extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
	private JLabel jlbl_clientName;
	private JLabel jlbl_ipClient;
	
	/**
	 * Constructor - Create the render and its components
	 */
	public ListServerRenderer() {
		setBackground(DEFAULT_BACKGROUND_COLOR);
		setLayout(new GridLayout(2,1));
		createClientLabels();
	}

	/**
	 * Method <CreateClientLabels> - Creation of the components for the client
	 */
	private void createClientLabels() {
		// Components for the client name
		jlbl_clientName = new JLabel();
		jlbl_clientName.setBackground(DEFAULT_BACKGROUND_COLOR);
		
		add(jlbl_clientName);
		
		// Components for the client ip
		JLabel jlbl_ip = new JLabel("IP : ");
		jlbl_ip.setBackground(DEFAULT_BACKGROUND_COLOR);
		jlbl_ipClient = new JLabel();
		jlbl_ipClient.setBackground(DEFAULT_BACKGROUND_COLOR);
		
		JPanel jp_ip = new JPanel();
		jp_ip.setBackground(DEFAULT_BACKGROUND_COLOR);
		jp_ip.setLayout(new FlowLayout());
		jp_ip.add(jlbl_ip);
		jp_ip.add(jlbl_ipClient);
		
		add(jlbl_ipClient);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof Client) {
			jlbl_clientName.setText(((Client)value).getClientName());
			//jlbl_ipClient.setText(((Client)value).getClientSocket().getLocalAddress().toString());
		}
		return null;
	}

}
