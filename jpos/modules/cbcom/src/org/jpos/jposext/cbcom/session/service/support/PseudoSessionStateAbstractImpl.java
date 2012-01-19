package org.jpos.jposext.cbcom.session.service.support;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionStateException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.model.TimerConfig;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.server.LoggedOffState;

/**
 * Abstract state class providing default behaviors when receiving a message
 * (that is transition method invocation)<BR>
 * 
 * @author dgrandemange
 * 
 */
public abstract class PseudoSessionStateAbstractImpl implements
		IPseudoSessionState {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.service.IPseudoSessionState#init(org.jpos
	 * .jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public abstract void init(PseudoSessionContext ctx);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduCNReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduACEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	@Override
	public void onIpduACReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	@Override
	public void onIpduCNEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduDEReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduDEEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.service.IPseudoSessionState#onIpduDEToSend
	 * (org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onIpduDEToSend(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduABReceived(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduABReceived(PseudoSessionContext ctx) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.cbcom.session.server.service.support.IPseudoSessionState2
	 * #
	 * onIpduABEmitted(org.jpos.jposext.cbcom.session.model.PseudoSessionContext
	 * )
	 */
	public void onIpduABEmitted(PseudoSessionContext ctx, int abortCode) {
		transNotImplementedForCurrState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.jpos.jposext.cbcom.session.server.service.IPseudoSessionState#
	 * onInvalidIpduReceived
	 * (org.jpos.jposext.cbcom.session.model.PseudoSessionContext)
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx) {
		// Send IPDU AB 0x23
		onInvalidIpduReceived(ctx, 0x23);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.jpos.jposext.cbcom.session.service.IPseudoSessionState#
	 * onInvalidIpduReceived
	 * (org.jpos.jposext.cbcom.session.model.PseudoSessionContext,
	 * org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException)
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx,
			CBCOMBadIPDUException e) {
		onInvalidIpduReceived(ctx, e.getReasonEnum().getPv01());
	}

	/**
	 * @param ctx
	 * @param pv01
	 */
	protected void onInvalidIpduReceived(PseudoSessionContext ctx, int pv01) {
		try {
			sendIpduAB(ctx, pv01);
		} catch (CBCOMSessionException e) {
			// Safe to ignore			
		}

		try {
			ctx.getChannelCallback().close();
		} catch (CBCOMSessionException e) {
			// Safe to ignore			
		}

		// Change session state to : LoggedOff
		ctx.setSessionState(ctx.getStateFactory().create(LoggedOffState.class));
	}

	/**
	 * @param ctx
	 * @param pv01
	 */
	protected void emitIpduAB(PseudoSessionContext ctx, int pv01) {
		try {
			final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			String state = this.getClass().getName();
			String transition = ste[2].getMethodName();
			String mesg = String
			.format("ctxId=%d, code=0x%x [%s<-%s]", ctx.getId(), pv01, state, transition);
			
			ctx.getChannelCallback().log("cbcom-ipdu-ab", mesg);
		} catch (Exception e) {
		}

		// Send IPDU AB
		try {
			sendIpduAB(ctx, pv01);
		} catch (CBCOMSessionException e) {
			// Safe to ignore			
		}

		onIpduABEmitted(ctx, pv01);
	}

	/**
	 * @param ctx
	 * @param pv01
	 * @throws CBCOMSessionException
	 */
	protected void sendIpduAB(PseudoSessionContext ctx, int pv01)
			throws CBCOMSessionException {

		// Prepare PI01
		PI pi01 = new PI(PIEnum.PI01, new byte[] { (byte) (pv01 & 0xFF) });

		// Prepare IPDU AB
		IPDU ipdu = new IPDU(IPDUEnum.AB, new PI[] { pi01 }, null, 0);

		// Channel callback to send the IPDU
		byte[] bIpdu = ipdu.toBytes();
		ctx.getChannelCallback().send(bIpdu, false);
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

	protected void cancelAndReplaceScheduledTimerTask(PseudoSessionContext ctx,
			Runnable runnable, int timerValue, TimeUnit timeUnit) {
		boolean previousCancelledOrDone;

		Future<?> taskFuture = ctx.getTaskFuture();

		if (null != taskFuture) {
			if ((!taskFuture.isDone()) && (!taskFuture.isCancelled())) {
				// TODO "May interrupt if running" parameter : set to false or
				// true ?
				previousCancelledOrDone = taskFuture.cancel(false);
			}
		}

		ctx.setTaskFuture(ctx.getDefferedTaskExecutor().schedule(runnable,
				timerValue, timeUnit));
	}

	/**
	 * @param taskFuture
	 */
	protected void cancelScheduledTask(Future<?> taskFuture) {
		if (null != taskFuture) {
			if (!taskFuture.isCancelled()) {
				boolean cancel = taskFuture.cancel(true);				
			}
		}
	}

	/**
	 * @param ctx
	 * @param nextStateClazz
	 */
	protected void changeState(PseudoSessionContext ctx,
			Class<? extends IPseudoSessionState> nextStateClazz) {
		IPseudoSessionState nextState = ctx.getStateFactory().create(
				nextStateClazz);
		ctx.setSessionState(nextState);
		nextState.init(ctx);
	}

	/**
	 * @param ctx
	 */
	protected void initCtxId(PseudoSessionContext ctx) {
		ctx.setId(new java.util.Date().getTime());
	}

	/**
	 * @param ctx
	 */
	protected void resetCtxWorkingVars(PseudoSessionContext ctx) {
		ctx.setId(-1);
		ctx.setTimerPIs(null);

		// Reset timers
		TimerConfig timerCfg = ctx.getTimerConfig();
		timerCfg.setNegotiatedTsi(timerCfg.getInitialTsi());
		timerCfg.setNegotiatedTnr(timerCfg.getInitialTnr());
		timerCfg.setNegotiatedTma(timerCfg.getInitialTma());
	}

	/**
	 * Server call this method to check the timer related PIs in the last
	 * received IPDU<BR>
	 * 
	 * If a timer is not valid, negotiation is NOK, and an abort code is
	 * returned depending on the timer nature. <BR>
	 * 
	 * If negotiation is OK, timer PIs to return to client are stored in the
	 * context (see property timerPIs), so that it can be further be used to
	 * populate the IPDU to respond.<BR>
	 * 
	 * Also note that server TSI timer may be updated in the process.<BR>
	 * 
	 * @param ctx
	 * @param ipdu
	 * @return 0x00 if negotiation succeed, an abort code otherwise
	 */
	protected int proceedToTimerNegotiation(PseudoSessionContext ctx, IPDU ipdu) {
		int abortCode = 0x00;

		List<PI> lstPis = ctx.getTimerPIs();
		if (null == lstPis) {
			lstPis = new ArrayList<PI>();
			ctx.setTimerPIs(lstPis);
		}
		lstPis.clear();

		// Handle timers negotiation if dedicated PI are present in IPDU CN
		// 1. Handle TNR timer negotiation
		if (0x00 == abortCode) {

			PI pi16 = ipdu.findPiByPIEnum(PIEnum.PI16);
			if (null != pi16) {
				byte[] bPv = pi16.getParamValue();
				int tnr = ((((int) bPv[0]) & 0xFF) << 8)
						| (((int) bPv[1]) & 0xFF);
				if ((tnr >= ctx.getTimerConfig().getMinTnr())
						&& (tnr <= ctx.getTimerConfig().getMaxTnr())) {
					int nvTnr = tnr;
					ctx.getTimerConfig().setNegotiatedTnr(nvTnr);
					byte[] bNewPv = ByteBuffer.allocate(4).putInt(nvTnr)
							.array();
					PI respPi16 = new PI(PIEnum.PI16, new byte[] { bNewPv[2],
							bNewPv[3] });
					lstPis.add(respPi16);
				} else {
					// Negotiation failed
					abortCode = 0x15;
				}
			}
		}

		// 2. Handle TSI timer negotiation
		if (0x00 == abortCode) {
			PI pi17 = ipdu.findPiByPIEnum(PIEnum.PI17);
			if (null != pi17) {
				byte[] bPv = pi17.getParamValue();
				int tsi = ((((int) bPv[0]) & 0xFF) << 8)
						| (((int) bPv[1]) & 0xFF);
				if ((tsi >= ctx.getTimerConfig().getMinTsi())
						&& (tsi <= ctx.getTimerConfig().getMaxTsi())) {
					int nvTsi = tsi;
					ctx.getTimerConfig().setNegotiatedTsi(nvTsi);
					byte[] bNewPv = ByteBuffer.allocate(4).putInt(nvTsi)
							.array();
					PI respPi17 = new PI(PIEnum.PI17, new byte[] { bNewPv[2],
							bNewPv[3] });
					lstPis.add(respPi17);

					// TSI is a server-side timer, so we must update it in
					// server pseudo session context
					ctx.getTimerConfig().setNegotiatedTsi(nvTsi);

				} else {
					// Negotiation failed
					abortCode = 0x16;
				}
			}
		}

		// 3. Handle TMA timer negotiation
		if (0x00 == abortCode) {

			PI pi18 = ipdu.findPiByPIEnum(PIEnum.PI18);
			if (null != pi18) {
				byte[] bPv = pi18.getParamValue();
				int tma = ((((int) bPv[0]) & 0xFF) << 8)
						| (((int) bPv[1]) & 0xFF);
				if ((tma >= ctx.getTimerConfig().getMinTma())
						&& (tma <= ctx.getTimerConfig().getMaxTma())) {
					int nvTma = tma;
					ctx.getTimerConfig().setNegotiatedTma(nvTma);
					byte[] bNewPv = ByteBuffer.allocate(4).putInt(nvTma)
							.array();
					PI respPi18 = new PI(PIEnum.PI18, new byte[] { bNewPv[2],
							bNewPv[3] });
					lstPis.add(respPi18);
				} else {
					// Negotiation failed
					abortCode = 0x17;
				}
			}
		}

		return abortCode;
	}

	/**
	 * @param ctx
	 * @param ipdu
	 */
	protected void updatePseudoSessionTimers(PseudoSessionContext ctx, IPDU ipdu) {
		PI pi16 = ipdu.findPiByPIEnum(PIEnum.PI16);
		PI pi17 = ipdu.findPiByPIEnum(PIEnum.PI17);
		PI pi18 = ipdu.findPiByPIEnum(PIEnum.PI18);

		if (null != pi16) {
			byte[] bPv = pi16.getParamValue();
			int tnr = ((((int) bPv[0]) & 0xFF) << 8) | (((int) bPv[1]) & 0xFF);
			;
			ctx.getTimerConfig().setNegotiatedTnr(tnr);
		}

		if (null != pi17) {
			byte[] bPv = pi17.getParamValue();
			int tsi = ((((int) bPv[0]) & 0xFF) << 8) | (((int) bPv[1]) & 0xFF);
			ctx.getTimerConfig().setNegotiatedTsi(tsi);
		}

		if (null != pi18) {
			byte[] bPv = pi18.getParamValue();
			int tma = ((((int) bPv[0]) & 0xFF) << 8) | (((int) bPv[1]) & 0xFF);
			ctx.getTimerConfig().setNegotiatedTma(tma);
		}
	}

}
