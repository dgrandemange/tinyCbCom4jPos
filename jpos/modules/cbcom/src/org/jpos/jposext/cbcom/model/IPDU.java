package org.jpos.jposext.cbcom.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * IPDU contents
 * 
 * @author dgrandemange
 * 
 */
public class IPDU {
	/**
	 * Ipdu type
	 */
	private IPDUEnum ipduType;

	/**
	 * List of effective PIs
	 */
	private Map<PIEnum, PI> mapPis;

	/**
	 * APDU
	 */
	byte[] apdu;

	/**
	 * APDU length
	 */
	int apduLength;


	public IPDU(IPDUEnum ipduType, Map<PIEnum, PI> mapPis, byte[] apdu,
			int apduLength) {
		super();
		this.ipduType = ipduType;
		this.mapPis = new TreeMap<PIEnum, PI>(mapPis);
		this.apdu = apdu;
		this.apduLength = apduLength;
	}

	public IPDU(IPDUEnum ipduType, PI[] tabPis, byte[] apdu, int apduLength) {
		super();
		this.ipduType = ipduType;
		
		this.mapPis = new TreeMap<PIEnum, PI>();
		
		for (PI pi : tabPis) {
			this.mapPis.put(pi.getPiEnum(), pi);
		}
		this.apdu = apdu;
		this.apduLength = apduLength;
	}

	public IPDUEnum getIpduType() {
		return ipduType;
	}

	public byte[] getApdu() {
		return apdu;
	}

	public int getApduLength() {
		return apduLength;
	}

	public PI findPiByPIEnum(PIEnum piEnum) {
		return mapPis.get(piEnum);
	}

	public Iterator<PI> getPIIterator() {
		return mapPis.values().iterator();
	}

	public List<PI> getPIList() {
		List<PI> res = new ArrayList<PI>(mapPis.values());
		return res;
	}

	public byte[] toBytes() {
		byte[] res = null;

		try {
			ByteArrayOutputStream bosPis = new ByteArrayOutputStream();
			ByteArrayOutputStream bosRes = new ByteArrayOutputStream();

			int lgi = 0;

			for (PI pi : mapPis.values()) {
				byte[] bPi = pi.toBytes();
				lgi += bPi.length;
				bosPis.write(bPi);
			}
			bosPis.flush();

			int ipduTotalLength = 2 + lgi + apduLength;
			byte[] bIpduTotalLength = new byte[4];
			for (int i = 3; i >= 0; i--) {
				bIpduTotalLength[i] = (byte) (ipduTotalLength & 0xFF);
				ipduTotalLength >>= 8;
			}

			bosRes.write(bIpduTotalLength);
			bosRes.write(ipduType.getIpdu());
			bosRes.write((byte) (lgi & 0xFF));
			bosRes.write(bosPis.toByteArray());
			if (null != apdu) {
				bosRes.write(apdu);
			}
			bosRes.flush();
			res = bosRes.toByteArray();
		} catch (IOException e) {
			// Should not occur
			e.printStackTrace();
		}
		return res;
	}
}
