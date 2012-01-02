package org.jpos.iso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Packager pour une structure TLV binaire<BR>
 * Le type est codé binaire sur 2 octets<BR>
 * La longeur est codée binaire sur 1 octet<BR>
 * La valeur est codée binaire sur le nombre d’octets défini par la longueur (le
 * format binaire est implicite pour chaque type)<BR>
 * 
 * @author dgrandemange
 * 
 */
public class ISOBinaryTLVSubFieldPackager extends
		ISOAbstractTLVSubFieldPackager {

	@Override
	protected int unpack(ISOComponent m, byte b[], int consumed)
			throws ISOException {

		int fieldNo = ((((int) b[consumed]) & 0xFF) << 8)
				| (((int) b[consumed + 1]) & 0xFF);

		int len = (int) (b[consumed + 2] & 0XFF);

		byte value[] = new byte[len];

		System.arraycopy(b, consumed + 3, value, 0, len);

		ISOBinaryField field = new ISOBinaryField(fieldNo, value);

		m.set(field);

		return len + 3;
	}

	@Override
	protected byte[] fieldPack(ISOField field) throws ISOException {
		String value = getFieldValue(field);

		int fieldKey = (Integer) field.getKey();
		byte fieldType[] = new byte[] { (byte) ((fieldKey >> 8) & 0xFF),
				(byte) (fieldKey & 0xFF) };

		int len = value.length();
		byte fieldLen[] = new byte[] { (byte) (len & 0xFF) };

		byte b[] = new byte[3 + len];
		System.arraycopy(fieldType, 0, b, 0, 2);
		System.arraycopy(fieldLen, 0, b, 2, 1);
		System.arraycopy(value.getBytes(), 0, b, 3, len);

		return b;
	}

	@Override
	protected byte[] binaryFieldPack(ISOBinaryField field) throws ISOException {
		byte[] value = field.getBytes();

		int fieldKey = (Integer) field.getKey();
		byte fieldType[] = new byte[] { (byte) ((fieldKey >> 8) & 0xFF),
				(byte) (fieldKey & 0xFF) };

		int len = value.length;
		byte fieldLen[] = new byte[] { (byte) (len & 0xFF) };

		byte b[] = new byte[3 + len];
		System.arraycopy(fieldType, 0, b, 0, 2);
		System.arraycopy(fieldLen, 0, b, 2, 1);
		System.arraycopy(value, 0, b, 3, len);

		return b;
	}

	@Override
	protected byte[] msgFieldPack(ISOMsg field) throws ISOException {
		return field.pack();
	}


}