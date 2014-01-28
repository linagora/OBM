/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.dao.testsuite;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;

import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class DeviceDaoTest {

	@Inject protected DeviceDao deviceDao;
	
	private User user;

	@Before
	public void setUp() {
		user = Factory.create().createUser("user@domain.org", "user@domain.org", "displayName");
	}
	
	@Test
	public void testRegisterNewDevice() {
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
	}
	
	@Test
	public void testRegisterSameDeviceManyTimes() {
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
	}
	
	@Test
	public void testGetDevice() {
		DeviceId deviceId = new DeviceId("devId");
		String deviceType = "devType";
		deviceDao.registerNewDevice(user, deviceId, deviceType);
		
		assertThat(deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121))
			.isEqualTo(new Device(1, deviceType, deviceId, new Properties(), ProtocolVersion.V121));
		assertThat(deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V120))
			.isEqualTo(new Device(1, deviceType, deviceId, new Properties(), ProtocolVersion.V120));
	}
	
	@Test
	public void testGetDeviceWrongUser() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");

		User wrongUser = Factory.create().createUser("other@domain.org", "other@domain.org", "displayName");
		assertThat(deviceDao.getDevice(wrongUser, deviceId, "userAgent", ProtocolVersion.V121)).isNull();
	}
	
	@Test
	public void testGetDeviceWrongDeviceId() {
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
		assertThat(deviceDao.getDevice(user, new DeviceId("otherId"), "userAgent", ProtocolVersion.V121)).isNull();
	}
	
	@Test(expected=DaoException.class)
	public void testAllocateNewPolicyKeyForNonRegisteredDevice() {
		deviceDao.allocateNewPolicyKey(user, new DeviceId("devId"), PolicyStatus.ACCEPTED);
	}

	@Test(expected=DaoException.class)
	public void testAllocateNewPolicyKeyForNonRegisteredUser() {
		deviceDao.registerNewDevice(user, new DeviceId("devId"), "devType");
		User otherUser = Factory.create().createUser("other@domain.org", "other@domain.org", "displayName");
		deviceDao.allocateNewPolicyKey(otherUser, new DeviceId("devId"), PolicyStatus.ACCEPTED);
	}
	
	@Test
	public void testAllocateNewPolicyKey() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyIdPending = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		long policyIdAccept = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isEqualTo(policyIdPending);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyIdAccept);
	}
	
	@Test
	public void testSyncAuthorizedWhenPendingPolicy() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		assertThat(deviceDao.syncAuthorized(user, deviceId)).isTrue();
	}
	
	@Test
	public void testSyncAuthorizedWhenAcceptedPolicy() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		assertThat(deviceDao.syncAuthorized(user, deviceId)).isTrue();
	}
	
	@Test
	public void testSyncAuthorizedWhenWrongDeviceId() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		assertThat(deviceDao.syncAuthorized(user, new DeviceId("otherDevId"))).isFalse();
	}
	
	@Test
	public void testSyncAuthorizedWhenWrongUser() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		User otherUser = Factory.create().createUser("other@domain.org", "other@domain.org", "displayName");
		assertThat(deviceDao.syncAuthorized(otherUser, deviceId)).isFalse();
	}
	
	@Test
	public void testGetPolicyKeyWhenNone() {
		assertThat(deviceDao.getPolicyKey(user, new DeviceId("devId"), PolicyStatus.PENDING)).isNull();
		assertThat(deviceDao.getPolicyKey(user, new DeviceId("devId"), PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testGetPolicyKeyWithPending() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isEqualTo(policyId);
	}
	
	@Test
	public void testGetPolicyKeyWithAccepted() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
	}
	
	@Test
	public void testGetPolicyKeyWithOtherPolicyStatus() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testGetPolicyKeyWithOtherUser() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		User otherUser = Factory.create().createUser("other@domain.org", "other@domain.org", "displayName");
		assertThat(deviceDao.getPolicyKey(otherUser, deviceId, PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testGetPolicyKeyWithOtherDeviceId() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		assertThat(deviceDao.getPolicyKey(user, new DeviceId("otherDevId"), PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testRemovePolicyKeyWhenPending() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isEqualTo(policyId);
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isNull();
	}
	
	@Test
	public void testRemovePolicyKeyWhenAccepted() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testRemoveSamePolicyKeyTwiceDoesNotTriggerException() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
		
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testRemovePolicyKeyWhenNoneDoesNotTriggerException() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");

		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
	}
	
	@Test
	public void testRemovePolicyKeyWhenWrongUser() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);

		User otherUser = Factory.create().createUser("other@domain.org", "other@domain.org", "displayName");
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
		deviceDao.removePolicyKey(otherUser, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
	}
	
	@Test
	public void testRemovePolicyKeyWhenWrongDevice() {
		DeviceId deviceId = new DeviceId("devId");
		DeviceId deviceId2 = new DeviceId("devId2");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		deviceDao.registerNewDevice(user, deviceId2, "devType");
		long policyId = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);

		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId2, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyId);
	}
	
	@Test
	public void testRemovePolicyKeyWhenDifferentEntries() {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		long policyIdPending = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING);
		long policyIdAccepted = deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);

		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isEqualTo(policyIdPending);
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isEqualTo(policyIdAccepted);
		deviceDao.removePolicyKey(user, deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121));
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING)).isNull();
		assertThat(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED)).isNull();
	}
	
	@Test
	public void testRemoveUnknownDeviceSyncPerm() throws Exception {
		DeviceId deviceId = new DeviceId("devId");
		deviceDao.registerNewDevice(user, deviceId, "devType");
		Device device = deviceDao.getDevice(user, deviceId, "userAgent", ProtocolVersion.V121);
		
		PolicyStatus policyStatus = PolicyStatus.PENDING;
		createUnknownDeviceSyncPerm(user, device, policyStatus);
		assertThat(userHasUnknownDeviceSyncPerm(user)).isTrue();
		deviceDao.removeUnknownDeviceSyncPerm(user, device);
		assertThat(userHasUnknownDeviceSyncPerm(user)).isFalse();
	}

	protected abstract void createUnknownDeviceSyncPerm(User user, Device device, PolicyStatus policyStatus) throws Exception;
	protected abstract boolean userHasUnknownDeviceSyncPerm(User user) throws Exception;
	
}
