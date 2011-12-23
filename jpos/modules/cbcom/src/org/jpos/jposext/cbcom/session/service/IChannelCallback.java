package org.jpos.jposext.cbcom.session.service;

import org.jpos.jposext.cbcom.exception.CBCOMSessionException;

/**
 * Channel callback interface
 * 
 * @author dgrandemange
 *
 */
public interface IChannelCallback {

	/**
	 * This callback method should close the channel an ensure it is closed
	 */
	void close() throws CBCOMSessionException;

	/**
	 * Send some data over channel
	 * 
	 * @param bIpduAC
	 */
	void send(byte[] bIpduAC) throws CBCOMSessionException;

	/**
	 * Process APDU
	 * 
	 * @param apdu APDU to process
	 * @param apduLen 
	 */
	void processApdu(byte[] apdu, int apduLen) throws CBCOMSessionException;

}
