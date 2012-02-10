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
	 * @param bIpdu
	 * @param doCount
	 * @throws CBCOMSessionException
	 */
	void send(byte[] bIpdu, boolean doCount) throws CBCOMSessionException;

	/**
	 * @param msg
	 */
	void log(String tag,Object msg);
}
