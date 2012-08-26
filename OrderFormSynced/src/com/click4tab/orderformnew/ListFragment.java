package com.click4tab.orderformnew;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This class produces the list on left pane of screen.
 * 
 * @author ayush@click4tab.com
 * 
 */

public class ListFragment extends android.app.ListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Create and open database

		TestAdapter mDbHelper = new TestAdapter(getActivity());
		mDbHelper.createDatabase();
		mDbHelper.open();

		// Get a cursor containing storelist

		Cursor curStoreList = mDbHelper.getStoreList();
		String[] StoreList = new String[curStoreList.getCount()];
		int i = 0;

		// Store returned items in StoreList[]
		while (curStoreList.moveToNext()) {
			String storeName = curStoreList.getString(0);
			StoreList[i] = storeName;
			i++;
		}

		// Create an adapter with list of stores and populate the list with
		// values
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, StoreList);
		setListAdapter(adapter);
		mDbHelper.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Handles the event when an item is clicked on left pane, performs action
	 * based on the selection in left pane
	 *  
	 * @see android.app.ListFragment#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String selectedStore = (String) getListAdapter().getItem(position);
		DetailFragment fragment = (DetailFragment) getFragmentManager()
				.findFragmentById(R.id.detailFragment);
		if (fragment != null && fragment.isInLayout()) {

			//l.setBackgroundColor(getResources().getColor(R.color.BLUE));
			// passes selectedStore to detail fragment  
			fragment.setText(selectedStore);

			// getItemList(selectedStore);

		} else {
			Intent intent = new Intent(getActivity().getApplicationContext(),
					DetailActivity.class);
			intent.putExtra("value", selectedStore);
			startActivity(intent);

		}

	}

}
