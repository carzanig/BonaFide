package de.jacobs.university.cnds.bonafide.plus.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.SignalChangeCollector;

/**
 * This service runs automatic measurements in background. An automatic measurement is 
 * fired in specified time and movement intervals. They are called tresholds.
 * @author Tomas
 *
 */

public class BonafideService extends Service implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	private static final int FOREGROUND_SERVICE_ID=94632572;
	
	// action keys
	public static final String BONAFIDE_SERVICE_ACTION_KEY="action";
	public static final String BONAFIDE_SERVICE_UPDATE_PREFERENCES="update_preferences";
	public static final String BONAFIDE_SERVICE_RECIEVE_ALARM="recieve_alarm";
	
	public static final String BONAFIDE_SERVICE_PREFERENCE_ACTIVE="preference_active";
	public static final String BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_TIME="preference_treshold_time";
	public static final String BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_MOVEMENT="preference_treshold_movement";
	
	// Location
    LocationRequest locationRequest;
    LocationClient locationClient;
    
    // Time
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    
    private boolean isAutomeasurementActive=false;
	
    private Location locationOfLastMeasurement;
    private Long timeOfLastMeasurement;
    private Integer tresholdTime;
    private Integer tresholdMovement;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	public void onCreate() {
		super.onCreate();
		// load default preferences
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		
		ApplicationGlobalContext.getInstance().setAppContext(this.getApplicationContext());
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationGlobalContext.getInstance().getAppContext());
		
		// initiates signal strength and network type collector
		SignalChangeCollector.getInstance(this);
		
 		// initiates Alarm fields
 		alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReciever.class);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
 		
 		// automeasurement start?
 		if (sharedPref.getBoolean(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_ACTIVE, false)==true) {
 			// automatic measurement is configured to be active
 			enableAutomeasurement(Integer.valueOf(sharedPref.getString(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_TIME, "30")), Integer.valueOf(sharedPref.getString(ApplicationGlobalContext.PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_MOVEMENT, "500")));
 		}
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		Bundle bundle = intent.getExtras();
		if (bundle!=null && bundle.containsKey(BONAFIDE_SERVICE_ACTION_KEY)) {
			// action should be performed
			
			// update preferences
			if (bundle.getString(BONAFIDE_SERVICE_ACTION_KEY).equals(BONAFIDE_SERVICE_UPDATE_PREFERENCES) && bundle.containsKey(BONAFIDE_SERVICE_PREFERENCE_ACTIVE) && bundle.containsKey(BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_MOVEMENT) && bundle.containsKey(BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_TIME)) {
				boolean active=bundle.getBoolean(BONAFIDE_SERVICE_PREFERENCE_ACTIVE);
				int treshold_time=bundle.getInt(BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_TIME);
				int treshold_movement=bundle.getInt(BONAFIDE_SERVICE_PREFERENCE_TRESHOLD_MOVEMENT);
				
				// tresholds are handeled via this service and not via the provider, because of their OR fusion
				
				if (active==true) {
					// start or restart automeasurement
					enableAutomeasurement(treshold_time,treshold_movement);
				}
				else {
					// disable automeasurement
					disableAutomeasurement();
				}
			}
			else if (bundle.getString(BONAFIDE_SERVICE_ACTION_KEY).equals(BONAFIDE_SERVICE_RECIEVE_ALARM)) {
				// alarm recieved, process time treshold
				onTimeExpired();
			}
		}
		
		return START_STICKY;
	}
	
	/**
	 * Starts or restarts automeasurement service.
	 * @param treshold_time in minutes
	 * @param treshold_movement in meters
	 */
	private void enableAutomeasurement(int treshold_time, int treshold_movement) {
		if (isAutomeasurementActive==true) {
			// restart
			disableAutomeasurement();
		}
		
		// set tresholds for use in the service implementation
		this.tresholdTime=treshold_time;
		this.tresholdMovement=treshold_movement;
		
		// start location service
		locationRequest=LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(10*1000); // millis
		locationRequest.setFastestInterval(10*1000); // updates will arrive after the interval
		// tresholds are handeled by this service
		//locationRequest.setSmallestDisplacement(treshold_movement);
		
		locationClient = new LocationClient(this, this, this);
		
		locationClient.connect();
		
		// start Alarm service
		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
		        SystemClock.elapsedRealtime()+2000, this.tresholdTime*60*1000, alarmIntent);
		
		// start in foreground
 		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.notification_bonafide_service_notification), System.currentTimeMillis());
 		Intent notificationIntent = new Intent(this, FrontendActivity.class);
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 		notification.setLatestEventInfo(this, getString(R.string.notification_bonafide_service_title),
 				getString(R.string.notification_bonafide_service_description), pendingIntent);
 		startForeground(FOREGROUND_SERVICE_ID, notification);
 		isAutomeasurementActive=true;
	}
	
	private void disableAutomeasurement() {
		if (isAutomeasurementActive==false) {
			return;
		}
		
		stopForeground(true);
		isAutomeasurementActive=false;
		
		alarmMgr.cancel(alarmIntent);
		locationClient.disconnect();
	}
	
	@Override
	public void onDestroy() {
		disableAutomeasurement();
		super.onDestroy();
	}

	/**
	 * This method is called by the Location provider and informs about the position change
	 */
	@Override
	public synchronized void onLocationChanged(Location location) {
		
		boolean shouldMeasure=false;
		// check treshold
		
		// movement
		if (locationOfLastMeasurement==null || locationOfLastMeasurement.distanceTo(location)>=this.tresholdMovement) {
			locationOfLastMeasurement=location;
			timeOfLastMeasurement=System.currentTimeMillis();
			
			// reschedule alarm service
			alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime()+(this.tresholdTime*60*1000), this.tresholdTime*60*1000, alarmIntent);
			
			shouldMeasure=true;
		}
		
		// fire measurement to queue when tresholds are met
		if (shouldMeasure) {
			// go ahead
			
			Intent intent = new Intent(this, MeasurementService.class);
			// all protocols
			// automatically pick measurement server
			// set location
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LATITUDE, location.getLatitude());
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LONGITUDE, location.getLongitude());
			// queue measurement request to service
			startService(intent);
		}
	}
	
	/**
	 * This method handles recieved notification from AlarmReciever
	 * (Time treshold implementation)
	 */
	private synchronized void onTimeExpired() {
		// process time treshold
		timeOfLastMeasurement=System.currentTimeMillis();
		// get location
		Location location = locationClient.getLastLocation();
		locationOfLastMeasurement=location;
		
		// go ahead, queue measurement
		
		Intent intent = new Intent(this, MeasurementService.class);
		// all protocols
		// automatically pick measurement server
		// set location
		if (location!=null) {
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LATITUDE, location.getLatitude());
			intent.putExtra(ApplicationGlobalContext.BUNDLE_LOCATION_LONGITUDE, location.getLongitude());
		}
		// queue measurement request to service
		startService(intent);
	}
	
	private void showNotification(String contentTitle, String contentText) {
		Notification.Builder notification = new Notification.Builder(this);
		notification.setContentTitle(contentTitle)
		.setContentText(contentText)
		.setSmallIcon(R.drawable.ic_action_search);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// first int parameter allows one to update the notification later on
		mNotificationManager.notify(3, notification.getNotification());
	}

	@Override
	public void onConnected(Bundle arg0) {
		// request location updates
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}
}
