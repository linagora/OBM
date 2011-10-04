package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
@Ignore
public class TestSettings extends AbstractPushTest {

	public void testSettingsSet() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SettingsRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Settings", doc, "Settings");
		assertNotNull(ret);
	}

	public void testSettingsGet() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("SettingsGet.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Settings", doc, "Settings");
		assertNotNull(ret);
	}
}
