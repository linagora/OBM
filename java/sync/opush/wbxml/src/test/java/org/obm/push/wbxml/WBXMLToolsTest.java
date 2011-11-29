package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class WBXMLToolsTest {
	
	private WBXMLTools wbxmlTools;

	@Before
	public void setUp() {
		wbxmlTools = new WBXMLTools();
	}
	
	@Test
	public void testToWbxmlWithAccents() throws IOException, SAXException, FactoryConfigurationError, WBXmlException{
		String expectedString = "éàâè";
		
		String xmlActiveSync = 
				"<?xml version=\"1.0\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<Class>Email</Class>" +
							"<SyncKey>" +
								"c488f7b0-0602-4910-82ab-45a6a117e66c" +
								expectedString +
							"</SyncKey>" +
							"<CollectionId>1169</CollectionId>" +
							"<DeletesAsMoves/>" +
							"<GetChanges/>" +
							"<WindowSize>5</WindowSize>" +
							"<Options>" +
								"<FilterType>5</FilterType>" +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		ByteArrayInputStream is = new ByteArrayInputStream(xmlActiveSync.getBytes("UTF-8"));		
		Document doc = DOMUtils.parse(is);
		
		byte[] byteDoc = wbxmlTools.toWbxml("AirSync", doc);

		Assert.assertThat(new String(byteDoc), 
				StringContains.containsString(expectedString));
		
	}
}
