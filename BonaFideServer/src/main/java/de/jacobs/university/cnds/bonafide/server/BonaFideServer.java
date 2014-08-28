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
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.beust.jcommander.JCommander;

import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;
import de.jacobs.university.cnds.bonafide.notificators.CentralServerNotificator;
import de.jacobs.university.cnds.bonafide.notificators.entities.MeasurementServerAdvertisement;
import de.jacobs.university.cnds.bonafide.utils.GlobalConstants;

/**
*
* Application entry point.
* 
* @author Vitali Bashko, v.bashko@jacobs-university.de
*
*/
public class BonaFideServer {

	private static Logger logger = Logger.getLogger(BonaFideServer.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String inEclipseStr = System.getProperty("runInEclipse");
		boolean inEclipse = "true".equalsIgnoreCase(inEclipseStr);
		
		if (inEclipse) {
			PropertyConfigurator.configure("log4j.properties");
		}
		
		// load config parameters
		JCommanderArgument argument = new JCommanderArgument();
		JCommander commander = new JCommander(argument);
		commander.setProgramName(GlobalConstants.SERVER_NAME);
		try {
			// parse command-line arguments
			commander.parse(args);
			// load from config file - all already provided via command-line will stay untouched
			argument.appendConfigFromFile();
			
			if (!argument.isConfigComplete()) {
				System.err.println("Not all required parameters are provided. Parameters can be provided by config file or by command-line arguments. Command-line arguments overwrite parameters from the config file.");
				commander.usage();
				logger.error("Can't start application. Missing parameters.");
				return;
			}
			
			if (argument.portNumber != null && (argument.portNumber < 1024 || argument.portNumber > 65535)) {
				System.err.println("The port number must be in range form 1024 to 65535");
				commander.usage();
				logger.error("Can't start application. Provided port number is out of range.");
				return;
			}
			
			if (argument.getLogLevel() == null) {
				System.err.println("Wring log level value provided.");
				commander.usage();
				logger.error("Wrong log level value provided.");
				return;
			}
						
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());						
			commander.usage();
			logger.error("Can't start application. Error occured while parsing input parameters.", e);
			return;
		}
		
		LogManager.getRootLogger().setLevel(argument.getLogLevel());
		
		System.out.print("Initialize application context......\t");
		BonaFideServerContext context = null;
		try {
			context = BonaFideServerContext.initializeInstance(argument.list, argument.storage);	
		} catch (IOException e) {
			System.out.println(":(");
			System.err.println(e.getMessage());
			logger.error("Can't start application. Error occured while context initializing.", e);
			return;
		}
		System.out.println(":)");
		
		try {
		        System.out.println("Binding to IP address " + argument.ipAddr);
		        InetAddress addr = InetAddress.getByName(argument.ipAddr);
			loadTestAcceptors(context.getProtocolHeaders(), addr);
			logger.info("Test acceptor sockets successfully started.");
		} catch (IOException e) {
			System.out.println("Error! Can't run test acceptor sockets.");
			logger.error("Can't start application. Error occured starting test acceptor sockets.", e);
			return;
		}
		
		int port = 0;
		if (argument.portNumber == null) {
			port = GlobalConstants.DEFAULT_PORT_NUMBER;
		} else {
			port = argument.portNumber; 
		}
		/*
		 *	Run main server socket 
		 */
		ServerSocket mainSocket = null;
		try {
		        InetAddress addr = InetAddress.getByName(argument.ipAddr);
		        mainSocket = new ServerSocket(port, 50, addr);
			System.out.println("Main socket successfully started on port " + port);
			logger.debug("Main socket successfully started on port " + port);
			
			System.out.print("Starting central server notification service......\t");
			CentralServerNotificator.startService(new MeasurementServerAdvertisement(argument.centralServerUrl, argument.name, argument.portNumber, argument.latitude, argument.longitude));
			System.out.println(":)");
			
			while (true) {
				Socket socket = mainSocket.accept();
				MainSocketProcess process = new MainSocketProcess(socket);
				logger.info("New connection accepted from: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				new Thread(process).start();
			}
			
		} catch (IOException e) {
			System.err.println("Can't create main socket.");
			System.err.println(e.getMessage());
			logger.error("Can't start application. Start main socket failed.", e);
			return;
		} finally {
				if (mainSocket != null) {
					try {
						mainSocket.close();	
					} catch (Exception e) {
					}					
				}
		}
	}
	
	/*
	 * private static method that iterates through the list of protocol descriptions and
	 * runs two separate threads (one for random flow, one for protocol flow) for processing 
	 * clients measurement tests 
	 */
         private static void loadTestAcceptors(List<ProtocolDescription> headers, InetAddress addr) throws IOException {
		for (ProtocolDescription header: headers) {
		    new Thread(new TestAcceptorProcess(header.getPFPort(), header, false, addr)).start();
		    new Thread(new TestAcceptorProcess(header.getRFPort(), header, true, addr)).start();			
		}
	}

}
