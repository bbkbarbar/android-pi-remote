package hu.barbar.piclient;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import hu.barbar.comm.client.Client;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.RGBMessage;
import hu.barbar.util.LogManager;

public class MainActivity extends Activity {

	MainActivity thisApp = null;
	
	Button btnConnect = null;
	private EditText editHost = null,
					 editPort = null;
	
	private Button btnSelectColor = null;
	private SeekBar[] seekBars = null;
	private static final int RED = 0,
							 GREEN = 1,
							 BLUE = 2;
	private LinearLayout colorSample = null;
	private TextView tvColor = null;
	
	private Button btnSendCommand = null;
	private EditText commandLine = null;
	
	private EditText textArea = null;
	
	private LogManager log;
	
	private Client myClient = null;
	//private CommunicationThread comm = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		thisApp = MainActivity.this;
		
		log = new LogManager(LogManager.Level.INFO) {
			
			@Override
			public void showWarn(String text) {
				showText(text);
			}
			
			@Override
			public void showInfo(String text) {
				showText(text);
			}
			
			@Override
			public void showError(String text) {
				showText(text);
			}
		}; 
		
		initUI();

	}
	
	private void initUI(){
		
		textArea = (EditText) findViewById(R.id.text_area); 
		
		btnSelectColor = (Button) findViewById(R.id.btn_select_color);
		btnSelectColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendColor();
			}
		});
		
		colorSample = (LinearLayout) findViewById(R.id.color_sample);

		seekBars = new SeekBar[3];
		seekBars[RED] = (SeekBar) findViewById(R.id.sb_color_red);
		seekBars[GREEN] = (SeekBar) findViewById(R.id.sb_color_green);
		seekBars[BLUE] = (SeekBar) findViewById(R.id.sb_color_blue);
		OnSeekBarChangeListener osbcl = new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int color = getColorFromSeekBars();
				if(tvColor != null){
					tvColor.setText("#" + getColorHex(color));
				}
				if(colorSample != null){
					colorSample.setBackgroundColor(color);
				}
				
				if(myClient != null && myClient.isConnected()){
					
					//comm.sendMessage(new RGBMessage("setColor", Color.red(color), Color.green(color), Color.blue(color)));
				}
				/*
				if(myClient != null && myClient.isConnected()){
					myClient.sendMessage();
				}/**/
			}
		};
		for(int i=0; i<seekBars.length; i++){
			seekBars[i].setMax(255);
			seekBars[i].setProgress(255);
			seekBars[i].setOnSeekBarChangeListener(osbcl);
		}
		
		tvColor = (TextView) findViewById(R.id.tv_color);
	
		/*
		int mPaint = 0xFF9933;
		ColorPickerDialog cp = null; 
		//cp = new ColorPickerDialog(MainActivity.this, MainActivity.this, mPaint).show();
		/**/
		
		
		
		editHost = (EditText) findViewById(R.id.edit_host);
		editPort = (EditText) findViewById(R.id.edit_port);
		
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnConnect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(btnConnect.getText().toString().equals("Disconnect")){
					disconnect();
				}else{
					String host = editHost.getText().toString();
					int port = Integer.valueOf(editPort.getText().toString());
					
					/*
					myClient = new Client(host, port) {
						
						@Override
						protected void showOutput(String text) {
							MainActivity.this.showText(text);
						}
						
						@Override
						protected void handleRecievedMessage(Msg message) {
							MainActivity.this.showText("Received: " + message.toString());
						}
					};/**/
					/*
					comm = new CommunicationThread(thisApp, host, port, 1000){

						@Override
						public void showText(String text) {
							MainActivity.this.showText(text);
						}

						@Override
						public void onClientConnected(String host, int port) {
							new Thread() {
						        public void run() {
						        	runOnUiThread(new Runnable() {
					                    @Override
					                    public void run() {
					                    	MainActivity.this.showText("Connected: " + comm.getHost() + " @ " + comm.getPort());
					                    	MainActivity.this.btnConnect.setText("Disconnect");
					                    }
					                });
						        }
						    }.start();
						}

						@Override
						protected void handleRecievedMessage(final Msg message) {
							new Thread() {
						        public void run() {
						        	runOnUiThread(new Runnable() {
					                    @Override
					                    public void run() {
					                    	MainActivity.this.showText("Received: " + message.toString());
					                    }
					                });
						        }
						    }.start();
						}

						@Override
						protected void onDisconnected(String host2, int port2) {
							// TODO Auto-generated method stub
							
						}
						
					};/**/
					
					
					myClient = new Client(host, port, 1000) {
						
						@Override
						protected void showOutput(String arg0) {
							showText(arg0);
						}
						
						public void onConnected(String host, int port) {
							MainActivity.this.onClientConnected(host, port);
						};
						
						public void onDisconnected(String host, int port) {
							//btnConnect.setText("Connect");
						};
						
						@Override
						protected void handleRecievedMessage(Msg message) {
							showText("Received: " + message.toString());
						}
					};
					/**/
					
					/*
					comm.setLogManager(log);
					comm.start();
					/**/
					
					myClient.setLogManager(log);
					myClient.execute();
					/**/
					
					
					//btnConnect.setText("Disconnect");
				}
			}
		});
		
		
		commandLine = (EditText) findViewById(R.id.edit_command_line);
		btnSendCommand = (Button) findViewById(R.id.btn_send);
		btnSendCommand.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendCommand();
				commandLine.setText("");
			}
		});
	}
	
	
	public void onClientConnected(String host, int port){
		btnConnect.setText("Disconnect");
	}
	
	
	private void disconnect(){
		if(myClient != null){
			myClient.disconnect();
			try{
				btnConnect.setText("Connect");
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
                			if(textArea != null){
                				textArea.append(text + "\n");
                			}
                		}catch(Exception e){
                			Log.e("MainActivity.showText", e.getMessage());
                		}
                    }
                });
	        }
	    }.start();
		
	}
	
	private int getColorFromSeekBars(){
		int color = Color.rgb(
			seekBars[RED].getProgress(), 
			seekBars[GREEN].getProgress(), 
			seekBars[BLUE].getProgress()
		);
		return color;
	}
	
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
	
	private void sendColor(){
		if(myClient != null){
			RGBMessage m =
			new RGBMessage("setColor", 
							seekBars[RED].getProgress(), 
							seekBars[GREEN].getProgress(), 
							seekBars[BLUE].getProgress()
			);
			if(myClient.sendMessage(m)){
				showText("Sent: ");
			}else{
				showText("NOT sent: ");
			}
			showText(m.toString());
		}
	}
	
	private void sendCommand(){
		if(myClient != null){
			Msg toSend = new Msg(commandLine.getText().toString(), Msg.Types.COMMAND);
			if( myClient.sendMessage(toSend) ){
				showText("Sent: " + toSend.toString());
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onPause() {
		disconnect();
		super.onPause();
	}
	
}
