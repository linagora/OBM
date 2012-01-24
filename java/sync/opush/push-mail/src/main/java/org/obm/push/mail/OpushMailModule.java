package org.obm.push.mail;

import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfigurationImpl;
import org.obm.push.backend.MailMonitoringBackend;
import org.obm.push.backend.PIMBackend;
import org.obm.push.mail.imap.client.IMAPClient;
import org.obm.push.mail.imap.client.IMAPClientImpl;
import org.obm.push.mail.smtp.SmtpProvider;
import org.obm.push.mail.smtp.SmtpProviderImpl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class OpushMailModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(MailMonitoringBackend.class).to(ImapMonitoringImpl.class);
		bind(MailboxService.class).to(ImapMailboxService.class);
		bind(MailBackend.class).to(MailBackendImpl.class);
		bind(ImapClientProvider.class).to(ImapClientProviderImpl.class);
		bind(EmailConfiguration.class).to(EmailConfigurationImpl.class);
		bind(IMAPClient.class).to(IMAPClientImpl.class);
		bind(SmtpProvider.class).to(SmtpProviderImpl.class);
		Multibinder<PIMBackend> pimBackends = 
				Multibinder.newSetBinder(binder(), PIMBackend.class);
		pimBackends.addBinding().to(MailBackendImpl.class);
	}

}
