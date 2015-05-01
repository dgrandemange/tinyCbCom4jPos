package fr.dgrandemange.cbcom.session.service.support;

import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.channel.CBCOMChannel;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import fr.dgrandemange.cbcom.exception.CBCOMSessionException;
import fr.dgrandemange.cbcom.session.service.IChannelCallback;

/**
 * Callback interface <BR>
 * Callbacks are called by the CBCOM state machine<BR>
 * This implementation mainly rely on a CBCOMChannel instance<BR>
 * 
 * @author dgrandemange
 * 
 */
public class ChannelCallbackImpl implements IChannelCallback {

	private CBCOMChannel channel;

	public ChannelCallbackImpl(CBCOMChannel channel) {
		super();
		this.channel = channel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.IChannelCallback#send
	 * (byte[])
	 */
	public void send(byte[] b, boolean doCount)
			throws CBCOMSessionException {
		try {
			// Delegate the send operation to the underlying channel
			channel.send(b, doCount);
		} catch (IOException e) {
			throw new CBCOMSessionException(e);
		} catch (ISOException e) {
			throw new CBCOMSessionException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.IChannelCallback#close
	 * ()
	 */
	public void close() throws CBCOMSessionException {
		try {
			// Delegate the close operation to the underlying channel
			channel.closeSocket();
		} catch (IOException e) {
			// Safe to ignore, it may be already closed
		}
	}

	/**
	 * @param tag
	 * @param msg
	 */
	public void log(String tag, Object msg) {
		LogEvent evt = new LogEvent(channel, tag);
		evt.addMessage(msg);
		Logger.log(evt);
	}

}
