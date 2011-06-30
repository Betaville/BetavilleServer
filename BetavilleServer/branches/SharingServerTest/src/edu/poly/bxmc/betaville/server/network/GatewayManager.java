package edu.poly.bxmc.betaville.server.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.poly.bxmc.betaville.server.Client;

public class GatewayManager {
	/**
	 * Constant <serverPort> - Port of the server used
	 */
	private final int serverPort = 14500;
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private Vector<String> serverList;
	private int currentIndex=0;
	private HashMap<String, Integer> failList = new HashMap<String, Integer>();
	private int failThreshold=3;
	private int challenge="HelloKitty".hashCode();

	/**
	 * Constructor - Create the server socket and connect to clients
	 * 
	 * @param manager
	 * @param gui
	 */
	public GatewayManager() {
		serverList = new Vector<String>();
		try {
			// Creation of the server socket
			ServerSocket server = new ServerSocket(serverPort);

			while (true) {
				// Waiting for a incoming client connection request
				final Socket socketClient = server.accept();
				threadPool.submit(new Runnable(){
					@Override
					public void run() {
						if (socketClient.isConnected()) {
							final Client client = new Client(socketClient);

							ObjectInputStream input;
							try {
								input = new ObjectInputStream(client.getClientSocket().getInputStream());
								ObjectOutputStream output = new ObjectOutputStream(client.getClientSocket().getOutputStream());
								String token = (String)input.readObject();
								if(token.equals("findserver")){
									output.writeObject(serverList.get(currentIndexLocation()));
									incrementCurrentIndex();
								}
								else if(token.equals("addserver")){
									output.writeObject("challenge");
									String response = (String) input.readObject();
									if(response.hashCode()==challenge){
										addServerToList(client.getClientSocket().getInetAddress().getHostAddress());
									}
								}
								else if(token.equals("removeserver")){
									output.writeObject("challenge");
									String response = (String) input.readObject();
									if(response.hashCode()==challenge){
										removeServerFromList(client.getClientSocket().getInetAddress().getHostAddress());
									}
								}
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void addServerToList(String address){
		serverList.add(address);
	}

	private synchronized void removeServerFromList(String addr){
		serverList.remove(addr);
		failList.remove(addr);
		while(currentIndex>(serverList.size()-1)){
			currentIndex--;
		}
	}

	private synchronized int currentIndexLocation(){
		return currentIndex;
	}

	private synchronized void incrementCurrentIndex(){
		if(currentIndex<(serverList.size()-1)){
			currentIndex++;
		}
		else{
			currentIndex=0;
		}
	}

	private void maintainServerList(){
		Iterator<String> it = serverList.iterator();
		while(it.hasNext()){
			String addr = it.next();
			try {
				Socket tester = new Socket(addr, serverPort);
			} catch (UnknownHostException e) {
				synchronized(failList){
					if(!failList.containsKey(addr)){
						failList.put(addr, 1);
					}
					else if(failList.get(addr)==failThreshold){
						removeServerFromList(addr);
					}
					else{
						int t = failList.get(addr);
						failList.put(addr, t+1);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
