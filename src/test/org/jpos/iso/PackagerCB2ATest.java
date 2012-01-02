package org.jpos.iso;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.XMLPackager;

/**
 * Classe de test du packager CB2A
 * 
 * @author dgrandemange
 * 
 */
public class PackagerCB2ATest extends TestCase {

	private GenericPackager isoPackager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String svspackagerXml = "jpos/modules/cbcomSimulator/cfg/cb2a.xml";
		File svsPackagerFile = new File(svspackagerXml);
		isoPackager = new GenericPackagerEntityResolverAble(svsPackagerFile
				.getPath());
	}

	/**
	 * Tester pack() et unpack() d'un message 100 EMV
	 * 
	 * @throws ISOException
	 */
	public void testMsg100EMV() throws ISOException {

		byte bufMsg100Emv[] = new byte[] { (byte) 0x01, (byte) 0x00,
				(byte) 0x70, (byte) 0x38, (byte) 0x46, (byte) 0x81,
				(byte) 0x88, (byte) 0xE2, (byte) 0x8A, (byte) 0x20,
				(byte) 0x10, (byte) 0x49, (byte) 0x73, (byte) 0x01,
				(byte) 0x96, (byte) 0x00, (byte) 0x60, (byte) 0x04,
				(byte) 0x85, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x03,
				(byte) 0x51, (byte) 0x16, (byte) 0x37, (byte) 0x00,
				(byte) 0x01, (byte) 0x24, (byte) 0x48, (byte) 0x14,
				(byte) 0x00, (byte) 0x51, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x0B, (byte) 0x04, (byte) 0x97,
				(byte) 0x30, (byte) 0x03, (byte) 0x00, (byte) 0x03,
				(byte) 0x0B, (byte) 0x09, (byte) 0x25, (byte) 0x00,
				(byte) 0x02, (byte) 0x36, (byte) 0x22, (byte) 0x30,
				(byte) 0x31, (byte) 0x39, (byte) 0x36, (byte) 0x30,
				(byte) 0x30, (byte) 0x36, (byte) 0x30, (byte) 0x30,
				(byte) 0x34, (byte) 0x38, (byte) 0x35, (byte) 0x50,
				(byte) 0x55, (byte) 0x42, (byte) 0x4C, (byte) 0x49,
				(byte) 0x50, (byte) 0x48, (byte) 0x4F, (byte) 0x33,
				(byte) 0x32, (byte) 0x33, (byte) 0x31, (byte) 0x33,
				(byte) 0x34, (byte) 0x33, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x46, (byte) 0x52,
				(byte) 0x41, (byte) 0x4E, (byte) 0x43, (byte) 0x45,
				(byte) 0x20, (byte) 0x54, (byte) 0x45, (byte) 0x4C,
				(byte) 0x45, (byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x5C, (byte) 0x4C, (byte) 0x59, (byte) 0x4F,
				(byte) 0x4E, (byte) 0x5C, (byte) 0x36, (byte) 0x39,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x46, (byte) 0x52, (byte) 0x36, (byte) 0x30,
				(byte) 0x38, (byte) 0x30, (byte) 0x38, (byte) 0x31,
				(byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35,
				(byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
				(byte) 0x36, (byte) 0x31, (byte) 0x34, (byte) 0x30,
				(byte) 0x35, (byte) 0x30, (byte) 0x30, (byte) 0x38,
				(byte) 0x35, (byte) 0x39, (byte) 0x33, (byte) 0x35,
				(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
				(byte) 0x30, (byte) 0x39, (byte) 0x37, (byte) 0x30,
				(byte) 0x38, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x41, (byte) 0x30, (byte) 0x30,
				(byte) 0x38, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x09, (byte) 0x78, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6D,
				(byte) 0x00, (byte) 0x57, (byte) 0x13, (byte) 0x49,
				(byte) 0x73, (byte) 0x01, (byte) 0x96, (byte) 0x00,
				(byte) 0x60, (byte) 0x04, (byte) 0x85, (byte) 0xD1,
				(byte) 0x21, (byte) 0x22, (byte) 0x01, (byte) 0x62,
				(byte) 0x78, (byte) 0x27, (byte) 0x20, (byte) 0x31,
				(byte) 0x58, (byte) 0x3F, (byte) 0x00, (byte) 0x82,
				(byte) 0x02, (byte) 0x3C, (byte) 0x00, (byte) 0x00,
				(byte) 0x95, (byte) 0x05, (byte) 0x80, (byte) 0x80,
				(byte) 0x00, (byte) 0x80, (byte) 0x00, (byte) 0x00,
				(byte) 0x9C, (byte) 0x01, (byte) 0x00, (byte) 0x5F,
				(byte) 0x24, (byte) 0x03, (byte) 0x12, (byte) 0x12,
				(byte) 0x31, (byte) 0x9F, (byte) 0x06, (byte) 0x07,
				(byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x42, (byte) 0x10, (byte) 0x10, (byte) 0x9F,
				(byte) 0x10, (byte) 0x0A, (byte) 0x06, (byte) 0x16,
				(byte) 0x0A, (byte) 0x03, (byte) 0xA4, (byte) 0x00,
				(byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x00,
				(byte) 0x9F, (byte) 0x26, (byte) 0x08, (byte) 0x93,
				(byte) 0xD1, (byte) 0xA6, (byte) 0x91, (byte) 0x55,
				(byte) 0xAC, (byte) 0xA8, (byte) 0xCD, (byte) 0x9F,
				(byte) 0x27, (byte) 0x01, (byte) 0x80, (byte) 0x9F,
				(byte) 0x33, (byte) 0x03, (byte) 0x20, (byte) 0x80,
				(byte) 0x00, (byte) 0x9F, (byte) 0x36, (byte) 0x02,
				(byte) 0x00, (byte) 0x1F, (byte) 0x9F, (byte) 0x37,
				(byte) 0x04, (byte) 0x6E, (byte) 0xD9, (byte) 0x15,
				(byte) 0x88, (byte) 0xDF, (byte) 0x80, (byte) 0x01,
				(byte) 0x00, (byte) 0xDF, (byte) 0x81, (byte) 0x01,
				(byte) 0x02, (byte) 0x3F, (byte) 0x01, (byte) 0x01,
				(byte) 0x02, (byte) 0x15, (byte) 0x10, (byte) 0x01,
				(byte) 0x01, (byte) 0x02, (byte) 0x16, (byte) 0x60,
				(byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x11,
				(byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x47,
				(byte) 0x02, (byte) 0x01, (byte) 0x06, (byte) 0x50,
				(byte) 0x45, (byte) 0x02, (byte) 0x45, (byte) 0x09,
				(byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x04,
				(byte) 0x03, (byte) 0x23, (byte) 0x13, (byte) 0x43,
				(byte) 0x02, (byte) 0x03, (byte) 0x02, (byte) 0x00,
				(byte) 0x01, (byte) 0x02, (byte) 0x05, (byte) 0x02,
				(byte) 0x02, (byte) 0x50, (byte) 0x02, (byte) 0x07,
				(byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x30, (byte) 0x00, (byte) 0x02,
				(byte) 0x0B, (byte) 0x07, (byte) 0xA0, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x42, (byte) 0x20, (byte) 0x47 };

		ISOMsg msg = new ISOMsg();
		msg.setPackager(isoPackager);
		msg.unpack(bufMsg100Emv);

		XMLPackager xmlPkgr = new XMLPackager();
		byte[] packed = xmlPkgr.pack(msg);
		System.out.println("Msg 100 EMV :");
		System.out.println(new String(packed));

		assertEquals(msg.getValue("2"), "4973019600600485");
		assertEquals(msg.getValue("3"), "000000");
		assertEquals(msg.getValue("4"), "000000003000");
		assertEquals(msg.getValue("11"), "000351");
		assertEquals(msg.getValue("12"), "163700");
		assertEquals(msg.getValue("13"), "0124");
		assertEquals(msg.getValue("18"), "4814");
		assertEquals(msg.getValue("22"), "051");
		assertEquals(msg.getValue("23"), "000");
		assertEquals(msg.getValue("25"), "00");
		assertEquals(msg.getValue("32"), "49730030003");
		assertEquals(msg.getValue("33"), "92500023622");
		assertEquals(msg.getValue("37"), "019600600485");
		assertEquals(msg.getValue("41"), "PUBLIPHO");
		assertEquals(msg.getValue("42"), "3231343        ");
		assertEquals(msg.getValue("43"),
				"FRANCE TELECOM\\LYON\\69                FR");
		assertEquals(msg.getValue("47." + 0x08), "12345678");
		assertEquals(msg.getValue("47." + 0x96), "05008593500000");
		assertEquals(msg.getValue("47." + 0x97), "        ");
		assertEquals(msg.getValue("47." + 0xA0), "        ");
		assertEquals(msg.getValue("49"), "978");
		assertEquals(msg.getValue("53"), "0000000000000000");

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x57 + ".1"), new byte[] {
				(byte) 0x49, (byte) 0x73, (byte) 0x01, (byte) 0x96,
				(byte) 0x00, (byte) 0x60, (byte) 0x04, (byte) 0x85,
				(byte) 0xD1, (byte) 0x21, (byte) 0x22, (byte) 0x01,
				(byte) 0x62, (byte) 0x78, (byte) 0x27, (byte) 0x20,
				(byte) 0x31, (byte) 0x58, (byte) 0x3F }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x82 + ".1"), new byte[] {
				0x3C, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x95 + ".1"),
				new byte[] { (byte) 0x80, (byte) 0x80, (byte) 0x00,
						(byte) 0x80, (byte) 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9C + ".1"),
				new byte[] { 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x5F24 + ".1"),
				new byte[] { 0x12, 0x12, 0x31 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F06 + ".1"),
				new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x42, 0x10, 0x10 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F10 + ".1"),
				new byte[] { 0x06, 0x16, 0x0A, 0x03, (byte) 0xA4, 0x00, 0x00,
						0x02, 0x00, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F26 + ".1"),
				new byte[] { (byte) 0x93, (byte) 0xD1, (byte) 0xA6,
						(byte) 0x91, (byte) 0x55, (byte) 0xAC, (byte) 0xA8,
						(byte) 0xCD }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F27 + ".1"),
				new byte[] { (byte) 0x80 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F33 + ".1"),
				new byte[] { 0x20, (byte) 0x80, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F36 + ".1"),
				new byte[] { 0x00, 0x1F }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x9F37 + ".1"),
				new byte[] { 0x6e, (byte) 0xd9, 0x15, (byte) 0x88 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0xDF80 + ".1"),
				new byte[] { (byte) 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0xDF81 + ".1"),
				new byte[] { (byte) 0x2 }));

		assertNull(msg.getBytes("59." + 0x0100));

		// Tster le champ 59-101 et ses 2 répétitions (quelle merde ce truc !)
		ISOComponent field59 = msg.getComponent(59);
		assertTrue(field59 instanceof ISOMsg);
		ISOComponent field59_101 = ((ISOMsg) field59).getComponent(0x0101);
		assertTrue(field59_101 instanceof ISOMsg);
		assertEquals(2, ((ISOMsg) field59_101).getMaxField());

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0101 + ".1"),
				new byte[] { (byte) 0x15, (byte) 0x10 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0101 + ".2"),
				new byte[] { (byte) 0x16, (byte) 0x60 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0102 + ".1"),
				new byte[] { 0x11 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0200 + ".1"),
				new byte[] { 0x47 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0201 + ".1"),
				new byte[] { 0x50, 0x45, 0x02, 0x45, 0x09, 0x01 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0202 + ".1"),
				new byte[] { 0x03, 0x23, 0x13, 0x43 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0203 + ".1"),
				new byte[] { 0x00, 0x01 }));

		assertNull(msg.getBytes("59." + 0x0204));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0205 + ".1"),
				new byte[] { 0x02, 0x50 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0207 + ".1"),
				new byte[] { 0x00, 0x00, 0x00, 0x00, 0x30, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x020B + ".1"),
				new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00, 0x42, 0x20, 0x47 }));

		byte[] bufMsg100EmvPacked = msg.pack();

		System.out.println("Msg 100 EMV inital buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg100Emv));
		System.out.println("Msg 100 EMV packed buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg100EmvPacked));

		assertTrue(Arrays.equals(bufMsg100Emv, bufMsg100EmvPacked));
	}

	/**
	 * Tester pack() et unpack() d'un message 110 EMV
	 * 
	 * @throws ISOException
	 */
	public void testMsg110EMV() throws ISOException {

		byte bufMsg110Emv[] = new byte[] { (byte) 0x01, (byte) 0x10,
				(byte) 0x72, (byte) 0x28, (byte) 0x02, (byte) 0x01,
				(byte) 0x0E, (byte) 0xC0, (byte) 0x8A, (byte) 0x20,
				(byte) 0x10, (byte) 0x49, (byte) 0x73, (byte) 0x01,
				(byte) 0x96, (byte) 0x00, (byte) 0x60, (byte) 0x04,
				(byte) 0x85, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x30, (byte) 0x00, (byte) 0x01, (byte) 0x24,
				(byte) 0x16, (byte) 0x37, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x51, (byte) 0x01, (byte) 0x24,
				(byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x04,
				(byte) 0x97, (byte) 0x30, (byte) 0x03, (byte) 0x00,
				(byte) 0x03, (byte) 0x30, (byte) 0x31, (byte) 0x39,
				(byte) 0x36, (byte) 0x30, (byte) 0x30, (byte) 0x36,
				(byte) 0x30, (byte) 0x30, (byte) 0x34, (byte) 0x38,
				(byte) 0x35, (byte) 0x33, (byte) 0x32, (byte) 0x38,
				(byte) 0x34, (byte) 0x32, (byte) 0x32, (byte) 0x30,
				(byte) 0x30, (byte) 0x50, (byte) 0x55, (byte) 0x42,
				(byte) 0x4C, (byte) 0x49, (byte) 0x50, (byte) 0x48,
				(byte) 0x4F, (byte) 0x33, (byte) 0x32, (byte) 0x33,
				(byte) 0x31, (byte) 0x33, (byte) 0x34, (byte) 0x33,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x09, (byte) 0x78, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x13, (byte) 0x00,
				(byte) 0x91, (byte) 0x0A, (byte) 0x07, (byte) 0xFA,
				(byte) 0xAF, (byte) 0xB2, (byte) 0xE4, (byte) 0x19,
				(byte) 0x26, (byte) 0x24, (byte) 0x30, (byte) 0x30,
				(byte) 0x5F, (byte) 0x24, (byte) 0x03, (byte) 0x12,
				(byte) 0x12, (byte) 0x31, (byte) 0x04, (byte) 0x01,
				(byte) 0x02, (byte) 0x01, (byte) 0x11 };

		ISOMsg msg = new ISOMsg();
		msg.setPackager(isoPackager);
		msg.unpack(bufMsg110Emv);

		XMLPackager xmlPkgr = new XMLPackager();
		byte[] packed = xmlPkgr.pack(msg);
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Msg 110 EMV :");
		System.out.println(new String(packed));

		assertEquals(msg.getValue("2"), "4973019600600485");
		assertEquals(msg.getValue("3"), "000000");
		assertEquals(msg.getValue("4"), "000000003000");
		assertEquals(msg.getValue("7"), "0124163700");
		assertEquals(msg.getValue("11"), "000351");
		assertEquals(msg.getValue("13"), "0124");
		assertEquals(msg.getValue("23"), "000");
		assertEquals(msg.getValue("32"), "49730030003");
		assertEquals(msg.getValue("37"), "019600600485");
		assertEquals(msg.getValue("38"), "328422");
		assertEquals(msg.getValue("39"), "00");
		assertEquals(msg.getValue("41"), "PUBLIPHO");
		assertEquals(msg.getValue("42"), "3231343        ");
		assertEquals(msg.getValue("49"), "978");
		assertEquals(msg.getValue("53"), "0000000000000000");

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x91 + ".1"), new byte[] {
				(byte) 0x07, (byte) 0xFA, (byte) 0xAF, (byte) 0xB2,
				(byte) 0xE4, (byte) 0x19, (byte) 0x26, (byte) 0x24,
				(byte) 0x30, (byte) 0x30 }));

		assertTrue(Arrays.equals(msg.getBytes("55." + 0x5F24 + ".1"),
				new byte[] { (byte) 0x12, (byte) 0x12, (byte) 0x31 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0102 + ".1"),
				new byte[] { 0x11 }));

		byte[] bufMsg110EmvPacked = msg.pack();

		System.out.println("Msg 110 EMV inital buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg110Emv));
		System.out.println("Msg 110 EMV packed buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg110EmvPacked));

		assertTrue(Arrays.equals(bufMsg110Emv, bufMsg110EmvPacked));
	}

	/**
	 * Tester pack() et unpack() d'un message 100 Piste
	 * 
	 * @throws ISOException
	 */
	public void testMsg100Piste() throws ISOException {

		byte bufMsg100Piste[] = new byte[] { (byte) 0x01, (byte) 0x00,
				(byte) 0x70, (byte) 0x3C, (byte) 0x44, (byte) 0x81,
				(byte) 0xA8, (byte) 0xE2, (byte) 0x88, (byte) 0x20,
				(byte) 0x10, (byte) 0x45, (byte) 0x39, (byte) 0x79,
				(byte) 0x57, (byte) 0x55, (byte) 0x32, (byte) 0x61,
				(byte) 0x51, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x15, (byte) 0x24, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x16, (byte) 0x40, (byte) 0x34,
				(byte) 0x01, (byte) 0x24, (byte) 0x13, (byte) 0x08,
				(byte) 0x48, (byte) 0x14, (byte) 0x00, (byte) 0x22,
				(byte) 0x08, (byte) 0x0B, (byte) 0x05, (byte) 0x13,
				(byte) 0x23, (byte) 0x51, (byte) 0x18, (byte) 0x99,
				(byte) 0x0B, (byte) 0x09, (byte) 0x25, (byte) 0x00,
				(byte) 0x02, (byte) 0x36, (byte) 0x22, (byte) 0x25,
				(byte) 0x04, (byte) 0x53, (byte) 0x97, (byte) 0x95,
				(byte) 0x75, (byte) 0x53, (byte) 0x26, (byte) 0x15,
				(byte) 0x1D, (byte) 0x13, (byte) 0x08, (byte) 0x20,
				(byte) 0x18, (byte) 0x55, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x10, (byte) 0x01, (byte) 0x37,
				(byte) 0x39, (byte) 0x35, (byte) 0x37, (byte) 0x35,
				(byte) 0x35, (byte) 0x33, (byte) 0x32, (byte) 0x36,
				(byte) 0x31, (byte) 0x35, (byte) 0x31, (byte) 0x31,
				(byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35,
				(byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x31,
				(byte) 0x30, (byte) 0x30, (byte) 0x31, (byte) 0x32,
				(byte) 0x30, (byte) 0x35, (byte) 0x35, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x46, (byte) 0x52,
				(byte) 0x41, (byte) 0x4E, (byte) 0x43, (byte) 0x45,
				(byte) 0x20, (byte) 0x54, (byte) 0x45, (byte) 0x4C,
				(byte) 0x45, (byte) 0x43, (byte) 0x4F, (byte) 0x4D,
				(byte) 0x5C, (byte) 0x4C, (byte) 0x59, (byte) 0x4F,
				(byte) 0x4E, (byte) 0x5C, (byte) 0x36, (byte) 0x39,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x46, (byte) 0x52, (byte) 0x36, (byte) 0x30,
				(byte) 0x38, (byte) 0x30, (byte) 0x38, (byte) 0x31,
				(byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35,
				(byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39,
				(byte) 0x36, (byte) 0x31, (byte) 0x34, (byte) 0x30,
				(byte) 0x35, (byte) 0x30, (byte) 0x30, (byte) 0x38,
				(byte) 0x35, (byte) 0x39, (byte) 0x33, (byte) 0x35,
				(byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30,
				(byte) 0x30, (byte) 0x39, (byte) 0x37, (byte) 0x30,
				(byte) 0x38, (byte) 0x31, (byte) 0x32, (byte) 0x33,
				(byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37,
				(byte) 0x38, (byte) 0x41, (byte) 0x30, (byte) 0x30,
				(byte) 0x38, (byte) 0x31, (byte) 0x32, (byte) 0x33,
				(byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37,
				(byte) 0x38, (byte) 0x09, (byte) 0x78, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3B,
				(byte) 0x01, (byte) 0x01, (byte) 0x02, (byte) 0x55,
				(byte) 0x55, (byte) 0x01, (byte) 0x02, (byte) 0x01,
				(byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0x01,
				(byte) 0x48, (byte) 0x02, (byte) 0x01, (byte) 0x06,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x02,
				(byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x20,
				(byte) 0x55, (byte) 0x02, (byte) 0x03, (byte) 0x02,
				(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x05,
				(byte) 0x02, (byte) 0x02, (byte) 0x50, (byte) 0x02,
				(byte) 0x07, (byte) 0x06, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x15, (byte) 0x24,
				(byte) 0x02, (byte) 0x0B, (byte) 0x08, (byte) 0x99,
				(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
				(byte) 0x99, (byte) 0x99, (byte) 0x99 };

		ISOMsg msg = new ISOMsg();
		msg.setPackager(isoPackager);
		msg.unpack(bufMsg100Piste);

		XMLPackager xmlPkgr = new XMLPackager();
		byte[] packed = xmlPkgr.pack(msg);
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Msg 100 Piste :");
		System.out.println(new String(packed));

		assertEquals(msg.getValue("2"), "4539795755326151");
		assertEquals(msg.getValue("3"), "000000");
		assertEquals(msg.getValue("4"), "000000001524");
		assertEquals(msg.getValue("11"), "000003");
		assertEquals(msg.getValue("12"), "164034");
		assertEquals(msg.getValue("13"), "0124");
		assertEquals(msg.getValue("14"), "1308");
		assertEquals(msg.getValue("18"), "4814");
		assertEquals(msg.getValue("22"), "022");
		assertEquals(msg.getValue("25"), "08");
		assertEquals(msg.getValue("32"), "51323511899");
		assertEquals(msg.getValue("33"), "92500023622");
		Object value = msg.getValue("35");
		assertEquals(value, "4539795755326151=13082018550000001001");
		assertEquals(msg.getValue("37"), "795755326151");
		assertEquals(msg.getValue("41"), "12345678");
		assertEquals(msg.getValue("42"), "10012055       ");
		assertEquals(msg.getValue("43"),
				"FRANCE TELECOM\\LYON\\69                FR");
		assertEquals(msg.getValue("47." + 0x08), "12345678");
		assertEquals(msg.getValue("47." + 0x96), "05008593500000");
		assertEquals(msg.getValue("47." + 0x97), "12345678");
		assertEquals(msg.getValue("47." + 0xA0), "12345678");
		assertEquals(msg.getValue("49"), "978");
		assertEquals(msg.getValue("53"), "0000000000000000");

		// Tester le champ 59-101 et ses répétitions (s'il y en a d'attendues)
		ISOComponent field59 = msg.getComponent(59);
		assertTrue(field59 instanceof ISOMsg);
		ISOComponent field59_101 = ((ISOMsg) field59).getComponent(0x0101);
		assertTrue(field59_101 instanceof ISOMsg);
		assertEquals(1, ((ISOMsg) field59_101).getMaxField());

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0101 + ".1"),
				new byte[] { (byte) 0x55, (byte) 0x55 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0102 + ".1"),
				new byte[] { 0x11 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0200 + ".1"),
				new byte[] { 0x48 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0201 + ".1"),
				new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0202 + ".1"),
				new byte[] { 0x00, 0x01, 0x20, 0x55 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0203 + ".1"),
				new byte[] { 0x00, 0x01 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0205 + ".1"),
				new byte[] { 0x02, 0x50 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0207 + ".1"),
				new byte[] { 0x00, 0x00, 0x00, 0x00, 0x15, 0x24 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x020B + ".1"),
				new byte[] { (byte) 0x99, (byte) 0x99, (byte) 0x99,
						(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
						(byte) 0x99 }));

		byte[] bufMsg100PistePacked = msg.pack();

		System.out.println("Msg 100 Piste inital buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg100Piste));
		System.out.println("Msg 100 Piste packed buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg100PistePacked));

		assertTrue(Arrays.equals(bufMsg100Piste, bufMsg100PistePacked));
	}

	/**
	 * Tester pack() et unpack() d'un message 110 Piste
	 * 
	 * @throws ISOException
	 */
	public void testMsg110Piste() throws ISOException {

		byte bufMsg110Piste[] = new byte[] { (byte) 0x01, (byte) 0x10,
				(byte) 0x70, (byte) 0x3C, (byte) 0x44, (byte) 0x81,
				(byte) 0x8A, (byte) 0xF0, (byte) 0x88, (byte) 0x20,
				(byte) 0x10, (byte) 0x45, (byte) 0x39, (byte) 0x79,
				(byte) 0x57, (byte) 0x55, (byte) 0x32, (byte) 0x61,
				(byte) 0x51, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x15, (byte) 0x24, (byte) 0x00, (byte) 0x00,
				(byte) 0x03, (byte) 0x16, (byte) 0x40, (byte) 0x34,
				(byte) 0x01, (byte) 0x24, (byte) 0x13, (byte) 0x08,
				(byte) 0x48, (byte) 0x14, (byte) 0x00, (byte) 0x22,
				(byte) 0x08, (byte) 0x0B, (byte) 0x05, (byte) 0x13,
				(byte) 0x23, (byte) 0x51, (byte) 0x18, (byte) 0x99,
				(byte) 0x0B, (byte) 0x09, (byte) 0x25, (byte) 0x00,
				(byte) 0x02, (byte) 0x36, (byte) 0x22, (byte) 0x37,
				(byte) 0x39, (byte) 0x35, (byte) 0x37, (byte) 0x35,
				(byte) 0x35, (byte) 0x33, (byte) 0x32, (byte) 0x36,
				(byte) 0x31, (byte) 0x35, (byte) 0x31, (byte) 0x33,
				(byte) 0x30, (byte) 0x31, (byte) 0x32, (byte) 0x33,
				(byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37,
				(byte) 0x38, (byte) 0x31, (byte) 0x30, (byte) 0x30,
				(byte) 0x31, (byte) 0x32, (byte) 0x30, (byte) 0x35,
				(byte) 0x35, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x46, (byte) 0x52, (byte) 0x41, (byte) 0x4E,
				(byte) 0x43, (byte) 0x45, (byte) 0x20, (byte) 0x54,
				(byte) 0x45, (byte) 0x4C, (byte) 0x45, (byte) 0x43,
				(byte) 0x4F, (byte) 0x4D, (byte) 0x5C, (byte) 0x4C,
				(byte) 0x59, (byte) 0x4F, (byte) 0x4E, (byte) 0x5C,
				(byte) 0x36, (byte) 0x39, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
				(byte) 0x20, (byte) 0x20, (byte) 0x46, (byte) 0x52,
				(byte) 0x09, (byte) 0x41, (byte) 0x41, (byte) 0x30,
				(byte) 0x35, (byte) 0x30, (byte) 0x34, (byte) 0x37,
				(byte) 0x30, (byte) 0x31, (byte) 0x09, (byte) 0x78,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x36, (byte) 0x01, (byte) 0x02, (byte) 0x01,
				(byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0x01,
				(byte) 0x48, (byte) 0x02, (byte) 0x01, (byte) 0x06,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x02,
				(byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x20,
				(byte) 0x55, (byte) 0x02, (byte) 0x03, (byte) 0x02,
				(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x05,
				(byte) 0x02, (byte) 0x02, (byte) 0x50, (byte) 0x02,
				(byte) 0x07, (byte) 0x06, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x15, (byte) 0x24,
				(byte) 0x02, (byte) 0x0B, (byte) 0x08, (byte) 0x99,
				(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
				(byte) 0x99, (byte) 0x99, (byte) 0x99 };

		ISOMsg msg = new ISOMsg();
		msg.setPackager(isoPackager);
		msg.unpack(bufMsg110Piste);

		XMLPackager xmlPkgr = new XMLPackager();
		byte[] packed = xmlPkgr.pack(msg);
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Msg 110 Piste :");
		System.out.println(new String(packed));

		assertEquals(msg.getValue("2"), "4539795755326151");
		assertEquals(msg.getValue("3"), "000000");
		assertEquals(msg.getValue("4"), "000000001524");
		assertEquals(msg.getValue("11"), "000003");
		assertEquals(msg.getValue("12"), "164034");
		assertEquals(msg.getValue("13"), "0124");
		assertEquals(msg.getValue("14"), "1308");
		assertEquals(msg.getValue("18"), "4814");
		assertEquals(msg.getValue("22"), "022");
		assertEquals(msg.getValue("25"), "08");
		assertEquals(msg.getValue("32"), "51323511899");
		assertEquals(msg.getValue("37"), "795755326151");
		assertEquals(msg.getValue("39"), "30");
		assertEquals(msg.getValue("41"), "12345678");
		assertEquals(msg.getValue("42"), "10012055       ");
		assertEquals(msg.getValue("43"),
				"FRANCE TELECOM\\LYON\\69                FR");
		assertEquals(msg.getValue("44." + 0xAA), "04701");
		assertEquals(msg.getValue("49"), "978");
		assertEquals(msg.getValue("53"), "0000000000000000");

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0102 + ".1"),
				new byte[] { 0x11 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0200 + ".1"),
				new byte[] { 0x48 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0201 + ".1"),
				new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0202 + ".1"),
				new byte[] { 0x00, 0x01, 0x20, 0x55 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0203 + ".1"),
				new byte[] { 0x00, 0x01 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0205 + ".1"),
				new byte[] { 0x02, 0x50 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x0207 + ".1"),
				new byte[] { 0x00, 0x00, 0x00, 0x00, 0x15, 0x24 }));

		assertTrue(Arrays.equals(msg.getBytes("59." + 0x020B + ".1"),
				new byte[] { (byte) 0x99, (byte) 0x99, (byte) 0x99,
						(byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99,
						(byte) 0x99 }));

		byte[] bufMsg110PistePacked = msg.pack();

		System.out.println("Msg 110 Piste inital buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg110Piste));
		System.out.println("Msg 110 Piste packed buffer :");
		System.out.println(ISOUtil.hexdump(bufMsg110PistePacked));

		assertTrue(Arrays.equals(bufMsg110Piste, bufMsg110PistePacked));
	}
}
