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
package org.minig.imap.mime;

import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class MimeMessageTest {

	@Test
	public void testGetInvitationOnMimeMessageWithoutInvitation() {
		IMimePart mimePart = buildMimePart("text/plain;");
		MimeMessage mimeMessage = new MimeMessage(mimePart);
		Assertions.assertThat(mimeMessage.getInvitation()).isNull();
	}
	
	@Test
	public void testGetInvitationOnMimeMessageWithRequestInvitation() {
		IMimePart parentMimePart = buildMimePart("multipart/alternative;");

		IMimePart textPlain = buildMimePart("text/plain;");
		parentMimePart.addPart(textPlain);
		
		IMimePart textCalendar = buildInvitationContentType("REQUEST");
		parentMimePart.addPart(textCalendar);
		
		MimeMessage mimeMessage = new MimeMessage(parentMimePart);
		IMimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}
	
	@Test
	public void testGetInvitationOnMimeMessagetWithCancelInvitation() {
		IMimePart parentMimePart = buildMimePart("multipart/alternative;");

		IMimePart textPlain = buildMimePart("text/plain;");
		parentMimePart.addPart(textPlain);
		
		IMimePart textCalendar = buildInvitationContentType("CANCEL");
		parentMimePart.addPart(textCalendar);
		
		MimeMessage mimeMessage = new MimeMessage(parentMimePart);
		IMimePart invitation = mimeMessage.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "CANCEL"));
	}
	
	@Test
	public void testGetInvitationOnParentMimePartWithInvitation() {
		IMimePart parentMimePart = buildMimePart("multipart/alternative;");

		IMimePart textPlain = buildMimePart("text/plain;");
		parentMimePart.addPart(textPlain);
		
		IMimePart textCalendar = buildInvitationContentType("REQUEST");
		parentMimePart.addPart(textCalendar);
		
		IMimePart invitation = parentMimePart.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}
	
	@Test
	public void testGetInvitationOnChildMimePartWithInvitation() {
		IMimePart parentMimePart = buildMimePart("multipart/alternative;");

		IMimePart textPlain = buildMimePart("text/plain;");
		parentMimePart.addPart(textPlain);
		
		IMimePart textCalendar = buildInvitationContentType("REQUEST");
		parentMimePart.addPart(textCalendar);
		
		IMimePart invitation = textPlain.getInvitation();
		Assertions.assertThat(invitation).isNotNull();
		Assertions.assertThat(invitation.getFullMimeType()).isEqualTo("text/calendar");
		Assertions.assertThat(invitation.getBodyParam("method")).isEqualTo(new BodyParam("method", "REQUEST"));
	}
	
	@Test
	public void testGetInvitationOnChildMimePartWithoutInvitation() {
		IMimePart parentMimePart = buildMimePart("multipart/alternative;");

		IMimePart textPlain = buildMimePart("text/plain;");
		parentMimePart.addPart(textPlain);
		
		IMimePart textHtml = buildMimePart("text/html;");
		parentMimePart.addPart(textHtml);
		
		Assertions.assertThat(textHtml.getInvitation()).isNull();
	}
	
	@Test
					public void testFindMainMessageNullContentType() {
						IMimePart mimePart = buildMimePart("text/plain");
						
						MimeMessage mimeMessage = new MimeMessage(mimePart);
						Assertions.assertThat(mimeMessage.findMainMessage(null)).isNull();
					}
	
	@Test
					public void testFindMainMessageUnknowContentType() {
						IMimePart mimePart = buildMimePart("text/plain");
						
						MimeMessage mimeMessage = new MimeMessage(mimePart);
						Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/html"))).isNull();
					}
	
	@Test
			public void testFindMainMessageSimpleMimePart() {
				IMimePart mimePart = buildMimePart("text/plain");
				
				MimeMessage mimeMessage = new MimeMessage(mimePart);
				Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/plain"))).isSameAs(mimePart);
			}
	
	@Test
			public void testFindMainMessageSimpleMimePartCaseInsensitive() {
				IMimePart mimePart = buildMimePart("text/plain");
				
				MimeMessage mimeMessage = new MimeMessage(mimePart);
				Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("TEXT/PLAIN"))).isSameAs(mimePart);
			}
	
	@Test
					public void testFindMainMessageInMultiPartAlternativeTree() {
						IMimePart mimePart = buildMimePart("multipart/alternative");
						
						IMimePart textPlain = buildMimePart("text/plain;");
						mimePart.addPart(textPlain);
						
						IMimePart textHtml = buildMimePart("text/html;");
						mimePart.addPart(textHtml);
						
						MimeMessage mimeMessage = new MimeMessage(mimePart);
						Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/html"))).isSameAs(textHtml);
					}
	
	@Test
					public void testFindMainMessageInMultiPartMixedTree() {
						IMimePart multiPartMixed = buildMimePart("multipart/mixed");
						
						IMimePart multiPartAlternative = buildMimePart("multipart/alternative");
						IMimePart textPlain = buildMimePart("text/plain;");
						IMimePart textHtml = buildMimePart("text/html;");
						multiPartAlternative.addPart(textPlain);
						multiPartAlternative.addPart(textHtml);
						
						IMimePart application = buildMimePart("application/octet-stream");
						multiPartMixed.addPart(multiPartAlternative);
						multiPartMixed.addPart(application);
						
						MimeMessage mimeMessage = new MimeMessage(multiPartMixed);
						Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/html"))).isSameAs(textHtml);
					}
	
	@Test
					public void testFindMainMessageWithNestedMessage() {
						IMimePart multiPartMixed = buildMimePart("multipart/mixed");
						multiPartMixed.addPart(buildMimePart("text/plain;"));
						
						IMimePart nestedMessage = buildMimePart("message/rfc822");
						nestedMessage.addPart(buildMimePart("text/plain;"));
						nestedMessage.addPart(buildMimePart("text/html;"));
						multiPartMixed.addPart(nestedMessage);
						
						MimeMessage mimeMessage = new MimeMessage(multiPartMixed);
						Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/html"))).isNull();
					}
	
	@Test
					public void testFindMainMessageWithAttachmentMimePart() {
						IMimePart multiPartMixed = buildMimePart("multipart/mixed");
						multiPartMixed.addPart(buildMimePart("text/plain;"));
						multiPartMixed.addPart(buildMimePart("text/html;"));
						
						MimeMessage mimeMessage = new MimeMessage(multiPartMixed);
						Assertions.assertThat(mimeMessage.findMainMessage(buildContentType("text/html"))).isNull();
					}
	
	private IMimePart buildMimePart(String contentType) {
		IMimePart mimePart = new MimePart();
		mimePart.setContentType(buildContentType(contentType));
		return mimePart;
	}
	
	private ContentType buildContentType(String contentType) {
		return new ContentType.Builder().contentType(contentType).build();
	}
	
	private IMimePart buildInvitationContentType(String method) {
		IMimePart mimePart = new MimePart();
		mimePart.setContentType(
				new ContentType.Builder().contentType("text/calendar; charset=utf-8; method=" + method).build());
		return mimePart;
	}
}