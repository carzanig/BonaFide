package de.jacobs.university.cnds.bonafide.plus.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLngBounds;

import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.rest.RESTServiceProvider;
import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResult;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultFilter;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultsResponse;

public class DrawableResultsTask extends AsyncTask<Void, Void, DrawableResultsResponse> {
	private LatLngBounds bounds;
	private float zoomLevel;
	private List<DrawableResultFilter> activeFilters;
	private String targetScope;
	private boolean filterActive;
	
	public DrawableResultsTask(LatLngBounds bounds, float zoomLevel, List<DrawableResultFilter> activeFilters, boolean filterActive, String targetScope) {
		this.bounds=bounds;
		this.zoomLevel=zoomLevel;
		this.activeFilters=activeFilters;
		this.targetScope=targetScope;
		this.filterActive=filterActive;
	}
	
	protected DrawableResultsResponse doInBackground(Void... input) {
		try {
			return RESTServiceProvider.getInstance().getDrawableResultsResponseForViewport(bounds,zoomLevel,activeFilters,filterActive,targetScope);
		} catch (RestException e) {
			return null;
		}
    }
	
    // onPostExecute re-draws results to map.
    @Override
    protected synchronized void onPostExecute(DrawableResultsResponse resultsResponse) {
    	FrontendActivity frontendActivity = FrontendActivity.getFrontendActivity();
    	if (frontendActivity!=null) {
    		// cleanup
    		frontendActivity.removeAllResults();
    		
    		if (resultsResponse!=null) {
	    		// fill filters into GUI
	    		frontendActivity.setFilters(resultsResponse.getAvailableFilters(), resultsResponse.getAppliedFilters());
	    		// and the same for scopes
	    		frontendActivity.setAvailableTargetScopes(resultsResponse.getAvailableTargetScopes(),resultsResponse.getTargetScope());
	        	
	    		// draw results
	        	Iterator<DrawableResult> iter = resultsResponse.getResults().iterator();
	        	while (iter.hasNext()) {
	        		DrawableResult result = iter.next();
	        		frontendActivity.addResultToMap(result);
	        	}
    		}
    	}
   }
}
