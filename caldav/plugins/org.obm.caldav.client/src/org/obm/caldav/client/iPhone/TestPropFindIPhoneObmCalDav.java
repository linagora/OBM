package org.obm.caldav.client.iPhone;

import java.io.InputStream;

import org.obm.caldav.client.CalendarServerPushTest;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;

public class TestPropFindIPhoneObmCalDav extends CalendarServerPushTest {
	
	public void testCalSync() throws Exception {
		InputStream in = loadDataFile("iPhonePropFind1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = propFindQuery(doc);
		assertNotNull(ret);

		DOMUtils.logDom(ret);
	}
}
