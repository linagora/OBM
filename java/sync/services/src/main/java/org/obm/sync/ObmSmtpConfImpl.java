package org.obm.sync;

import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSmtpConfImpl implements ObmSmtpConf {
	
	private static final int SMTP_DEFAULT_PORT = 25;
	private LocatorService locatorService;
	
	@Inject
	private ObmSmtpConfImpl(LocatorService locatorService) {
		this.locatorService = locatorService;
	}

	@Override
	public int getServerPort(String domain) {
		return SMTP_DEFAULT_PORT;
	}
	
	@Override
	public String getServerAddr(String domain) throws LocatorClientException {
		return locatorService.getServiceLocation("mail/smtp_out", domain);
	}
	
}
