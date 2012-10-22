package com.clicknect.ichat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.clicknect.android.ichat.R;

public class SplashActivity extends Activity {
	
	private Thread thread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_splash);
		
		thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					startActivity(new Intent(SplashActivity.this, MainActivity.class));
					SplashActivity.this.finish();
				}
				super.run();
			}
		};
		thread.start();
	}

}
