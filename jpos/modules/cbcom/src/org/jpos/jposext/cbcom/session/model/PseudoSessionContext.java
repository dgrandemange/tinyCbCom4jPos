package org.jpos.jposext.cbcom.session.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.service.IIPDUFactory;
import org.jpos.jposext.cbcom.session.service.IChannelCallback;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.ISessionStateFactory;

/**
 * A context dedidacted to a CBCOM pseudo session.<BR>
 * Its life cycle is bounded to the pseudo session life cycle.<BR>
 * Wraps the current pseudo session state. <BR>
 * 
 * @author dgrandemange
 * 
 */
public class PseudoSessionContext {

	/**
	 * Serializable attributes
	 */
	private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

	/**
	 * Volatile attributes
	 */
	transient private Map<String, Object> transientAttributes = new HashMap<String, Object>();

	/**
	 * Pseudo session state
	 */
	private IPseudoSessionState sessionState;
	
	/**
	 * Last IPDU received
	 */
	private IPDU ipdu;

	/**
	 * channel callback
	 */
	protected IChannelCallback channelCallback;
	
	/**
	 * Session state factory
	 */
	protected ISessionStateFactory stateFactory;
	
	/**
	 * ipdu factory
	 */
	protected IIPDUFactory ipduFactory;
	
	/**
	 * Scheduled executor service dedicated to scheduled tasks 
	 */
	private ScheduledExecutorService defferedTaskExecutor;
	
	/**
	 * Future on inactivity task 
	 */
	private Future<?> inactivityFuture = null;

	/**
	 * Inactivity veil timer min value allowed
	 */
	private int minTsi;	

	/**
	 * Inactivity veil timer max value allowed
	 */
	private int maxTsi;	
	
	
	/**
	 * Inactivity veil timer
	 */
	private int tsi;
	
	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value) {
		if (value instanceof Serializable) {
			transientAttributes.remove(key);
			attributes.put(key, (Serializable) value);
		} else {
			attributes.remove(key);
			transientAttributes.put(key, value);
		}
	}

	/**
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		Object obj = attributes.get(key);
		if (null != obj) {
			return 	obj;
		}
		else {
			return transientAttributes.get(key);			
		}
	}
	
	/**
	 * @param key
	 */
	public void remove(String key) {
			transientAttributes.remove(key);
			attributes.remove(key);
	}	
	
	/**
	 * 
	 */
	public void clear() {
		attributes.clear();
		transientAttributes.clear();
	}

	public IPseudoSessionState getSessionState() {
		return sessionState;
	}

	public void setSessionState(IPseudoSessionState sessionState) {
		this.sessionState = sessionState;
	}

	public IPDU getIpdu() {
		return ipdu;
	}

	public void setIpdu(IPDU ipdu) {
		this.ipdu = ipdu;
	}

	public IChannelCallback getChannelCallback() {
		return channelCallback;
	}

	public void setChannelCallback(IChannelCallback channelCallback) {
		this.channelCallback = channelCallback;
	}

	public ISessionStateFactory getStateFactory() {
		return stateFactory;
	}

	public void setStateFactory(ISessionStateFactory stateFactory) {
		this.stateFactory = stateFactory;
	}

	public IIPDUFactory getIpduFactory() {
		return ipduFactory;
	}

	public void setIpduFactory(IIPDUFactory ipduFactory) {
		this.ipduFactory = ipduFactory;
	}

	public Future<?> getInactivityFuture() {
		return inactivityFuture;
	}

	public void setInactivityFuture(Future<?> inactivityFuture) {
		this.inactivityFuture = inactivityFuture;
	}

	public int getTsi() {
		return tsi;
	}

	public void setTsi(int tsi) {
		this.tsi = tsi;
	}

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

	public ScheduledExecutorService getDefferedTaskExecutor() {
		return defferedTaskExecutor;
	}

	public void setDefferedTaskExecutor(
			ScheduledExecutorService defferedTaskExecutor) {
		this.defferedTaskExecutor = defferedTaskExecutor;
	}
}
