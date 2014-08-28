package de.jacobs.university.cnds.bonafide.plus.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * This class extends the basic onMapChange listener by the onMapIdle listener, which is not available in the V2 of Google Maps API. This listener ignores frequent position changes caused by scrolling the map and fires onMapIdle event after the position is not changed for a specified piece of time.
 * @author Tomas
 *
 */
public class OnMapIdleListener implements OnCameraChangeListener, Runnable {
	/**
	 * Time in ms, after which the map is considered idle (only if position doesn't change)
	 */
	private static final long IDLE_TRESHHOLD=500;
	private OnMapIdleReciever listener;
	private ScheduledFuture scheduledTask;
	
	public OnMapIdleListener(OnMapIdleReciever listener) {
		setOnMapIdleListener(listener);
	}
	
	// internal logic
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);
	private LatLng lastTarget;
	private float lastZoom;

	public void setOnMapIdleListener(OnMapIdleReciever listener) {
		this.listener=listener;
	}
	
	@Override
	public void onCameraChange(CameraPosition position) {
		LatLng currentTarget = position.target;
		float currentZoom = position.zoom;
		
		// process only if viewport changed
		if (lastTarget==null || currentTarget.latitude!=lastTarget.latitude || currentTarget.longitude!=lastTarget.longitude || currentZoom!=lastZoom) {
			if (scheduledTask!=null && !scheduledTask.isDone()) {
				// previously scheduled, cancel and reschedule
				scheduledTask.cancel(false);
			}
			
			lastTarget=currentTarget;
			lastZoom=currentZoom;
			
			scheduledTask = scheduler.schedule(this, IDLE_TRESHHOLD, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * Fired when scheduled task is invoked. It means that at this place the onMapIdle event is fired.
	 * 
	 * Attention: this doesn't invoke callback on the UI thread, so UI operations (e.g. Toast) will fail!
	 */
	@Override
	public void run() {
		if (listener!=null) {
			listener.onMapIdle();
		}
	}
	
}
