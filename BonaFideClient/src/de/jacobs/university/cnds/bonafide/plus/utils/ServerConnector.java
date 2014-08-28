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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.Command.CommandType;
import de.jacobs.university.cnds.bonafide.model.Command.ExecutionSide;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.plus.model.ClientExecutionResults;
import de.jacobs.university.cnds.bonafide.plus.model.MeasurementTestEventListener;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;
import de.jacobs.university.cnds.bonafide.utils.ProtocolDescriptionParser;

/**
 * 
 * 	ServerConnector file provides a set of static methods which are used for communicating with the server 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class ServerConnector {
	
	public static final int SMALL_BUFFER_SIZE = 32;
	public static final int BUFFER_SIZE = 1024;
	public static final int MAX_BUFFER_SIZE = 4096;
	public static final int EXTRA_BUFFER_SIZE = 16000;
		
	private static void close(Socket socket, BufferedInputStream bis, BufferedOutputStream bos) throws IOException {		
		bis.close();
		bos.close();
		socket.close();
	}
	
	/**
	 * 
	 * Executes the measurement cycle for the measurement test with a given uuid
	 * 
	 * @param hostname defines the server's IP address
	 * @param uuid defines measurement test UUID
	 * @param protocolDescription is a protocol description of the tested application protocol
	 * @param randomFlow indicates whether measurement cycle is running for the random flow or for the protocol flow
	 * @param cycleNumber is the sequence number of a measurement cycle
	 * @param clientPerformance is an initialized list for storing measurement results in the upload direction
	 * @return
	 */
	private static CompletenessResult executeProtocolPacketExchange(String hostname, 
													String uuid, 
													ProtocolDescription protocolDescription, 
													boolean randomFlow,
													int cycleNumber,
													List<BandwidthPerformance> clientPerformance) {
				
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		boolean measurementStarted = false;
							
		boolean measure_on_client = false;
		boolean measure_on_server = false;
		boolean skip_next_send_client = false;
		long start_time = 0;
		long end_time = 0;
		int number_of_bytes_sent = 0;					
		
		List<Command> commands = ApplicationGlobalContext.getInstance().getMeasurementTestBody();
			
		int port;
		if (randomFlow) {
			port = protocolDescription.getRFPort();
		} else {
			port = protocolDescription.getPFPort();
		}
		
		byte[] maxBuffer = new byte[MAX_BUFFER_SIZE];
		try {
			//Establish a new TCP connection for current measurement cycle
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.EXECUTE_SOCKET_TIMEOUT_MS);			
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());			
												
			measurementStarted = true;
			
			for (int i = 0; i < commands.size(); i++) {				
				Command command = commands.get(i);		
				
				//Execute send command
				if (command.getType() == CommandType.SEND_COMMAND) {	
					
					// if the server side is an executor, than receive a message from the server 
					if (command.getExecutionSide() == ExecutionSide.SERVER_SIDE) {
						
						int total = 0;
						while (true) {
							int r = bis.read(maxBuffer, 0, MAX_BUFFER_SIZE);							
							total += r;
							
							//If -1 received, than socket has been closed - terminate
							if (r == -1) {
								close(socket, bis, bos);
								return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
							}
							
							//If the "terminate_message" message has been received than exit from the "measure on server" block 
							if (total == GlobalConstants.TERMINATE_MEASUREMENT_CMD.length() + GlobalConstants.LINE_DELIMETER.length()) {
								if ((GlobalConstants.TERMINATE_MEASUREMENT_CMD + GlobalConstants.LINE_DELIMETER).equals(new String(maxBuffer, 0, total))) {
			    					int index = i;
			    					while (true) {
			    						index++;
			    						if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {
			    							break;
			    						}
			    					}
			    					i = index - 1;			    					
			    					break;
								}
							}
							
							//Error! received more bytes than defined by the current command
		    				if (total > command.getMessageLength()) {
		        				return CompletenessResult.WRONG_NUMBER_OF_BYTES_RECEIVED;
		    				}
		    				
		    				//The entire message defined by the command has been received
		    				if (total == command.getMessageLength()) {
		    					break;
		    				}		    				
						}//end while(true) block
						
						//If we are inside the "measurement on client" block, than client measures the "end time" and calculates the bilk message RTT value
						if (measure_on_client) {
							end_time = System.nanoTime();
							BandwidthPerformance performance = new BandwidthPerformance();
							performance.setBytesSent(number_of_bytes_sent);
							performance.setRoundTripTime((end_time - start_time)/1000);
							clientPerformance.add(performance);
							
							//If calculated RTT value is greater than MAX_RTT - send "terminate_message" to the server and 
							//exit from the "measure on client" block 
							if (performance.getRoundTripTime() > GlobalConstants.MAX_RTT) {
		    					if (commands.get(i+1).getType() != CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {			    							    						
		    						skip_next_send_client = true;
		    						
		    						bos.write(GlobalConstants.TERMINATE_MEASUREMENT_CMD.getBytes());
		    						bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
		    						bos.flush();
		    						
			    					int index = i;
			    					while (true) {
			    						index++;
			    						if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {
			    							break;
			    						}
			    					}
			    					i = index - 1;		    							    					
		    					}		    					
							}							
						} // end if (measure_on_client)
						
					} else if (command.getExecutionSide() == ExecutionSide.CLIENT_SIDE) {	// if the client side is an executor, than send a message to the server
						
						if (skip_next_send_client) {
							skip_next_send_client = false;
							continue;
						}
						
						//If we are inside the "measure on client" block, then measure the "start time" value
						if (measure_on_client) {
							start_time = System.nanoTime();
							number_of_bytes_sent = command.getMessageLength();
						}
						
						//If the "Terminate measurements" button has been pressed, then send the "terminate_all_measurements" to the server
						//and exit from the current measurement test
						if (ApplicationGlobalContext.getInstance().isTerminateAndReturnFlag()) {
							bos.write(GlobalConstants.SKIP_MEASUREMENT_TEST_CMD.getBytes());
							bos.flush();
							return CompletenessResult.TEST_TERMINATED_BY_USER;
						}
						//-------------------------------------------//
						
						
						//send a message defined by the current command to the server
						if (randomFlow) {																					
							bos.write(ApplicationGlobalContext.getInstance().getRndBytes(), 0, command.getMessageLength());
							bos.flush();							
						} else {
							if (command.isRandomByteComponent()) {
								bos.write(ApplicationGlobalContext.getInstance().getRndBytes(), 0, command.getMessageLength());
							} else {
								bos.write(command.getMessage(), 0, command.getMessageLength());	
							}							
							bos.flush();
						}
					}
					
				} else if (command.getType() == CommandType.START_MEASURE_ON_CLIENT_COMMAND) {
					measure_on_client = true;//enter to the "measure on client" block
				} else if (command.getType() == CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {
					measure_on_client = false;//leave the "measure on client" block
				} else if (command.getType() == CommandType.START_MEASURE_ON_SERVER_COMMAND) {
					measure_on_server = true;//enter to the "measure on server" block
				} else if (command.getType() == CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {
					measure_on_server = false;//leave the "measure on server" block
				} else if (command.getType() == CommandType.DONE_COMMAND) {
					break;
				} else if (command.getType() == CommandType.INJECT_PROTOCOL_HEADER_COMMAND) {// execute "inject protocol header" command
					
					List<Command> requests = protocolDescription.getRequests();
					List<Command> responses = protocolDescription.getResponses();
										
					if (!measure_on_server) {
						//Send and receive the set of "request-response" messages defined in a protocol description files
						for (int position = 0; position < requests.size(); position++) {
							if (!randomFlow) {
								bos.write(requests.get(position).getMessage(), 0, requests.get(position).getMessageLength());
							} else {
								bos.write(ApplicationGlobalContext.getInstance().getRndBytes(), 0, requests.get(position).getMessageLength());
							}													
							bos.flush();
							
							int total = 0;
							while (true) {
								int r = bis.read(maxBuffer, 0, MAX_BUFFER_SIZE);							
								total += r;
								
								//If -1 received, than socket has been closed - terminate
								if (r == -1) {
									close(socket, bis, bos);
									return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
								}
								
								if (total == responses.get(position).getMessageLength()) {
			    					break;
			    				}
								if (total > responses.get(position).getMessageLength()) {
			        				return CompletenessResult.WRONG_NUMBER_OF_BYTES_RECEIVED;
			    				}
							}
						}						
					} else {												
						boolean terminate = false;		
						//Receive and send the set of "request-response" messages defined in a protocol description files
						for (int position = 0; position < requests.size(); position++) {
							int total = 0;
							while (true) {
								int r = bis.read(maxBuffer, 0, MAX_BUFFER_SIZE);							
								total += r;
								
								//If -1 received, than socket has been closed - terminate
								if (r == -1) {
									close(socket, bis, bos);
									return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
								}
								
								//If the "terminate_measurement" message has been received than exit from the "measure on server" block 
								if (total == GlobalConstants.TERMINATE_MEASUREMENT_CMD.length() + GlobalConstants.LINE_DELIMETER.length()) {
									if ((GlobalConstants.TERMINATE_MEASUREMENT_CMD + GlobalConstants.LINE_DELIMETER).equals(new String(maxBuffer, 0, total))) {
				    					int index = i;
				    					while (true) {
				    						index++;
				    						if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {
				    							break;
				    						}
				    					}
				    					i = index - 1;			    					
				    					terminate = true;
				    					break;
									}
								}
								
			    				//The entire message defined by the command has been received								
								if (total == requests.get(position).getMessageLength()) {
			    					break;
			    				}
								
								//Error! received more bytes than defined by the current command
								if (total > requests.get(position).getMessageLength()) {
			        				return CompletenessResult.WRONG_NUMBER_OF_BYTES_RECEIVED;
			    				}
							}
							if (!terminate) {
								if (!randomFlow) {								
									bos.write(responses.get(position).getMessage(), 0, responses.get(position).getMessageLength());
								} else {
									bos.write(ApplicationGlobalContext.getInstance().getRndBytes(), 0, responses.get(position).getMessageLength());
								}
								
								bos.flush();	
							} else {
								break;
							}
						}											
					}					
					
				} else if (command.getType() == CommandType.SEND_UUID_COMMAND) {//execute "send UUID" command
					StringBuffer sb = new StringBuffer();
					sb.append(uuid).append(" ").append(cycleNumber).append(GlobalConstants.LINE_DELIMETER);
					bos.write(sb.toString().getBytes());
					bos.flush();
																
					byte[] buffer = new byte[SMALL_BUFFER_SIZE];
					StringBuffer body = new StringBuffer();
					while (true) {
						int n = bis.read(buffer);
						if (n < 0) {
							close(socket, bis, bos);
							return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;				
						}						
						String line = new String(buffer, 0, n);
						body.append(line);
						String c = body.toString();
						if (c.endsWith(GlobalConstants.LINE_DELIMETER)) {
							break;
						}				
					}
															
					if (!body.toString().equals(GlobalConstants.UUID_RECEIVED_CMD + GlobalConstants.LINE_DELIMETER)) {
						close(socket, bis, bos);
						return CompletenessResult.WRONG_COMMAND_RECEIVED;
					}
					
				}
			}
			
			bos.write(GlobalConstants.DONE_CMD.getBytes());
			bos.flush();
			
			byte[] buffer = new byte[SMALL_BUFFER_SIZE];
			
			int n = bis.read(buffer);
			if (n < 0) {
				close(socket, bis, bos);
				return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
			}
			
			String response = new String(buffer, 0, n); 
			if (!response.equals(GlobalConstants.DONE_CMD)) {
				close(socket, bis, bos);
				return CompletenessResult.WRONG_COMMAND_RECEIVED;
			}						
			close(socket, bis, bos);
						
			return CompletenessResult.SUCCESS;
		} catch (Exception e) {
			
			if (!measurementStarted) {
				return CompletenessResult.OPENING_SOCKET_FAILED;
			}
						
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return CompletenessResult.FATAL_ERROR;
			}
			
			if (e instanceof SocketTimeoutException) {
				return CompletenessResult.TIMEOUT_RECEIVED;	
			}
						
			return CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;									
		}		
	}
	
	
	/**
	 * This method triggers the traffic shaping measurement test for the selected application protocol and delay test.  
	 * 
	 * @param hostname defines the server's IP address
	 * @param port is a server's main socket port number
	 * @param protocolDescription is a protocol description of the tested application protocol
	 * @param cycles defines number of measurement cycles within the measurement test 
	 * @param results is an initialized list for storing measurement results in the upload direction
	 * @param eventListener is an MeasurementTestEventListener interface implementation used for update the application GUI
	 */
	public static void runTest(String hostname, int port, 
												ProtocolDescription protocolDescription, 
												int cycles,
												ClientExecutionResults results,
												MeasurementTestEventListener eventListener) {				
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;		
		
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);			
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			
			StringBuffer sb = new StringBuffer();
			sb.append(GlobalConstants.START_NEW_TEST_CMD).append(" ").append(protocolDescription.getProtocolName());
			sb.append(" ").append(cycles).append(GlobalConstants.LINE_DELIMETER);
			bos.write(sb.toString().getBytes());
			bos.flush();
			
			
			
			sb = new StringBuffer();
			byte[] buffer = new byte[BUFFER_SIZE];			
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);
					results.setErrorMessage("Error occur while reading from socket.");
					return;				
				}						
				String line = new String(buffer, 0, read);
				sb.append(line);
				String c = sb.toString();
				if (c.endsWith(GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
			
			String uuid = sb.toString();
			uuid = uuid.substring(0, uuid.length() - 2);
			if (uuid.equals(GlobalConstants.SERVER_IS_BUSY_CMD)) {
				close(socket, bis, bos);				
				results.setErrorMessage(uuid);
				return;
			}												
						
			results.setUUID(uuid);
			
			//Execute a sequence of measurement cycles
			for (int number = 0; number < cycles; number++) {
				
				//Run measurement cycle for the Random flow
				List<BandwidthPerformance> clientPerformances = new ArrayList<BandwidthPerformance>();
				CompletenessResult resolution = executeProtocolPacketExchange(hostname, 
																	uuid, 
																	protocolDescription, 
																	true,
																	number,
																	clientPerformances);								
				
				
				switch (resolution) {
					case SUCCESS:
						results.addClientRandomPerformanceResults(clientPerformances, number);
						break;
					case TEST_TERMINATED_BY_USER:
						results.setTerminated(true);
						close(socket, bis, bos);
						return;
					case FATAL_ERROR:
						results.setErrorMessage("Fatal error occurred during the test.");
						close(socket, bis, bos);
						return;
					case OPENING_SOCKET_FAILED:
						results.setErrorMessage("Connection refused.");
						close(socket, bis, bos);
						return;						
					default:	
						BandwidthPerformance performance = new BandwidthPerformance(resolution);
						clientPerformances.add(performance);
						results.addClientRandomPerformanceResults(clientPerformances, number);
						break;
				}
												
				//eventListener.handleStepCompletedEvent();				
				clientPerformances = new ArrayList<BandwidthPerformance>();

				
				//Run measurement cycle for the Protocol flow
				resolution = executeProtocolPacketExchange(hostname, 
															uuid, 
															protocolDescription, 
															false, 
															number,
															clientPerformances);
				
				switch (resolution) {
					case SUCCESS:
						results.addClientProtocolPerformanceResults(clientPerformances, number);
						break;
					case TEST_TERMINATED_BY_USER:
						results.setTerminated(true);
						close(socket, bis, bos);
						return;
					case FATAL_ERROR:
						results.setErrorMessage("Fatal error occurred during the test.");
						close(socket, bis, bos);
						return;
					case OPENING_SOCKET_FAILED:
						results.setErrorMessage("Connection refused.");
						close(socket, bis, bos);
						return;
					default:	
						BandwidthPerformance performance = new BandwidthPerformance(resolution);
						clientPerformances.add(performance);
						results.addClientProtocolPerformanceResults(clientPerformances, number);
						break;
				}
								
				//eventListener.handleStepCompletedEvent();				
			}
																																												
			close(socket, bis, bos);
			
			// perform delay test
			Long delay=performDelayTest(hostname, port);
			if (delay!=null) {
				results.setDelay(delay);
			}
			
		} catch (Exception e) {			
			results.setErrorMessage(e.getMessage());
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {				
			}
		}		
	}
	
	/**
	 * 
	 * Retrieves the protocol description file from the server
	 * 
	 * @param hostname defines the server's IP address 
	 * @param port defines the server's main socket port number
	 * @param protocolName is an application protocol name
	 * @return
	 */
	public static ProtocolDescription getProtocolDescription(String hostname, int port, String protocolName) {
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;	
		
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(3000);			
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
									
			StringBuffer sb = new StringBuffer();
			sb.append(GlobalConstants.GET_PROTOCOL_CMD).append(" ").append(protocolName).append(GlobalConstants.LINE_DELIMETER);			
			bos.write(sb.toString().getBytes());
			bos.flush();														
					
			StringBuffer content = new StringBuffer();
			byte[] buffer = new byte[MAX_BUFFER_SIZE];
			content = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);
					return null;				
				}						
				String line = new String(buffer, 0, read);
				content.append(line);
				String c = content.toString();
				if (c.endsWith(GlobalConstants.END_OF_MESSAGE + GlobalConstants.LINE_DELIMETER) ||
						c.endsWith(GlobalConstants.GET_PROTOCOL_FAILED_CMD + GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
			
			if (content.toString().equals(GlobalConstants.GET_PROTOCOL_FAILED_CMD)) {
				return null;
			}
			
			ProtocolDescription header = ProtocolDescriptionParser.parseProtocolHeader(content.toString());
			header.setProtocolName(protocolName);			
			return header;
		} catch (Exception e) {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return null;
			}
			return null;				
		}
	}
	
	/**
	 * Performs test of the delay - so calles ping-pong test. Returned value is the delay in milliseconds. If test was interrupted, null will be returned
	 * @param hostname
	 * @param port
	 * @return
	 */
	public static Long performDelayTest(String hostname, int port) {
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		long startTime=System.currentTimeMillis();
		
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());			
												
			bos.write(GlobalConstants.PING_CMD.getBytes());
			bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
			bos.flush();
			
			
			byte[] buffer = new byte[BUFFER_SIZE];
			StringBuffer sb = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);
					return null;				
				}						
				String line = new String(buffer, 0, read);
				sb.append(line);
				if (sb.toString().endsWith(GlobalConstants.END_OF_MESSAGE + GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
						
			close(socket, bis, bos);
			
			long endTime=System.currentTimeMillis();
			
			String content = sb.toString();			
			content = content.substring(0, content.length() - GlobalConstants.LINE_DELIMETER.length() - GlobalConstants.END_OF_MESSAGE.length());
			
			if (content.equals(GlobalConstants.PONG_RESPONSE)) {
				return endTime-startTime;
			}
			else {
				return null;
			}
		} catch (Exception e) {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return null;
			}
			return null;
		}		 
	}
	
	/**
	 * 
	 * Retrieves the list of available protocol description files form the server 
	 * 
	 * @param hostname defines the server's IP address
	 * @param port defines the server's main socket port number
	 * @return list or null if an error occurred
	 */
	public static String getProtocolDescriptionsList(String hostname, int port) {
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());			
												
			bos.write(GlobalConstants.GET_ALL_PROTOCOLS_CMD.getBytes());
			bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
			bos.flush();
			
			
			byte[] buffer = new byte[BUFFER_SIZE];
			StringBuffer sb = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);
					return null;				
				}						
				String line = new String(buffer, 0, read);
				sb.append(line);
				if (sb.toString().endsWith(GlobalConstants.END_OF_MESSAGE + GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
						
			close(socket, bis, bos);
			
			String content = sb.toString();			
			return content.substring(0, content.length() - GlobalConstants.LINE_DELIMETER.length() - GlobalConstants.END_OF_MESSAGE.length());
		} catch (Exception e) {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return null;
			}
			return null;
		}		 
	}
	
	
	/**
	 * 
	 * Retrieves the list of available protocol descriptions form the measurement server 
	 * 
	 * @param hostname defines the server's IP address
	 * @param port defines the server's main socket port number
	 * @return list or null if an error occurred
	 */
	public static List<ProtocolDescription> getProtocolDescriptions(String hostname, int port) {
		ArrayList<ProtocolDescription> res = new ArrayList<ProtocolDescription>();
		
		String protocolNames=getProtocolDescriptionsList(hostname, port);
		if (protocolNames==null) {
			return null;
		}
		
		StringTokenizer st = new StringTokenizer(protocolNames, "\r\n");
    	while (st.hasMoreTokens()) {
    		String protocolName=st.nextToken();
    		ProtocolDescription protocolDescription = getProtocolDescription(hostname, port, protocolName);
    		if (protocolDescription!=null) {
    			res.add(protocolDescription);
    		}
    	}
    	
    	return res;
	}
	
	/**
	 * 
	 * Retrieves the download direction measurement results from the server 
	 * 
	 * @param hostname defines the server's IP address
	 * @param port defines the server's main socket port number
	 * @param uuid
	 * @return
	 */
	public static String retrieveServerTestExecutionResults(String hostname, int port, String uuid) {
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;		
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			
			StringBuffer sb = new StringBuffer();
			sb.append(GlobalConstants.RETRIEVE_TEST_RESULTS_CMD).append(" ").append(uuid).append(GlobalConstants.LINE_DELIMETER);
			bos.write(sb.toString().getBytes());
			bos.flush();				
			
			byte[] buffer = new byte[BUFFER_SIZE];
			StringBuffer body = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);
					return null;				
				}						
				String line = new String(buffer, 0, read);
				body.append(line);
				String c = body.toString();
				if (c.endsWith(GlobalConstants.END_OF_MESSAGE + GlobalConstants.LINE_DELIMETER) ||
						c.endsWith(GlobalConstants.TEST_RESULTS_NOT_FOUND + GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
			
									
			close(socket, bis, bos);
			String result = body.toString();
			result = result.substring(0, result.length() - 2);
			return result;
		} catch (Exception e) {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return null;
			}
			return null;
		}
	}
	
	
	/**
	 * 
	 * Pushes the measurement test report file to the server
	 * 
	 * @param hostname defines the server's IP address
	 * @param port defines the server's main socket port number
	 * @param filepath
	 * @return
	 */
	public static boolean uploadTestResultsToServer(String hostname, int port, String filepath) {
		Socket socket = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		try {
			socket = new Socket(hostname, port);
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
						
			bos.write(GlobalConstants.UPLOAD_MEASUREMENT_RESULTS.getBytes());
			bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
			bos.flush();
			
			
			byte[] buffer = new byte[SMALL_BUFFER_SIZE];		
			StringBuffer body = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					close(socket, bis, bos);			
					return false;				
				}						
				String line = new String(buffer, 0, read);
				body.append(line);
				String c = body.toString();
				if (c.endsWith(GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
			
			String result = body.toString();
			result = result.substring(0, result.length() - GlobalConstants.LINE_DELIMETER.length());
			
			if (!result.equals(GlobalConstants.READY_CMD)) {
				close(socket, bis, bos);			
				return false;				
			}
			
			File file = new File(filepath);
			if (!file.exists()) {
				close(socket, bis, bos);			
				return false;				
			}
			
			BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
			StringBuffer strContent = new StringBuffer();
			int ch;
			while((ch = fis.read()) != -1) {
				strContent.append((char) ch);
			}
			fis.close();
						
			bos.write(strContent.toString().getBytes());
			bos.write(GlobalConstants.END_OF_MESSAGE.getBytes());
			bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
			bos.flush();				
			
			close(socket, bis, bos);
		} catch (Exception e) {
			try {
				if (bos != null) {
					bos.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (socket != null) {			
					socket.close();
				}
			} catch (IOException error) {
				return false;
			}
			return false;
		}
		
		
		return true;
	}
	
	/*	public static boolean isPortAvailabile(String hostname, int port) {	
	DatagramChannel channel = null;
	try {
		channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress(hostname, port));
		channel.configureBlocking(false);
								
		String result = null;
		for (int i = 0; i < 15; i++) {
			int write = channel.write(ByteBuffer.wrap(GlobalConstants.CHECK_PORT_CMD.getBytes()));
			
			if (write < 0) {
				channel.close();
				return false;
			}
			
			ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
			int counter = 0;					
			while (true) {
				int n = 0;
				try {
					n = channel.read(buf);							
				} catch (PortUnreachableException e) {
		        	counter++;
		        	continue;
				}			
			    if (n > 0) {
			    	result = new String(buf.array()).substring(0, n);
			       	break;
			    }
			    Thread.sleep(5);
			    if (counter++ > 300) {
			    	channel.close();
			    	return false;
			    }
			}
			
			if (result.equals(GlobalConstants.CHECK_PORT_SUCCESS_CMD)) {
				break;
			}
			
		}

						
		channel.close();							
		return result.equals(GlobalConstants.CHECK_PORT_SUCCESS_CMD)?true:false;
	} catch (Exception e) {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException error) {
				return false;
			}
		}
		return false;
	}
}
*/

}


