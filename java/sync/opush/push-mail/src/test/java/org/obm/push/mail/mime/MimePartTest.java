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
package org.obm.push.mail.mime;

import static org.easymock.EasyMock.createMock;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.james.mime4j.codec.QuotedPrintableInputStream;
import org.junit.Test;


public class MimePartTest {


	@Test
	public void testContentTransfertEncodingBase64() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding("BASE64").build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isInstanceOf(Base64InputStream.class);
	}

	@Test
	public void testContentTransfertEncodingBaSe64IgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding("BaSe64").build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isInstanceOf(Base64InputStream.class);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrintable() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding("QUOTED-PRINTABLE").build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isInstanceOf(QuotedPrintableInputStream.class);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrinTableIgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding("Quoted-PrinTable").build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isInstanceOf(QuotedPrintableInputStream.class);
	}

	@Test
	public void testBadContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding("Toto").build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}
	
	@Test
	public void testDefaultContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding(null).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}
	
	@Test
	public void testGetAttachmentExtension() {
		MimePart mimePart = MimePartImpl.builder().contentType("image/jpeg").encoding(null).build();
		assertThat(mimePart.getAttachmentExtension()).isEqualTo(".jpg");
	}

	@Test
	public void testContainsCalendarMethod() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/calendar; charset=utf-8; method=method").encoding(null).build();
		assertThat(mimePart.containsCalendarMethod()).isTrue();
	}

	@Test
	public void testDoesntContainsCalendarMethod() {
		MimePart mimePart = MimePartImpl.builder().contentType("text/plain").encoding(null).build();
		assertThat(mimePart.containsCalendarMethod()).isFalse();
	}
}
