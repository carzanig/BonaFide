package de.jacobs.university.cnds.bonafide.plus.tasks;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;

/**
 * Task for fetching measurement servers from central server for use in custom measurement
 * @author Tomas
 *
 */

public class FetchMeasurementServers extends AsyncTask<Void, Void, List<MeasurementServer>> {
	/**
	 * Results will be delivered to this interface
	 */
	private FetchMeasurementServersCallbackInterface callbackInterface;
	
	public FetchMeasurementServers(FetchMeasurementServersCallbackInterface callbackInterface) {
		this.callbackInterface=callbackInterface;
	}
	
	protected List<MeasurementServer> doInBackground(Void... input) {
		try {
			return RESTServiceProvider.getInstance().getMeasurementServerList();
		} catch (RestException e) {
			return null;
		}
    }
	
    // onPostExecute returns Measurement Servers to callback.
    @Override
    protected synchronized void onPostExecute(List<MeasurementServer> result) {
    	callbackInterface.measurementServersRecieved(result);
   }
}
