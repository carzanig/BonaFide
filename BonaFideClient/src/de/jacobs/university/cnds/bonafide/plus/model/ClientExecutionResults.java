/*
	Copyright (c) 2012, Vitali Bashko
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met: 

	1. Redistributions of source code must retain the above copyright notice, this
   	list of conditions and the following disclaimer. 
	2. Redistributions in binary form must reproduce the above copyright notice,
   	this list of conditions and the following disclaimer in the documentation
   	and/or other materials provided with the distribution. 

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

	The views and conclusions contained in the software and documentation are those
	of the authors and should not be interpreted as representing official policies, 
	either expressed or implied, of the FreeBSD Project.
*/


package de.jacobs.university.cnds.bonafide.plus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementResult;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;

/**
 * ClientExecutionResults class is used to accumulate the measurement test results (including 
 * the mobile operator information, measurement results for the download and the upload directions,
 * network state changes, signal strength changes)
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class ClientExecutionResults implements Serializable {
	
	private static final long serialVersionUID = 8468766780711200704L;
	
	private String protocolSpecificationName;
	private String uuid;
	private String errorMessage = null;
	private String operatorName;
	private String operator;
	private String country;
	private int cycles;
	private boolean terminated = false;
	private boolean isMobileNetwork;
	private String resultFilePath;
	private MeasurementServer measurementServer;
	private long delay=0;
	private double latitude=MeasurementResult.UNKNOWN_LATITUDE;
	private double longitude=MeasurementResult.UNKNOWN_LONGITUDE;
			
	//private Map<Integer, Boolean> blockedPortsMap;

	@Expose
	@SerializedName("signal_strength")
	private int signalStrength;
	@Expose
	@SerializedName("network_type")
	private String networkType;
	
	
	private List<List<BandwidthPerformance>> clientProtocolPerformance;
	private List<List<BandwidthPerformance>> clientRandomPerformance;
	private List<List<BandwidthPerformance>> serverProtocolPerformance;
	private List<List<BandwidthPerformance>> serverRandomPerformance;
	
	public ClientExecutionResults(int cyclesNumber) {	
		clientProtocolPerformance = new ArrayList<List<BandwidthPerformance>>(cyclesNumber);
		clientRandomPerformance = new ArrayList<List<BandwidthPerformance>>(cyclesNumber);
		serverProtocolPerformance = new ArrayList<List<BandwidthPerformance>>(cyclesNumber);
		serverRandomPerformance = new ArrayList<List<BandwidthPerformance>>(cyclesNumber);
		
		//this.blockedPortsMap = new HashMap<Integer, Boolean>();
	}
	
	public MeasurementServer getMeasurementServer() {
		return measurementServer;
	}

	public void setMeasurementServer(MeasurementServer measurementServer) {
		this.measurementServer = measurementServer;
	}
	
	public int getSignalStrength() {
		return signalStrength;
	}

	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}
	
	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void addClientProtocolPerformanceResults(List<BandwidthPerformance> performances, int cycle) {
		this.clientProtocolPerformance.add(cycle, performances);
	}
	
	public List<BandwidthPerformance> getClientProtocolPerformanceResults(int cycle) {
		return this.clientProtocolPerformance.get(cycle);
	}
	
	public void addClientRandomPerformanceResults(List<BandwidthPerformance> performances, int cycle) {
		this.clientRandomPerformance.add(cycle, performances);
	}
	
	public List<BandwidthPerformance> getClientRandomPerformanceResults(int cycle) {
		return this.clientRandomPerformance.get(cycle);
	}
	
	public void addServerProtocolPerformanceResults(List<BandwidthPerformance> performances, int cycle) {
		this.serverProtocolPerformance.add(cycle, performances);
	}
	
	public List<BandwidthPerformance> getServerProtocolPerformanceResults(int cycle) {
		return this.serverProtocolPerformance.get(cycle);
	}

	public void addServerRandomPerformanceResults(List<BandwidthPerformance> performances, int cycle) {
		this.serverRandomPerformance.add(cycle, performances);
	}
	
	public List<BandwidthPerformance> getServerRandomPerformanceResults(int cycle) {
		return this.serverRandomPerformance.get(cycle);
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

	/**
	 * @return the clientProtocolPerformance
	 */
	public List<List<BandwidthPerformance>> getClientProtocolPerformance() {
		return clientProtocolPerformance;
	}

	/**
	 * @return the clientRandomPerformance
	 */
	public List<List<BandwidthPerformance>> getClientRandomPerformance() {
		return clientRandomPerformance;
	}

	/**
	 * @return the serverProtocolPerformance
	 */
	public List<List<BandwidthPerformance>> getServerProtocolPerformance() {
		return serverProtocolPerformance;
	}

	/**
	 * @return the serverRandomPerformance
	 */
	public List<List<BandwidthPerformance>> getServerRandomPerformance() {
		return serverRandomPerformance;
	}

