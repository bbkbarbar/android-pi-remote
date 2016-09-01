package hu.barbar.comm.client;

import java.io.IOException;
import java.io.ObjectInputStream;

import hu.barbar.comm.util.Msg;

public abstract class ReceiverThread extends Thread {

	private ObjectInputStream objIn = null;
	private Client myParent = null;
	
	/**
	 * @param in ObjectInputStream from client's socket.
	 * @param log 
	 */
	public ReceiverThread(ObjectInputStream in, Client parent){
		this.objIn = in;
		myParent = parent;
	}
	
	@Override
	public void run() {
		
		Msg msg = null;
		
		try {

			while( (msg = (Msg) objIn.readObject()) != null ){
				handleMessage(msg);
			}
			
		} catch (IOException e) {
			if(myParent.getWantToDisconnect() == false){
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 *  Abstract function to handle incoming Message objects in place of using Receiver object.
	 * @param message
	 */
	protected abstract void handleMessage(Msg message);
	
}
