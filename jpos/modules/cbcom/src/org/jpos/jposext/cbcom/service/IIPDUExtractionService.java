package org.jpos.jposext.cbcom.service;

/**
 * Describes a service that provides IPDU info extraction capacities<BR>
 * Typical implementation may operate sequential readings on an input stream<BR>
 * 
 * @author dgrandemange
 * 
 */
public interface IIPDUExtractionService {

	/**
	 * @return Total length of IPDU
	 */
	public int getIpduLen();

	/**
	 * @return IPDU type (PGI, i.e. CN, AC, DE, AB) as a byte
	 */
	public byte getIpduPGI();

	/**
	 * @return IPDU params length (LGI)
	 */
	public int getIpduLGI();

	/**
	 * @param ipduLgi
	 *            IPDU params length
	 * @return IPDU params as a byte array (should not contain any optional
	 *         APDU)
	 */
	public byte[] getIpduParams(int ipduLgi);

	/**
	 * @param apduLen
	 *            APDU length
	 * @return APDU as a byte array
	 */
	public byte[] getApdu(int apduLen);
}
