package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadMimeMessage;

import java.io.IOException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.address.MailboxList;
import org.apache.james.mime4j.dom.field.MailboxListField;
import org.apache.james.mime4j.field.DefaultFieldParser;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.parser.ParserException;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class SendEmailTest {

	@Test
	public void testMailTextPlain() throws MimeException, IOException, ParserException{
		Message message = loadMimeMessage(getClass(), "plainText.eml");
		String defaultFrom = "john@test.opush";
		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		Assertions.assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		Assertions.assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		Assertions.assertThat(sendEmail.getTo()).containsOnly(addr("a@test"), addr("B <b@test>"));
		Assertions.assertThat(sendEmail.getCc()).containsOnly(addr("c@test"));
		Assertions.assertThat(sendEmail.getCci()).containsOnly(addr("d@test"));
		Assertions.assertThat(sendEmail.isInvitation()).isFalse();
	}

	private Address addr(String addr) throws ParserException {
		return Address.parse(addr);
	}
	
	private MailboxList from(String addr) throws MimeException {
		MailboxListField field = (MailboxListField) DefaultFieldParser.parse("From: " + addr);
		return field.getMailboxList();
	}
	
	@Test
	public void testMailAsNoFrom() throws MimeException, IOException, ParserException{
		Message message = loadMimeMessage(getClass(), "plainTextNoFrom.eml");
		String defaultFrom = "john@test.opush";
		SendEmail sendEmail = new SendEmail(defaultFrom, message);
		Assertions.assertThat(sendEmail.getFrom()).isEqualTo(defaultFrom);
		Assertions.assertThat(sendEmail.getMimeMessage().getFrom()).isEqualTo(from(defaultFrom));
		Assertions.assertThat(sendEmail.getCc()).isEmpty();
		Assertions.assertThat(sendEmail.getCci()).isEmpty();
	}
	
	@Test
	public void testAndroidIsInvitation() throws MimeException, IOException, ParserException{
		Message message = loadMimeMessage(getClass(), "androidInvit.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);
		Assert.assertTrue(sendEmail.isInvitation());
	}
	
	@Test
	public void testForwardedInvitation() throws MimeException, IOException, ParserException{
		Message message = loadMimeMessage(getClass(), "forwardInvitation.eml");
		SendEmail sendEmail = new SendEmail("john@test.opush", message);
		Assert.assertFalse(sendEmail.isInvitation());
	}
}
