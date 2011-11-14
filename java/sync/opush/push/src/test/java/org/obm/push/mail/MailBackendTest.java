package org.obm.push.mail;

import static org.obm.push.mail.MailTestsUtils.loadEmail;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.calendar.CalendarClient;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

public class MailBackendTest {

	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException {
		final User user = Factory.create().createUser("test@test", "test@domain");
		final String password = "pass";
		final AccessToken at = new AccessToken(1, 1, "o-push");
		
		EmailManager emailManager = EasyMock.createMock(EmailManager.class);
		CalendarClient calendarClient = EasyMock.createMock(CalendarClient.class);
		BackendSession backendSession = EasyMock.createMock(BackendSession.class);
		
		EasyMock.expect(backendSession.getUser()).andReturn(user).once();
		EasyMock.expect(backendSession.getPassword()).andReturn(password).once();

		EasyMock.expect(calendarClient.login(user.getLoginAtDomain(), password, "o-push"))
				.andReturn(at).once();
		EasyMock.expect(calendarClient.getUserEmail(at)).andReturn(user.getLoginAtDomain()).once();
		calendarClient.logout(at);
		EasyMock.expectLastCall().once();
		Set<Address> addrs = Sets.newHashSet();
		emailManager.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class), EasyMock.anyBoolean());
		EasyMock.expectLastCall().once();
		
		MailBackend mailBackend = new MailBackend(emailManager, null, null, null, calendarClient, null, new Mime4jUtils(), mockOpushConfigurationService());

		EasyMock.replay(emailManager, calendarClient, backendSession);

		InputStream emailStream = loadEmail(getClass(), "bigEml.eml");
		mailBackend.sendEmail(backendSession, ByteStreams.toByteArray(emailStream), true);
		
		EasyMock.verify(emailManager, calendarClient, backendSession);
	}
	
}
