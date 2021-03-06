package org.jpos.iso.channel;

import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.qbean.QThreadPoolExecutor;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar.NotFoundException;

import fr.dgrandemange.cbcom.exception.CBCOMBadIPDUException;
import fr.dgrandemange.cbcom.exception.CBCOMException;
import fr.dgrandemange.cbcom.exception.CBCOMSessionClosedException;
import fr.dgrandemange.cbcom.exception.CBCOMSessionStateException;
import fr.dgrandemange.cbcom.model.IPDU;
import fr.dgrandemange.cbcom.model.IPDUEnum;
import fr.dgrandemange.cbcom.model.PIEnum;
import fr.dgrandemange.cbcom.service.IIPDUExtractionService;
import fr.dgrandemange.cbcom.service.support.IPDUExtractionServiceImpl;
import fr.dgrandemange.cbcom.service.support.IPDUFactoryImpl;
import fr.dgrandemange.cbcom.session.model.PseudoSessionContext;
import fr.dgrandemange.cbcom.session.model.TimerConfig;
import fr.dgrandemange.cbcom.session.service.IIdentificationProtocolValidator;
import fr.dgrandemange.cbcom.session.service.IPseudoSessionState;
import fr.dgrandemange.cbcom.session.service.ISessionStateFactory;
import fr.dgrandemange.cbcom.session.service.IThreadPoolExecutorProvider;
import fr.dgrandemange.cbcom.session.service.support.ChannelCallbackImpl;
import fr.dgrandemange.cbcom.session.service.support.SessionStateFactoryImpl;

/**
 * A channel implementation that internally manage the CBCOM pseudo session
 * layer
 * 
 * @author dgrandemange
 * 
 */
@SuppressWarnings("unchecked")
public class CBCOMChannel extends BaseChannel {

	class IncomingApduNotifier {

		private byte[] apdu;

		private LogEvent evt;

		private Exception exception;

		private byte abortCode;

		public IncomingApduNotifier(LogEvent evt) {
			super();
			this.evt = evt;
		}

		public byte[] getApdu() {
			return apdu;
		}

		public LogEvent getEvt() {
			return evt;
		}

		public void setEvt(LogEvent evt) {
			this.evt = evt;
		}

		public void setApdu(byte[] apdu) {
			this.apdu = apdu;
		}

		public void add(Object msg) {
			evt.addMessage(msg);
		}

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public byte getAbortCode() {
			return abortCode;
		}

		public void setAbortCode(byte abortCode) {
			this.abortCode = abortCode;
		}

	}

	class IncomingIpduMgmtTask implements Callable<Object> {

		/**
		 * The CBCOM Channel
		 */
		private CBCOMChannel channel;

		/**
		 * Synchronization object to notify that an apdu has been successfully
		 * extracted from an IPDU DE
		 */
		private IncomingApduNotifier incomingApduNotifier;

