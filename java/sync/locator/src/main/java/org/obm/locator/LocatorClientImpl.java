package org.obm.locator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.commons.io.IOUtils;
import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.store.LocatorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocatorClientImpl implements LocatorService {

	private final String locatorUrl;

	@Inject
	private LocatorClientImpl(ObmConfigurationService obmConfigurationService) throws ConfigurationException {
		locatorUrl = ensureTrailingSlash( obmConfigurationService.getLocatorUrl() );
	}

	private String ensureTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url;
		}
		return url + "/";
	}
	
	@Override
	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
		String url = buildFullServiceUrl(serviceSlashProperty, loginAtDomain);
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			List<String> lines = IOUtils.readLines(is, "utf-8");
			return lines.get(0);
		} catch (MalformedURLException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private String buildFullServiceUrl(String serviceSlashProperty, String loginAtDomain) {
		return locatorUrl + "location/host/" + serviceSlashProperty + "/" + loginAtDomain;
	}
}