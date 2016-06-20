package hu.barbar.comm.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class made to handle communication with server
 * @author BA
 */
public abstract class ClientThread extends Thread {
	
	private Socket mySocket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	private boolean isInitialized = false;
	
	private boolean expectFurtherMessages = true;
	
	
	public ClientThread(Socket socket) {
		this.mySocket = socket;
	}
	
	/**
	 * @return True when everything is ready for communication.
	 */
	public boolean isOK(){
		return (isInitialized && mySocket != null && mySocket.isConnected());
	}
	
	
	@Override
	public void run() {
		
		String receivedText = null;
		if(mySocket.isConnected()){
			
			try {
				in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
				out = new PrintWriter(mySocket.getOutputStream(), true);
				
				isInitialized = true;
				
				int exceptionCounter = 0;
				
				while( expectFurtherMessages ){
					try{
						receivedText = in.readLine();
						handleReceivedMessage(receivedText);
					}catch(Exception e){
						exceptionCounter++;
						//System.out.println("Exception while try to read incoming message.");
						if(exceptionCounter >= 5){
							disconnect();
						}
					}
				}
				
			} catch (IOException e) {
				// TODO handle exception
				e.printStackTrace();
			}
		}else{
			System.out.println("SOCKET is closed on client side");
		}
		
	}

	public void disconnect(){
		this.expectFurtherMessages = false;
		this.interrupt();
	}
	
	public boolean sendMessageToServer(String message){
		
		int numberOfAttemps = 0;
		while(!isInitialized && numberOfAttemps++ < 5){
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}
		
		if (this.out != null) {
			out.println(message);
			return true;
		} else {
			System.out.println("ERROR: objOutput is NULL!");
			return false;
		}
	}
	
	public abstract boolean handleReceivedMessage(String message);
	
}
