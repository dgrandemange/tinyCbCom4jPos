package org.jpos.iso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jpos.iso.ISOBasePackager;
import org.jpos.iso.ISOComponent;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOField;

/**
 * Un packager abstrait pour les champs au structure TLV
 * 
 * @author keyser_pa
 * 
 */
public abstract class ISOAbstractTLVSubFieldPackager extends ISOBasePackager
{
	/**
	 * Un Comparator d'ISOComponent à peine masque qui utilise le numero de champs, dans l'ordre croissant
	 */
	private final static Comparator<Object> isoComponentComparator = new Comparator<Object>() {

		public int compare(Object o1, Object o2)
		{
			try
			{
				if (o1 instanceof ISOComponent && o2 instanceof ISOComponent)
				{
					int key1 = (Integer) ((ISOComponent) o1).getKey();
					int key2 = (Integer) ((ISOComponent) o2).getKey();

					return key1 - key2;
				}
			}
			catch (ISOException isoe)
			{

			}
			return -1;
		}
	};

	@Override
	protected boolean emitBitMap()
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] pack(ISOComponent c) throws ISOException
	{
		try
		{
			ByteArrayOutputStream str = new ByteArrayOutputStream();

			List<Object> values = new ArrayList<Object>(c.getChildren().values());
			Collections.sort(values, isoComponentComparator);

			for (Object obj : values)
			{
				if (obj instanceof ISOField)
				{
					ISOField field = (ISOField) obj;
					str.write(fieldPack(field));
				}
				else if (obj instanceof ISOBinaryField) {
					ISOBinaryField field = (ISOBinaryField) obj;
					str.write(binaryFieldPack(field));					
				}
				else if (obj instanceof ISOMsg) {
					ISOMsg field = (ISOMsg) obj;
					str.write(msgFieldPack(field));					
				}				
			}

			return str.toByteArray();
		}
		catch (Exception ex)
		{
			throw new ISOException(ex);
		}
	}	

	@Override
	public int unpack(ISOComponent m, byte b[]) throws ISOException
	{
		int consumed = 0;
		while (consumed < b.length)
			consumed += unpack(m, b, consumed);

		return consumed;
	}

	/**
	 * Methode à surcharger pour certains champs (ex le champ 59-0300 CVV)
	 * 
	 * @param field
	 * @return
	 */
	protected String getFieldValue(ISOField field)
	{
		return (String) field.getValue();
	}
	
	/**
	 * Permet de depacker le sous-champ qui commence à l'index consumed de b[]
	 * 
	 * @param m
	 * @param b
	 * @param consumed
	 * @return le nombre d'octets consomme par le message
	 * @throws ISOException
	 */
	protected abstract int unpack(ISOComponent m, byte b[], int consumed) throws ISOException;

	/**
	 * Package a sub field
	 * 
	 * @param field
	 * @return
	 * @throws ISOException
	 */
	protected abstract byte[] fieldPack(ISOField field) throws ISOException;
	
	/**
	 * Package a binary sub field
	 * 
	 * @param binary field
	 * @return
	 */
	protected abstract byte[] binaryFieldPack(ISOBinaryField field) throws ISOException;
	
	/**
	 * Package a message sub field
	 * 
	 * @param field
	 * @return
	 */
	protected abstract byte[] msgFieldPack(ISOMsg field) throws ISOException;
}
