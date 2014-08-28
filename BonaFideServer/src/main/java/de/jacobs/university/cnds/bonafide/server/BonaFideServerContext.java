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


package de.jacobs.university.cnds.bonafide.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.model.ServerExecutionResults;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;
import de.jacobs.university.cnds.bonafide.utils.MeasurementTestGenerator;
import de.jacobs.university.cnds.bonafide.utils.ProtocolDescriptionParser;

/**
 * BonaFideServerContext holds an information that is shared by multiple measurement test executor 
 * threads and the client requests executors (e.g., list of all protocol description files, random byte 
 * array used for injecting randomly generated messages, measurement results in the download direction,
 * etc.). BonaFideServerContext is implemented using a singleton pattern, which allows to create only 
 * one instance of this class. 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class BonaFideServerContext {
	
	private static Logger logger = Logger.getLogger(BonaFideServerContext.class);
	
	private static BonaFideServerContext instance;
	
	private List<Command> measurementTestBody;
	private List<ProtocolDescription> protocolHeaders;
	private byte[] rndBytes;
	private File fileStorage;
	
	private ConcurrentHashMap<String, ServerExecutionResults> executions; 
	
	private BonaFideServerContext(String protocolsFile, String storage) throws IOException {
		logger.debug("Initialize context..");
		measurementTestBody = MeasurementTestGenerator.createMeasurementTestBody();
		protocolHeaders = loadAllProtocolHeaders(protocolsFile);
		executions = new ConcurrentHashMap<String, ServerExecutionResults>();
		initializeRandomByteArray();
		initializeFileStorage(storage);
		logger.debug("Context successfully initialized.");
	}
	
	public static synchronized BonaFideServerContext initializeInstance(String protocolsFile, String storage) throws IOException {
		if (instance == null) {			
			instance = new BonaFideServerContext(protocolsFile, storage);
		}
		return instance;
	}
	
	public static synchronized BonaFideServerContext getInstance() {
		return instance;
	}
	
	/*
	 * private method used for creating the byte array with randomly generated values
	 * which are used for injecting randomly generated messages during the measurement tests 
	 */
	private void initializeRandomByteArray() {
		logger.debug("Generate random buffer..");
		Random random = new Random(System.currentTimeMillis());
		byte[] rndBytes = new byte[GlobalConstants.messages_size[GlobalConstants.messages_size.length - 1]];
		random.nextBytes(rndBytes);
		this.rndBytes = rndBytes;
	}
	
	private void initializeFileStorage(String storage) {
		logger.debug("Initialize file storage..");
		File file = new File(storage);
		
		if (!file.exists()) {
			file.mkdir();
		}
		
		fileStorage = file;
	}
	
	/*
	 * private method that iterates through the list of protocol description files and parsed them  
	 */
	private List<ProtocolDescription> loadAllProtocolHeaders(String protocolsFile) throws IOException {
		logger.debug("Start parsing protocol headers..");
		
		List<ProtocolDescription> list = new ArrayList<ProtocolDescription>();
		
		File file = new File(protocolsFile);
		if (!file.exists()) {
			logger.error("Protocol headers list file " + protocolsFile + " doesn't exist.");
			throw new IOException("Protocol headers list file " + protocolsFile + " doesn't exist.");
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		try {							
			String line = null;			
			while ((line = reader.readLine()) != null) {
				
				if (line.trim().length() == 0) {
					continue;
				}
				
				File description = new File(line);
				if (!description.exists()) {
					logger.error("Protocol header description file " + description + " doesn't exist.");
					throw new IOException("Protocol header description file " + description + " doesn't exist.");
				}
				
				FileReader descriptionReader = new FileReader(description);
				StringBuffer content = new StringBuffer();
				try {
					int c;
					while ((c = descriptionReader.read()) != -1) {
						content.append((char) c);
					}
				} finally {
					descriptionReader.close();
				}				
				
				ProtocolDescription protocolHeader = ProtocolDescriptionParser.parseProtocolHeader(content.toString());
				protocolHeader.setDescriptionFilePath(description.getAbsolutePath());
				list.add(protocolHeader);
			}
	
			logger.debug("Parsing protocol headers done.");
			return list;		
		} finally {
			reader.close();			
		}
	} 

	/**
	 * 
	 * @return the measurementTestBody
	 */
	public synchronized List<Command> getMeasurementTestBody() {
		List<Command> copy = new ArrayList<Command>(measurementTestBody.size()); 
		for (Command command: measurementTestBody) {
			Command cmd = new Command(command);
			copy.add(cmd);
		}
		
		return copy;
	}

	/**
	 * 
	 * @return the protocolHeaders
	 */
	public synchronized List<ProtocolDescription> getProtocolHeaders() {
		
		List<ProtocolDescription> copy = new ArrayList<ProtocolDescription>(protocolHeaders.size());
		for (ProtocolDescription header: protocolHeaders) {
			ProtocolDescription cp = new ProtocolDescription(header);
			copy.add(cp);
		}
		
		return copy;
	}
	
	/**
	 * Returns the list of all available application protocol names separated with "\r\n" delimiter
	 */
	public synchronized String getAllProtocolHeaderNames() {
		StringBuffer sb = new StringBuffer();
		
		for (ProtocolDescription header: protocolHeaders) {
			sb.append(header.getProtocolName());
			sb.append("\r\n");
		}
		sb.append(GlobalConstants.END_OF_MESSAGE);
		sb.append("\r\n");
		
		return sb.toString();
	}
	
	/**
	 * Returns the protocol description instance by the application name
	 * 
	 * @param protocolName
	 * @return
	 */
	public synchronized ProtocolDescription getProtocolHeaderByName(String protocolName) {
		for (ProtocolDescription header: protocolHeaders) {
			if (header.getProtocolName().equals(protocolName)) {
				return new ProtocolDescription(header);
			}				
		}
			
		return null;
	}

	public void addTestExecutionEntity(String uuid, ServerExecutionResults execution) {		
		executions.putIfAbsent(uuid, execution);		
	}
	
	public void addPerformanceResultToTestExecution(String uuid, List<BandwidthPerformance> performance, int cycle, boolean isRandomFlow) {
		ServerExecutionResults testExecution = executions.get(uuid);
		testExecution.putBandwidthResult(performance, cycle, isRandomFlow);
	}
	
	public ServerExecutionResults getTestExecutionResult(String uuid) {
		return executions.get(uuid);
	}
	
	public void cleanTestExecutionResult(String uuid) {
		executions.remove(uuid);
	}

	/**
	 * @return the rndBytes
	 */
	public byte[] getRndBytes() {
		return rndBytes;
	}
	
	public synchronized void storeIncomingFile(String context) throws IOException {
		logger.debug("Storing incoming file..");
		String name = String.valueOf(System.currentTimeMillis());
		File file = new File(fileStorage, name);
		FileWriter writer = new FileWriter(file);		
		writer.write(context);
		writer.flush();
		writer.close();
		logger.info("Storing incoming file done.");
	}
	
}
