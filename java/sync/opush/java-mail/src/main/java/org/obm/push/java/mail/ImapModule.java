package org.obm.push.java.mail;

import org.obm.push.mail.MailboxService;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.MessageInputStreamProviderImpl;
import org.obm.push.resource.JavaMailResourcesService;
import org.obm.push.resource.ResourcesService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class ImapModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(ImapStore.Factory.class).to(ImapStoreImpl.Factory.class);
		bind(MessageInputStreamProvider.class).to(MessageInputStreamProviderImpl.class);
		
		Multibinder<ResourcesService> resources = Multibinder.newSetBinder(binder(), ResourcesService.class);
		resources.addBinding().to(JavaMailResourcesService.class);
	}
	
}
