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

/**
 * Enumerator that provides all possible measurement completeness value.
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public enum CompletenessResult {
	
	SUCCESS,
	OPENING_SOCKET_FAILED,
	SOCKET_CLOSED_DURING_THE_TEST,
	TIMEOUT_RECEIVED,
	WRONG_NUMBER_OF_BYTES_RECEIVED,
	TEST_TERMINATED_BY_USER,
	WRONG_COMMAND_RECEIVED,
	FATAL_ERROR;
	
	
	public static String getStringRepresentation(CompletenessResult result) {
		switch (result) {
		case SUCCESS:
			return "success";
		case OPENING_SOCKET_FAILED:
			return "connection_refused";
		case SOCKET_CLOSED_DURING_THE_TEST:
			return "connection_reset";
		case TIMEOUT_RECEIVED:
			return "timeout";
		case WRONG_NUMBER_OF_BYTES_RECEIVED:
			return "wrong_number_of_bytes_received";
		case WRONG_COMMAND_RECEIVED:
			return "wrong_command_received";
		case FATAL_ERROR:
			return "fatal_error";
		case TEST_TERMINATED_BY_USER:
			return "terminated_by_user";
		default:
			return "";
		}		
	}
	
	public static CompletenessResult getValueByStringRepresentation(String value) {
		if (value.equals("success")) {
			return SUCCESS;
		}
		
		if (value.equals("connection_refused")) {
			return OPENING_SOCKET_FAILED;
		}
		
		if (value.equals("connection_reset")) {
			return SOCKET_CLOSED_DURING_THE_TEST;
		}
		
		if (value.equals("timeout")) {
			return TIMEOUT_RECEIVED;
		}
		
		if (value.equals("wrong_number_of_bytes_received")) {
			return WRONG_NUMBER_OF_BYTES_RECEIVED;
		}
		
		if (value.equals("wrong_command_received")) {
			return WRONG_COMMAND_RECEIVED;
		}
		
		if (value.equals("fatal_error")) {
			return FATAL_ERROR;					
		}
		
		if (value.equals("terminated_by_user")) {
			return TEST_TERMINATED_BY_USER;
		}
		
		return null;
	}
	
	

}
