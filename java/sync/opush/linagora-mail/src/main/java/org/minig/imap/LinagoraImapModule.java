package org.minig.imap;

import org.minig.imap.idle.IdleClient;
import org.obm.mail.message.MessageFetcher;
import org.obm.mail.message.MessageFetcherImpl;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.mail.imap.LinagoraMailboxService;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.MessageInputStreamProviderImpl;
import org.obm.push.mail.imap.MinigStoreClient;
import org.obm.push.mail.imap.MinigStoreClientImpl;

import com.google.inject.AbstractModule;

public class LinagoraImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(ImapClientProvider.class).to(LinagoraImapClientProvider.class);
		bind(MinigStoreClient.Factory.class).to(MinigStoreClientImpl.Factory.class);
		bind(PrivateMailboxService.class).to(LinagoraMailboxService.class);
		bind(MailboxService.class).to(LinagoraMailboxService.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
		bind(MessageFetcher.Factory.class).to(MessageFetcherImpl.Factory.class);
		bind(IdleClient.Factory.class).to(IdleClientImpl.Factory.class);
	}
	
}
