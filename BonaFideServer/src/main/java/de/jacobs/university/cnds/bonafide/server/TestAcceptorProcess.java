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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;

import com.beust.jcommander.JCommander;

/**
 * 
 * The main goal of the TestAcceptorProcess process is to accept new TCP client's connections
 * when it initiates a new measurement cycle, and create a new thread to process this measurement cycle.  
 * 
 * @author Vitali Bashko, v.bashko@jacobs-university.de
 *
 */
public class TestAcceptorProcess implements Runnable {

	private static Logger logger = Logger.getLogger(TestAcceptorProcess.class);
	
	private ProtocolDescription protocolHeader;
	private ServerSocket testRunnerSocket;
	private boolean randomFlow;
	
        public TestAcceptorProcess(int port, ProtocolDescription protocolHeader, boolean randomFlow, InetAddress addr) throws IOException {
		this.protocolHeader = protocolHeader;
		this.randomFlow = randomFlow;
		this.testRunnerSocket = new ServerSocket(port, 50, addr);
		if (randomFlow) {
			System.out.println("Socket acceptor for random flow of " + protocolHeader.getProtocolName() + " protocol test has been successfully started. Listening port number: " + port);
			logger.debug("Socket acceptor for random flow of " + protocolHeader.getProtocolName() + " protocol test has been successfully started. Listening port number: " + port);
		} else {
			System.out.println("Socket acceptor for protocol flow of " + protocolHeader.getProtocolName() + " protocol test has been successfully started. Listening port number: " + port);
			logger.debug("Socket acceptor for protocol flow of " + protocolHeader.getProtocolName() + " protocol test has been successfully started. Listening port number: " + port);
		}
	}

	@Override
	public void run() {		
		while (true) {
			try {
				Socket socket = testRunnerSocket.accept();
				logger.debug("New connection accepted from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				new Thread(new TestRunnerProcess(socket, protocolHeader, this.randomFlow)).start();
			} catch (IOException e) {				
				logger.error("Can't start new thread to process execution test for " + protocolHeader.getProtocolName() + " protocol.", e);
			} 
		}
	}

}