/*	public void addPortScanResult(Integer port, Boolean result) {
		this.blockedPortsMap.put(port, result);
	}
	
	public Map<Integer, Boolean> getBlockedPortsMap() {
		return blockedPortsMap;
	}
*/
	
	/**
	 * @return the protocolSpecificationName
	 */
	public String getProtocolSpecificationName() {
		return protocolSpecificationName;
	}

	/**
	 * @param protocolSpecificationName the protocolSpecificationName to set
	 */
	public void setProtocolSpecificationName(String protocolSpecificationName) {
		this.protocolSpecificationName = protocolSpecificationName;
	}

	/**
	 * @return the uuid
	 */
	public String getUUID() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUUID(String uuid) {
		this.uuid = uuid;
	}



	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
	

	/**
	 * @return the operatorName
	 */
	public String getOperatorName() {
		return operatorName;
	}


	/**
	 * @param operatorName the operatorName to set
	 */
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	
	/**
	 * @return the cycles
	 */
	public int getCycles() {
		return cycles;
	}

	/**
	 * @param cycles the cycles to set
	 */
	public void setCycles(int cycles) {
		this.cycles = cycles;
	}
	

	/**
	 * @return the terminated
	 */
	public boolean isTerminated() {
		return terminated;
	}

	/**
	 * @param terminated the terminated to set
	 */
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	
	/**
	 * @return the resultFilePath
	 */
	public String getResultFilePath() {
		return resultFilePath;
	}

	/**
	 * @param resultFilePath the resultFilePath to set
	 */
	public void setResultFilePath(String resultFilePath) {
		this.resultFilePath = resultFilePath;
	}
	
	/**
	 * @return the isMobileNetwork
	 */
	public boolean isMobileNetwork() {
		return isMobileNetwork;
	}

	/**
	 * @param isMobileNetwork the isMobileNetwork to set
	 */
	public void setMobileNetwork(boolean isMobileNetwork) {
		this.isMobileNetwork = isMobileNetwork;
	}

	/**
	 * @return the country
	 */
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * this static method is used to parse the measurement results in the download direction
	 * retrieved from the server 
	 * 
	 * @param results
	 * @param content
	 */
	public static void parseServerTestExecutionResults(ClientExecutionResults results, String content) {
		StringTokenizer st = new StringTokenizer(content, "\n");
		int cycle = 0;
		List<BandwidthPerformance> performances = null;
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.startsWith("Cycle")) {
				StringTokenizer tmp = new StringTokenizer(line, " ");
				tmp.nextToken();
				cycle = Integer.valueOf(tmp.nextToken());
				continue;
			}
			
			if (line.equals("End cycle")) {
				results.addServerRandomPerformanceResults(performances, cycle);
				continue;
			}
			
			if (line.equals("Random")) {
				results.addServerProtocolPerformanceResults(performances, cycle);
				performances = new ArrayList<BandwidthPerformance>();
				continue;
			}
			
			if (line.equals("Protocol")) {				
				performances = new ArrayList<BandwidthPerformance>();
				continue;
			}
			
			if (line.contains(" ")) {
				BandwidthPerformance bandwidth = new BandwidthPerformance();			
				StringTokenizer tokens = new StringTokenizer(line, " ");
				bandwidth.setBytesSent(Integer.valueOf(tokens.nextToken()).intValue());
				bandwidth.setRoundTripTime(Long.valueOf(tokens.nextToken()).longValue());
				performances.add(bandwidth);				
			} else {
				BandwidthPerformance performance = new BandwidthPerformance(CompletenessResult.getValueByStringRepresentation(line));
				performances.add(performance);
			}
			

		}
		
	}
}
