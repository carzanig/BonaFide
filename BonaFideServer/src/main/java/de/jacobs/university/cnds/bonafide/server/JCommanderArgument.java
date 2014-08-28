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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;

import com.beust.jcommander.Parameter;

/**
 * 
 * JCommanderArgument uses jcommander library in order to parse the server input parameters
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class JCommanderArgument {
	
	@Parameter(names = {"-p", "--port"}, 
						description = "Port Number used to run main socket",
						required = false)
	protected Integer portNumber;
	
        @Parameter(names = {"-a", "--address"},
	                                        description = "IPv4 Address used to run main socket",
	                                        required = false)
        protected String ipAddr;

	// required
	@Parameter(names = {"-h", "--centralserverurl"},
						description = "(required) Path to the central server service",
						required = false)
	protected String centralServerUrl;
	
	// required
	@Parameter(names = {"-s", "--storage"},
						description = "(required) path to a local folder to store submitted measurement results",
						required = false)
	protected String storage;
	
	// required
	@Parameter(names = {"-l", "--list"},
						description = "(required) path to file that contains list of protocols to load",
						required = false)
	protected String list;
	
	@Parameter(names = {"-n", "--name"},
			description = "name of this measurement server",
			required = false)
	protected String name;
	
	// required
	@Parameter(names = {"--latitude"},
			description = "(required) latitude of this measurement server",
			required = false)
	protected Double latitude;
	
	// required
	@Parameter(names = {"--longitude"},
			description = "(required) latitude of this measurement server",
			required = false)
	protected Double longitude;
	
	@Parameter(names = {"-c", "--config"},
			description = "path to configuration file. Default is bonafide.conf. Loaded parameters can be overwritten by command-line arguments.",
			required = false)
	protected String configPath="bonafide.conf";
	
	@Parameter(names = {"-v", "--verbose"}, 
						description = "define level of logging. Possible values sorted by priority: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE. INFO is default value.",						
						required = false)
	protected String verbose = null;
	
	/**
	 * Checks if all required parameters are present.
	 * @return true if all config parameters present, false otherwise
	 */
	protected boolean isConfigComplete() {
		return (this.centralServerUrl!=null && this.storage!=null && this.list!=null && this.latitude!=null && this.longitude!=null);
	}
	
	protected Level getLogLevel() {
		if (verbose == null) {
			return Level.INFO;
		}
		if (verbose.equals("OFF")) {
			return Level.OFF;
		}
		if (verbose.equals("FATAL")) {
			return Level.FATAL;
		}		
		if (verbose.equals("ERROR")) {
			return Level.ERROR;
		}
		if (verbose.equals("WARN")) {
			return Level.WARN;
		}		
		if (verbose.equals("INFO")) {
			return Level.INFO;
		}
		if (verbose.equals("DEBUG")) {
			return Level.DEBUG;
		}
		if (verbose.equals("TRACE")) {
			return Level.TRACE;
		}
		return null;
	}
	
	/**
	 * This method loads all parameters from the config file and should be run after command-line parameters are parsed by JCommander. When configuration parameter already provided by command-line argument, it is not overwritten (command-line arguments have priority).
	 */
	protected void appendConfigFromFile() {
		try {
			FileInputStream is = new FileInputStream(this.configPath);
			Properties props = new Properties();
			props.load(is);
			is.close();
			
			if (this.name==null) {
				this.name=props.getProperty("name");
			}
			if (this.portNumber==null) {
				try {
					this.portNumber=Integer.parseInt(props.getProperty("port"));
				} catch (NumberFormatException ex) {
					// bad port number. Let it be null.
				}
			}
			if (this.ipAddr==null) {
				this.ipAddr=props.getProperty("addr");
			}
			if (this.list==null) {
				this.list=props.getProperty("list");
			}
			if (this.storage==null) {
				this.storage=props.getProperty("storage");
			}
			if (this.verbose==null) {
				this.verbose=props.getProperty("verbose");
			}
			if (this.centralServerUrl==null) {
				this.centralServerUrl=props.getProperty("centralserverurl");
			}
			if (this.latitude==null) {
				try {
					this.latitude=Double.parseDouble(props.getProperty("latitude"));
				} catch (NumberFormatException ex) {
					// bad port number. Let it be null.
				}
			}
			if (this.longitude==null) {
				try {
					this.longitude=Double.parseDouble(props.getProperty("longitude"));
				} catch (NumberFormatException ex) {
					// bad port number. Let it be null.
				}
			}
		} catch (IOException e) {
			// skip loading from file
			System.err.println("Reading configuration from "+this.configPath+" failed.");
		}
	}

}
