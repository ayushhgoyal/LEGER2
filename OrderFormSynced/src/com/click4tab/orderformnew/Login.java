package com.click4tab.orderformnew;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Login extends Activity {
	
	Button loginButton;
	EditText id, password;
	String sID, sPassword;
	TestAdapter obj;
	Context loginContext;
	public static int loginSuccessful;
	public static int salesManPermanent;
	public static int  userRegistered;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		//Initialising variables 
		
		loginButton = (Button)findViewById(R.id.button1);
		id = (EditText)findViewById(R.id.editText1);
		id.setInputType(InputType.TYPE_CLASS_NUMBER);
		password = (EditText)findViewById(R.id.editText2);
		password.setInputType(InputType.TYPE_CLASS_NUMBER);
		loginContext = this;
		obj = new TestAdapter(loginContext);
		
//		obj.readOrWrite = 2;
//		sID = id.getText().toString();
//		sPassword = password.getText().toString();
//		
//		StringBuffer buf = new StringBuffer();
//		buf.append(sID + ":::" + sPassword);
//		
//		
//		TestAdapter.params = new ArrayList<NameValuePair>();
//		TestAdapter.params.add(new BasicNameValuePair("tag", "login"));
//		Log.e("login", "Client is sending " + buf.toString());
//		TestAdapter.params.add( new BasicNameValuePair("query", buf.toString()));
		
		loginButton.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				
				obj.readOrWrite = 2;
				sID = id.getText().toString();
				sPassword = password.getText().toString();
				
				//set SalesmanId permanently 
				salesManPermanent = Integer.parseInt(sID);
				
				
				StringBuffer buf = new StringBuffer();
				buf.append(sID + ":::" + sPassword);
				
				
				TestAdapter.params = new ArrayList<NameValuePair>();
				TestAdapter.params.add(new BasicNameValuePair("tag", "login"));
				Log.e("login", "Client is sending " + buf.toString());
				TestAdapter.params.add( new BasicNameValuePair("query", buf.toString()));

				
				//send login information to authenticate user

				obj.new Read().execute("");
				
//				Intent i = new Intent("mainActivity");
//				startActivity(i);
				
			}
		});
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		this.finish();
	}


	
}
