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
package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;
import static org.obm.opush.IntegrationPushTestUtils.mockNextGeneratedSyncKey;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;
import static org.obm.opush.command.sync.EmailSyncTestUtils.getCollectionWithId;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Flags.Flag;

import org.easymock.IMocksControl;
import org.fest.assertions.api.Assertions;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.Configuration;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.DaoException;
import org.obm.push.mail.imap.GuiceModule;
import org.obm.push.mail.imap.SlowGuiceRunner;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.service.DateService;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.inject.Inject;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SlowGuiceRunner.class) @Slow
@GuiceModule(MailBackendTestModule.class)
public class MailBackendGetChangedTest {

	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject GreenMail greenMail;
	@Inject ImapConnectionCounter imapConnectionCounter;
	@Inject PendingQueriesLock pendingQueries;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	
	private ItemTrackingDao itemTrackingDao;
	private CollectionDao collectionDao;
	private DateService dateService;

	private GreenMailUser greenMailUser;
	private ImapHostManager imapHostManager;
	private OpushUser user;
	private String mailbox;
	private String inboxCollectionPath;
	private int inboxCollectionId;
	private String inboxCollectionIdAsString;
	private String trashCollectionPath;
	private int trashCollectionId;
	private String trashCollectionIdAsString;

	@Before
	public void init() throws Exception {
		user = singleUserFixture.jaures;
		greenMail.start();
		mailbox = user.user.getLoginAtDomain();
		greenMailUser = greenMail.setUser(mailbox, user.password);
		imapHostManager = greenMail.getManagers().getImapHostManager();
		imapHostManager.createMailbox(greenMailUser, "Trash");

		inboxCollectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(user);
		inboxCollectionId = 1234;
		inboxCollectionIdAsString = String.valueOf(inboxCollectionId);
		trashCollectionPath = IntegrationTestUtils.buildEmailTrashCollectionPath(user);
		trashCollectionId = 1645;
		trashCollectionIdAsString = String.valueOf(trashCollectionId);
		
		itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		collectionDao = classToInstanceMap.get(CollectionDao.class);
		dateService = classToInstanceMap.get(DateService.class);

		bindCollectionIdToPath();
	}

