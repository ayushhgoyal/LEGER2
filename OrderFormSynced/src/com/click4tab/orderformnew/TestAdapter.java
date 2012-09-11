package com.click4tab.orderformnew;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Handles all the database related operations including writing on local
 * database along with synchronizing it with database on server
 * 
 * 
 * @author ayush@click4tab.com ; sgar431@gmail.com
 * 
 */
public class TestAdapter {

	protected static final String TAG = "DataAdapter";

	private final Context mContext;
	private SQLiteDatabase mDb;
	private DatabaseHandler mDbHelper;
	public static String NETORDER_SQL;

	HttpClient client;
	static JSONObject json, jsonQuery, json_user;
	static int flagValue;
	static List<NameValuePair> params;
	static int readOrWrite;
	static List<String> toBeWrittenOnServer;
	static StringBuffer toBeWrittenOnLocalDb;
	final static String URL = "http://seekonline.in/develop/Appsyn/order.php";

	// final static String URL = "http://192.168.1.8/Appsyn/order.php";
	public TestAdapter(Context context) {
		this.mContext = context;
		mDbHelper = new DatabaseHandler(mContext);
	}

	public TestAdapter createDatabase() throws SQLException {
		try {
			mDbHelper.createDataBase();
		} catch (IOException mIOException) {
			Log.e(TAG, mIOException.toString() + "  UnableToCreateDatabase");
			throw new Error("UnableToCreateDatabase");
		}
		return this;
	}

