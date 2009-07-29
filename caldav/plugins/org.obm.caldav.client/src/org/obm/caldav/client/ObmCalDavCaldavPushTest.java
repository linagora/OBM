package org.obm.caldav.client;

import java.io.InputStream;

public class ObmCalDavCaldavPushTest extends AbstractPushTest{

	@Override
	protected InputStream getConf() {
		return getClass().getClassLoader().getResourceAsStream(
		"conf/testObmCaldav.properties");
	}

}
