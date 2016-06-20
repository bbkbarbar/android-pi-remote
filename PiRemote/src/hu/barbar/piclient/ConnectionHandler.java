package hu.barbar.piclient;

import java.util.ArrayList;

import hu.barbar.comm.client.Client;

public class ConnectionHandler extends Thread{
	
	private static final int PORT_UNDEFINED = -1;
	
	private MainActivity myParent = null;
	private Client myClient = null;
	
	private String host = null; 
	private int port = PORT_UNDEFINED;
	
	private ArrayList<String> messageQueue; 
	
	public ConnectionHandler(MainActivity parent, String host, int port){
		this.myParent = parent;
		this.host = host;
		this.port = port;
		messageQueue = new ArrayList<String>();
	}
	
	@Override
	public void run() {
		
		
		//showText("Host: " + host + " @ " + port);
		
		myClient = new Client() {
			
			@Override
			protected void showOutput(String arg0) {
				myParent.showText("Client.output: " + arg0);
			}
			
			@Override
			protected void handleRecievedMessage(String arg0) {
				myParent.showText("Received msg: " + arg0);
			}
			
		};
		
		myClient.setHost(host);
		myClient.setPort(port);
		
		myClient.connect();
		
		if(myClient.waitWhileIsOK()){
			messageQueue.add("Client connected");
			//myParent.btnConnect.setText("Disconnect");
		}else{
			messageQueue.add("Client NOT connected");
		}
		/**/
		
		super.run();
		myParent.storeClient(myClient);
	}
	
	public String getQueueContent(){
		String res = "";
		for(int i=0;i<messageQueue.size(); i++){
			res += messageQueue.get(i) + "\n";
		}
		return res;
	}
	
	protected void connect(){
		//showText("Host: " + host + " @ " + port);
			
		myClient = new Client() {
			
			@Override
			protected void showOutput(String arg0) {
				myParent.showText("Client.output: " + arg0);
			}
			
			@Override
			protected void handleRecievedMessage(String arg0) {
				myParent.showText("Received msg: " + arg0);
			}
			
		};
		
		myClient.setHost(host);
		myClient.setPort(port);
		/*
		myClient.connect();
		
		if(myClient.waitWhileIsOK()){
			showText("Client connected");
			btnConnect.setText("Disconnect");
		}else{
			showText("Client NOT connected");
		}
		/**/
	}

}
