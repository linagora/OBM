package org.obm.push.mail;

import org.obm.push.backend.MailMonitoringBackend;
import org.obm.push.backend.PIMBackend;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class OpushMailModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MailMonitoringBackend.class).to(ImapMonitoringImpl.class);
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(MailBackend.class).to(MailBackendImpl.class);
		bind(ImapClientProvider.class).to(ImapClientProviderImpl.class);
		Multibinder<PIMBackend> pimBackends = 
				Multibinder.newSetBinder(binder(), PIMBackend.class);
		pimBackends.addBinding().to(MailBackendImpl.class);
	}

}
