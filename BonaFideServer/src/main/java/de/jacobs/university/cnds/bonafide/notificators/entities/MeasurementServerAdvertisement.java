package de.jacobs.university.cnds.bonafide.notificators.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class encapsulates informations about this server. JAXB annotations added to support automatic JSON marshaling.
 * @author Tomas Ludrovan
 *
 */

@XmlRootElement
public class MeasurementServerAdvertisement {
	private String name;
	private int port=4000;
	private double latitude=0.0;
	private double longitude=0.0;
	private String centralServerUri;
	
	public MeasurementServerAdvertisement() {
		
	}
	
	public MeasurementServerAdvertisement(String centralServerUri, String name, int port, double latitude, double longitude) {
		this.name=name;
		this.port=port;
		this.latitude=latitude;
		this.longitude=longitude;
		this.centralServerUri=centralServerUri;
	}
	
	
	public String getCentralServerUri() {
		return centralServerUri;
	}
	public void setCentralServerUri(String centralServerUri) {
		this.centralServerUri = centralServerUri;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
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
}
