package de.jacobs.university.cnds.bonafide.plus.services;

import de.jacobs.university.cnds.bonafide.plus.R;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SystemBootReciever extends BroadcastReceiver {

	/**
	 * On recieve of the system boot event, this method starts the service, which will perform measurements in background
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Intent service = new Intent(context, BonafideService.class);
			context.startService(service);
		}
	}
	
}
