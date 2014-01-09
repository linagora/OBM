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
package org.obm.push.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.push.mail.MSMailTestsUtils.addr;
import static org.obm.push.mail.MSMailTestsUtils.loadMimeMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.junit.Test;


import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;



public class SendEmailTest {

	@Test(expected=NullPointerException.class)
	public void testNullDefaultFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainText.eml");
		@SuppressWarnings("unused")
		SendEmail sendEmail = new SendEmail(null, message);
	}
	
	@Test(expected=NullPointerException.class)
	public void testEmptyDefaultFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainText.eml");
		@SuppressWarnings("unused")
		SendEmail sendEmail = new SendEmail("", message);
	}
	
	@Test
	public void testMailTextPlain() throws MimeException, IOException {
		Message message = loadMimeMessage("plainText.eml");
		String defaultFrom = "john@test.opush";

		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		assertThat(sendEmail.getTo()).containsOnly(addr("a@test"), addr("b@test"));
		assertThat(sendEmail.getCc()).containsOnly(addr("c@test"));
		assertThat(sendEmail.getCci()).containsOnly(addr("d@test"));
		assertThat(sendEmail.isInvitation()).isFalse();
	}
	
	private MailboxList from(String addr) throws MimeException {
		MailboxListField field = (MailboxListField) DefaultFieldParser.parse("From: " + addr);
		return field.getMailboxList();
	}
	
	@Test
	public void testMailHasNoFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainTextNoFrom.eml");
		String defaultFrom = "john@test.opush";

		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		assertThat(sendEmail.getCc()).isEmpty();
		assertThat(sendEmail.getCci()).isEmpty();
	}
	
	@Test
	public void testMailHasEmptyFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainTextEmptyFrom.eml");
		String defaultFrom = "john@test.opush";

		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		assertThat(sendEmail.getCc()).isEmpty();
		assertThat(sendEmail.getCci()).isEmpty();
	}
	
	@Test
	public void testMailSpoofFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainTextSpoofFrom.eml");
		String defaultFrom = "john@test.opush";

		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		assertThat(sendEmail.getCc()).isEmpty();
		assertThat(sendEmail.getCci()).isEmpty();
	}

	@Test
	public void testAndroidIsInvitation() throws MimeException, IOException {
		Message message = loadMimeMessage("androidInvit.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);

		assertThat(sendEmail.isInvitation()).isTrue();
	}
	
	@Test
	public void testForwardedInvitation() throws MimeException, IOException{
		Message message = loadMimeMessage("forwardInvitation.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);

		assertThat(sendEmail.isInvitation()).isFalse();
	}

	@Test
	public void testForwardedEmbeddedInvitation() throws MimeException, IOException{
		Message message = loadMimeMessage("forwardEmbeddedInvitation.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);

		assertThat(sendEmail.isInvitation()).isTrue();
	}
	
	@Test
	public void testEmailWithEmbeddedImage() throws MimeException, IOException {
		Message message = loadMimeMessage("androidEmbeddedImage.eml");

		SendEmail sendEmail = new SendEmail("john@test.opush", message);
		Message afterARoundTrip = loadMimeMessage(new ByteArrayInputStream(sendEmail.serializeMimeData().toByteArray()));
		testEmailWithEmbeddedImage(sendEmail);
		SendEmail sendEmailAfterARoundTrip = new SendEmail("john@test.opush", afterARoundTrip);
		testEmailWithEmbeddedImage(sendEmailAfterARoundTrip);
	}

	private void testEmailWithEmbeddedImage(SendEmail sendEmail)
			throws IOException {
		Message mimeMessage = sendEmail.getMimeMessage();
		assertThat(mimeMessage.getMimeType()).isEqualTo("multipart/alternative");
		Body mainBody = mimeMessage.getBody();
		assertThat(mainBody).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) mainBody;
		assertThat(multipart.getCount()).isEqualTo(2);
		Entity textPlain = multipart.getBodyParts().get(0);
		Entity secondPart = multipart.getBodyParts().get(1);
		assertThat(textPlain.getMimeType()).isEqualTo("text/plain");
		assertThat(secondPart.getMimeType()).isEqualTo("multipart/relative");
		Multipart multipartRelative = (Multipart) secondPart.getBody();
		assertThat(multipartRelative.getCount()).isEqualTo(2);
		Entity htmlPart = multipartRelative.getBodyParts().get(0);
		Entity imagePart = multipartRelative.getBodyParts().get(1);
		assertThat(htmlPart.getMimeType()).isEqualTo("text/html");
		assertThat(imagePart.getMimeType()).isEqualTo("image/png");
		TextBody htmlTextBody = (TextBody) htmlPart.getBody();
		String htmlText = Joiner.on('\n').join(CharStreams.readLines(htmlTextBody.getReader()));
		assertThat(htmlText).contains("Galaxy S II")
			.contains("img src=\"cid:_media_external_images_media_7@sec.galaxytab\"");
		String contentId = imagePart.getHeader().getFields("content-id").get(0).getBody();
		assertThat(contentId).isEqualTo("_media_external_images_media_7@sec.galaxytab");
	}
}
