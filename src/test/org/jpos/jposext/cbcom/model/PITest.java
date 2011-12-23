package org.jpos.jposext.cbcom.model;

import java.util.Arrays;

import junit.framework.TestCase;

public class PITest extends TestCase {
	public void testToBytes() {
		PI pi = new PI(PIEnum.PI01);
		byte[] paramValue = new byte[] {0x00}; 
		pi.setParamValue(paramValue);
		pi.setParamLen(paramValue.length);
		
		assertTrue(Arrays.equals(new byte[] {0x01, 0x01, 0x00}, pi.toBytes()));
	}
}
