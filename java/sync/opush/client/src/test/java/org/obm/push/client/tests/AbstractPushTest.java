/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.obm.push.bean.DeviceId;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.WBXMLOPClient;
import org.obm.sync.push.client.beans.ProtocolVersion;
import org.w3c.dom.Document;

@Ignore("It's necessary to do again all tests")
public class AbstractPushTest {

	protected OPClient opc;
	protected WBXMLTools wbxmlTools;

	protected AbstractPushTest() {
	}

	// "POST /Microsoft-Server-ActiveSync?User=thomas@zz.com&DeviceId=Appl87837L1XY7H&DeviceType=iPhone&Cmd=Sync HTTP/1.1"

	private String p(Properties p, String k) {
		return p.getProperty(k);
	}

	@Before
	protected void setUp() throws Exception {

		InputStream in = loadDataFile("test.properties.sample");
		Properties p = new Properties();
		p.load(in);
		in.close();

		String login = p(p, "login");
		String password = p(p, "password");
		String devId = p(p, "devId");
		String devType = p(p, "devType");
		String userAgent = p(p, "userAgent");

		wbxmlTools = new WBXMLTools();
		opc = new WBXMLOPClient(login, password, new DeviceId(devId), devType, userAgent, "localhost", 9142, "/ActiveSyncServlet/", wbxmlTools);
	}

	@After
	protected void tearDown() {
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
		return opc.postXml(namespace, doc, cmd, null, false);
	}

	public Document postMultipartXml(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V121);
		return opc.postXml(namespace, doc, cmd, null, true);
	}

	public Document postXml25(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V25);
		return opc.postXml(namespace, doc, cmd, null, false);
	}

	public Document postXml120(String namespace, Document doc, String cmd)
			throws Exception {
		opc.setProtocolVersion(ProtocolVersion.V120);
		return opc.postXml(namespace, doc, cmd, null, false);
	}

	public byte[] postGetAttachment(String attachmentName) throws Exception {
		return opc.postGetAttachment(attachmentName);
	}

}
