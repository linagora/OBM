package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
@Ignore
public class TestCloneDom extends AbstractPushTest {

	public void testCloneDom() throws Exception {
		InputStream in = loadDataFile("FullSyncCalAdd.xml");
		Document doc = DOMUtils.parse(in);
		Document clone = DOMUtils.cloneDOM(doc);
		assertNotNull(clone);
		DOMUtils.logDom(clone);
	}
	
}
