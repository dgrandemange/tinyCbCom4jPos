package org.jpos.iso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;

/**
 * Packager pour une structure TLV binaire<BR>
 * Le type est codé binaire sur 2 octets<BR>
 * La longeur est codée binaire sur 1 octet<BR>
 * La valeur est codée binaire sur le nombre d’octets défini par la longueur (le
 * format binaire est implicite pour chaque type)<BR>
 * 
 * La notion de type répètable est gérée (non mais quelle aberration ce truc !)
 * 
 * @author dgrandemange
 * 
 */
public class ISOBinaryTLVRepeatableTypeSubFieldPackager extends
		ISOAbstractTLVSubFieldPackager {

	@Override
	protected int unpack(ISOComponent m, byte b[], int consumed)
			throws ISOException {

		int fieldNo = ((((int) b[consumed]) & 0xFF) << 8)
				| (((int) b[consumed + 1]) & 0xFF);

		int len = (int) (b[consumed + 2] & 0XFF);

		byte value[] = new byte[len];

		System.arraycopy(b, consumed + 3, value, 0, len);

		ISOMsg parent = (ISOMsg) m;

		ISOComponent cmp = parent.getComponent(fieldNo);
		ISOMsg msg;

		if (null != cmp) {
			msg = (ISOMsg) cmp;
		} else {
			msg = new ISOMsg();
			msg.setFieldNumber(fieldNo);
		}

		int lastIndex = msg.getMaxField();
		ISOBinaryField field = new ISOBinaryField(++lastIndex, value);
		msg.set(field);

		m.set(msg);

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
		ByteArrayOutputStream bis = new ByteArrayOutputStream();

		int fieldKey = (Integer) field.getKey();
		byte fieldType[] = new byte[] { (byte) ((fieldKey >> 8) & 0xFF),
				(byte) (fieldKey & 0xFF) };
		
		for (int i = 1; i <= field.getMaxField(); i++) {
			ISOBinaryField currSubField = (ISOBinaryField) field
					.getComponent(i);
			byte[] currSubFieldValue = currSubField.getBytes();
			int currSubFieldLen = currSubFieldValue.length;
			byte fieldLen[] = new byte[] { (byte) (currSubFieldLen & 0xFF) };

			bis.write(fieldType, 0, 2);
			bis.write(fieldLen, 0, 1);
			bis.write(currSubFieldValue, 0, currSubFieldLen);
		}
		try {
			bis.flush();
		} catch (IOException e) {
			// Safe to ignore
		}
		return bis.toByteArray();
	}	
}