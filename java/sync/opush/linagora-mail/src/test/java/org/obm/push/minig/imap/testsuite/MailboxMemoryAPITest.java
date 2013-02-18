package org.obm.push.minig.imap.testsuite;

import org.obm.push.minig.imap.ExternalProcessMailEnvModule;
import org.obm.test.GuiceModule;

@GuiceModule(ExternalProcessMailEnvModule.class)
public class MailboxMemoryAPITest extends org.obm.push.mail.imap.testsuite.MailboxMemoryAPITest {
}
