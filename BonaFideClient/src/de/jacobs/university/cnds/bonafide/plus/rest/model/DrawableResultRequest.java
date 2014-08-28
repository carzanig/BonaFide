package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class DrawableResultRequest {
	@SerializedName("filters_active")
	private boolean filtersActive;
	@SerializedName("apply_filters")
	private List<DrawableResultFilter> applyFilters = new ArrayList<DrawableResultFilter>();
	@SerializedName("target_scope")
	private String targetScope="";
	
	public void addFilter(DrawableResultFilter filter) {
		this.applyFilters.add(filter);
	}
	
	public List<DrawableResultFilter> getApplyFilters() {
		return applyFilters;
	}

	public void setApplyFilters(List<DrawableResultFilter> applyFilters) {
		this.applyFilters = applyFilters;
	}



	public boolean isFiltersActive() {
		return filtersActive;
	}

	public void setFiltersActive(boolean filtersActive) {
		this.filtersActive = filtersActive;
	}

	public String getTargetScope() {
		return targetScope;
	}



	public void setTargetScope(String targetScope) {
		this.targetScope = targetScope;
	}



	public String toJSON() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
