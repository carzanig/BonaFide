package de.jacobs.university.cnds.bonafide.plus.rest.model;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.utils.ApplicationGlobalContext;

public class MeasurementResult {
	public static double UNKNOWN_LATITUDE = 300.0;
	public static double UNKNOWN_LONGITUDE= 300.0;
	
	@SerializedName("user_token")
	private String userToken="";
	
	@SerializedName("operator_name")
	private String operatorName;
	private String operator;
	private String country;
	@SerializedName("is_mobile_network")
	private boolean isMobileNetwork;
	@SerializedName("network_type")
	private String networkType;
	@SerializedName("signal_strength")
	private int signalStrength;
	@SerializedName("measurement_server_id")
	private int measurementServerId;
	@SerializedName("measurement_server_name")
	private String measurementServerName;
	@SerializedName("latitude")
	private double latitude=UNKNOWN_LATITUDE;
	@SerializedName("longitude")
	private double longitude=UNKNOWN_LONGITUDE;
	@SerializedName("measurement_datetime")
	private Date date;
	@SerializedName("latency")
	long latency=0;
	
	@SerializedName("protocol_specification_name")
	private String protocolName;
	@SerializedName("upload_protocol_completness")
	private CompletenessResult uploadProtocolCompletness;
	@SerializedName("upload_protocol_roundtrip_time")
	private long uploadProtocolRoundtripTime;
	@SerializedName("upload_protocol_bytes_sent")
	private int uploadProtocolBytesSent;
	@SerializedName("upload_protocol_bandwidth")
	private long uploadProtocolBandwidth;
	@SerializedName("download_protocol_completness")
	private CompletenessResult downloadProtocolCompletness;
	@SerializedName("download_protocol_roundtrip_time")
	private long downloadProtocolRoundtripTime;
	@SerializedName("download_protocol_bytes_sent")
	private int downloadProtocolBytesSent;
	@SerializedName("download_protocol_bandwidth")
	private long downloadProtocolBandwidth;
	@SerializedName("upload_random_completness")
	private CompletenessResult uploadRandomCompletness;
	@SerializedName("upload_random_roundtrip_time")
	private long uploadRandomRoundtripTime;
	@SerializedName("upload_random_bytes_sent")
	private int uploadRandomBytesSent;
	@SerializedName("upload_random_bandwidth")
	private long uploadRandomBandwidth;
	@SerializedName("download_random_completness")
	private CompletenessResult downloadRandomCompletness;
	@SerializedName("download_random_roundtrip_time")
	private long downloadRandomRoundtripTime;
	@SerializedName("download_random_bytes_sent")
	private int downloadRandomBytesSent;
	@SerializedName("download_random_bandwidth")
	private long downloadRandomBandwidth;
	@SerializedName("error_message")
	private String errorMessage;
	// total bytes send during the test - not only by the last brust
	@SerializedName("upload_total_bytes")
	private int uploadTotalBytes;
	@SerializedName("download_total_bytes")
	private int downloadTotalBytes;
	
