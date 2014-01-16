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
package org.obm.push.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class MSEmailBodyTypeTest {

	@Test
	public void testPlainText() {
		MSEmailBodyType plainText = MSEmailBodyType.getValueOf(1);
		assertThat(plainText).isEqualTo(MSEmailBodyType.PlainText);
		assertThat(plainText.asXmlValue()).isEqualTo(1);
		assertThat(plainText.getMimeType()).isEqualTo("text/plain");
	}
	
	@Test
	public void testPlainTextFromMimeType() {
		MSEmailBodyType plainText = MSEmailBodyType.fromMimeType("text/plain");
		assertThat(plainText).isEqualTo(MSEmailBodyType.PlainText);
		assertThat(plainText.asXmlValue()).isEqualTo(1);
		assertThat(plainText.getMimeType()).isEqualTo("text/plain");
	}
	
	@Test
	public void testHTML() {
		MSEmailBodyType html = MSEmailBodyType.getValueOf(2);
		assertThat(html).isEqualTo(MSEmailBodyType.HTML);
		assertThat(html.asXmlValue()).isEqualTo(2);
		assertThat(html.getMimeType()).isEqualTo("text/html");
	}
	
	@Test
	public void testHTMLFromMimeType() {
		MSEmailBodyType html = MSEmailBodyType.fromMimeType("text/html");
		assertThat(html).isEqualTo(MSEmailBodyType.HTML);
		assertThat(html.asXmlValue()).isEqualTo(2);
		assertThat(html.getMimeType()).isEqualTo("text/html");
	}
	
	@Test
	public void testRTF() {
		MSEmailBodyType rtf = MSEmailBodyType.getValueOf(3);
		assertThat(rtf).isEqualTo(MSEmailBodyType.RTF);
		assertThat(rtf.asXmlValue()).isEqualTo(3);
		assertThat(rtf.getMimeType()).isEqualTo("text/rtf");
	}
	
	@Test
	public void testRTFFromMimeType() {
		MSEmailBodyType rtf = MSEmailBodyType.fromMimeType("text/rtf");
		assertThat(rtf).isEqualTo(MSEmailBodyType.RTF);
		assertThat(rtf.asXmlValue()).isEqualTo(3);
		assertThat(rtf.getMimeType()).isEqualTo("text/rtf");
	}
	
	@Test
	public void testMime() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(4);
		assertThat(mime).isEqualTo(MSEmailBodyType.MIME);
		assertThat(mime.asXmlValue()).isEqualTo(4);
		assertThat(mime.getMimeType()).isEqualTo("message/rfc822");
	}
	
	@Test
	public void testMimeFromMimeType() {
		MSEmailBodyType mime = MSEmailBodyType.fromMimeType("message/rfc822");
		assertThat(mime).isEqualTo(MSEmailBodyType.MIME);
		assertThat(mime.asXmlValue()).isEqualTo(4);
		assertThat(mime.getMimeType()).isEqualTo("message/rfc822");
	}
	
	@Test
	public void testInvalidInteger() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(0);
		assertThat(mime).isNull();
	}
	
	@Test
	public void testNullInteger() {
		MSEmailBodyType mime = MSEmailBodyType.getValueOf(null);
		assertThat(mime).isNull();
	}
}