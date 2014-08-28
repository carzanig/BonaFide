package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class MeasurementResultResponse {
	@SerializedName("status")
	private String status;
	@SerializedName("status_message")
	private String statusMessage;
	@SerializedName("user_token")
	private String userToken;
	// list available only when fetching results and not while storing a result
	@SerializedName("measurement_results")
	private List<MeasurementResult> results;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusMessage() {
		return statusMessage;
	}
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	public String getUserToken() {
		return userToken;
	}
	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}
	public List<MeasurementResult> getResults() {
		return results;
	}
	public void setResults(List<MeasurementResult> results) {
		this.results = results;
	}
	public static MeasurementResultResponse fromJSON(String json) {
		Gson gson = new GsonBuilder()
		   .setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		MeasurementResultResponse instance = gson.fromJson(json, MeasurementResultResponse.class);
		return instance;
	}
}
