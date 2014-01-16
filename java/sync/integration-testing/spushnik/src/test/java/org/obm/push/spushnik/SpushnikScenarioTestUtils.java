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
package org.obm.push.spushnik;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Properties;

import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.mail.MailBackend;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.task.TaskBackend;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginClient;

public class SpushnikScenarioTestUtils {

	public static void mockWorkingFolderSync(
			ClassToInstanceAgregateView<Object> classToInstanceMap, OpushUser userFixture) throws AuthFault {
		
		IntegrationTestUtils.expectUserLoginFromOpush(classToInstanceMap.get(LoginClient.class), userFixture);
		User user = userFixture.user;
		DeviceId deviceId = new DeviceId("spushnik");
		Device device = new Device(user.hashCode(), 
				"spushnikProbe", 
				deviceId, 
				new Properties(), 
				userFixture.deviceProtocolVersion);
		
		// First provisionning
		DeviceDao deviceDao = classToInstanceMap.get(DeviceDao.class);
		expect(deviceDao.getDevice(user, 
				deviceId, 
				"spushnikAgent",
				userFixture.deviceProtocolVersion))
			.andReturn(device).anyTimes();
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(null).once();
		deviceDao.removeUnknownDeviceSyncPerm(user, device);
		expectLastCall().once();
		Long policyKey = new Long(1);
		expect(deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(policyKey).once();
		
		// Second provisionning
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.PENDING))
			.andReturn(policyKey).once();
		deviceDao.removePolicyKey(user, device);
		expectLastCall().once();
		expect(deviceDao.allocateNewPolicyKey(user, deviceId, PolicyStatus.ACCEPTED))
			.andReturn(policyKey).once();
		expect(deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED))
			.andReturn(policyKey).once();
		
		// FolderSync
		SyncKey syncKey = new SyncKey("123");
		expect(classToInstanceMap.get(SyncKeyFactory.class).randomSyncKey())
			.andReturn(syncKey).once();
		FolderSyncState syncState = FolderSyncState.builder()
				.syncKey(syncKey)
				.id(1)
				.build();
		expect(classToInstanceMap.get(CollectionDao.class).allocateNewFolderSyncState(device, syncKey))
			.andReturn(syncState).once();
		FolderSyncState initialSyncState = FolderSyncState.builder()
				.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
				.id(0)
				.build();
		ContactsBackend contactsBackend = classToInstanceMap.get(ContactsBackend.class);
		expect(contactsBackend.getPIMDataType())
			.andReturn(PIMDataType.CONTACTS).once();
		expect(contactsBackend.getHierarchyChanges(anyObject(UserDataRequest.class), eq(initialSyncState), eq(syncState)))
			.andReturn(HierarchyCollectionChanges.builder()
					.build()).once();
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		expect(calendarBackend.getPIMDataType())
			.andReturn(PIMDataType.CALENDAR).once();
		expect(calendarBackend.getHierarchyChanges(anyObject(UserDataRequest.class), eq(initialSyncState), eq(syncState)))
			.andReturn(HierarchyCollectionChanges.builder()
					.build()).once();
		TaskBackend taskBackend = classToInstanceMap.get(TaskBackend.class);
		expect(taskBackend.getPIMDataType())
			.andReturn(PIMDataType.TASKS).once();
		expect(taskBackend.getHierarchyChanges(anyObject(UserDataRequest.class), eq(initialSyncState), eq(syncState)))
			.andReturn(HierarchyCollectionChanges.builder()
					.build()).once();
		MailBackend mailBackend = classToInstanceMap.get(MailBackend.class);
		expect(mailBackend.getPIMDataType())
			.andReturn(PIMDataType.EMAIL).once();
		expect(mailBackend.getHierarchyChanges(anyObject(UserDataRequest.class), eq(initialSyncState), eq(syncState)))
			.andReturn(HierarchyCollectionChanges.builder()
					.build()).once();
		classToInstanceMap.get(FolderSyncStateBackendMappingDao.class).createMapping(anyObject(PIMDataType.class), eq(syncState));
		expectLastCall().anyTimes();
	}
	
}
