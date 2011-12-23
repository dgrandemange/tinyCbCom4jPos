package org.jpos.jposext.cbcom.session.service.support;

import java.util.concurrent.Future;

import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionStateException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.server.LoggedOffState;

/**
 * Abstract state class providing default behaviors when receiving a message
 * (that is transition method invocation)<BR>
 * 
 * @author dgrandemange
 * 
 */
public abstract class PseudoSessionStateAbstractImpl implements IPseudoSessionState {

	/**
	 * Task to operate when no question underlying channel is inactive for a certain laps of
	 * time : emit IPDU AB
	 * 
	 */
	protected class ChannelInactivityHandler implements Runnable {

		/**
		 * Pseudo session context
		 */
		private PseudoSessionContext ctx;
		
		/**
		 * @param channel
		 *            The underlying channel
		 */
		public ChannelInactivityHandler(PseudoSessionContext ctx) {
			this.ctx = ctx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			// Emit IPDU AB with code 25 (0x19) : inactivity timer expired
			emitIpduAB(ctx, 0x19);
		}

	};	
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.service.IPseudoSessionState#init(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public abstract void init(PseudoSessionContext ctx);
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduCNReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduACEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduDEReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}	
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduDEEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduABReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduABReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2#onIpduABEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduABEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.server.service.IPseudoSessionState#onInvalidIpduReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx) {
		// Send IPDU AB 0x23		
		onInvalidIpduReceived(ctx, 0x23);
	}

	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.session.service.IPseudoSessionState#onInvalidIpduReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext, org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException)
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx,
			CBCOMBadIPDUException e) {
		onInvalidIpduReceived(ctx, e.getReasonEnum().getPv01());
	}	

	/**
	 * @param ctx
	 * @param pv01
	 */
	protected void onInvalidIpduReceived(PseudoSessionContext ctx,
			int pv01) {
		try {
			sendIpduAB(ctx, pv01);
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}		
		
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}
		
		// Change session state to : LoggedOff
		ctx.setSessionState(ctx.getStateFactory().create(LoggedOffState.class));		
	}		

	/**
	 * @param ctx
	 * @param pv01
	 */
	protected void emitIpduAB(PseudoSessionContext ctx, int pv01) {
		// Send IPDU AB
		try {
			sendIpduAB(ctx, pv01);
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		}

		onIpduABEmitted(ctx);
	}	
	
	/**
	 * @param ctx
	 * @param pv01
	 * @throws CBCOMSessionException
	 */
	protected void sendIpduAB(PseudoSessionContext ctx, int pv01) throws CBCOMSessionException {
		
		// Prepare PI01		
		PI pi01 = new PI(PIEnum.PI01, new byte[] { (byte) (pv01 & 0xFF)});
		
		// Prepare IPDU AB
		IPDU ipdu = new IPDU(IPDUEnum.AB, new PI[] {pi01}, null, 0);
		
		// Channel callback to send the IPDU
		byte[] bIpdu = ipdu.toBytes();
		ctx.getChannelCallback().send(bIpdu);
	}	
	
	/**
	 * @param transitionName
	 * @throws CBCOMSessionStateException 
	 */
	protected void transNotImplementedForCurrState() {
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		String state = this.getClass().getSimpleName();
		String transition = ste[2].getMethodName();
		throw new CBCOMSessionStateException(state, transition);
	}
	
	/**
	 * @param inactivityFuture
	 */
	protected void cancelScheduledInactivityHandler(Future<?> inactivityFuture) {
		if (null != inactivityFuture) {
			if (!inactivityFuture.isCancelled()) {
				inactivityFuture.cancel(true);
			}
		}		
	}
	
	/**
	 * @param ctx
	 * @param nextStateClazz
	 */
	protected void changeState(PseudoSessionContext ctx,
			Class<? extends IPseudoSessionState> nextStateClazz) {
		IPseudoSessionState nextState = ctx.getStateFactory().create(nextStateClazz);
		ctx.setSessionState(nextState);
		nextState.init(ctx);
	}
	
}
