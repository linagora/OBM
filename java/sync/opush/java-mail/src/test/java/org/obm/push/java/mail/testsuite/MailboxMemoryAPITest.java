package org.obm.push.java.mail.testsuite;

import org.obm.push.java.mail.ExternalProcessMailEnvModule;
import org.obm.test.GuiceModule;

@GuiceModule(ExternalProcessMailEnvModule.class)
public class MailboxMemoryAPITest extends org.obm.push.mail.imap.testsuite.MailboxMemoryAPITest {
}
