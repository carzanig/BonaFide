package de.jacobs.university.cnds.bonafide.plus.services;

import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.tasks.RedrawMeasurementServersTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This is BroadcastReciever, which handles connection state changes. When connection becomes available, this reciever updates the GUI
 * @author Tomas
 *
 */

public class NetworkConnectivityReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
		
		boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
		
		if (isConnected) {
			// redraw measurement servers only if map is shown
			if (FrontendActivity.getFrontendActivity()!=null && FrontendActivity.getFrontendActivity().isVisible()) {
				new RedrawMeasurementServersTask().execute();
				FrontendActivity.getFrontendActivity().startResultsTask();
			}
		}
	}
	
}
