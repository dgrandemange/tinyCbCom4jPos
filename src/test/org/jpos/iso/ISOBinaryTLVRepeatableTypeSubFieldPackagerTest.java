package org.jpos.iso;

import java.util.Arrays;

import org.jpos.iso.packager.GenericPackager;

import junit.framework.TestCase;

public class ISOBinaryTLVRepeatableTypeSubFieldPackagerTest extends TestCase {
	private GenericPackager isoPackager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		isoPackager = new CustomGenericPackager();
		isoPackager.readFile(ISOBinaryTLVSubFieldPackagerTest.class
				.getResourceAsStream("test2-packager.xml"));
	}

	public void testNominal1() throws ISOException {
		byte[] valField55_009C_1 = new byte[] { 0x01 };
		byte[] valField55_9F37_1 = new byte[] { (byte) 0xF5, 0x6B, (byte) 0xA5,
				0x36 };

		byte[] valField59_0101_1 = new byte[] { 0x15, 0x10 };
		byte[] valField59_0101_2 = new byte[] { 0x16, 0x60 };

		ISOMsg msg = new ISOMsg("0100");
		
		ISOMsg champ55 = new ISOMsg(55);
		ISOMsg champ55_009C = new ISOMsg(0x009C);
		ISOMsg champ55_9F37 = new ISOMsg(0x9F37);
		champ55.set(champ55_009C);
		champ55.set(champ55_9F37);
		champ55_009C.set(1, valField55_009C_1);
		champ55_9F37.set(1, valField55_9F37_1);
		msg.set(champ55);

		ISOMsg champ59 = new ISOMsg(59);
		ISOMsg champ59_0101 = new ISOMsg(0x0101);
		champ59.set(champ59_0101);
		champ59_0101.set(1, valField59_0101_1);
		champ59_0101.set(2, valField59_0101_2);
		msg.set(champ59);

		msg.recalcBitMap();

		byte[] pack = isoPackager.pack(msg);
		System.out.println(ISOUtil.hexdump(pack));

		ISOMsg res = new ISOMsg();
		int consummed = isoPackager.unpack(res, pack);
		
		ISOMsg res_champ55 = (ISOMsg) res.getComponent(55);		
		ISOMsg res_champ55_009C = (ISOMsg) res_champ55.getComponent(0x009C);
		ISOMsg res_champ55_9F37 = (ISOMsg) res_champ55.getComponent(0x9F37);
		
		assertTrue(Arrays.equals(valField55_009C_1, res_champ55_009C
				.getBytes(1)));
		assertTrue(Arrays.equals(valField55_9F37_1, res_champ55_9F37
				.getBytes(1)));
		
		ISOMsg res_champ59 = (ISOMsg) res.getComponent(59);
		ISOMsg res_champ59_0101 = (ISOMsg) res_champ59.getComponent(0x0101);		
		
		assertTrue(Arrays.equals(valField59_0101_1, res_champ59_0101
				.getBytes(1)));
		assertTrue(Arrays.equals(valField59_0101_2, res_champ59_0101
				.getBytes(2)));
		
	}
}
