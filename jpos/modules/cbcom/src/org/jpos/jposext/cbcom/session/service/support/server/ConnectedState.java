package org.jpos.jposext.cbcom.session.service.support.server;

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
 * Connected pseudo session state "server" implementation<BR>
 * 
 * @author dgrandemange
 * 
 */
public class ConnectedState extends PseudoSessionStateAbstractImpl implements
		IPseudoSessionState {

	@Override
	public void init(PseudoSessionContext ctx) {
		// Schedule inactivity timer task
		final PseudoSessionContext fCtx = ctx;
		cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
			public void run() {
				// Emit IPDU AB with code 25 (0x19) : inactivity
				// timer expired
				emitIpduAB(fCtx, 0x19);
			}
		}, ctx.getTimerConfig().getNegotiatedTsi(), TimeUnit.SECONDS);
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx, int abortCode) {
		boolean closeCnx = true;

		try {

			if (0x80 == abortCode) {
				closeCnx = false;
			}

			if (closeCnx) {
				try {
					ctx.getChannelCallback().close();
				} catch (CBCOMSessionException e) {
					// Safe to ignore
				} finally {
					// Cancel any scheduled task
					cancelScheduledTask(ctx.getTaskFuture());
				}
			} else {
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
				}, ctx.getTimerConfig().getInterSessionTimer(),
						TimeUnit.SECONDS);
			}
		} finally {
			// Change session state to : LoggedOff
			changeState(ctx, LoggedOffState.class);
		}
	}

	public void OLD_onIpduABEmitted_OLD(PseudoSessionContext ctx) {
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
		// We received an IPDU AB from client
		// Default behavior is : we close the network communication
		// BUT, in case of a normal termination (abort code 0x80),
		// I choose to keep the network communication alive.
		// Doing this allow the client to establish a new pseudo session
		// using the same underlying channel.
		// Still, we schedule an inter-session timer that will care of closing
		// the communication in case client does not further request a
		// pseudo-session establishment (IPDU-CN).

		boolean closeCnx = true;

		try {
			IPDU ipdu = ctx.getIpdu();
			PI pi01 = ipdu.findPiByPIEnum(PIEnum.PI01);

			if (pi01 != null) {
				byte bCode = pi01.getParamValue()[0];
				if (((byte) 0x80) == bCode) {
					closeCnx = false;
				}
			}

			if (closeCnx) {
				try {
					ctx.getChannelCallback().close();
				} catch (CBCOMSessionException e) {
					// Safe to ignore
				} finally {
					// Cancel any scheduled task
					cancelScheduledTask(ctx.getTaskFuture());
				}
			} else {
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
				}, ctx.getTimerConfig().getInterSessionTimer(),
						TimeUnit.SECONDS);
			}
		} finally {
			// Change session state to : LoggedOff
			changeState(ctx, LoggedOffState.class);
		}
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// Unexpected IPDU CN : a pseudo session is already established, invalid
		// sequence
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		// Server should never received an IPDU AC from client.
		// Besides pseudo-session is already connected here
		emitIpduAB(ctx, 0x24);
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
		// N/A
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		// Schedule inactivity timer task
		final PseudoSessionContext fCtx = ctx;
		cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
			public void run() {
				// Emit IPDU AB with code 25 (0x19) : inactivity
				// timer expired
				emitIpduAB(fCtx, 0x19);
			}
		}, ctx.getTimerConfig().getNegotiatedTsi(), TimeUnit.SECONDS);
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		// Cancel the scheduled inactivity timer task
		cancelScheduledTask(ctx.getTaskFuture());

		// Handle potential timer negotiation
		int abortCode = proceedToTimerNegotiation(ctx, ctx.getIpdu());

		if (0x00 == abortCode) {
			// Schedule response guarantee timer task
			final PseudoSessionContext fCtx = ctx;
			cancelAndReplaceScheduledTimerTask(ctx, new Runnable() {
				public void run() {
					// When response guarantee timer expires,
					// emit IPDU AB with code 26 (0x1A)
					emitIpduAB(fCtx, 0x1A);
				}
			}, ctx.getTimerConfig().getTgr(), TimeUnit.SECONDS);
		} else {
			// If there is an IPDU AB to send, process it
			emitIpduAB(ctx, abortCode);
		}
	}

	@Override
	public void onIpduDEToSend(PseudoSessionContext ctx) {
		ctx.getChannelCallback().log(
				"server-connected",
				String.format("ctxId=%s\nonIpduDEToSend", ctx.getId()));
		
		// First, we cancel the scheduled response guarantee timer task
		cancelScheduledTask(ctx.getTaskFuture());

		// Then, we proceed to send the IPDU-DE
		int abortCode = 0x00;

		byte[] bApdu = ctx.getApdu();
		ctx.setApdu(null);

		List<PI> lstPis = new ArrayList<PI>();

		// Prepare PI07
		byte[] bLen = ByteBuffer.allocate(4).putInt(bApdu.length).array();
		PI pi07 = new PI(PIEnum.PI07, bLen);
		lstPis.add(pi07);

		// This IPDU DE is a response, so we must set a PI01
		PI pi01 = new PI(PIEnum.PI01, new byte[] { 0x00 });
		lstPis.add(pi01);

		// Add PIs potentially issued from a previous timer negotiation
		lstPis.addAll(ctx.getTimerPIs());
		ctx.setTimerPIs(null);

		if (0x00 == abortCode) {
			// Populate IPDU-DE
			PI[] pis = lstPis.toArray(new PI[lstPis.size()]);
			IPDU ipduDE = new IPDU(IPDUEnum.DE, pis, bApdu, bApdu.length);

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
