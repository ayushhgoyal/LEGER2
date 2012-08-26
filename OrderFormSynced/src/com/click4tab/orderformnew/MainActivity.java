package com.click4tab.orderformnew;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * This is the activity which is called on the starting, it sets the view to
 * main.xml which contains two fragments - List and Details, also contains a method to 
 * handle "Menu" button. 
 * 
 * 
 * @author ayush@click4tab.com
 * 
 * 
 */
public class MainActivity extends Activity {
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * 
	 * This method is called when "Menu" button is pressed and produces the list of selectable items.
	 * Calls two methods which handle the writing/reading to server. 
	 * To write data on server - writeUnwrittenNetOrder() 
	 * To get updated data from server - getDataFromServer()
	 * 
	 * 
	 * 
	 */
	
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.item1:
			Toast.makeText(MainActivity.this, "writing in progress",
					Toast.LENGTH_SHORT).show();
			// write data on server
			TestAdapter mDbHelper = new TestAdapter(this);
			mDbHelper.readOrWrite = 1;
			mDbHelper.createDatabase();
			mDbHelper.open();

			try {
				mDbHelper.writeUnwrittenData();
			} catch (Exception e) {
				//e.printStackTrace();
				Log.e("err", e.toString());
			} finally {
				mDbHelper.close();
			}


			break;

		case R.id.item2:
			Toast.makeText(this, "Reading data", Toast.LENGTH_LONG)
					.show();
			TestAdapter mDbHelper2 = new TestAdapter(this);
			mDbHelper2.readOrWrite = 0;
			mDbHelper2.createDatabase();
			mDbHelper2.open();

			try {
				mDbHelper2.getDataFromServer();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				Log.e("err", e.toString());

			}
			mDbHelper2.close();

			break;
			
		case R.id.item3:
			Login.userRegistered = 0;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
}
