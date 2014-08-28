package de.jacobs.university.cnds.bonafide.plus.utils;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

/**
 * This class revieves signal updates and network type updates. It can be started and stopped
 * and provides processed informations.
 * @author Tomas
 *
 */

public class SignalChangeCollector extends PhoneStateListener {
	private static SignalChangeCollector self;
	
	private boolean active=false;
	
    private ArrayList<Integer> signalStrengths=new ArrayList<Integer>();
    private Integer lastSignalStrength=null;
    private Integer networkType=null;
    private boolean networkTypeChanged=false;
    
    TelephonyManager telephonyManager;
    
    private SignalChangeCollector(Context context) {
    	telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    	telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
    
    public static synchronized SignalChangeCollector getInstance(Context context) {
    	if (self==null) {
    		self=new SignalChangeCollector(context);
    	}
    	
    	return self;
    }
    
    /**
     * This method enables signal and network type capturing. Previous data will be deleted.
     */
    public synchronized void startCapture() {
    	signalStrengths=new ArrayList<Integer>();
    	if (lastSignalStrength!=null) {
    		// init by last known strength
    		signalStrengths.add(lastSignalStrength);
    	}
    	networkType=null;
    	networkTypeChanged=false;
    	
    	active=true;
    	
    	// initiate network type
    	networkType=telephonyManager.getNetworkType();
    	
 		telephonyManager.listen(this, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
 		// signal strength is always running, because it only supply changes and not the current strength
 		//telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
    
    public synchronized void stopCapture() {
    	telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
    	telephonyManager.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    	
    	active=false;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
    	super.onSignalStrengthsChanged(signalStrength);
    	lastSignalStrength=signalStrength.getGsmSignalStrength();
    	
    	if (active) {
	    	signalStrengths.add(signalStrength.getGsmSignalStrength());
    	}
    }
    
    @Override
	public void onDataConnectionStateChanged(int state, int networkType) {
    	if (this.networkType==null) {
    		this.networkType=networkType;
    	}
    	else {
    		if (networkType!=this.networkType) {
        		this.networkType=networkType;
        		this.networkTypeChanged=true;
    		}
    	}
	}
    
    /**
     * Returns average signal strength
     * @return
     */
    public int getAverageSignalStrength() {
    	int sum=0;
    	Iterator<Integer> iter = signalStrengths.iterator();
    	while (iter.hasNext()) {
    		sum+=iter.next();
    	}
    		if (signalStrengths.size()==0) return 0;
    	
    	return sum/signalStrengths.size();
    }
    
    public boolean hasNetworkTypeChanged() {
    	return this.networkTypeChanged;
    }
    
    public String getNetworkType() {
    	StringBuffer sb = new StringBuffer();
		
    	if (networkType==null) {
    		return"N/A";
    	}
    	 
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				sb.append("1xRTT");
				break;
			case TelephonyManager.NETWORK_TYPE_CDMA:
				sb.append("CDMA");
				break;
			case TelephonyManager.NETWORK_TYPE_EDGE:
				sb.append("EDGE");
				break;
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				sb.append("EVDO_0");
				break;				
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				sb.append("EVDO_A");
				break;
			case TelephonyManager.NETWORK_TYPE_GPRS:
				sb.append("GPRS");
				break;
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				sb.append("HSDPA");
				break;			
			case TelephonyManager.NETWORK_TYPE_HSPA:
				sb.append("HSPA");
				break;			
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				sb.append("HSUPA");
				break;
			case TelephonyManager.NETWORK_TYPE_IDEN:
				sb.append("IDEN");
				break;			
			case TelephonyManager.NETWORK_TYPE_UMTS:
				sb.append("UMTS");
				break;			
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				sb.append("UNKNOWN");
				break;
		}
		
		return sb.toString();
    }
}
