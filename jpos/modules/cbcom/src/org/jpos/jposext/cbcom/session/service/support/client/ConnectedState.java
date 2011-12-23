package org.jpos.jposext.cbcom.session.service.support.client;

import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.support.PseudoSessionStateAbstractImpl;

/**
 * Connected pseudo session state "client" implementation<BR> 
 * @author dgrandemange
 *
 */
public class ConnectedState extends PseudoSessionStateAbstractImpl implements IPseudoSessionState{

	@Override
	public void onInvalidIpduReceived(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onInvalidIpduReceived(ctx);
	}

	@Override
	public void onIpduABEmitted(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduABEmitted(ctx);
	}

	@Override
	public void onIpduABReceived(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduABReceived(ctx);
	}

	@Override
	public void onIpduACEmitted(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduACEmitted(ctx);
	}

	@Override
	public void onIpduCNReceived(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduCNReceived(ctx);
	}

	@Override
	public void onIpduDEEmitted(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduDEEmitted(ctx);
	}

	@Override
	public void onIpduDEReceived(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		super.onIpduDEReceived(ctx);
	}

	@Override
	public void init(PseudoSessionContext ctx) {
		// TODO Auto-generated method stub
		
	}

}
