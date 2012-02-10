package org.jpos.jposext.cbcom.service.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.exception.CBCOMException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.service.IIPDUExtractionService;

public class IPDUFactoryImplTest extends TestCase {

	private IPDUFactoryImpl factory;

	private Map<PIEnum, PI> mapPIById;

	/**
	 * Mock implementation for unit test purpose only
	 * 
	 * @author dgrandemange
	 * 
	 */
	class IPDUReaderServiceMockImpl implements IIPDUExtractionService {
		private byte[] apdu;

		private int ipduLGI;

		private int ipduLen;

		private byte ipduPGI;

		private byte[] ipduParams;

		public byte[] getApdu(int apduLen) {
			return apdu;
		}

		public int getIpduLGI() {
			return ipduLGI;
		}

		public int getIpduLen() {
			return ipduLen;
		}

		public byte getIpduPGI() {
			return ipduPGI;
		}

		public byte[] getIpduParams(int ipduLgi) {
			return ipduParams;
		}

		public byte[] getApdu() {
			return apdu;
		}

		public void setApdu(byte[] apdu) {
			this.apdu = apdu;
		}

		public void setIpduLGI(int ipduLGI) {
			this.ipduLGI = ipduLGI;
		}

		public void setIpduLen(int ipduLen) {
			this.ipduLen = ipduLen;
		}

		public void setIpduPGI(byte ipduPGI) {
			this.ipduPGI = ipduPGI;
		}

		public byte[] getIpduParams() {
			return ipduParams;
		}

		public void setIpduParams(byte[] ipduParams) {
			this.ipduParams = ipduParams;
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new IPDUFactoryImpl();
		mapPIById = new HashMap<PIEnum, PI>();
	}

	public void testGetHexString() {
		byte[] tab = new byte[] { (byte) 0xC2 };
		assertEquals("c2", IPDUFactoryImpl.getHexString(tab));
	}

	public void testExtractIPDUParams__PARAM_ZONE_WRAPS_1PI__OK()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x01, (byte) 0x01,
				(byte) 0x00 };

		int lgi = 3;

		factory.extractIPDUParams(bufIpduParams, IPDUEnum.AC, lgi, 0,
				mapPIById);

		assertEquals(1, mapPIById.size());
		assertTrue(mapPIById.containsKey(PIEnum.PI01));

