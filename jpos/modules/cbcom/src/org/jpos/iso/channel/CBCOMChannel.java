package org.jpos.iso.channel;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.jposext.cbcom.exception.CBCOMBadIPDUException;
import org.jpos.jposext.cbcom.exception.CBCOMException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionException;
import org.jpos.jposext.cbcom.exception.CBCOMSessionStateException;
import org.jpos.jposext.cbcom.model.IPDU;
import org.jpos.jposext.cbcom.model.IPDUEnum;
import org.jpos.jposext.cbcom.model.PI;
import org.jpos.jposext.cbcom.model.PIEnum;
import org.jpos.jposext.cbcom.service.IIPDUExtractionService;
import org.jpos.jposext.cbcom.service.support.IPDUFactoryImpl;
import org.jpos.jposext.cbcom.session.model.PseudoSessionContext;
import org.jpos.jposext.cbcom.session.service.IChannelCallback;
import org.jpos.jposext.cbcom.session.service.IPseudoSessionState;
import org.jpos.jposext.cbcom.session.service.ISessionStateFactory;
import org.jpos.jposext.cbcom.session.service.support.SessionStateFactoryImpl;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

/**
 * A channel implementation that internally manage the CBCOM pseudo session
 * layer
 * 
 * @author dgrandemange
 * 
 */
public class CBCOMChannel extends BaseChannel {

	private static final String PSEUDO_SESSION_CONTEXT__ATTR_NAME__LAST_ISO_MSG = "lastIsoMsg";

	private static final int SCHED_THREAD_POOL__DEFAULT_CORE_SIZE = 100;

	/**
	 * IPDU extraction service
	 * 
	 * @author dgrandemange
	 * 
	 */
	class IPDUExtractionServiceImpl implements IIPDUExtractionService {

		/**
		 * IPDU len as read in 4 bytes header
		 */
		private int ipduLen;

		private ByteArrayInputStream ipduIs;

