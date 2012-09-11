package com.click4tab.orderformnew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DashActivity extends Activity implements OnClickListener {
	Button continueButton, syncButton, logoutButton;
	TextView welcomeText;
	Context dashContext;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.welcome);
		initElements();

		TestAdapter mDbHelper = new TestAdapter(this);
		mDbHelper.createDatabase();
		mDbHelper.open();

		String nameOfSalesman = mDbHelper
				.getNameOfSalesman(Login.salesManPermanent);
		welcomeText.setText("Welcome " + nameOfSalesman + "!");

		mDbHelper.close();
	}

	private void initElements() {
		// TODO Auto-generated method stub
		continueButton = (Button) findViewById(R.id.button4);
		continueButton.setOnClickListener(this);
		welcomeText = (TextView) findViewById(R.id.textView1);
		syncButton = (Button) findViewById(R.id.button1);
		syncButton.setOnClickListener(this);
		dashContext = this;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == continueButton) {
			// start MainActivity

			Intent intent = new Intent("mainActivity");
			startActivity(intent);
		}
		if (v == syncButton) {
			// read/write online data
			performReadOperation();
			//performWriteOperation();

		}
	}

	public void performReadOperation() {
		Toast.makeText(getApplicationContext(), "Reading data",
				Toast.LENGTH_LONG).show();
		TestAdapter mDbHelper2 = new TestAdapter(getApplicationContext());
		Log.e("dash", "read called in dash");
		mDbHelper2.readOrWrite = 0;
		mDbHelper2.createDatabase();
		mDbHelper2.open();

		try {
			Log.e("dash", "getting data - read called in dash");

			mDbHelper2.getDataFromServer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			Log.e("err", e.toString());

		}
		mDbHelper2.close();

	}

//	public void performWriteOperation() {
//
//		if (isOnline()) {
////			Toast.makeText(getApplicationContext(), "writing in progress",
////					Toast.LENGTH_SHORT).show();
//			// write data on server
//			TestAdapter mDbHelper = new TestAdapter(dashContext);
//			mDbHelper.readOrWrite = 1;
//			mDbHelper.createDatabase();
//			mDbHelper.open();
//			try {
//
//				// if alreadySynced()
//				if (!mDbHelper.alreadySynced())
//
//				{
//					mDbHelper.writeUnwrittenData();
//				} else {
//					Toast.makeText(getApplicationContext(), "Already synced!",
//							Toast.LENGTH_SHORT).show();
//				}
//			} catch (Exception e) {
//				// e.printStackTrace();
//				Log.e("err", e.toString());
//			} finally {
//				mDbHelper.close();
//			}
//
//		} else {
//			Toast.makeText(getApplicationContext(), "No internet access",
//					Toast.LENGTH_SHORT).show();
//		}
//	}

	public void logoutUser() {
		// TODO Auto-generated method stub
		Login.userRegistered = 0;
		finish();
	}

	public Boolean isOnline() {

//		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo ni = cm.getActiveNetworkInfo();
//		if (ni != null && ni.isConnected()) {
//			return true;
//		} else {
//			Log.e("net", "No net access");
//			return false;
//		}
		return true;	
	}

}