		public IncomingIpduMgmtTask(CBCOMChannel channel,
				IncomingApduNotifier incomingApduNotifier) {
			super();
			this.channel = channel;
			this.incomingApduNotifier = incomingApduNotifier;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public Object call() throws Exception {
			ctx.setChannelCallback(new ChannelCallbackImpl(this.channel));

			IPDU lastIpduReceived = null;
			boolean sessionAborted = false;
			byte[] b = null;

			try {
				while (!sessionAborted) {
					if (!channel.isConnected())
						throw new ISOException("unconnected CBCOMChannel");

					int len;

					synchronized (channel.serverInLock) {

						// Trying to read length
						len = channel.getMessageLength();

						int hLen = channel.getHeaderLength();

						if (len == -1) {
							if (hLen > 0) {
								header = channel.readHeader(hLen);
							}
							b = channel.streamReceive();
						} else if (len > 0
								&& len <= channel.getMaxPacketLength()) {
							if (hLen > 0) {
								// ignore message header (TPDU)
								// Note header length is not necessarily equal
								// to hLen
								// (see VAPChannel)
								header = channel.readHeader(hLen);
								len -= header.length;
							}
							b = new byte[len];
							channel.getMessage(b, 0, len);
							channel.getMessageTrailler();
						} else
							throw new ISOException("receive length " + len
									+ " seems strange - maxPacketLength = "
									+ channel.getMaxPacketLength());
					}

					lastIpduReceived = extractIPDU(b, len);

					manageReceivedIPDU(lastIpduReceived);

					IPDUEnum lastReceivedIpduType = lastIpduReceived
							.getIpduType();
					sessionAborted = (IPDUEnum.AB == lastReceivedIpduType)
							|| (!isConnected());

					if (IPDUEnum.AB == lastReceivedIpduType) {
						byte abortCode;
						try {
							abortCode = lastIpduReceived.findPiByPIEnum(
									PIEnum.PI01).getParamValue()[0];
						} catch (Exception e) {
							abortCode = 0x04;
						}
						if ((0x80 != abortCode) || (0x00 != abortCode)) {
							throw new VetoException(new CBCOMException(
									abortCode));
						}
					}

					if (!sessionAborted) {
						if (IPDUEnum.DE == lastReceivedIpduType) {
							incomingApduNotifier.setApdu(lastIpduReceived
									.getApdu());
							synchronized (incomingApduNotifier) {
								incomingApduNotifier.notify();
							}
						}
					}

					Thread.yield();

				}
			} catch (VetoException e) {
				incomingApduNotifier.setException(e);
			} catch (ISOException e) {
				if (header != null) {
					incomingApduNotifier.add("--- header ---");
					incomingApduNotifier.add(ISOUtil.hexdump(header));
				}
				if (b != null) {
					incomingApduNotifier.add("--- data ---");
					incomingApduNotifier.add(ISOUtil.hexdump(b));
				}
				incomingApduNotifier.setException(e);
			} catch (EOFException e) {
				incomingApduNotifier.setException(e);
			} catch (SocketException e) {
				incomingApduNotifier.setException(e);
			} catch (InterruptedIOException e) {
				incomingApduNotifier.setException(e);
			} catch (IOException e) {
				incomingApduNotifier.setException(e);
			} catch (Exception e) {
				incomingApduNotifier.setException(e);
			}

			synchronized (incomingApduNotifier) {
				incomingApduNotifier.notify();
			}

			return null;

		}

		protected IPDU extractIPDU(byte[] b, int len) {
			IIPDUExtractionService ipduReader = new IPDUExtractionServiceImpl(
					b, len);

			IPDU ipdu = null;

			PseudoSessionContext sessionCtx = channel.ctx;

			try {
				ipdu = CBCOMChannel.ipduFactory.create(ipduReader);
				if (logCBCOM) {
					LogEvent evt = new LogEvent(channel, "receive");
					evt.addMessage(ISOUtil.hexdump(b));
					Logger.log(evt);
				}
			} catch (CBCOMBadIPDUException e) {
				// IPDU seems invalid
				sessionCtx.getSessionState().onInvalidIpduReceived(sessionCtx,
						e);
			} catch (CBCOMException e) {
				sessionCtx.getSessionState().onInvalidIpduReceived(sessionCtx);
			}

			return ipdu;
		}

		/**
		 * 
		 * @param b
		 * @param len
		 */
		protected void manageReceivedIPDU(IPDU ipdu) {

			PseudoSessionContext sessionCtx = channel.ctx;

			// Set the ipdu in pseudo session context
			sessionCtx.setIpdu(ipdu);

			try {
				IPDUEnum ipduEnum = ipdu.getIpduType();
				String transitionName = String.format("onIpdu%sReceived",
						ipduEnum.name());
				Method transitionMethod = null;

				try {
					// TODO Method search optimization ?
					transitionMethod = sessionCtx
							.getSessionState()
							.getClass()
							.getMethod(transitionName,
									PseudoSessionContext.class);
				} catch (SecurityException e) {
					// Safe to ignore
				} catch (NoSuchMethodException e) {
					// Unknown method : unable to handle transition
					// Keep the transitionMethod variable to null
				}

				if (null == transitionMethod) {
					// No transition method found for current state
					sessionCtx.getSessionState().onInvalidIpduReceived(
							sessionCtx);
				} else {
					try {
						transitionMethod.invoke(sessionCtx.getSessionState(),
								sessionCtx);
					} catch (IllegalArgumentException e) {
						// Safe to ignore
					} catch (IllegalAccessException e) {
						// Safe to ignore
					} catch (InvocationTargetException e) {
						// Safe to ignore
					} catch (CBCOMSessionStateException e) {
						// Oups, state machine seems not fully implemented. Bad
						// job
						// here ...
						e.printStackTrace();
						try {
							channel.closeSocket();
						} catch (IOException e1) {
						}
					}
				}
			} finally {
				// Reset the ipdu in pseudo session context
				sessionCtx.setIpdu(null);
			}
		}
	}

	class ThreadPoolExecutorProviderImpl implements IThreadPoolExecutorProvider {

		private String executorName;

		public ThreadPoolExecutorProviderImpl(String executorName) {
			super();
			this.executorName = executorName;
		}

