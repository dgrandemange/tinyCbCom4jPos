package org.jpos.iso;

/*
 * Created on 14 août 07 by dgrandemange
 *
 * Copyright (c) 2005 Setib
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Setib ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Setib.
 */

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.packager.GenericPackager.GenericContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Un packager générique qui surcharge le GenericPackager de JPos en y ajoutant
 * la possibilité d'injecter un EntityResolver
 * 
 * @author dgrandemange
 * 
 */
public class CustomGenericPackager extends GenericPackager {

	/**
	 * Résolveur d'entités qui par défaut associe à un id publique d'entité, la
	 * ressource conf/genericpackager.dtd relative à cette classe
	 */
	private EntityResolver entityResolver = new EntityResolver() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
		 * java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			InputSource is = null;

			if ("genericpackager.dtd".equalsIgnoreCase(publicId)) {
				InputStream dtdResource = this.getClass().getResourceAsStream(
						"conf/genericpackager.dtd");
				is = new InputSource(dtdResource);
			}

			return is;
		}

	};

	public CustomGenericPackager() throws ISOException {
		super();
	}

	public void readFile(String filename) throws ISOException {
		try {
			createXMLReader().parse(filename);
		} catch (Exception e) {
			throw new ISOException(e);
		}
	}

	public void readFile(InputStream input) throws ISOException {
		try {
			createXMLReader().parse(new InputSource(input));
		} catch (Exception e) {
			throw new ISOException(e);
		}
	}

    private XMLReader createXMLReader () throws SAXException {
        XMLReader reader = null;
        try {
            reader = XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            reader = XMLReaderFactory.createXMLReader (
                System.getProperty( 
                    "org.xml.sax.driver", 
                    "org.apache.crimson.parser.XMLReaderImpl"
                )
            );
        }
        reader.setFeature ("http://xml.org/sax/features/validation", true);
        GenericContentHandler handler = new GenericContentHandler();
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        reader.setEntityResolver(entityResolver);
        
        return reader;
    }	
	
	/**
	 * @return the entityResolver
	 */
	public EntityResolver getEntityResolver() {
		return entityResolver;
	}

	/**
	 * @param entityResolver
	 *            the entityResolver to set
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}
}
