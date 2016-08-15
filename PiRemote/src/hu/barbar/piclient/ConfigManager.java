package hu.barbar.piclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
	
	public static final String KEY_HOST = "hostname";
	
	Activity myActivity = null;
	
	
	public ConfigManager(Activity activity) {
		myActivity = activity;
	}

	public String loadString(String key, String defaultValue) {
		SharedPreferences sharedPref = myActivity.getPreferences(Context.MODE_PRIVATE);
		return sharedPref.getString(key, defaultValue);
	}
	
	public String loadString(String key) {
		return this.loadString(key, "");
	}

	public void storeString(String key, String value) {
		
		SharedPreferences sharedPref = myActivity.getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
		
	}
	
	
	
}
