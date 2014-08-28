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
import java.util.Arrays;

/**
 *
 * This class provides the command implementation which are executed by the server and the client sides 
 * during the measurement cycle. 
 *
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 */
public class Command implements Serializable {
	
	public static enum CommandType {
		SEND_COMMAND,	
		INJECT_PROTOCOL_HEADER_COMMAND,	
		START_MEASURE_ON_CLIENT_COMMAND,
		STOP_MEASURE_ON_CLIENT_COMMAND,
		START_MEASURE_ON_SERVER_COMMAND,
		STOP_MEASURE_ON_SERVER_COMMAND,
		DONE_COMMAND,
		SEND_UUID_COMMAND;		
	}
	
	public static enum ExecutionSide {
		CLIENT_SIDE,
		SERVER_SIDE,
		UNDEFINED;		
	}
	
	private static final long serialVersionUID = -1439431815966923204L;
	
	/*
	 * this field defines the command type 
	 */
	private CommandType type;
	
	/*
	 * this filed defines which side is an executor for this command 
	 */
	private ExecutionSide executionSide;	
	
	/*
	 * this field defines the message size that command might send 
	 */	
	private int messageLength;
		
	/*
	 * this boolean flag indicates whether the message content is generated randomly, 
	 * or it carries the payload that conform to the tested application
	 */
	private boolean randomByteComponent;
	
	/*
	 * defines the message content
	 */
	private byte[] message;	
	
	public Command() {
		
	}
	
	public Command(CommandType type) {
		this.type = type;
	}
	
	/**
	 * This constructor makes a deep copy of the provided Command object. The deep copy is required for supporting the thread-safe 
	 * measurement test execution on the server component 
	 * 
	 * @param command
	 */
	public Command(Command command) {
		this.type = command.type;
		this.executionSide = command.executionSide;
		this.messageLength = command.messageLength;
		this.randomByteComponent = command.randomByteComponent;
		if (command.message != null) {
			this.message = Arrays.copyOf(command.message, command.message.length);	
		}		
	}

	/**
	 * Returns the message content
	 * @return the message
	 */
	public byte[] getMessage() {
		return message;
	}

	/**
	 * Sets the message content
	 * @param message the message to set
	 */
	public void setMessage(byte[] message) {
		this.message = message;
	}

	/**
	 * Returns the current command type value 
	 * 
	 * @return the type
	 */
	public CommandType getType() {
		return type;
	}

	/**
	 * Sets the command type value
	 * @param type
	 */
	public void setType(CommandType type) {
		this.type = type;
	}

	/**
	 * Returns the execution side value
	 * 
	 * @return the executionSide
	 */
	public ExecutionSide getExecutionSide() {
		return executionSide;
	}

	/**
	 * Sets the execution side value
	 * 
	 * @param executionSide the executionSide to set
	 */
	public void setExecutionSide(ExecutionSide executionSide) {
		this.executionSide = executionSide;
	}

	/**
	 * Sets the message size value
	 * 
	 * @return the messageLength
	 */
	public int getMessageLength() {
		return messageLength;
	}

	/**
	 * Returns the message size value
	 * 
	 * @param messageLength 
	 */
	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}

	/**
	 * @return the randomComponent
	 */
	public boolean isRandomByteComponent() {
		return randomByteComponent;
	}

	/**
	 * @param randomComponent the randomComponent to set
	 */
	public void setRandomByteComponent(boolean randomByteComponent) {
		this.randomByteComponent = randomByteComponent;
	}
	
}
