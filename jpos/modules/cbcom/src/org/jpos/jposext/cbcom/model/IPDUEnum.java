package org.jpos.jposext.cbcom.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * IPDU enumeration along their respective PIs
 * 
 * @author dgrandemange
 * 
 */
public enum IPDUEnum {

	CN((byte) 0xC2, "connection request", new IPDUPI[] {
			createIPDUPI(PIEnum.PI25, false), createIPDUPI(PIEnum.PI04, false),
			createIPDUPI(PIEnum.PI05, true), createIPDUPI(PIEnum.PI06, false),
			createIPDUPI(PIEnum.PI08, false), createIPDUPI(PIEnum.PI15, false),
			createIPDUPI(PIEnum.PI16, false), createIPDUPI(PIEnum.PI17, false),
			createIPDUPI(PIEnum.PI18, false) }, false),

	AC((byte) 0xC3, "connection acknowledge",
			new IPDUPI[] { createIPDUPI(PIEnum.PI01, true),
					createIPDUPI(PIEnum.PI06, false),
					createIPDUPI(PIEnum.PI08, false),
					createIPDUPI(PIEnum.PI16, false),
					createIPDUPI(PIEnum.PI17, false),
					createIPDUPI(PIEnum.PI18, false) }, false),

	DE((byte) 0xC1, "data transfer", new IPDUPI[] {
			createIPDUPI(PIEnum.PI01, false), createIPDUPI(PIEnum.PI07, true),
			createIPDUPI(PIEnum.PI16, false), createIPDUPI(PIEnum.PI17, false),
			createIPDUPI(PIEnum.PI18, false) }, true),

	AB((byte) 0xC4, "logout/abort", new IPDUPI[] { createIPDUPI(
			PIEnum.PI01, true) }, false);

	// TODO Cas IPDU-DE de réponse, le PI01 est requis => comment modéliser ça
	// proprement ?

	/**
	 * Byte representation of IPDU
	 */
	byte ipdu;

	/**
	 * IPDU description
	 */
	private String description;

	/**
	 * Associated PI along presence indicator
	 */
	private Map<PIEnum, IPDUPI> mapPis;

	/**
	 * Indicates if an APDU is required
	 */
	private boolean apduRequired;

	IPDUEnum(byte ipdu, String description, IPDUPI[] tabIpduPis,
			boolean apduRequired) {
		this.description = description;
		this.ipdu = ipdu;
		this.apduRequired = apduRequired;
		this.mapPis = new HashMap<PIEnum, IPDUPI>();
		for (IPDUPI ipduPi : tabIpduPis) {
			mapPis.put(ipduPi.getPi().getPiEnum(), ipduPi);
		}
	}

	protected static IPDUPI createIPDUPI(PIEnum piEnum, boolean mandatory) {
		return new IPDUPI(new PI(piEnum), mandatory);
	}

	public byte getIpdu() {
		return ipdu;
	}

	public String getDescription() {
		return description;
	}

	public boolean isApduRequired() {
		return apduRequired;
	}

	public IPDUPI getIpduPi(PIEnum piEnum) {
		return mapPis.get(piEnum);
	}

	public Iterator<IPDUPI> getIPDUPIsIterator() {
		return mapPis.values().iterator();
	}

}
