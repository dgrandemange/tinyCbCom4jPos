package org.jpos.jposext.cbcom.session.service.support.client;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.PseudoSessionStateAbstractImpl;

/**
 * Connected pseudo session state "client" implementation<BR>
 * 
 * @author dgrandemange
 * 
 */
public class ConnectedState extends PseudoSessionStateAbstractImpl implements
		IPseudoSessionState {

	@Override
	public void init(PseudoSessionContext ctx) {
		// Nothing to do do really
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx, int abortCode) {
		// Change session state to : LoggedOff
		changeState(ctx, LoggedOffState.class);
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
		// Change session state to : LoggedOff
		changeState(ctx, LoggedOffState.class);
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// Client should never received an IPDU CN from server
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		// Client should never received an IPDU AC from server when in connected
		// state
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		// Schedule non response timer task
		final PseudoSessionContext fCtx = ctx;
		cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
			public void run() {
				// When TNR expires, emit IPDU-AB code 27 (0x1B)
				emitIpduAB(fCtx, 0x1B);
			}
		}, ctx.getTimerConfig().getNegotiatedTnr(), TimeUnit.SECONDS);
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		try {
			// Proceed to a normal termination of the pseudo session
			emitIpduAB(ctx, 0x80);
		} finally {
			// Update current pseudo session timers using the timers PI returned
			// by server
			updatePseudoSessionTimers(ctx, ctx.getIpdu());
			
			// Don't forget to cancel the non response timer task
			cancelScheduledTask(ctx.getTaskFuture());
		}
	}

	@Override
	public void onIpduDEToSend(PseudoSessionContext ctx) {
		int abortCode = 0x00;

		if (0x00 == abortCode) {
			byte[] bApdu = ctx.getApdu();
			ctx.setApdu(null);

			// Prepare PI07
			byte[] bLen = ByteBuffer.allocate(4).putInt(bApdu.length).array();
			PI pi07 = new PI(PIEnum.PI07, bLen);

			// Populate IPDU-DE with PI01, PI07, and APDU
			IPDU ipduDE = new IPDU(IPDUEnum.DE, new PI[] { pi07 }, bApdu,
					bApdu.length);

			byte[] bIpduDE = ipduDE.toBytes();

			try {
				// Delegate the send operation to the channel callback instance
				ctx.getChannelCallback().send(bIpduDE, true);

				// Transition onIpduDEEmitted
				onIpduDEEmitted(ctx);
			} catch (CBCOMSessionException e) {
				abortCode = 0x04;
			}
		}

		// If there is an IPDU AB to send, process it
		if (0x00 != abortCode) {
			emitIpduAB(ctx, abortCode);
		}
	}

}
