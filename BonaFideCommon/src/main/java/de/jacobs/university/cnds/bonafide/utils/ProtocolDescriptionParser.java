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


package de.jacobs.university.cnds.bonafide.utils;

import java.util.StringTokenizer;

import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.Command.CommandType;
import de.jacobs.university.cnds.bonafide.model.Command.ExecutionSide;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;

/**
 * 
 * Utility class used for parsing the content of a protocol description file 
 * that defines the application protocol to test
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 */
public class ProtocolDescriptionParser {
	
	private static String PROTOCOL_TAG = "protocol";
	private static String PF_PORT_TAG = "PFport";
	private static String RF_PORT_TAG = "RFport";
	//private static String BLOCKED_PORTS_TAG = "blocked_ports";	

	private static String REQUEST_TAG = "request";
	private static String RESPONSE_TAG = "response";
	
	private static String BYTE_TAG  = "byte";
	private static String STRING_TAG = "string";
	private static String REPBYTE_TAG = "repbyte";
	private static String RNDBYTE_TAG = "rndbyte";
	
	
	/**
	 * Parses the provided content of a protocol specification header that has been downloaded form the measurement server
	 * 
	 *  the general structure of protocol description file looks like on example below
	 *  
	 *  	protocol <application protocol name>
	 * 		port1 <the destination port number for measuring the Protocol flow performance>
	 *		port2 <the destination port number for measuring the Random flow performance>
	 *		
	 *		request <string()*, byte()*>
	 *		response <string()*, byte()*>
	 *  
	 *  (the set of string() or byte() operators that define the content of)
	 *  
	
	

	 *  
	 * 
	 * @param content
	 * @return the ProtocolHeader object instance of a corresponding protocol specification header
	 */
	public static ProtocolDescription parseProtocolHeader(String content) {
		ProtocolDescription header = new ProtocolDescription();
		
		StringTokenizer tokenizer = new StringTokenizer(content, "\n");
		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			
			if (line.startsWith(PROTOCOL_TAG)) {
				header.setProtocolName(line.substring(PROTOCOL_TAG.length() + 1, line.length()));
				continue;
			}
			
			if (line.startsWith(PF_PORT_TAG)) {
				header.setPFPort(Integer.valueOf(line.substring(PF_PORT_TAG.length() + 1, line.length())));
				continue;
			}
			
			if (line.startsWith(RF_PORT_TAG)) {
				header.setRFPort(Integer.valueOf(line.substring(RF_PORT_TAG.length() + 1, line.length())));
				continue;
			}
						
/*			if (line.startsWith(BLOCKED_PORTS_TAG)) {						
				List<Integer> ports = new ArrayList<Integer>();				
				StringTokenizer segment = new StringTokenizer(line.substring(BLOCKED_PORTS_TAG.length() + 1, line.length()), ",");
				while (segment.hasMoreTokens()) {
					String element = segment.nextToken();
					if (element.contains("-")) {						
						Integer start_port = Integer.valueOf(element.substring(0, element.indexOf("-")));
						Integer end_port = Integer.valueOf(element.substring(element.indexOf("-") + 1, element.length()));
						for (Integer port = start_port; port <= end_port; port++) {
							ports.add(port);
						}
					} else {
						ports.add(Integer.valueOf(element));
					}
				}
				header.setBlockedPorts(ports);
				continue;
			}*/

			if (line.startsWith(REQUEST_TAG)) {
				Command command = parseCommand(line);
				//header.setRequest(command);
				header.addRequest(command);				
			}
			
			if (line.startsWith(RESPONSE_TAG)) {
				Command command = parseCommand(line);
				//header.setResponse(command);
				header.addResponse(command);
			}
		}
					
		return header;
	}
	
	/*
	 * private static method used for parsing the "command" content 
	 */
	private static Command parseCommand(String content) {		
		Command command = new Command();
		
		if (content.startsWith(REQUEST_TAG)) {
			command.setType(CommandType.SEND_COMMAND);
			command.setExecutionSide(ExecutionSide.UNDEFINED);
			content = content.substring(REQUEST_TAG.length() + 1, content.length());
		}
		
		if (content.startsWith(RESPONSE_TAG)) {
			command.setType(CommandType.SEND_COMMAND);
			command.setExecutionSide(ExecutionSide.UNDEFINED);
			content = content.substring(RESPONSE_TAG.length() + 1, content.length());
		}
		
		byte[] message = new byte[200];
		int pos = 0;
		int max_capasity = 200;
		
		while (content.length() > 0) {
			if (content.startsWith(BYTE_TAG)) {				
				content = content.substring(BYTE_TAG.length() + 1, content.length());
				int end = content.indexOf(")");
				byte value = Integer.valueOf(content.substring(0, end)).byteValue();				
				content = content.substring(end + 1, content.length());
				content = content.trim();
				if (pos + 1 >= max_capasity) {
					message = increaseBuffer(message, max_capasity + 100);
					max_capasity += 100;					
				}
				message[pos++] = value;
			}
			
			if (content.startsWith(STRING_TAG)) {
				content = content.substring(STRING_TAG.length() + 2, content.length());
				int end = content.indexOf("\")");
				byte[] text = content.substring(0, end).getBytes();
				content = content.substring(end + 2, content.length());
				content = content.trim();
				if (pos + text.length >= max_capasity) {
					message = increaseBuffer(message, max_capasity + text.length);
					max_capasity += text.length;									
				}
				for (int i = 0; i < text.length; i++) {
					message[pos++] = text[i];
				}
			}
			
			if (content.startsWith(REPBYTE_TAG)) {
				content = content.substring(REPBYTE_TAG.length() + 1, content.length());
				int end = content.indexOf(",");
				byte value = Integer.valueOf(content.substring(0, end)).byteValue();
				content = content.substring(end+1, content.length());
				end = content.indexOf(")");
				int repeat = Integer.valueOf(content.substring(0, end)).intValue();
				content = content.substring(end+1, content.length());
				content = content.trim();
				if (pos + repeat >= max_capasity) {
					message = increaseBuffer(message, max_capasity + repeat);
					max_capasity += repeat;									
				}
				for (int i = 0; i < repeat; i++) {
					message[pos++] = value;
				}
			}
			
			if (content.startsWith(RNDBYTE_TAG)) {
				content = content.substring(RNDBYTE_TAG.length() + 1, content.length());
				int end = content.indexOf(")");
				int repeat = Integer.valueOf(content.substring(0, end));
				content = content.substring(end+1, content.length());
				content = content.trim();
				command.setRandomByteComponent(true);				
				pos += repeat;				
			}			
		}
		
		command.setMessage(message);
		command.setMessageLength(pos);		
		return command;
	}
	
	private static byte[] increaseBuffer(byte[] buf, int new_size) {
		byte[] new_buf = new byte[new_size];
		for (int i = 0; i < buf.length; i++) {
			new_buf[i] = buf[i];
		}		
		return new_buf;
	}

}
