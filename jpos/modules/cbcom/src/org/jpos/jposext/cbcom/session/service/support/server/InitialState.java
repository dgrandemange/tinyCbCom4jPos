package org.jpos.jposext.cbcom.session.service.support.server;

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
 * Initial pseudo session state "server" implementation<BR>
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

		// Schedule pre-connection timer task
		final PseudoSessionContext fCtx = ctx;
		cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
			public void run() {
				// On pre-connection timer expiration, close network
				// communication
				try {
					fCtx.getChannelCallback().close();
				} catch (CBCOMSessionException e) {
					// TODO Is there anything we can do here ?
					e.printStackTrace();
				}
			}
		}, ctx.getTimerConfig().getPreConnectionTimer(), TimeUnit.SECONDS);
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx, int abortCode) {
		try {
			// Schedule inter-session timer task
			final PseudoSessionContext fCtx = ctx;
			cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
				public void run() {
					// On inter-session timer expiration, close network
					// communication
					try {
						fCtx.getChannelCallback().close();
					} catch (CBCOMSessionException e) {
						// TODO Is there anything we can do here ?
						e.printStackTrace();
					}
				}
			}, ctx.getTimerConfig().getInterSessionTimer(), TimeUnit.SECONDS);
		} finally {
			// Change session state to : LoggedOff
			changeState(ctx, LoggedOffState.class);
		}
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
		// We receive an IPDU AB from client -> close the network communication
		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore
			e.printStackTrace();
		} finally {
			// Change session state to : LoggedOff
			changeState(ctx, LoggedOffState.class);
		}
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		// Change session state to : Connected
		changeState(ctx, ConnectedState.class);
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		// Server should never receive an IPDU-AC from client
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// First, we cancel the pre-connection timer task
		cancelScheduledTask(ctx.getTaskFuture());

		// Then, we proceed
		int abortCode = 0x00;

		IPDU ipduCN = ctx.getIpdu();

		PI pi05 = ipduCN.findPiByPIEnum(PIEnum.PI05);
		// Check PI05
		// By default, we consider pi05 bad
		abortCode = 0x26;
		if (pi05.getParamLen() == 4) {
			byte clientCbcomVersion = pi05.getParamValue()[0];
			byte clientProtocolType = pi05.getParamValue()[1];
			byte[] clientCb2aVersion = new byte[] { pi05.getParamValue()[2],
					pi05.getParamValue()[3] };

			if ((byte) 0x02 != clientProtocolType) {
				// Transactional CB2A protocol type was expected
				abortCode = 0x1F;
			} else {
				// Check this : version 1.1 <= Client CBCOM version <= Server
				// CBCOM version
				if ((ctx.getCbcomProtocolVersion() < clientCbcomVersion)
						|| (clientCbcomVersion < 0x11)) {
					abortCode = 0x1E;
				} else {
					// TODO Check CB2A version if required

					// When CB2A version is OK, set abortCode to 0x00
					abortCode = 0x00;
				}
			}
		}

		List<PI> lstPis = new ArrayList<PI>();

		if (0x00 == abortCode) {
			abortCode = proceedToTimerNegotiation(ctx, ipduCN);
		}

		if (0x00 == abortCode) {
			// Acknowledge connection
			// Create an IPDU AC with PI01 with pseudo session code=00 (OK, no
			// anomaly)
			PI pi01 = new PI(PIEnum.PI01, new byte[] { 0x00 });
			lstPis.add(pi01);

			// Add PIs potentially issued from a previous timer negotiation
			lstPis.addAll(ctx.getTimerPIs());
			ctx.setTimerPIs(null);

			PI[] pis = lstPis.toArray(new PI[lstPis.size()]);
			IPDU ipduAc = new IPDU(IPDUEnum.AC, pis, null, 0);

			// Channel callback to send IPDU
			byte[] bIpduAC = ipduAc.toBytes();

			try {
				ctx.getChannelCallback().send(bIpduAC, false);

				// Send transition IPDU AC emitted
				onIpduACEmitted(ctx);
			} catch (CBCOMSessionException e) {
				abortCode = 0x04;
			}
		}

		// If there is an IPDU AB to send, process it
		if (0x00 != abortCode) {
			emitIpduAB(ctx, abortCode);
		}

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
		// Send IPDU AB 0x24
		emitIpduAB(ctx, 0x24);
	}

}
