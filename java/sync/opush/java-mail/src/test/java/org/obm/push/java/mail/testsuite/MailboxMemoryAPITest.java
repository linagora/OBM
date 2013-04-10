package org.obm.push.java.mail.testsuite;

import org.obm.guice.GuiceModule;
import org.obm.push.java.mail.ExternalProcessMailEnvModule;

@GuiceModule(ExternalProcessMailEnvModule.class)
public class MailboxMemoryAPITest extends org.obm.push.mail.imap.testsuite.MailboxMemoryAPITest {
}