		/**
		 * @param bufIpdu
		 * @param ipduLen
		 * @throws CBCOMException
		 */
		public IPDUExtractionServiceImpl(byte[] bufIpdu, int ipduLen) {
			super();
			this.ipduLen = ipduLen;
			this.ipduIs = new ByteArrayInputStream(bufIpdu);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.service.IIPDUExtractionService#getApdu(int)
		 */
		public byte[] getApdu(int apduLen) {
			byte[] buf = new byte[apduLen];
			ipduIs.read(buf, 0, apduLen);
			return buf;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.service.IIPDUExtractionService#getIpduLGI()
		 */
		public int getIpduLGI() {
			byte[] buf = new byte[1];
			ipduIs.read(buf, 0, 1);
			return buf[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.service.IIPDUExtractionService#getIpduLen()
		 */
		public int getIpduLen() {
			return ipduLen;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.service.IIPDUExtractionService#getIpduPGI()
		 */
		public byte getIpduPGI() {
			byte[] buf = new byte[1];
			ipduIs.read(buf, 0, 1);
			return buf[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.service.IIPDUExtractionService#getIpduParams
		 * (int)
		 */
		public byte[] getIpduParams(int lgi) {
			byte[] buf = new byte[lgi];
			ipduIs.read(buf, 0, lgi);
			return buf;
		}

	}

	/**
	 * Callback interface <BR>
	 * Callbacks are called by the CBCOM state machine<BR>
	 * This implementation mainly rely on a CBCOMChannel instance<BR>
	 * 
	 * @author dgrandemange
	 * 
	 */
	class ChannelCallbackImpl implements IChannelCallback {

		private CBCOMChannel channel;

		public ChannelCallbackImpl(CBCOMChannel channel) {
			super();
			this.channel = channel;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.session.server.service.IChannelCallback#send
		 * (byte[])
		 */
		public void send(byte[] b) throws CBCOMSessionException {
			try {
				// Delegate the send operation to the underlying channel
				channel.send(b);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CBCOMSessionException(e);
			} catch (ISOException e) {
				e.printStackTrace();
				throw new CBCOMSessionException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.jpos.jposext.cbcom.session.server.service.IChannelCallback#
		 * processApdu(byte[], int)
		 */
		public void processApdu(byte[] apdu, int lenApdu)
				throws CBCOMSessionException {
			// Delegate apdu processing to the underlying channel
			try {
				channel.processApdu(apdu, lenApdu);
			} catch (ISOException e) {
				e.printStackTrace();
				throw new CBCOMSessionException(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.jpos.jposext.cbcom.session.server.service.IChannelCallback#close
		 * ()
		 */
		public void close() throws CBCOMSessionException {
			try {
				// Delegate the close operation to the underlying channel
				channel.closeSocket();
			} catch (IOException e) {
				// Safe to ignore, it may be already closed
				e.printStackTrace();
			}
		}

	};

	private static IPDUFactoryImpl ipduFactory = new IPDUFactoryImpl();

	private static ISessionStateFactory stateFactoryServer;

	private static ISessionStateFactory stateFactoryClient;

	static {
		stateFactoryServer = new SessionStateFactoryImpl();
		((SessionStateFactoryImpl) stateFactoryServer)
				.setAvailableStates(
						new Class[] {
								org.jpos.jposext.cbcom.session.service.support.server.InitialState.class,
								org.jpos.jposext.cbcom.session.service.support.server.ConnectedState.class,
								org.jpos.jposext.cbcom.session.service.support.server.LoggedOffState.class },
						org.jpos.jposext.cbcom.session.service.support.server.InitialState.class);

		stateFactoryClient = new SessionStateFactoryImpl();
		((SessionStateFactoryImpl) stateFactoryClient)
				.setAvailableStates(
						new Class[] {
								org.jpos.jposext.cbcom.session.service.support.client.InitialState.class,
								org.jpos.jposext.cbcom.session.service.support.client.ConnectedState.class,
								org.jpos.jposext.cbcom.session.service.support.client.LoggedOffState.class },
						org.jpos.jposext.cbcom.session.service.support.client.InitialState.class);
	}

	private PseudoSessionContext ctx;

	private int defaultTsi;

	private int defaultMaxTsi;

	private int defaultMinTsi;

	private ScheduledExecutorService defferedTaskExecutor;

	public CBCOMChannel() {
		super();
		init();
	}

	public CBCOMChannel(ISOPackager p, ServerSocket serverSocket)
			throws IOException {
		super(p, serverSocket);
		init();
	}

	public CBCOMChannel(ISOPackager p) throws IOException {
		super(p);
		init();
	}

	public CBCOMChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
		init();
	}

	void init() {
		ctx = new PseudoSessionContext();
		ctx.setIpduFactory(ipduFactory);

		// By default, we consider the channel a "client" channel.
		ctx.setStateFactory(stateFactoryClient);
		IPseudoSessionState initialState = stateFactoryClient.getInitialState();
		ctx.setSessionState(initialState);
		initialState.init(ctx);

		// In case of a "server" channel, these properties may be overridden in
		// accept(ServerSocket s) method
	}

	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		super.setConfiguration(cfg);

		// TODO Read default scheduled thread pool core size if configured, use
		// constant SCHED_THREAD_POOL__DEFAULT_CORE_SIZE otherwise
		this.defferedTaskExecutor = Executors
				.newScheduledThreadPool(SCHED_THREAD_POOL__DEFAULT_CORE_SIZE);
		// TODO See if defferedTaskExecutor be better static

		this.defaultTsi = cfg.getInt("timer-TSI-default");		
		this.ctx.setTsi(this.defaultTsi);

		this.defaultMaxTsi = cfg.getInt("timer-TSI-max");
		this.ctx.setMaxTsi(this.defaultMaxTsi);

		this.defaultMinTsi = cfg.getInt("timer-TSI-min");
		this.ctx.setMinTsi(this.defaultMaxTsi);
	}

	@Override
	public ISOMsg receive() throws IOException, ISOException {
		byte[] b = null;
		byte[] header = null;

		LogEvent evt = new LogEvent(this, "receive");

		ISOMsg m = null;
		IPDU receivedIpdu = null;

		try {
			if (!isConnected())
				throw new ISOException("unconnected CBCOMChannel");

			int len;

			synchronized (serverInLock) {

				// Trying to read length
				len = getMessageLength();

				int hLen = getHeaderLength();

				if (len == -1) {
					if (hLen > 0) {
						header = readHeader(hLen);
					}
					b = streamReceive();
				} else if (len > 0 && len <= getMaxPacketLength()) {
					if (hLen > 0) {
						// ignore message header (TPDU)
						// Note header length is not necessarily equal to hLen
						// (see VAPChannel)
						header = readHeader(hLen);
						len -= header.length;
					}
					b = new byte[len];
					getMessage(b, 0, len);
					getMessageTrailler();
				} else
					throw new ISOException("receive length " + len
							+ " seems strange - maxPacketLength = "
							+ getMaxPacketLength());
			}

			receivedIpdu = manageCBCOM(b, len);
			// At this stage, the ISOMsg may be (or not) populated
			// by CBCOM management, depending on whether the IPDU
			// was a DE or not

			// So we get ISO message from the pseudo session context ...
			m = (ISOMsg) ctx
					.get(PSEUDO_SESSION_CONTEXT__ATTR_NAME__LAST_ISO_MSG);
			// ... then remove it from context
			ctx.remove(PSEUDO_SESSION_CONTEXT__ATTR_NAME__LAST_ISO_MSG);

			// At this stage, m is either populated (IPDU DE) or null (others
			// IPDUs)

			// If m is null, there is no need to further process it
			if (null != m) {
				cnt[RX]++;
				setChanged();
				evt.addMessage(m);
				notifyObservers(m);
			}

		} catch (ISOException e) {
			evt.addMessage(e);
			if (header != null) {
				evt.addMessage("--- header ---");
				evt.addMessage(ISOUtil.hexdump(header));
			}
			if (b != null) {
				evt.addMessage("--- data ---");
				evt.addMessage(ISOUtil.hexdump(b));
			}
			throw e;
		} catch (EOFException e) {
			closeSocket();
			evt.addMessage("<peer-disconnect/>");
			throw e;
		} catch (SocketException e) {
			closeSocket();
			if (usable)
				evt.addMessage("<peer-disconnect>" + e.getMessage()
						+ "</peer-disconnect>");
			throw e;
		} catch (InterruptedIOException e) {
			closeSocket();
			evt.addMessage("<io-timeout/>");
			throw e;
		} catch (IOException e) {
			closeSocket();
			if (usable)
				evt.addMessage(e);
			throw e;
		} catch (Exception e) {
			evt.addMessage(m);
			evt.addMessage(e);
			throw new ISOException("unexpected exception", e);
		} finally {
			Logger.log(evt);
		}

		// If message (m) is null (which means : received IPDU didn't wraps an
		// APDU), we should not let the caller further process it.
		// In a case of an ISOServer caller, unless we throw a VetoException,
		// the null message is going to be transferred to the ISORequestListener
		// associated to this ISOServer. In this case, the ISORequestListener
		// should ensure message is not null before trying to process it.

		// TODO See if these 2 behaviors can be configured in the channel XML
		// configuration via a dedicated
		// "send VetoException when received IPDU has no APDU" parameter

		if (null == m) {
			if (null != receivedIpdu) {
				throw new VetoException(String.format(
						"IPDU-%s CBCOM : no apdu to process", receivedIpdu
								.getIpduType().name()));
			} else {
				throw new VetoException("IPDU CBCOM : no apdu to process");
			}
		}

		return m;
	}

	/**
	 * CBCOM management
	 * 
	 * @param b
	 * @param len
	 */
	protected IPDU manageCBCOM(byte[] b, int len) {
		IChannelCallback channelCallback = new ChannelCallbackImpl(this);

		ctx.setChannelCallback(channelCallback);

		IIPDUExtractionService ipduReader = new IPDUExtractionServiceImpl(b,
				len);

		IPDU ipdu = null;

		try {
			ipdu = ipduFactory.create(ipduReader);

			// Set the ipdu in pseudo session context
			ctx.setIpdu(ipdu);

			IPDUEnum ipduEnum = ipdu.getIpduType();
			String transitionName = String.format("onIpdu%sReceived", ipduEnum
					.name());
			Method transitionMethod = null;

			try {
				// TODO Optimisation : recherche de la méthode dans une
				// map statique pré-peuplée (statiquement par exemple)
				transitionMethod = ctx.getSessionState().getClass().getMethod(
						transitionName, PseudoSessionContext.class);
			} catch (SecurityException e) {
				// Safe to ignore
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// Unknown method : unable to handle transition
				// Keep the transitionMethod variable to null
			}

			if (null == transitionMethod) {
				// No transition method found for current state
				ctx.getSessionState().onInvalidIpduReceived(ctx);
			} else {
				try {
					transitionMethod.invoke(ctx.getSessionState(), ctx);
				} catch (IllegalArgumentException e) {
					// Safe to ignore
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// Safe to ignore
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// Safe to ignore
					e.printStackTrace();
				} catch (CBCOMSessionStateException e) {
					// Oups, state machine seems not fully implemented. Bad job here ...
					e.printStackTrace();
					try {
						this.closeSocket();
					} catch (IOException e1) {
					}
				}
			}

		} catch (CBCOMBadIPDUException e) {
			// IPDU seems invalid
			ctx.getSessionState().onInvalidIpduReceived(ctx, e);
		} catch (CBCOMException e) {
			ctx.getSessionState().onInvalidIpduReceived(ctx);
			e.printStackTrace();
		} finally {
			// Reset ipdu in pseudo session context
			ctx.setIpdu(null);
		}

		return ipdu;

	}

	/**
	 * The apdu processing has one task : try to get an ISOMsg from the apdu and
	 * put in the CBCOM pseudo session context, so that it can be further
	 * processed by jpos
	 * 
	 * @param apdu
	 * @param lenApdu
	 * @return
	 * @throws ISOException
	 */
	protected void processApdu(byte[] apdu, int lenApdu) throws ISOException {
		LogEvent evt = new LogEvent(this, "processApdu");

		try {
			ISOMsg m = createMsg();
			m.setSource(this);

			m.setPackager(getDynamicPackager(header, apdu));
			m.setHeader(getDynamicHeader(header));
			if (apdu.length > 0 && !shouldIgnore(header))
				unpack(m, apdu);
			m.setDirection(ISOMsg.INCOMING);
			m = applyIncomingFilters(m, header, apdu, evt);
			m.setDirection(ISOMsg.INCOMING);

			// Add iso msg into CBCOM session context
			ctx.put(PSEUDO_SESSION_CONTEXT__ATTR_NAME__LAST_ISO_MSG, m);

			evt.addMessage(m);
			notifyObservers(m);
		} catch (ISOException e) {
			evt.addMessage(e);
			if (header != null) {
				evt.addMessage("--- header ---");
				evt.addMessage(ISOUtil.hexdump(header));
			}
			if (apdu != null) {
				evt.addMessage("--- data ---");
				evt.addMessage(ISOUtil.hexdump(apdu));
			}
			throw e;
		}
	}

	/**
	 * Why the hell is this method private in super class BaseChannel ????
	 * 
	 * @throws IOException
	 */
	protected void closeSocket() throws IOException {
		Socket socket = getSocket();
		if (socket != null) {
			try {
				socket.setSoLinger(true, 0);
			} catch (SocketException e) {
				// safe to ignore - can be closed already
				// e.printStackTrace();
			}
			socket.close();
			socket = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jpos.iso.BaseChannel#getMessageLength()
	 */
	protected int getMessageLength() throws IOException, ISOException {
		byte[] b = new byte[4];
		serverIn.readFully(b, 0, 4);
		return (int) (((((int) b[0]) & 0xFF) << 24)
				| ((((int) b[1]) & 0xFF) << 16) | ((((int) b[2]) & 0xFF) << 8) | (((int) b[3]) & 0xFF));
	}

	@Override
	public void accept(ServerSocket s) throws IOException {
		ctx.setStateFactory(stateFactoryServer);
		IPseudoSessionState initialState = stateFactoryServer.getInitialState();
		ctx.setSessionState(initialState);
		initialState.init(ctx);

		super.accept(s);
	}

	/**
	 * sends an ISOMsg over the TCP/IP session
	 * 
	 * @param m
	 *            the Message to be sent
	 * @exception IOException
	 * @exception ISOException
	 * @exception ISOFilter.VetoException
	 *                ;
	 */
	@Override
	public void send(ISOMsg m) throws IOException, ISOException {
		LogEvent evt = new LogEvent(this, "send");
		try {
			if (!isConnected())
				throw new ISOException("unconnected ISOChannel");
			m.setDirection(ISOMsg.OUTGOING);
			m = applyOutgoingFilters(m, evt);
			evt.addMessage(m);
			m.setDirection(ISOMsg.OUTGOING); // filter may have drop this info
			m.setPackager(getDynamicPackager(m));
			byte[] bApdu = m.pack();

			// Prepare PI01
			PI pi01 = new PI(PIEnum.PI01, new byte[] {0x00});
			
			// Prepare PI07
			byte[] bLen = ByteBuffer.allocate(4).putInt(bApdu.length).array();
			PI pi07 = new PI(PIEnum.PI07, bLen);
			
			// Populate IPDU-DE with PI01, PI07, and APDU
			IPDU ipduDE = new IPDU(IPDUEnum.DE, new PI[] { pi01, pi07 }, bApdu, bApdu.length);
			byte[] bIpduDE = ipduDE.toBytes();

			synchronized (serverOutLock) {
				serverOut.write(bIpduDE);
				serverOut.flush();
			}
			cnt[TX]++;
			setChanged();
			notifyObservers(m);
			ctx.getSessionState().onIpduDEEmitted(ctx);
		} catch (VetoException e) {
			// if a filter vets the message it was not added to the event
			evt.addMessage(m);
			evt.addMessage(e);
			throw e;
		} catch (ISOException e) {
			evt.addMessage(e);
			throw e;
		} catch (IOException e) {
			evt.addMessage(e);
			throw e;
		} catch (Exception e) {
			evt.addMessage(e);
			throw new ISOException("unexpected exception", e);
		} finally {
			Logger.log(evt);
		}

	}

	@Override
	public Object clone() {
		CBCOMChannel clone = (CBCOMChannel) super.clone();

		clone.ctx = new PseudoSessionContext();
		clone.ctx.setIpduFactory(CBCOMChannel.ipduFactory);

		clone.ctx.setStateFactory(this.ctx.getStateFactory());
		IPseudoSessionState initialState = this.ctx.getStateFactory()
				.getInitialState();
		clone.ctx.setSessionState(initialState);
		initialState.init(clone.ctx);

		clone.ctx.setIpdu(null);
		clone.ctx.setTsi(this.defaultTsi);
		clone.ctx.setMinTsi(this.defaultMinTsi);
		clone.ctx.setMaxTsi(this.defaultMaxTsi);
		clone.ctx.setDefferedTaskExecutor(this.defferedTaskExecutor);

		return clone;
	}

}
