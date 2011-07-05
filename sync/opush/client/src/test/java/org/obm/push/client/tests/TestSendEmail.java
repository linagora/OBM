package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
@Ignore
public class TestSendEmail extends AbstractPushTest {

	public void testGetItemEstimate() throws Exception {
		InputStream in = loadDataFile("GetItemEstimateRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}

}
