package org.obm.push.minig.imap;

import org.obm.push.LinagoraImapModule;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.impl.CollectionPathHelper;
import org.obm.push.mail.greenmail.GreenMailProviderModule;

import com.google.inject.AbstractModule;

public class TimeoutMailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new LinagoraImapModule());
		install(new GreenMailProviderModule());
		install(new org.obm.push.mail.MailEnvModule(5000));
		
		bind(ICollectionPathHelper.class).to(CollectionPathHelper.class);
	}
	
}
