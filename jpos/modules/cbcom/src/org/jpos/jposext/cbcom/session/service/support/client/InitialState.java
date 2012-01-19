package org.jpos.jposext.cbcom.session.service.support.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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
 * Initial pseudo session state "client" implementation<BR>
 * 
 * @author dgrandemange
 * 
 */
public class InitialState extends PseudoSessionStateAbstractImpl implements
		IPseudoSessionState {

	@Override
	public void init(PseudoSessionContext ctx) {
		// New pseudo session debut : compute a pseudo session for internal use
		resetCtxWorkingVars(ctx);
		initCtxId(ctx);
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
		// // N/A
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
		// Schedule post connection timer task
		final PseudoSessionContext fCtx = ctx;
		cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
			public void run() {
				try {
					fCtx.getChannelCallback().close();
				} catch (CBCOMSessionException e) {
					// TODO Is there anything we can do here ?
					e.printStackTrace();
				}
			}
		}, ctx.getTimerConfig().getPostCnxTimer(), TimeUnit.SECONDS);
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		// Cancel post-connection timer task
		cancelScheduledTask(ctx.getTaskFuture());

		// Update current pseudo session timers using the timers PI returned by
		// server
		updatePseudoSessionTimers(ctx, ctx.getIpdu());

		// Change session state to : Connected
		changeState(ctx, ConnectedState.class);

		// There is an APDU to process, so we call the transition to
		// onIpduDEToSend() on the new state
		ctx.getSessionState().onIpduDEToSend(ctx);
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		// Send IPDU AB 0x24
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduDEToSend(PseudoSessionContext ctx) {
		// At this point, the pseudo session is not yet established.
		// So before sending any IPDU DE, we must establish this pseudo
		// session by first sending an IPDU CN (that is a connection request)

		int abortCode = 0x00;

		if (0x00 == abortCode) {
			List<PI> lstPIs = new ArrayList<PI>();

			// First, create a PI05
			byte cbcomProtocolVersion = ctx.getCbcomProtocolVersion();
			byte protocolType = ctx.getProtocolType();
			byte[] cb2aProtocolVersion = ctx.getCb2aProtocolVersion();
			byte[] pv05 = new byte[1 + 1 + cb2aProtocolVersion.length];
			pv05[0] = cbcomProtocolVersion;		
			pv05[1] = protocolType;			
			System.arraycopy(cb2aProtocolVersion, 0, pv05, 2,
					cb2aProtocolVersion.length);

			PI pi05 = new PI(PIEnum.PI05, pv05);
			lstPIs.add(pi05);

			// TNR Negotiation
			int tnr = ctx.getTimerConfig().getInitialTnr();
			byte[] pv16 = ByteBuffer.allocate(4).putInt(tnr).array();
			PI pi16 = new PI(PIEnum.PI16, new byte[] { pv16[2], pv16[3] });
			lstPIs.add(pi16);

			// TSI Negotiation
			int tsi = ctx.getTimerConfig().getInitialTsi();
			if (0 != tsi) {
				byte[] pv17 = ByteBuffer.allocate(4).putInt(tsi).array();
				PI pi17 = new PI(PIEnum.PI17, new byte[] { pv17[2], pv17[3] });
				lstPIs.add(pi17);
			}

			// TMA Negotiation
			int tma = ctx.getTimerConfig().getInitialTma();
			if (0 != tma) {
				byte[] pv18 = ByteBuffer.allocate(4).putInt(tma).array();
				PI pi18 = new PI(PIEnum.PI18, new byte[] { pv18[2], pv18[3] });
				lstPIs.add(pi18);
			}

			// Create the IPDU CN
			PI[] pis = new PI[lstPIs.size()];
			pis = lstPIs.toArray(pis);
			IPDU ipduCN = new IPDU(IPDUEnum.CN, pis, null, 0);
			byte[] bIpduCN = ipduCN.toBytes();

			try {
				// Callback underlying channel to send the IPDU
				ctx.getChannelCallback().send(bIpduCN, false);

				// Transition IPDU CN emitted
				onIpduCNEmitted(ctx);
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
