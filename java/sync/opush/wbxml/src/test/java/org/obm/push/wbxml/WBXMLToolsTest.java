/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.wbxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.parsers.FactoryConfigurationError;

import org.custommonkey.xmlunit.XMLAssert;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;


public class WBXMLToolsTest {
	
	private WBXMLTools wbxmlTools;

	@Before
	public void setUp() {
		wbxmlTools = new WBXMLTools();
	}
	
	@Test
	public void testWbxmlDecodeEncodeRoundtrip() throws IOException, SAXException, FactoryConfigurationError, WBXmlException{
		
		String expectedResult = 
				"<?xml version=\"1.0\"?>" +
				"<Sync>" +
					"<Collections>" +
						"<Collection>" +
							"<Class>Email</Class>" +
							"<SyncKey>" +
								"c488f7b0-0602-4910-82ab-45a6a117e66c" +
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
		
		String xmlActiveSync = 
				"<?xml version=\"1.0\"?>" +
				"<Sync>   " +
					"<Collections>" +
						"<Collection>  \r\n" +
							"<Class>Email</Class>" +
							"<SyncKey>" +
								"c488f7b0-0602-4910-82ab-45a6a117e66c" +
							"</SyncKey>" +
							"<CollectionId>1169</CollectionId>" +
							"<DeletesAsMoves/>" +
							"<GetChanges/>" +
							"<WindowSize>5</WindowSize>" +
							"<Options>" +
								"<FilterType>5</FilterType> " +
							"</Options>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		ByteArrayInputStream is = new ByteArrayInputStream(xmlActiveSync.getBytes("UTF-8"));		
		Document doc = DOMUtils.parse(is);
		byte[] byteDoc = wbxmlTools.toWbxml("AirSync", doc);
		Document actual = wbxmlTools.toXml(byteDoc);
		XMLAssert.assertXMLEqual(DOMUtils.parse(expectedResult), actual);
	}
	
