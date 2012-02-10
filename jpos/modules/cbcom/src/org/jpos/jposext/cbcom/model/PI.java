package org.jpos.jposext.cbcom.model;



/**
 * Describes a PI in an IPDU context
 * 
 * @author dgrandemange
 *
 */
public class PI {

	/**
	 * PI element in PI enumeration
	 */
	private PIEnum piEnum;
	
	private int paramLen;
	
	private byte[] paramValue;
	
	public PI(PIEnum piEnum) {
		this.piEnum = piEnum;
	}

	public PI(PIEnum piEnum, byte[] paramValue) {
		this.piEnum = piEnum;
		this.paramValue = paramValue;
		this.paramLen = paramValue.length;
	}	
	
	public PIEnum getPiEnum() {
		return piEnum;
	}

	public int getParamLen() {
		return paramLen;
	}

	public void setParamLen(int paramLen) {
		this.paramLen = paramLen;
	}

	public byte[] getParamValue() {
		return paramValue;
	}

	public void setParamValue(byte[] paramValue) {
		this.paramValue = paramValue;
	}

	public byte[] toBytes() {
		byte[] res = new byte[2+paramLen];
		res[0]=piEnum.getPi();
		res[1]=(byte) (paramLen & 0xFF);
		System.arraycopy(paramValue, 0, res, 2, paramLen);
		return res;
	}
	
}
