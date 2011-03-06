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
