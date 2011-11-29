package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.w3c.dom.Document;

@Ignore("It's necessary to do again all tests")
public class TestiPhoneExchange2k7 extends AbstractPushTest {

	private void decode(String fileName) throws Exception {
		InputStream in = loadDataFile(fileName);
		byte[] data = FileUtils.streamBytes(in, true);
		Document doc = wbxmlTools.toXml(data);
		DOMUtils.logDom(doc);
	}
	
	public void testProvisionRequest1() throws Exception {
		decode("iphoneProvReq1.wbxml");
	}
	public void testProvisionResponse1() throws Exception {
		decode("ex2k7provResp1.wbxml");
	}
	public void testProvisionRequest2() throws Exception {
		decode("iphoneProvReq2.wbxml");
	}
	public void testProvisionResponse2() throws Exception {
		decode("ex2k7provResp2.wbxml");
	}
	
	public void testDiffOpushExchange() throws Exception {
		decode("tom_prov_resp.wbxml");
		decode("ex_prov_resp.wbxml");
	}
}