	public TestAdapter open() throws SQLException {
		try {
			mDbHelper.openDataBase();
			mDbHelper.close();
			mDb = mDbHelper.getReadableDatabase();
		} catch (SQLException mSQLException) {
			Log.e(TAG, "open >>" + mSQLException.toString());
			throw mSQLException;
		}
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}

	}

	/**
	 * Executes the sql (a SELECT operation) on local database to get storeList
	 * 
	 * @return a cursor ("mCur" here) which contains the list of stores
	 */
	public Cursor getStoreList() {
		String sql = "SELECT DISTINCT Name from Stores";
		Cursor mCur = mDb.rawQuery(sql, null);

		return mCur;
	}

	/**
	 * Makes a query to database (SELECT operation) and gets ItemList which
	 * corresponds to selected item in left pane. Takes the parameter
	 * selectedStore. RawQuery method is used when a query being executed is
	 * supposed to return something. Press F3 to jump to method definition.
	 * 
	 * @param selectedStore
	 * @return returns a cursor to returned rows from database due to executed
	 *         query.
	 */
	public Cursor getItemList(String selectedStore) {
		String sql = "SELECT ItemName from ItemData WHERE ItemID in ( SELECT ItemID from StoreItem WHERE StoreId IN (SELECT ID from Stores WHERE Name='"
				+ selectedStore + "'))";
		Cursor mCur = mDb.rawQuery(sql, null);
		return mCur;
	}

	/**
	 * This method generates the NetOrderID by taking the parameters and makes
	 * an INSERT query to local database. After that, another query is executed
	 * on local database which gives us the auto-generated netOrderID (
	 * retrieved from NetOrder table). Press F3 to jump to method definition.
	 * 
	 * @param selectedStore
	 *            It is set when user clicks on an item in left pane.
	 * @param SalesManID
	 *            It is set when the used logs in for the first time.
	 * @return netOrderID which is generated lately in integer format.
	 */
	public int generateNetOrderId(String selectedStore, int SalesManID) {
		NETORDER_SQL = "INSERT INTO NetOrderID ( StoreID, SalesManID) Select ID, "
				+ SalesManID
				+ " from Stores WHERE Name='"
				+ selectedStore
				+ "'";
		//
		// // ExecSQL is used to make a such queries on
		// // database which are not supposed to return anything.
		//
		// // ----------------
		// mDb.execSQL(sql);
		// Log.e("my", "local db updated"); // means netOrderID is generated and
		// stored in database.
		// Now we need to retrieve the latest auto-generated netOrderID so that
		// it can be passed on to
		// methods which places order.

		String sql2 = "Select MAX(NetOrderID) FROM NetOrderID";
		Cursor mCur = mDb.rawQuery(sql2, null);

		// Initially, Cursor points to the -1 position of returned records, so
		// we need to move it to
		// first position
		mCur.moveToFirst();
		// orderCount =0;
		int netOrderId = mCur.getInt(0);
		Log.e("my", "Netorder id generated");
		return netOrderId;
	}

	/**
	 * 
	 * This method here is used to place order by generating an INSERT query and
	 * executing it on local database. Parameters it takes are mentioned below.
	 * 
	 * @param netOrderId
	 * @param itemSelectedID
	 * @param quantity
	 * @param price
	 */
	public void placeOrder(int netOrderId, int itemSelectedID, float quantity,
			float price) {
		DetailFragment.isOrderPlaced = 1;

		// mDb.execSQL(NET_SQL);

		String sql = "Insert INTO OrderDetails (NetOrderId, ItemID, Quantity, Price) Values ( "
				+ netOrderId
				+ ","
				+ itemSelectedID
				+ ","
				+ quantity
				+ ","
				+ price + ")";

		mDb.execSQL(sql);

		mDb.execSQL(NETORDER_SQL);

	}

	/**
	 * This method writes the data present on local database over Server
	 * database but doesn't process records which were already written on
	 * server. This check is performed with the help of flag variable. Value of
	 * flag determines the presence of records on server (0 for unsynced and 1
	 * for synced). This method deals with the data of tables- NetOrder and
	 * OrderDetails.
	 * 
	 * It makes a SELECT operation on local database and gets the records which
	 * have flag set as 0. Returned records are in the form of cursor, that
	 * cursor is then traversed and an INSERT query is generated with unsynced
	 * values which is appended into stringBuffer. This resulting stringBuffer
	 * is converted into string and is added to "params" which is sent over
	 * network. Queries appended in buffer are seperated by "#". Press F3 to
	 * jump on method definition.
	 * 
	 * @throws Exception
	 * @see setFlag
	 * @author ayush@click4tab.com, sgarg431@gmail.com
	 */
	public void writeUnwrittenData() throws Exception {

		String sql = "Select * from NetOrderID where (flag='0')";

		if (mDb == null) {
			Log.e("my", "mdb is null");
		} else {
			Log.e("my", "mdb is not null");
		}
		// ------cheching sql
		Cursor c = mDb.rawQuery(sql, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String check = c.getString(0);

			Log.e("SQLCHECK", check);

		}

		// -----------

		Cursor mCur = mDb.rawQuery(sql, null);
		if (mCur != null) {
			Log.e("my", "INSIDE");
			// Toast.makeText(mContext, "query executed",
			// Toast.LENGTH_SHORT).show();
		}

		params = new ArrayList<NameValuePair>();
		// Toast.makeText(mContext, "param added", Toast.LENGTH_SHORT).show();

		StringBuffer strbuf = new StringBuffer();
		params.add(new BasicNameValuePair("tag", "add_NetOrderID"));
		if (mCur != null) {
			for (mCur.moveToFirst(); !mCur.isAfterLast(); mCur.moveToNext()) {

				// String sql3 =
				// "INSERT into NetOrderID (StoreID, Date, SalesManID, NetOrderID) VALUES ("
				// + mCur.getString(0)
				// + ", "
				// + mCur.getString(1)
				// + ", "
				// + mCur.getString(2) + ", " + mCur.getString(3) + ");";

				String net = mCur.getString(0) + ":::" + mCur.getString(1)
						+ ":::" + mCur.getString(2) + ":::" + mCur.getString(3)
						+ "#";

				if ((mCur.isLast())) {
					strbuf.append(net + "#");
				} else { // i.e. mCur is not at last
					strbuf.append(net);
				}
				Log.e("net", "net appended" + net);

			}
		}
		// Toast.makeText(mContext, "executing 2nd", Toast.LENGTH_SHORT).show();

		String sql4 = "Select * from OrderDetails where (flag='0')";
		if (mDb == null) {
			Log.e("my", "mdb is null");
		} else {
			Log.e("my", "mdb is not null");
		}
		Cursor mCur2 = mDb.rawQuery(sql4, null);
		if (mCur2 != null) {
			Log.e("my", "INSIDE");
		}

		Log.e("my", "sql executed");
		if (mCur2 != null) {
			// strbuf.append("#");
			for (mCur2.moveToFirst(); !mCur2.isAfterLast(); mCur2.moveToNext()) {

				String sql3 = "INSERT into OrderDetails (NetOrderId, ItemID, Quantity, Price, OrderID) VALUES ("
						+ mCur2.getString(0)
						+ ", "
						+ mCur2.getString(1)
						+ ", "
						+ mCur2.getString(2)
						+ ", "
						+ mCur2.getString(3)
						+ ", "
						+ mCur2.getString(4) + ");";

				if ((mCur2.isLast())) {
					strbuf.append(sql3);
				} else {
					strbuf.append(sql3 + "#");
				}
				Log.e("sql", sql3);

				Log.e("sql", "query appended");

			}
		}
		// Toast.makeText(mContext, "both executed and adding params",
		// Toast.LENGTH_SHORT).show();

		params.add(new BasicNameValuePair("query", strbuf.toString()));
		// Toast.makeText(mContext, "added", Toast.LENGTH_SHORT).show();

		// finally params are added and execute() method is called which starts
		// the async task.

		Log.e("param", "param added");
		Log.e("myBuffer", strbuf.toString());
		// Toast.makeText(mContext, "calling execute",
		// Toast.LENGTH_SHORT).show();

		new Read().execute("");

	}

	/**
	 * This method makes a request to server to read data from ServerDatabase
	 * and copy it in local database.
	 * 
	 */
	public void getDataFromServer() {
		params = new ArrayList<NameValuePair>();

		params.add(new BasicNameValuePair("tag", "read_data"));
		Log.e("TA", "about to call read()");

		new Read().execute("");

	}

	/**
	 * This method actually gets data from serverDatabase in the form of INSERT
	 * queries which will be executed as-they-are on local database. Queries
	 * come separated by "#" and they are disintegrated locally via
	 * StringTokenizer method (Lifesaver!).
	 * 
	 * Before writing data on local database, previous entries on database are
	 * deleted by calling the method deletePreviousEntries().
	 * 
	 * 
	 * @param queries
	 */
	public void getData(String queries) {
		Log.e("get", "inside get data and about to delete entries");
		deletePreviousEntries();
		Log.e("get", "entries deleted");
		if (queries == null) {
			Log.e("here", "queries is null");
		}
		StringTokenizer str = new StringTokenizer(queries);
		while (str.hasMoreElements()) {
			Log.e("get", "writing data");

			mDb.execSQL(str.nextToken("#"));

			Log.e("get", "data written");

		}
		Toast.makeText(mContext, "Reading complete", Toast.LENGTH_SHORT).show();
		// After the reading has completed, calling write operation
		Log.e("Write", " writing started");
		performWriteOperation();

		AlertDialog.Builder clickAlert = new AlertDialog.Builder(mContext);
		clickAlert
				.setMessage("Database has been updated, please restart application to load new data. ");
		clickAlert.setPositiveButton("Restart", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// Intent i = new Intent("android.intent.action.MAIN");
				Intent mainIntent = new Intent("mainActivity");
				mContext.startActivity(mainIntent);

			}
		});
		// clickAlert.create().show();

	}

	public void performWriteOperation() {
		DashActivity dashObj = new DashActivity();
		if (dashObj.isOnline()) {
			// Toast.makeText(getApplicationContext(), "writing in progress",
			// Toast.LENGTH_SHORT).show();
			// write data on server
			// TestAdapter mDbHelper = new TestAdapter(this);
			readOrWrite = 1;
			Log.e("TA", "readwrite set = " + readOrWrite);
			createDatabase();
			open();
			try {

				// if alreadySynced()
				if (!alreadySynced())

				{
					Log.e("sync", "not synced");

					writeUnwrittenData();
				} else {
					Log.e("sync", "already synced");
					// Toast.makeText(this, "Already synced!",
					// Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				// e.printStackTrace();
				Log.e("err", e.toString());
			} finally {
				mDbHelper.close();
			}

		} else {
			// Toast.makeText(getApplicationContext(), "No internet access",
			// Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * This method clears the database and welcomes new Records sent by server.
	 * It deletes all records from Category, ItemData, Salesman, StoreItem,
	 * Stores, TerritoryData
	 */
	private void deletePreviousEntries() {
		// Dropping all tables ( Category, ItemData, Salesman, StoreItem,
		// Stores, TerritoryData )
		Log.e("get", "inside delete");

		mDb.execSQL("DELETE FROM Category");
		Log.e("get", "cat deleted");

		mDb.execSQL("DELETE FROM ItemData");
		Log.e("get", "cat deleted");

		mDb.execSQL("DELETE FROM Salesman");
		Log.e("get", "cat deleted");

		mDb.execSQL("DELETE FROM StoreItem");
		Log.e("get", "cat deleted");

		mDb.execSQL("DELETE FROM Stores");
		Log.e("get", "cat deleted");

		mDb.execSQL("DELETE FROM TerritoryData");
		Log.e("get", "cat deleted");

	}

	// FLAG CODE HERE
	// -------------------------------------------------------------------

	/**
	 * This method sets the flag of records which are successfully written on
	 * the server. It examines the flagvalue variable and if found "success"
	 * then it runs an UPDATE query on NetOrder and OrderDetails
	 * 
	 * @author Ayush@click4tab.com
	 */
	public void setflag() {
		Log.e("my", "in setflag method");

		// TODO Auto-generated method stub
		if (flagValue == 1) {
			Log.e("my", "in setflag");
			String sqlToSetFlagNetOrder = "Update NetOrderID SET flag = '1' where flag ='0'";

			mDb.execSQL(sqlToSetFlagNetOrder);

			String sqlToSetFlagOrderDetails = "Update OrderDetails SET flag = '1' where flag='0'";
			mDb.execSQL(sqlToSetFlagOrderDetails);
			Toast.makeText(mContext, "Writing complete", Toast.LENGTH_SHORT)
					.show();

			Log.e("my", "flag column updated");

		}
	}

	/**
	 * It takes the "itemSelected" and processes a SELECT query on database
	 * retrieving the "id" of "itemSlected".
	 * 
	 * @param itemSelected
	 * @return id of selected item in Integer format.
	 * 
	 * @author Ayush@click4tab.com
	 * 
	 */
	public int getItemSelectedID(String itemSelected) {

		String sql = "Select ItemID from ItemData WHERE ItemName = '"
				+ itemSelected + "'";
		Cursor mCur = mDb.rawQuery(sql, null);
		mCur.moveToFirst();
		return Integer.parseInt(mCur.getString(0));

	}

	/**
	 * The is the method which interacts with server. It sets up the connection
	 * with specified url, and sends "params" which gets values from different
	 * methods, over the network.
	 * 
	 * 
	 * @return returns a JSON object as a reponse from server which when
	 *         examined can tell us if the network operation was successful or
	 *         not.
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws JSONException
	 * 
	 * @author Ayush@click4tab.com, and a special thanks to sgarg431@gmail.com
	 *         for providing syncing support from server.
	 */
	public JSONObject sendJSONtoUrl() throws ClientProtocolException,
			IOException, JSONException {
		Log.e("my", "inside sendJson");
		client = new DefaultHttpClient();

		// DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URL);
		Log.e("my", "after post");

		try {
			Log.e("my", "before setEntity");

			httpPost.setEntity(new UrlEncodedFormEntity(params));
			Log.e("my", "after setEntity");

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("my", "before execute");

		HttpResponse r = client.execute(httpPost);
		Log.e("my", "after execute");

		int status = r.getStatusLine().getStatusCode();
		if (status == 200) {
			Log.e("my", "status verified");
			HttpEntity e = r.getEntity();
			String data = EntityUtils.toString(e);
			Log.e("Bang", data);
			JSONArray datastream1 = new JSONArray(data);

			JSONObject message = datastream1.getJSONObject(0);

			return message;

		} else {
			Log.e("my", "status not verified");
			return null;

		}

	}

	/**
	 * This inner class is used to start Asynchronous task which deals with
	 * network and writes/reads data to/from server. A method execute() is
	 * required to be called to start this service. When it is called, first
	 * doInBackground() is called and upon execution, onPostExecute() is called.
	 * 
	 * @author Ayush@click4tab.com, sgarg431@gmail.com
	 * 
	 */
	class Read extends AsyncTask<String, Integer, String> {

		/*
		 * (non-Javadoc)
		 * 
		 * sendJSONtoUrl() is called which returns a JSONObject called json.
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... params) {
			try {
				Log.e("my", "inside async and calling sendJson");
				// Toast.makeText(mContext, "calling send json",
				// Toast.LENGTH_SHORT).show();

				json = sendJSONtoUrl();

				// Toast.makeText(mContext, "json received",
				// Toast.LENGTH_SHORT).show();

				Log.e("my", "inside json called and returned");
				Log.e("TA", "readwrite = " + readOrWrite);
				if (readOrWrite == 1) {

					return json.getString("success");

				}
				// ----------
				else if (readOrWrite == 2) {
					return json.getString("success");
				}

				else {
					Log.e("TA", "creating string tokenizer");
					toBeWrittenOnLocalDb = new StringBuffer();

					for (int i = 1; i <= 9; i++) {

						jsonQuery = json.getJSONObject("data" + i);

						int n = 1;
						while (n <= jsonQuery.length()) {
							toBeWrittenOnLocalDb.append(jsonQuery
									.getString("query" + n) + "#");
							Log.e("my", jsonQuery.getString("query" + n));
							n++;
						}

					}

					return toBeWrittenOnLocalDb.toString();
				}
			}

			catch (JSONException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * If the server response is positive, the flagvalue is set to 1.
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			Log.e("result", "yo");
			// Toast.makeText(mContext, "yo " + result,
			// Toast.LENGTH_SHORT).show();

			TestAdapter mDbHelper = new TestAdapter(mContext);

			mDbHelper.createDatabase();
			mDbHelper.open();
			if (readOrWrite == 1) {
				if (result == null) {
					Log.e("my", "result is null");
				}
				if (result != null) {
					Log.e("my", "result is  not null");
					if (Integer.parseInt(result) == 1) {
						flagValue = 1;
						Log.e("my", "flag value set");
						mDbHelper.setflag();

					}

				} else if (result == null) {
					Log.e("here", "result is null");
				}
			}
			// --------
			if (readOrWrite == 2) {
				if (result == null) {
					Log.e("my", "result is null");
				}
				if (result != null) {

					Log.e("my", "result is  not null and result is " + result);
					if (Integer.parseInt(result) == 10) {

						Log.e("my", "login success");
						Login.userRegistered = 1;

						Intent intent = new Intent("dashActivity");
						mContext.startActivity(intent);

					}

					else {
						Toast.makeText(mContext, "Gadbad!", Toast.LENGTH_SHORT)
								.show();
					}
				}

				else {
					// Toast.makeText(mContext, "Try Again",
					// Toast.LENGTH_SHORT).show();
					Log.e("c", "in else part");
				}

			}

			// -------

			// changed following else to elseif else if( readOrWrite == 0)
			else if (readOrWrite == 0) {
				Log.e("TA", "read  = 0 , calling getData");

				mDbHelper.getData(result);
			}
			mDbHelper.close();

		}

	}

	public boolean alreadySynced() {
		// TODO Auto-generated method stub
		String sql1 = "SELECT * FROM OrderDetails WHERE flag=0";
		Cursor cur1 = mDb.rawQuery(sql1, null);
		String sql2 = "SELECT * FROM NetOrderID WHERE flag=0";
		Cursor cur2 = mDb.rawQuery(sql2, null);
		if (cur1.getCount() == 0 && cur2.getCount() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public String getUnitForItem(int itemSelectedID) {
		// TODO Auto-generated method stub

		String sql = " SELECT Unit_Name from ItemData as i INNER JOIN unit as u on i.Unit_id = u.Unit_Id WHERE ItemID = "
				+ itemSelectedID;
		Cursor cur = mDb.rawQuery(sql, null);

		cur.moveToFirst();
		return cur.getString(0);
	}

	public String getNameOfSalesman(int salesManPermanent) {
		// TODO Auto-generated method stub
		String sql = "SELECT Name FROM Salesman WHERE ID = "
				+ salesManPermanent;
		Cursor cur = mDb.rawQuery(sql, null);
		if (cur.getCount() == 0) {
			return "";
		} else {
			cur.moveToFirst();
			return cur.getString(0);
		}

	}

}
