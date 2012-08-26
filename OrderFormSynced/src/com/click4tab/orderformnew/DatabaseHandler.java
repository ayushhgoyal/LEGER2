package com.click4tab.orderformnew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHandler extends SQLiteOpenHelper {

	private static String TAG = "DataBaseHelper";
	private static final int DATABASE_VERSION = 1;
	private static final String DB_NAME = "NewOrderDB";
	private static String DB_PATH = "";
	private final Context mContext;
	private SQLiteDatabase mDatabase;

	public DatabaseHandler(Context context) {
		super(context, DB_NAME, null, DATABASE_VERSION);
		DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";

		mContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */

	public void createDataBase() throws IOException {

		// If database not exists copy it from the assets

		boolean mDataBaseExist = checkDataBase();
		if (!mDataBaseExist) {
			this.getWritableDatabase();
			this.close();
			try {
				// Copy the database from assests
				copyDataBase();
				Log.e(TAG, "createDatabase database created");
			} catch (IOException mIOException) {
				throw new Error("ErrorCopyingDataBase");
			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		File dbFile = new File(DB_PATH + DB_NAME);
		// Log.v("dbFile", dbFile + "   "+ dbFile.exists());
		return dbFile.exists();
	}

	/**
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 * */
	// Copy the database from assets
	private void copyDataBase() throws IOException {
		InputStream mInput = mContext.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream mOutput = new FileOutputStream(outFileName);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer)) > 0) {
			mOutput.write(mBuffer, 0, mLength);
		}
		mOutput.flush();
		mOutput.close();
		mInput.close();
	}

	// Open the database, so we can query it
	public boolean openDataBase() throws SQLException {
		String mPath = DB_PATH + DB_NAME;
		// Log.v("mPath", mPath);
		mDatabase = SQLiteDatabase.openDatabase(mPath, null,
				SQLiteDatabase.CREATE_IF_NECESSARY);
		// mDataBase = SQLiteDatabase.openDatabase(mPath, null,
		// SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		return mDatabase != null;
	}

	@Override
	public synchronized void close() {
		if (mDatabase != null)
			mDatabase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}