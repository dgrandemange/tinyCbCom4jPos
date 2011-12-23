package org.jpos.jposext.cbcom.session.service.support.server;

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
		// Nothing to do really
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
		// Change session state to : Connected
		changeState(ctx, ConnectedState.class);
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		int abortCode = 0x00;

		IPDU ipduCN = ctx.getIpdu();
		// TODO Here we may check cbcom/app protocol versions if needed
		// (Using pre-defined attributes in PseudoSessionContext ?)

		// Handle timers negotiation if dedicated PI are present in IPDU CN
		if (0x00 == abortCode) {

			// 1. PI17 : Inactivity timer (TSI) 
			PI pi17 = ipduCN.findPiByPIEnum(PIEnum.PI17);
			if (null != pi17) {
				byte[] bPv = pi17.getParamValue();
				int tsi = bPv[0] << 8 | bPv[1];
				if ((tsi >= ctx.getMinTsi()) && (tsi <= ctx.getMaxTsi())) {
					ctx.setTsi(tsi);
				} else {
					abortCode = 0x16;
				}
			}
		}

		if (0x00 == abortCode) {
			// Acknowledge connection

			// Create an IPDU AC with PI01 with pseudo session code=00 (OK, no
			// anomaly)
			PI pi01 = new PI(PIEnum.PI01, new byte[] { 0x00 });
			IPDU ipduAc = new IPDU(IPDUEnum.AC, new PI[] { pi01 }, null, 0);

			// Channel callback to send IPDU
			byte[] bIpduAC = ipduAc.toBytes();

			try {
				ctx.getChannelCallback().send(bIpduAC);

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

}
