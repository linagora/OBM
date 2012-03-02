package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;


public class ForwardEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testForwardMessageMixedMultiPartWithAttachment() throws MimeException, IOException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage(getClass(), "OBMFULL-2958-ATTACHMENT.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message);
		
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
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage(getClass(), "OBMFULL-2958.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message);
		
		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		Assertions.assertThat(messageAsString).isNotNull();
		Assertions.assertThat(messageAsString).contains("Content-Type: text/plain");
		Assertions.assertThat(messageAsString).contains("C'est le message ;-)");
	}
		
}
