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
package org.obm.opush.command.sync.folder;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChanges;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.PortNumber;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.Device;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.mail.MailBackend;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.task.TaskBackend;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.OPClient;

import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class FolderSyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(FolderSyncHandlerTestModule.class);

	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testInitialFolderSync() throws Exception {
		String initialSyncKey = "0";
		int serverId = 4;
				
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockHierarchyChanges(classToInstanceMap);
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		mockCollectionDao(collectionDao, initialSyncKey, serverId);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);
		
		replayMocks(classToInstanceMap);
		
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		Assertions.assertThat(folderSyncResponse.getStatus()).isEqualTo(1);
	}
	
	@Test
	public void testFolderSyncUnchange() throws Exception {
		String syncKey = "d58ea559-d1b8-4091-8ba5-860e6fa54875";
		int serverId = 4;
		
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		expect(calendarBackend.getHierarchyChanges(anyObject(UserDataRequest.class), anyObject(Date.class)))
				.andReturn(buildHierarchyItemsChangeEmpty()).anyTimes();
		
		TaskBackend taskBackend = classToInstanceMap.get(TaskBackend.class);
		expect(taskBackend.getHierarchyChanges(anyObject(UserDataRequest.class), anyObject(Date.class)))
				.andReturn(buildHierarchyItemsChangeEmpty()).anyTimes();
		
		ContactsBackend contactsBackend = classToInstanceMap.get(ContactsBackend.class);
		expect(contactsBackend.getHierarchyChanges(anyObject(UserDataRequest.class), anyObject(Date.class)))
				.andReturn(buildHierarchyItemsChangeEmpty()).anyTimes();
		
		MailBackend mailBackend = classToInstanceMap.get(MailBackend.class);
		expect(mailBackend.getHierarchyChanges(anyObject(UserDataRequest.class), anyObject(Date.class)))
				.andReturn(buildHierarchyItemsChangeEmpty()).anyTimes();
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		mockCollectionDao(collectionDao, syncKey, serverId);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);		
		
		replayMocks(classToInstanceMap);
		
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(syncKey);
		
		Assertions.assertThat(folderSyncResponse.getStatus()).isEqualTo(1);
		Assertions.assertThat(folderSyncResponse.getCount()).isEqualTo(0);
		Assertions.assertThat(folderSyncResponse.getFolders()).isEmpty();
	}

	private HierarchyItemsChanges buildHierarchyItemsChangeEmpty() {
		return new HierarchyItemsChanges.Builder().build();
	}
	
	private void mockItemTrackingDao(ItemTrackingDao itemTrackingDao) throws DaoException {
		itemTrackingDao.markAsSynced(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		expect(itemTrackingDao.isServerIdSynced(anyObject(SyncState.class), anyObject(ServerId.class))).andReturn(false).anyTimes();
	}

	private void mockCollectionDao(CollectionDao collectionDao, String syncKey, int serverId) throws DaoException {
		expect(collectionDao.getCollectionMapping(anyObject(Device.class), anyObject(String.class)))
				.andReturn(serverId).anyTimes();
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncState.class)))
				.andReturn((int)(Math.random()*10000)).anyTimes();
		ItemSyncState state = new ItemSyncState(syncKey);
		expect(collectionDao.findItemStateForKey(syncKey)).andReturn(state).anyTimes();
	}
}
