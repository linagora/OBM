package org.obm.push.mail;

import org.obm.push.backend.MailMonitoringBackend;

import com.google.inject.AbstractModule;

public class OpushMailModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MailMonitoringBackend.class).to(ImapMonitoringImpl.class);
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(MailBackend.class).to(MailBackendImpl.class);
		bind(ImapClientProvider.class).to(ImapClientProviderImpl.class);
	}

}
