package org.obm.locator;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocatorClient {

	private static final Log logger = LogFactory.getLog(LocatorClient.class);
	private final String locatorUrl;

	public LocatorClient(String LocatorUrl) {
		locatorUrl = ensureTrailingSlash(LocatorUrl);
	}

	private String ensureTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url;
		}
		return url + "/";
	}

	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) {
		String url = buildFullServiceUrl(serviceSlashProperty, loginAtDomain);
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			List<String> lines = IOUtils.readLines(is, "utf-8");
			return lines.get(0);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private String buildFullServiceUrl(String serviceSlashProperty, String loginAtDomain) {
		return locatorUrl + "location/host/" + serviceSlashProperty + "/" + loginAtDomain;
	}
}
