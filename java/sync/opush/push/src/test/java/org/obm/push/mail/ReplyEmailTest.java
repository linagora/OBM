package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.columba.ristretto.parser.ParserException;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEmail;
import org.obm.push.utils.Mime4jUtils;


public class ReplyEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testJira2362() throws IOException, MimeException, ParserException, TransformerException {
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
	public void testReplyCopyOfAddress() throws IOException, MimeException, ParserException, TransformerException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");

		Message reply = loadMimeMessage(getClass(), "plainText.eml");
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
	
		Assertions.assertThat(replyEmail.getFrom()).isEqualToIgnoringCase("from@linagora.test");
		Assertions.assertThat(replyEmail.getTo()).containsOnly(MailTestsUtils.addr("a@test"), MailTestsUtils.addr("B <b@test>"));
		Assertions.assertThat(replyEmail.getCc()).containsOnly(MailTestsUtils.addr("c@test"));
		Assertions.assertThat(replyEmail.getCci()).containsOnly(MailTestsUtils.addr("d@test"));
	}
	
	@Test
	public void testReplyEncodingShouldBeUTF8() throws IOException, MimeException, ParserException, TransformerException {
		Message reply = loadMimeMessage(getClass(), "plainText.eml");
		MSEmail original = MailTestsUtils.createMSEmailPlainTextASCII("origin");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.getCharset()).isEqualToIgnoringCase("UTF-8");
	}

	@Test
	public void testReplyTextToText() throws IOException, MimeException, ParserException, TransformerException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessagePlainText(mime4jUtils,"response text");

		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody body = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(body);
		Assertions.assertThat(messageAsString).contains("response text").contains("\n> origin").contains("\n> Cordialement");
	}
	
	@Test
	public void testReplyHtmlToHtml() throws IOException, MimeException, ParserException, TransformerException {
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
	public void testReplyTextToBoth() throws IOException, MimeException, ParserException, TransformerException {
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
	public void testReplyHtmlToBoth() throws IOException, MimeException, ParserException, TransformerException {
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
		Assertions.assertThat(messageAsString).contains("response html").containsIgnoringCase("</blockquote>");
	}

	@Test
	public void testReplyBothToText() throws IOException, MimeException, ParserException, TransformerException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response html and text");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("response html and text").contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("</blockquote>").contains("Content-Type: text/plain").contains("Content-Type: text/html");
	}
	
	@Test
	public void testReplyBothToHtml() throws IOException, MimeException, ParserException, TransformerException {
		MSEmail original = MailTestsUtils.createMSEmailHtmlText("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response html and text");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("</blockquote>").contains("response html and text");
	}
	
	@Test
	public void testReplyBothToBoth() throws IOException, MimeException, ParserException, TransformerException {
		MSEmail original = MailTestsUtils.createMSEmailMultipartAlt("origin\nCordialement");
		Message reply = MailTestsUtils.createMessageTextAndHtml(mime4jUtils, "response html and text");
		
		ReplyEmail replyEmail = new ReplyEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, reply);
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.isMultipart()).isTrue();
		Assertions.assertThat(message.getMimeType()).isEqualTo("multipart/alternative");
		Assertions.assertThat(message.getBody()).isInstanceOf(Multipart.class);
		String messageAsString = mime4jUtils.toString(message.getBody());
		Assertions.assertThat(messageAsString).contains("response html and text").contains("origin").contains("Cordialement");
		Assertions.assertThat(messageAsString).containsIgnoringCase("</blockquote>").contains("Content-Type: text/plain").contains("Content-Type: text/html");
	}
}
