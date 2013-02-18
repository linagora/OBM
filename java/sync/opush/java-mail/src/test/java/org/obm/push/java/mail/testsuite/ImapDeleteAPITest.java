package org.obm.push.java.mail.testsuite;

import org.obm.push.java.mail.MailEnvModule;
import org.obm.test.GuiceModule;

@GuiceModule(MailEnvModule.class)
public class ImapDeleteAPITest extends org.obm.push.mail.imap.testsuite.ImapDeleteAPITest {
}
