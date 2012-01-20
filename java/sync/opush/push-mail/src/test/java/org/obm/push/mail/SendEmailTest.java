package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.addr;
import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;

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
import org.fest.assertions.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;

public class SendEmailTest {

	@Test
	public void testMailTextPlain() throws MimeException, IOException {
		Message message = loadMimeMessage("plainText.eml");
		String defaultFrom = "john@test.opush";
		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		Assertions.assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		Assertions.assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		Assertions.assertThat(sendEmail.getTo()).containsOnly(addr("a@test"), addr("b@test"));
		Assertions.assertThat(sendEmail.getCc()).containsOnly(addr("c@test"));
		Assertions.assertThat(sendEmail.getCci()).containsOnly(addr("d@test"));
		Assertions.assertThat(sendEmail.isInvitation()).isFalse();
	}
	
	private MailboxList from(String addr) throws MimeException {
		MailboxListField field = (MailboxListField) DefaultFieldParser.parse("From: " + addr);
		return field.getMailboxList();
	}
	
	@Test
	public void testMailAsNoFrom() throws MimeException, IOException {
		Message message = loadMimeMessage("plainTextNoFrom.eml");
		String defaultFrom = "john@test.opush";
		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		Assertions.assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		Assertions.assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		Assertions.assertThat(sendEmail.getCc()).isEmpty();
		Assertions.assertThat(sendEmail.getCci()).isEmpty();
	}
	
	@Test
	public void testAndroidIsInvitation() throws MimeException, IOException{
		Message message = loadMimeMessage("androidInvit.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);
		Assert.assertTrue(sendEmail.isInvitation());
	}
	
	@Test
	public void testForwardedInvitation() throws MimeException, IOException{
		Message message = loadMimeMessage("forwardInvitation.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);
		Assert.assertFalse(sendEmail.isInvitation());
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
		Assertions.assertThat(mimeMessage.getMimeType()).isEqualTo("multipart/alternative");
		Body mainBody = mimeMessage.getBody();
		Assertions.assertThat(mainBody).isInstanceOf(Multipart.class);
		Multipart multipart = (Multipart) mainBody;
		Assertions.assertThat(multipart.getCount()).isEqualTo(2);
		Entity textPlain = multipart.getBodyParts().get(0);
		Entity secondPart = multipart.getBodyParts().get(1);
		Assertions.assertThat(textPlain.getMimeType()).isEqualTo("text/plain");
		Assertions.assertThat(secondPart.getMimeType()).isEqualTo("multipart/relative");
		Multipart multipartRelative = (Multipart) secondPart.getBody();
		Assertions.assertThat(multipartRelative.getCount()).isEqualTo(2);
		Entity htmlPart = multipartRelative.getBodyParts().get(0);
		Entity imagePart = multipartRelative.getBodyParts().get(1);
		Assertions.assertThat(htmlPart.getMimeType()).isEqualTo("text/html");
		Assertions.assertThat(imagePart.getMimeType()).isEqualTo("image/png");
		TextBody htmlTextBody = (TextBody) htmlPart.getBody();
		String htmlText = Joiner.on('\n').join(CharStreams.readLines(htmlTextBody.getReader()));
		Assertions.assertThat(htmlText).contains("Galaxy S II")
			.contains("img src=\"cid:_media_external_images_media_7@sec.galaxytab\"");
		String contentId = imagePart.getHeader().getFields("content-id").get(0).getBody();
		Assertions.assertThat(contentId).isEqualTo("_media_external_images_media_7@sec.galaxytab");
	}
}
