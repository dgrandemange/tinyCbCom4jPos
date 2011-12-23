package org.jpos.jposext.cbcom.session.service;

import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;

/**
 * Describes all transitions (events) that can occurred against a CBCOM pseudo
 * session
 * 
 * @author dgrandemange
 * 
 */
public interface IPseudoSessionState {

	/**
	 * Transition : when entering the state 
	 * 
	 * @param ctx
	 */
	public void init(PseudoSessionContext ctx);
	
	/**
	 * Transition : when receiving an IPDU CN 
	 * 
	 * @param ctx
	 */
	public void onIpduCNReceived(PseudoSessionContext ctx);

	/**
	 * Transition : when an IPDU AC has been responded 
	 * 
	 * @param ctx
	 */
	public void onIpduACEmitted(PseudoSessionContext ctx);

	/**
	 * Transition : when receiving an IPDU DE 
	 * 
	 * @param ctx
	 */
	public void onIpduDEReceived(PseudoSessionContext ctx);

	/**
	 * Transition : when an IPDU DE has been responded 
	 * 
	 * @param ctx
	 */
	public void onIpduDEEmitted(PseudoSessionContext ctx);

	/**
	 * Transition : when receiving an IPDU AB 
	 * 
	 * @param ctx
	 */
	public void onIpduABReceived(PseudoSessionContext ctx);

	/**
	 * Transition : when an IPDU AB has been responded 
	 * 
	 * @param ctx
	 */
	public void onIpduABEmitted(PseudoSessionContext ctx);

	/**
	 * Transition : when receiving an invalid IPDU 
	 * 
	 * @param ctx
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx);
	
	/**
	 * Transition : when receiving an invalid IPDU
	 * 
	 * @param ctx
	 * @param e
	 */
	public void onInvalidIpduReceived(PseudoSessionContext ctx, CBCOMBadIPDUException e);
}