		PI pi01 = mapPIById.get(PIEnum.PI01);
		assertEquals(1, pi01.getParamLen());
		assertTrue(Arrays.equals(new byte[] { (byte) 0x00 }, pi01
				.getParamValue()));
	}

	public void testExtractIPDUParams__PARAM_ZONE_WRAPS_2PI__OK()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x05, (byte) 0x04,
				(byte) 0x12, (byte) 0x02, (byte) 0x11, (byte) 0x20,
				(byte) 0x08, (byte) 0x01, (byte) 0x01 };

		int lgi = 9;

		factory.extractIPDUParams(bufIpduParams, IPDUEnum.CN, lgi, 0,
				mapPIById);

		assertEquals(2, mapPIById.size());
		assertTrue(mapPIById.containsKey(PIEnum.PI05));
		assertTrue(mapPIById.containsKey(PIEnum.PI08));

		PI pi05 = mapPIById.get(PIEnum.PI05);
		assertEquals(4, pi05.getParamLen());
		assertTrue(Arrays.equals(new byte[] { (byte) 0x12, (byte) 0x02,
				(byte) 0x11, (byte) 0x20 }, pi05.getParamValue()));

		PI pi08 = mapPIById.get(PIEnum.PI08);
		assertEquals(1, pi08.getParamLen());
		assertTrue(Arrays.equals(new byte[] { (byte) 0x01 }, pi08
				.getParamValue()));
	}

	public void testExtractIPDUParams__PARAM_VALUE_ENDS_PREMATURELY_CASE1()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x05, (byte) 0x04 };

		int lgi = 6;

		try {
			factory.extractIPDUParams(bufIpduParams, IPDUEnum.CN, lgi, 0,
					mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA, e
							.getReasonEnum());
		}
	}

	public void testExtractIPDUParams__PARAM_VALUE_ENDS_PREMATURELY_CASE2()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x05, (byte) 0x04,
				(byte) 0x12, (byte) 0x02, (byte) 0x11 };

		int lgi = 6;

		try {
			factory.extractIPDUParams(bufIpduParams, IPDUEnum.CN, lgi, 0,
					mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(
					CBCOMBadIPDUException.ReasonEnum.PREMATURATE_END_OF_DATA, e
							.getReasonEnum());
		}
	}

	public void testExtractIPDUParams__INVALID_PI() throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x00, (byte) 0x01,
				(byte) 0x00 };

		int lgi = 3;

		try {
			factory.extractIPDUParams(bufIpduParams, IPDUEnum.CN, lgi, 0,
					mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.INVALID_PI));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.INVALID_PI, e
					.getReasonEnum());
		}
	}

	public void testExtractIPDUParams__UNEXPECTED_PI_FOR_IPDU()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x01, (byte) 0x01,
				(byte) 0x00 };

		int lgi = 3;

		try {
			factory.extractIPDUParams(bufIpduParams, IPDUEnum.CN, lgi, 0,
					mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.UNEXPECTED_PI_FOR_IPDU));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(
					CBCOMBadIPDUException.ReasonEnum.UNEXPECTED_PI_FOR_IPDU, e
							.getReasonEnum());
		}

	}

	public void testExtractIPDUParams__LGI_LI_INCOHERENCE()
			throws CBCOMException {
		byte bufIpduParams[] = new byte[] { (byte) 0x01, (byte) 0x03,
				(byte) 0x00, (byte) 0x00 };

		int lgi = 3;

		try {
			factory.extractIPDUParams(bufIpduParams, IPDUEnum.AC, lgi, 0,
					mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.LGI_LI_INCOHERENCE));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.LGI_LI_INCOHERENCE, e
					.getReasonEnum());
		}

	}

	public void testCheckMandatoryPIs_OK() throws CBCOMException {
		mapPIById.put(PIEnum.PI05, new PI(PIEnum.PI05));
		factory.checkMandatoryPIs(IPDUEnum.CN, mapPIById);
	}

	public void testCheckMandatoryPIs_NOT_OK() throws CBCOMException {
		try {
			// We deliberately omit the mandatory PI05 required in an IPDU CN
			factory.checkMandatoryPIs(IPDUEnum.CN, mapPIById);

			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.PI_REQUIRED_BY_IPDU));

		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.PI_REQUIRED_BY_IPDU,
					e.getReasonEnum());
		}
	}

	public void testByteToInt() {
		int expected = 324;
		byte[] inLen = ByteBuffer.allocate(4).putInt(expected).array();
		int res = IPDUFactoryImpl.byteToInt(inLen.length, inLen);
		assertEquals(expected, res);
	}

	public void testCheckForApdu_PI07NotSet() throws CBCOMBadIPDUException,
			IOException {
		final StringBuffer invokedIndicator = new StringBuffer();

		IIPDUExtractionService ipduReader = new IIPDUExtractionService() {
			public byte[] getIpduParams(int ipduLgi) {
				return null;
			}

			public byte getIpduPGI() {
				return 0;
			}

			public int getIpduLen() {
				return 0;
			}

			public int getIpduLGI() {
				return 0;
			}

			public byte[] getApdu(int apduLen) {
				invokedIndicator.append("UNEXPECTED_INVOCATION");
				return null;
			}
		};

		ByteArrayOutputStream apduWrapper = new ByteArrayOutputStream();
		int apduLen = factory.checkForApdu(ipduReader, mapPIById, apduWrapper);
		assertNotSame("UNEXPECTED_INVOCATION", invokedIndicator.toString());
		assertEquals(-1, apduLen);
		assertEquals(0, apduWrapper.toByteArray().length);
	}

	public void testCheckForApdu_PI07Set() throws CBCOMBadIPDUException,
			IOException {

		final byte[] apdu = new byte[] { 0x00, 0x01 };

		IIPDUExtractionService ipduReader = new IIPDUExtractionService() {
			public byte[] getIpduParams(int ipduLgi) {
				return null;
			}

			public byte getIpduPGI() {
				return 0;
			}

			public int getIpduLen() {
				return 0;
			}

			public int getIpduLGI() {
				return 0;
			}

			public byte[] getApdu(int apduLen) {
				return apdu;
			}
		};

		PI pi07 = new PI(PIEnum.PI07);
		pi07.setParamLen(2);

		// Longueur (LI) de la valeur du paramètre (PV) : ici fixé
		// arbitrairement à 266, soit 010A en hexa
		byte[] paramValue = new byte[] { 0x01, 0x0A };

		pi07.setParamValue(paramValue);
		mapPIById.put(PIEnum.PI07, pi07);

		ByteArrayOutputStream apduWrapper = new ByteArrayOutputStream();
		int apduLen = factory.checkForApdu(ipduReader, mapPIById, apduWrapper);
		assertEquals(266, apduLen);
		assertTrue(Arrays.equals(apdu, apduWrapper.toByteArray()));
	}

	public void testCheckForApdu_PI07Set_APDULenTooHigh()
			throws CBCOMBadIPDUException, IOException {
		IIPDUExtractionService ipduReader = null;

		PI pi07 = new PI(PIEnum.PI07);
		pi07.setParamLen(5);
		pi07.setParamValue(new byte[] { 0x00, 0x00, 0x00, 0x01, 0x0A });
		mapPIById.put(PIEnum.PI07, pi07);

		try {
			ByteArrayOutputStream apduWrapper = new ByteArrayOutputStream();
			factory.checkForApdu(ipduReader, mapPIById, apduWrapper);
			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.APDU_LEN_TOO_HIGH));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.APDU_LEN_TOO_HIGH, e
					.getReasonEnum());
		}

	}

	public void testCreate_InvalidPGI() throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();
		byte anyInvalidPGI = 0x00;
		ipduReader.setIpduPGI(anyInvalidPGI);

		try {
			factory.create(ipduReader);
			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.INVALID_PGI));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.INVALID_PGI, e
					.getReasonEnum());
		}
	}

	public void testCreate_InvalidLGI() throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();
		byte anyValidPGI = IPDUEnum.CN.getIpdu();
		ipduReader.setIpduPGI(anyValidPGI);
		ipduReader.setIpduLen(-1);

		try {
			factory.create(ipduReader);
			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.INVALID_LGI));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.INVALID_LGI, e
					.getReasonEnum());
		}
	}

	public void testCreate_IncoherentLGI() throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();
		byte anyValidPGI = IPDUEnum.CN.getIpdu();
		ipduReader.setIpduPGI(anyValidPGI);
		ipduReader.setIpduLen(5);
		ipduReader.setIpduLGI(6);

		try {
			factory.create(ipduReader);
			fail(String.format("Exception %s was expected",
					CBCOMBadIPDUException.ReasonEnum.LGI_INCOHERENCE));
		} catch (CBCOMBadIPDUException e) {
			assertEquals(CBCOMBadIPDUException.ReasonEnum.LGI_INCOHERENCE, e
					.getReasonEnum());
		}
	}

	public void testCreate_OK__No_APDU() throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();

		ipduReader.setIpduPGI(IPDUEnum.CN.getIpdu());

		byte[] ipduParams = new byte[] { (byte) 0x05, (byte) 0x04, (byte) 0x12,
				(byte) 0x02, (byte) 0x11, (byte) 0x20, (byte) 0x08,
				(byte) 0x01, (byte) 0x01 };

		ipduReader.setIpduParams(ipduParams);

		ipduReader.setIpduLGI(ipduParams.length);

		ipduReader.setIpduLen(ipduParams.length + 2);

		IPDU ipdu = factory.create(ipduReader);
		assertEquals(IPDUEnum.CN, ipdu.getIpduType());

		assertEquals(2, ipdu.getPIList().size());

		PI pi05 = ipdu.findPiByPIEnum(PIEnum.PI05);
		assertNotNull(pi05);

		PI pi08 = ipdu.findPiByPIEnum(PIEnum.PI08);
		assertNotNull(pi08);

		assertNull(ipdu.getApdu());
		assertEquals(-1, ipdu.getApduLength());
	}

	public void testCreate_OK__With_APDU() throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();

		ipduReader.setIpduPGI(IPDUEnum.DE.getIpdu());

		byte[] ipduParams = new byte[] { (byte) 0x07, (byte) 0x04, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x05 };

		byte[] apdu = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03,
				(byte) 0x04, (byte) 0x05 };

		ipduReader.setIpduParams(ipduParams);

		ipduReader.setIpduLGI(ipduParams.length);

		ipduReader.setIpduLen(ipduParams.length + 2);

		ipduReader.setApdu(apdu);

		IPDU ipdu = factory.create(ipduReader);
		assertEquals(IPDUEnum.DE, ipdu.getIpduType());

		assertEquals(1, ipdu.getPIList().size());

		PI pi07 = ipdu.findPiByPIEnum(PIEnum.PI07);
		assertNotNull(pi07);

		assertNotNull(ipdu.getApdu());
		assertEquals(5, ipdu.getApduLength());
		assertTrue(Arrays.equals(apdu, ipdu.getApdu()));
	}

	public void testCreate_OK__With_APDU_AND_OPTIONAL_PI()
			throws CBCOMException, IOException {
		IPDUReaderServiceMockImpl ipduReader = new IPDUReaderServiceMockImpl();

		ipduReader.setIpduPGI(IPDUEnum.DE.getIpdu());

		byte[] ipduParams = new byte[] { (byte) 0x01, (byte) 0x01, (byte) 0xA0,
				(byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x05 };

		byte[] apdu = new byte[] { (byte) 0x01, (byte) 0x02, (byte) 0x03,
				(byte) 0x04, (byte) 0x05 };

		ipduReader.setIpduParams(ipduParams);

		ipduReader.setIpduLGI(ipduParams.length);

		ipduReader.setIpduLen(ipduParams.length + 2);

		ipduReader.setApdu(apdu);

		IPDU ipdu = factory.create(ipduReader);
		assertEquals(IPDUEnum.DE, ipdu.getIpduType());

		assertEquals(2, ipdu.getPIList().size());

		PI pi01 = ipdu.findPiByPIEnum(PIEnum.PI01);
		assertNotNull(pi01);
		assertEquals(1, pi01.getParamLen());
		assertTrue(Arrays.equals(new byte[] { (byte) 0xA0 }, pi01
				.getParamValue()));

		PI pi07 = ipdu.findPiByPIEnum(PIEnum.PI07);
		assertNotNull(pi07);
		assertNotNull(ipdu.getApdu());
		assertEquals(5, ipdu.getApduLength());
		assertTrue(Arrays.equals(apdu, ipdu.getApdu()));
	}

}