		public <T extends ThreadPoolExecutor> T provide(Class<T> clazz) {
			T exec;
			try {
				exec = QThreadPoolExecutor.getThreadPoolExecutor(executorName,
						clazz);
			} catch (NotFoundException e) {
				exec = null;
			}
			return exec;
		}
	}

	private static IPDUFactoryImpl ipduFactory = new IPDUFactoryImpl();

	private static ISessionStateFactory stateFactoryServer;

	private static ISessionStateFactory stateFactoryClient;

	private IThreadPoolExecutorProvider ipduMgmtTaskExecutorProvider;

	static {
		stateFactoryServer = new SessionStateFactoryImpl();
		((SessionStateFactoryImpl) stateFactoryServer)
				.setAvailableStates(
						new Class[] {
								fr.dgrandemange.cbcom.session.service.support.server.InitialState.class,
								fr.dgrandemange.cbcom.session.service.support.server.ConnectedState.class,
								fr.dgrandemange.cbcom.session.service.support.server.LoggedOffState.class },
						fr.dgrandemange.cbcom.session.service.support.server.InitialState.class,
						fr.dgrandemange.cbcom.session.service.support.server.LoggedOffState.class);

		stateFactoryClient = new SessionStateFactoryImpl();
		((SessionStateFactoryImpl) stateFactoryClient)
				.setAvailableStates(
						new Class[] {
								fr.dgrandemange.cbcom.session.service.support.client.InitialState.class,
								fr.dgrandemange.cbcom.session.service.support.client.ConnectedState.class,
								fr.dgrandemange.cbcom.session.service.support.client.LoggedOffState.class },
						fr.dgrandemange.cbcom.session.service.support.client.InitialState.class,
						fr.dgrandemange.cbcom.session.service.support.client.LoggedOffState.class);
	}

	private PseudoSessionContext ctx;

	private transient Future<Object> ipduMgmtTaskFuture;

	private transient IncomingApduNotifier incomingApduNotifier;

	private boolean logCBCOM;

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
		// ctx.setPseudoSessionTimerTasksExecutorProvider(new
		// PseudoSessionTimerTasksExecutorProvider(this));
	}

