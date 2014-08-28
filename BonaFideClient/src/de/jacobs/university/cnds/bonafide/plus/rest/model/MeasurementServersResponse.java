package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.ArrayList;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sun.jersey.api.provider.jaxb.XmlHeader;

public class MeasurementServersResponse {
	@SerializedName("status")
	private String status;
	@SerializedName("status_message")
	private String statusMessage;
	@SerializedName("servers")
	private List<MeasurementServer> servers = new ArrayList<MeasurementServer>();
	
	public static MeasurementServersResponse fromJSON(String json) {
		Gson gson = new Gson();
		MeasurementServersResponse instance = gson.fromJson(json, MeasurementServersResponse.class);
		return instance;
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
	public List<MeasurementServer> getServers() {
		return servers;
	}
	public void setServers(List<MeasurementServer> servers) {
		this.servers = servers;
	}
}
