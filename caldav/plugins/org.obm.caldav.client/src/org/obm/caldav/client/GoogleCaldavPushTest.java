package org.obm.caldav.client;

import java.io.InputStream;

public class GoogleCaldavPushTest extends AbstractPushTest{

	@Override
	protected InputStream getConf() {
		return getClass().getClassLoader().getResourceAsStream(
		"conf/testGoogleCalDav.properties");
	}

}
