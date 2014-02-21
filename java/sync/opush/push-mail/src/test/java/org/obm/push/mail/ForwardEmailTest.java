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

import static org.obm.push.mail.MSMailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MSMailTestsUtils.mockOpushConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.exception.NotQuotableEmailException;

import com.google.common.collect.ImmutableMap;


public class ForwardEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testForwardMessageMixedMultiPartWithAttachment() throws MimeException, IOException, NotQuotableEmailException {
		Map<MSEmailBodyType, EmailView> original = EmailViewTestsUtils.createPlainTextMap("origin");
		Message message = loadMimeMessage("MAIL-WITH-ATTACHMENT.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfiguration(), mime4jUtils, "from@linagora.test", original, message, 
						ImmutableMap.<String, MSAttachementData>of());
		
		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		int attachmentCount = mime4jUtils.getAttachmentCount((Multipart)mimeMessage.getBody());
		
		Assertions.assertThat(messageAsString).isNotNull();
		
		Assertions.assertThat(messageAsString).contains("Content-Type: text/plain");
		Assertions.assertThat(messageAsString).contains("C'est le message ;-)");
		
		Assertions.assertThat(attachmentCount).isEqualTo(1);
		Assertions.assertThat(messageAsString).contains("Content-Type: application/octet-stream;");
		Assertions.assertThat(messageAsString).contains("name=\"exploits_of_a_mom.png\"");
	}
	
	@Test
	public void testForwardMessageAlternativeMultiPart() throws MimeException, IOException, NotQuotableEmailException {
		Map<MSEmailBodyType, EmailView> original = EmailViewTestsUtils.createPlainTextMap("origin");
		Message message = loadMimeMessage("OBMFULL-2958.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfiguration(), mime4jUtils, "from@linagora.test", original, message, 
						ImmutableMap.<String, MSAttachementData>of());
		
		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		Assertions.assertThat(messageAsString).isNotNull();
		Assertions.assertThat(messageAsString).contains("Content-Type: text/plain");
		Assertions.assertThat(messageAsString).contains("C'est le message ;-)");
	}
	
	@Test
	public void testForwardMessageWithAddingOriginalMailAttachments() throws MimeException, IOException, NotQuotableEmailException {
		Map<MSEmailBodyType, EmailView> original = EmailViewTestsUtils.createPlainTextMap("origin");
		Message message = loadMimeMessage("OBMFULL-2958.eml");
		
		String text = "It\'s my attachment";
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		
		MSAttachementData msAttachementData = new MSAttachementData("application/octet-stream", is);
		Map<String, MSAttachementData> ms = new HashMap<String, MSAttachementData>();
		ms.put("file.txt", msAttachementData);
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfiguration(), mime4jUtils, "from@linagora.test", original, message, ms);

		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		String mixedMultipartBoundary = getMixedMultipartBoundary(messageAsString, "Content-Type: multipart/alternative;");
		String mixedMultipartAttachmentBoundary = getMixedMultipartAttachmentBoundary(messageAsString);
		
		Assertions.assertThat(messageAsString).isNotNull();
		Assertions.assertThat(messageAsString).
			contains("Content-Type: multipart/alternative;").
			contains("Content-Type: text/plain").
			contains("Content-Type: text/html").
			contains("application/octet-stream");
		Assertions.assertThat(mixedMultipartBoundary).isEqualTo(mixedMultipartAttachmentBoundary);
	}
	
	@Test
	public void testForwardSampleBodyMessageWithAddingOriginalMailAttachments() throws MimeException, IOException, NotQuotableEmailException {
		Map<MSEmailBodyType, EmailView> original = EmailViewTestsUtils.createPlainTextMap("origin");
		Message message = loadMimeMessage("plainText.eml");
		
		String text = "It\'s my attachment";
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		
		MSAttachementData msAttachementData = new MSAttachementData("application/octet-stream", is);
		Map<String, MSAttachementData> ms = new HashMap<String, MSAttachementData>();
		ms.put("file.txt", msAttachementData);
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfiguration(), mime4jUtils, "from@linagora.test", original, message, ms);
		
		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		String mixedMultipartBoundary = getMixedMultipartBoundary(messageAsString, "Content-Type: text/plain");
		String mixedMultipartAttachmentBoundary = getMixedMultipartAttachmentBoundary(messageAsString);
		
		Assertions.assertThat(messageAsString).isNotNull();
		Assertions.assertThat(messageAsString).
			contains("Content-Type: text/plain").
			contains("application/octet-stream");
		Assertions.assertThat(mixedMultipartBoundary).isEqualTo(mixedMultipartAttachmentBoundary);
	}
		
	private String getMixedMultipartBoundary(String messageAsString, String str) {
		int startMultipartAlternativeIndex = messageAsString.indexOf(str);
		String mixedMultipartBoundary = messageAsString.substring(0, startMultipartAlternativeIndex);
		return mixedMultipartBoundary;
	}
	
	private String getMixedMultipartAttachmentBoundary(String messageAsString) {
		int startAttachmentIndex = messageAsString.indexOf("Content-Type: application/octet-stream");
		String messageBeforeAttachment = messageAsString.substring(0, startAttachmentIndex);
		int endMultipartAlternativeIndex = messageBeforeAttachment.lastIndexOf("---=Part");
		String mixedMultipartAttachmentBoundary = messageBeforeAttachment.substring(endMultipartAlternativeIndex);
		return mixedMultipartAttachmentBoundary;
	}
	
}
