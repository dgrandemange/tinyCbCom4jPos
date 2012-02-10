package org.jpos.jposext.cbcom.service;

import org.jpos.jposext.cbcom.exception.CBCOMException;
import org.jpos.jposext.cbcom.model.IPDU;

/**
 * Factory that creates IPDU instances from different sources
 * 
 * @author dgrandemange
 * 
 */
public interface IIPDUFactory {

	/**
	 * @param ipduExtractSrv
	 *            Service that offer a strategy to extract IPDU parts from an
	 *            input stream
	 * @return IPDU extracted from input stream
	 * @exception CBCOMException
	 *                if unable to extract a valid IPDU
	 */
	IPDU create(IIPDUExtractionService ipduExtractSrv)
			throws CBCOMException;

}
