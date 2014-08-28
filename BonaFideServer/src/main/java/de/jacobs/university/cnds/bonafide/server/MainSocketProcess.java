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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.jacobs.university.cnds.bonafide.model.BandwidthPerformance;
import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.model.CompletenessResult;
import de.jacobs.university.cnds.bonafide.model.ServerExecutionResults;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;

/**
 * 
 * When the MainSocketProcess is used to process all client's pre- and post- measurement test requests.  
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class MainSocketProcess implements Runnable {
	
	private static Logger logger = Logger.getLogger(MainSocketProcess.class);
	
	private static final int SMALL_BUFFER_SIZE = 256; 
	private static final int MAX_BUFFER_SIZE = 4096;
	
	private Socket socket;
	
	public MainSocketProcess(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		BonaFideServerContext context = BonaFideServerContext.getInstance();		
		try {							
			socket.setSoTimeout(GlobalConstants.DEFAULT_SOCKET_TIMEOUT_MS);					
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			
			byte[] buffer = new byte[SMALL_BUFFER_SIZE];
			StringBuffer b = new StringBuffer();
			while (true) {
				int read = bis.read(buffer);
				if (read < 0) {
					break;
				}
				String line = new String(buffer, 0, read);
				b.append(line);
				if (b.toString().endsWith(GlobalConstants.LINE_DELIMETER)) {
					break;
				}				
			}
			
			String command = b.toString();		
									
			//remove \r\n symbol
			command = command.substring(0, command.length() - GlobalConstants.LINE_DELIMETER.length());
			
			/*
			 * process "ping-pong" command - used for delay measurement
			 */
			if (command.startsWith(GlobalConstants.PING_CMD)) {
				logger.debug("PING_PONG command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				bos.write(GlobalConstants.PONG_RESPONSE.getBytes());
				bos.write(GlobalConstants.END_OF_MESSAGE.getBytes());
				bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
				bos.flush();
			}
			
			/*
			 * process "get_all_protocols" command
			 */
			if (command.startsWith(GlobalConstants.GET_ALL_PROTOCOLS_CMD)) {
				logger.debug("GET_ALL_PROTOCOLS command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				String names = context.getAllProtocolHeaderNames();
				bos.write(names.getBytes());
				bos.flush();
			}
			
			/*
			 * process "get <protocol_name>" command
			 */
			if (command.startsWith(GlobalConstants.GET_PROTOCOL_CMD)) {				
				String protocolName = command.substring(GlobalConstants.GET_PROTOCOL_CMD.length() + 1, command.length());
				logger.debug("GET_PROTOCOL " + protocolName +" command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				
				ProtocolDescription protocolHeader = context.getProtocolHeaderByName(protocolName);
				
				if (protocolHeader == null) {
					bos.write(GlobalConstants.GET_PROTOCOL_FAILED_CMD.getBytes());
					bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
					bos.flush();
				} else {
					File file = new File(protocolHeader.getDescriptionFilePath());
					FileReader reader = new FileReader(file);
					StringBuffer sb = new StringBuffer();
					int c;
					
					while((c = reader.read()) != -1) {
						sb.append((char) c);
					}
					
					reader.close();
					
					bos.write(sb.toString().getBytes());	
					bos.write(GlobalConstants.END_OF_MESSAGE.getBytes());
					bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
					bos.flush();									
				}
				
			}
			
			/*
			 * process "start_new_test <protocol_name> <number>" command
			 */
			if (command.startsWith(GlobalConstants.START_NEW_TEST_CMD)) {				
				logger.info("START_NEW_TEST command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				st.nextToken();				
				int total_cycles = Integer.valueOf(st.nextToken());				
				String uuid = UUID.randomUUID().toString();
				long startTime = System.currentTimeMillis();
				
				ServerExecutionResults execution = new ServerExecutionResults(uuid, total_cycles, startTime);
				context.addTestExecutionEntity(uuid, execution);
				
				bos.write(uuid.getBytes());
				bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
				bos.flush();
			}
			
			/*
			 * process "retrieve_test_results <uuid> command
			 */
			if (command.startsWith(GlobalConstants.RETRIEVE_TEST_RESULTS_CMD)) {			
				String uuid = command.substring(GlobalConstants.RETRIEVE_TEST_RESULTS_CMD.length() + 1, command.length());
				logger.debug("RETRIEVE_TEST_RESULTS for test uuid="+ uuid +" command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				ServerExecutionResults result = context.getTestExecutionResult(uuid);
				if (result == null) {
					bos.write(GlobalConstants.TEST_RESULTS_NOT_FOUND.getBytes());
					bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
					bos.flush();					
				} else {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < result.getCyclesTotal(); i++) {
						sb.append("Cycle ");
						sb.append(i);
						sb.append("\n");
						
						sb.append("Protocol\n");
						List<BandwidthPerformance> serverProtocolBandwidthPerformance = result.getServerProtocolBandwidthPerformance(i);
						if (serverProtocolBandwidthPerformance != null) {
							for (BandwidthPerformance performance: serverProtocolBandwidthPerformance) {
								if (performance.getTestResult() == CompletenessResult.SUCCESS) {
									sb.append(performance.getBytesSent());
									sb.append(" ");
									sb.append(performance.getRoundTripTime());
									sb.append("\n");
								} else {
									sb.append(CompletenessResult.getStringRepresentation(performance.getTestResult()));
									sb.append("\n");
								}
							}
						}
						
						sb.append("Random\n");
						List<BandwidthPerformance> serverRandomBandwidthPerformance = result.getServerRandomBandwidthPerformance(i);
						if (serverRandomBandwidthPerformance != null) {
							for (BandwidthPerformance performance: serverRandomBandwidthPerformance) {
								if (performance.getTestResult() == CompletenessResult.SUCCESS) {
									sb.append(performance.getBytesSent());
									sb.append(" ");
									sb.append(performance.getRoundTripTime());
									sb.append("\n");									
								} else {
									sb.append(CompletenessResult.getStringRepresentation(performance.getTestResult()));
									sb.append("\n");									
								}
							}
						}
						
						sb.append("End cycle\n");
					}
					
					bos.write(sb.toString().getBytes());
					bos.write(GlobalConstants.END_OF_MESSAGE.getBytes());
					bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
					bos.flush();
					
					context.cleanTestExecutionResult(uuid);
				}								
			}
			
			/*
			 * process "upload_measurement_results" command
			 */
			if (command.startsWith(GlobalConstants.UPLOAD_MEASUREMENT_RESULTS)) {
				logger.debug("UPLOAD_MEASUREMENT_RESULTS  command received from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				bos.write(GlobalConstants.READY_CMD.getBytes());
				bos.write(GlobalConstants.LINE_DELIMETER.getBytes());
				bos.flush();
				
				StringBuffer sb = new StringBuffer();
				byte[] buf = new byte[MAX_BUFFER_SIZE];
				
				while (true) {							
					int read = bis.read(buf);
					if (read < 0) {
						logger.error("error occur while accepting the measurement results from the client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
						close(socket, bis, bos);			
						return;				
					}						
					String line = new String(buf, 0, read);
					sb.append(line);
					String c = sb.toString();
					if (c.endsWith(GlobalConstants.END_OF_MESSAGE + GlobalConstants.LINE_DELIMETER)) {
						break;
					}	
										
				}
				
				String html = sb.toString();
				html = html.substring(0, html.length() - GlobalConstants.LINE_DELIMETER.length() - GlobalConstants.END_OF_MESSAGE.length());				
				context.storeIncomingFile(html);
			}
			
			close(socket, bis, bos);
		} catch (Exception e) {
			System.err.println("Error occur while processing client request.");
		}				
	}
	
	private void close(Socket socket, BufferedInputStream bis, BufferedOutputStream bos) throws IOException {		
		bis.close();
		bos.close();
		socket.close();
	}

}
