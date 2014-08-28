/**
 * Model representing a measurement server
 */
package de.jacobs.university.cnds.bonafide.plus.rest.model;


import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class MeasurementServer implements Serializable {
	private static final long serialVersionUID = -3337790860182691523L;
	
	@SerializedName("id")
	private int id;
	@SerializedName("ip")
	private String ip;
	@SerializedName("name")
	private String name="";
	@SerializedName("port")
	private int port;
	@SerializedName("latitude")
	private double latitude=0;
	@SerializedName("longitude")
	private double longitude=0;
	
	// position can be retrieved by the getter method getPosition
	//private LatLng position;
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public boolean equals(Object other) {
		if (other instanceof MeasurementServer) {
			MeasurementServer otherServer = (MeasurementServer) other;
			if (otherServer.getIp().equals(this.getIp())
					&& otherServer.getLongitude()==this.getLongitude()
					&& otherServer.getLatitude()==this.getLatitude()
					&& otherServer.getName().equals(this.getName())
					&& otherServer.getPort()==this.getPort()) {
				return true;
			}
		}
		
		return false;
	}
	
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public LatLng getPosition() {
		return new LatLng(latitude, longitude);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		return this.name;
	}
}
