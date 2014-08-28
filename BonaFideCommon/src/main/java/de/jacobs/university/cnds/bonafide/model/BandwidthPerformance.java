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

import java.io.Serializable;

import javax.xml.ws.ServiceMode;

import com.google.gson.annotations.SerializedName;

/**
 * BandwidthPerformance class instances are used to store the throughput measurement results.  
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class BandwidthPerformance implements Serializable {
	
	
	private static final long serialVersionUID = 5067944127275657795L;
	
	/*
	 * this field stores the measured round trip time value in microseconds
	 */
	@SerializedName("round_trip_time")
	private long roundTripTime;
	
	/*
	 * this field stores the size of a bulk message used to traverse the network 
	 */
	@SerializedName("bytes_sent")
	private int bytesSent;
	
	/*
	 *  this field stores the measurement completeness value 
	 */
	private CompletenessResult testResult = CompletenessResult.SUCCESS;
	
	public BandwidthPerformance() {
		
	}
	
	public BandwidthPerformance(CompletenessResult testResult) {
		this.testResult = testResult;
	}
	
	/**
	 * Returns the measured round trip time value
	 * @return the roundTripTime
	 */
	public long getRoundTripTime() {
		return roundTripTime;
	}
	
	/**
	 * Sets the measured round trip time value
	 * @param roundTripTime the roundTripTime to set
	 */
	public void setRoundTripTime(long roundTripTime) {
		this.roundTripTime = roundTripTime;
	}
	
	/**
	 * Returns the size of a bulk message used to traverse the network 
	 * @return the bytesSent
	 */
	public int getBytesSent() {
		return bytesSent;
	}
	
	/**
	 * Stores the size of a bulk message used to traverse the network 
	 * @param bytesSent the bytesSent to set
	 */
	public void setBytesSent(int bytesSent) {
		this.bytesSent = bytesSent;
	}

	/**
	 * Returns the measurement completeness value
	 * @return the testResult
	 */
	public CompletenessResult getTestResult() {
		return testResult;
	}		
	
	
	
}
