package org.obm.push.java.mail;

import org.obm.push.mail.MailboxService;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.MessageInputStreamProviderImpl;

import com.google.inject.AbstractModule;

public class ImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(ImapStore.Factory.class).to(ImapStoreImpl.Factory.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
	}
	
}
