package org.obm.push.mail.imap.command;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.mail.IMAPException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.minig.imap.StoreClient;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

//@Slow
@GuiceModule(org.obm.push.minig.imap.MailEnvModule.class)
@RunWith(SlowGuiceRunner.class)
public class ACLCommandsTest {

	@Inject LinagoraImapClientProvider clientProvider;
	@Inject MailboxService mailboxService;
	@Inject GreenMail greenMail;
	
	private String mailbox;
	private String password;
	private UserDataRequest udr;
	private StoreClient client;
	
	@Before
	public void setUp() throws OpushLocatorException, IMAPException {
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
		client = loggedClient();
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Ignore("SETACL is not implemented in Greenmail yet")
	@Test
	public void setAclCommandTest() throws OpushLocatorException, IMAPException {
		client.setAcl(mailbox, mailbox, "");
	}
	
	private StoreClient loggedClient() throws OpushLocatorException, IMAPException  {
		return clientProvider.getImapClient(udr);
	}
}
