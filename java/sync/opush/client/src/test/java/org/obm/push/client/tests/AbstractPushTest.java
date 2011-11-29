package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.ProtocolVersion;
import org.w3c.dom.Document;

@Ignore("It's necessary to do again all tests")
public class AbstractPushTest extends TestCase {

	protected OPClient opc;
	protected WBXMLTools wbxmlTools;

	protected AbstractPushTest() {
	}

	// "POST /Microsoft-Server-ActiveSync?User=thomas@zz.com&DeviceId=Appl87837L1XY7H&DeviceType=iPhone&Cmd=Sync HTTP/1.1"

	private String p(Properties p, String k) {
		return p.getProperty(k);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		InputStream in = loadDataFile("test.properties.sample");
		Properties p = new Properties();
		p.load(in);
		in.close();

		String login = p(p, "login");
		String password = p(p, "password");
		String devId = p(p, "devId");
		String devType = p(p, "devType");
		String userAgent = p(p, "userAgent");
		String url = p(p, "url");

		wbxmlTools = new WBXMLTools();
		
		opc = new OPClient(login, password, devId, devType, userAgent, url, wbxmlTools);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		opc.destroy();
		opc = null;
	}

	protected InputStream loadDataFile(String name) {
		return AbstractPushTest.class.getClassLoader().getResourceAsStream(
				"data/" + name);
	}

	public void optionsQuery() throws Exception {
		opc.options();
	}

	public Document postXml(String namespace, Document doc, String cmd,
			String policyKey, String pv, boolean multipart) throws Exception {
		if ("2.5".equals(pv)) {
			opc.setProtocolVersion(ProtocolVersion.V25);
		} else if ("12.0".equals(pv)) {
			opc.setProtocolVersion(ProtocolVersion.V120);
		} else {
			opc.setProtocolVersion(ProtocolVersion.V121);
		}
		return opc.postXml(namespace, doc, cmd, policyKey, multipart);
	}

	public Document postXml(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V121);
		return opc.postXml(namespace, doc, cmd);
	}

	public Document postMultipartXml(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V121);
		return opc.postXml(namespace, doc, cmd, null, true);
	}

	public Document postXml25(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V25);
		return opc.postXml(namespace, doc, cmd);
	}

	public Document postXml120(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V120);
		return opc.postXml(namespace, doc, cmd);
	}

	public byte[] postGetAttachment(String attachmentName) throws Exception {
		return opc.postGetAttachment(attachmentName);
	}

}
