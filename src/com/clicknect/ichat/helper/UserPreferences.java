package com.clicknect.ichat.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

	private static String PREF_NAME = "iChat.userinfo";
	private static String PREF_USERNAME = "iChat.username";
	private static String PREF_PASSWORD = "iChat.password";
	
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	
	public UserPreferences(Context context) {
		sharedPreferences = context.getSharedPreferences(PREF_NAME, 0);
	}
	
	public void saveCurrentUsername(String username) {
		editor = sharedPreferences.edit();
		editor.putString(PREF_USERNAME, username);
		editor.commit();
	}
	
	public void clearCurrentUsername() {
		editor = sharedPreferences.edit();
		editor.putString(PREF_USERNAME, null);
		editor.commit();		
	}
	
	public String loadCurrentUsername() {
		return sharedPreferences.getString(PREF_USERNAME, null);
	}
	
	public void saveCurrentPassword(String password) {
		editor = sharedPreferences.edit();
		editor.putString(PREF_PASSWORD, password);
		editor.commit();
	}
	
	public void clearCurrentPassword() {
		editor = sharedPreferences.edit();
		editor.putString(PREF_PASSWORD, null);
		editor.commit();		
	}
	
	public String loadCurrentPassword() {
		return sharedPreferences.getString(PREF_PASSWORD, null);
	}	
	
}
