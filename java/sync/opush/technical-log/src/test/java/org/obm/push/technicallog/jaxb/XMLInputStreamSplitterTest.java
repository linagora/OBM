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
package org.obm.push.technicallog.jaxb;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.obm.push.technicallog.jaxb.EndOfStreamException;
import org.obm.push.technicallog.jaxb.XMLInputStreamSplitter;

public class XMLInputStreamSplitterTest {
	
	@Test (expected=EndOfStreamException.class)
	public void testEmptyStream() throws Exception {
		String value = new String();
		ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		splitter.readNextXML();
	}
	
	@Test (expected=EndOfStreamException.class)
	public void testNoXMLInStream() throws Exception {
		String value = "noxml";
		ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		splitter.readNextXML();
	}
	
	@Test (expected=EndOfStreamException.class)
	public void testNoClosingRootTagInStream() throws Exception {
		String value = "<root>";
		ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		splitter.readNextXML();
	}
	
	@Test (expected=EndOfStreamException.class)
	public void testBadClosingRootTagInStream() throws Exception {
		String value = "<root></bad>";
		ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		splitter.readNextXML();
	}
	
	@Test
	public void testSimpleRoot() throws Exception {
		String expectedValue = "<root></root>";
		ByteArrayInputStream input = new ByteArrayInputStream(expectedValue.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		String value = splitter.readNextXML();
		
		assertThat(value).isEqualTo(expectedValue);
	}
	
	@Test
	public void testSimpleRootAfterNoneXMLElement() throws Exception {
		String expectedValue = "<root></root>";
		String completeValue = "noneXML" + expectedValue;
		ByteArrayInputStream input = new ByteArrayInputStream(completeValue.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		String value = splitter.readNextXML();
		
		assertThat(value).isEqualTo(expectedValue);
	}
	
	@Test
	public void testComplexeRoot() throws Exception {
		String expectedValue = "<root name=\"myName\"></root>";
		ByteArrayInputStream input = new ByteArrayInputStream(expectedValue.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		String value = splitter.readNextXML();
		
		assertThat(value).isEqualTo(expectedValue);
	}
	
	@Test
	public void testComplexeXML() throws Exception {
		String expectedValue = 
				"<root name=\"myName\">" +
					"<firstLevel>" +
						"<secondLevel param=\"myParam\">" +
							"<thirdLevel>" +
							"</thirdLevel>" +
							"<thirdLevel>" +
							"</thirdLevel>" +
						"</secondLevel>" +
					"</firstLevel>" +
					"<firstLevel>" +
					"<secondLevel param=\"mySecondParam\">" +
						"<thirdLevel>" +
						"</thirdLevel>" +
					"</secondLevel>" +
					"</firstLevel>" +
				"</root>";
		ByteArrayInputStream input = new ByteArrayInputStream(expectedValue.getBytes());
		
		XMLInputStreamSplitter splitter = new XMLInputStreamSplitter(input);
		String value = splitter.readNextXML();
		
		assertThat(value).isEqualTo(expectedValue);
	}
}
