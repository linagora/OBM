package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.NotQuotableEmailException;
import org.obm.push.utils.Mime4jUtils;

import com.google.common.collect.ImmutableMap;


public class ForwardEmailTest {

	private Mime4jUtils mime4jUtils;

	@Before
	public void setUp() {
		mime4jUtils = new Mime4jUtils();
	}
	
	@Test
	public void testForwardMessageMixedMultiPartWithAttachment() throws MimeException, IOException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage("MAIL-WITH-ATTACHMENT.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message, 
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
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage("OBMFULL-2958.eml");
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message, 
						ImmutableMap.<String, MSAttachementData>of());
		
		Message mimeMessage = forwardEmail.getMimeMessage();
		String messageAsString = mime4jUtils.toString(mimeMessage.getBody());
		
		Assertions.assertThat(messageAsString).isNotNull();
		Assertions.assertThat(messageAsString).contains("Content-Type: text/plain");
		Assertions.assertThat(messageAsString).contains("C'est le message ;-)");
	}
	
	@Test
	public void testForwardMessageWithAddingOriginalMailAttachments() throws MimeException, IOException, NotQuotableEmailException {
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage("OBMFULL-2958.eml");
		
		String text = "It\'s my attachment";
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		
		MSAttachementData msAttachementData = new MSAttachementData("application/octet-stream", is);
		Map<String, MSAttachementData> ms = new HashMap<String, MSAttachementData>();
		ms.put("file.txt", msAttachementData);
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message, ms);
		
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
		MSEmail original = MailTestsUtils.createMSEmailPlainText("origin");
		Message message = loadMimeMessage("plainText.eml");
		
		String text = "It\'s my attachment";
		InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
		
		MSAttachementData msAttachementData = new MSAttachementData("application/octet-stream", is);
		Map<String, MSAttachementData> ms = new HashMap<String, MSAttachementData>();
		ms.put("file.txt", msAttachementData);
		
		ForwardEmail forwardEmail = 
				new ForwardEmail(mockOpushConfigurationService(), mime4jUtils, "from@linagora.test", original, message, ms);
		
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
