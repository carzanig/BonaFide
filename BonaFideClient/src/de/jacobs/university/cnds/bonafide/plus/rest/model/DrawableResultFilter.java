package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import de.jacobs.university.cnds.bonafide.plus.activities.FrontendActivity;
import de.jacobs.university.cnds.bonafide.plus.utils.RessourcesUtils;

public class DrawableResultFilter {
	@SerializedName("filter_name")
	String filterName="";
	@SerializedName("filter_options")
	List<String> filterOptions=new ArrayList<String>();
	
	public String getFilterName() {
		return filterName;
	}
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	public List<String> getFilterOptions() {
		return filterOptions;
	}
	public void setFilterOptions(List<String> filterOptions) {
		this.filterOptions = filterOptions;
	}
	public void addFilterOption(String option) {
		this.filterOptions.add(option);
	}
	
	public String getHumanReadableName() {
		return RessourcesUtils.getStringResourceByName(FrontendActivity.getFrontendActivity(), "filters_names_"+this.filterName);
	}
	
	public String toString() {
		return getHumanReadableName();
	}
	
	public boolean equals(Object other) {
		if (other instanceof DrawableResultFilter) {
			return ((DrawableResultFilter)other).getFilterName().equals(this.filterName);
		}
		
		return false;
	}
	
}
