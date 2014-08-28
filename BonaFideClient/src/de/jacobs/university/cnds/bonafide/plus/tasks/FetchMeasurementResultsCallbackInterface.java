package de.jacobs.university.cnds.bonafide.plus.tasks;

import de.jacobs.university.cnds.bonafide.plus.adapter.ResultsAdapter;

public interface FetchMeasurementResultsCallbackInterface {
	/**
	 * accepts ResultsAdapter and registers it to the view. It recieves null when an error occured
	 * @param resultsAdapter
	 */
	public void measurementResultsRecieved(ResultsAdapter resultsAdapter);
}