	@Test
	public void testToWbxmlWithAccents() throws IOException, SAXException, FactoryConfigurationError, WBXmlException{
		String expectedString = "éàâè";
		
		String xmlActiveSync = 
				"<?xml version=\"1.1\"?>" +
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
	
	@Test
	public void cDataSectionWithIllegalCharUsingXML10DoesNotFail() throws Exception{
		Charset usedCharset = Charsets.UTF_8;
		String dataWithInvalidXML10Chars = "text&#12; and <> illegal &#1;chars";
		String xml =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<Sync " +
			"xmlns:Email=\"Email\" " +
			"xmlns:AirSyncBase=\"AirSyncBase\" " +
			"attributeFormDefault=\"unqualified\" " +
			"elementFormDefault=\"qualified\" " +
			"targetNamespace=\"Email:\" " +
			">" +
				"<Collections>" +
					"<Collection>" +
						"<SyncKey>5b22b86c-6195-48af-b2a9-1ba9af2b6141</SyncKey>" +
						"<CollectionId>1528</CollectionId>" +
						"<Status>1</Status>" +
						"<Responses>" +
							"<Fetch>" +
								"<ServerId>1528:57965</ServerId>" +
								"<Status>1</Status>" +
								"<ApplicationData>" +
									"<Email:To>to@domain.com</Email:To>" +
									"<Email:From>from@domain.com</Email:From>" +
									"<Email:Subject>Subject</Email:Subject>" +
									"<Email:DateReceived>2012-07-24T13:01:48.000Z</Email:DateReceived>" +
									"<Email:Importance>1</Email:Importance>" +
									"<Email:Read>1</Email:Read>" +
									"<AirSyncBase:Body>" +
//										"<AirSyncBase:Data>replaced below data</AirSyncBase:Data>" +
										"<AirSyncBase:Type>1</AirSyncBase:Type>" +
										"<AirSyncBase:Truncated>1</AirSyncBase:Truncated>" +
										"<AirSyncBase:EstimatedDataSize>32768</AirSyncBase:EstimatedDataSize>" +
									"</AirSyncBase:Body>" +
									"<Email:MessageClass>IPM.Note</Email:MessageClass>" +
									"<Email:ContentClass>urn:content-classes:message</Email:ContentClass>" +
									"<Email:InternetCPID>65001</Email:InternetCPID>" +
									"<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>" +
								"</ApplicationData>" +
							"</Fetch>" +
						"</Responses>" + 
					"</Collection>" +
				"</Collections>" +
			"</Sync>";
		InputStream dataInputStream = new ByteArrayInputStream(dataWithInvalidXML10Chars.getBytes(usedCharset));
		
		Document doc = DOMUtils.parse(xml);
		Element docBody = DOMUtils.getUniqueElement(doc.getDocumentElement(), "AirSyncBase:Body");
		DOMUtils.createElementAndCDataText(docBody, "AirSyncBase:Data", dataInputStream, usedCharset);
		wbxmlTools.toWbxml("AirSync", doc);
	}
	
	@Test
	public void testIllegalCharInCDATASection() throws Exception {
		Charset usedCharset = Charsets.UTF_8;
		String invalidCDATAChars = "illegal cdata "+ (char) 0x92 +" char";
		String xml =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<Sync " +
				"xmlns:Email=\"Email\" " +
				"xmlns:AirSyncBase=\"AirSyncBase\" " +
				"attributeFormDefault=\"unqualified\" " +
				"elementFormDefault=\"qualified\" " +
				"targetNamespace=\"Email:\" " +
				">" +
					"<Collections>" +
						"<Collection>" +
							"<SyncKey>5b22b86c-6195-48af-b2a9-1ba9af2b6141</SyncKey>" +
							"<CollectionId>1528</CollectionId>" +
							"<Commands>" +
								"<Add>" +
									"<ServerId>1544:6514</ServerId>" +
									"<ApplicationData>" +
										"<Email:To>\"'Some body'\" &lt;some.body@thilaire.lng.org&gt;</Email:To>" +
										"<Email:From>\"Any body\" &lt;any.body@thilaire.lng.org&gt; </Email:From>" +
										"<Email:Subject>RE: my subject</Email:Subject>" +
										"<Email:DateReceived>2012-07-23T06:41:18.000Z</Email:DateReceived>" +
										"<Email:DisplayTo>\"'Some body'\" &lt;some.body@thilaire.lng.org&gt;</Email:DisplayTo>" +
										"<Email:Importance>1</Email:Importance>" +
										"<Email:Read>1</Email:Read>" +
										"<AirSyncBase:Body>" +
//											"<AirSyncBase:Data>replaced below data</AirSyncBase:Data>" 
											"<AirSyncBase:Type>2</AirSyncBase:Type>" +
											"<AirSyncBase:Truncated>1</AirSyncBase:Truncated>" +
											"<AirSyncBase:EstimatedDataSize>3485</AirSyncBase:EstimatedDataSize>" +
										"</AirSyncBase:Body>" +
										"<Email:MessageClass>IPM.Note</Email:MessageClass>" +
										"<Email:ContentClass>urn:content-classes:message</Email:ContentClass>" +
										"<Email:InternetCPID>65001</Email:InternetCPID>" +
										"<AirSyncBase:NativeBodyType>2</AirSyncBase:NativeBodyType>" +
									"</ApplicationData>" +
								"</Add>" +
							"</Commands>" +
						"</Collection>" +
					"</Collections>" +
				"</Sync>";
		
		InputStream dataInputStream = new ByteArrayInputStream(invalidCDATAChars.getBytes(usedCharset));
		
		Document doc = DOMUtils.parse(xml);
		Element docBody = DOMUtils.getUniqueElement(doc.getDocumentElement(), "AirSyncBase:Body");
		DOMUtils.createElementAndCDataText(docBody, "AirSyncBase:Data", dataInputStream, usedCharset);
		wbxmlTools.toWbxml("AirSync", doc);
	}
}