	/**
	 * 
	 * @param userToken
	 * @param results
	 * @param cycle identifies the cycle which should be used for bandwidth extraction
	 */
	public MeasurementResult(String userToken,ClientExecutionResults results, int cycle) {
		this.userToken=userToken;
		this.operator=results.getOperator();
		this.operatorName=results.getOperatorName();
		this.country=results.getCountry();
		this.isMobileNetwork=results.isMobileNetwork();
		this.measurementServerId=results.getMeasurementServer().getId();
		this.measurementServerName=results.getMeasurementServer().getName();
		this.latitude=results.getLatitude();
		this.longitude=results.getLongitude();
		this.protocolName=results.getProtocolSpecificationName();
		this.errorMessage=results.getErrorMessage();
		this.operator=results.getOperator();
		this.operatorName=results.getOperatorName();
		this.country=results.getCountry();
		this.signalStrength=results.getSignalStrength();
		this.networkType=results.getNetworkType();
		this.latency=results.getDelay();
		
		this.uploadTotalBytes=0;
		this.downloadTotalBytes=0;
		
		
		// extract bandwidth information
		// upload
		List<List<BandwidthPerformance>> clientProtocolPerformance = results.getClientProtocolPerformance();
		List<BandwidthPerformance> res = clientProtocolPerformance.get(cycle);
		BandwidthPerformance tmpPerformance = res.get(res.size() - 1);
		//this.uploadProtocolBandwidth=tmpPerformance.getRoundTripTime();
		this.uploadProtocolCompletness=tmpPerformance.getTestResult();
		
		this.uploadProtocolBytesSent=tmpPerformance.getBytesSent();
		this.uploadProtocolRoundtripTime=tmpPerformance.getRoundTripTime();
		
		// sum all byte counts
		Iterator<BandwidthPerformance> bandwidthIter=res.iterator();
		while (bandwidthIter.hasNext()) {
			this.uploadTotalBytes+=bandwidthIter.next().getBytesSent();
		}
		
		List<List<BandwidthPerformance>> clientRandomPerformance = results.getClientRandomPerformance();
		res = clientRandomPerformance.get(cycle);
		tmpPerformance = res.get(res.size() - 1);
		//this.uploadRandomBandwidth=tmpPerformance.getRoundTripTime();
		this.uploadRandomCompletness=tmpPerformance.getTestResult();
		
		this.uploadRandomBytesSent=tmpPerformance.getBytesSent();
		this.uploadRandomRoundtripTime=tmpPerformance.getRoundTripTime();
		
		// sum all byte counts
		bandwidthIter=res.iterator();
		while (bandwidthIter.hasNext()) {
			this.uploadTotalBytes+=bandwidthIter.next().getBytesSent();
		}

		// download
		List<List<BandwidthPerformance>> serverProtocolPerformance = results.getServerProtocolPerformance();
		res = serverProtocolPerformance.get(cycle);
		if (res.size()>=1) {
			tmpPerformance = res.get(res.size() - 1);
			//this.downloadProtocolBandwidth=tmpPerformance.getRoundTripTime();
			this.downloadProtocolCompletness=tmpPerformance.getTestResult();
			
			this.downloadProtocolBytesSent=tmpPerformance.getBytesSent();
			this.downloadProtocolRoundtripTime=tmpPerformance.getRoundTripTime();
		}
		
		// sum all byte counts
		bandwidthIter=res.iterator();
		while (bandwidthIter.hasNext()) {
			this.downloadTotalBytes+=bandwidthIter.next().getBytesSent();
		}

		List<List<BandwidthPerformance>> serverRandomPerformance = results.getServerRandomPerformance();
		res = serverRandomPerformance.get(cycle);
		if (res.size()>=1) {
			tmpPerformance = res.get(res.size() - 1);
			//this.downloadRandomBandwidth=tmpPerformance.getRoundTripTime();
			this.downloadRandomCompletness=tmpPerformance.getTestResult();
			
			this.downloadRandomBytesSent=tmpPerformance.getBytesSent();
			this.downloadRandomRoundtripTime=tmpPerformance.getRoundTripTime();
		}

		// sum all byte counts
		bandwidthIter=res.iterator();
		while (bandwidthIter.hasNext()) {
			this.downloadTotalBytes+=bandwidthIter.next().getBytesSent();
		}
		
	}
	
