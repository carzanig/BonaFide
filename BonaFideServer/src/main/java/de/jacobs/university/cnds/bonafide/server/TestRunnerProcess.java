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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.Command.CommandType;
import de.jacobs.university.cnds.bonafide.model.Command.ExecutionSide;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;

/**
 * TestRunnerProcess is responsible for executing the measurement cycle on the server side
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class TestRunnerProcess implements Runnable {
	
	private static Logger logger = Logger.getLogger(TestRunnerProcess.class);
	
	private static final int SMALL_BUFFER_SIZE = 256; 
	private static final int MAX_BUFFER_SIZE = 4096;
	
	private ProtocolDescription protocolHeader;
	private Socket socket;
	
	//indicates whether current measurement cycle is executed for random or protocol flow 
	private boolean randomFlow;

	public TestRunnerProcess(Socket socket, ProtocolDescription protocolHeader, boolean randomFlow) {
		this.protocolHeader = protocolHeader;
		this.socket = socket;
		this.randomFlow = randomFlow;
	}
	
	@Override
	public void run() {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		BonaFideServerContext context = BonaFideServerContext.getInstance();
		CompletenessResult completeResult = null;
		String uuid = null;
		int cycle_number = -1;
		
		//create a list for storing measurement results
		List<BandwidthPerformance> serverPerformance = new ArrayList<BandwidthPerformance>();
		
		try {
			socket.setSoTimeout(GlobalConstants.EXECUTE_SOCKET_TIMEOUT_MS);					
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
						
			List<Command> commands = context.getMeasurementTestBody();
			
			boolean measure_on_client = false;
			boolean measure_on_server = false;
			
			long start_time = 0;
			long end_time = 0;
			int number_of_bytes_sent = 0;
														
			for (int i = 0; i < commands.size(); i++) {
				Command command = commands.get(i);
				
				if (command.getType() == CommandType.DONE_COMMAND) {
					completeResult = CompletenessResult.SUCCESS;
					logger.debug("Test uuid: " + uuid + ". DONE command execution");
					break;
				}
				
				//enter to the "measure on client" block
				if (command.getType() == CommandType.START_MEASURE_ON_CLIENT_COMMAND) {
					measure_on_client = true;
					logger.debug("Test uuid: " + uuid + ". START_MEASURE_ON_CLIENT command execution");
					continue;
				}
				
				//enter to the "measure on server" block
				if (command.getType() == CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {				
					measure_on_client = false;
					logger.debug("Test uuid: " + uuid + ". STOP_MEASURE_ON_CLIENT command execution");
					continue;
				}
				
				//leave the "measure on client" block
				if (command.getType() == CommandType.START_MEASURE_ON_SERVER_COMMAND) {
					measure_on_server = true;
					logger.debug("Test uuid: " + uuid + ". START_MEASURE_ON_SERVER command execution");
					continue;
				}
				
				//leave the "measure on server" block
				if (command.getType() == CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {					
					measure_on_server = false;
					logger.debug("Test uuid: " + uuid + ". STOP_MEASURE_ON_SERVER command execution");
					continue;
				}
				
				//Process send_uuid command
				if (command.getType() == CommandType.SEND_UUID_COMMAND) {					
					logger.debug("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". SEND_UUID command execution");
					
					byte[] buffer = new byte[SMALL_BUFFER_SIZE];
					StringBuffer body = new StringBuffer();
					while (true) {
						int n = bis.read(buffer);
						if (n < 0) {
							completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
							logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Socket reset by peer.");
							break;			
						}						
						String line = new String(buffer, 0, n);
						body.append(line);
						String c = body.toString();
						if (c.endsWith(GlobalConstants.LINE_DELIMETER)) {
							break;
						}				
					}
					
					String res = body.toString();
					res = res.substring(0, res.length() - GlobalConstants.LINE_DELIMETER.length());
					
					StringTokenizer st = new StringTokenizer(res, " ");
					uuid = st.nextToken();
					cycle_number = Integer.valueOf(st.nextToken());
					
					if (uuid == null) {//If uuid has not been received terminate measurement cycle
						completeResult = CompletenessResult.WRONG_COMMAND_RECEIVED;
						logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Wrong command received.");
						break;
					}					
					logger.debug("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Test uuid received: " + uuid);
					
					bos.write(GlobalConstants.UUID_RECEIVED_CMD.getBytes());
					bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
					bos.flush();
					continue;
				}
				
				// execute "inject protocol header" command
				if (command.getType() == CommandType.INJECT_PROTOCOL_HEADER_COMMAND) {					
					if (uuid != null) {
						logger.debug("Test uuid: " + uuid + ". INJECT_PROTOCOL_HEADER command execution");
					} else {
						logger.debug("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". INJECT_PROTOCOL_HEADER command execution");
					}
					
					boolean errorOccur = false;
					boolean terminatedOnClientReceived = false;
					
					List<Command> requests = protocolHeader.getTSRequests();
					List<Command> responses = protocolHeader.getTSResponses();
					
					//Process if measurement cycle right now is in the MEASUREMENT_ON_SERVER block
					if (!measure_on_server) {
						byte[] buffer = new byte[MAX_BUFFER_SIZE];
						//Receive and send the set of "request-response" messages defined in a protocol description files
						for (int position = 0; position < requests.size(); position++) {
							int total = 0;
							while (true) {
								int r = bis.read(buffer, 0, MAX_BUFFER_SIZE);
								total += r;
								
								//If -1 received, than socket has been closed - terminate
								if (r == -1) {
									errorOccur = true;
									completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
									if (uuid != null) {
										logger.warn("Test uuid: " + uuid + ". Socket reset by peer.");
									} else {
										logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Socket reset by peer.");
									}
									break;
								}
								
								//If the "terminate_message" has been received than exit from the "measure on client" block 
								if (total == GlobalConstants.TERMINATE_MEASUREMENT_CMD.length() + GlobalConstants.LINE_DELIMETER.length()) {
									String message = new String(buffer, 0, total);
									if (message.equals(GlobalConstants.TERMINATE_MEASUREMENT_CMD + GlobalConstants.LINE_DELIMETER)) {
										if (measure_on_client) {
											int index = i;
											while (true) {
												index++;
												if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {
													break;
												}
											}
											measure_on_client = false;
											i = index+1;	
										}
										
										if (uuid != null) {
											logger.debug("Test uuid: " + uuid + ". Measurements on client terminated.");
										} else {
											logger.debug("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Measurements on client terminated.");
										}
										
										terminatedOnClientReceived = true;
									}
								}
															
								if (terminatedOnClientReceived) {
									break;
								}
								
								//The entire message defined by the command has been received
								if (total == requests.get(position).getMessageLength()) {
									break;
								}
								
								//Error! received more bytes than defined by the current command
								if (total > requests.get(position).getMessageLength()) {
									errorOccur = true;
									completeResult = CompletenessResult.WRONG_NUMBER_OF_BYTES_RECEIVED;
									if (uuid != null) {
										logger.warn("Test uuid: " + uuid + ". Wrong number of bytes received.");
									} else {
										logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Wrong number of bytes received.");
									}
		    					break;
		    				}
							}				
							
							if (terminatedOnClientReceived) {
								break;
							}
							
							if (errorOccur) {
								break;
							}
							
							if (!randomFlow) {
								bos.write(responses.get(position).getMessage(), 0, responses.get(position).getMessageLength());
							} else {
								bos.write(context.getRndBytes(), 0, responses.get(position).getMessageLength());
							}

							bos.flush();
						}
						
						if (terminatedOnClientReceived) {
							continue;
						}
												
					}					
					//Process if measurement cycle right now is NOT in the MEASUREMENT_ON_SERVER block
					else {						
						//Send and receive the set of "request-response" messages defined in a protocol description files
						for (int position = 0; position < responses.size(); position++) {
							if (!randomFlow) {
								bos.write(requests.get(position).getMessage(), 0, requests.get(position).getMessageLength());
							} else {
								bos.write(context.getRndBytes(), 0, requests.get(position).getMessageLength());
							}
							bos.flush();
							
							byte[] buffer = new byte[MAX_BUFFER_SIZE];
							int total = 0;
							while (true) {
								int r = bis.read(buffer, 0, MAX_BUFFER_SIZE);
								total += r;
								
								if (r == -1) {
									completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
									errorOccur = true;
									if (uuid != null) {
										logger.warn("Test uuid: " + uuid + ". Socket reset by peer.");
									} else {
										logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Socket reset by peer.");
									}
									break;
								}
								
								//The entire message defined by the command has been received
								if (total == responses.get(position).getMessageLength()) {
									break;
								}
		    				
								//Error! received more bytes than defined by the current command
								if (total > responses.get(position).getMessageLength()) {
									errorOccur = true;
									if (uuid != null) {
										logger.warn("Test uuid: " + uuid + ". Wrong number of bytes received.");
									} else {
										logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Wrong number of bytes received.");
									}
									break;
								}
							}
							if (errorOccur) {
								break;
							}	
						}											
					}															
					continue;
				}
				
				//Execute send command
				if (command.getType() == CommandType.SEND_COMMAND) {					
					
					if (command.getExecutionSide() == ExecutionSide.CLIENT_SIDE) {
						logger.debug("Test uuid: " + uuid + ". SEND_COMMAND command execution. Client side: " + command.getMessageLength() + " bytes");						
					} else {
						logger.debug("Test uuid: " + uuid + ". SEND_COMMAND command execution. Server side: " + command.getMessageLength() + " bytes");						
					}
					
					if (command.getExecutionSide() == ExecutionSide.SERVER_SIDE) {
						//Measure the bulk message "start time" 
						if (measure_on_server) {
							start_time = System.nanoTime();
							number_of_bytes_sent = command.getMessageLength();
						}
						
						if (!randomFlow && !command.isRandomByteComponent()) {
							bos.write(command.getMessage(), 0, command.getMessageLength());								
						} else {
							bos.write(context.getRndBytes(), 0, command.getMessageLength());
						}						
						bos.flush();
						logger.debug("Test uuid: " + uuid + ". SEND_COMMAND command. " + command.getMessageLength() + " bytes sent.");		
					}
					
					if (command.getExecutionSide() == ExecutionSide.CLIENT_SIDE) {
						boolean errorOccur = false;
						boolean terminatedOnClientReceived = false;
						byte[] buffer = new byte[MAX_BUFFER_SIZE];
						int total = 0;
						
						while (true) {
							int r = bis.read(buffer);
							total += r;
							
							if (r == -1) {
								completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
								errorOccur = true;
								logger.warn("Test uuid: " + uuid + ". Socket reset by peer.");
								break;
							}
							
							//Stop measurement test if TERMINATE_ALL_MEASUREMENTS_CMD received
							if (total == GlobalConstants.SKIP_MEASUREMENT_TEST_CMD.length()) {
								String message = new String(buffer, 0, total);
								if (message.equals(GlobalConstants.SKIP_MEASUREMENT_TEST_CMD)) {																		
									System.out.println("Test " + uuid + " terminated!");
									context.cleanTestExecutionResult(uuid);
									close(socket, bis, bos);		
									return;
								}
							}
							
							//If the "terminate_message" has been received than exit from the "measure on client" block 
							if (total == GlobalConstants.TERMINATE_MEASUREMENT_CMD.length() + GlobalConstants.LINE_DELIMETER.length()) {
								String message = new String(buffer, 0, total);
								if (message.equals(GlobalConstants.TERMINATE_MEASUREMENT_CMD + GlobalConstants.LINE_DELIMETER)) {
									if (measure_on_client) {
			    					int index = i;
			    					while (true) {
			    						index++;
			    						if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_CLIENT_COMMAND) {
			    							break;
			    						}
			    					}
			    					measure_on_client = false;
			    					i = index+1;	
									}
									logger.debug("Test uuid: " + uuid + ". Measurements on client terminated.");
									terminatedOnClientReceived = true;
								}
							}
							
							if (terminatedOnClientReceived) {
								break;
							}
							
							//The entire message defined by the command has been received
							if (total == command.getMessageLength()) {
								break;
							}
	    				
							if (total > command.getMessageLength()) {
								completeResult = CompletenessResult.WRONG_NUMBER_OF_BYTES_RECEIVED;	    					
								errorOccur = true;
								logger.warn("Test uuid: " + uuid + ". Wrong number of bytes received.");
								break;
							}							
						}
												
						if (terminatedOnClientReceived) {
							continue;
						}
						
						if (errorOccur) {
							break;
						}
						logger.debug("Test uuid: " + uuid + ". SEND_COMMAND command. " + command.getMessageLength() + " bytes received.");
						
						if (measure_on_server) {
							end_time = System.nanoTime();
							BandwidthPerformance performance = new BandwidthPerformance();
							performance.setBytesSent(number_of_bytes_sent);
							performance.setRoundTripTime((end_time - start_time)/1000);
							serverPerformance.add(performance);
							
							//If calculated RTT value is greater than MAX_RTT - send "terminate_message" to the client and 
							//exit from the "measure on server" block 
							if (performance.getRoundTripTime() > GlobalConstants.MAX_RTT) {
								if (commands.get(i+1).getType() != CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {
									bos.write(GlobalConstants.TERMINATE_MEASUREMENT_CMD.getBytes());
									bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
									bos.flush();
									
									int index = i;
									while (true) {
										index++;
										if (commands.get(index).getType() == CommandType.STOP_MEASURE_ON_SERVER_COMMAND) {
											break;
										}
									}
									i = index - 1;
									logger.debug("Test uuid: " + uuid + ". Measurements on server terminated.");
								}
							}							
						}
						
					}
															
					continue;					
				}				
			}
			
			
			byte[] buf = new byte[SMALL_BUFFER_SIZE];
			int read = bis.read(buf);
			if (read == -1) {
				completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
				logger.warn("Test uuid: " + uuid + ". Socket reset by peer.");
			} else {
				String result = new String(buf, 0, read);			
				if (!result.equals(GlobalConstants.DONE_CMD)) {
					completeResult = CompletenessResult.WRONG_COMMAND_RECEIVED;			
					logger.warn("Test uuid: " + uuid + ". WRONG command received.");
				}											
			}
							
		} catch (Exception e) {			
				if (e instanceof SocketTimeoutException) {
					completeResult = CompletenessResult.TIMEOUT_RECEIVED;				
					if (uuid != null) {
						logger.warn("Test uuid: " + uuid + ". Socket timeout exection occured.", e);
					} else {
						logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Socket timeout exection occured.", e);
					}
				} else {
					completeResult = CompletenessResult.SOCKET_CLOSED_DURING_THE_TEST;
					if (uuid != null) {
						logger.warn("Test uuid: " + uuid + ". Connection reset by peer.", e);
					} else {
						logger.warn("Client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ". Connection reset by peer.", e);
					}
				}
							
		}	
		
		try {			
			switch (completeResult) {
			case SUCCESS:
				context.addPerformanceResultToTestExecution(uuid, serverPerformance, cycle_number, randomFlow);
				bos.write(GlobalConstants.DONE_CMD.getBytes());
				bos.flush();
				break;				
			default:
				BandwidthPerformance performance = new BandwidthPerformance(completeResult);
				serverPerformance.add(performance);
				context.addPerformanceResultToTestExecution(uuid, serverPerformance, cycle_number, randomFlow);
				break;
			}
									
			close(socket, bis, bos);
		} catch (IOException e) {
			logger.error("Unexpected exception received.", e);
		}
		
	}
	
	private void close(Socket socket, BufferedInputStream bis, BufferedOutputStream bos) throws IOException {		
		bis.close();
		bos.close();
		socket.close();
	}

}

