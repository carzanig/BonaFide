package de.jacobs.university.cnds.bonafide.plus.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.services.MeasurementService;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchAvailableProtocolsCallbackInterface;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchAvailableProtocolsTask;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchMeasurementServers;
import de.jacobs.university.cnds.bonafide.plus.tasks.FetchMeasurementServersCallbackInterface;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.LocationProviderAvailabilityChecker;

public class CustomMeasurementActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnItemSelectedListener,FetchAvailableProtocolsCallbackInterface,FetchMeasurementServersCallbackInterface {

	ProgressDialog progress;
	
	// Location
    LocationRequest locationRequest;
    LocationClient locationClient;
    Location measurementLocation; // not injected to the measurement request when null
	
	HashMap<ProtocolDescription, CheckBox> protocolToCheckBoxMapping = new HashMap<ProtocolDescription, CheckBox>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_measurement);
		
		// get list of measurement servers
		showProgressBar();
		
		// add listener to show progress of seekbar
		SeekBar seekBar = (SeekBar) findViewById(R.id.custom_measurement_number_of_cycles);
		final TextView cyclesCaption = (TextView) findViewById(R.id.custom_measurement_cycles_title);
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean arg2) {
				// update the progress in view
				cyclesCaption.setText(getString(R.string.custom_measurement_cycles_title)+" "+String.valueOf(progress+1));
			}
		});
		// update the caption
		seekBar.setProgress(1);
		seekBar.setProgress(0);
		
		// add listener
		Spinner spinner = (Spinner) findViewById(R.id.custom_measurement_spinner);
		spinner.setOnItemSelectedListener(this);
		
		Log.i("BonaFide Provider", "Fetching list of measurement servers");
		new FetchMeasurementServers(this).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.custom_measurement, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		LocationProviderAvailabilityChecker.requestLocationProviderPermissionWhenDisabled(this);
		startLocationRequest();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopLocationRequests();
	}
	
	/**
	 * Shows dialog, which informs user about an error in interaction with the central server
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private void showErrorDialog() {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(getString(R.string.custom_measurement_retrieval_error_title));
		alert.setMessage(getString(R.string.custom_measurement_retrieval_error));
		alert.setButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				finish();
			}
		});
		alert.show();
	}
	
	private void showProgressBar() {
		progress = ProgressDialog.show(this, getString(R.string.custom_measurement_progress_title),
				getString(R.string.custom_measurement_progress), true);
	}
	
	private void hideProgressBar() {
		if (progress!=null) {
			progress.dismiss();
		}
	}
	
	// button listener defined in layout xml
	public void startButtonPressed(View view) {
		SeekBar cyclesSeekBar = (SeekBar) findViewById(R.id.custom_measurement_number_of_cycles);
		if (cyclesSeekBar.getProgress()>0) {
			// warning about traffic

	        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.custom_measurement_cycles_confirm_title)
		        .setMessage(R.string.custom_measurement_cycles_confirm_warning)
		        .setPositiveButton(R.string.custom_measurement_cycles_confirm_option_positive, new DialogInterface.OnClickListener() {
	
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	startMeasurement();
		            }
	
		        })
		        .setNegativeButton(R.string.custom_measurement_cycles_confirm_option_negative, null)
		        .show();
		}
		else {
			startMeasurement();
		}
	}
	
	public void startMeasurement() {
		// queue task to measurement service
		Spinner measurementServersSpinner = (Spinner) findViewById(R.id.custom_measurement_spinner);
		SeekBar cyclesSeekBar = (SeekBar) findViewById(R.id.custom_measurement_number_of_cycles);
		
		MeasurementServer measurementServer = (MeasurementServer)measurementServersSpinner.getSelectedItem();
			if (measurementServer==null) {
				return;
			}
		
		// add protocol descriptions from GUI to list
		ArrayList<ProtocolDescription> protocolDescriptions = new ArrayList<ProtocolDescription>();
		Iterator<ProtocolDescription> keys = this.protocolToCheckBoxMapping.keySet().iterator();
		while (keys.hasNext()) {
			ProtocolDescription key = keys.next();
			CheckBox checkBox = this.protocolToCheckBoxMapping.get(key);
			if (checkBox.isChecked()) {
				protocolDescriptions.add(key);
			}
		}
		
		if (protocolDescriptions.size()==0) {
			return;
		}
		
		int cycles = cyclesSeekBar.getProgress()+1; // SeekBar starts with 0
		
		Intent intent = new Intent(this, MeasurementService.class);
		// add location
		if (measurementLocation!=null) {
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LATITUDE, measurementLocation.getLatitude());
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LONGITUDE, measurementLocation.getLongitude());
		}
		// add protocols
		intent.putExtra(ApplicationGlobalContext.BUNDLE_PROTOCOL_DESCRIPTION, protocolDescriptions);
		// add measurement server
		intent.putExtra(ApplicationGlobalContext.BUNDLE_MEASUREMENT_SERVER, measurementServer);
		// add number of cycles
		intent.putExtra(ApplicationGlobalContext.BUNDLE_CYCLES, cycles);
		// queue measurement request to service
		startService(intent);
		
		Toast.makeText(this, getString(R.string.custom_measurement_started_msg), Toast.LENGTH_LONG).show();
		finish();
	}

	/**
	 * Called when list of measurement servers is retrieved
	 */
	@Override
	public void measurementServersRecieved(
			List<MeasurementServer> measurementServers) {
		Log.i("BonaFide Provider", "Measurement servers are loaded");
		
		if (measurementServers==null) {
			// there was an error
			hideProgressBar();
			showErrorDialog();
			return;
		}
		
		Spinner measurementServersSpinner = (Spinner) findViewById(R.id.custom_measurement_spinner);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, measurementServers);
		measurementServersSpinner.setAdapter(adapter);
		hideProgressBar();
		
	}

	/**
	 * Responds to measurement server selection
	 * @param arg0
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		MeasurementServer selectedMeasurementServer = (MeasurementServer) parent.getItemAtPosition(pos);
		// fetch protocols from measurement server
		showProgressBar();
		new FetchAvailableProtocolsTask(this).execute(selectedMeasurementServer);
	}

	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}

	/**
	 * Called when list of available protocols is retrieved
	 */
	@Override
	public void availableProtocolsRecieved(
			List<ProtocolDescription> availableProtocols) {
		
		if (availableProtocols==null) {
			// there was an error
			hideProgressBar();
			showErrorDialog();
			return;
		}
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.custom_measurement_protocols_layout);
		
		// remove all previous checkboxes
		layout.removeAllViews();
		protocolToCheckBoxMapping.clear();
		
		// add checkboxes to view
		
		Iterator<ProtocolDescription> iter = availableProtocols.iterator();
		while (iter.hasNext()) {
			ProtocolDescription protocol = iter.next();
			CheckBox protocolCheckBox = new CheckBox(this);
			protocolCheckBox.setText(protocol.getProtocolName());
			protocolCheckBox.setChecked(true);
			// add to layout
			layout.addView(protocolCheckBox);
			protocolToCheckBoxMapping.put(protocol, protocolCheckBox);
		}
		
		hideProgressBar();
	}
	
	
	/**
	 * This method recieves location and should it delegate to the view and prepare it for injection to the measurement request
	 */
	private synchronized void gotPositionForMeasurement(Location location) {
		measurementLocation=location;
		
		ProgressBar progress = (ProgressBar) findViewById(R.id.location_progress);
		TextView locationView = (TextView) findViewById(R.id.custom_measurement_location_retrieving);
		
		if (measurementLocation!=null) {
			// update progress bar
			progress.setVisibility(View.INVISIBLE);
			
			// update text
			locationView.setText(getString(R.string.custom_measurement_location_retrieved));
		}
		else {
			// reset to the init state
			
			// update progress bar
			progress.setVisibility(View.VISIBLE);
			
			// update text
			locationView.setText(getString(R.string.custom_measurement_location_retrieving));
		}
	}
	
	/**
	 * This method starts location requests
	 */
	private void startLocationRequest() {
		// start location service
		locationRequest=LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(1000); // millis
		
		locationClient = new LocationClient(this, this, this);
		
		locationClient.connect();
	}
	
	private void stopLocationRequests() {
		if (locationClient!=null && locationClient.isConnected()) {
			locationClient.disconnect();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		gotPositionForMeasurement(location);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {
		// request location updates
		gotPositionForMeasurement(locationClient.getLastLocation());
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {

	}

}
