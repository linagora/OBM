package org.obm.sync;

import org.obm.locator.LocatorClientException;

public interface ObmSmtpConf {

	int getServerPort(String domain);

	String getServerAddr(String domain) throws LocatorClientException;

}