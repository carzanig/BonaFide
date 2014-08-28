package de.jacobs.university.cnds.bonafide.plus.services;

/**
 * This service performs measurements.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.Log;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.plus.activities.ResultsActivity;
import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;
import de.jacobs.university.cnds.bonafide.plus.utils.ServerConnector;
import de.jacobs.university.cnds.bonafide.plus.utils.SignalChangeCollector;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;

public class MeasurementService extends IntentService {
	private static final Integer NOTIFICATION_ID = 1035764;
	
	public MeasurementService() {
		super("MeasurementService");
	}

	/**
	 * This method handles the measurement Queue. Measurement is performed and the device is
	 * forced to be awake while performing it. Awake lock is released when measurement is done.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {
		try {
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		        "MeasurementServiceWakeLock");
		wakeLock.acquire();
		
		// show progress notification - init
		showProgressNotification(NOTIFICATION_ID, 0, 0, true, getString(R.string.notification_measurement_title), getString(R.string.notification_measurement_init));
		
		Bundle bundle = intent.getExtras();
		
		Double latitude=null;
		Double longitude=null;
		int cycles=1; // default
		List<MeasurementServer> givenMeasurementServerList=null;
		List<ProtocolDescription> givenProtocolDescription=null;
		
		
		if (bundle!=null) {
			if (bundle.containsKey(ApplicationGlobalContext.BUNDLE_LOCATION_LATITUDE) && bundle.containsKey(ApplicationGlobalContext.BUNDLE_LOCATION_LONGITUDE)) {
				latitude=bundle.getDouble(ApplicationGlobalContext.BUNDLE_LOCATION_LATITUDE);
				longitude=bundle.getDouble(ApplicationGlobalContext.BUNDLE_LOCATION_LONGITUDE);
			}
			
			if (bundle.containsKey(ApplicationGlobalContext.BUNDLE_CYCLES)) {
				cycles=bundle.getInt(ApplicationGlobalContext.BUNDLE_CYCLES);
			}
			
			if (bundle.containsKey(ApplicationGlobalContext.BUNDLE_MEASUREMENT_SERVER)) {
				MeasurementServer measurementServer=(MeasurementServer) intent.getSerializableExtra(ApplicationGlobalContext.BUNDLE_MEASUREMENT_SERVER);
				givenMeasurementServerList=new ArrayList<MeasurementServer>();
				givenMeasurementServerList.add(measurementServer);
			}
			
			if (bundle.containsKey(ApplicationGlobalContext.BUNDLE_PROTOCOL_DESCRIPTION)) {
				givenProtocolDescription=(List<ProtocolDescription>) intent.getSerializableExtra(ApplicationGlobalContext.BUNDLE_PROTOCOL_DESCRIPTION);
			}
		}
		
		
		
		
		
		// check the Internet connectivity
		final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);		
		boolean isWiFi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		boolean isMobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
		
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		if (!isMobileNetwork && !isWiFi) {
			// nothing to do - no connection
			showNotification(NOTIFICATION_ID, getString(R.string.notification_error_title), getString(R.string.notification_no_connection_message), true);
			wakeLock.release();
			return;
		}
				
		// show progress notification - picking server and loading protocols
		showProgressNotification(NOTIFICATION_ID, 0, 0, true, getString(R.string.notification_measurement_title), getString(R.string.notification_measurement_server_and_protocol_init));
		
		
		
		// now we have list of servers. Now we will try to download list of protocols if not already provided
		MeasurementServer targetMeasurementServer=null; // target server
		List<ProtocolDescription> targetProtocolDescriptions=null;
		
		if (givenMeasurementServerList!=null && givenProtocolDescription!=null) {
			// we already have server and list of protocol descriptions
			targetMeasurementServer=givenMeasurementServerList.get(0); // cant be empty when != null (so is the current implementation)
			targetProtocolDescriptions=givenProtocolDescription;
		}
		else {
			// we dont have the complete target bundle. Maybe we have server...
			
			// pick measurement server
			if (givenMeasurementServerList==null) {
				if (latitude!=null && longitude!=null) {
					// pick the nearest server
					try {
						List<MeasurementServer> serverList = RESTServiceProvider.getInstance().getNearestMeasurementServerList(latitude,longitude);
						if (serverList.size()>0) {
							givenMeasurementServerList=serverList;
						}
					} catch (RestException e) {
						// measurement server listing (nearest) failed
					}
				}
			}
			
			// check if still null - cases: no GPS data provided or nearest servers retrieval failed
			if (givenMeasurementServerList==null) {
				try {
					givenMeasurementServerList=RESTServiceProvider.getInstance().getRandomMeasurementServers();
					
					if (givenMeasurementServerList.size()==0) {
						showNotification(NOTIFICATION_ID, getString(R.string.notification_error_title), getString(R.string.notification_no_measurement_server), true);
						wakeLock.release();
						return;
					}
				}
				catch (RestException e) {
					// no measurement server - unrecoverable error
					showNotification(NOTIFICATION_ID, getString(R.string.notification_error_title), getString(R.string.notification_no_measurement_server), true);
					wakeLock.release();
					return;
				}
			}
			
			// now we have server(s)
			// next step is to grab protocols
			
			Iterator<MeasurementServer> serverIterator = givenMeasurementServerList.iterator();
			
			// pick measurement server, from which list of protocols can be retrieved and put them to target
			// loop makes sure, that we will measure against a working server
			protocolLoop: while (serverIterator.hasNext()) {
				targetMeasurementServer=serverIterator.next();
				
				// show progress notification - picking server and loading protocols
				showProgressNotification(NOTIFICATION_ID, 0, 0, true, targetMeasurementServer.getName(), getString(R.string.notification_measurement_init_protocols));
			
				
				// protocols
				// load them from the measurement server
				try {
					targetProtocolDescriptions=ServerConnector.getProtocolDescriptions(targetMeasurementServer.getIp(), targetMeasurementServer.getPort());
		        	if (targetProtocolDescriptions==null || targetProtocolDescriptions.size()==0) {
						// retrieving list of available protocols failed
		        		// repeat with next server
		        		targetMeasurementServer=null;
		        		targetProtocolDescriptions=null;
					}
		        	else {
		        		// protocols are loaded, jump to measurement
		        		break protocolLoop;
		        	}
				} catch (Exception e) {
					// problem while retrieving list of protocols
					// repeat with next server
					targetMeasurementServer=null;
	        		targetProtocolDescriptions=null;
				}
			}
		}
		
		
		
		if (targetProtocolDescriptions==null || targetProtocolDescriptions.size()==0) {
			showNotification(NOTIFICATION_ID, getString(R.string.notification_error_title), getString(R.string.notification_no_protocol_descriptions), true);
			wakeLock.release();
			return;
		}
		
			
		// perform measurements for specified protocols
		int protocolCounter=0; // used for notifications
		int successfulCounter=0;
		Iterator<ProtocolDescription> protocolIter = targetProtocolDescriptions.iterator();
		measurementLoop: while (protocolIter.hasNext()) {
			ProtocolDescription protocolDesc = protocolIter.next();
			
			// show progress notification - measurement progress
			showProgressNotification(NOTIFICATION_ID, targetProtocolDescriptions.size(), protocolCounter, false, "("+(protocolCounter+1)+"/"+targetProtocolDescriptions.size()+") "+targetMeasurementServer.getName(), getString(R.string.notification_measurement_testing_protocol)+": "+protocolDesc.getProtocolName());
			
			ClientExecutionResults results = new ClientExecutionResults(cycles);
			// set position if available
			if (latitude!=null && longitude!=null) {
				results.setLatitude(latitude);
				results.setLongitude(longitude);
			}
			results.setCycles(cycles);
			results.setMeasurementServer(targetMeasurementServer);
			
			if (isMobileNetwork) {
				results.setMobileNetwork(true);
			}
			
			
			// read information about the signal and network type
			SignalChangeCollector.getInstance(this).startCapture();										
			
			results.setOperatorName(telephonyManager.getNetworkOperatorName());
			results.setOperator(telephonyManager.getNetworkOperator());
			results.setCountry(telephonyManager.getSimCountryIso());
			
			results.setProtocolSpecificationName(protocolDesc.getProtocolName());
					
			Log.i(ApplicationGlobalContext.LOG_TAG, "Starting measurement for "+protocolDesc.getProtocolName()+" at protocol "+protocolDesc.getPFPort()+" and random "+protocolDesc.getRFPort());
			// run test
			ServerConnector.runTest(targetMeasurementServer.getIp(), targetMeasurementServer.getPort(), protocolDesc, cycles, results, null);
			Log.i(ApplicationGlobalContext.LOG_TAG, "Measurement done");
			
			SignalChangeCollector.getInstance(this).stopCapture();
			results.setNetworkType(SignalChangeCollector.getInstance(this).getNetworkType());
			results.setSignalStrength(SignalChangeCollector.getInstance(this).getAverageSignalStrength());
			
			Log.i(ApplicationGlobalContext.LOG_TAG, "Retrieving results from the measurement server for UID "+results.getUUID());
			// retrieve download bandwidth measurement results
			String serverTestExecutionResults = ServerConnector.retrieveServerTestExecutionResults(targetMeasurementServer.getIp(), 
					targetMeasurementServer.getPort(), 
					results.getUUID());
			
			if (serverTestExecutionResults != null && !serverTestExecutionResults.equals(GlobalConstants.TEST_RESULTS_NOT_FOUND) ) {
				Log.i(ApplicationGlobalContext.LOG_TAG, "Done.");
				serverTestExecutionResults = serverTestExecutionResults.substring(0, serverTestExecutionResults.length() - GlobalConstants.END_OF_MESSAGE.length());
				ClientExecutionResults.parseServerTestExecutionResults(results, serverTestExecutionResults);
				successfulCounter++;
			} else {
				Log.e(ApplicationGlobalContext.LOG_TAG, "Measurement server results are null or failed. This is the error: "+results.getErrorMessage());
				showNotification(NOTIFICATION_ID, getString(R.string.notification_error_title), results.getErrorMessage()+" "+getString(R.string.notification_results_not_available)+" "+targetMeasurementServer.getName()+" - "+protocolDesc.getProtocolName(), true);
				// TODO - maybe omit this? - yes, we can send also partial results to the central server
				//continue measurementLoop;
			}
			
			protocolCounter++;
			
			// submit results to server
			try {
				// store each cycle as a separate results
				int cyclesCount=results.getCycles();
				Log.i(ApplicationGlobalContext.LOG_TAG, "Storing results to the central server");
				for (int i=0; i<cyclesCount; i++) {
					RESTServiceProvider.getInstance().storeMeasurementResults(results,i);
				}
				Log.i(ApplicationGlobalContext.LOG_TAG, "Results are stored");
			} catch (RestException e) {
				Log.e(ApplicationGlobalContext.LOG_TAG, "Exception while storing results to the central server: "+e.getLocalizedMessage());
				showNotification(NOTIFICATION_ID, "Error storing to server", e.getMessage(), true);
			}
			
			if (SignalChangeCollector.getInstance(this).hasNetworkTypeChanged()) {
				// network technology changed - test inconsistent - repeat!
				// TODO:
			}
		}
		
		showNotification(NOTIFICATION_ID, "Done", successfulCounter+" successful of "+targetProtocolDescriptions.size(), false);
		wakeLock.release();
		
		} catch (Exception e) {
			Log.e(ApplicationGlobalContext.LOG_TAG, e.getLocalizedMessage());
		}
	}
	
	//private boolean performMeasurement(MeasurementServer measurementServer, List<ProtocolDescription> protocolDescriptions, int cycles)
	
	
	private void showNotification(int msgId, String contentTitle, String contentText, boolean isError) {
		msgId=(int)(10000*Math.random());
		Notification.Builder notification = new Notification.Builder(this);
		notification.setContentTitle(contentTitle)
		.setContentText(contentText)
		.setSmallIcon(R.drawable.ic_launcher);
		

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ResultsActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ResultsActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );

		notification.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// first int parameter allows one to update the notification later on
		mNotificationManager.notify(3, notification.getNotification());
	}
	
	/**
	 * Displays and updates the notification with progress bar
	 * @param msgId
	 * @param maxProgress
	 * @param currentProgress
	 * @param indeterminate
	 * @param contentTitle
	 * @param contentText
	 */
	private void showProgressNotification(int msgId, int maxProgress, int currentProgress, boolean indeterminate, String contentTitle, String contentText) {
		Notification.Builder notification = new Notification.Builder(this);
		notification.setContentTitle(contentTitle)
		.setContentText(contentText)
		.setSmallIcon(R.drawable.ic_launcher)
		.setProgress(maxProgress, currentProgress, indeterminate);
		
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, ResultsActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ResultsActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );

		notification.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// first int parameter allows one to update the notification later on
		mNotificationManager.notify(3, notification.getNotification());
	}
	
	private int generateUniqueMsgId() {
		return (int)(10000*Math.random());
	}

}
