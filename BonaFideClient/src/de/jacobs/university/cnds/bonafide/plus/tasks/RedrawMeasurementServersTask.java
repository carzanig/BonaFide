package de.jacobs.university.cnds.bonafide.plus.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;

public class RedrawMeasurementServersTask extends AsyncTask<Void, Void, List<MeasurementServer>> {
	
	public RedrawMeasurementServersTask() {
		
	}
	protected List<MeasurementServer> doInBackground(Void... input) {
		try {
			return RESTServiceProvider.getInstance().getMeasurementServerList();
		} catch (RestException e) {
			return new ArrayList<MeasurementServer>();
		}
    }
	
    // onPostExecute re-draws Measurement Servers to map.
    @Override
    protected synchronized void onPostExecute(List<MeasurementServer> result) {
    	FrontendActivity frontendActivity = FrontendActivity.getFrontendActivity();
    	if (frontendActivity!=null && frontendActivity.isVisible()) {
    		frontendActivity.removeAllMeasurementServerMarkers();
        	
        	Iterator<MeasurementServer> iter = result.iterator();
        	while (iter.hasNext()) {
        		MeasurementServer server = iter.next();
        		frontendActivity.addMeasurementServerToMap(server);
        	}
    	}
   }
}
