package org.jpos.jposext.cbcom.session.service;

import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;

/**
 * Describes a oentification protocol validator
 * 
 * @author dgrandemange
 * 
 */
public interface IIdentificationProtocolValidator {

	/**
	 * @param ctx
	 * @param clientCbcomVersion
	 * @param clientProtocolType
	 * @param clientCb2aVersion
	 * @return a pseudo session abort code on validation error, 0x00 if
	 *         validation succeeds
	 */
	int validate(PseudoSessionContext ctx, byte clientCbcomVersion,
			byte clientProtocolType, byte[] clientCb2aVersion);
}
