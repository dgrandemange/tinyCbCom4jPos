package org.jpos.jposext.cbcom.exception;

public class CBCOMException extends Exception {

	/**
	 * PI01 value associated to the exception<BR>
	 * Typical use is to pass it in an IPDU AB<BR>
	 * Default value is 0x04 
	 */
	private byte pv01 = 0x04;
	
	public CBCOMException() {
		super();
	}

	public CBCOMException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CBCOMException(String arg0) {
		super(arg0);
	}

	public CBCOMException(Throwable arg0) {
		super(arg0);
	}

	public CBCOMException(byte pv01) {		
		super();
		this.pv01 = pv01;
	}

	public CBCOMException(String arg0, Throwable arg1, byte pv01) {
		super(arg0, arg1);
		this.pv01 = pv01;
	}

	public CBCOMException(String arg0, byte pv01) {
		super(arg0);
		this.pv01 = pv01;
	}

	public CBCOMException(Throwable arg0, byte pv01) {
		super(arg0);
		this.pv01 = pv01;
	}
		
}
