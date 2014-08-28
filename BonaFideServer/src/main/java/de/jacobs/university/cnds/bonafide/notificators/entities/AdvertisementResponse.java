package de.jacobs.university.cnds.bonafide.notificators.entities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AdvertisementResponse {
	@XmlElement(name="status")
	private String status="";
	@XmlElement(name="status_message")
	private String statusMessage="";
	@XmlElement(name="next_advertisement_delay")
	private int nextAdvertisementDelay=30;
	
	public int getNextAdvertisementDelay() {
		return nextAdvertisementDelay;
	}
	public void setNextAdvertisementDelay(int nextAdvertisementDelay) {
		this.nextAdvertisementDelay = nextAdvertisementDelay;
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
}
