package de.jacobs.university.cnds.bonafide.plus.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import de.jacobs.university.cnds.bonafide.plus.R;

public class LocationProviderAvailabilityChecker {
	public static boolean isAnyLocationProviderAvailable(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
		
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	/**
	 * Sends user to the location provider settings if not enabled
	 */
	public static void requestLocationProviderPermissionWhenDisabled(final Context context) {
		if (!isAnyLocationProviderAvailable(context)) {
			// show notification
			new AlertDialog.Builder(context)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle(R.string.alertdialog_location_provider_not_available_title)
		        .setMessage(R.string.alertdialog_location_provider_not_available)
		        .setPositiveButton(R.string.alertdialog_location_provider_not_available_option_enable, new DialogInterface.OnClickListener() {
	
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
		            	Intent callGPSSettingIntent = new Intent(
		                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		                context.startActivity(callGPSSettingIntent);
		            }
	
		        })
	        .setNegativeButton(R.string.alertdialog_location_provider_not_available_option_cancel, null)
	        .show();
		}
	}
}
