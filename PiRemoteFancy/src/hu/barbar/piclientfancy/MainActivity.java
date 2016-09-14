package hu.barbar.piclientfancy;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import hu.barbar.comm.util.Msg;
import hu.barbar.comm.util.tasker.Commands;
import hu.barbar.comm.util.tasker.RGBMessage;
import hu.barbar.comm.util.tasker.StateMsg;
import hu.barbar.util.LogManager;

/**
 * Main activity of Android PiClient (fancy version) 
 * @author Barbar
 */
public class MainActivity extends Activity {

	protected MainActivity thisApp = null;
	
	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	
	private LogManager log = null;
	private ConfigManager myConfigManager = null;
	
	
	private CommunicationThread comm = null;
	
	private boolean instantConnectEnabled = false;
	

	/*
	 *  UI - Connect and Refresh button 
	 */
	ImageButton btnConnectAndRefresh = null;
	private static final int FUNCTION_CONNECT = 0;
	private static final int FUNCTION_REFRESH = 1;

	private int funcitonOfConnectButton = FUNCTION_CONNECT;
	
	/*
	 *  UI - Value display textViews
	 */
	private TextView tvTempAir = null;
	private TextView tvHumidity = null;
	private TextView tvTempWater = null;
	
	/*
	 *  UI - Toggle buttons for external devices (light, heater etc)
	 */
	private ToggleButton btntAirPump = null;
	private ToggleButton btntFilter = null;
	private ToggleButton btntHeater = null;
	private ToggleButton btntLight = null;
	
	/*
	 *  Set color
	 */
	private static final int DEFAULT_COLOR = 0xFFFFFF;	
	private LinearLayout colorSample = null;
	private SeekBar[] seekBars = null;
	private ImageButton btnSetColor = null;

