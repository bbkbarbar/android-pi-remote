package hu.barbar.piclientfancy;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.tasker.RGBMessage;
import hu.barbar.comm.util.tasker.StateMsg;
import hu.barbar.util.LogManager;

public class MainActivity extends Activity {

	MainActivity thisApp = null;
	
	private static final int RED = 0,
							 GREEN = 1,
							 BLUE = 2;
	
	private LogManager log = null;
	private ConfigManager myConfigManager = null;
	private CommunicationThread comm = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		myConfigManager = new ConfigManager(MainActivity.this);
		
		thisApp = MainActivity.this;
		
		log = new LogManager(LogManager.Level.INFO) {
			
			@Override
			public void showWarn(String text) {
				Log.w("Comm", text);
			}
			
			@Override
			public void showInfo(String text) {
				Log.i("Comm", text);
			}
			
			@Override
			public void showError(String text) {
				Log.e("Comm", text);
			}
		}; 
		
		initUI();
		
		int port = Integer.valueOf(myConfigManager.loadString("port","10714"));
		String host = myConfigManager.loadString("host","barbarhome.ddns.net");
		connect(host, port);

	}
	
	private void initUI(){
		//TODO
	}
	
	
	protected void handleRecievedMessage(Msg message) {
		showText("Received: " + message.toString());
		//TODO
		if(message.getType() == Msg.Types.RESPONSE_STATE){
			StateMsg sm = (StateMsg) message;
			Toast.makeText(getApplication(), "State response:\n" + sm.toString(), 1).show();
		}
	}
	
	
	protected void connect(String host, int port){
		
		comm = new CommunicationThread(thisApp, host, port, 1000){

			@Override
			public void showText(String text) {
				MainActivity.this.showText(text);
			}

			@Override
			public void onClientConnected(final String host, final int port) {
				new Thread() {
			        public void run() {
			        	runOnUiThread(new Runnable() {
		                    @Override
		                    public void run() {
		                    	MainActivity.this.onClientConnected(host, port);
		                    }
		                });
			        }
			    }.start();
			}

			@Override
			protected void handleRecievedMessage(final Msg message) {
				Thread thread =
				new Thread() {
			        public void run() {
			        	runOnUiThread(new Runnable() {
		                    @Override
		                    public void run() {
		                    	MainActivity.this.handleRecievedMessage(message);
		                    }
		                });
			        }
			    };
			    thread.start();
			}

			@Override
			protected void onDisconnected(String host2, int port2) {
			}
			
		};
		
		comm.start();
	}
	
	protected void onClientConnected(String host, int port){
		showText("Connected: " + host + " @ " + port);
		//TODO
    	//btnConnect.setText("Disconnect");
	}

	
	private void disconnect(){
		if(comm != null){
			comm.disconnect();
			try{
				//TODO
				//btnConnect.setText("Connect");
			}catch(Exception doesNotMatterWhenTryToDisconnectBecauseAppClosing){}
		}
	}
	
	
	public void showText(final String text){
		
		new Thread() {
	        public void run() {
	        	runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	try{
                    		
                    		//TODO Show text here
                    		
                    		/*
                			if(textArea != null){
                				textArea.append(text + "\n");
                			}
                			/**/
                		}catch(Exception e){
                			Log.e("MainActivity.showText", e.getMessage());
                		}
                    }
                });
	        }
	    }.start();
		
	}
	
	/*
	private int getColorFromSeekBars(){
		int color = Color.rgb(
			seekBars[RED].getProgress(), 
			seekBars[GREEN].getProgress(), 
			seekBars[BLUE].getProgress()
		);
		return color;
	}/**/
	
	private String getColorHex(int c){
		String res = ""; 
		String r = Integer.toHexString(Color.red(c));
		String g = Integer.toHexString(Color.green(c));
		String b = Integer.toHexString(Color.blue(c));
		if(r.length()==1)
			r = "0" + r;
		if(g.length()==1)
			g = "0" + g;
		if(b.length()==1)
			b = "0" + b;
		res += r;
		res += g;
		res += b;
		return res;
	}
	
	private void sendColor(int red, int green, int blue){
		if(comm != null){
			RGBMessage m =
			new RGBMessage("setColor", 
							red, 
							green, 
							blue
			);
			
			comm.sendMessage(m);
			
		}
	}
	
	private void sendCommand(String command){
		if(comm != null){
			Msg toSend = new Msg(command, Msg.Types.REQUEST);
			if( comm.sendMessage(toSend) ){
			}else{
				showText("Can NOT send message");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	
	@Override
	protected void onPause() {
		
		myConfigManager.storeString("port", comm.getPort() + "");
		myConfigManager.storeString("host", comm.getHost());
		
		disconnect();
		super.onPause();
	}
	
}
