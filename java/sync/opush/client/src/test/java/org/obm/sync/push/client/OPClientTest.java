/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.push.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.Future;

import javax.xml.transform.TransformerException;

import org.apache.http.client.fluent.Async;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.DeviceId;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;


public class OPClientTest {
	
	private OPClient opClient;
	private CloseableHttpClient httpClient;

	@Before
	public void setUp() {
		httpClient = HttpClientBuilder.create().build();
		String loginAtDomain = "log@domain";
		String password = "pwd";
		DeviceId devId = new DeviceId("devId");
		String devType = "devType";
		String userAgent = "userAgent";
		String url = "url";
		opClient = new OPClient(httpClient, loginAtDomain, password, devId, devType, userAgent, url, ProtocolVersion.V121) {
			
			@Override
			public Document postXml(String namespace, Document doc, String cmd, String policyKey, boolean multipart) throws TransformerException,
					WBXmlException, IOException, HttpRequestException {
				throw new RuntimeException("this testing OPClient cannot performs request");
			}

			@Override
			public <T> Future<T> postASyncXml(Async async, String namespace, Document doc, String cmd, String policyKey, boolean multipart, ResponseTransformer<T> documentHandler)
					throws TransformerException, WBXmlException, IOException, HttpRequestException {
				throw new RuntimeException("this testing OPClient cannot performs request");
			}
		};
	}
	
	@After
	public void teardown() throws IOException {
		httpClient.close();
	}
	
	@Test
	public void testBuildUrlSimple() {
		String url = "url";
		String login = "login";
		DeviceId deviceId = new DeviceId("DeviceId");
		String devType = "devType";
		
		String buildUrl = opClient.buildUrl(url, login, deviceId, devType);

		assertThat(buildUrl).isEqualTo("url?User=login&DeviceId=DeviceId&DeviceType=devType");
	}
	
	@Test
	public void testBuildUrlCommand() {
		String url = "url";
		String login = "login";
		DeviceId deviceId = new DeviceId("DeviceId");
		String devType = "devType";
		String cmd = "cmd";
		
		String buildUrl = opClient.buildUrl(url, login, deviceId, devType, cmd);

		assertThat(buildUrl).isEqualTo("url?User=login&DeviceId=DeviceId&DeviceType=devType&Cmd=cmd");
	}
	
	@Test
	public void testBuildUrlExtra() {
		String url = "url";
		String login = "login";
		DeviceId deviceId = new DeviceId("DeviceId");
		String devType = "devType";
		String cmd = "cmd";
		String extra = "&AttachmentName=yeah";
		
		String buildUrl = opClient.buildUrl(url, login, deviceId, devType, cmd, extra);

		assertThat(buildUrl).isEqualTo("url?User=login&DeviceId=DeviceId&DeviceType=devType&Cmd=cmd&AttachmentName=yeah");
	}
	
}