	private void bindCollectionIdToPath() throws Exception {
		expect(collectionDao.getCollectionPath(inboxCollectionId)).andReturn(inboxCollectionPath).anyTimes();
		expect(collectionDao.getCollectionPath(trashCollectionId)).andReturn(trashCollectionPath).anyTimes();
		
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		SyncCollection syncCollection = new SyncCollection(inboxCollectionId, inboxCollectionPath);
		expect(syncedCollectionDao.get(user.credentials, user.device, inboxCollectionId))
			.andReturn(syncCollection).anyTimes();
		SyncCollection trashCollection = new SyncCollection(trashCollectionId, trashCollectionPath);
		expect(syncedCollectionDao.get(user.credentials, user.device, trashCollectionId))
			.andReturn(trashCollection).anyTimes();
		
		syncedCollectionDao.put(eq(user.credentials), eq(user.device), anyObject(SyncCollection.class));
		expectLastCall().anyTimes();
	}

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		greenMail.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testInitialGetChangedWithNoSnapshot() throws Exception {
		String emailId1 = ":1";
		String emailId2 = ":2";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState allocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(allocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, allocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		SyncResponse firstSyncResponse = opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse firstInboxResponse = getCollectionWithId(firstSyncResponse, inboxCollectionIdAsString);
		SyncCollectionResponse secondInboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		
		assertThat(firstInboxResponse.getItemChanges()).isEmpty();
		assertThat(secondInboxResponse.getItemChanges()).containsOnly(
				new ItemChangeBuilder().serverId(inboxCollectionIdAsString + emailId1).withNewFlag(true).build(),
				new ItemChangeBuilder().serverId(inboxCollectionIdAsString + emailId2).withNewFlag(true).build());

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 2);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void fetchWithNoSnapshot() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState allocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expect(collectionDao.findItemStateForKey(firstAllocatedSyncKey)).andReturn(allocatedState).times(2);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		SyncResponse firstSyncResponse = opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.syncWithCommand(firstAllocatedSyncKey, inboxCollectionIdAsString, SyncCommand.FETCH, inboxCollectionIdAsString + ":1");
		mocksControl.verify();

		SyncCollectionResponse firstInboxResponse = getCollectionWithId(firstSyncResponse, inboxCollectionIdAsString);
		
		assertThat(firstInboxResponse.getItemChanges()).isEmpty();
		assertThat(syncResponse.getStatus()).isEqualTo(SyncStatus.INVALID_SYNC_KEY);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 2);
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(0);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(0);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(0);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}
	
	@Test
	public void testInitialGetChangedWithSnapshotNoChanges() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		SyncResponse firstSyncResponse = opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse firstInboxResponse = getCollectionWithId(firstSyncResponse, inboxCollectionIdAsString);
		SyncCollectionResponse secondInboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		
		assertThat(firstInboxResponse.getItemChanges()).isEmpty();
		assertThat(secondInboxResponse.getItemChanges()).isEmpty();

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 2);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testInitialGetChangedWithSnapshotWithChanges() throws Exception {
		String emailId3 = ":3";
		String emailId4 = ":4";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		SyncResponse firstSyncResponse = opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		sendTwoEmailsToImapServer();
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse firstInboxResponse = getCollectionWithId(firstSyncResponse, inboxCollectionIdAsString);
		SyncCollectionResponse secondInboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		
		assertThat(firstInboxResponse.getItemChanges()).isEmpty();
		assertThat(secondInboxResponse.getItemChanges()).containsOnly(
				new ItemChangeBuilder().serverId(inboxCollectionIdAsString + emailId3).withNewFlag(true).build(),
				new ItemChangeBuilder().serverId(inboxCollectionIdAsString + emailId4).withNewFlag(true).build());
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 4);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}


	@Test
	public void testGetChangedWithFilterTypeChange() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expect(collectionDao.findItemStateForKey(secondAllocatedSyncKey)).andReturn(currentAllocatedState).times(2);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		sendTwoEmailsToImapServer();
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.ONE_DAY_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		
		assertThat(inboxResponse.getSyncCollection().getStatus()).isEqualTo(SyncStatus.INVALID_SYNC_KEY);
	}
	
	@Test
	public void testGetChangedWithClientDeletionReturnResponseWithDeletion() throws Exception {
		String emailId1 = ":1";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.deleteEmail(secondAllocatedSyncKey, inboxCollectionId, inboxCollectionId + emailId1);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		assertThat(inboxResponse.getItemChanges()).isEmpty();
		assertThat(inboxResponse.getItemChangesDeletion()).containsOnly(
				ItemDeletion.builder().serverId("1234:1").build());
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
	}
	
	@Test(expected=AssertionError.class)
	public void testGetChangedDoesnotReturnDeleteAskByClient() throws Exception {
		String emailId1 = ":1";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.deleteEmail(secondAllocatedSyncKey, inboxCollectionId, inboxCollectionId + emailId1);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		assertThat(inboxResponse.getItemChanges()).isEmpty();
		assertThat(inboxResponse.getItemChangesDeletion()).isEmpty();
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
	}

	@Test
	public void testGetChangedOnTrashReturnsPreviousClientDeletion() throws Exception {
		String emailId1 = ":1";
		String trashEmailId = ":1";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey thirdAllocatedSyncKey = new SyncKey("1012");
		SyncKey firstAllocatedSyncKeyTrash = new SyncKey("1156");
		SyncKey secondAllocatedSyncKeyTrash = new SyncKey("1241");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int allocatedStateId3 = 5;
		int allocatedStateId4 = 6;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey,
				thirdAllocatedSyncKey, firstAllocatedSyncKeyTrash, secondAllocatedSyncKeyTrash);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState secondAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState thirdAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(thirdAllocatedSyncKey)
				.id(allocatedStateId3)
				.build();
		ItemSyncState firstAllocatedStateTrash = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKeyTrash)
				.id(allocatedStateId4)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).anyTimes();
		expect(dateService.getCurrentDate()).andReturn(secondAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(thirdAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(firstAllocatedStateTrash.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, secondAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, secondAllocatedState, thirdAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedStateTrash, trashCollectionId);
		expect(collectionDao.findItemStateForKey(secondAllocatedSyncKeyTrash)).andReturn(firstAllocatedStateTrash).times(2);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.deleteEmail(secondAllocatedSyncKey, inboxCollectionId, inboxCollectionId + emailId1);
		opClient.syncEmail(initialSyncKey, trashCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKeyTrash, trashCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, trashCollectionIdAsString);
		assertThat(inboxResponse.getItemChangesDeletion()).isEmpty();
		assertThat(inboxResponse.getItemChanges()).containsOnly(
				new ItemChangeBuilder().serverId(trashCollectionId + trashEmailId).withNewFlag(true).build());
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
	}
	
	@Test
	public void testGetChangedWithReadFlag() throws Exception {
		String emailId = "1";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", greenMail.getSmtp().getServerSetup());
		greenMail.waitForIncomingEmail(1);
		
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		
		MailFolder folder = imapHostManager.getFolder(greenMailUser, EmailConfiguration.IMAP_INBOX_NAME);
		folder.setFlags(new Flags(Flag.SEEN), true, 1, null, true);
		
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		assertThat(inboxResponse.getItemChanges()).containsOnly(
				new ItemChangeBuilder().serverId(inboxCollectionId + ":" + emailId).withNewFlag(false).build());
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testGetChangedWithDeletedFlag() throws Exception {
		String emailId = "1";
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T18:17:26"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expect(dateService.getCurrentDate()).andReturn(newAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", greenMail.getSmtp().getServerSetup());
		greenMail.waitForIncomingEmail(1);
		
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		
		MailFolder folder = imapHostManager.getFolder(greenMailUser, EmailConfiguration.IMAP_INBOX_NAME);
		folder.setFlags(new Flags(Flag.DELETED), true, 1, null, true);
		
		SyncResponse syncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		mocksControl.verify();

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, inboxCollectionIdAsString);
		assertThat(inboxResponse.getItemChangesDeletion()).containsOnly(
				ItemDeletion.builder().serverId(inboxCollectionId + ":" + emailId).build());
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testGetChangedWithWindowsSize() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey thirdAllocatedSyncKey = new SyncKey("321");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int windowSize = 3;
		int numberOfEmails = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, thirdAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate()).once();
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expect(collectionDao.findItemStateForKey(secondAllocatedSyncKey)).andReturn(currentAllocatedState).times(2);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();

		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendNEmailsToImapServer(numberOfEmails);
		
		SyncResponse initialSyncResponse = opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, windowSize);
		SyncCollectionResponse initialInboxResponse = getCollectionWithId(initialSyncResponse, inboxCollectionIdAsString);
		assertThat(initialInboxResponse.getAllocateNewSyncKey()).isEqualTo(firstAllocatedSyncKey);
		
		SyncResponse firstPartSyncResponse = opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, windowSize);
		SyncCollectionResponse inboxResponse = getCollectionWithId(firstPartSyncResponse, inboxCollectionIdAsString);
		assertThat(inboxResponse.getItemChanges()).hasSize(windowSize);
		assertThat(inboxResponse.getAllocateNewSyncKey()).isEqualTo(secondAllocatedSyncKey);
		
		SyncResponse lastPartSyncResponse = opClient.syncEmail(secondAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, windowSize);
		mocksControl.verify();
		
		SyncCollectionResponse lastInboxResponse = getCollectionWithId(lastPartSyncResponse, inboxCollectionIdAsString);
		assertThat(lastInboxResponse.getItemChanges()).hasSize(numberOfEmails - windowSize);
		assertThat(lastInboxResponse.getAllocateNewSyncKey()).isEqualTo(secondAllocatedSyncKey);
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, numberOfEmails);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testFetchCommandGenerateSyncKey() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey secondAllocatedSyncKey = new SyncKey("789");
		SyncKey newAllocatedSyncKey = new SyncKey("1012");
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		int newAllocatedStateId = 5;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockNextGeneratedSyncKey(classToInstanceMap, firstAllocatedSyncKey, secondAllocatedSyncKey, newAllocatedSyncKey);
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstAllocatedState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(firstAllocatedSyncKey)
				.id(allocatedStateId)
				.build();
		ItemSyncState currentAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(secondAllocatedSyncKey)
				.id(allocatedStateId2)
				.build();
		ItemSyncState newAllocatedState = ItemSyncState.builder()
				.syncDate(date("2012-10-10T16:22:53"))
				.syncKey(newAllocatedSyncKey)
				.id(newAllocatedStateId)
				.build();
		expect(dateService.getEpochPlusOneSecondDate()).andReturn(initialDate).times(2);
		expect(dateService.getCurrentDate()).andReturn(currentAllocatedState.getSyncDate());
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, currentAllocatedState, inboxCollectionId);
		expectCollectionDaoPerformSync(secondAllocatedSyncKey, currentAllocatedState, newAllocatedState, inboxCollectionId);

		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", greenMail.getSmtp().getServerSetup());
		greenMail.waitForIncomingEmail(1);
		
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);

		String serverId = inboxCollectionIdAsString + ":1";
		SyncResponse syncResponseWithFetch = opClient.syncWithCommand(
				secondAllocatedSyncKey, inboxCollectionIdAsString, SyncCommand.FETCH, serverId);
		
		mocksControl.verify();

		SyncCollectionResponse lastInboxResponse = getCollectionWithId(syncResponseWithFetch, inboxCollectionIdAsString);
		assertThat(lastInboxResponse.getAllocateNewSyncKey()).isEqualTo(newAllocatedSyncKey);
		
		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	private void expectCollectionDaoPerformSync(SyncKey requestSyncKey,
			ItemSyncState allocatedState, ItemSyncState newItemSyncState, int collectionId)
					throws DaoException {
		expect(collectionDao.findItemStateForKey(requestSyncKey)).andReturn(allocatedState).times(2);
		expect(collectionDao.updateState(user.device, collectionId, newItemSyncState.getSyncKey(), newItemSyncState.getSyncDate()))
				.andReturn(newItemSyncState);
	}

	private void expectCollectionDaoPerformInitialSync(SyncKey initialSyncKey,
			ItemSyncState itemSyncState, int collectionId)
					throws DaoException {
		
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.updateState(user.device, collectionId, itemSyncState.getSyncKey(), itemSyncState.getSyncDate()))
			.andReturn(itemSyncState);
		collectionDao.resetCollection(user.device, collectionId);
		expectLastCall();
	}

	private void sendTwoEmailsToImapServer() throws InterruptedException {
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject", "body", greenMail.getSmtp().getServerSetup());
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", greenMail.getSmtp().getServerSetup());
		greenMail.waitForIncomingEmail(2);
	}

	private void sendNEmailsToImapServer(int numberOfEmails) throws InterruptedException {
		for (int i = 0; i< numberOfEmails; i++) {
			GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject" + i, "body", greenMail.getSmtp().getServerSetup());
		}
		greenMail.waitForIncomingEmail(numberOfEmails);
	}

	private void assertEmailCountInMailbox(String mailbox, Integer expectedNumberOfEmails) {
		MailFolder inboxFolder = imapHostManager.getFolder(greenMailUser, mailbox);
		Assertions.assertThat(inboxFolder.getMessageCount()).isEqualTo(expectedNumberOfEmails);
	}
}
