package de.jacobs.university.cnds.bonafide.plus.tasks;

import java.util.List;

import de.jacobs.university.cnds.bonafide.model.ProtocolDescription;

public interface FetchAvailableProtocolsCallbackInterface {
	/**
	 * returns list of available measurement servers or null when an error occured
	 * @param measurementServers
	 */
	public void availableProtocolsRecieved(List<ProtocolDescription> availableProtocols);
}
