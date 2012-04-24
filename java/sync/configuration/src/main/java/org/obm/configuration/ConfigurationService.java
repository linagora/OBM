package org.obm.configuration;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.store.StoreNotFoundException;

public interface ConfigurationService {

	String getLocatorUrl() throws ConfigurationException;

	String getObmUIBaseUrl();

	InputStream getStoreConfiguration() throws StoreNotFoundException;

	String getObmSyncUrl(String obmSyncHost);

	int getLocatorCacheTimeout();

	TimeUnit getLocatorCacheTimeUnit();

	ResourceBundle getResourceBundle(Locale locale);
	
	String getActiveSyncServletUrl();

	Charset getDefaultEncoding();

	int getTransactionTimeout();

	TimeUnit getTransactionTimeoutUnit();
	
}