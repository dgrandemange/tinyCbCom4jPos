/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.simulator.cbcom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.XMLPackager;
import org.jpos.q2.iso.ChannelAdaptor;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import bsh.BshClassManager;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.UtilEvalError;

public class TestRunner extends org.jpos.q2.QBeanSupport implements Runnable {
	ChannelAdaptor channelAdaptor;
	ISOPackager packager;
	Interpreter bsh;
	public static final long TIMEOUT = 60000;
	static Integer sessionCount;

	public TestRunner() {
		super();
	}

	protected void initService() throws ISOException {
		packager = new XMLPackager();
	}

	protected void startService() {
		sessionCount = new Integer(0);
		// int nbSessions = cfg.getInt("sessions", 1);
		int nbSessions = 1;
		for (int i = 0; i < nbSessions; i++)
			new Thread(this).start();
	}

	public void run() {
		int currentSessionNumber;
		synchronized (sessionCount) {
			currentSessionNumber = sessionCount;
			sessionCount++;
		}
		try {
			Interpreter bsh = initBSH();
			bsh.set("sessionNumber", currentSessionNumber);
			channelAdaptor = (ChannelAdaptor) NameRegistrar.get(cfg
					.get("channel"));
			List suite = initSuite(getPersist().getChild("test-suite"));
			runSuite(suite, channelAdaptor, bsh);
		} catch (NameRegistrar.NotFoundException e) {
			LogEvent evt = getLog().createError();
			evt.addMessage(e);
			evt.addMessage(NameRegistrar.getInstance());
			Logger.log(evt);
		} catch (Throwable t) {
			getLog().error(t);
		}
	}

	private void runSuite(List suite, ChannelAdaptor channelAdaptor,
			Interpreter bsh) throws ISOException, EvalError {
		LogEvent evt = getLog().createLogEvent("results");
		Iterator iter = suite.iterator();
		long start = System.currentTimeMillis();
		long serverTime = 0;
		while (iter.hasNext()) {
			TestCase tc = (TestCase) iter.next();
			ISOMsg m = (ISOMsg) tc.getRequest().clone();
			if (tc.getPreEvaluationScript() != null) {
				bsh.set("testcase", tc);
				bsh.set("request", m);
				bsh.eval(tc.getPreEvaluationScript());
			}
			tc.setExpandedRequest(applyRequestProps(m, bsh));
			tc.start();
			if (channelAdaptor.isConnected()) {
				channelAdaptor.send(m);
				tc.setResponse(channelAdaptor.receive(tc.getTimeout()));
				tc.end();
				assertResponse(tc, bsh);
				evt.addMessage(tc.toString());
			} else {
				tc.setResultCode(TestCase.CHANNEL_NOT_CONNECTED);
				tc.end();
				evt.addMessage(tc.toString());
			}
			serverTime += tc.elapsed();
			if (!tc.ok()) {
				getLog().error(tc);
				if (!tc.isContinueOnErrors())
					break;
			}
		}
		long end = System.currentTimeMillis();

		long simulatorTime = end - start - serverTime;
		long total = end - start;

		evt.addMessage("elapsed server=" + serverTime + "ms("
				+ percentage(serverTime, total) + "%)" + ", simulator="
				+ simulatorTime + "ms(" + percentage(simulatorTime, total)
				+ "%)" + ", total=" + total + "ms");
		ISOUtil.sleep(100); // let the channel do its logging first
		Logger.log(evt);
	}

	private List initSuite(Element suite) throws IOException, ISOException {
		List l = new ArrayList();
		String prefix = suite.getChildTextTrim("path");
		Iterator iter = suite.getChildren("test").iterator();
		while (iter.hasNext()) {
			Element e = (Element) iter.next();
			boolean cont = "yes".equals(e.getAttributeValue("continue"));
			String s = e.getAttributeValue("count");
			int count = s != null ? Integer.parseInt(s) : 1;
			String path = e.getAttributeValue("file");
			String name = e.getAttributeValue("name");
			if (name == null)
				name = path;

			for (int i = 0; i < count; i++) {
				TestCase tc = new TestCase(name);
				tc.setContinueOnErrors(cont);
				tc.setRequest(getMessage(prefix + path + "_s"));
				tc.setExpectedResponse(getMessage(prefix + path + "_r"));
				tc.setPreEvaluationScript(e.getChildTextTrim("init"));
				tc.setPostEvaluationScript(e.getChildTextTrim("post"));

				String to = e.getAttributeValue("timeout");
				if (to != null)
					tc.setTimeout(Long.parseLong(to));
				else
					tc.setTimeout(cfg.getLong("timeout", TIMEOUT));
				l.add(tc);
			}
		}
		return l;
	}

