package de.jacobs.university.cnds.bonafide.plus.activities;

/**
 * 
 * @author Tomas Ludrovan, tomas@ludrovan.sk
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.gui.components.FilterMultiSpinner;
import de.jacobs.university.cnds.bonafide.plus.gui.components.FilterMultiSpinner.MultiSpinnerListener;
import de.jacobs.university.cnds.bonafide.plus.model.TargetScope;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResult;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultFilter;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.services.BonafideService;
import de.jacobs.university.cnds.bonafide.plus.tasks.DrawableResultsTask;
import de.jacobs.university.cnds.bonafide.plus.tasks.RedrawMeasurementServersTask;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.OnMapIdleListener;
import de.jacobs.university.cnds.bonafide.plus.utils.OnMapIdleReciever;
public class FrontendActivity extends Activity implements MultiSpinnerListener {
	
	public static final int REQUEST_PLAY_SERVICES_RECOVERY=1023;
	
	private static FrontendActivity frontendActivity;
	private boolean isVisible=false;
	
	private GoogleMap map;
	private HashMap<MeasurementServer,Marker> measurementServerMarkers=new HashMap<MeasurementServer,Marker>();
	private ArrayList<Polygon> resultPolygons = new ArrayList<Polygon>();
	
	private Marker focusedLocation=null;
	
	private List<DrawableResultFilter> filters = new ArrayList<DrawableResultFilter>();
	// filters in the following variable contains only selected values
	private List<DrawableResultFilter> activeFilters = new ArrayList<DrawableResultFilter>();
	private List<TargetScope> scopes = new ArrayList<TargetScope>();
	
	private TargetScope selectedTargetScope=null;
	
	// time until which the callback is blocked
	private long spinnerCallbackDelayTime=System.currentTimeMillis();
	
	// custom implementation of map onIdle listener
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_frontend);
		
		// load default configuration on first run
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
		// first call to this can take longer
		ApplicationGlobalContext.getInstance().setAppContext(this.getApplicationContext());
		
		
		// Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.setMyLocationEnabled(true);
        
        // show current location when available
        LatLng moveTo = new LatLng(47.414346, 8.549716); // UZH Zurich
        Location currentLocation = map.getMyLocation();
        if (currentLocation!=null) {
        	moveTo=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(moveTo, 13));
        
        OnMapIdleReciever mapIdleReciever = new OnMapIdleReciever() {
			@Override
			public void onMapIdle() {
				runOnUiThread(new Runnable() {
			        @Override
			        public void run() {
			        	startResultsTask();
			        }
			    });
			}
		};
        
        map.setOnCameraChangeListener(new OnMapIdleListener(mapIdleReciever));
        
        
        frontendActivity=this;
        
        Intent service = new Intent(this, BonafideService.class);
    	startService(service);
    	
    	final FrontendActivity thisActivity = this;
    	
    	// set GUI listeners
    	Spinner filterNameSpinner = (Spinner) findViewById(R.id.frontend_filter_name);
    	filterNameSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos,
					long id) {
				// updates options
				DrawableResultFilter selectedFilter = (DrawableResultFilter) parent.getItemAtPosition(pos);
				
				FilterMultiSpinner filterOptionsSpinner = (FilterMultiSpinner) findViewById(R.id.frontend_filter_options);
				filterOptionsSpinner.setItems(selectedFilter, activeFilters, getString(R.string.frontend_filter_details_label), thisActivity);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// set options to "view all"
			}
    		
		});
    	
    	Spinner targetScopesSpinner = (Spinner) findViewById(R.id.frontend_target_scope);
    	targetScopesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos,
					long id) {
				/*if (pos==0) {
					return;
				}*/
				if (spinnerCallbackDelayTime>System.currentTimeMillis()) {
					return;
				}
				TargetScope selectedScope = (TargetScope) parent.getItemAtPosition(pos);
				updateFiltersAndScope(selectedScope);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * Disables callback handling for short time - workaround for temporary callback deactivation
	 */
	private void delaySpinnerCallback() {
		this.spinnerCallbackDelayTime=System.currentTimeMillis()+800;
	}
	
	/**
	 * This method updates the view based on selected filters and target scope
	 */
	public void updateFiltersAndScope(TargetScope selectedScope) {
		// prevent update loop when selection changed by recieved result
		Spinner targetScopesSpinner = (Spinner) findViewById(R.id.frontend_target_scope);
		if (selectedTargetScope!=null && selectedTargetScope.equals(((TargetScope)(targetScopesSpinner.getSelectedItem())).getName())) {
			return;
		}
		startResultsTask();
	}
	
	public void moveCameraToMeasurementServer(double latitude, double longitude, String pointName, String snippetText) {
		if (focusedLocation!=null) {
			focusedLocation.remove();
		}
		
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13));
		
		// add point
		
		Marker marker = map.addMarker(new MarkerOptions()
        .title(pointName)
        .snippet("")
        .position(new LatLng(latitude, longitude)));
		
		focusedLocation=marker;
	}
	
	@Override
	protected void onResume() {
	  super.onResume();
	  this.isVisible=true;
	  if (checkPlayServices()) {
	    // Then we're good to go!
		  // redraw measurement servers
		  new RedrawMeasurementServersTask().execute();
	  }
	}
	
	public void startResultsTask() {
		LatLngBounds viewPort = map.getProjection().getVisibleRegion().latLngBounds;
		Spinner targetScopesSpinner = (Spinner) findViewById(R.id.frontend_target_scope);
		
		ToggleButton toggle = (ToggleButton) findViewById(R.id.filterSwitch);
		boolean filterActive=toggle.isChecked();
		
		String selectedTargetScope=null;
		if (targetScopesSpinner.getSelectedItem()!=null) {
			selectedTargetScope = ((TargetScope) targetScopesSpinner.getSelectedItem()).getName();
		}
		new DrawableResultsTask(viewPort,map.getCameraPosition().zoom,this.activeFilters, filterActive, selectedTargetScope).execute();
	}
	
	@Override
	protected void onPause() {
		this.isVisible=false;
		super.onPause();
	}
	
	public boolean isVisible() {
		return this.isVisible;
	}

	private boolean checkPlayServices() {
		// check support for Google services
		int isGooglePlaySupported = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
		
		if (isGooglePlaySupported!=ConnectionResult.SUCCESS) {
			// Google Play is not installed or is outdated
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isGooglePlaySupported, this, REQUEST_PLAY_SERVICES_RECOVERY);
			dialog.show();
			return false;
		}
		else {
			return true;
		}
	}
	
	public static FrontendActivity getFrontendActivity() {
		return frontendActivity;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  switch (requestCode) {
		    case REQUEST_PLAY_SERVICES_RECOVERY:
		      if (resultCode == RESULT_CANCELED) {
		        Toast.makeText(this, "Google Play Services must be installed.",
		            Toast.LENGTH_SHORT).show();
		        finish();
		      }
		      return;
		  }
		  super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.frontend, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.frontend_settings:
	        	//Intent service = new Intent(this, BonafideService.class);
	        	//startService(service);
	        	Intent set = new Intent(this, SettingsActivity.class);
				startActivityForResult(set, ApplicationGlobalContext.REQUEST_CHANGE_SETTINGS);
	            return true;
	        case R.id.frontend_results:
	        	Intent activity = new Intent(this, ResultsActivity.class);
	        	startActivity(activity);
	        	return true;
	        case R.id.frontend_about:
	        	Intent aboutIntent = new Intent(this, AboutActivity.class);
				startActivity(aboutIntent);
				return true;
	        case R.id.frontend_custom_measurement:
	        	Intent customMeasurement = new Intent(this, CustomMeasurementActivity.class);
	        	startActivity(customMeasurement);
	        	return true;
	        case R.id.frontend_privacy:
	        	Intent privacyActivity = new Intent(this, PrivacyActivity.class);
	        	startActivity(privacyActivity);
	        	return true;
	        case R.id.frontend_migration:
	        	AlertDialog alert = new AlertDialog.Builder(this).create();
	    		alert.setTitle(getString(R.string.frontend_action_migration));
	    		alert.setMessage(getString(R.string.frontend_migration_dialog));
	    		
	    		alert.show();
	    		
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
		
		//Toast.makeText(getBaseContext(), "OK", Toast.LENGTH_LONG).show();
	}
	
	
	// methods for map control
	public synchronized void addResultToMap(DrawableResult result) {
		// compute color for segment
		double qualityCoeficient=result.getQuality()/100.0; // 0-100 /100
		int green=(int)(qualityCoeficient*255);
		int red=255-green;
		
		// Instantiates a new Polygon object and adds points to define a rectangle
		PolygonOptions rectOptions = new PolygonOptions()
		              .add(new LatLng(result.getSouthWestLatitude(), result.getSouthWestLongitude()),
		                   new LatLng(result.getSouthWestLatitude(), result.getNorthEastLongitude()),
		                   new LatLng(result.getNorthEastLatitude(), result.getNorthEastLongitude()),
		                   new LatLng(result.getNorthEastLatitude(), result.getSouthWestLongitude()),
		                   new LatLng(result.getSouthWestLatitude(), result.getSouthWestLongitude()))
		                   
		                   .fillColor(Color.argb(80, red, green, 0))
		                   .strokeWidth(0);

		// Get back the mutable Polygon
		Polygon polygon = map.addPolygon(rectOptions);
		
		resultPolygons.add(polygon);
	}
	
	public synchronized void removeAllResults() {
		Iterator<Polygon> iter = resultPolygons.iterator();
		while (iter.hasNext()) {
			Polygon polygon = iter.next();
			polygon.remove();
		}
		
		resultPolygons.clear();
	}
	
	/**
	 * This method adds measurement server marker to the map. Server.getName() and server.getPosition must return non-null values
	 * @param position
	 * @param name
	 */
	public synchronized void addMeasurementServerToMap(MeasurementServer server) {
		if (measurementServerMarkers.containsKey(server)) {
			return;
		}
		Marker marker = map.addMarker(new MarkerOptions()
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_measurement_server))
        .title("Measurement server")
        .snippet(server.getName())
        .position(server.getPosition()));
		
		measurementServerMarkers.put(server,marker);
	}
	
	/**
	 * This methods removes all measurement servers from the map
	 */
	public synchronized void removeAllMeasurementServerMarkers() {
		Iterator<Marker> iter = measurementServerMarkers.values().iterator();
		while (iter.hasNext()) {
			iter.next().remove();
		}
		measurementServerMarkers.clear();
	}
	
	/**
	 * This method sets available filters and updates them in GUI
	 * @param filters
	 */
	public void setFilters(List<DrawableResultFilter> filters, List<DrawableResultFilter> activeFilters) {
		this.filters=filters;
		this.activeFilters=activeFilters;
		
		Log.e(ApplicationGlobalContext.LOG_TAG, "Filter size: "+filters.size());
		Log.e(ApplicationGlobalContext.LOG_TAG, "active size: "+activeFilters.size());
		
		// TODO: active filters
		
		// update GUI
		Spinner filterNameSpinner = (Spinner) findViewById(R.id.frontend_filter_name);
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.filters);
		filterNameSpinner.setAdapter(adapter);
	}
	
	public void setAvailableTargetScopes(List<String> scopes, String selectedScope) {
		int selectedIndex=0;
		
		this.scopes.clear();
		
		for (int i=0; i<scopes.size(); i++) {
			String scope = scopes.get(i);
			
			TargetScope newScope = new TargetScope(this, scope);
			
			if (selectedScope.equals(scope)) {
				selectedIndex=i;
				selectedTargetScope=newScope;
			}
			this.scopes.add(newScope);
		}
		
		// update GUI
		
		Spinner targetScopesSpinner = (Spinner) findViewById(R.id.frontend_target_scope);
		
		
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, this.scopes);
		
		delaySpinnerCallback();
		
		targetScopesSpinner.setAdapter(adapter);
		if (selectedIndex>0) {
			targetScopesSpinner.setSelection(selectedIndex);
		}
		
	}

	/**
	 * This method is invoked when multiselect (filter options) changes
	 */
	@Override
	public void onItemsSelected(DrawableResultFilter filter, List<String> selectedStrings) {
		// replace active filters config
		DrawableResultFilter updatedFilter = this.activeFilters.get(this.activeFilters.indexOf(filter));
		List<String> options = updatedFilter.getFilterOptions();
		options.clear();
		// fill new options
		Iterator<String> iter = selectedStrings.iterator();
		while (iter.hasNext()) {
			options.add(iter.next());
		}
		
		// check if switch for filtering enabled
		ToggleButton toggle = (ToggleButton) findViewById(R.id.filterSwitch);
		/*if (!toggle.isChecked()) {
			return;
		}*/
		// turn on filtering
		toggle.setChecked(true);
		
		// update filters = send request to the server
		startResultsTask();
	}
	
	// filter button listener
	public void filterButtonClicked(View view) {
		startResultsTask();
	}

}
