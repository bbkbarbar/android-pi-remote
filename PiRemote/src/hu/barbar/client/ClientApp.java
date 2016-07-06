package hu.barbar.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import hu.barbar.comm.client.Client;
import hu.barbar.comm.util.Commands;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.RGBMessage;
import hu.barbar.util.LogManager;

public class ClientApp {

	private static ClientApp me = null;

	private static final boolean DEBUG_MODE = true;

	public static String ver = "0.1";
	public static final String DEFAULT_SERVER_HOSTNAME = (DEBUG_MODE ? "barbarhome.ddns.net" : "localhost");
	//public static final String DEFAULT_SERVER_HOSTNAME = (DEBUG_MODE ? "localhost" : "192.168.0.101");
	public static final int DEFAULT_SERVER_PORT = (DEBUG_MODE ? 10714 : 13003 );

	public String SERVER_HOSTNAME = DEFAULT_SERVER_HOSTNAME;
	public int SERVER_PORT = -1;

	private LogManager log = null;
	private Client myClient = null;
	
	//TODO
	boolean responseReceived = false;
	
	public static void main(String[] args) {

		if (DEBUG_MODE) {
			ver = ver + " DEBUG MODE";
		}

		me = new ClientApp();
		me.start(args);
	}

	private void start(String[] args) {
		
		this.log = new LogManager("Client ::", LogManager.Level.WARN){

			@Override
			public void showInfo(String text) {
				System.out.println(text);
			}

			@Override
			public void showWarn(String text) {
				System.out.println(text);
			}

			@Override
			public void showError(String text) {
				System.out.println(text);
			}
			
		};
		
		myClient = new Client(DEFAULT_SERVER_HOSTNAME, DEFAULT_SERVER_PORT) {
			
			@Override
			protected void showOutput(String text) {
				System.out.println(text);
			}
			
			@Override
			protected void handleRecievedMessage(Msg message) {
				if(message.getContent().startsWith("Temp: ")){
					String[] result = message.getContent().split(" ");
					System.out.println("\nTemp: " + result[result.length-1] + "C");
				}else{
					System.out.println("\nMessage received from SERVER: " + message.getContent());
				System.out.println(message.toString());
			}
			}
			
			@Override
			public void onConnectionRefused(String host, int port) {
				System.out.println("Connection refused (" + host + " @ " + port + ").");
			}
			
		};
		myClient.setLogManager(log);
		myClient.start();
		
		if(myClient.waitWhileIsInitialized()){
			System.out.println("CLIENT IS INITILAIZED");

			
			Msg msg = new Msg(Commands.GET_DATE, Msg.Types.REQUEST);
			if(myClient.sendMessage(msg)){
				System.out.println("Sent: " + Commands.GET_DATE); 
			}
			readLine();

			
			if(myClient.sendMessage(new RGBMessage("setAll", 0,0,0))){
				System.out.println("Sent: " + "Color");
			}/**/

			// wait a little ..
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("InterruptedException in ClientApp");
			} /**/
			

			if(myClient.sendMessage(new Msg(Commands.GET_TEMP, Msg.Types.REQUEST))){
				System.out.println("Sent: " + Commands.GET_TEMP);
			}

			/*
			// wait a little ..
			try {
				Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("InterruptedException in ClientApp");
		} /**/
		
		
		}
		

		readLine();

		
		myClient.disconnect();

	}
	
	private String readLine(){
		String s = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        s = br.readLine();
	    }catch(Exception e){
	    	s = "Exception cought.";
	    }
        return s;
	}
	
	public void showOutput(String text){
		System.out.println(text);
	}

}
