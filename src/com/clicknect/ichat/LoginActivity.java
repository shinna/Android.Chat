package com.clicknect.ichat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.clicknect.android.ichat.R;
import com.clicknect.ichat.helper.UserPreferences;

public class LoginActivity extends Activity {
	
	private Context context;
	private EditText usernameEditText, passwordEditText;
	private Button submitButton;	
	private String username, password;
	private UserPreferences userPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_login);
		
		// Context
		context = this;
		
		// User Preferences
		userPreferences = new UserPreferences(context);
		if ( userPreferences.loadCurrentUsername() != null && userPreferences.loadCurrentPassword() != null ) {
			username = userPreferences.loadCurrentUsername();
			password = userPreferences.loadCurrentPassword();
			
			sendLoginInformation();			
		}
		
		// View matching
		usernameEditText = (EditText) findViewById(R.id.login_username);
		passwordEditText = (EditText) findViewById(R.id.login_password);
		submitButton = (Button) findViewById(R.id.login_submit);
		
		// Event
		submitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				username = usernameEditText.getText().toString();
				password = passwordEditText.getText().toString();
				
				if ( username.trim().length() < 1 ) {
					Toast.makeText(context, "Your username is too short.", Toast.LENGTH_SHORT).show();
				} else if ( password.trim().length() < 1 ) {
					Toast.makeText(context, "Your password is too short.", Toast.LENGTH_SHORT).show();
				} else {
					sendLoginInformation();
				}
			}
		});
	}
	
	public void sendLoginInformation() {
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		intent.putExtra("username", username);
		intent.putExtra("password", password);
		startActivity(intent);
		LoginActivity.this.finish();
	}

}
