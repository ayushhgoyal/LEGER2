package com.click4tab.orderformnew;

import java.nio.channels.AlreadyConnectedException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * This is the activity which is called on the starting, it sets the view to
 * main.xml which contains two fragments - List and Details, also contains a
 * method to handle "Menu" button.
 * 
 * 
 * @author ayush@click4tab.com
 * 
 * 
 */
public class MainActivity extends Activity {
	/** Called when the activity is first created. */
	DashActivity dash = new DashActivity();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// INSTRUCTIONS DIALOG
		AlertDialog.Builder instructionsAlert = new AlertDialog.Builder(this);
		instructionsAlert.setTitle("Instructions");
		instructionsAlert
				.setMessage("Select store from Left pane to start placing orders.  "
						+ "Click on items in right pane to enter quantity and price.  "
						+ "Click anywhere to cancel this dialog.");
		instructionsAlert.setCancelable(true);
		instructionsAlert.create().show();

		// get salesman's name
		// "user" in following message will be replaced by salesman's name
		AlertDialog.Builder welcomeAlert = new AlertDialog.Builder(this);
		String s = "";
		// TestAdapter t = new TestAdapter(this);
		// t.createDatabase();// create and open here
		// if ()

		welcomeAlert
				.setMessage("Greetings user! Select from below or click anywhere to cancel this dialog. ");
		welcomeAlert.setNegativeButton("Logout", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dash.logoutUser();
			}
		});

		welcomeAlert.setPositiveButton("Download", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dash.performReadOperation();
			}
		});

		welcomeAlert.setNeutralButton("Upload", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				dash.performWriteOperation();
			}
		});

		welcomeAlert.setCancelable(true);
		welcomeAlert.create().show();

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * This method is called when "Menu" button is pressed and produces the list
	 * of selectable items. Calls two methods which handle the writing/reading
	 * to server. To write data on server - writeUnwrittenNetOrder() To get
	 * updated data from server - getDataFromServer()
	 */

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.item1:
//			dash.performWriteOperation();
			break;

		case R.id.item2:
			dash.performReadOperation();
			break;

		case R.id.item3:
			dash.logoutUser();

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

}
