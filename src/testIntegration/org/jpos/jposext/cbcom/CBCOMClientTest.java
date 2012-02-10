package org.jpos.jposext.cbcom;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.jposext.cbcom.exception.CBCOMException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.service.IIPDUExtractionService;
import org.jpos.jposext.cbcom.service.support.IPDUFactoryImpl;

/**
 * This class operates some integration tests against a cbcom simulator instance (see jpos/modules/cbcomSimulator)
 * running on localhost:11019<BR>
 * 
 * @author dgrandemange
 * 
 */
public class CBCOMClientTest extends TestCase {

	private static final String CBCOM_SERVER_HOSTNAME = "localhost";

	private static final int CBCOM_SERVER_PORT = 11019;

	private static final int SO_TIMEOUT = 0;// 2000;

	private IPDUFactoryImpl ipduFactory = new IPDUFactoryImpl();

	private GenericPackager myPackager;

	private Socket sock;

	private InputStream is;

	private OutputStream out;

	class IPDUExtractionServiceImpl implements IIPDUExtractionService {

		private InputStream is;

		public IPDUExtractionServiceImpl(InputStream is) {
			super();
			this.is = is;
		}

		public byte[] getApdu(int apduLen) {
			try {
				byte[] b = new byte[apduLen];
				is.read(b, 0, apduLen);
				return b;

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public int getIpduLGI() {
			try {
				byte[] b = new byte[1];
				is.read(b, 0, 1);
				return b[0] & 0xFF;

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public int getIpduLen() {
			try {
				byte[] b = new byte[4];
				is.read(b, 0, 4);
				return (int) (((((int) b[0]) & 0xFF) << 24)
						| ((((int) b[1]) & 0xFF) << 16)
						| ((((int) b[2]) & 0xFF) << 8) | (((int) b[3]) & 0xFF));

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public byte getIpduPGI() {
			try {
				byte[] b = new byte[1];
				is.read(b, 0, 1);
				return b[0];

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		public byte[] getIpduParams(int lgi) {
			try {
				byte[] b = new byte[lgi];
				is.read(b, 0, lgi);
				return b;

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String myPackagerXml = "jpos/modules/cbcomSimulator/cfg/cb2a.xml";
		File myPackagerFile = new File(myPackagerXml);
		myPackager = new GenericPackagerEntityResolverAble(myPackagerFile
				.getPath());

		// Connect to server
		sock = new Socket(CBCOM_SERVER_HOSTNAME, CBCOM_SERVER_PORT);
		sock.setSoTimeout(SO_TIMEOUT);
		is = sock.getInputStream();
		out = sock.getOutputStream();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		try {
			is = null;
			out = null;
			sock.close();
		} catch (Exception e) {
			// Safe to ignore
		}
	}

	public void testSessionOK() throws UnknownHostException, IOException,
			CBCOMException, ISOException {
		// Prepare and send IPDU CN
		byte[] bufIpduCN = new byte[] { 0x00, 0x00, 0x00, 0x0B, (byte) 0xC2,
				0x09, (byte) 0x05, (byte) 0x04, (byte) 0x12, (byte) 0x02,
				(byte) 0x11, (byte) 0x20, (byte) 0x08, (byte) 0x01, (byte) 0x01 };
		System.out.println(ISOUtil.hexdump(bufIpduCN, 0, bufIpduCN.length));
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AC in response
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		assertEquals(IPDUEnum.AC, ipduResp1.getIpduType());

		// Prepare and send IPDU DE

		// First, prepare the ISO msg (APDU)
		ISOMsg m = new ISOMsg();
		m.setMTI("0100");
		m.set(2, "1234123412341234");
		m.set(4, "000000000100");
		m.recalcBitMap();
		byte[] bApdu = myPackager.pack(m);
		System.out.println(ISOUtil.hexdump(bApdu, 0, bApdu.length));

		// Then, prepare the IPDU DE
		byte[] bLi07 = ByteBuffer.allocate(4).putInt(bApdu.length).array();
		IPDU ipduReqDE = new IPDU(IPDUEnum.DE, new PI[] { new PI(PIEnum.PI07,
				bLi07) }, bApdu, bApdu.length);

		// Send the IPDU DE
		byte[] bIpduReqDE = ipduReqDE.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduReqDE, 0, bIpduReqDE.length));
		out.write(bIpduReqDE);
		out.flush();

		// Expect an IPDU DE in response
		IPDU ipduResp2 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp2 = ipduResp2.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp2, 0, bIpduResp2.length));
		assertEquals(IPDUEnum.DE, ipduResp2.getIpduType());

		assertNotNull(ipduResp2.findPiByPIEnum(PIEnum.PI01));
		assertNotNull(ipduResp2.findPiByPIEnum(PIEnum.PI07));

		byte[] bApduResp = ipduResp2.getApdu();
		System.out.println(ISOUtil.hexdump(bApduResp, 0, bApduResp.length));
		ISOMsg mResp = new ISOMsg();
		myPackager.unpack(mResp, bApduResp);

		System.out.println(String.format("Response code (field 39)=%s", mResp
				.getString(39)));

		// Prepare and send IPDU AB
		IPDU ipduAB = new IPDU(IPDUEnum.AB, new PI[] { new PI(PIEnum.PI01,
				new byte[] { (byte) 128 }) }, null, 0);

		// Sleep some times (connection should still be available after that)
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Send an IPDU AB Normal termination(connection should then be kept open by distant server)
		byte[] bIpduReqAB = ipduAB.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduReqAB, 0, bIpduReqAB.length));
		out.write(bIpduReqAB);
		out.flush();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			out.write(new byte[] { 0x00 });
			//fail("At this point, connection shouldn't be available anymore");
		} catch (java.net.SocketException e) {
			fail(e.getMessage());
			//assertTrue(e.getMessage().matches("^.*Connection reset by peer.*$"));
		}

	}

	public void testSendBadIPDUPGI() throws UnknownHostException, IOException,
			CBCOMException, ISOException {
		// Prepare and send a non existing IPDU
		byte[] bufBadIpduPGI = new byte[] { 0x00, 0x00, 0x00, 0x0B,
				(byte) 0xFF, 0x09, (byte) 0x05, (byte) 0x04, (byte) 0x12,
				(byte) 0x02, (byte) 0x11, (byte) 0x20, (byte) 0x08,
				(byte) 0x01, (byte) 0x01 };
		System.out.println(ISOUtil.hexdump(bufBadIpduPGI, 0,
				bufBadIpduPGI.length));
		out.write(bufBadIpduPGI);
		out.flush();

		// Expect an IPDU AB (abort) in response : 0x02 Invalid PGI identifier
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp1 = ipduResp1.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp1, 0, bIpduResp1.length));
		assertEquals(IPDUEnum.AB, ipduResp1.getIpduType());
		assertEquals(0x02, ipduResp1.findPiByPIEnum(PIEnum.PI01)
				.getParamValue()[0]);
	}

