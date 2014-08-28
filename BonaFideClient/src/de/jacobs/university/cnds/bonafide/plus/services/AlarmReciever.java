package de.jacobs.university.cnds.bonafide.plus.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

public class AlarmReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// send the notification to the BonafideService 
		Intent notificationIntent = new Intent(context, BonafideService.class);
		notificationIntent.putExtra(BonafideService.BONAFIDE_SERVICE_ACTION_KEY, BonafideService.BONAFIDE_SERVICE_RECIEVE_ALARM);
		context.startService(notificationIntent);
	}

}