	@Override
	public void setConfiguration(Configuration cfg)
			throws ConfigurationException {
		super.setConfiguration(cfg);

		String strLogCBCOM = cfg.get("hexdump-CBCOM", "false");
		this.logCBCOM = Boolean.parseBoolean(strLogCBCOM);

		TimerConfig timerCfg = new TimerConfig();

		String strTgrTimer = cfg.get("TGR-timer", "30");
		timerCfg.setTgr(Integer.parseInt(strTgrTimer));

		String strTsiTimer = cfg.get("TSI-timer", "780");
		timerCfg.setInitialTsi(Integer.parseInt(strTsiTimer));
		timerCfg.setNegotiatedTsi(timerCfg.getInitialTsi());

		String strMinTsi = cfg.get("TSI-timer-min", "120");
		timerCfg.setMinTsi(Integer.parseInt(strMinTsi));

		String strMaxTsi = cfg.get("TSI-timer-max", "1800");
		timerCfg.setMaxTsi(Integer.parseInt(strMaxTsi));

		String strTnrTimer = cfg.get("TNR-timer", "50");
		timerCfg.setInitialTnr(Integer.parseInt(strTnrTimer));
		timerCfg.setNegotiatedTnr(timerCfg.getInitialTnr());

		String strMinTnr = cfg.get("TNR-timer-min", "" + timerCfg.getTgr() + 2);
		timerCfg.setMinTnr(Integer.parseInt(strMinTnr));

		String strMaxTnr = cfg
				.get("TNR-timer-max", "" + timerCfg.getTgr() + 32);
		timerCfg.setMaxTnr(Integer.parseInt(strMaxTnr));

		String strTmaTimer = cfg.get("TMA-timer", "720");
		timerCfg.setInitialTma(Integer.parseInt(strTmaTimer));
		timerCfg.setNegotiatedTma(timerCfg.getInitialTma());

		String strMinTma = cfg.get("TMA-timer-min", "120");
		timerCfg.setMinTma(Integer.parseInt(strMinTma));

		String strMaxTma = cfg.get("TMA-timer-max", "1800");
		timerCfg.setMaxTma(Integer.parseInt(strMaxTma));

		String strPreConnectionTimer = cfg.get("pre-connection-timer", "15");
		timerCfg.setPreConnectionTimer(Integer.parseInt(strPreConnectionTimer));

		String strInterSessionTimer = cfg.get("inter-session-timer", "30");
		timerCfg.setInterSessionTimer(Integer.parseInt(strInterSessionTimer));

		String strPostConnectionTimer = cfg.get("post-connection-timer", "30");
		timerCfg.setPostCnxTimer(Integer.parseInt(strPostConnectionTimer));

		this.ctx.setTimerConfig(timerCfg);

		String strCBCOMProtocolVersion = cfg
				.get("cbcom-protocol-version", "11");
		byte[] cbcomProtocolVersion = ISOUtil.str2bcd(strCBCOMProtocolVersion,
				false);
		this.ctx.setCbcomProtocolVersion(cbcomProtocolVersion[0]);

		String strProtocolType = cfg.get("protocol-type", "02");
		byte[] protocolType = ISOUtil.str2bcd(strProtocolType, false);
		this.ctx.setProtocolType(protocolType[0]);

		String strCB2AProtocolVersion = cfg.get("cb2a-protocol-version", "123");
		byte[] cb2aProtocolVersion = ISOUtil.str2bcd(strCB2AProtocolVersion,
				false);
		this.ctx.setCb2aProtocolVersion(cb2aProtocolVersion);

		String strIdProtValidatorClass = cfg.get(
				"identification-protocol-validator", null);
		IIdentificationProtocolValidator idProtValidator;
		try {
			idProtValidator = createIdProtValidator(strIdProtValidatorClass);
			this.ctx.setIdProtValidator(idProtValidator);
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}

		/**
		 * Add two properties : 1. a reference to a Q2 registered fixed thread
		 * pool executor (dedicated to ipdu management tasks) 2. a reference to
		 * a Q2 registered scheduled thread pool executor (dedicated to pseudo
		 * session timers management)
		 * 
		 * Configuration should fail if referenced executors are not found
		 */
		String lIpduMgmtTaskExecutorQName = cfg.get(
				"ipdu-mgmt-executor-qbean-ref", null);
		// TODO Should we check executor registration here ?  
//		try {
//			QThreadPoolExecutor
//					.getThreadPoolExecutor(lIpduMgmtTaskExecutorQName);
//		} catch (NotFoundException e) {
//			throw new ConfigurationException(e);
//		}

		String lPseudoSessionTimerTaskExecutorQName = cfg.get(
				"pseudosession-timer-mgmt-executor-qbean-ref", null);
		// TODO Should we check executor registration here ?
//		try {
//			QThreadPoolExecutor
//					.getThreadPoolExecutor(lPseudoSessionTimerTaskExecutorQName);
//		} catch (NotFoundException e) {
//			throw new ConfigurationException(e);
//		}

		this.ipduMgmtTaskExecutorProvider = new ThreadPoolExecutorProviderImpl(
				lIpduMgmtTaskExecutorQName);
		
		ctx.setPseudoSessionTimerTasksExecutorProvider(new ThreadPoolExecutorProviderImpl(
				lPseudoSessionTimerTaskExecutorQName));
	}

	protected IIdentificationProtocolValidator createIdProtValidator(
			String strIdProtValidatorClass) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		IIdentificationProtocolValidator validator = null;

		if (null != strIdProtValidatorClass) {
			Class<?> clazz = Class.forName(strIdProtValidatorClass);
			if (IIdentificationProtocolValidator.class.isAssignableFrom(clazz)) {
				validator = (IIdentificationProtocolValidator) clazz
						.newInstance();
			}
		}

