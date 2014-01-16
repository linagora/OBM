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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.technicallog.jaxb.EndOfStreamException;
import org.obm.push.technicallog.jaxb.XMLInputStreamSplitter;
import org.obm.push.technicallog.jaxb.XMLSplitterIterator;

public class XMLSplitterIteratorTest {
	
	@Test
	public void testConstructorNullArgument() {
		XMLSplitterIterator enumeration = new XMLSplitterIterator(null);
		assertThat(enumeration.hasNext()).isEqualTo(false);
	}
	
	@Test
	public void testNoElement() throws Exception {
		XMLInputStreamSplitter splitter = createStrictMock(XMLInputStreamSplitter.class);
		expect(splitter.readNextXML())
			.andThrow(new EndOfStreamException())
			.once();
		
		replay(splitter);
		
		XMLSplitterIterator enumeration = new XMLSplitterIterator(splitter);
		boolean hasNext = enumeration.hasNext();
		assertThat(hasNext).isEqualTo(false);
		
		verify(splitter);
	}
	
	@Test
	public void testOneElement() throws Exception {
		String expectedValue = "next";
		XMLInputStreamSplitter splitter = createStrictMock(XMLInputStreamSplitter.class);
		expect(splitter.readNextXML())
			.andReturn(expectedValue)
			.once();
		expect(splitter.readNextXML())
			.andThrow(new EndOfStreamException())
			.once();
		
		replay(splitter);
		
		XMLSplitterIterator enumeration = new XMLSplitterIterator(splitter);
		boolean hasNext = enumeration.hasNext();
		String value = enumeration.next();
		
		assertThat(hasNext).isEqualTo(true);
		assertThat(value).isEqualTo(expectedValue);
		
		hasNext = enumeration.hasNext();
		assertThat(hasNext).isEqualTo(false);
		
		verify(splitter);
	}
	
	@Test
	public void testTwoElements() throws Exception {
		String expectedFirstValue = "first";
		XMLInputStreamSplitter splitter = createStrictMock(XMLInputStreamSplitter.class);
		expect(splitter.readNextXML())
			.andReturn(expectedFirstValue)
			.once();
		String expectedSecondValue = "second";
		expect(splitter.readNextXML())
			.andReturn(expectedSecondValue)
			.once();
		expect(splitter.readNextXML())
			.andThrow(new EndOfStreamException())
			.once();
		
		replay(splitter);
		
		XMLSplitterIterator enumeration = new XMLSplitterIterator(splitter);
		boolean hasNext = enumeration.hasNext();
		assertThat(hasNext).isEqualTo(true);
		String value = enumeration.next();
		assertThat(value).isEqualTo(expectedFirstValue);
		
		hasNext = enumeration.hasNext();
		assertThat(hasNext).isEqualTo(true);
		value = enumeration.next();
		assertThat(value).isEqualTo(expectedSecondValue);
		
		hasNext = enumeration.hasNext();
		assertThat(hasNext).isEqualTo(false);
		
		verify(splitter);
	}
}
