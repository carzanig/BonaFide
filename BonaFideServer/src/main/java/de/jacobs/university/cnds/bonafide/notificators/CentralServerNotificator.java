package de.jacobs.university.cnds.bonafide.notificators;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.jmx.LoggerDynamicMBean;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import java.util.List;
import org.apache.log4j.Logger;

import de.jacobs.university.cnds.bonafide.notificators.entities.AdvertisementResponse;
import de.jacobs.university.cnds.bonafide.notificators.entities.MeasurementServerAdvertisement;
import de.jacobs.university.cnds.bonafide.server.JCommanderArgument;

/**
 * This class implements notification service with the central server about availability of this measurement server. This service runs as a Thread.
 * @author Tomas Ludrovan
 *
 */
public class CentralServerNotificator extends Thread {
	private static CentralServerNotificator self;
	private MeasurementServerAdvertisement measurementServerAdvertisement;
	
	private static Logger logger = Logger.getLogger(CentralServerNotificator.class.getName());
	
	private CentralServerNotificator(MeasurementServerAdvertisement measurementServerAdvertisement) {
		// singleton
		this.measurementServerAdvertisement=measurementServerAdvertisement;
	}
	
	public static synchronized void startService(MeasurementServerAdvertisement measurementServerAdvertisement) {
		if (self==null) {
			self=new CentralServerNotificator(measurementServerAdvertisement);
			self.start();
		}
	}
	
	public static synchronized void stopService() {
		if (self!=null) {
			self.interrupt();
			self=null;
		}
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Client client = new Client();
				WebResource webResource = client
				   .resource(measurementServerAdvertisement.getCentralServerUri()).path("measurement-servers").path("add");
		 
				ClientResponse response = webResource.accept("application/json").type(MediaType.APPLICATION_JSON).post(ClientResponse.class,measurementServerAdvertisement);
		 
				if (response.getStatus() == 200) {
					// OK response from the server
					
					AdvertisementResponse res = response.getEntity(AdvertisementResponse.class);
					logger.info("Next advertisement to the central server in: "+res.getNextAdvertisementDelay()+"s");
					Thread.sleep(res.getNextAdvertisementDelay()*1000);
				}
				else {
					// possible server internal error
					logger.error("Central server responded with code "+response.getStatus()+" "+response.getStatusInfo());
					
					// ignore and repeat after the default timeout
					AdvertisementResponse res = new AdvertisementResponse();
					Thread.sleep(res.getNextAdvertisementDelay()*1000);
				}
	
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					break;
				}
				e.printStackTrace();
				logger.error(e.getLocalizedMessage());
				AdvertisementResponse res = new AdvertisementResponse();
				try {
					Thread.sleep(res.getNextAdvertisementDelay()*1000);
				} catch (Exception ex) { }
			}
		}
	}
	
}
