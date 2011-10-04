package org.obm.push.mail;

import java.io.InputStream;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.store.LocatorService;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;

import com.google.common.collect.Sets;

public class EmailManagerTest {

	@Test
	public void testSendEmailWithBigInputStream() throws ProcessingEmailException, StoreEmailException, SendEmailException, SmtpInvalidRcptException {
		
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		LocatorService locatorService = EasyMock.createMock(LocatorService.class);
		SmtpSender smtpSender = EasyMock.createMock(SmtpSender.class);
		BackendSession backendSession = EasyMock.createMock(BackendSession.class);
		
		EasyMock.expect(emailConfiguration.loginWithDomain()).andReturn(true).once();
		EasyMock.expect(emailConfiguration.activateTls()).andReturn(false).once();
		Set<Address> addrs = Sets.newHashSet();
		smtpSender.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class));
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(emailConfiguration, smtpSender, backendSession);
		
		EmailManager emailManager = new EmailManager(null, emailConfiguration, smtpSender, null, locatorService);

		emailManager.sendEmail(backendSession,
				new Address("test@test.fr"),
				addrs,
				addrs,
				addrs,
				loadDataFile("bigEml.eml"), false);
		
		EasyMock.verify(emailConfiguration, smtpSender, backendSession);
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"eml/" + name);
	}
}
