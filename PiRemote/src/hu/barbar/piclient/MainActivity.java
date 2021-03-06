package hu.barbar.piclient;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.tasker.Commands;
import hu.barbar.comm.util.tasker.PWMMessage;
import hu.barbar.comm.util.tasker.RGBMessage;
import hu.barbar.comm.util.tasker.StateMsg;
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
	
	
	private SeekBar pwmOutput = null;
	private TextView tvPWMOutput = null;
	private Spinner pwmChannelSpinner = null;
	private Button btnSetPWMOutput = null;
	
	private OnClickListener sendBtnOnClickListener = null;
	private Button btnSendCommand = null;
	private Button btnClear = null;
	private EditText commandLine = null;
	
	private EditText textArea = null;
	
	private LogManager log;
	private ConfigManager myConfigManager = null;
	
	public static AlertDialog alertForDialog = null;
	
	//private Client myClient = null;
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
				showText(text);
				Log.w("Comm", text);
			}
			
			@Override
			public void showInfo(String text) {
				showText(text);
				Log.i("Comm", text);
			}
			
			@Override
			public void showError(String text) {
				showText(text);
				Log.e("Comm", text);
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
				
			}
		};
		for(int i=0; i<seekBars.length; i++){
			seekBars[i].setMax(255);
			seekBars[i].setProgress(255);
			seekBars[i].setOnSeekBarChangeListener(osbcl);
		}
		
		tvPWMOutput = (TextView) findViewById(R.id.tv_pwm_output);
		pwmOutput = (SeekBar) findViewById(R.id.sb_pwm);
		pwmOutput.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				tvPWMOutput.setText("" + progress + "%");
			}
		});
		
		pwmChannelSpinner = (Spinner) findViewById(R.id.pwm_channel_spinner);
		btnSetPWMOutput = (Button) findViewById(R.id.btn_set_pwm);
		btnSetPWMOutput.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				int ch = pwmChannelSpinner.getSelectedItemPosition();
				if (ch >= 0){
					ch += 3;
					PWMMessage pwmMessage = new PWMMessage(ch, Float.valueOf( pwmOutput.getProgress() ));
					if(comm != null && comm.isConnected()){
						comm.sendMessage(pwmMessage);
						textArea.setText(
								textArea.getText() + "Sent: "
								+ pwmMessage.toString() + "\n"
						);
					}else{
						textArea.setText(
								textArea.getText() + "Not connected;\nCan not send: "
								+ pwmMessage.toString() + "\n"
						);
					}
					
				}
			}
		});
		
		tvColor = (TextView) findViewById(R.id.tv_color);
	
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
			}
		});
		
		
		commandLine = (EditText) findViewById(R.id.edit_command_line);
		btnSendCommand = (Button) findViewById(R.id.btn_send);
		sendBtnOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendCommand(commandLine.getText().toString());
				commandLine.setText("");
			}
		};
		btnSendCommand.setOnClickListener(sendBtnOnClickListener);
		
		btnClear = (Button) findViewById(R.id.btn_clear);
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(textArea != null){
					textArea.setText("");
				}
			}
		});
		
		editPort.setText(myConfigManager.loadString("port", "10714"));
		editHost.setText(myConfigManager.loadString("host", "barbarhome.ddns.net"));
		
	}
	
	
	protected void handleRecievedMessage(Msg message) {
		showText("Received: " + message.toString());
		if(message.getType() == Msg.Types.RESPONSE_STATE){
			StateMsg sm = (StateMsg) message;
			Toast.makeText(getApplication(), "State response:\n" + sm.toString(), 1).show();
		}
	}
	
	protected void onClientConnected(String host, int port){
		showText("Connected: " + host + " @ " + port);
    	btnConnect.setText("Disconnect");
	}

	
	private void disconnect(){
		if(comm != null){
			comm.disconnect();
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
		if(comm != null){
			RGBMessage m =
			new RGBMessage("setColor", 
							seekBars[RED].getProgress(), 
							seekBars[GREEN].getProgress(), 
							seekBars[BLUE].getProgress()
			);
			showText(m.toString());
			if(comm.sendMessage(m)){
				showText("..Sent");
			}else{
				showText("..NOT sent");
			}
			
		}
	}
	
	private void sendCommand(String command){
		if(comm != null){
			Msg toSend = new Msg(command, Msg.Types.REQUEST);
			if( comm.sendMessage(toSend) ){
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
		if (id == R.id.show_dialog_for_commands) {
			
			final CharSequence[] items = {
									"Read temp",
									"Enable todo item", 
									"Disable todo item", 
									"Client count",
									"Send workerinfo mail",
									"stop server",
									"get worker info"
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Do something with the selection
				
				switch (item) {
				case 0:
					/*commandLine.setText(Commands.GET_TEMP);
					sendBtnOnClickListener.onClick(null);/**/
					comm.sendMessage(new Msg(Commands.GET_TEMP, Msg.Types.REQUEST));
					break;
				case 1:
					commandLine.setText(Commands.ENABLE_TODO_ITEM);
					break;
				case 2:
					commandLine.setText(Commands.DISBALE_TODO_ITEM);
					break;
				case 3:
					commandLine.setText(Commands.GET_CLIENT_COUNT);
					sendBtnOnClickListener.onClick(null);
					break;
				case 4:
					commandLine.setText(Commands.EMAIL_WORKER_INFO + " bbk.barbar@gmail.com");
					sendBtnOnClickListener.onClick(null);
					break;
				case 5:
					commandLine.setText(Commands.STOP_SERVER);
					sendBtnOnClickListener.onClick(null);
					break;
				case 6:
					commandLine.setText(Commands.GET_WORKER_INFO);
					sendBtnOnClickListener.onClick(null);
					break;

				default:
					break;
				}
				
				alertForDialog.dismiss();
			}
			});
			alertForDialog = builder.create();
			alertForDialog.show();
		}
		return super.onOptionsItemSelected(item);
	}

	
	@Override
	protected void onPause() {
		
		myConfigManager.storeString("port", editPort.getText().toString());
		myConfigManager.storeString("host", editHost.getText().toString());
		
		disconnect();
		super.onPause();
	}
	
}
