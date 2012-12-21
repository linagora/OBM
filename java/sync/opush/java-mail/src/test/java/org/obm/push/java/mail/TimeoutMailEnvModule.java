package org.obm.push.java.mail;

import org.obm.push.mail.greenmail.GreenMailProviderModule;

import com.google.inject.AbstractModule;

public class TimeoutMailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new ImapModule());
		install(new GreenMailProviderModule());
		install(new org.obm.push.mail.MailEnvModule(5000));
	}
}
