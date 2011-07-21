package org.obm.push;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.store.StoreNotFoundException;

public class StoreManagerConfigurationTest {

	protected ConfigurationService initConfigurationServiceMock() throws FileNotFoundException, StoreNotFoundException {
		ConfigurationService configurationService = EasyMock.createMock(OpushConfigurationService.class);
		EasyMock.expect(configurationService.getStoreConfiguration()).andReturn( getStoreConfiguration() );
		EasyMock.replay(configurationService);
		
		return configurationService;
	}

	private InputStream getStoreConfiguration() throws FileNotFoundException {
		String absolutePath = new File("src/main/webapp/WEB-INF/objectStoreManager.xml").getAbsolutePath();
		return new FileInputStream(absolutePath);
	}	 
	
}