	private int selectedColor = DEFAULT_COLOR;
	
	
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
		}; /**/

		initUI();
		
		instantConnectEnabled = myConfigManager.loadBoolean("instant_connect", true);
		if(instantConnectEnabled){
			getConnectionParametersAndConnect();
		}
		
		/*
		 *  Load last setted color
		 */
		this.selectedColor = myConfigManager.loadInt("lastColor", DEFAULT_COLOR);
		showColorFromSeekBars(this.selectedColor);
		
	}
	
	
	private void initUI(){
		
		/*
		 *  Connect and Refresh button
		 */
		btnConnectAndRefresh = (ImageButton) findViewById(R.id.btn_connect_and_refresh);
		btnConnectAndRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(funcitonOfConnectButton == FUNCTION_CONNECT){
					
					getConnectionParametersAndConnect();
					
				}
				else
				if(funcitonOfConnectButton == FUNCTION_REFRESH){
					
					sendRequestsForRefresh();
					
				}else{
					Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_message_function_of_connect_and_refresh_button), 1).show();
				}
				
			}
		});
		
		
		/*
		 *   Value display line
		 */
		tvTempAir = (TextView) findViewById(R.id.tv_temp_air);
		tvHumidity = (TextView) findViewById(R.id.tv_humidity);
		tvTempWater = (TextView) findViewById(R.id.tv_temp_water);
		tvTempAir.setText("--.-°C");
		tvHumidity.setText("--.-%");
		tvTempWater.setText("--.--°C");
		
		
		/*
		 *  Toggle buttons for external devices
		 */
		btntAirPump = (ToggleButton) findViewById(R.id.btnt_air_pump);
		btntFilter  = (ToggleButton) findViewById(R.id.btnt_filter);
		btntHeater  = (ToggleButton) findViewById(R.id.btnt_heater);
		btntLight   = (ToggleButton) findViewById(R.id.btnt_light);
		
		btntAirPump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSetStateRequest("air_pump", (btntAirPump.isChecked()));
			}
		});
		btntFilter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSetStateRequest("filter", (btntFilter.isChecked()));
			}
		});
		btntHeater.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSetStateRequest("heater", (btntHeater.isChecked()));
			}
		});

		btntLight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*
				 *  wants to turn the lights ON
				 */
				if(btntLight.isChecked()){
					btnSetColor.setEnabled(true);
					//TODO try it out home (it should works properly)
					//btntLight.setChecked(false);
					sendColor(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
				}else{
					btnSetColor.setEnabled(false);
					sendColor(0, 0, 0);
				}
			}
		});
		
		
		/*
		 *  Color set seekBars
		 */
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
				
				selectedColor = getColorFromSeekBars();
				if(colorSample != null){
					colorSample.setBackgroundColor(selectedColor);
				}
				
			}
		};
		for(int i=0; i<seekBars.length; i++){
			seekBars[i].setMax(255);
			seekBars[i].setProgress(255);
			seekBars[i].setOnSeekBarChangeListener(osbcl);
		}
		
		/*
		 *  Set color button
		 */
		btnSetColor = (ImageButton) findViewById(R.id.btn_apply_color);
		btnSetColor.setEnabled(false);
		btnSetColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(btntLight.isChecked()){
					sendColor(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
				}
			}
		});
		
	}
	
	
	protected void getConnectionParametersAndConnect() {
		
		/*
		 *  Get host and port from stored references
		 */
		int port = Integer.valueOf(myConfigManager.loadString("port", "10714"));
		String host = myConfigManager.loadString("host", "barbarhome.ddns.net");
		
		/*
		 *  Connect
		 */
		connect(host, port);
		
	}

	
	protected void handleRecievedMessage(Msg message) {
		showText("Received: " + message.toString());
		
		/*
		 *  Process received STATE_OF_... message
		 */
		if(message.getType() == Msg.Types.RESPONSE_STATE){
			StateMsg stateMsg = (StateMsg) message;
			
			if(stateMsg.getName().equals("air_pump")){
				btntAirPump.setChecked(stateMsg.getValue() > 0);
			}
			else
			if(stateMsg.getName().equals("heater")){
				btntHeater.setChecked(stateMsg.getValue() > 0);
			}
			else
			if(stateMsg.getName().equals("filter")){
				btntFilter.setChecked(stateMsg.getValue() > 0);
			}
			else
			if(stateMsg.getName().equals("light")){
				btntLight.setChecked(stateMsg.getValue() > 45);
				btnSetColor.setEnabled(btntLight.isChecked());
			}
			
		}
		
		else
		
		/*
		 *  Process temperature response
		 */
		if(message.getContent().startsWith("Temp: ")){
			String[] parts = message.getContent().split(" ");
			if(parts.length >= 4){
				int IDX_OF_AIR_TEMP = 3;
				int IDX_OF_WATER_TEMP = 2;
				float air   = Float.valueOf(parts[IDX_OF_AIR_TEMP]);
				float water = Float.valueOf(parts[IDX_OF_WATER_TEMP]);
				showTempOnUI(air, water);
			}else{
				showErrorMessage("Can not find multile temperature value in temp response message: \"" + message.toString() + "\"");
			}
		}
		
		
		/*
		 *  Process humidity response
		 */
		if(message.getContent().startsWith("Humidity: ")){
			String[] parts = message.getContent().split(" ");
			if(parts.length > 1){
				int humidityValue = Integer.valueOf(parts[1]);
				showHumidityOnUI(humidityValue);
			}else{
				showErrorMessage("Can not find humidity value in response message: \"" + message.toString() + "\"");
			}
		}
		
	}
	
	
	protected void showHumidityOnUI(int humidity){
		tvHumidity.setText(humidity + "%");
	}

	
	protected void showTempOnUI(float air, float water){
		tvTempAir.setText(   String.format("%.1f", air)   + "°C" );
		tvTempWater.setText( String.format("%.2f", water) + "°C" );
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
		
		/*
		 *  Change connect button to refresh button
		 */
		funcitonOfConnectButton = FUNCTION_REFRESH;
		btnConnectAndRefresh.setImageResource(R.drawable.sync_s);
		
		/*
		 *  Get values from host
		 */
		sendRequestsForRefresh();
		
	}

	
	private void disconnect(){
		if(comm != null){
			comm.disconnect();
			try{
				//TODO
				//btnConnect.setText("Connect");
			}catch(Exception doesNotMatterWhenTryToDisconnectBecauseAppClosing){}
		}
		showConnectButton();
	}

	
	private void showConnectButton(){
		/*
		 *  Change refresh button to connect button
		 */
		funcitonOfConnectButton = FUNCTION_CONNECT;
		btnConnectAndRefresh.setImageResource(R.drawable.connect2_s);
	}
	
	
	private void sendSetStateRequest(String what, boolean newState){
		
		Msg request = new Msg(Commands.SET_STATE_OF + " " + what + " " + (newState?1:0), Msg.Types.REQUEST);
		if(comm != null && comm.isConnected()){
			comm.sendMessage(request);
		}else{
			showErrorMessage("Could not send SetStateOf... -request..");
			showConnectButton();
		}
		
	}
	
	
	public void sendRequestsForRefresh(){
		
		sendRequestToGetTemperature();
		getStatesOfPerfiherials();
		sendRequestToGetHumidity();
		 
	}
	
	
	private void getStatesOfPerfiherials(){
		
		String[] pheripherialListToGetStates = {"air_pump", "heater", "filter", "light"};
		
		for(int i=0; i< pheripherialListToGetStates.length; i++){
			if(sendRequestToGetStateOf(pheripherialListToGetStates[i]) == false){
				// do NOT try to send more requests to get states of other pheripherials, 
				// because requests could NOT be send, and user got an error message while first failed send attemption ended.
				return;
			}
		}
		
	}
	
	
	private boolean sendRequestToGetHumidity(){
		
		Msg requestToGetHumidity = new Msg(Commands.GET_ONLY_HUMIDITY, Msg.Types.REQUEST);
		if(comm != null && comm.isConnected()){
			comm.sendMessage(requestToGetHumidity);
			return true;
		}else{
			showErrorMessage("Could not send GetOnlyHumidity... -request..");
			showConnectButton();
			return false;
		}
		
	}
	
	
	private boolean sendRequestToGetStateOf(String what){
		
		Msg requestToGetState = new Msg(Commands.GET_STATE_OF + " " + what, Msg.Types.REQUEST);
		if(comm != null && comm.isConnected()){
			comm.sendMessage(requestToGetState);
			return true;
		}else{
			showErrorMessage("Could not send GetStateOf... -request..");
			showConnectButton();
			return false;
		}
		
	}
	
	
	/**
	 * Send request to server to get temperature values.
	 */
	private void sendRequestToGetTemperature() {
		if(comm != null){
			if(comm.isConnected()){
				comm.sendMessage(new Msg(Commands.GET_TEMP, Msg.Types.COMMAND));
			}else{
				showErrorMessage("Can not get temperature values: Not connected.");
				showConnectButton();
			}
		}
	}
	
	
	protected void showErrorMessage(String text){
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
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
	
	
	private int getColorFromSeekBars(){
		return Color.rgb(
			seekBars[RED].getProgress(), 
			seekBars[GREEN].getProgress(), 
			seekBars[BLUE].getProgress()
		);
	}
	
	private void showColorFromSeekBars(int color){
		seekBars[RED].setProgress(Color.red(color)); 
		seekBars[GREEN].setProgress(Color.green(color)); 
		seekBars[BLUE].setProgress(Color.blue(color));
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
	
	
	private void sendColor(int red, int green, int blue){
		if(comm != null && comm.isConnected()){
			RGBMessage m =
			new RGBMessage("setColor", 
							red, 
							green, 
							blue
			);
			
			if(comm.sendMessage(m)){
				sendRequestsForRefresh();
			}else{
				showErrorMessage("Could not send new color.");
			}
			
		}else{
			showErrorMessage("Can not send new color:\nNot connected");
			showConnectButton();
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
	protected void onPause() {
		
		myConfigManager.storeString("port", comm.getPort() + "");
		myConfigManager.storeString("host", comm.getHost());
		myConfigManager.storeBoolean("instant_connect", instantConnectEnabled);
		
		myConfigManager.storeInt("lastColor", selectedColor);
		
		disconnect();
		super.onPause();
	}
	
}
