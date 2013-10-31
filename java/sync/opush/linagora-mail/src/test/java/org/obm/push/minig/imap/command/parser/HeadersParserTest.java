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
package org.obm.push.minig.imap.command.parser;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.minig.imap.command.parser.HeadersParser;

import com.google.common.collect.Sets;

public class HeadersParserTest {


	private Map<String, String> parseStringAsHeaders(String data) throws IOException {
		return new HeadersParser().parseRawHeaders(new StringReader(data));
	}
	
	@Test
	public void testParsingBasicHeaders() throws IOException {
		String data = "headername: headervalue";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("headername");
		assertThat(headers.values()).containsOnly("headervalue");
	}
	
	@Test
	public void testParsingBasicHeadersKeyCaseInsensitive() throws IOException {
		String data = "headerName: headervalue";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("headername");
		assertThat(headers.values()).containsOnly("headervalue");
	}

	@Test
	public void testParsingBasicHeadersValueCaseSensitive() throws IOException {
		String data = "headername: headerValue";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("headername");
		assertThat(headers.values()).containsOnly("headerValue");
	}
	
	@Test
	public void testParsingHeadersValueContainsColon() throws IOException {
		String data = "headername: header:Value";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("headername");
		assertThat(headers.values()).containsOnly("header:Value");
	}

	@Test
	public void testParsingAllPossibleValueForName() throws IOException {
		StringBuilder sb = new StringBuilder();
		HashSet<Character> excludedChars = Sets.newHashSet(':');
		for (int i = 33; i <= 126; ++i) {
			Character character = Character.valueOf((char) i);
			if (!excludedChars.contains(character)) {
				sb.append(character);
			}
		}
		String headerName = sb.toString();
		String data = headerName + ": headerValue";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly(headerName.toLowerCase());
		assertThat(headers.values()).containsOnly("headerValue");
	}

	
	@Test
	public void testParsingHeadersRFC822Ch311Ex1() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("to");
		assertThat(headers.values()).containsOnly("\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN");
	}
	
	@Test
	public void testParsingHeadersRFC822Ch311Ex2() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\n" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("to");
		assertThat(headers.values()).containsOnly("\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN");
	}
	
	@Test
	public void testParsingHeadersRFC822Ch311WithHTAB() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\n" +
				"\tJJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("to");
		assertThat(headers.values()).containsOnly("\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN");
	}
	
	@Test
	public void testParsingHeadersWithCr() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\r" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("to");
		assertThat(headers.values()).containsOnly("\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN");
	}
	
	@Test
	public void testParsingHeadersWithCrLf() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\r\n" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		assertThat(headers.size()).isEqualTo(1);
		assertThat(headers.keySet()).containsOnly("to");
		assertThat(headers.values()).containsOnly("\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN");
	}
	
	@Test(expected=IllegalStateException.class)
	public void testParsingHeadersWithEmptyLine() throws IOException {
		String data = "		";
		parseStringAsHeaders(data);
	}
	
}
