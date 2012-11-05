package org.minig.imap.testsuite;

import org.minig.imap.MailEnvModule;
import org.obm.push.mail.imap.GuiceModule;

@GuiceModule(MailEnvModule.class)
public class MailboxMemoryAPITest extends org.obm.push.mail.imap.testsuite.MailboxMemoryAPITest {
}
