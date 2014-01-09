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
package org.obm.push;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.service.DeviceService;


public class SessionServiceTest {

	private User user;
	private Credentials credentials;
	private ActiveSyncRequest activeSyncRequest;
	private DeviceId deviceId;
	private String command;
	private Device device;
	private ProtocolVersion protocolVersion;
	private UserDataRequest userDataRequest;

	@Before
	public void setup() {
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		credentials = new Credentials(user, "test");
		deviceId = new DeviceId("devId");
		command = "autodiscover";
		protocolVersion = ProtocolVersion.V121;
		device = new Device(1, "devType", deviceId, new Properties(), protocolVersion);
		bindActiveSyncRequest();
	}
	
	public void bindActiveSyncRequest() {
		activeSyncRequest = createMock(ActiveSyncRequest.class);
		expect(activeSyncRequest.getUserAgent()).andReturn(user.getDisplayName()).anyTimes();
		expect(activeSyncRequest.getDeviceId()).andReturn(deviceId).anyTimes();
		expect(activeSyncRequest.getCommand()).andReturn(command).anyTimes();
		expect(activeSyncRequest.getMSASProtocolVersion()).andReturn(null).anyTimes();
		replay(activeSyncRequest);
	}
	
	@Test
	public void testEnsureThatGetSessionDontCallCloseResources() throws DaoException {
		userDataRequest = createMock(UserDataRequest.class);
		replay(userDataRequest);
		
		SessionService sessionService = createSessionService();
		 
		UserDataRequest userDataRequest = sessionService.getSession(credentials, deviceId, activeSyncRequest);
		assertThat(userDataRequest).isNotNull();
	}
	
	private SessionService createSessionService() throws DaoException {
		UserDataRequest.Factory userDataRequestFactory = createMock(UserDataRequest.Factory.class);
		expect(userDataRequestFactory.createUserDataRequest(eq(credentials), eq(command), eq(device)))
			.andReturn(userDataRequest);
		
		DeviceService deviceService = createMock(DeviceService.class);
		expect(deviceService.getDevice(user, deviceId, user.getDisplayName(), protocolVersion))
			.andReturn(device);
		
		replay(userDataRequestFactory, deviceService);
		
		return new SessionService(deviceService, userDataRequestFactory);
	}
}
