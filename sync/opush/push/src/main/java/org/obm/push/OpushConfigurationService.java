package org.obm.push;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.obm.configuration.ObmConfigurationService;
import org.obm.configuration.store.StoreNotFoundException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OpushConfigurationService extends ObmConfigurationService {
	
	private final ServletContext servletContext;
	
	@Inject
	private OpushConfigurationService(ServletContext servletContext) {
		super();
		this.servletContext = servletContext;
	}
	
	@Override
	public InputStream getStoreConfiguration() throws StoreNotFoundException {
		InputStream storeConfigurations = servletContext.getResourceAsStream("/WEB-INF/objectStoreManager.xml");
		if (storeConfigurations == null) {
			throw new StoreNotFoundException("/WEB-INF/objectStoreManager.xml not found !");
		}
		return storeConfigurations;
	}
	
}
