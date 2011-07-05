package org.obm.locator.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.locator.client.impl.XTrustProvider;

public class LocatorClient {

	private static final Log logger = LogFactory.getLog(LocatorClient.class);

	public String locateHost(String serviceSlashProperty, String loginAtDomain) {
		String url = getLocatorUrl() + "location/host/"
				+ serviceSlashProperty + "/" + loginAtDomain;
		String ip = null;

		try {
			InputStream is = new URL(url).openStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(is,
					Charset.forName("utf-8")));
			ip = r.readLine();
			r.close();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return ip;
	}
	
	private String getLocatorUrl() {
		XTrustProvider.install();
		Properties p = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("/etc/obm/obm_conf.ini");
			p.load(fis);
			return "https://" + p.getProperty("host") + ":8084/";
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (fis != null) {
					fis.close();	
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

}
