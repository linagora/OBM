package org.obm.push.minig.imap;

import org.obm.push.LinagoraImapModule;
import org.obm.push.mail.greenmail.GreenMailProviderModule;

import com.google.inject.AbstractModule;

public class MailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapModule());
		install(new org.obm.push.mail.MailEnvModule());
		install(new GreenMailProviderModule());
		
	}
	
}
