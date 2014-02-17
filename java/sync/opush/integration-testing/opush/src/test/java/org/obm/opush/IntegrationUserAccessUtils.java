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
package org.obm.opush;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Collection;
import java.util.Properties;

import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.bean.Device;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginClient;

public class IntegrationUserAccessUtils {

	public static void mockUsersAccess(ClassToInstanceAgregateView<Object> classToInstanceMap,
			Collection<OpushUser> users) throws DaoException, AuthFault {
		LoginClient loginClient = classToInstanceMap.get(LoginClient.class);
		expectUserLoginFromOpush(loginClient, users);
		
		DeviceDao deviceDao = classToInstanceMap.get(DeviceDao.class);
		expectUserDeviceAccess(deviceDao, users);
	}
	
	public static void expectUserLoginFromOpush(LoginClient loginClient, Collection<OpushUser> users) throws AuthFault {
		for (OpushUser user : users) {
			expectUserLoginFromOpush(loginClient, user);
		}
	}

	public static void expectUserLoginFromOpush(LoginClient loginClient, OpushUser user) throws AuthFault {
		expect(loginClient.login(user.user.getLoginAtDomain(), user.password)).andReturn(user.accessToken).anyTimes();
		loginClient.logout(user.accessToken);
		expectLastCall().anyTimes();
		expect(loginClient.authenticate(user.user.getLoginAtDomain(), user.password)).andReturn(user.accessToken).anyTimes();
	}


	public static void expectUserDeviceAccess(DeviceDao deviceDao, Collection<OpushUser> users) throws DaoException {
		for (OpushUser user : users) {
			expectUserDeviceAccess(deviceDao, user);
		}
	}
	
	public static void expectUserDeviceAccess(DeviceDao deviceDao, OpushUser user) throws DaoException {
		expect(deviceDao.getDevice(user.user, 
				user.deviceId, 
				user.userAgent,
				user.deviceProtocolVersion))
				.andReturn(
						new Device(user.device.getDatabaseId(), user.deviceType, user.deviceId, new Properties(), user.deviceProtocolVersion))
						.anyTimes();
		expect(deviceDao.getPolicyKey(user.user, user.deviceId, PolicyStatus.ACCEPTED)).andReturn(0l).anyTimes();
	}
}
