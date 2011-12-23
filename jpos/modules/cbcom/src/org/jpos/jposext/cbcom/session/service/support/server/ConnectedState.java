package org.jpos.jposext.cbcom.session.service.support.server;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.PseudoSessionStateAbstractImpl;

/**
 * Connected pseudo session state "server" implementation<BR> 
 * 
 * @author dgrandemange
 *
 */
public class ConnectedState extends PseudoSessionStateAbstractImpl implements IPseudoSessionState{

	@Override
	public void init(PseudoSessionContext ctx) {
		// Arm inactivity timer
		ctx.setInactivityFuture(ctx.getDefferedTaskExecutor().schedule(new ChannelInactivityHandler(ctx), ctx.getTsi(),TimeUnit.SECONDS));		
	}
	
	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx) {
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
		
		// Change session state to : LoggedOff
		changeState(ctx, LoggedOffState.class);
		
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
		
		// Change session state to : LoggedOff		
		changeState(ctx, LoggedOffState.class);
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// Unexpected IPDU AC : a pseudo session is already established, invalid sequence
		emitIpduAB(ctx, 0x24);		
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		// Arm inactivity timer
		ScheduledFuture<?> inactivityTaskFuture = ctx.getDefferedTaskExecutor().schedule(new ChannelInactivityHandler(ctx), ctx.getTsi(),TimeUnit.SECONDS);
		ctx.setInactivityFuture(inactivityTaskFuture);
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		cancelScheduledInactivityHandler(ctx.getInactivityFuture());
		
		IPDU ipdu = ctx.getIpdu();
		try {
			ctx.getChannelCallback().processApdu(ipdu.getApdu(), ipdu.getApduLength());
		}
		catch(CBCOMSessionException e) {
			// Send IPDU AB 0x04
			// TODO See if we can be more accurate in our IPDU AB code
			emitIpduAB(ctx, 0x04);
		}
	}
	
}
