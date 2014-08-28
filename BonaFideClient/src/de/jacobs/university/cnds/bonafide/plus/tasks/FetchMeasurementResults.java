package de.jacobs.university.cnds.bonafide.plus.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ExpandableListView;
import de.jacobs.university.cnds.bonafide.plus.R;
import de.jacobs.university.cnds.bonafide.plus.adapter.ResultsAdapter;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementResult;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;

public class FetchMeasurementResults extends AsyncTask<Void, Void, List<MeasurementResult>> {
	/**
	 * Results will be delivered to this interface
	 */
	private FetchMeasurementResultsCallbackInterface callbackInterface;
	private Context context;
	
	public FetchMeasurementResults(Context context, FetchMeasurementResultsCallbackInterface callbackInterface) {
		this.callbackInterface=callbackInterface;
		this.context=context;
	}
	
	protected List<MeasurementResult> doInBackground(Void... input) {
		try {
			return RESTServiceProvider.getInstance().getMeasurementResults();
		} catch (RestException e) {
			Log.e(ApplicationGlobalContext.LOG_TAG, e.getMessage());
			return null;
		}
    }
	
    // onPostExecute returns Measurement Servers to callback.
    @Override
    protected synchronized void onPostExecute(List<MeasurementResult> results) {
    	if (results==null) {
    		this.callbackInterface.measurementResultsRecieved(null);
    		return;
    	}
    	
		HashMap<MeasurementResult, List<MeasurementResult>> children = new HashMap<MeasurementResult, List<MeasurementResult>>();
		
		Iterator<MeasurementResult> iter = results.iterator();
		while (iter.hasNext()) {
			MeasurementResult measurementResult=iter.next();
			ArrayList<MeasurementResult> tmp = new ArrayList<MeasurementResult>();
			tmp.add(measurementResult);
			children.put(measurementResult, tmp);
		}
		
		ResultsAdapter resultsAdapter = new ResultsAdapter(this.context, results, children);
    	
		this.callbackInterface.measurementResultsRecieved(resultsAdapter);
   }
}
