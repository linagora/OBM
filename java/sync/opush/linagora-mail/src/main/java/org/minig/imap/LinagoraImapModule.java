package org.minig.imap;

import org.obm.mail.message.MessageFetcher;
import org.obm.mail.message.MessageFetcherImpl;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.imap.ImapClientProviderImpl;
import org.obm.push.mail.imap.ImapMailboxService;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.ImapStoreImpl;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.MessageInputStreamProviderImpl;
import org.obm.push.mail.imap.MinigStoreClient;
import org.obm.push.mail.imap.MinigStoreClientImpl;

import com.google.inject.AbstractModule;

public class LinagoraImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(ImapClientProvider.class).to(ImapClientProviderImpl.class);
		bind(MinigStoreClient.Factory.class).to(MinigStoreClientImpl.Factory.class);
		bind(PrivateMailboxService.class).to(ImapMailboxService.class);
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(ImapStore.Factory.class).to(ImapStoreImpl.Factory.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
		bind(MessageFetcher.Factory.class).to(MessageFetcherImpl.Factory.class);
	}
	
}
