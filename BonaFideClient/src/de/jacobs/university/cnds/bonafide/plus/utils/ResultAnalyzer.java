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


package de.jacobs.university.cnds.bonafide.plus.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.rest.model.Statistics;

/**
 * 
 * Helper class that perform statistical analyzing of previously obtained measurement test results the using two-steps
 * methodology. First the the Mann–Whitney U test runs to check whether measured throughput values for random and
 * protocol flows are comparable. If they don't, the confidence intervals for both flows are calculated and compared.    
 * 
 * @author Vitali Bashko <v.bashko@jacobs-university.de>
 */
public class ResultAnalyzer {
	
	private static ResultAnalyzer instance;
	
	static {
		if (instance == null) {
			instance = new ResultAnalyzer(); 
		}
	}
	
	private ResultAnalyzer() {
	}
	
	public enum Decision {
		SHAPING,
		NO_SHAPING,		
		MOST_PROBABLY_SHAPING,
		MOST_PROBABLY_NO_SHAPING,
		NOT_ENOUGH_DATA;			
	}
	
	/*
	 * Critical Values for the Mann-Whitney U-Test
	 * Level of significance: 5% (P = 0.05) 
	 * 
	 * min measurement results per flow:		3
	 * max measurement results per flow:		15
	 */
	private final static Integer[][] MANN_WHITNEY_VALUES = new Integer[][] {
		{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
		{null, null, null, 		0, 		0,		0,		1,		1,		2,		2,		3,		3,		4,		4,		5,		5},
		{null, null, null, null,		0,		1,		2,		3,		4,		4,		5,		6,		7,		8,		9,		10},
		{null, null, null, null, null,		2,		3,		5,		6,		7,		8,		9,		11,		12,		13,		14},
		{null, null, null, null, null, null,		5,		6,		8,		10,		11,		13,		14,		16,		17,		19},
		{null, null, null, null, null, null, null,		8,		10,		12,		14,		16,		18,		20,		22,		24},
		{null, null, null, null, null, null, null, null,		13,		15,		17,		19,		22,		24,		26,		29},
		{null, null, null, null, null, null, null, null, null,		17,		20,		23,		26,		28,		31,		34},
		{null, null, null, null, null, null, null, null, null, null,		23,		26,		29,		33,		36,		39},
		{null, null, null, null, null, null, null, null, null, null, null,		30,		33,		37,		40,		44},
		{null, null, null, null, null, null, null, null, null, null, null, null,		37,		41,		45,		49},
		{null, null, null, null, null, null, null, null, null, null, null, null, null,		45,		50,		54},
		{null, null, null, null, null, null, null, null, null, null, null, null, null, null,		55,		59},
		{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,		64}
	};
	
	
	/*
	 * Student's-t distribution coefficients for an one-sided critical regions with a confidence of 95%  
	 */
	private final static Double[] STUDENT_COEFFICIENTS = new Double[] {null, 6.314, 2.920, 2.353, 2.132, 2.015, 1.943, 1.895, 1.860, 1.833, 1.812, 1.796, 1.782, 1.771, 1.761, 1.753};
		
	
	
	/**
	 * This method analyze measurement test results, in order to make a decision about the presence of traffic shaping along the path
	 * 
	 * @param results
	 * @return an array Decision[], that contains the decisions about traffic shaping presence in upload and download directions
	 */
	public static Statistics[] analyzeResults(ClientExecutionResults results) {
		
		
		//System.out.println("Upload direction");
		//Analyze measurement result in upload direction
		Statistics upload = analyzeDirection(results, true);
		
		//System.out.println("\nDownload direction");
		//Analyze measurement result in download direction
		Statistics download = analyzeDirection(results, false);
		
		return new Statistics[]{upload, download};
	}
	
	private static Statistics analyzeDirection(ClientExecutionResults results, boolean upload) {
		
		List<BandwidthPerformance> lastProtocolMeasurements = new ArrayList<BandwidthPerformance>();
		List<BandwidthPerformance> lastRandomMeasurements = new ArrayList<BandwidthPerformance>();
		
		if (upload) {
			List<List<BandwidthPerformance>> clientProtocolPerformance = results.getClientProtocolPerformance();
			for (List<BandwidthPerformance> res: clientProtocolPerformance) {
				lastProtocolMeasurements.add(res.get(res.size() - 1));
			}
			
			List<List<BandwidthPerformance>> clientRandomPerformance = results.getClientRandomPerformance();
			for (List<BandwidthPerformance> res: clientRandomPerformance) {
				lastRandomMeasurements.add(res.get(res.size() - 1));
			}			
		} else {
			List<List<BandwidthPerformance>> serverProtocolPerformance = results.getServerProtocolPerformance();
			for (List<BandwidthPerformance> res: serverProtocolPerformance) {
				lastProtocolMeasurements.add(res.get(res.size() - 1));
			}			
			
			List<List<BandwidthPerformance>> serverRandomPerformance = results.getServerRandomPerformance();
			for (List<BandwidthPerformance> res: serverRandomPerformance) {
				lastRandomMeasurements.add(res.get(res.size() - 1));
			}			
		}
		
		Decision byCompleteness = analyzeByCompletenessCriterion(lastProtocolMeasurements, lastRandomMeasurements);
		if (byCompleteness == Decision.SHAPING) {
			Statistics statistics = new Statistics();
			statistics.setDecisionByCompleteness(byCompleteness);
			return statistics;
		}
		
		Statistics statistics = analyzeByThroughputCriterion(lastProtocolMeasurements, lastRandomMeasurements);								
		return statistics;
	}
	
	private static Decision analyzeByCompletenessCriterion(List<BandwidthPerformance> lastProtocolMeasurements, List<BandwidthPerformance> lastRandomMeasurements) {
		
		double randomFailRatio = 0;
		double protocolFailRatio = 0;
		
		for (BandwidthPerformance performance: lastProtocolMeasurements) {
			if (!performance.getTestResult().equals(CompletenessResult.SUCCESS)) {
				protocolFailRatio++;
			}
		}
		
		for (BandwidthPerformance performance: lastRandomMeasurements) {
			if (!performance.getTestResult().equals(CompletenessResult.SUCCESS)) {
				randomFailRatio++;
			}
		}		
		
		protocolFailRatio /= lastProtocolMeasurements.size();
		randomFailRatio /= lastRandomMeasurements.size();
		
		
		if (randomFailRatio < 0.2) {			
			if (protocolFailRatio <= 0.7)	return Decision.MOST_PROBABLY_SHAPING;
			return Decision.SHAPING; 			
		}
		
		return Decision.NOT_ENOUGH_DATA;		
	}
	
	private static Statistics analyzeByThroughputCriterion(List<BandwidthPerformance> lastProtocolMeasurements, List<BandwidthPerformance> lastRandomMeasurements) {
		
		Statistics statistics = new Statistics();		
		List<ThroughputByFlow> observations = new ArrayList<ThroughputByFlow>();
		
		int numProtocolValues = 0;
		int numRandomValues = 0;
				
		double meanProtocol = 0;
		double maxProtocol = 0;
				
		double meanRandom = 0;		
		double maxRandom = 0;	
		
				
		for (BandwidthPerformance performance: lastProtocolMeasurements) {
			if (performance.getTestResult().equals(CompletenessResult.SUCCESS)) {
				ThroughputByFlow throughput = instance.new ThroughputByFlow(false, performance);
				observations.add(throughput);
				numProtocolValues++;
				meanProtocol += throughput.getThroughput();
				if (throughput.getThroughput() > maxProtocol) {
					maxProtocol = throughput.getThroughput();
				}
			}
		}
		
		for (BandwidthPerformance performance: lastRandomMeasurements) {
			if (performance.getTestResult().equals(CompletenessResult.SUCCESS)) {
				ThroughputByFlow throughput = instance.new ThroughputByFlow(true, performance);
				observations.add(throughput);
				numRandomValues++;
				meanRandom += throughput.getThroughput();
				if (throughput.getThroughput() > maxRandom) {
					maxRandom = throughput.getThroughput();
				}
			}
		}
		
		meanProtocol /= numProtocolValues;
		meanRandom /= numRandomValues;
		
		//We need at least 3 success measurements for each flow to compare results, otherwise return NOT_ENOUGH_DATA decision  
		if (numProtocolValues < 3 || numRandomValues < 3) {
			statistics.setDecisionByData(Decision.NOT_ENOUGH_DATA);
			return statistics;
		}
		
		//Sort all performance measurements by RTT
		Collections.sort(observations);
		
		//Iterate through all results and calculate the ranks for both flows
		int randomRank = 0;
		int protocolRank = 0;
		for (int i = 0; i < observations.size(); i++) {
			ThroughputByFlow value = observations.get(i);			
			if (value.isRandom()) {
				randomRank += (i+1);
			} else {
				protocolRank += (i+1);
			}
		}
		
		//Calculate the Mann–Whitney U value		
		int U;
		if (protocolRank > randomRank) {
			U = numProtocolValues*numRandomValues + (numProtocolValues*(numProtocolValues+1))/2 - protocolRank;
		} else {
			U = numProtocolValues*numRandomValues + (numRandomValues*(numRandomValues+1))/2 - randomRank;
		}
		
		//Compare U with a corresponding value from the Mann–Whitney table
		Integer Ucritical = MANN_WHITNEY_VALUES[Math.min(numProtocolValues, numRandomValues)][Math.max(numProtocolValues, numRandomValues)];
		
		if (Ucritical == null) {// check if the value is a null. Should never happen, if so NOT_ENOUGH_DATA is returned, to prevent the NullPointerException throwing
			statistics.setDecisionByData(Decision.NOT_ENOUGH_DATA);
			return statistics;	
		}
		
		//System.out.println("U: " + U);
		//System.out.println("Ucritical: " + Ucritical);
				
		statistics.setU(U);
		statistics.setUcritical(Ucritical);
						
		//Remove values that are out of a confidence interval and recalculate mean values
		double sigma_random = 0;
		double sigma_protocol = 0;

		while (true) {
			for (ThroughputByFlow val: observations) {
				if (val.isRandom()) {
					sigma_random += ((meanRandom - val.getThroughput())*(meanRandom - val.getThroughput()));
				} else {
					sigma_protocol += ((meanProtocol - val.getThroughput())*(meanProtocol - val.getThroughput()));
				}
			}

			sigma_protocol = Math.sqrt(sigma_protocol/(numProtocolValues*(numProtocolValues - 1)));
			sigma_random = Math.sqrt(sigma_random/(numRandomValues*(numRandomValues - 1)));
						
			List<ThroughputByFlow> copy = new ArrayList<ThroughputByFlow>();
			boolean removed = false;
			
			long tmpMeanProtocol = 0;
			long tmpMeanRandom = 0;
			
			int tmpNumProtocolValues = 0;
			int tmpNumRandomValues = 0;
			
			for (ThroughputByFlow val: observations) {
				if (val.isRandom()) {
					if ((val.getThroughput() >= (meanRandom - STUDENT_COEFFICIENTS[numRandomValues]*sigma_random)) 
								&& ((val.getThroughput() <= (meanRandom + STUDENT_COEFFICIENTS[numRandomValues]*sigma_random)))) {
						copy.add(val);
						tmpNumRandomValues++;
						tmpMeanRandom += val.getThroughput();
					} else {
						removed = true;
					}
				} else {
					if ((val.getThroughput() >= (meanProtocol - STUDENT_COEFFICIENTS[numProtocolValues]*sigma_protocol)) 
								&& ((val.getThroughput() <= (meanProtocol + STUDENT_COEFFICIENTS[numProtocolValues]*sigma_protocol)))) {
						copy.add(val);		
						tmpNumProtocolValues++;
						tmpMeanProtocol += val.getThroughput();
					} else {
						removed = true;
					}
				}										
			}
			
			meanProtocol = tmpMeanProtocol/tmpNumProtocolValues;
			meanRandom = tmpMeanRandom/tmpNumRandomValues;
			numProtocolValues = tmpNumProtocolValues;
			numRandomValues = tmpNumRandomValues;
			
			observations = copy;
			if (!removed) {
				break;
			}
		}
		
		statistics.setPmean((int)meanProtocol);
		statistics.setRmean((int)meanRandom);
		statistics.setPinterval("[" + ((int)(meanProtocol - sigma_protocol)) + ";" + ((int)(meanProtocol + sigma_protocol)) + "]");
		statistics.setRinterval("[" + ((int)(meanRandom - sigma_random)) + ";" + ((int)(meanRandom + sigma_random)) + "]");
		
		
		//(U > Ucritical) means that no traffic shaping is observed
		if (U > Ucritical) {
			statistics.setDecisionByData(Decision.NO_SHAPING);
			return statistics;
		}
		
		/*
		 * (If U <= Ucritical) it still does not mean that we observe traffic shaping. To clarify that we need to compare the max and mean values
		 */
		
		//If random and protocol confidence intervals intersect, or protocol values are greater than random - NO_SHAPING		
		if ((meanProtocol >= meanRandom) || (meanProtocol + sigma_protocol >= meanRandom - sigma_random)) {
			statistics.setDecisionByData(Decision.NO_SHAPING);
			return statistics; 	
		}
		
		/*
		 * If "protocol mean" value is more than 80% of "random mean" value,
		 * or maxProtocol value more than left confidence interval limit of random flow 
		 * return MOST_PROBABLY_NO_SHAPING 
		 */		 
		if ((meanProtocol > 0.8*meanRandom) || maxProtocol >= meanRandom - sigma_random) {
			statistics.setDecisionByData(Decision.MOST_PROBABLY_NO_SHAPING);
			return statistics;
		}

		/*
		 * If "protocol mean" value is more than 40% but less than 80% of "random mean" value, 
		 * return MOST_PROBABLY_SHAPING 
		 */		
		if (meanProtocol > 0.6*meanRandom && meanProtocol <= 0.8*meanRandom) {
			statistics.setDecisionByData(Decision.MOST_PROBABLY_SHAPING);
			return statistics;
		}
		
		/*
		 * If "protocol mean" value is less than 40% of "random mean" value, 
		 * return SHAPING 
		 */		
		if (meanProtocol <= 0.6*meanRandom) {
			statistics.setDecisionByData(Decision.SHAPING);
			return statistics;
		}
		
		statistics.setDecisionByData(Decision.NOT_ENOUGH_DATA);
		return statistics;
	}
		
	
	private class ThroughputByFlow implements Comparable<ThroughputByFlow> {
		
		private boolean random;		
		private long throughput;
		
		public ThroughputByFlow(boolean random, BandwidthPerformance performance) {
			this.random = random;
			this.throughput = ((long)8000*performance.getBytesSent())/performance.getRoundTripTime();									
		}

		/**
		 * @return the random
		 */
		public boolean isRandom() {
			return random;
		}

		/**
		 * @return the throughput
		 */
		public long getThroughput() {
			return throughput;
		}

		@Override
		public int compareTo(ThroughputByFlow o) {
			if (this.throughput == o.throughput) return 0;			
			return (this.throughput > o.throughput?	1:	-1);
		}
		
	}

}
