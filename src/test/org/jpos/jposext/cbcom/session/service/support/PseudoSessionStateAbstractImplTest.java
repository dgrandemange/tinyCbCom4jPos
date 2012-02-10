package org.jpos.jposext.cbcom.session.service.support;

import junit.framework.TestCase;

import org.jpos.jposext.cbcom.exception.CBCOMSessionStateException;
import org.jpos.jposext.cbcom.session.server.service.support.DummyState;

public class PseudoSessionStateAbstractImplTest extends TestCase {

	private DummyState state = new DummyState();
	
	public void testTransNotImplementedForCurrState() {
			try {
				state.onIpduCNReceived(null);
				fail("Exception expected");
			} catch (CBCOMSessionStateException e) {
				assertEquals(DummyState.class.getSimpleName(), e.getState());
				assertEquals("onIpduCNReceived", e.getTransition());
			}
		}

}
