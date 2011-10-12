package org.obm.push;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestUtils {

	public static Document getXml(String data) throws SAXException, IOException, FactoryConfigurationError{
		return DOMUtils.parse(new ByteArrayInputStream(data.getBytes()));
	}
	
}
