package org.jpos.jposext.cbcom;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import junit.framework.TestCase;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.channel.CBCOMChannel;
import org.jpos.jposext.cbcom.exception.CBCOMException;

public class ClientCBCOMChannelTest extends TestCase {

	private static final int SERVER_PRE_CONNECTION_TIMER = 15;

	private static final String CBCOM_SERVER_HOSTNAME = "localhost";

	private static final int CBCOM_SERVER_PORT = 11019;

	private ISOMsg msg;

	private ISOPackager packager;

	private Configuration cfg;

	private CBCOMChannel channel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String myPackagerXml = "jpos/modules/cbcomSimulator/cfg/cb2a.xml";
		File myPackagerFile = new File(myPackagerXml);
		packager = new GenericPackagerEntityResolverAble(myPackagerFile
				.getPath());

		cfg = new SimpleConfiguration();
		cfg.put("hexdump-CBCOM", "true");
		cfg.put("TSI-timer", "5");
		cfg.put("TNR-timer", "15");
		cfg.put("TMA-timer", "720");
		cfg.put("post-connection-timer", "30");
		cfg.put("cbcom-protocol-version", "12");
		cfg.put("cb2a-protocol-version", "123");

		msg = new ISOMsg();
		msg.setMTI("0100");
		msg.set(2, "1234123412341234");
		msg.set(4, "000000000100");

		channel = new CBCOMChannel(CBCOM_SERVER_HOSTNAME, CBCOM_SERVER_PORT,
				packager);

		channel.setConfiguration(cfg);
	}

	public void testNominalOK() throws IOException, ISOException {
		channel.connect();
		channel.send(msg);
		ISOMsg resp = channel.receive();
		assertEquals("0110", resp.getMTI());
		channel.disconnect();
	}

	public void testBadCbcomVersion() throws IOException, ISOException {
		// Force a bad cbcom protocol version
		cfg.put("cbcom-protocol-version", "09");
		channel.setConfiguration(cfg);

		channel.connect();

		try {
			channel.send(msg);
			channel.receive();
			fail("A cbcom exception wrapped in a veto exception should have been thrown by channel");
			channel.disconnect();
		} catch (VetoException e) {
			assertEquals(CBCOMException.class, e.getNested().getClass());
			assertEquals(0x1E, ((CBCOMException) e.getNested()).getPI01Value());
		}
	}

	public void testPreConnectionTimerExpires() throws IOException,
			ISOException {
		channel.connect();

		try {
			Thread.sleep((SERVER_PRE_CONNECTION_TIMER + 1) * 1000);
		} catch (InterruptedException e) {
		}

		try {
			channel.send(msg);
			channel.receive();
			fail("Pre connection timer should have expired on server side");
			channel.disconnect();
		} catch (SocketException e) {
			assertTrue(e.getMessage().matches("^.*Connection reset.*$"));
		}
	}

}
