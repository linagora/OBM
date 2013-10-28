/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.items;

import javax.xml.parsers.FactoryConfigurationError;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class AbstractItemsParserTest {

	private AbstractItemsParser abstractItemsParser;

	@Before
	public void setUp() {
		abstractItemsParser = new AbstractItemsParser() {};
	}

	@Test
	public void testParseInteger() {
		String docKey = "anInteger";
		Element doc = createElement(docKey, "5");
		
		Integer parsed = abstractItemsParser.i(doc, docKey);
		
		Assertions.assertThat(parsed).isEqualTo(5);
	}
	
	@Test
	public void testParseIntegerWhenKeyDoesntExist() {
		String docKey = "anInteger";
		Element doc = createElement("anotherInteger", "5");

		Integer parsed = abstractItemsParser.i(doc, docKey);
		
		Assertions.assertThat(parsed).isNull();
	}
	
	@Test
	public void testParseIntegerWhenNegative() {
		String docKey = "anInteger";
		Element doc = createElement(docKey, "-5");
		
		Integer parsed = abstractItemsParser.i(doc, docKey);
		
		Assertions.assertThat(parsed).isEqualTo(-5);
	}
	
	@Test(expected=NumberFormatException.class)
	public void testParseIntegerWhenFloat() {
		String docKey = "anInteger";
		Element doc = createElement(docKey, "5.1234");
		
		abstractItemsParser.i(doc, docKey);
	}
	
	@Test
	public void testParseIntegerGiveDefaultValueWhenFails() {
		String docKey = "anInteger";
		Element doc = createElement("anotherInteger", "5");
		Integer defaultValue = 1337;

		Integer parsed = abstractItemsParser.i(doc, docKey, defaultValue);
		
		Assertions.assertThat(parsed).isEqualTo(defaultValue);
	}
	
	@Test
	public void testParseIntegerGiveDefaultValueWhenFailsNull() {
		String docKey = "anInteger";
		Element doc = createElement("anotherInteger", "5");
		Integer defaultValue = null;

		Integer parsed = abstractItemsParser.i(doc, docKey, defaultValue);
		
		Assertions.assertThat(parsed).isNull();
	}
	
	private Element createElement(String elementName, String elementValue) {
		Element rootDocument = createDocument();
		DOMUtils.createElementAndText(rootDocument, elementName, elementValue);
		return rootDocument;
	}

	private Element createDocument() throws FactoryConfigurationError {
		return DOMUtils.createDoc("namespace", "rootElement").getDocumentElement();
	}
}
