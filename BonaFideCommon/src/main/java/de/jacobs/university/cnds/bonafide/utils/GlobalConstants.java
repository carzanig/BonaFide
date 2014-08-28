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

/**
 * 
 * This class contains the global constants which are used by both components. 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class GlobalConstants {
	
	public static final String SERVER_NAME = "BonaFideServer";
	public static final int DEFAULT_PORT_NUMBER = 4000;
	
	//public static final String CHECK_PORT_CMD = "test_port";	
	//public static final String CHECK_PORT_SUCCESS_CMD = "test_port_success";
	
	public static final String PING_CMD = "ping";
	public static final String PONG_RESPONSE = "pong";
	public static final String GET_ALL_PROTOCOLS_CMD = "get_all_protocols";
	public static final String GET_PROTOCOL_CMD = "get_protocol";
	public static final String GET_PROTOCOL_FAILED_CMD = "get_protocol_failed";
	public static final String DONE_CMD = "done";
	public static final String READY_CMD = "ready";	
	public static final String START_NEW_TEST_CMD = "start_new_test";
	public static final String SERVER_IS_BUSY_CMD = "server_is_busy";		
	public static final String RETRIEVE_TEST_RESULTS_CMD = "retrieve_test_results";
	public static final String TEST_RESULTS_NOT_FOUND = "test_results_not_found";
	public static final String UPLOAD_MEASUREMENT_RESULTS = "upload_measurement_results";
	public static final String CYCLES_TOTAL_CMD = "cycles_total";	
	public static final String UUID_RECEIVED_CMD = "uuid_received";
	public static final String TERMINATE_MEASUREMENT_CMD = "terminate_message";	
	public static final String SKIP_MEASUREMENT_TEST_CMD = "skip_measurement_test";
	public static final String END_OF_MESSAGE = "end_of_message";
	public static final String OK_NOTIFICATION = "ok";
	public static final String LINE_DELIMETER = "\r\n";
	
	public static final int[] messages_size = {2000, 4000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 768000, 1024000, 2048000, 4096000, 8192000};
	
	public static final double MAX_RTT = 2000000;	
	public static final int DEFAULT_SOCKET_TIMEOUT_MS = 5000;
	public static final int EXECUTE_SOCKET_TIMEOUT_MS = 60000;
	
}
