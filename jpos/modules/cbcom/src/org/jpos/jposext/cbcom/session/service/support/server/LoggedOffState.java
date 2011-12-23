package org.jpos.jposext.cbcom.session.service.support.server;

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
		cancelScheduledInactivityHandler(ctx.getInactivityFuture());
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
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
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
	}

}
