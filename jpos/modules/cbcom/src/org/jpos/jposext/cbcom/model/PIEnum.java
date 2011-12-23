package org.jpos.jposext.cbcom.model;

/**
 * PI enumeration
 * 
 * @author dgrandemange
 * 
 */
public enum PIEnum {
	PI01((byte) 0x01, "pseudo-session return code", false), PI04((byte) 0x04,
			"pseudo-session identifer", false), PI05((byte) 0x05,
			"protocol identifier", false), PI06((byte) 0x06, "IPDU max length",
			true), PI07((byte) 0x07, "transported field (APDU) length", false), PI08(
			(byte) 0x08, "data encoding format", false), PI15((byte) 0x0F,
			"total length of data to transfer", false), PI16((byte) 0x10,
			"TNR (non response timer)", true), PI17((byte) 0x11,
			"TSI (iddleness watchout timer)", true), PI18((byte) 0x12,
			"TMA (activity sustain timer)", true), PI25((byte) 0x19,
			"extended peudo-session id", false);

	/**
	 * Byte representation of PI
	 */
	private byte pi;

	/**
	 * PI description
	 */
	private String description;

	/**
	 * Isthe PI negotiable ?
	 */
	private boolean negotiable;

	PIEnum(byte pi, String description, boolean negotiable) {
		this.pi = pi;
		this.description = description;
		this.negotiable = negotiable;
	}

	public String getDescription() {
		return description;
	}

	public boolean isNegotiable() {
		return negotiable;
	}

	public byte getPi() {
		return pi;
	}

}
