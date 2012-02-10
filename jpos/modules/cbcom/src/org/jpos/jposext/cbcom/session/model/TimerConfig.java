package org.jpos.jposext.cbcom.session.model;

/**
 * Wraps all timers related to CBCOM and pesudo session management
 * 
 * @author dgrandemange
 * 
 */
public class TimerConfig {

	private int minTsi;

	private int maxTsi;
	
	private int initialTsi;
	
	private int negotiatedTsi;

	private int initialTnr;
	
	private int negotiatedTnr;

	private int tgr;

	private int minTnr;

	private int maxTnr;

	private int minTma;

	private int maxTma;

	private int initialTma;
	
	private int negotiatedTma;

	/**
	 * post-connection timer
	 */
	private int postCnxTimer;

	/**
	 * pre-connection timer
	 */
	private int preConnectionTimer;

	/**
	 * inter-session timer
	 */
	private int interSessionTimer;

	public int getMinTsi() {
		return minTsi;
	}

	public void setMinTsi(int minTsi) {
		this.minTsi = minTsi;
	}

	public int getMaxTsi() {
		return maxTsi;
	}

	public void setMaxTsi(int maxTsi) {
		this.maxTsi = maxTsi;
	}

	public int getNegotiatedTsi() {
		return negotiatedTsi;
	}

	public void setNegotiatedTsi(int tsi) {
		this.negotiatedTsi = tsi;
	}

	public int getNegotiatedTnr() {
		return negotiatedTnr;
	}

	public void setNegotiatedTnr(int tnr) {
		this.negotiatedTnr = tnr;
	}

	public int getTgr() {
		return tgr;
	}

	public void setTgr(int tgr) {
		this.tgr = tgr;
	}

	public int getMinTnr() {
		return minTnr;
	}

	public void setMinTnr(int minTnr) {
		this.minTnr = minTnr;
	}

	public int getMaxTnr() {
		return maxTnr;
	}

	public void setMaxTnr(int maxTnr) {
		this.maxTnr = maxTnr;
	}

	public int getMinTma() {
		return minTma;
	}

	public void setMinTma(int minTma) {
		this.minTma = minTma;
	}

	public int getMaxTma() {
		return maxTma;
	}

	public void setMaxTma(int maxTma) {
		this.maxTma = maxTma;
	}

	public int getNegotiatedTma() {
		return negotiatedTma;
	}

	public void setNegotiatedTma(int tma) {
		this.negotiatedTma = tma;
	}

	public int getPostCnxTimer() {
		return postCnxTimer;
	}

	public void setPostCnxTimer(int postCnxTimer) {
		this.postCnxTimer = postCnxTimer;
	}

	public int getPreConnectionTimer() {
		return preConnectionTimer;
	}

	public void setPreConnectionTimer(int preConnectionTimer) {
		this.preConnectionTimer = preConnectionTimer;
	}

	public int getInterSessionTimer() {
		return interSessionTimer;
	}

	public void setInterSessionTimer(int interSessionTimer) {
		this.interSessionTimer = interSessionTimer;
	}

	public int getInitialTsi() {
		return initialTsi;
	}

	public void setInitialTsi(int initialTsi) {
		this.initialTsi = initialTsi;
	}

	public int getInitialTnr() {
		return initialTnr;
	}

	public void setInitialTnr(int initialTnr) {
		this.initialTnr = initialTnr;
	}

	public int getInitialTma() {
		return initialTma;
	}

	public void setInitialTma(int initialTma) {
		this.initialTma = initialTma;
	}
}