	private ISOMsg getMessage(String filename) throws IOException, ISOException {
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		try {
			byte[] b = new byte[fis.available()];
			fis.read(b);
			ISOMsg m = new ISOMsg();
			m.setPackager(packager);
			m.unpack(b);
			return m;
		} finally {
			fis.close();
		}
	}

	private boolean processResponse(ISOMsg er, ISOMsg m, ISOMsg expected,
			Interpreter bsh) throws ISOException, EvalError {
		int maxField = m.getMaxField();
		for (int i = 0; i <= maxField; i++) {
			if (expected.hasField(i)) {
				ISOComponent c = expected.getComponent(i);
				if (c instanceof ISOField) {
					String value = expected.getString(i);
					if (value.charAt(0) == '!' && m.hasField(i)
							&& value.length() > 1) {
						bsh.set("value", m.getString(i));
						Object ret = bsh.eval(value.substring(1));
						if (ret instanceof Boolean)
							if (!((Boolean) ret).booleanValue())
								return false;
						expected.set(i, String.valueOf(ret));
					} else if (value.startsWith("*M")) {
						if (m.hasField(i)) {
							expected.unset(i);
							m.unset(i);
						} else {
							return false;
						}
					} else if (value.startsWith("*E")) {
						if (m.hasField(i) && er.hasField(i)) {
							expected.set(i, er.getString(i));
						} else {
							return false;
						}
					} else if (value.startsWith("*O")) {
						if (m.hasField(i) && er.hasField(i)) {
							expected.set(i, er.getString(i));
						}
					}
				} else if (c instanceof ISOMsg) {
					ISOMsg rc = (ISOMsg) m.getComponent(i);
					if (rc instanceof ISOMsg) {
						processResponse(er, rc, (ISOMsg) c, bsh);
					}
				}
			} else {
				m.unset(i);
			}
		}
		return true;
	}

	private boolean assertResponse(TestCase tc, Interpreter bsh)
			throws ISOException, EvalError {
		if (tc.getResponse() == null) {
			tc.setResultCode(TestCase.TIMEOUT);
			return false;
		}
		ISOMsg e = (ISOMsg) tc.getExpandedRequest().clone();
		ISOMsg c = (ISOMsg) tc.getResponse().clone();
		ISOMsg expected = (ISOMsg) tc.getExpectedResponse().clone();
		c.setHeader((ISOHeader) null);
		if (!processResponse(e, c, expected, bsh)) {
			tc.setResultCode(TestCase.FAILURE);
			return false;
		}
		ISOPackager p = new XMLPackager();
		expected.setPackager(p);
		c.setPackager(p);

		if (tc.getPostEvaluationScript() != null) {
			bsh.set("testcase", tc);
			bsh.set("response", tc.getResponse());
			Object ret = bsh.eval(tc.getPostEvaluationScript());
			if (ret instanceof Boolean) {
				if (!((Boolean) ret).booleanValue()) {
					tc.setResultCode(TestCase.FAILURE);
					return false;
				}
			}
		}
		if (!(new String(c.pack())).equals(new String(expected.pack()))) {
			tc.setResultCode(TestCase.FAILURE);
			return false;
		}
		tc.setResultCode(TestCase.OK);
		return true;
	}

	private void eval(Element e, String name, Interpreter bsh) throws EvalError {
		Element ee = e.getChild(name);
		if (ee != null)
			bsh.eval(ee.getText());
	}

	private Interpreter initBSH() throws UtilEvalError, EvalError {
		Interpreter bsh = new Interpreter();
		BshClassManager bcm = bsh.getClassManager();
		bcm.setClassPath(getServer().getLoader().getURLs());
		bcm.setClassLoader(getServer().getLoader());
		bsh.set("qbean", this);
		bsh.set("log", getLog());
		bsh.eval(getPersist().getChildTextTrim("init"));
		return bsh;
	}

	private ISOMsg applyRequestProps(ISOMsg m, Interpreter bsh)
			throws ISOException, EvalError {
		int maxField = m.getMaxField();
		for (int i = 0; i <= maxField; i++) {
			if (m.hasField(i)) {
				ISOComponent c = m.getComponent(i);
				if (c instanceof ISOMsg) {
					applyRequestProps((ISOMsg) c, bsh);
				} else if (c instanceof ISOField) {
					String value = (String) c.getValue();
					try {
						switch (value.charAt(0)) {
						case '!':
							m.set(i, bsh.eval(value.substring(1)).toString());
							break;
						case '@':
							m.set(i, ISOUtil.hex2byte(bsh.eval(
									value.substring(1)).toString()));
							break;
						}
					} catch (NullPointerException e) {
						m.unset(i);
					}
				}
			}
		}
		return m;
	}

	private long percentage(long a, long b) {
		double d = (double) a / b;
		return (long) (d * 100.00);
	}
}
