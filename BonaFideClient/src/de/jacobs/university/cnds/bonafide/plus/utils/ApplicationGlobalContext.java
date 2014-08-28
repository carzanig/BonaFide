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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.os.Environment;

import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;
import de.jacobs.university.cnds.bonafide.utils.MeasurementTestGenerator;

/**
 * 
 * The single instance of ApplicationGlobalContext is used by BonaFide Provider application to store 
 * shared information between the activities. The ApplicationGlobalContext is implemented using Singleton 
 * design pattern to make sure that only one instance is created. 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class ApplicationGlobalContext {
	
	public static final String FOLDER_NAME = "BonaFideProvider"; 
	public static final String STORAGE_INFO_FILE = ".info";
	
	public static final String LOG_TAG="Bonafide";
	
	public static final String PREFERENCES_KEY_CENTRAL_SERVER_IP = "central_server_url";
	public static final String PREFERENCES_KEY_CENTRAL_SERVER_USER_TOKEN = "central_server_user_token";
	public static final String PREFERENCES_KEY_AUTOMEASUREMENT_ACTIVE = "automeasurement_active";
	public static final String PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_TIME = "automeasurement_treshold_time";
	public static final String PREFERENCES_KEY_AUTOMEASUREMENT_TRESHOLD_MOVEMENT = "automeasurement_treshold_movement";
	public static final String BUNDLE_PROTOCOL_HEADER = "protocol_header";
	public static final String BUNDLE_LOCATION_LATITUDE = "latitude";
	public static final String BUNDLE_LOCATION_LONGITUDE = "longitude";
	public static final String BUNDLE_CYCLES = "cycles";
	public static final String BUNDLE_MEASUREMENT_SERVER = "measurement_server";
	public static final String BUNDLE_PROTOCOL_DESCRIPTION = "protocol_description";
	
	public static int REQUEST_CHANGE_SETTINGS = 2;
	public static int RESULT_CHANGE_SETTINGS = 3;
	
	/*
	 * stores the instance value of application global context
	 */
	private static ApplicationGlobalContext context;
	
	/**
	 * Stores application context (used by tasks and non-context classes)
	 */
	private static Context appContext;
	
	public Context getAppContext() {
		return appContext;
	}

	public void setAppContext(Context appContext) {
		ApplicationGlobalContext.appContext = appContext;
	}

	/*
	 * Stores the map of parsed specification protocol headers. The key value represents the protocol name  
	 */		
	private Map<String, ProtocolDescription> protocolHeaderMap;

	/*
	 * array of byte values used for generating the random flows' payload
	 */
	private byte[] rndBytes;
	
	/*
	 * stores the sequence of commands that are executed during the measurement cycle
	 */	
	private List<Command> measurementTestBody;
	
	/*
	 * maintains the list of already submitted measurement reports in 
	 * order to prevent them upload twice. The list of all submitted results is stored in the
	 * @see {GlobalConstants.STORAGE_INFO_FILE} hidden file on SD-card.  
	 */	
	private List<String> storageInfo;
	
	/*
	 * provides the full path to a folder on SD-card where all measurement reports are stored
	 */	
	private String storageInfoPath;
	
	/*
	 * boolean flag used to inform test executor that test has been terminated by the user 
	 */	
	private boolean terminate;
	
	
	/*
	 * private constructor initialize all field instances
	 */
	private ApplicationGlobalContext() throws IOException {		
		protocolHeaderMap = new HashMap<String, ProtocolDescription>();
		storageInfo = new ArrayList<String>();
		initializeRandomByteArray();
		measurementTestBody = MeasurementTestGenerator.createMeasurementTestBody();
		terminate = false;
		
		File info = createStorageFolder();
		storageInfoPath = info.getAbsolutePath();
		loadStorageInfo(info);
	}
	
	/**
	 * The Singleton method which returns always the same instance of ApplicationGlobalContext. If the object 
	 * has not been initialized yet it does that.
	 */	
	public static synchronized ApplicationGlobalContext getInstance() {
		if (context == null) {
			try {
				context = new ApplicationGlobalContext();
			} catch (IOException e) {
				return null;
			}
		}
		return context;
	}
	
	/**
	 *  The method returns protocol specification header by its name value
	 *  
	 * @param protocolHeaderName
	 * @return the parsed protocol specification header by its name value
	 */	
	public ProtocolDescription getProtocolHeader(String protocolHeaderName) {
		return protocolHeaderMap.get(protocolHeaderName);
	}

	/**
	 * This method adds the parsed protocol specification header to the global context
	 * 
	 * @param protocolHeader
	 */
	public void putProtocolHeader(ProtocolDescription protocolHeader) {
		protocolHeaderMap.put(protocolHeader.getProtocolName(), protocolHeader);
	}	
	
	/*
	 * private method used for checking whether folder for storing measurement results already exists and
	 * creates such if no folder found on SD-card
	 */
	private File createStorageFolder() throws IOException {
		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()) {
			File folder = new File(root, FOLDER_NAME);
			if (!folder.exists()) {
				if (!folder.mkdir()) {
					throw new IOException();
				}										
			}
			
			File info = new File(folder, STORAGE_INFO_FILE);
			if (!info.exists()) {
				if (!info.createNewFile()) {
					throw new IOException();
				}								
			}			
			return info;
		} else {
			throw new IOException();
		}
	}
	
	/*
	 * read information from hidden file about the measurement results that already have been 
	 * submitted to the measurement server
	 */
	private void loadStorageInfo(File info) throws IOException {		
		BufferedReader br = new BufferedReader(new FileReader(info));
		String line = null;
		while ((line = br.readLine()) != null) {
			storageInfo.add(line);
		}
		br.close();
	}

	/*
	 * fills the byte array with random values
	 */
	private void initializeRandomByteArray() {
		Random random = new Random(System.currentTimeMillis());
		byte[] rndBytes = new byte[GlobalConstants.messages_size[GlobalConstants.messages_size.length - 1]];
		random.nextBytes(rndBytes);
		this.rndBytes = rndBytes;
	}

	/**
	 * Returns the array of randomly generated values
	 * 
	 * @return array of random byte values
	 */
	public byte[] getRndBytes() {
		return rndBytes;
	}

	/**
	 * Returns the list of commands that compose the measurement cycle process 
	 * 
	 * @return the sequence of execution commands within a measurement cycle  
	 */
	public List<Command> getMeasurementTestBody() {
		return measurementTestBody;
	}


	/**
	 * Return whether the measurement test has been terminated by user not not, 
	 * and if so returns the "terminate flag" back to the false value
	 * 
	 * @return whether the measurement test has been terminated by user
	 */
	public synchronized boolean isTerminateAndReturnFlag() {
		boolean v = terminate;
		terminate = false;
		return v;
	}

	/**
	 * Set the "terminate value" to provided boolean value
	 * 
	 * @param terminate sets the "terminate flag" to provided boolean value
	 */
	public synchronized void setTerminate(boolean terminate) {
		this.terminate = terminate;
	}

	/**
	 * Returns the list of measurement reports' names that have been already submitted to the measurement server
	 * 
	 * @return the list of measurement reports' names that have been already submitted
	 */
	public List<String> getStorageInfo() {
		return storageInfo;
	}

	/**
	 * Adds the measurement report name to a list of files that have been already submitted to the measurement server
	 * 
	 * @param filePath a full path to a measurement report file 
	 * @throws IOException
	 */
	public synchronized void addFileToStorageInfo(String filePath) throws IOException {
		storageInfo.add(filePath);
		File info = new File(storageInfoPath);
		BufferedWriter writer = new BufferedWriter(new FileWriter(info, true));
		writer.append(filePath);
		writer.append("\n");
		writer.flush();
		writer.close();
	}
	
	
}