	public void testSendIPDUCNTwice() throws UnknownHostException, IOException,
			CBCOMException, ISOException {
		// Prepare and send IPDU CN
		byte[] bufIpduCN = new byte[] { 0x00, 0x00, 0x00, 0x0B, (byte) 0xC2,
				0x09, (byte) 0x05, (byte) 0x04, (byte) 0x12, (byte) 0x02,
				(byte) 0x11, (byte) 0x20, (byte) 0x08, (byte) 0x01, (byte) 0x01 };
		System.out.println(ISOUtil.hexdump(bufIpduCN, 0, bufIpduCN.length));
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AC in response
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		assertEquals(IPDUEnum.AC, ipduResp1.getIpduType());

		// Re-send IPDU CN
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AB (abort) in response : 0x24 Bad IPDU sequence
		IPDU ipduResp2 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp2 = ipduResp2.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp2, 0, bIpduResp2.length));
		assertEquals(IPDUEnum.AB, ipduResp2.getIpduType());
		assertEquals(0x24, ipduResp2.findPiByPIEnum(PIEnum.PI01)
				.getParamValue()[0]);
	}

	public void testSendIPDUCN_NegociateTSI_TSI_DoesNotExpires()
			throws UnknownHostException, IOException, CBCOMException,
			ISOException {

		// Prepare and send IPDU CN with inicactivity timer negociation
		// We want to negociate a 5 seconds TSI
		int tsi = 3;
		byte[] pv17 = ByteBuffer.allocate(4).putInt(tsi).array();

		IPDU ipduCN = new IPDU(IPDUEnum.CN, new PI[] {
				new PI(PIEnum.PI05, new byte[] { (byte) 0x12, (byte) 0x02,
						(byte) 0x11, (byte) 0x20 }),
				new PI(PIEnum.PI17, new byte[] { pv17[2], pv17[3] }) }, null, 0);

		byte[] bufIpduCN = ipduCN.toBytes();
		System.out.println(ISOUtil.hexdump(bufIpduCN, 0, bufIpduCN.length));
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AC in response
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp1 = ipduResp1.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp1, 0, bIpduResp1.length));
		assertEquals(IPDUEnum.AC, ipduResp1.getIpduType());

		// Prepare and send IPDU DE

		// First, prepare the ISO msg (APDU)
		ISOMsg m = new ISOMsg();
		m.setMTI("0100");
		m.set(2, "1234123412341234");
		m.set(4, "000000000100");
		m.recalcBitMap();
		byte[] bApdu = myPackager.pack(m);
		System.out.println(ISOUtil.hexdump(bApdu, 0, bApdu.length));

		// Sleep some time < TSI
		try {
			Thread.sleep(((tsi - 1) * 1000));
		} catch (InterruptedException e) {
		}

		// Then, prepare the IPDU DE
		byte[] bLi07 = ByteBuffer.allocate(4).putInt(bApdu.length).array();
		IPDU ipduReqDE = new IPDU(IPDUEnum.DE, new PI[] { new PI(PIEnum.PI07,
				bLi07) }, bApdu, bApdu.length);

		// Send the IPDU DE
		byte[] bIpduReqDE = ipduReqDE.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduReqDE, 0, bIpduReqDE.length));
		out.write(bIpduReqDE);
		out.flush();

		// Expect an IPDU DE in response
		IPDU ipduResp2 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp2 = ipduResp2.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp2, 0, bIpduResp2.length));
		assertEquals(IPDUEnum.DE, ipduResp2.getIpduType());

		byte[] bApduResp = ipduResp2.getApdu();
		System.out.println(ISOUtil.hexdump(bApduResp, 0, bApduResp.length));
		ISOMsg mResp = new ISOMsg();
		myPackager.unpack(mResp, bApduResp);

		System.out.println(String.format("Response code (field 39)=%s", mResp
				.getString(39)));

		// Sleep some time < TSI
		try {
			Thread.sleep(((tsi - 1) * 1000));
		} catch (InterruptedException e) {
		}

		// Prepare and send IPDU AB
		IPDU ipduAB = new IPDU(IPDUEnum.AB, new PI[] { new PI(PIEnum.PI01,
				new byte[] { (byte) 128 }) }, null, 0);

		// Send an IPDU AB (connection should then be closed by distant server)
		byte[] bIpduReqAB = ipduAB.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduReqAB, 0, bIpduReqAB.length));
		out.write(bIpduReqAB);
		out.flush();

		try {
			Thread.sleep(((tsi - 1) * 1000));
		} catch (InterruptedException e) {
		}

		try {
			out.write(new byte[] { 0x00 });
			//fail("At this point, connection shouldn't be available anymore");
		} catch (java.net.SocketException e) {
			fail(e.getMessage());
			//assertTrue(e.getMessage().matches("^.*Connection reset by peer.*$"));
		}
	}

	public void testSendIPDUCN_NegociateTSI_TSI_Expires()
			throws UnknownHostException, IOException, CBCOMException,
			ISOException {

		// Prepare and send IPDU CN with inactivity timer negociation
		// We want to negociate a 5 seconds TSI
		int tsi = 5;
		byte[] pv17 = ByteBuffer.allocate(4).putInt(tsi).array();

		IPDU ipduCN = new IPDU(IPDUEnum.CN, new PI[] {
				new PI(PIEnum.PI05, new byte[] { (byte) 0x12, (byte) 0x02,
						(byte) 0x11, (byte) 0x20 }),
				new PI(PIEnum.PI17, new byte[] { pv17[2], pv17[3] }) }, null, 0);

		byte[] bufIpduCN = ipduCN.toBytes();
		System.out.println(ISOUtil.hexdump(bufIpduCN, 0, bufIpduCN.length));
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AC in response
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp1 = ipduResp1.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp1, 0, bIpduResp1.length));
		assertEquals(IPDUEnum.AC, ipduResp1.getIpduType());

		// Then, prepare the IPDU DE
		ISOMsg m = new ISOMsg();
		m.setMTI("0100");
		m.set(2, "1234123412341234");
		m.set(4, "000000000100");
		m.recalcBitMap();
		byte[] bApdu = myPackager.pack(m);

		byte[] bLi07 = ByteBuffer.allocate(4).putInt(bApdu.length).array();
		IPDU ipduReqDE = new IPDU(IPDUEnum.DE, new PI[] { new PI(PIEnum.PI07,
				bLi07) }, bApdu, bApdu.length);

		// Wait for the inactivity timer time-out by peer side
		try {
			Thread.sleep(((tsi + 2) * 1000));
		} catch (InterruptedException e) {
		}

		// Send the IPDU DE
		byte[] bIpduReqDE = ipduReqDE.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduReqDE, 0, bIpduReqDE.length));
		try {
			out.write(bIpduReqDE);
			out.flush();
			fail("Socket should be closed by peer : inactivity time out should have occurred by peer side");
		} catch (IOException e) {
			assertTrue(e.getMessage().matches("^.*Connection reset by peer.*$"));
		}
	}

	public void testSendIPDUCN_NegociateTSI_OutOfBound()
			throws UnknownHostException, IOException, CBCOMException,
			ISOException {

		// Prepare and send IPDU CN with inactivity timer negociation
		// We want to negociate a 1 hour TSI (value should be > max TSI)
		int tsi = 3600; // 1 hour
		byte[] pv17 = ByteBuffer.allocate(4).putInt(tsi).array();

		IPDU ipduCN = new IPDU(IPDUEnum.CN, new PI[] {
				new PI(PIEnum.PI05, new byte[] { (byte) 0x12, (byte) 0x02,
						(byte) 0x11, (byte) 0x20 }),
				new PI(PIEnum.PI17, new byte[] { pv17[2], pv17[3] }) }, null, 0);

		byte[] bufIpduCN = ipduCN.toBytes();
		System.out.println(ISOUtil.hexdump(bufIpduCN, 0, bufIpduCN.length));
		out.write(bufIpduCN);
		out.flush();

		// Expect an IPDU AB in response
		IPDU ipduResp1 = ipduFactory.create(new IPDUExtractionServiceImpl(is));
		byte[] bIpduResp1 = ipduResp1.toBytes();
		System.out.println(ISOUtil.hexdump(bIpduResp1, 0, bIpduResp1.length));
		assertEquals(IPDUEnum.AB, ipduResp1.getIpduType());
		assertEquals(0x16, ipduResp1.findPiByPIEnum(PIEnum.PI01)
				.getParamValue()[0]);
	}
	
}
