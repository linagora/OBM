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
package org.obm.push.mail.mime;

import static org.easymock.EasyMock.createMock;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.InputStream;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class MimeMessageTest {

	@Test
	public void testGetInvitationOnMimeMessageWithoutInvitation() {
		MimePart mimePart = buildMimePart("text/plain;");
		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.getInvitation()).isNull();
	}

	@Test
	public void testGetInvitationOnMimeMessageWithRequestInvitation() {
		MimePart parentMimePart = MimePart.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("REQUEST")).build(); 

		MimeMessage mimeMessage = MimeMessage.builder().from(parentMimePart).build();
		IMimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnMimeMessagetWithCancelInvitation() {
		MimePart parentMimePart = MimePart.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("CANCEL")).build();

		MimeMessage mimeMessage = MimeMessage.builder().from(parentMimePart).build();
		IMimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "CANCEL"));
	}

	@Test
	public void testGetInvitationOnParentMimePartWithInvitation() {
		MimePart parentMimePart = MimePart.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), buildInvitationContentType("REQUEST")).build();

		IMimePart invitation = parentMimePart.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnChildMimePartWithInvitation() {
		MimePart textPlain = buildMimePart("text/plain;");

		MimePart.builder().contentType("multipart/alternative;").addChildren(
				textPlain, buildInvitationContentType("REQUEST")).build();

		IMimePart invitation = textPlain.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}

	@Test
	public void testGetInvitationOnChildMimePartWithoutInvitation() {
		MimePart textHtml = buildMimePart("text/html;");
		MimePart.builder().contentType("multipart/alternative;")
				.addChildren(buildMimePart("text/plain;"), textHtml).build();

		Assertions.assertThat(textHtml.getInvitation()).isNull();
	}

	@Test
	public void testFindMainMessageNullContentType() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(null)).isNull();
	}

	@Test
	public void testFindMainMessageUnknowContentType() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}

	@Test
	public void testFindMainMessageSimpleMimePart() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/plain"))).isSameAs(mimePart);
	}

	@Test
	public void testFindMainMessageSimpleMimePartCaseInsensitive() {
		MimePart mimePart = buildMimePart("text/plain");

		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("TEXT/PLAIN"))).isSameAs(mimePart);
	}

	@Test
	public void testFindMainMessageInMultiPartAlternativeTree() {
		MimePart textHtml = buildMimePart("text/html;");
		MimePart mimePart = MimePart.builder().contentType("multipart/alternative")
				.addChildren(textHtml).build();

		MimeMessage mimeMessage = MimeMessage.builder().from(mimePart).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isSameAs(textHtml);
	}

	@Test
	public void testFindMainMessageInMultiPartMixedTree() {
		MimePart textHtml = buildMimePart("text/html;");
		
		MimePart multiPartMixed = MimePart.builder().contentType("multipart/mixed")
			.addChildren(
				MimePart.builder().contentType("multipart/alternative")
					.addChildren(buildMimePart("text/plain;"), textHtml).build(),
				buildMimePart("application/octet-stream")).build();

		MimeMessage mimeMessage = MimeMessage.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isSameAs(textHtml);
	}

	@Test
	public void testFindMainMessageWithNestedMessage() {
		MimePart multiPartMixed = MimePart.builder().contentType("multipart/mixed")
				.addChildren(
					buildMimePart("text/plain;"),
					MimePart.builder().contentType("message/rfc822")
						.addChildren(
							buildMimePart("text/plain;"),
							buildMimePart("text/html;"))
						.build())
				.build();

		MimeMessage mimeMessage = MimeMessage.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}

	@Test
	public void testFindMainMessageWithAttachmentMimePart() {
		MimePart multiPartMixed = MimePart.builder().contentType("multipart/mixed")
				.addChildren(buildMimePart("text/plain;"), buildMimePart("text/html;")).build();

		MimeMessage mimeMessage = MimeMessage.builder().from(multiPartMixed).build();
		Assertions.assertThat(mimeMessage.findMainMessage(contentType("text/html"))).isNull();
	}


	@Test
	public void testIsAttachmentIsFalseWhenTextPlain() {
		IMimePart mimePart = buildMimePart("text/plain");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenTextHtml() {
		IMimePart mimePart = buildMimePart("text/html");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenTextCalendar() {
		IMimePart mimePart = buildMimePart("text/calendar");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentWhenTwoMixedTextParts() {
		IMimePart firstPart = buildMimePart("text/plain");
		IMimePart secondPart = buildMimePart("text/plain;Content-Disposition=attachment");

		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		firstPart.defineParent(parentMimePart, 1);
		secondPart.defineParent(parentMimePart, 2);

		Assertions.assertThat(firstPart.isAttachment()).isFalse();
		Assertions.assertThat(secondPart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentWhenTwoMixedTextPartsAndInlineDisposition() {
		IMimePart firstPart = buildMimePart("text/plain");
		IMimePart secondPart = buildMimePart("text/plain;Content-Disposition=inline");

		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		firstPart.defineParent(parentMimePart, 1);
		secondPart.defineParent(parentMimePart, 2);

		Assertions.assertThat(firstPart.isAttachment()).isFalse();
		Assertions.assertThat(secondPart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartAlternative() {
		IMimePart mimePart = buildMimePart("multipart/alternative");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartMixed() {
		IMimePart mimePart = buildMimePart("multipart/mixed");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsFalseWhenMultipartReport() {
		IMimePart mimePart = buildMimePart("multipart/report");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isFalse();
	}

	@Test
	public void testIsAttachmentIsTrueWhenMessageRfc822() {
		IMimePart mimePart = buildMimePart("message/rfc822");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenMessageDeliveryStatus() {
		IMimePart mimePart = buildMimePart("message/delivery-status");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenApplicationICS() {
		IMimePart mimePart = buildMimePart("application/ics");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}

	@Test
	public void testIsAttachmentIsTrueWhenApplicationPDF() {
		IMimePart mimePart = buildMimePart("application/pdf");

		int mimePartIndex = 5;
		IMimePart parentMimePart = buildMimePart("multipart/mixed");
		mimePart.defineParent(parentMimePart, mimePartIndex);

		Assertions.assertThat(mimePart.isAttachment()).isTrue();
	}
	
	@Test
	public void testContentTransfertEncodingBase64() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding("BASE64").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingBaSe64IgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding("BaSe64").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrintable() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding("QUOTED-PRINTABLE").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testContentTransfertEncodingQuotedPrinTableIgnoreCase() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding("Quoted-PrinTable").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testBadContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding("Toto").build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}
	
	@Test
	public void testDefaultContentTransfert() {
		InputStream inputStream = createMock(InputStream.class);
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding(null).build()).build();
		Object actual = mimePart.decodeMimeStream(inputStream);
		assertThat(actual).isSameAs(inputStream);
	}

	@Test
	public void testGetAttachmentExtension() {
		MimeMessage mimePart = MimeMessage.builder().from(MimePart.builder().contentType("text/plain").encoding(null).build()).build();
		assertThat(mimePart.getAttachmentExtension()).isNull();
	}

	@Test
	public void testContainsCalendarMethod() {
		MimeMessage mimePart = MimeMessage.builder().from(buildInvitationContentType("method")).build();
		assertThat(mimePart.containsCalendarMethod()).isTrue();
	}

	@Test
	public void testDoesntContainsCalendarMethod() {
		MimeMessage mimePart = MimeMessage.builder().from(buildMimePart("text/plain")).build();
		assertThat(mimePart.containsCalendarMethod()).isFalse();
	}
	
	private MimePart buildMimePart(String contentType) {
		return MimePart.builder().contentType(contentType).build();
	}

	private MimePart buildInvitationContentType(String method) {
		return buildMimePart("text/calendar; charset=utf-8; method=" + method);
	}
	
	private ContentType contentType(String contentType) {
		return ContentType.builder().contentType(contentType).build();
	}
}