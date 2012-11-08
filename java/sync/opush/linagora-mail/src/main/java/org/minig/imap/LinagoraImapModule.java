package org.minig.imap;

import org.minig.imap.idle.IdleClient;
import org.obm.push.backend.MailMonitoringBackend;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.imap.ImapMonitoringImpl;
import org.obm.push.mail.imap.LinagoraMailboxService;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.MessageInputStreamProviderImpl;
import org.obm.push.mail.imap.MinigStoreClient;
import org.obm.push.mail.imap.MinigStoreClientImpl;

import com.google.inject.AbstractModule;

public class LinagoraImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(MailMonitoringBackend.class).to(ImapMonitoringImpl.class);
		bind(MinigStoreClient.Factory.class).to(MinigStoreClientImpl.Factory.class);
		bind(MailboxService.class).to(LinagoraMailboxService.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
		bind(IdleClient.Factory.class).to(IdleClientImpl.Factory.class);
	}
	
}
