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
package org.obm.opush.command.sync;

import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChangesOnlyInbox;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectAllocateFolderState;
import static org.obm.opush.IntegrationTestUtils.expectContinuationTransactionLifecycle;
import static org.obm.opush.IntegrationTestUtils.expectCreateFolderMappingState;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasAddItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasDeleteItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkSyncDefaultMailFolderHasNoChange;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncClasses;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.obm.push.ContinuationService;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.DataDeltaBuilder;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.ItemChangesBuilder;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Delete;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.SyncResponse;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(SyncHandlerTestModule.class);

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
	public void testSyncDefaultMailFolderUnchange() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "1";
		int syncEmailCollectionId = 4;
		DataDelta delta = new DataDeltaBuilder().withSyncDate(new Date()).build();
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());

		checkSyncDefaultMailFolderHasNoChange(inbox, syncEmailResponse);
	}
	
	@Test
	public void testSyncOneInboxMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;

		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":0")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
			.withSyncDate(new Date()).build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		checkSyncDefaultMailFolderHasAddItems(inbox, syncEmailResponse, 
				new Add(syncEmailCollectionId + ":" + 0));
	}

	@Test
	public void testSyncTwoInboxMails() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		
		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":0")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":1")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
			.withSyncDate(new Date()).build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());

		checkSyncDefaultMailFolderHasAddItems(inbox, syncEmailResponse, 
				new Add(syncEmailCollectionId + ":" + 0),
				new Add(syncEmailCollectionId + ":" + 1));
	}

	@Test
	public void testSyncOneInboxDeletedMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		
		DataDelta delta = new DataDeltaBuilder()
			.addDeletions(
				new ItemChangesBuilder().addItemChange(
					new ItemChangeBuilder().serverId(syncEmailCollectionId + ":0")
						.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
			.withSyncDate(new Date()).build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		checkSyncDefaultMailFolderHasDeleteItems(inbox, syncEmailResponse, 
				new Delete(syncEmailCollectionId + ":" + 0));
	}

	@Test
	public void testSyncInboxOneNewOneDeletedMail() throws Exception {
		String initialSyncKey = "0";
		String syncEmailSyncKey = "13424";
		int syncEmailCollectionId = 432;
		DataDelta delta = new DataDeltaBuilder()
			.addChanges(
				new ItemChangesBuilder().addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":123")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
			.addDeletions(
				new ItemChangesBuilder().addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":122")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
			.withSyncDate(new Date()).build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId());
		
		checkSyncDefaultMailFolderHasItems(inbox, syncEmailResponse, 
				Arrays.asList(new Add(syncEmailCollectionId + ":123")),
				Arrays.asList(new Delete(syncEmailCollectionId + ":122")));
	}
	
	private FolderSyncState newSyncState(String syncEmailSyncKey) {
		return new FolderSyncState(syncEmailSyncKey);
	}
	
	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return new MSEmail.MSEmailBuilder()
			.uid(1l)
			.header(MSEmailHeader.builder().build())
			.body(new MSEmailBody(new SerializableInputStream(
					new ByteArrayInputStream(message.getBytes())), emailBodyType, 0, Charsets.UTF_8, false)).build();
	}

}
