package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.TextBody;
import org.columba.ristretto.parser.ParserException;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEmailBody;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.Mime4jUtils;


public class ReplyEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testJira2362() throws IOException, MimeException, ParserException {
		Message reply = loadMimeMessage(getClass(), "jira-2362.eml");
		
		MSEmail original = createPlainTextMSEmail("origin");
		
		ReplyEmail replyEmail = new ReplyEmail(mime4jUtils, "from@linagora.test", original, reply);
		
		Message message = replyEmail.getMimeMessage();
		Assertions.assertThat(message.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(message.isMultipart()).isFalse();
		Assertions.assertThat(message.getBody()).isInstanceOf(TextBody.class);
		TextBody textBody = (TextBody) message.getBody();
		String messageAsString = mime4jUtils.toString(textBody);
		Assertions.assertThat(messageAsString).contains("Envoyé depuis mon HTC").contains("> origin");
	}

	private MSEmail createPlainTextMSEmail(String content) {
		MSEmail original = new MSEmail();
		MSEmailBody msEmailBody = new MSEmailBody();
		msEmailBody.addConverted(MSEmailBodyType.PlainText, content);
		original.setBody(msEmailBody);
		return original;
	}

}