	public MeasurementResult() {
		
	}
	
	
	
	
	
	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public long getLatency() {
		return latency;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public boolean isMobileNetwork() {
		return isMobileNetwork;
	}

	public void setMobileNetwork(boolean isMobileNetwork) {
		this.isMobileNetwork = isMobileNetwork;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public int getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	

	public int getMeasurementServerId() {
		return measurementServerId;
	}

	public void setMeasurementServerId(int measurementServerId) {
		this.measurementServerId = measurementServerId;
	}

	public String getMeasurementServerName() {
		return measurementServerName;
	}

	public void setMeasurementServerName(String measurementServerName) {
		this.measurementServerName = measurementServerName;
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

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
	
	
	
	

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public CompletenessResult getUploadProtocolCompletness() {
		return uploadProtocolCompletness;
	}

	public void setUploadProtocolCompletness(
			CompletenessResult uploadProtocolCompletness) {
		this.uploadProtocolCompletness = uploadProtocolCompletness;
	}

	public long getUploadProtocolRoundtripTime() {
		return uploadProtocolRoundtripTime;
	}

	public void setUploadProtocolRoundtripTime(long uploadProtocolRoundtripTime) {
		this.uploadProtocolRoundtripTime = uploadProtocolRoundtripTime;
	}

	public int getUploadProtocolBytesSent() {
		return uploadProtocolBytesSent;
	}

	public void setUploadProtocolBytesSent(int uploadProtocolBytesSent) {
		this.uploadProtocolBytesSent = uploadProtocolBytesSent;
	}

	public CompletenessResult getDownloadProtocolCompletness() {
		return downloadProtocolCompletness;
	}

	public void setDownloadProtocolCompletness(
			CompletenessResult downloadProtocolCompletness) {
		this.downloadProtocolCompletness = downloadProtocolCompletness;
	}

	public long getDownloadProtocolRoundtripTime() {
		return downloadProtocolRoundtripTime;
	}

	public void setDownloadProtocolRoundtripTime(long downloadProtocolRoundtripTime) {
		this.downloadProtocolRoundtripTime = downloadProtocolRoundtripTime;
	}

	public int getDownloadProtocolBytesSent() {
		return downloadProtocolBytesSent;
	}

	public void setDownloadProtocolBytesSent(int downloadProtocolBytesSent) {
		this.downloadProtocolBytesSent = downloadProtocolBytesSent;
	}

	public CompletenessResult getUploadRandomCompletness() {
		return uploadRandomCompletness;
	}

	public void setUploadRandomCompletness(
			CompletenessResult uploadRandomCompletness) {
		this.uploadRandomCompletness = uploadRandomCompletness;
	}

	public long getUploadRandomRoundtripTime() {
		return uploadRandomRoundtripTime;
	}

	public void setUploadRandomRoundtripTime(long uploadRandomRoundtripTime) {
		this.uploadRandomRoundtripTime = uploadRandomRoundtripTime;
	}

	public int getUploadRandomBytesSent() {
		return uploadRandomBytesSent;
	}

	public void setUploadRandomBytesSent(int uploadRandomBytesSent) {
		this.uploadRandomBytesSent = uploadRandomBytesSent;
	}

	public CompletenessResult getDownloadRandomCompletness() {
		return downloadRandomCompletness;
	}

	public void setDownloadRandomCompletness(
			CompletenessResult downloadRandomCompletness) {
		this.downloadRandomCompletness = downloadRandomCompletness;
	}

	public long getDownloadRandomRoundtripTime() {
		return downloadRandomRoundtripTime;
	}

	public void setDownloadRandomRoundtripTime(long downloadRandomRoundtripTime) {
		this.downloadRandomRoundtripTime = downloadRandomRoundtripTime;
	}

	public int getDownloadRandomBytesSent() {
		return downloadRandomBytesSent;
	}

	public void setDownloadRandomBytesSent(int downloadRandomBytesSent) {
		this.downloadRandomBytesSent = downloadRandomBytesSent;
	}

	public long getUploadProtocolBandwidth() {
		return uploadProtocolBandwidth;
	}

	public void setUploadProtocolBandwidth(long uploadProtocolBandwidth) {
		this.uploadProtocolBandwidth = uploadProtocolBandwidth;
	}

	public long getDownloadProtocolBandwidth() {
		return downloadProtocolBandwidth;
	}

	public void setDownloadProtocolBandwidth(long downloadProtocolBandwidth) {
		this.downloadProtocolBandwidth = downloadProtocolBandwidth;
	}

	public long getUploadRandomBandwidth() {
		return uploadRandomBandwidth;
	}

	public void setUploadRandomBandwidth(long uploadRandomBandwidth) {
		this.uploadRandomBandwidth = uploadRandomBandwidth;
	}

	public long getDownloadRandomBandwidth() {
		return downloadRandomBandwidth;
	}

	public void setDownloadRandomBandwidth(long downloadRandomBandwidth) {
		this.downloadRandomBandwidth = downloadRandomBandwidth;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	

	public int getUploadTotalBytes() {
		return uploadTotalBytes;
	}

	public void setUploadTotalBytes(int uploadTotalBytes) {
		this.uploadTotalBytes = uploadTotalBytes;
	}

	public int getDownloadTotalBytes() {
		return downloadTotalBytes;
	}

	public void setDownloadTotalBytes(int downloadTotalBytes) {
		this.downloadTotalBytes = downloadTotalBytes;
	}

	public String toJSON() {
		Gson gson = new GsonBuilder()
		   .setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	    
		String json = gson.toJson(this);
		return json;
	}
}
