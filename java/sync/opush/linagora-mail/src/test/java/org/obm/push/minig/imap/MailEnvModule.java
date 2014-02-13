package org.obm.push.minig.imap;

import org.obm.push.LinagoraImapClientModule;
import org.obm.push.mail.greenmail.GreenMailProviderModule;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class MailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapClientModule());
		install(new GreenMailProviderModule());
		bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(3600000);
	}
	
}
