package org.jpos.jposext.cbcom.session.service.support.server;

import org.jpos.jposext.cbcom.exception.CBCOMSessionClosedException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.PseudoSessionStateAbstractImpl;

/**
 * Logged off pseudo session state "server" implementation<BR>
 * 
 * @author dgrandemange
 * 
 */
public class LoggedOffState extends PseudoSessionStateAbstractImpl implements
		IPseudoSessionState {

	@Override
	public void init(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx, int abortCode) {
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
		// We receive an IPDU AB from client -> close the network communication
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// Cancel the inter session timer task
		cancelScheduledTask(ctx.getTaskFuture());

		// Change to initial state
		changeState(ctx, InitialState.class);

		// Force transition onIpduCNReceived
		ctx.getSessionState().onIpduCNReceived(ctx);
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		// Server should not receive an IPDU-DE from client while logged off
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduDEToSend(PseudoSessionContext ctx) {
		// Sorry, too late to send an IPDU-DE : the pseudo session is logged off
		throw new CBCOMSessionClosedException();
	}

}
