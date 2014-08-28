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
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store the application protocol description.
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 */
public class ProtocolDescription implements Serializable {

	private static final long serialVersionUID = 1849865124300889885L;
	
	/*
	 * defines the port number used for running protocol flows
	 */
	private int PFport;
	
	/*
	 * defines the port number used for running random flows
	 */
	private int RFport;	
	
	/*
	 * defines the application protocol name
	 */
	private String protocolName;
	
	/*
	 * stores the full path to the protocol description file (used by the server component only) 
	 */
	private String descriptionFilePath;	

	/*
	 * stores the list of requests defined by the protocol description file 
	 */
	private List<Command> requests;
	
	/*
	 * stores the list of responses defined by the protocol description file 
	 */
	private List<Command> responses;
	
	//private List<Integer> blockedPorts;
	
	public ProtocolDescription() {
		requests = new ArrayList<Command>();
		responses = new ArrayList<Command>();
	}
	
	
	/**
	 * This constructor makes a deep copy of the provided ProtocolDescription object. The deep copy is required for supporting the thread-safe 
	 * measurement test execution on the server component 
	 * 
	 * @param protocolDescription
	 */
	public ProtocolDescription(ProtocolDescription protocolDescription) {
		this.PFport = protocolDescription.PFport;
		this.RFport = protocolDescription.RFport;
		this.protocolName = protocolDescription.protocolName;
		this.descriptionFilePath = protocolDescription.descriptionFilePath;
		this.requests = new ArrayList<Command>();
		this.responses = new ArrayList<Command>();
		
/*		if (protocolHeader.blockedPorts != null) {
			List<Integer> copy = new ArrayList<Integer>(protocolHeader.blockedPorts.size());
			for (Integer val: protocolHeader.blockedPorts) {
				copy.add(val);
			}
			this.blockedPorts = copy;	
		}*/
		
		for (Command request: protocolDescription.requests) {
			Command r = new Command(request);
			this.requests.add(r);
		}

		for (Command response: protocolDescription.responses) {
			Command r = new Command(response);
			this.responses.add(r);
		}
	}
	
	
	/**
	 * Returns the port number used for running protocol flows
	 * 
	 * @return the port1
	 */
	public int getPFPort() {
		return PFport;
	}
	
	/**
	 * Sets the port number used for running protocol flows
	 * 
	 * @param port1 
	 */
	public void setPFPort(int PFport) {
		this.PFport = PFport;
	}
	
	/**
	 * Returns the port number used for running random flows
	 * 
	 * @return the port2
	 */
	public int getRFPort() {
		return RFport;
	}
	
	/**
	 * Sets the port number used for running random flows
	 * 
	 * @param port2 
	 */
	public void setRFPort(int RFport) {
		this.RFport = RFport;
	}
	
	/**
	 * Returns the application protocol name
	 *  
	 * @return
	 */
	public String getProtocolName() {
		return protocolName;
	}
	
	/**
	 * Sets the application protocol name
	 * 
	 * @param protocolName
	 */
	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}
			
	/**
	 * Returns the absolute path for a protocol description file
	 * (used by the server component only)
	 * 
	 * @return
	 */
	public String getDescriptionFilePath() {
		return descriptionFilePath;
	}
	
	/**
	 * Sets the absolute path for a protocol description file
	 * (used by the server component only)
	 * @param descriptionFilePath
	 */
	public void setDescriptionFilePath(String descriptionFilePath) {
		this.descriptionFilePath = descriptionFilePath;
	}	
	
	/**
	 * Adds the request command defined by the protocol description file
	 * 
	 * @param request
	 */
	public void addRequest(Command request) {
		this.requests.add(request);
	}

	/**
	 * Adds the response command defined by the protocol description file
	 * 
	 * @param response
	 */
	public void addResponse(Command response) {
		this.responses.add(response);
	}
	
	/**
	 * Returns the list of requests defined by the protocol description file
	 * 
	 * @return
	 */
	public List<Command> getRequests() {		
		return this.requests;
	}

	/**
	 * Returns the list of responses defined by the protocol description file
	 * 
	 * @return
	 */
	public List<Command> getResponses() {
		return this.responses;
	}
	
	/**
	 * Thread safe method that returns the copy of list of requests defined by the protocol description file
	 * (used by the server component only)
	 * 
	 * @return 
	 */
	public List<Command> getTSRequests() {
		List<Command> res = new ArrayList<Command>();
		
		for (Command request: this.requests) {
			Command r = new Command(request);
			res.add(r);
		}
		
		return res;
	}
	
	/**
	 * Thread safe method that returns the copy of list of responses defined by the protocol description file 
	 * (used by the server component only)
	 * 
	 * @return 
	 */
	public List<Command> getTSResponses() {
		List<Command> res = new ArrayList<Command>();
		
		for (Command response: this.responses) {
			Command r = new Command(response);
			res.add(r);
		}
		
		return res;
	}
	
/*	
	public List<Integer> getBlockedPorts() {
		return blockedPorts;
	}
	
	
	public void setBlockedPorts(List<Integer> blockedPorts) {
		this.blockedPorts = blockedPorts;
	}
*/	
	
}
