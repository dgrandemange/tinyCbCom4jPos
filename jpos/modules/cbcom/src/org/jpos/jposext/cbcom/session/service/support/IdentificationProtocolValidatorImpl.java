package org.jpos.jposext.cbcom.session.service.support;

import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IIdentificationProtocolValidator;

/**
 * A simple CB2A/CBCOM validator implementation
 * 
 * @author dgrandemange
 * 
 */
public class IdentificationProtocolValidatorImpl implements
		IIdentificationProtocolValidator {

	
	public int validate(PseudoSessionContext ctx, byte clientCbcomVersion, byte clientProtocolType,
			byte[] clientCb2aVersion) {
		int abortCode;
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
		return abortCode;
	}

}
