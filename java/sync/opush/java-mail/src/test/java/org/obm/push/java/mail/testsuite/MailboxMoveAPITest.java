package org.obm.push.java.mail.testsuite;

import org.obm.guice.GuiceModule;
import org.obm.push.java.mail.MailEnvModule;

@GuiceModule(MailEnvModule.class)
public class MailboxMoveAPITest extends org.obm.push.mail.imap.testsuite.MailboxMoveAPITest {
}
