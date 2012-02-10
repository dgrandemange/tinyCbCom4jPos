package org.jpos.jposext.cbcom;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Comme JPOS est plutôt fermé dans sa gestion de rséolution des entités (DTD)
 * externes, je propose ici une classe surchargeant GenericPackager permettant
 * une résolution des entités publiques.<BR/>
 * Le mapping se fait au niveau de la méthode initEntityResolutionMap(), qui
 * peut être complétée au besoin.<BR/>
 * 
 * @author dgrandemange
 * 
 */
public class GenericPackagerEntityResolverAble extends GenericPackager {

	private final static Pattern cpPtrn = Pattern.compile("^classpath:(.*)$");

	private Map<String, String> mapEntityResolutionMap;

	protected void initEntityResolutionMap() {
		if (null == mapEntityResolutionMap) {
			mapEntityResolutionMap = new HashMap<String, String>();

			mapEntityResolutionMap.put("-//JPOS/Generic Packager DTD//EN",
					"classpath:/org/jpos/jposext/cbcom/conf/genericpackager.dtd");
		}
	}

	public GenericPackagerEntityResolverAble(InputStream input)
			throws ISOException {
		super(input);
	}

	public GenericPackagerEntityResolverAble(String filename)
			throws ISOException {
		super(filename);
	}

	public GenericPackagerEntityResolverAble() throws ISOException {
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

	private XMLReader createXMLReader() throws SAXException {
		initEntityResolutionMap();

		XMLReader reader = null;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			reader = XMLReaderFactory.createXMLReader(System.getProperty(
					"org.xml.sax.driver",
					"org.apache.crimson.parser.XMLReaderImpl"));
		}
		reader.setFeature("http://xml.org/sax/features/validation", true);
		GenericContentHandler handler = new GenericContentHandler();
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		reader.setEntityResolver(new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				InputSource res = null;

				if (null != publicId) {
					String relativePath = mapEntityResolutionMap.get(publicId);
					if (null != relativePath) {
						InputStream in;
						Matcher matcher = cpPtrn.matcher(relativePath);
						if (matcher.matches()) {
							String resourcePath = matcher.group(1);
							in = this.getClass().getResourceAsStream(
									resourcePath);
						} else {
							in = new FileInputStream(relativePath);
						}
						res = new InputSource(in);
					}
				}

				return res;
			}

		});
		return reader;
	}

}
