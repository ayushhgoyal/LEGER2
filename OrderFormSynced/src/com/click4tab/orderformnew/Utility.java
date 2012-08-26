package com.click4tab.orderformnew;

import android.database.Cursor;

public class Utility {


 	/**
 	 * Gets the column value by taking the cursor and the column name and returning the value present
 	 * at the column index. 
 	 * 
 	 * @param cur
 	 * @param ColumnName
 	 * @return
 	 */
 	public static String GetColumnValue(Cursor cur, String ColumnName) {
		try {
			return cur.getString(cur.getColumnIndex(ColumnName));
		} catch (Exception ex) {
			return "";
		}
	}
	
}
