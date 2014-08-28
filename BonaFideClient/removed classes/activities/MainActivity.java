/*
	Copyright (c) 2012, Vitali Bashko
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met: 

	1. Redistributions of source code must retain the above copyright notice, this
   	list of conditions and the following disclaimer. 
	2. Redistributions in binary form must reproduce the above copyright notice,
   	this list of conditions and the following disclaimer in the documentation
   	and/or other materials provided with the distribution. 

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

	The views and conclusions contained in the software and documentation are those
	of the authors and should not be interpreted as representing official policies, 
	either expressed or implied, of the FreeBSD Project.
*/

package de.jacobs.university.cnds.bonafide.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.jacobs.university.cnds.bonafide.R;
import de.jacobs.university.cnds.bonafide.adapter.ProtocolSpecificationAdapter;
import de.jacobs.university.cnds.bonafide.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.utils.ServerConnector;


/**
 * Main activity is launched as the first activity in application. It checks the networks availability, 
 * initiates the application context, downloads the list of available application protocols to test 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class MainActivity extends Activity {
					
	private List<String> listOfProtocols = new ArrayList<String>();
	
	private ListView listView_protocolSpecification;	
	
	private ProgressDialog progressDialog;
	
	private TextView testInetStatus;
	private TextView initializeStatus;
	private TextView connectStatus;
	
	private String ip = null; 	
	private Integer port = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {            
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		ip = mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, "212.201.44.162");
		port = Integer.valueOf(mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_SENTRAL_SERVER_PORT, "4000"));
		
        
    	progressDialog = new ProgressDialog(MainActivity.this);
    	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	progressDialog.setMessage(getResources().getString(R.string.connectivity_status));    	
    	progressDialog.show();
    	

    	testInetStatus = (TextView) findViewById(R.id.text_view_test_status);
    	initializeStatus = (TextView) findViewById(R.id.text_view_initialize_status);
    	connectStatus = (TextView) findViewById(R.id.text_view_connect_to_server_status);
    	
    	
        listView_protocolSpecification = (ListView) findViewById(R.id.list_view_protocol_specification_list);
        listView_protocolSpecification.setVisibility(View.GONE);  
    	
    	//Check the Internet Connectivity   	    	
    	testInetStatus.setText(R.string.connectivity_status);
    	testInetStatus.setVisibility(View.VISIBLE);
    	
		final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);		
		boolean isWiFi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		boolean isMobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		
		//If no connectivity found - show an error dialog 
		if (!isWiFi && !isMobileNetwork) {
			progressDialog.cancel();
			String message = getResources().getString(R.string.connectivity_status) + getResources().getString(R.string.smile_unhappy);
			testInetStatus.setText(message);			
			showFailDialog(R.string.network_failure_title, R.string.network_failure_text, false);										
		} else {
			String message = getResources().getString(R.string.connectivity_status) + getResources().getString(R.string.smile_happy);
			testInetStatus.setText(message);
				
			new InitializeApplicationContextTask().execute();						
		}
    }
    
	/**
	 * create the options menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu1_layout, menu);
		return true;
	}
	
	/**
	 * Set handlers to the options menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_about:
				Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
				startActivity(aboutIntent);
				break;
			case R.id.menu_view_all_results:
				Intent intent = new Intent(MainActivity.this, MaintainMeasurementResultsActivity.class);
				startActivity(intent);
				break;			
			case R.id.settings:
				Intent set = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(set, ApplicationGlobalContext.REQUEST_CHANGE_SETTINGS);	
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		//Reconnect to the server, if the application settings have been modified
		if (requestCode == ApplicationGlobalContext.REQUEST_CHANGE_SETTINGS) {			
	        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			ip = mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, null);
			port = Integer.valueOf(mPrefs.getString(ApplicationGlobalContext.PREFERENCES_KEY_SENTRAL_SERVER_PORT, null));
			
			listOfProtocols.clear();
			listView_protocolSpecification.refreshDrawableState();
			new ConnectToServerTask().execute(); 
		}
	}

	/*
	 * private method used for displaying the custom fail dialog
	 */
	private void showFailDialog(int title, int text, boolean showSettings) {
		final Dialog dialog = new Dialog(MainActivity.this);
		
		View view = View.inflate(MainActivity.this, R.layout.failure_dialog_layout, null);
		TextView textView = (TextView) view.findViewById(R.id.failure_dialog_text);
		textView.setText(text);		
		Button cancelBtn = (Button) view.findViewById(R.id.button_close_application);
		cancelBtn.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				dialog.cancel();
				finish();
			}
		});
		
		Button settings = (Button) view.findViewById(R.id.button_show_settings);
		if (showSettings) {
			settings.setVisibility(View.VISIBLE);
		} else {
			settings.setVisibility(View.GONE);
		}
		
		settings.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dialog.cancel();
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(intent, ApplicationGlobalContext.REQUEST_CHANGE_SETTINGS);
			}
		});
					
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);			
		dialog.setTitle(title);
		dialog.setContentView(view);
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);
		dialog.show();	
    }
   
	
	//an asynchronous task used for initializing the application context on the background	 	
   private class InitializeApplicationContextTask extends AsyncTask<Void, Void, Boolean> {
	   	   
	   @Override
	   protected void onPreExecute() {
		   initializeStatus.setText(R.string.initialize_status);
		   initializeStatus.setVisibility(View.VISIBLE);
		   progressDialog.setMessage(getResources().getString(R.string.initialize_status)); 
	   }

	@Override
	   protected Boolean doInBackground(Void... params) {
		   ApplicationGlobalContext context = ApplicationGlobalContext.getInstance();
		   if (context == null) {
			   return false;
		   }
		   return true;
	   }

	   @Override
	   protected void onPostExecute(Boolean result) {
		   String message = null;
		   if (result) {
			   message = getResources().getString(R.string.initialize_status) + getResources().getString(R.string.smile_happy);   
		   } else {
			   message = getResources().getString(R.string.initialize_status) + getResources().getString(R.string.smile_unhappy);
		   }
		   
		   MainActivity.this.initializeStatus.setText(message);
		   
		   if (result) {
			   new ConnectToServerTask().execute();   
		   } else {
			   showFailDialog(R.string.initialize_context_failure_title, R.string.initialize_context_failure_text, false);
		   }		  		   
	   }	   	      	
   }
   
	//an asynchronous task used for connection to the measurement server and retrieving the list of available protocol description files
   private class ConnectToServerTask extends AsyncTask<Void, Void, String> {
	   
	   @Override
	   protected void onPostExecute(String specifications) {
		   String message = null;		   
		   if (specifications == null) {
			   progressDialog.cancel();
			   message = getResources().getString(R.string.server_status) + getResources().getString(R.string.smile_unhappy);
			   connectStatus.setText(message);			   
			   
			   showFailDialog(R.string.server_down_failure_title, R.string.server_down_failure_text, true);
		   } else {
			   	message = getResources().getString(R.string.server_status) + getResources().getString(R.string.smile_happy);
			   	connectStatus.setText(message);
	    		StringTokenizer st = new StringTokenizer(specifications, "\r\n");
	        	while (st.hasMoreTokens()) {
	        		listOfProtocols.add(st.nextToken());
	        	}
	        	listView_protocolSpecification.setVisibility(View.VISIBLE);
	        	listView_protocolSpecification.setAdapter(new ProtocolSpecificationAdapter(listOfProtocols, MainActivity.this)); 
	        	progressDialog.cancel();
		   }
	   }

	   @Override
	   protected void onPreExecute() {
			connectStatus.setText(R.string.server_status);
			connectStatus.setVisibility(View.VISIBLE);
			progressDialog.setMessage(getResources().getString(R.string.server_status)); 
	   }

	@Override
	   protected String doInBackground(Void... params) {
			//download the list of available protocol description files
			String specifications = ServerConnector.getProtocolDescriptionsList(ip, port);													
			return specifications;
	   }	   
   }
}