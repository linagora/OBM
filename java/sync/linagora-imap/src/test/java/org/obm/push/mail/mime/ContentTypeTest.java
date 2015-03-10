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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ContentTypeTest {

	@Test
	public void testContentType() {
		ContentType contentType = ContentType.builder().contentType("text/plain").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).isEmpty();
	}
	
	@Test
	public void testContentTypeWithSeparator() {
		ContentType contentType = ContentType.builder().contentType("text/plain;").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testContenTypeWithNullPrimaryType() {
		ContentType.Builder builder = ContentType.builder().primaryType(null).subType("subType");
		builder.build();
	}
	
	@Test(expected=NullPointerException.class)
	public void testContenTypeWithNullSubType() {
		ContentType.Builder builder = ContentType.builder().primaryType("primaryType").subType(null);
		builder.build();
	}
	
	@Test
	public void testContentTypeWithParameter() {
		ContentType contentType = ContentType.builder().contentType("text/plain; charset=utf-8").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("charset", "utf-8"));
	}
	
	@Test
	public void testContentTypeWithParameterAndSepartor() {
		ContentType contentType = ContentType.builder().contentType("text/plain; charset=utf-8;").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("charset", "utf-8"));
	}
	
	@Test
	public void testContentTypeWithParameters() {
		ContentType contentType = ContentType.builder().
				contentType("text/plain; charset=utf-8; method=REQUEST").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).
			containsOnly(new BodyParam("charset", "utf-8"), new BodyParam("method", "REQUEST"));
	}
	
	@Test
	public void testContentTypeWithSpaceCharacterInParameter() {
		ContentType contentType = ContentType.builder().contentType("text/plain; charset= utf-8").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("charset", "utf-8"));
	}
	
	@Test
	public void testContentTypeWithInSensitiveParameter() {
		ContentType contentType = ContentType.builder().contentType("text/plain; CHARSET=utf-8").build();
		
		assertThat(contentType.getPrimaryType()).isEqualTo("text");
		assertThat(contentType.getSubType()).isEqualTo("plain");
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("charset", "utf-8"));
	}
	
	@Test
	public void testTrimKeyOnContentTypeBodyParams() {
		ContentType contentType = ContentType.builder().contentType("text/plain;         charset=utf-8").build();
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("charset", "utf-8"));
	}
	
	@Test
	public void testBoundaryWithoutQuote() {
		ContentType contentType = ContentType.builder().contentType("multipart/mixed; boundary=----=_Part_0_1330682067197").build();
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("boundary", "----=_Part_0_1330682067197"));
	}
	
	@Test
	public void testBoundaryWithQuote() {
		ContentType contentType = ContentType.builder().contentType("multipart/mixed; boundary=\"----=_Part_0_1330682067197\"").build();
		assertThat(contentType.getBodyParams()).containsOnly(new BodyParam("boundary", "----=_Part_0_1330682067197"));
	}
	
	@Test
	public void isAttachmentShouldBeFalseWhenContentDispositionIsNotAttachment() {
		ContentType contentType = ContentType.builder()
				.contentType("image/jpeg")
				.contentDisposition(ContentDisposition.INLINE.name())
				.build();
		assertThat(contentType.isAttachment()).isFalse();
	}
	
	@Test
	public void isAttachmentShouldBeFalseWhenContentDispositionIsNotSet() {
		ContentType contentType = ContentType.builder()
				.contentType("image/jpeg")
				.build();
		assertThat(contentType.isAttachment()).isFalse();
	}
	
	@Test
	public void isAttachmentShouldBeTrueWhenContentDispositionIsAttachment() {
		ContentType contentType = ContentType.builder()
				.contentType("image/jpeg")
				.contentDisposition(ContentDisposition.ATTACHMENT.name())
				.build();
		assertThat(contentType.isAttachment()).isTrue();
	}
}