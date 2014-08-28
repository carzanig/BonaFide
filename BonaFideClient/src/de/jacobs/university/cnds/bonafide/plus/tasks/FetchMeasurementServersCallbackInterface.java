package de.jacobs.university.cnds.bonafide.plus.tasks;

import java.util.List;

import de.jacobs.university.cnds.bonafide.plus.rest.RestException;
import de.jacobs.university.cnds.bonafide.plus.rest.model.MeasurementServer;

public interface FetchMeasurementServersCallbackInterface {
	/**
	 * returns list of available measurement servers or null when an error occured
	 * @param measurementServers
	 */
	public void measurementServersRecieved(List<MeasurementServer> measurementServers);
}
