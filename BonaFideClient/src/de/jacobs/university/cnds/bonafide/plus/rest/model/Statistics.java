package de.jacobs.university.cnds.bonafide.plus.rest.model;

/**
 * CLASS NOT USED IN CURRENT VERSION
 */

import de.jacobs.university.cnds.bonafide.plus.utils.ResultAnalyzer.Decision;

public class Statistics {
	private int U;
	private int Ucritical;
	
	private int Pmean;
	private int Rmean;
	
	private String Pinterval;
	private String Rinterval;
	
	private Decision decisionByData;
	private Decision decisionByCompleteness;
	
	/**
	 * @return the u
	 */
	public int getU() {
		return U;
	}
	/**
	 * @param u the u to set
	 */
	public void setU(int u) {
		U = u;
	}
	/**
	 * @return the ucritical
	 */
	public int getUcritical() {
		return Ucritical;
	}
	/**
	 * @param ucritical the ucritical to set
	 */
	public void setUcritical(int ucritical) {
		Ucritical = ucritical;
	}
	/**
	 * @return the pmean
	 */
	public int getPmean() {
		return Pmean;
	}
	/**
	 * @param pmean the pmean to set
	 */
	public void setPmean(int pmean) {
		Pmean = pmean;
	}
	/**
	 * @return the rmean
	 */
	public int getRmean() {
		return Rmean;
	}
	/**
	 * @param rmean the rmean to set
	 */
	public void setRmean(int rmean) {
		Rmean = rmean;
	}
	/**
	 * @return the pinterval
	 */
	public String getPinterval() {
		return Pinterval;
	}
	/**
	 * @param pinterval the pinterval to set
	 */
	public void setPinterval(String pinterval) {
		Pinterval = pinterval;
	}
	/**
	 * @return the rinterval
	 */
	public String getRinterval() {
		return Rinterval;
	}
	/**
	 * @param rinterval the rinterval to set
	 */
	public void setRinterval(String rinterval) {
		Rinterval = rinterval;
	}
	/**
	 * @return the decision
	 */
	public Decision getDecisionByData() {
		return decisionByData;
	}
	/**
	 * @param decision the decision to set
	 */
	public void setDecisionByData(Decision decision) {
		this.decisionByData = decision;
	}
	/**
	 * @return the decisionByCompleteness
	 */
	public Decision getDecisionByCompleteness() {
		return decisionByCompleteness;
	}
	/**
	 * @param decisionByCompleteness the decisionByCompleteness to set
	 */
	public void setDecisionByCompleteness(Decision decisionByCompleteness) {
		this.decisionByCompleteness = decisionByCompleteness;
	}	
}
