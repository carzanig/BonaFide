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

import java.util.ArrayList;
import java.util.List;

import de.jacobs.university.cnds.bonafide.model.Command;
import de.jacobs.university.cnds.bonafide.model.Command.CommandType;
import de.jacobs.university.cnds.bonafide.model.Command.ExecutionSide;

/**
 * 
 * This class provides a static method for generating the sequence commands that are executed
 * during the measurement test 
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class MeasurementTestGenerator {
	
	/**
	 * This static method generates the sequence of commands that are executed by the server and the client 
	 * components within a single measurement cycle. The measurement cycle structure is presented below:    
	 * 
	 * 		start new measurement cycle
	 *		inject protocol messages
	 *		client sends UUID and current cycle number
	 *		inject protocol messages
	 *
	 * 		start measurements on the client-side
	 * 		repeat until valid:
	 * 			inject protocol messages
	 * 			client sends bulk message
	 *			server responds with OK notification
	 *			break or increase message size
	 * 		stop measurements on the client-side
	 * 
	 *		repeat multiple times:
	 *			client sends short message
	 *			server sends short message
	 *		
	 *		start measurements on server-side
	 *		repeat until valid:
	 *			inject protocol messages
	 *			server sends bulk message
	 *			client responds with OK notification
	 *			break or increase message size
	 *		stop measurements on the server-side
	 * 
	 * 		measurement cycle is done
	 * 
	 * @return the list of commands that define the measurement cycle 
	 */
	public static List<Command> createMeasurementTestBody() {
		List<Command> commands = new ArrayList<Command>();
		
		Command inject_header = new Command(CommandType.INJECT_PROTOCOL_HEADER_COMMAND);
		commands.add(inject_header);
		
		Command send_uuid = new Command(CommandType.SEND_UUID_COMMAND);
		commands.add(send_uuid);
		
		Command start_measure_on_client = new Command(CommandType.START_MEASURE_ON_CLIENT_COMMAND);
		commands.add(start_measure_on_client);
		
		for (int i = 0; i < GlobalConstants.messages_size.length; i++) {
			
			Command inject = new Command(CommandType.INJECT_PROTOCOL_HEADER_COMMAND);
			commands.add(inject);
						
			Command send = new Command(CommandType.SEND_COMMAND);
			send.setRandomByteComponent(true);
			send.setExecutionSide(ExecutionSide.CLIENT_SIDE);
			send.setMessageLength(GlobalConstants.messages_size[i]);
			commands.add(send);
			
			Command notify = new Command(CommandType.SEND_COMMAND);
			notify.setRandomByteComponent(false);		
			notify.setExecutionSide(ExecutionSide.SERVER_SIDE);
			notify.setMessage(GlobalConstants.OK_NOTIFICATION.getBytes());
			notify.setMessageLength(GlobalConstants.OK_NOTIFICATION.length());
			commands.add(notify);			
		}
		
		Command stop_measure_on_client = new Command(CommandType.STOP_MEASURE_ON_CLIENT_COMMAND);
		commands.add(stop_measure_on_client);
		
		for (int i = 0; i < 5; i++) {
			Command slowdown = new Command(CommandType.SEND_COMMAND);
			slowdown.setRandomByteComponent(false);
			slowdown.setMessage(GlobalConstants.OK_NOTIFICATION.getBytes());
			slowdown.setMessageLength(GlobalConstants.OK_NOTIFICATION.length());
			if (i == 0 || i == 2 || i == 4) {
				slowdown.setExecutionSide(ExecutionSide.CLIENT_SIDE);			
			} else {
				slowdown.setExecutionSide(ExecutionSide.SERVER_SIDE);
			}
			commands.add(slowdown);
		}
		
		Command start_measure_on_server = new Command(CommandType.START_MEASURE_ON_SERVER_COMMAND);
		commands.add(start_measure_on_server);
		
		for (int i = 0; i < GlobalConstants.messages_size.length; i++) {
			
			Command inject = new Command(CommandType.INJECT_PROTOCOL_HEADER_COMMAND);
			commands.add(inject);
						
			Command send = new Command(CommandType.SEND_COMMAND);
			send.setRandomByteComponent(true);
			send.setExecutionSide(ExecutionSide.SERVER_SIDE);
			send.setMessageLength(GlobalConstants.messages_size[i]);
			commands.add(send);
			
			Command notify = new Command(CommandType.SEND_COMMAND);
			notify.setRandomByteComponent(false);			
			notify.setExecutionSide(ExecutionSide.CLIENT_SIDE);
			notify.setMessage(GlobalConstants.OK_NOTIFICATION.getBytes());
			notify.setMessageLength(GlobalConstants.OK_NOTIFICATION.length());
			commands.add(notify);
		}		
		
		Command stop_measure_on_server = new Command(CommandType.STOP_MEASURE_ON_SERVER_COMMAND);
		commands.add(stop_measure_on_server);
		
		Command done = new Command(CommandType.DONE_COMMAND);
		commands.add(done);
						
		return commands;
	}

}
