package org.obm.opush.env;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.util.Locale;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.SyncPermsConfigurationService;

public final class ConfigurationModule extends AbstractOverrideModule {

	private final Configuration configuration;

	public ConfigurationModule(Configuration configuration) {
		super();
		this.configuration = configuration;
	}
	
	@Override
	protected void configureImpl() {
		bindWithMock(ConfigurationService.class);
		bindWithMock(EmailConfiguration.class);
		bindWithMock(SyncPermsConfigurationService.class);
		defineBehavior();
	}

	private void defineBehavior() {
		ConfigurationService configurationService = getMock(ConfigurationService.class);
		expect(configurationService.getResourceBundle(anyObject(Locale.class)))
			.andReturn(configuration.bundle).anyTimes();
		
		SyncPermsConfigurationService syncPerms = getMock(SyncPermsConfigurationService.class);
		expect(syncPerms.getBlackListUser()).andReturn(configuration.syncPerms.blacklist).anyTimes();
		expect(syncPerms.allowUnknownPdaToSync()).andReturn(configuration.syncPerms.allowUnkwownDevice).anyTimes();
		
		EmailConfiguration emailConfiguration = getMock(EmailConfiguration.class);
		expect(emailConfiguration.activateTls()).andReturn(configuration.mail.activateTls).anyTimes();
		expect(emailConfiguration.loginWithDomain()).andReturn(configuration.mail.loginWithDomain).anyTimes();
	}
}