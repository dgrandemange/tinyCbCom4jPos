package org.jpos.iso;

import java.util.Arrays;

import junit.framework.TestCase;

import org.jpos.iso.packager.GenericPackager;

public class ISOBinaryTLVSubFieldPackagerTest extends TestCase {

	private GenericPackager isoPackager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		isoPackager = new CustomGenericPackager();
		isoPackager.readFile(ISOBinaryTLVSubFieldPackagerTest.class
				.getResourceAsStream("test-packager.xml"));
	}

	public void testNominal1() throws ISOException {
		byte[] valField55_009C = new byte[] { 0x01 };
		byte[] valField55_9F37 = new byte[] { (byte) 0xF5, 0x6B, (byte) 0xA5,
				0x36 };

		ISOMsg msg = new ISOMsg("0100");
		ISOMsg champ55 = new ISOMsg(55);
		champ55.set(0x009C, valField55_009C);
		champ55.set(0x9F37, valField55_9F37);
		msg.set(champ55);
		msg.recalcBitMap();

		byte[] pack = isoPackager.pack(msg);
		System.out.println(ISOUtil.hexdump(pack));

		ISOMsg res = new ISOMsg();
		int consummed = isoPackager.unpack(res, pack);
		ISOMsg res_champ55 = (ISOMsg) res.getComponent(55);
		
		assertTrue(Arrays.equals(valField55_009C, res_champ55.getBytes(0x009C)));
		assertTrue(Arrays.equals(valField55_9F37, res_champ55.getBytes(0x9F37)));		
	}

}
