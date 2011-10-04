package fr.aliacom.obm.common.calendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.obm.sync.calendar.Attendee;

import com.ctc.wstx.io.CharsetNames;
import com.google.common.collect.ImmutableList;

public class MailSendTest {

	@Test
	public void testBasicEventEmail() throws MessagingException, IOException {
		InternetAddress from = new InternetAddress("sender@test");
		Attendee attendee1 = new Attendee();
		attendee1.setEmail("attendee1@test");
		ImmutableList<Attendee> attendees = ImmutableList.of(attendee1);
		String subject = "subject";
		String bodyTxt = "text";
		String bodyHtml = "html";
		String icsContent = "ics";
		EventMail eventMail = new EventMail(from, attendees, subject, bodyTxt, bodyHtml, icsContent, "REQUEST");
		MimeMessage mail = eventMail.buildMimeMail(Session.getDefaultInstance(new Properties()));
		ByteArrayOutputStream mailByteStream = new ByteArrayOutputStream();
		mail.writeTo(mailByteStream);
		String content = new String(mailByteStream.toByteArray(), CharsetNames.CS_UTF8);
		Assert.assertThat(content, new StringContains("Subject: subject"));
		Assert.assertThat(content, new StringContains("To: " + attendee1.getEmail()));
	}
	
}
