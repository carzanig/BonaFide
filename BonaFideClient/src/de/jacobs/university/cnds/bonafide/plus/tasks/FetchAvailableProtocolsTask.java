package de.jacobs.university.cnds.bonafide.plus.tasks;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.utils.ServerConnector;

/**
 * Task for fetching measurement servers from central server for use in custom measurement
 * @author Tomas
 *
 */

public class FetchAvailableProtocolsTask extends AsyncTask<MeasurementServer, Void, List<ProtocolDescription>> {
	/**
	 * Results will be delivered to this interface
	 */
	private FetchAvailableProtocolsCallbackInterface callbackInterface;
	
	public FetchAvailableProtocolsTask(FetchAvailableProtocolsCallbackInterface callbackInterface) {
		this.callbackInterface=callbackInterface;
	}
	
	protected List<ProtocolDescription> doInBackground(MeasurementServer... input) {
		try {
			MeasurementServer measurementServer = input[0];
			List<ProtocolDescription> protocolDescriptions = ServerConnector.getProtocolDescriptions(measurementServer.getIp(), measurementServer.getPort());
			return protocolDescriptions;
		} catch (Exception e) {
			return null;
		}
    }
	
    // onPostExecute returns Measurement Servers to callback.
    @Override
    protected synchronized void onPostExecute(List<ProtocolDescription> result) {
    	callbackInterface.availableProtocolsRecieved(result);
   }
}
