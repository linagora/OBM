package org.obm.push.store.ehcache;

import java.io.InputStream;

import org.easymock.EasyMock;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.store.StoreNotFoundException;

public class StoreManagerConfigurationTest {

	protected ConfigurationService initConfigurationServiceMock() throws StoreNotFoundException {
		ConfigurationService configurationService = EasyMock.createMock(ConfigurationService.class);
		EasyMock.expect(configurationService.getStoreConfiguration()).andReturn( getStoreConfiguration() );
		EasyMock.replay(configurationService);
		
		return configurationService;
	}

	private InputStream getStoreConfiguration() {
		return getClass().getClassLoader().getResourceAsStream("objectStoreManager.xml");
	}	 
	
}
