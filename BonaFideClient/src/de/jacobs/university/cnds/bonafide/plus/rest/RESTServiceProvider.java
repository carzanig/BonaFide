package de.jacobs.university.cnds.bonafide.plus.rest;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.service.ServiceFinder;

import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultFilter;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultRequest;
import de.jacobs.university.cnds.bonafide.plus.rest.model.DrawableResultsResponse;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementResult;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementResultResponse;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServersResponse;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;


public class RESTServiceProvider {
	private static String serviceUrl="";
	private static String userToken="";
	private static RESTServiceProvider self;
	
	// singleton
	private RESTServiceProvider() {
		// fix for nullpointerexception from Jersey in Android
		ServiceFinder.setIteratorProvider(new AndroidServiceIteratorProvider());
		
		// read central server url from config
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationGlobalContext.getInstance().getAppContext());
		setServiceUrl(sharedPref.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_IP, ""));
		setUserToken(sharedPref.getString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_USER_TOKEN, ""));
	}
	
	public static synchronized RESTServiceProvider getInstance() {
		if (self==null) {
			self=new RESTServiceProvider();
		}
		return self;
	}
	
	public void setServiceUrl(String serviceUrl) {
		RESTServiceProvider.serviceUrl=serviceUrl;
	}
	
	public void setUserToken(String userToken) {
		RESTServiceProvider.userToken=userToken;
	}
	
	/**
	 * This method returns list of Measurement Servers
	 * @return
	 */
	public List<MeasurementServer> getMeasurementServerList() throws RestException {
			try {
				Client client = new Client();
				WebResource webResource = client
				   .resource(serviceUrl).path("measurement-servers").path("list");
				webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
				ClientResponse response = webResource.get(ClientResponse.class);
				
				return MeasurementServersResponse.fromJSON(response.getEntity(String.class)).getServers();
			}
			catch (Exception e) {
				throw new RestException(e.getLocalizedMessage());
			}
	}
	
	/**
	 * This method returns random Measurement Server
	 * @return
	 */
	public List<MeasurementServer> getRandomMeasurementServers() throws RestException {
			List<MeasurementServer> measurementServers=getMeasurementServerList();
			Collections.shuffle(measurementServers);
			return measurementServers;
	}
	
	/**
	 * This method returns list of nearest Measurement Servers
	 * @return
	 */
	public List<MeasurementServer> getNearestMeasurementServerList(double latitude, double longitude) throws RestException {
			try {
				Client client = new Client();
				WebResource webResource = client
				   .resource(serviceUrl).path("measurement-servers").path("list").path(String.valueOf(latitude)).path(String.valueOf(longitude));
				webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
				ClientResponse response = webResource.get(ClientResponse.class);
				
				List<MeasurementServer> measurementServers = MeasurementServersResponse.fromJSON(response.getEntity(String.class)).getServers();
				
				return measurementServers;
			}
			catch (Exception e) {
				throw new RestException(e.getLocalizedMessage());
			}
	}
	
	/**
	 * This method stores measurement results to the central server
	 */
	public void storeMeasurementResults(ClientExecutionResults results, int cycle) throws RestException {
		try {
			// analyze results and obtain statistics
			//Statistics[] statistics = ResultAnalyzer.analyzeResults(results);//do statistical analysis
			//TestResultsPrinter.printTestExecutionResultsAsHTML(results, statictics);//print result to HTML-formatted file
			
			MeasurementResult resultDTO = new MeasurementResult(userToken,results,cycle);
			
			Client client = new Client();
			WebResource webResource = client
			   .resource(serviceUrl).path("measurement-results").path("add");
			webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			
			String resultsJson=resultDTO.toJSON();
			
			ClientResponse response = webResource.post(ClientResponse.class,resultsJson);
			
			MeasurementResultResponse measurementResponse = MeasurementResultResponse.fromJSON(response.getEntity(String.class));
			// update userToken
			if (measurementResponse.getStatus().equals("OK") && measurementResponse.getUserToken()!=null) {
				Log.i(ApplicationGlobalContext.LOG_TAG,"Updating user token to: "+measurementResponse.getUserToken());
				// update properties
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ApplicationGlobalContext.getInstance().getAppContext());
				Editor editor = sharedPref.edit();
				editor.putString(ApplicationGlobalContext.PREFERENCES_KEY_CENTRAL_SERVER_USER_TOKEN, measurementResponse.getUserToken());
				editor.commit();
				// update runtime
				setUserToken(measurementResponse.getUserToken());
			}
		}
		catch (Exception e) {
			throw new RestException(e.getLocalizedMessage());
		}
	}
	/**
	 * Returns drawable results (squares) from the server. If you don't want to apply filter, provide null as an argument
	 * @param filters
	 * @return
	 * @throws RestException
	 */
	public List<MeasurementResult> getMeasurementResults() throws RestException {
		try {
			Client client = new Client();
			WebResource webResource = client
			   .resource(serviceUrl).path("measurement-results").path("list").path(this.userToken);
			webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			ClientResponse response = webResource.get(ClientResponse.class);
			
			String responseString=response.getEntity(String.class);
			
			return MeasurementResultResponse.fromJSON(responseString).getResults();
		} catch (Exception e) {
			throw new RestException(e.getLocalizedMessage());
		}
	}
	
	public DrawableResultsResponse getDrawableResultsResponseForViewport(LatLngBounds bounds, float zoomLevel, List<DrawableResultFilter> activeFilters, boolean filterActive, String targetScope) throws RestException {
		//ArrayList<DrawableResult> res = new ArrayList<DrawableResult>();
		
		DrawableResultRequest request = new DrawableResultRequest();
		
		request.setFiltersActive(filterActive);
		
		if (targetScope!=null) {
			request.setTargetScope(targetScope);
		}
		
		if (activeFilters!=null) {
			Iterator<DrawableResultFilter> filtersIter = activeFilters.iterator();
			while (filtersIter.hasNext()) {
				request.addFilter(filtersIter.next());
			}
		}
		
		//res.add(new DrawableResult(47.4187338, 8.5074062, 47.4504297, 8.4332485, 100));
		try {
			Client client = new Client();
			WebResource webResource = client
			   .resource(serviceUrl).path("measurement-results").path("list-for-viewport").path(String.valueOf(bounds.southwest.latitude)).path(String.valueOf(bounds.southwest.longitude)).path(String.valueOf(bounds.northeast.latitude)).path(String.valueOf(bounds.northeast.longitude)).path(String.valueOf(zoomLevel));
			webResource.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON);
			ClientResponse response = webResource.post(ClientResponse.class,request.toJSON()); // POST because we need to send the request - not possible with GET
			
			return DrawableResultsResponse.fromJSON(response.getEntity(String.class));
		} catch (Exception e) {
			throw new RestException(e.getLocalizedMessage());
		}
	}
}
