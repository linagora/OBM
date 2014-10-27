/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
package org.obm.push.mail.transformer;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;


public class HtmlToTextTest {

	private Transformer testee;

	@Before
	public void setup() {
		testee = new HtmlToText.Factory().create(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void transformNullShouldThrowNPE() throws IOException {
		testee.transform(null, Charsets.UTF_8);
	}

	@Test(expected=NullPointerException.class)
	public void transformNullCharsetShouldThrowNPE() throws IOException {
		testee.transform(new ByteArrayInputStream(new byte[] {0x22}), null);
	}
	
	@Test
	public void transformSimpleString() throws IOException {
		ByteArrayInputStream inputStream = stringToInputStream("<html><body>simple string</body></html>");
		InputStream actual = testee.transform(inputStream, Charsets.UTF_8);
		assertThat(actual).hasContentEqualTo(stringToInputStream("simple string"));
	}

	@Test
	public void transformMultilineString() throws IOException {
		ByteArrayInputStream inputStream = stringToInputStream("<html><body>line one<br/>line two</body></html>");
		InputStream actual = testee.transform(inputStream, Charsets.UTF_8);
		assertThat(actual).hasContentEqualTo(stringToInputStream("line one\r\nline two\r\n"));
	}
	
	@Test
	public void transformStringWithEntity() throws IOException {
		ByteArrayInputStream inputStream = stringToInputStream("<html><body>line one<br/> &lt line two</body></html>");
		InputStream actual = testee.transform(inputStream, Charsets.UTF_8);
		assertThat(actual).hasContentEqualTo(stringToInputStream("line one\r\n < line two\r\n"));
	}
	
	@Test
	public void transformTruncatedString() throws IOException {
		String html = "<html>" +
				"  <head>" +
				"    <meta content=\"text/html; charset=utf-8\" http-equiv=\"Content-Type\">" +
				"  </head>" +
				"  <body bgcolor=\"#FFFFFF\" text=\"#000000\">" +
				"    <div class=\"moz-cite-prefix\">Salut David,<br>" +
				"      <br>" +
				"      on a oublié un petit détail. Sur la page d'obm.org <a" +
				"        href=\"http://obm.org/content/supported-mobile-phones\">suivante</a>" +
				"      on a écrit \"starting with OBM 3, iOS5, Windows Mobile &lt; 7 and" +
				"      Android &lt; 3 are no more supported.\"<br>" +
				"      <br>" +
				"      On parle d'OBM 3, pas d'OPush 3, donc:<br>" +
				"      <ul>" +
				"        <li>est-ce que ça veut dire qu'aussi à partir d'OPush 3 on ne" ;

		String text = "Salut David,\r\n \r\n" +
				" on a oublié un petit détail. Sur la page d'obm.org suivante on a écrit \"starting " +
				"with OBM 3, iOS5, Windows Mobile < 7 and Android < 3 are no more supported.\"\r\n \r\n" +
				" On parle d'OBM 3, pas d'OPush 3, donc:\r\n" +
				"  est-ce que ça veut dire qu'aussi à partir d'OPush 3 on ne";
		
		ByteArrayInputStream inputStream = stringToInputStream(html);
		InputStream actual = testee.transform(inputStream, Charsets.UTF_8);
		assertThat(actual).hasContentEqualTo(stringToInputStream(text));
	}
	
	private ByteArrayInputStream stringToInputStream(String content) {
		return new ByteArrayInputStream(content.getBytes(Charsets.UTF_8));
	}
	
}
