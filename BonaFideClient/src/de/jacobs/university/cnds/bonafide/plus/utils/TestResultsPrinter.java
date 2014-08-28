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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import android.os.Environment;
import android.text.format.DateFormat;
import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.rest.model.Statistics;
import de.jacobs.university.cnds.bonafide.plus.utils.ResultAnalyzer.Decision;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;

/**
 * This class provides a static method for printing out the obtained measurement 
 * results into a HTML formatted file.
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class TestResultsPrinter {
	
	public static void printTestExecutionResultsAsHTML(ClientExecutionResults results, Statistics[] statistics) throws IOException {
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()) {
			File folder = new File(root, ApplicationGlobalContext.FOLDER_NAME);
			if (!folder.exists()) {
				if (!folder.mkdir()) {
					throw new IOException("Can't create folder in root external directory");
				}
			}
				
			String format = (String) DateFormat.format("MM-dd-yyyy-hh-mmaa", Calendar.getInstance());
			File output = new File(folder, format + "-" + results.getProtocolSpecificationName());
			if (!output.createNewFile()) {
				throw new IOException("Can't create output file");
			}
			
			FileWriter writer = new FileWriter(output);
			
			writer.write("<html>\n<body>\n");
			writer.write("Protocol Name: ");
			writer.write(results.getProtocolSpecificationName());
			writer.write("<br/>\n");
			writer.write("Execution Date: ");
			writer.write(format);
			writer.write("<br/>\n");
			
			if (results.isMobileNetwork()) {
				writer.write("\nNetwork Type: Mobile");
				writer.write("<br/>\n");
				writer.write("\nNetwork Operator: ");
				writer.write(results.getOperator());
				writer.write("\nNetwork Operator Name: ");
				writer.write(results.getOperatorName());
				writer.write("<br/>\n");
				writer.write("Coutry ISO: \n");
				writer.write(results.getCountry());
				writer.write("<br/>\n");
			} else {
				writer.write("\nNetwork Type: WiFi");
				writer.write("<br/>\n");
			}
			
			
/*			writer.write("<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Port Scanning Results<br/>\n");
			for (Integer port: results.getBlockedPortsMap().keySet()) {
				writer.write("port " + port + " is ");
				if (results.getBlockedPortsMap().get(port).booleanValue()) {
					writer.write("available<br/>\n");
				} else {
					writer.write("blocked<br/>\n");
				}
			}
			writer.write("<br/><br/>\n");
*/			
			if (results.isMobileNetwork()) {
				writer.write("<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Network State changes<br/>\n");
				
				Comparator<String> comparator = new Comparator<String>() {
					@Override
					public int compare(String object1, String object2) {
						return object1.compareTo(object2);
					}				
				};
				
				/*
				ConcurrentHashMap<String, String> networkState = results.getNetworkState();
				
				Set<String> keySet = networkState.keySet();
				
				SortedSet<String> treeSet = new TreeSet<String>(comparator);
				for (String key: keySet) {
					treeSet.add(key);
				}
				
				Iterator<String> iterator = treeSet.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					writer.write(key + "&nbsp;&nbsp;&nbsp;&nbsp;" + networkState.get(key) + "<br/>\n");
				}*/
				
				writer.write("<br/><br/>\n");
				writer.write("<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Signal Strength changes<br/>\n");
				/*ConcurrentHashMap<String, String> signal = results.getSignalStrength();
				keySet = signal.keySet();			

				treeSet = new TreeSet<String>(comparator);
				for (String key: keySet) {
					treeSet.add(key);
				}
								
				iterator = treeSet.iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					writer.write(key + "&nbsp;&nbsp;&nbsp;&nbsp;" + signal.get(key) + "<br/>\n");
				}	*/			
			}
									
			printResultTableAsHTML(writer, results.getServerRandomPerformance(), results.getCycles(), "Download performance(kbps): Random Flow");
			printResultTableAsHTML(writer, results.getServerProtocolPerformance(), results.getCycles(), "Download performance(kbps): Protocol Flow");	
			printResultTableAsHTML(writer, results.getClientRandomPerformance(), results.getCycles(), "Upload performance(kbps): Random Flow");
			printResultTableAsHTML(writer, results.getClientProtocolPerformance(), results.getCycles(), "Upload performance(kbps): Protocol Flow");
			
			
			writer.write("<br/>\n");
			printDecision(writer, statistics[0], true);
			printDecision(writer, statistics[1], false);
			
			writer.write("</body>\n</html>");
			writer.flush();			
			writer.close();
			
			results.setResultFilePath(output.getAbsolutePath());			
		}
	}
	
	private static void printDecision(FileWriter writer, Statistics statistics, boolean upload) throws IOException {
		writer.write("<br/>\n");
		if (upload) {
			writer.write("Upload Direction: ");	
		} else {
			writer.write("Download Direction: ");
		}
		
		if (statistics.getDecisionByCompleteness() == Decision.SHAPING) {
			writer.write("traffic shaping detected");
			return;
		}
		
		Decision decisionByData = statistics.getDecisionByData();
		switch (decisionByData) {
		case NOT_ENOUGH_DATA:
			writer.write("not enough data points");
			return;	
			
		case NO_SHAPING:
			writer.write("no traffic shaping detected");
			break;

		case SHAPING:
			writer.write("traffic shaping detected");
			break;

		case MOST_PROBABLY_NO_SHAPING:
			writer.write("most probably no traffic shaping");
			break;

		case MOST_PROBABLY_SHAPING:
			writer.write("most probably traffic shaping observed");
			break;
			
		default:
			break;
		}
					
		writer.write("<br/>\n\t\t\tMann Whitney U = " + statistics.getU() + "<br/>\n\t\t\t");
		writer.write("Ucritical = " + statistics.getUcritical() + "<br/>\n\t\t\t");		
		writer.write("Random mean throughput = " + statistics.getRmean() + "<br/>\n\t\t\t");
		writer.write("Protocol mean throughput =  " + statistics.getPmean() + "<br/>\n\t\t\t");
		writer.write("Random confidence interval = " + statistics.getRinterval() + "<br/>\n\t\t\t");
		writer.write("Protocol confidence interval = " + statistics.getPinterval() + "<br/>\n\t\t\t");
	}
	
	private static void printResultTableAsHTML(FileWriter writer, List<List<BandwidthPerformance>> collection, int cycles, String tableHeader) throws IOException {
		writer.write("<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		writer.write(tableHeader);
		writer.write("<br/>\n");		
		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Message size(kB)<br/>\n");
		writer.write("<table>\n");
		writer.write("<tr><td>Cycle</td>");				
		for (int i = 0; i < GlobalConstants.messages_size.length; i++) {
			writer.write("<td>");
			writer.write(String.valueOf(GlobalConstants.messages_size[i]/1000));
			writer.write("</td>");
		}
		writer.write("</tr>\n");		
		
		for (int i = 0; i < cycles; i++) {
			writer.write("<tr><td>");
			writer.write(String.valueOf(i+1));
			writer.write("</td>");
			List<BandwidthPerformance> performances = collection.get(i);
			for (BandwidthPerformance performance: performances) {				
				writer.write("<td>");
				if (performance.getTestResult() == CompletenessResult.SUCCESS) {
					BigDecimal value = new BigDecimal(performance.getBytesSent());
					value = value.multiply(new BigDecimal(8000));
					value = value.divide(new BigDecimal(performance.getRoundTripTime()), 0, RoundingMode.CEILING);
					writer.write(value.toString());							
				} else {
					writer.write(CompletenessResult.getStringRepresentation(performance.getTestResult()));
				}
				writer.write("</td>");
			}
			writer.write("</tr>\n");
		}
		writer.write("</table>\n");
	}
		
}
