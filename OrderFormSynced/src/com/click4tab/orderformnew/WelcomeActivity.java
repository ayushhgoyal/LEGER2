package com.click4tab.orderformnew;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends Activity {
	
	private Handler mHandler = new Handler();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		mHandler.postDelayed(new Runnable() {
            public void run() {
                doStuff();
            }
        }, 3000);
    }

	protected void doStuff() {
		// TODO Auto-generated method stub
		if (Login.userRegistered == 1){
			//
			Intent int1 = new Intent("mainActivity");
			startActivity(int1);
		
	}
		else {
			Intent int2 = new Intent("loginActivity");
			startActivity(int2);
		}
		
		
		
		
	}
}
