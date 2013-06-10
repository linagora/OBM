package org.obm.push.java.mail;

import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.impl.CollectionPathHelper;
import org.obm.push.mail.greenmail.ExternalGreenMailModule;

import com.google.inject.AbstractModule;

public class ExternalProcessMailEnvModule extends AbstractModule {
	
	@Override
	protected void configure() {
		install(new ImapModule());
		install(new ExternalGreenMailModule());
		
		bind(ICollectionPathHelper.class).to(CollectionPathHelper.class);
	}
}
