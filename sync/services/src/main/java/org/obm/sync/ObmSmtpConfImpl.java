package org.obm.sync;

import javax.naming.ConfigurationException;

import org.obm.locator.LocatorClient;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.services.constant.ConstantService;

@Singleton
public class ObmSmtpConfImpl implements ObmSmtpConf {
	
	private static final int SMTP_DEFAULT_PORT = 25;
	private LocatorClient locatorClient;
	
	@Inject
	private ObmSmtpConfImpl(ConstantService constantService) throws ConfigurationException {
		String locatorUrl = constantService.getLocatorUrl();
		locatorClient = new LocatorClient(locatorUrl);
	}
	
	@Override
	public int getServerPort(String domain) {
		return SMTP_DEFAULT_PORT;
	}
	
	@Override
	public String getServerAddr(String domain) {
		return locatorClient.getServiceLocation("mail/smtp_out", domain);
	}
}
