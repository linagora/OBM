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
package org.obm.opush.env.arquillian;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.obm.ConfigurationModule.PolicyConfigurationProvider;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.login.LoginClient;

public class AuthenticatedArquillianModule  extends ArquillianOpushModule {
	
	private static DeviceId deviceId;
	protected static Device device;
	protected static User user;
	private static String userId;
	protected static String password;
	private static String domainId;

	public AuthenticatedArquillianModule() {
		super();
		deviceId = new DeviceId("ApplDLXH7601DVD3");
		device = new Device(1, "iPad", deviceId, null, ProtocolVersion.V121);
		userId = new String("user");
		password = new String("pass");
		domainId = new String("obm.org");
		user = User.Factory.create().createUser(domainId + "\\" + userId, null, null);
	}
	
	@Override
	protected void expectedBehaviour() throws Exception {
		expectLoginClient();
		expectPolicyConfiguration();
	}
	
	private void expectLoginClient() throws Exception {
		LoginClient loginClient = mockMap.get(LoginClient.class);
		expect(loginClient.authenticate(userId + "@" + domainId, password))
			.andReturn(new AccessToken(1, userId));
		
		loginClient.logout(anyObject(AccessToken.class));
		expectLastCall();
		
		DeviceDao deviceDao = mockMap.get(DeviceDao.class);
		expect(deviceDao.getDevice(eq(user), eq(deviceId), anyObject(String.class), eq(ProtocolVersion.V121)))
			.andReturn(device).anyTimes();
		
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED))
			.andReturn(Long.valueOf(1));
	}

	private void expectPolicyConfiguration() {
		PolicyConfigurationProvider policyConfigurationProvider = mockMap.get(PolicyConfigurationProvider.class); 
		expect(policyConfigurationProvider.get()).andReturn("fakeConfig");
	}

	public static HttpRequestBase post(URL baseURL, String command) {
		HttpPost httpPost = new HttpPost(baseURL.toExternalForm() + "ActiveSyncServlet/?" +
				"User=" + userId + 
				"&DeviceId=" + deviceId.getDeviceId() + 
				"&DeviceType=" + device.getDevType() + 
				"&Cmd=" + command);
		httpPost.addHeader(new BasicHeader("Authorization", "Basic " + Base64.encodeBase64String(new String(domainId + "\\" + userId + ":" + password).getBytes())));
		httpPost.addHeader(new BasicHeader("MS-ASProtocolVersion", "12.1"));
		return httpPost;
	}
}
