package com.click4tab.orderformnew;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Gets the selectedItem from ListFragment and produces the list by retrieving
 * data from server and displays it. Also, makes them clickable and defines
 * methods to place order.
 * 
 * @author Sony
 * 
 */
public class DetailFragment extends Fragment {

	int netOrderId;
	String[] itemList;
	static int isOrderPlaced;
	View empty;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Log is used to make an entry in LogCat with (tag, Message) to make
		// sure code is executing till here
		Log.e("Test", "hello");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflates the right pane
		View view = inflater.inflate(R.layout.details, container, false);
		return view;
	}

	/**
	 * It takes the parameter and passes it to further methods to make suitable
	 * query to database which ultimately populates the right pane from returned
	 * values (ItemList corresponding to selectedStore).
	 * 
	 * @param selectedStore
	 *            Item selected in left pane.
	 */
	public void setText(final String selectedStore) {
		final ListView listview = (ListView) getView()
				.findViewById(R.id.myList);

		// setting empty view for this ListView
		// listview.setEmptyView(R.id.tvGone);

		TestAdapter mDbHelper = new TestAdapter(getActivity());
		mDbHelper.createDatabase();
		mDbHelper.open();

		// Make an entry in NetOrderID - GENERATING NETORDER ID

		int salesManId = Login.salesManPermanent;

		// Get a netOrderID corresponding to selected store

		if (isOrderPlaced == 1) {

			isOrderPlaced = 0;
			netOrderId = mDbHelper
					.generateNetOrderId(selectedStore, salesManId) + 1;

			// Toast is used to create a notification on currect activity which
			// has
			// no connection to underlying elements
			Toast.makeText(getActivity(), "id is " + netOrderId,
					Toast.LENGTH_SHORT).show();
			// }
			// else {
			// //display a dialog to confirm
			// //set isOrderPlaced = 1;
			//
			// AlertDialog.Builder clickAlert2 = new
			// AlertDialog.Builder(getActivity());
			// clickAlert2.setMessage("You have not placed any order");
			// clickAlert2.setPositiveButton("Continue", new OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface dialog, int which) {
			// // TODO Auto-generated method stub
			// isOrderPlaced = 1;
			// }
			// });

			// }
			// Get the itemList from server corresponding to selectedStore.
			Cursor curItemList = mDbHelper.getItemList(selectedStore);
			itemList = new String[curItemList.getCount()];

			// Returned ItemList is saved in itemList[] array which will be
			// passed
			// to adapter.
			int i = 0;
			while (curItemList.moveToNext()) {
				String itemName = curItemList.getString(0);
				itemList[i] = itemName;
				i++;
			}

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1,
					itemList);
			listview.setAdapter(adapter);

			// Following method handles the clickEvents on Items present on
			// rightPane
			listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					Object o = listview.getAdapter().getItem(arg2);
					final String itemSelected = o.toString();

					// Creating an alertBox which handles placement of Orders
					final LinearLayout alertLayout = new LinearLayout(
							getActivity());
					alertLayout.setOrientation(LinearLayout.VERTICAL);
					final EditText eQuantity = new EditText(getActivity());
					eQuantity.setHint("Enter Quantity here");
					eQuantity.setInputType(InputType.TYPE_CLASS_NUMBER
							| InputType.TYPE_NUMBER_FLAG_DECIMAL
							| InputType.TYPE_NUMBER_FLAG_SIGNED);
					final EditText ePrice = new EditText(getActivity());
					ePrice.setInputType(InputType.TYPE_CLASS_NUMBER
							| InputType.TYPE_NUMBER_FLAG_DECIMAL
							| InputType.TYPE_NUMBER_FLAG_SIGNED);
					// ePrice.setHint("Rate per unit");

					// get unit for selected item
					final TestAdapter mDbHelper = new TestAdapter(getActivity());
					mDbHelper.createDatabase();
					mDbHelper.open();

					final int itemSelectedID = mDbHelper
							.getItemSelectedID(itemSelected);
					String unitForSelectedItem = mDbHelper
							.getUnitForItem(itemSelectedID);
					ePrice.setHint("Rate per " + unitForSelectedItem);

					// adding the above created views to alertBox..dynamically
					alertLayout.addView(eQuantity);
					alertLayout.addView(ePrice);

					AlertDialog.Builder clickAlert = new AlertDialog.Builder(
							getActivity());
					clickAlert
							.setTitle("Enter price and quantity")
							.setCancelable(false)
							.setPositiveButton("Ok", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									String qty = eQuantity.getText().toString();
									float quantity = Float.parseFloat(qty);
									String pr = ePrice.getText().toString();
									float price = Float.parseFloat(pr);

									// Following code is used to placeOrder and
									// insert OrderDetails
									// into local database

									// TestAdapter mDbHelper = new TestAdapter(
									// getActivity());
									// mDbHelper.createDatabase();
									// mDbHelper.open();

									// int itemSelectedID = mDbHelper
									// .getItemSelectedID(itemSelected);

									// A call to method which makes an entry to
									// database as the order
									// is placed.
									mDbHelper.placeOrder(netOrderId,
											itemSelectedID, quantity, price);
									Toast.makeText(getActivity(),
											"Order Placed", Toast.LENGTH_SHORT)
											.show();
									mDbHelper.close();

								}
							})
							.setNegativeButton("Cancel", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});

					clickAlert.setView(alertLayout);

					AlertDialog alert = clickAlert.create();
					alert.show();

				}

			});

		} else {

			// display a dialog to confirm
			// set isOrderPlaced = 1;

			AlertDialog.Builder clickAlert2 = new AlertDialog.Builder(
					getActivity());
			clickAlert2.setMessage("You have not placed any order");
			clickAlert2.setPositiveButton("Continue", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					isOrderPlaced = 1;
					setText(selectedStore);
				}
			});
			clickAlert2.create().show();

		}
	}

}
