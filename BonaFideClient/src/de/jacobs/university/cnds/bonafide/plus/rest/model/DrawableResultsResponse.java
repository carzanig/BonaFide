package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DrawableResultsResponse {
	@SerializedName("status")
	private String status;
	@SerializedName("status_message")
	private String statusMessage;
	@SerializedName("target_scope")
	private String targetScope;
	@SerializedName("available_target_scopes")
	private List<String> availableTargetScopes;
	@SerializedName("available_filters")
	private List<DrawableResultFilter> availableFilters = new ArrayList<DrawableResultFilter>();
	@SerializedName("applied_filters")
	private List<DrawableResultFilter> appliedFilters = new ArrayList<DrawableResultFilter>();
	@SerializedName("results")
	private List<DrawableResult> results = new ArrayList<DrawableResult>();
	
	public static DrawableResultsResponse fromJSON(String json) {
		Gson gson = new Gson();
		DrawableResultsResponse instance = gson.fromJson(json, DrawableResultsResponse.class);
		return instance;
	}

	public List<DrawableResult> getResults() {
		return results;
	}

	public void setResults(List<DrawableResult> results) {
		this.results = results;
	}

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

	public List<DrawableResultFilter> getAvailableFilters() {
		return availableFilters;
	}

	public void setAvailableFilters(List<DrawableResultFilter> availableFilters) {
		this.availableFilters = availableFilters;
	}
	

	public String getTargetScope() {
		return targetScope;
	}

	public void setTargetScope(String targetScope) {
		this.targetScope = targetScope;
	}

	public List<DrawableResultFilter> getAppliedFilters() {
		return appliedFilters;
	}

	public void setAppliedFilters(List<DrawableResultFilter> appliedFilters) {
		this.appliedFilters = appliedFilters;
	}

	public List<String> getAvailableTargetScopes() {
		return availableTargetScopes;
	}

	public void setAvailableTargetScopes(List<String> availableTargetScopes) {
		this.availableTargetScopes = availableTargetScopes;
	}
	
	
	
}
