package org.jpos.iso;

import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;

/**
 * Packager pour une structure TLV en Ascii, les données sont de l'ASCII, le type est du décimal en ascii sur 2 octets, et la longueur est de l'ascii sur 2
 * octets.
 * 
 * @author keyser_pa, dgrandemange
 * 
 */
public class ISOAsciiTLVSubFieldPackager extends ISOAbstractTLVSubFieldPackager
{
	@Override
	protected int unpack(ISOComponent m, byte b[], int consumed) throws ISOException
	{
		String decString = new String(b, consumed, 2);
		
		int fieldNo = -1;
		try
		{
			fieldNo = Integer.parseInt(decString, 10);
		}
		catch (NumberFormatException nfe)
		{
			throw new ISOException(nfe.getMessage(), nfe);
		}
		ISOField field = new ISOField(fieldNo);

		String slen = new String(b, consumed + 2, 2);
		int len = Integer.parseInt(slen);

		field.setValue(new String(b, consumed + 4, len));

		m.set(field);
		return len + 4;
	}

	@Override
	protected byte[] fieldPack(ISOField field) throws ISOException
	{
		String value = getFieldValue(field);
		int len = value.length();

		String subField = String.format("%02d", (Integer) field.getKey());
		byte fieldTag[] = subField.getBytes();

		String slen = String.format("%02d", len);
		byte fieldLen[] = slen.getBytes();

		byte b[] = new byte[len + 4];
		System.arraycopy(fieldTag, 0, b, 0, fieldTag.length);
		System.arraycopy(fieldLen, 0, b, 2, fieldLen.length);
		System.arraycopy(value.getBytes(), 0, b, 4, len);

		return b;
	}
	
	@Override
	protected byte[] binaryFieldPack(ISOBinaryField field) throws ISOException {
		byte[] value = field.getBytes();
		
		int len = value.length;

		String subField = String.format("%02d", (Integer) field.getKey());
		byte fieldTag[] = subField.getBytes();

		String slen = String.format("%02d", len);
		byte fieldLen[] = slen.getBytes();

		byte b[] = new byte[len + 4];
		System.arraycopy(fieldTag, 0, b, 0, fieldTag.length);
		System.arraycopy(fieldLen, 0, b, 2, fieldLen.length);
		System.arraycopy(value, 0, b, 4, len);

		return b;
	}

	@Override
	protected byte[] msgFieldPack(ISOMsg field) throws ISOException {
		return field.pack();
	}		
}