package org.jpos.jposext.cbcom.service.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.exception.CBCOMException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.IPDUPI;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.service.IIPDUExtractionService;
import org.jpos.jposext.cbcom.service.IIPDUFactory;

/**
 * Basic implementation of an IPDU factory
 * 
 * @author dgrandemange
 * 
 */
public class IPDUFactoryImpl implements IIPDUFactory {

	/**
	 * IPDU maximum length
	 */
	private int maxIpduLen = -1;

	private static Map<Byte, IPDUEnum> mapIpduEnumByPGI;

	private static Map<Byte, PIEnum> mapPIEnumByPI;

	static {
		mapIpduEnumByPGI = new HashMap<Byte, IPDUEnum>();
		for (IPDUEnum ipduEnum : IPDUEnum.values()) {
			mapIpduEnumByPGI.put(ipduEnum.getIpdu(), ipduEnum);
		}

		mapPIEnumByPI = new HashMap<Byte, PIEnum>();
		for (PIEnum piEnum : PIEnum.values()) {
			mapPIEnumByPI.put(piEnum.getPi(), piEnum);
		}
	}


	/* (non-Javadoc)
	 * @see org.jpos.jposext.cbcom.service.IIPDUFactory#create(org.jpos.jposext.cbcom.service.IIPDUExtractionService)
	 */
	public IPDU create(IIPDUExtractionService ipduReader) throws CBCOMException {
		
		int lenIpdu = ipduReader.getIpduLen();

		byte bPgi = ipduReader.getIpduPGI();

		IPDUEnum ipduEnum = mapIpduEnumByPGI.get(bPgi);

		if (null == ipduEnum) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.INVALID_PGI, String
							.format("Invalid PGI found (%s)", IPDUFactoryImpl
									.getHexString(new byte[] { bPgi })));
		}

		int lgi = ipduReader.getIpduLGI();

		if (lgi <= 0) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.INVALID_LGI,
					String
							.format(
									"Invalid LGI found (%d) : LGI can't be negative or zero length",
									lgi));
		} else {
			if (lgi > (lenIpdu - 2)) {
				throw new CBCOMBadIPDUException(
						CBCOMBadIPDUException.ReasonEnum.LGI_INCOHERENCE,
						String
								.format(
										"Invalid LGI found (%d) : conflict with previously read IPDU total length (%d)",
										lgi, lenIpdu));
			}
		}

		byte[] bufIpduParams = ipduReader.getIpduParams(lgi);

		Map<PIEnum, PI> mapPIById = new HashMap<PIEnum, PI>();
		extractIPDUParams(bufIpduParams, ipduEnum, lgi, 0, mapPIById);

		// Check that the IPDU parameters (PIs) required by current IPDU are
		// present
		checkMandatoryPIs(ipduEnum, mapPIById);

		// Check for optional APDU
		ByteArrayOutputStream apduWrapper = new ByteArrayOutputStream();
		int apduLen = checkForApdu(ipduReader, mapPIById, apduWrapper);
		byte[] apdu = null;
		if (apduLen >= 0) {
			apdu = apduWrapper.toByteArray();
		}

		IPDU ipdu = new IPDU(ipduEnum, mapPIById, apdu, apduLen);

		return ipdu;
	}

	protected int checkForApdu(IIPDUExtractionService ipduReader,
			Map<PIEnum, PI> mapPIById, ByteArrayOutputStream bos)
			throws CBCOMBadIPDUException {
		PI pi07 = mapPIById.get(PIEnum.PI07);
		if (null != pi07) {
			int li = pi07.getParamLen();
			// We accept an APDU length up to to (2^32 - 1) bytes
			if (li > 4) {
				throw new CBCOMBadIPDUException(
						CBCOMBadIPDUException.ReasonEnum.APDU_LEN_TOO_HIGH,
						String.format("APDU length is too high (%d)", li));
			}
			byte[] bufApduLen = pi07.getParamValue();
			int apduLen = byteToInt(li, bufApduLen);
			byte[] readApdu = ipduReader.getApdu(apduLen);
			try {
				bos.write(readApdu);
				bos.flush();
			} catch (IOException e) {
				// Safe to ignore				
			}			
			return apduLen;
		} else {
			return -1;
		}
	}

	protected static int byteToInt(int li, byte[] bufApduLen) {
		int apduLen = 0;

		for (int i = 0; i < (li - 1); i++) {
			apduLen |= bufApduLen[i] & 0xFF;
			apduLen <<= 8;
		}

		apduLen |= bufApduLen[(li - 1)] & 0xFF;

		return apduLen;
	}

	protected void extractIPDUParams(byte[] bufIpduParams, IPDUEnum ipduEnum,
			int lgi, int lastIdx, Map<PIEnum, PI> mapPIById)
			throws CBCOMException {
		if (lastIdx == lgi) {
			return;
		}

		if (lgi - lastIdx - 1 < 2) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA,
					String
							.format(
									"Not enough data to extract a valid IPDU parameter [%s]",
									IPDUFactoryImpl.getHexString(bufIpduParams,
											lastIdx)));
		}

		byte bPi = bufIpduParams[lastIdx++];
		PIEnum piEnum = mapPIEnumByPI.get(bPi);
		if (null == piEnum) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.INVALID_PI, String.format(
							"Invalid PI found [%s]", IPDUFactoryImpl
									.getHexString(new byte[] { bPi })));
		}

		IPDUPI ipduPi = ipduEnum.getIpduPi(piEnum);
		if (null == ipduPi) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.UNEXPECTED_PI_FOR_IPDU,
					String.format("PI not valid for IPDU (PI=[%s], IPDU=[%s])",
							piEnum.name(), ipduEnum.name()));
		}
		PI pi = ipduPi.getPi();

		byte bLi = bufIpduParams[lastIdx++];
		int li = bLi & 0xFF;

		if (lgi - lastIdx < li) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.LGI_LI_INCOHERENCE,
					String
							.format(
									"Param length (LI=[%d]) incoherence with parameters total length (LGI=[%d])",
									li, lgi));
		}

		if (lastIdx + li > bufIpduParams.length) {
			throw new CBCOMBadIPDUException(
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA,
					String
							.format(
									"Not enough data to extract IPDU parameter (data=[%s], expected length LI=[%d])",
									IPDUFactoryImpl.getHexString(bufIpduParams,
											lastIdx), li));
		}

		pi.setParamLen(li);

		byte[] pv = Arrays.copyOfRange(bufIpduParams, lastIdx, lastIdx + li);
		lastIdx += li;
		pi.setParamValue(pv);

		mapPIById.put(piEnum, pi);

		extractIPDUParams(bufIpduParams, ipduEnum, lgi, lastIdx, mapPIById);
	}

	protected void checkMandatoryPIs(IPDUEnum ipduEnum,
			Map<PIEnum, PI> mapPIById) throws CBCOMBadIPDUException {
		IPDUPI ipduPi;

		for (Iterator<IPDUPI> it = ipduEnum.getIPDUPIsIterator(); it.hasNext();) {
			ipduPi = it.next();
			if (ipduPi.isMandatory()) {
				PIEnum piEnum = ipduPi.getPi().getPiEnum();
				if (!(mapPIById.containsKey(piEnum))) {
					throw new CBCOMBadIPDUException(
							CBCOMBadIPDUException.ReasonEnum.PI_REQUIRED_BY_IPDU,
							String.format("PI [%s] is required for IPDU [%s]",
									piEnum.name(), ipduEnum.name()));
				}
			}
		}
	}

	public int getMaxIpduLen() {
		return maxIpduLen;
	}

	public void setMaxIpduLen(int maxIpduLen) {
		this.maxIpduLen = maxIpduLen;
	}

	public static String getHexString(byte[] b, int offset) {
		String result = "";
		for (int i = offset; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public static String getHexString(byte[] b) {
		return getHexString(b, 0);
	}

}
