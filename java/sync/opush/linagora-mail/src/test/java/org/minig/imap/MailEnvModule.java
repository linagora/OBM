package org.minig.imap;

import com.google.inject.AbstractModule;

public class MailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapModule());
		install(new org.obm.push.mail.MailEnvModule());
		
	}
	
}
