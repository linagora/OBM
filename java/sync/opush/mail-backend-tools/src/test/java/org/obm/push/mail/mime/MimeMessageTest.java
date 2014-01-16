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

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MimeMessageTest {

	@Test
	public void testGetInvitationOnMimeMessageWithoutInvitation() {
		MimePart mimePart = buildMimePart("text/plain;");
		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.getInvitation()).isNull();
	}

	@Test
	public void testGetInvitationOnMimeMessageWithRequestInvitation() {
		MimePart parentMimePart = MimePartImpl.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("REQUEST")).build(); 

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(parentMimePart).build();
		MimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnMimeMessagetWithCancelInvitation() {
		MimePart parentMimePart = MimePartImpl.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("CANCEL")).build();

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(parentMimePart).build();
		MimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "CANCEL"));
	}

	@Test
	public void testGetInvitationOnParentMimePartWithInvitation() {
		MimePart parentMimePart = MimePartImpl.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("REQUEST")).build();

		MimePart invitation = parentMimePart.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnChildMimePartWithInvitation() {
		MimePart textPlain = buildMimePart("text/plain;");

		MimePartImpl.builder().contentType("multipart/alternative;").addChildren(
				textPlain, buildInvitationContentType("REQUEST")).build();

		MimePart invitation = textPlain.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnChildMimePartWithoutInvitation() {
		MimePart textHtml = buildMimePart("text/html;");
		MimePartImpl.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), textHtml).build();

		Assertions.assertThat(textHtml.getInvitation()).isNull();
	}

	@Test
	public void testFindMainMessageNullContentType() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(null)).isNull();
	}

	@Test
	public void testFindMainMessageUnknowContentType() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}

	@Test
	public void testFindMainMessageSimpleMimePart() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/plain"))).isSameAs(mimePart);
	}

	@Test
	public void testFindMainMessageSimpleMimePartCaseInsensitive() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("TEXT/PLAIN"))).isSameAs(mimePart);
	}

	@Test
	public void testFindMainMessageInMultiPartAlternativeTree() {
		MimePart textHtml = buildMimePart("text/html;");
		MimePart mimePart = MimePartImpl.builder().contentType("multipart/alternative")
				.addChildren(textHtml).build();

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isSameAs(textHtml);
	}

	@Test
	public void testFindMainMessageInMultiPartMixedTree() {
		MimePart textHtml = buildMimePart("text/html;");
		
		MimePart multiPartMixed = MimePartImpl.builder().contentType("multipart/mixed")
			.addChildren(
				MimePartImpl.builder().contentType("multipart/alternative")
					.addChildren(buildMimePart("text/plain;"), textHtml).build(),
				buildMimePart("application/octet-stream")).build();

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isSameAs(textHtml);
	}

	@Test
	public void testFindMainMessageWithNestedMessage() {
		MimePart multiPartMixed = MimePartImpl.builder().contentType("multipart/mixed")
				.addChildren(
					buildMimePart("text/plain;"),
					MimePartImpl.builder().contentType("message/rfc822")
						.addChildren(
							buildMimePart("text/plain;"),
							buildMimePart("text/html;"))
						.build())
				.build();

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}

	@Test
	public void testFindMainMessageWithAttachmentMimePart() {
		MimePart multiPartMixed = MimePartImpl.builder().contentType("multipart/mixed")
				.addChildren(buildMimePart("text/plain;"), buildMimePart("text/html;")).build();

		MimeMessage mimeMessage = MimeMessageImpl.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}


	@Test
	public void testIsAttachmentIsFalseWhenTextPlain() {
		MimePart mimePart = buildMimePart("text/plain");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenTextHtml() {
		MimePart mimePart = buildMimePart("text/html");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenTextCalendar() {
		MimePart mimePart = buildMimePart("text/calendar");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentWhenTwoMixedTextParts() {
		MimePart firstPart = buildMimePart("text/plain");
		MimePart secondPart = buildMimePart("text/plain;Content-Disposition=attachment");

		MimePart parentMimePart = buildMimePart("multipart/mixed");
		firstPart.defineParent(parentMimePart, 1);
		secondPart.defineParent(parentMimePart, 2);

		Assertions.assertThat(firstPart.isAttachment()).isFalse();
		Assertions.assertThat(secondPart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentWhenTwoMixedTextPartsAndInlineDisposition() {
		MimePart firstPart = buildMimePart("text/plain");
		MimePart secondPart = buildMimePart("text/plain;Content-Disposition=inline");

		MimePart parentMimePart = buildMimePart("multipart/mixed");
		firstPart.defineParent(parentMimePart, 1);
		secondPart.defineParent(parentMimePart, 2);

		Assertions.assertThat(firstPart.isAttachment()).isFalse();
		Assertions.assertThat(secondPart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartAlternative() {
		MimePart mimePart = buildMimePart("multipart/alternative");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartMixed() {
		MimePart mimePart = buildMimePart("multipart/mixed");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartReport() {
		MimePart mimePart = buildMimePart("multipart/report");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsTrueWhenMessageRfc822() {
		MimePart mimePart = buildMimePart("message/rfc822");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenMessageDeliveryStatus() {
		MimePart mimePart = buildMimePart("message/delivery-status");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenApplicationICS() {
		MimePart mimePart = buildMimePart("application/ics");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenApplicationPDF() {
		MimePart mimePart = buildMimePart("application/pdf");

		int mimePartIndex = 5;
		MimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}
	
	@Test
	public void testContentTransfertEncodingBase64() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding("BASE64").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingBaSe64IgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding("BaSe64").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrintable() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding("QUOTED-PRINTABLE").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrinTableIgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding("Quoted-PrinTable").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testBadContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding("Toto").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}
	
	@Test
	public void testDefaultContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding(null).build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testGetAttachmentExtension() {
		MimeMessage mimePart = MimeMessageImpl.builder().from(MimePartImpl.builder().contentType("text/plain").encoding(null).build()).build();
		assertThat(mimePart.getAttachmentExtension()).isNull();
	}

	@Test
	public void testContainsCalendarMethod() {
		MimeMessage mimePart = MimeMessageImpl.builder().from(buildInvitationContentType("method")).build();
		assertThat(mimePart.containsCalendarMethod()).isTrue();
	}

	@Test
	public void testDoesntContainsCalendarMethod() {
		MimeMessage mimePart = MimeMessageImpl.builder().from(buildMimePart("text/plain")).build();
		assertThat(mimePart.containsCalendarMethod()).isFalse();
	}
	
	private MimePart buildMimePart(String contentType) {
		return MimePartImpl.builder().contentType(contentType).build();
	}

	private MimePart buildInvitationContentType(String method) {
		return buildMimePart("text/calendar; charset=utf-8; method=" + method);
	}
	
	private ContentType contentType(String contentType) {
		return ContentType.builder().contentType(contentType).build();
	}
}