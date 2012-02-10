package org.jpos.jposext.cbcom.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class IPDUTest extends TestCase {

	public void testToBytes_NO_APDU() {
		Map<PIEnum, PI> mapPIs = new HashMap<PIEnum, PI>();

		PI pi01 = new PI(PIEnum.PI01);
		byte[] paramValue = new byte[] { 0x00 };
		pi01.setParamValue(paramValue);
		pi01.setParamLen(paramValue.length);

		mapPIs.put(PIEnum.PI01, pi01);

		IPDU ipdu = new IPDU(IPDUEnum.AC, mapPIs, null, 0);
		byte[] expected = new byte[] { 0x00, 0x00, 0x00, 0x05, (byte) 0xC3,
				0x03, 0x01, 0x01, 0x00 };
		byte[] bytes = ipdu.toBytes();
		assertTrue(Arrays.equals(expected, bytes));
	}

	public void testToBytes_APDU() {
		Map<PIEnum, PI> mapPIs = new HashMap<PIEnum, PI>();

		byte[] apdu = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
		PI pi07 = new PI(PIEnum.PI07);
		byte[] paramValue = new byte[] { 0x00, 0x00, 0x00,
				(byte) (apdu.length & 0xFF) };
		pi07.setParamValue(paramValue);
		pi07.setParamLen(paramValue.length);

		mapPIs.put(PIEnum.PI07, pi07);

		IPDU ipdu = new IPDU(IPDUEnum.DE, mapPIs, apdu, apdu.length);
		byte[] expected = new byte[] { 0x00, 0x00, 0x00, 0x0D, (byte) 0xC1,
				0x06, 0x07, 0x04, 0x00, 0x00, 0x00,
				(byte) (apdu.length & 0xFF), 0x01, 0x02, 0x03, 0x04, 0x05 };
		byte[] bytes = ipdu.toBytes();
		assertTrue(Arrays.equals(expected, bytes));
	}

	public void testToBytes_APDU_Case2() {
		Map<PIEnum, PI> mapPIs = new HashMap<PIEnum, PI>();

		byte[] apdu = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };
		PI pi07 = new PI(PIEnum.PI07);
		byte[] paramValue = new byte[] { 0x00, 0x00, 0x00,
				(byte) (apdu.length & 0xFF) };
		pi07.setParamValue(paramValue);
		pi07.setParamLen(paramValue.length);

		PI pi16 = new PI(PIEnum.PI16);
		byte[] paramValue2 = new byte[] { 0x00 };
		pi16.setParamValue(paramValue2);
		pi16.setParamLen(paramValue2.length);

		mapPIs.put(PIEnum.PI07, pi07);
		mapPIs.put(PIEnum.PI16, pi16);

		IPDU ipdu = new IPDU(IPDUEnum.DE, mapPIs, apdu, apdu.length);
		byte[] expected = new byte[] { 0x00, 0x00, 0x00, 0x10, (byte) 0xC1,
				0x09, 0x07, 0x04, 0x00, 0x00, 0x00,
				(byte) (apdu.length & 0xFF), 0x10, 0x01, 0x00, 0x01, 0x02,
				0x03, 0x04, 0x05 };
		byte[] bytes = ipdu.toBytes();
		assertTrue(Arrays.equals(expected, bytes));
	}
}
