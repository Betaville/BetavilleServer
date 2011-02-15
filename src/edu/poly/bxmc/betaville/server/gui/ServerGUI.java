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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;


/**
 * Class <ServerGUI> - Graphical user interface for the server
 * 
 * @author Caroline Bouchat
 * @version 0.1 - Spring 2009
 */
public class ServerGUI extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * Constant <DEFAULT_BACKGROUND_COLOR> - Default background color
	 */
	private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;

	private DefaultListModel listModel;

	/**
	 * Constructor - Instantiate the object
	 */
	public ServerGUI() {
		initFrame();
	}

	private JScrollPane createList() {
		listModel = new DefaultListModel();
		
		JList list = new JList(listModel);
		list.setBackground(DEFAULT_BACKGROUND_COLOR);
		list.setCellRenderer(new ListServerRenderer());
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setVisibleRowCount(10);
		
		JScrollPane jsp = new JScrollPane(list);
		jsp.setBackground(DEFAULT_BACKGROUND_COLOR);
		return jsp;
	}

	/**
	 * Method <createInfoServerPanel> - Create the panel which display the
	 * information about the server
	 * 
	 * @return Panel which contains the server's information
	 */
	private JPanel createInfoServerPanel() {
		JLabel jlbl = new JLabel("Server ...");

		JPanel jp = new JPanel();
		jp.setBackground(DEFAULT_BACKGROUND_COLOR);
		jp.setBorder(new TitledBorder("Information server"));

		jp.add(jlbl);
		return jp;
	}

	/**
	 * Method <initFrame> - Initialization of the main window
	 */
	private void initFrame() {
		// Sets the frame
		setBackground(DEFAULT_BACKGROUND_COLOR);
		setLayout(new BorderLayout());
		setLocationRelativeTo(null);
		setSize(800, 600);
		setTitle("Server");
		setVisible(true);
	
		// Add components
		JPanel jp = createInfoServerPanel();
		add(jp, BorderLayout.NORTH);
	
		JScrollPane jsp = createList();
		add(jsp, BorderLayout.CENTER);
	
		// Add listeners
		addWindowListener(new WindowAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent
			 * )
			 */
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(0);
			}
		});
	
		validate();
	}

	/**
	 * Method <getListModel> - Gets the list's model
	 *
	 * @return The model
	 */
	public DefaultListModel getListModel() {
		return listModel;
	}
}
