package org.obm.caldav.client;

import java.io.InputStream;

public class CalendarServerPushTest extends AbstractPushTest{

	@Override
	protected InputStream getConf() {
		return getClass().getClassLoader().getResourceAsStream(
		"conf/testCalendarServer.properties");
	}

}
