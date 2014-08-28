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


package de.jacobs.university.cnds.bonafide.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServerExecutionResults class is used by the server to store the 
 * measurement test results in the download direction.
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class ServerExecutionResults {
	
	private String uuid;	
	private int cyclesTotal;
	private long startTime;
	
	private ConcurrentHashMap<Integer, List<BandwidthPerformance>> serverRandomPerformance;
	private ConcurrentHashMap<Integer, List<BandwidthPerformance>> serverProtocolPerformance;
	
	public ServerExecutionResults(String uuid, int cyclesTotal, long startTime) {
		this.cyclesTotal = cyclesTotal;
		this.startTime = startTime;
		this.uuid = uuid;
		
		serverProtocolPerformance = new ConcurrentHashMap<Integer, List<BandwidthPerformance>>();
		serverRandomPerformance = new ConcurrentHashMap<Integer, List<BandwidthPerformance>>();
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}
	
	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	/**
	 * @return the cyclesTotal
	 */
	public int getCyclesTotal() {
		return cyclesTotal;
	}
	
	/**
	 * @param cyclesTotal the cyclesTotal to set
	 */
	public void setCyclesTotal(int cyclesTotal) {
		this.cyclesTotal = cyclesTotal;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void putBandwidthResult(List<BandwidthPerformance> performance, Integer cycle, boolean isRandomFlow) {
			if (isRandomFlow) {
				serverRandomPerformance.putIfAbsent(cycle, performance);
			} else {
				serverProtocolPerformance.putIfAbsent(cycle, performance);
			}
	}
	
	public List<BandwidthPerformance> getServerProtocolBandwidthPerformance(Integer cycle) {
		return serverProtocolPerformance.get(cycle);
	}

	public List<BandwidthPerformance> getServerRandomBandwidthPerformance(Integer cycle) {
		return serverRandomPerformance.get(cycle);
	}
	
}

