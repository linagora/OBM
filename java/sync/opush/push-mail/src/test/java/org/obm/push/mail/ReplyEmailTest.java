package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;


public class ReplyEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testJira2362() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message reply = loadMimeMessage(getClass(), "jira-2362.eml");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("Envoyé depuis mon HTC").contains("> origin");
	}

	@Test
	public void testReplyCopyOfAddress() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message reply = loadMimeMessage(getClass(), "plainText.eml");
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
	
		Assertions.assertThat(replyEmail.getFrom()).isEqualToIgnoringCase("from@linagora.test");
		Assertions.assertThat(replyEmail.getTo()).containsOnly(MailTestsUtils.addr("a@test"), MailTestsUtils.addr("b@test"));
		Assertions.assertThat(replyEmail.getCc()).containsOnly(MailTestsUtils.addr("c@test"));
		Assertions.assertThat(replyEmail.getCci()).containsOnly(MailTestsUtils.addr("d@test"));
	}
	
	@Test
	public void testReplyEncodingShouldBeUTF8() throws IOException, MimeException, NotQuotableEmailException {
		Message reply = loadMimeMessage(getClass(), "plainText.eml");
		MSEmail original = MailTestsUtils.createMSEmailPlainTextASCII("origin");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.getCharset()).isEqualToIgnoringCase("UTF-8");
	}

	@Test
	public void testReplyTextToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessagePlainText(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}

	@Test
	public void testReplyTextToTextFormated() throws IOException, MimeException, NotQuotableEmailException {
		String replyText = "\nresponse text\r\r\rEnvoyé à partir de mon SuperPhone\n\n\n";
		String replyTextExpected = "\r\nresponse text\r\n\r\n\r\nEnvoyé à partir de mon SuperPhone\r\n\r\n\r\n"; 
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessagePlainText(mime4jUtils,replyText);

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).startsWith(replyTextExpected);
		Assertions.assertThat(messageAsString).contains("\r\n> origin").contains("\r\n> Cordialement");
	}
	
	@Test
	public void testReplyHtmlToHtml() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailHtmlText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageHtml(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).contains("response text").containsIgnoringCase("</blockquote>");
	}
	
	@Test
	public void testReplyTextToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MailTestsUtils.createMessagePlainText(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}

	@Test
	public void testReplyHtmlToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageHtml(mime4jUtils, "<b>response html</b>");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("<b>response html</b>").containsIgnoringCase("</blockquote>");
	}

	@Test
	public void testReplyBothToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text", "response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) message.getBody();
		Assertions.assertThat(multipart.getBodyParts()).hasSize(2);
		Entity plainTextPart = multipart.getBodyParts().get(0);
		Entity htmlTextPart = multipart.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(htmlTextPart.getMimeType()).isEqualTo("text/html");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
		String textHtmlAsString = mime4jUtils.toString(htmlTextPart.getBody());
		Assertions.assertThat(textHtmlAsString).containsIgnoringCase("</blockquote>").contains("response html").contains("origin").contains("Cordialement");
	}
	
	@Test
	public void testReplyBothToHtml() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailHtmlText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text", "response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("</blockquote>").contains("response html");
	}
	
	@Test
	public void testReplyBothToBoth() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response text","response html");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) message.getBody();
		Assertions.assertThat(multipart.getBodyParts()).hasSize(2);
		Entity plainTextPart = multipart.getBodyParts().get(0);
		Entity htmlTextPart = multipart.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(htmlTextPart.getMimeType()).isEqualTo("text/html");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
		String textHtmlAsString = mime4jUtils.toString(htmlTextPart.getBody());
		Assertions.assertThat(textHtmlAsString).containsIgnoringCase("</blockquote>").contains("response html").contains("origin").contains("Cordialement");
	}
	
	@Test
	public void testReplyTextToMixed() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailMultipartMixed("origin\nCordialement");
		Message reply = MailTestsUtils.createMessagePlainText(mime4jUtils, "response text");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}
	
	@Test
	public void testReplyMixedToText() throws IOException, MimeException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		byte[] dataToSend = new byte[]{0,1,2,3,4};
		Message reply = MailTestsUtils.createMessageMultipartMixed(mime4jUtils, "response text", dataToSend);

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/mixed");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		
		Multipart parts = (Multipart) message.getBody();
		Assertions.assertThat(parts.getCount()).isEqualTo(2);
		Assertions.assertThat(mime4jUtils.getAttachmentCount(parts)).isEqualTo(1);
		Assertions.assertThat(mime4jUtils.getAttachmentContentTypeList(parts)).containsOnly("image/png");
		
		BinaryBody binaryPart = (BinaryBody) parts.getBodyParts().get(0).getBody();
        byte[] dataRead = new byte[5];
        binaryPart.getInputStream().read(dataRead);
        Assertions.assertThat(dataRead).isEqualTo(dataToSend);

		Entity plainTextPart = parts.getBodyParts().get(1);
		Assertions.assertThat(plainTextPart.getMimeType()).isEqualTo("text/plain");
		String textPlainAsString = mime4jUtils.toString(plainTextPart.getBody());
		Assertions.assertThat(textPlainAsString).contains("response text").contains("origin").contains("Cordialement");
	}
}