		return validator;
	}

	@Override
	public ISOMsg receive() throws IOException, ISOException {
		LogEvent evt = new LogEvent(this, "receive");
		ISOMsg m = null;
		byte[] apdu = null;

		ThreadPoolExecutor ipduMgmtTaskExecutor = this.ipduMgmtTaskExecutorProvider
				.provide(ThreadPoolExecutor.class);

		if (null == ipduMgmtTaskExecutor) {
			String err = "Unable to retrieve ipdu mgr task executor service";
			evt.addMessage(err);
			Logger.log(evt);
			throw new ISOException(err);
		}

		if ((null == ipduMgmtTaskFuture) || (ipduMgmtTaskFuture.isDone())
				|| (ipduMgmtTaskFuture.isCancelled())) {
			incomingApduNotifier = new IncomingApduNotifier(evt);

			ipduMgmtTaskFuture = ipduMgmtTaskExecutor
					.submit(new IncomingIpduMgmtTask(this, incomingApduNotifier));
		}

		try {
			// TODO Set a time-out to the wait() ?
			synchronized (incomingApduNotifier) {
				incomingApduNotifier.wait();
				apdu = incomingApduNotifier.getApdu();
				incomingApduNotifier.setApdu(null);
			}

			Exception e = incomingApduNotifier.getException();
			if (e != null) {
				throw e;
			}

			if (apdu != null) {

				m = createMsg();
				m.setSource(this);
				m.setPackager(getDynamicPackager(header, apdu));
				m.setHeader(getDynamicHeader(header));
				if (apdu.length > 0 && !shouldIgnore(header))
					unpack(m, apdu);
				m.setDirection(ISOMsg.INCOMING);
				m = applyIncomingFilters(m, header, apdu, evt);
				m.setDirection(ISOMsg.INCOMING);

				evt.addMessage(m);
				cnt[RX]++;
				setChanged();
				notifyObservers(m);
			}
		} catch (InterruptedException e) {
			evt.addMessage(e);
			// TODO Think about this case
			e.printStackTrace();
		} catch (VetoException e) {
			if (e.getNested() instanceof CBCOMException) {
				byte pi01Value = ((CBCOMException) e.getNested())
						.getPI01Value();
				evt.addMessage(String.format(
						"CBCOM pseudo session abort code=0x%x", pi01Value));
			}
			throw e;
		} catch (ISOException e) {
			evt.addMessage(e);
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
			evt.addMessage(e);
			throw new ISOException("unexpected exception", e);
		} finally {
			Logger.log(evt);
		}

		if (null != m) {
			return m;
		} else {
			throw new VetoException();
		}

	}

	/**
	 * Why the hell is this method private in super class BaseChannel ????
	 * 
	 * @throws IOException
	 */
	public void closeSocket() throws IOException {
		Socket socket = getSocket();
		if (socket != null) {
			try {
				socket.setSoLinger(true, 0);
			} catch (SocketException e) {
				// safe to ignore - can be closed already
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
	public void connect() throws IOException {
		super.connect();

		if (serverSocket != null) {
			ctx.setStateFactory(stateFactoryServer);
		} else {
			ctx.setStateFactory(stateFactoryClient);
		}

		IPseudoSessionState initialState = ctx.getStateFactory()
				.getInitialState();
		ctx.setSessionState(initialState);
		initialState.init(ctx);
	}

	@Override
	public void accept(ServerSocket s) throws IOException {
		super.accept(s);
		ctx.setStateFactory(stateFactoryServer);
		IPseudoSessionState initialState = ctx.getStateFactory()
				.getInitialState();
		ctx.setSessionState(initialState);
		initialState.init(ctx);
	}

	/**
	 * sends a byte[] over the TCP/IP session
	 * 
	 * @param b
	 *            Buffer to send
	 * @param doCount
	 *            Indicates if the transmit counter should be incremented or not
	 * @throws IOException
	 * @throws ISOException
	 */
	public void send(byte[] b, boolean doCount) throws IOException,
			ISOException {
		LogEvent evt = new LogEvent(this, "send");

		try {
			if (!isConnected())
				throw new ISOException("unconnected ISOChannel");
			synchronized (serverOutLock) {
				serverOut.write(b);
				serverOut.flush();
			}

			if (doCount) {
				cnt[TX]++;
			}

			if (logCBCOM) {
				evt.addMessage(ISOUtil.hexdump(b));
			}

			setChanged();
		} catch (Exception e) {
			evt.addMessage(e);
			throw new ISOException("unexpected exception", e);
		} finally {
			if (logCBCOM) {
				Logger.log(evt);
			}
		}
	}

	/**
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
		ctx.setChannelCallback(new ChannelCallbackImpl(this));

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

			// Now, we just have to put the apdu in the context
			// and throw a transition on the current state
			ctx.setApdu(bApdu);
			ctx.getSessionState().onIpduDEToSend(ctx);
			setChanged();
			notifyObservers(m);
		} catch (CBCOMSessionClosedException e) {
			// Too late too send the message ...
			VetoException e2 = new VetoException(
					"Unable to send IPDU-DE : pseudo session is closed");
			evt.addMessage(e2.getMessage());
			throw e2;
			// TODO Is there any further processing we can do with the ISOMsg we
			// can't send ?
		} catch (VetoException e) {
			// if a filter vetoes the message it was not added to the event
			evt.addMessage(e.getMessage());
			throw e;
		} catch (ISOException e) {
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

		clone.logCBCOM = this.logCBCOM;
		clone.ipduMgmtTaskExecutorProvider = this.ipduMgmtTaskExecutorProvider;

		try {
			clone.ctx = (PseudoSessionContext) ctx.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

		return clone;
	}

}
