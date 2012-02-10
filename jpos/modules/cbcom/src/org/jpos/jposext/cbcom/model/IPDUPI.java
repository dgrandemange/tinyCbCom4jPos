package org.jpos.jposext.cbcom.model;

/**
 * Wraps a PI in an IPDU context
 * 
 * @author dgrandemange
 * 
 */
public class IPDUPI {

	/**
	 * IPDU parameter (PI)
	 */
	private PI pi;

	/**
	 * Indicates if IPDU parameter (PI) is mandatory(true) or not(false)
	 */
	private boolean mandatory;

	public IPDUPI(PI pi, boolean mandatory) {
		this.pi = pi;
		this.mandatory = mandatory;
	}

	public PI getPi() {
		return pi;
	}

	public boolean isMandatory() {
		return mandatory;
	}
}
