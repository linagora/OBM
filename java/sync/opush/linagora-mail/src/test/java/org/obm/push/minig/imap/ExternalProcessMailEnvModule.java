package org.obm.push.minig.imap;

import org.obm.push.LinagoraImapModule;
import org.obm.push.mail.greenmail.ExternalGreenMailModule;

import com.google.inject.AbstractModule;

public class ExternalProcessMailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapModule());
		install(new ExternalGreenMailModule());
		
	}
	
}
