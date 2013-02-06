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

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.exception.DaoException;
import org.obm.push.mail.imap.GuiceModule;
import org.obm.push.mail.imap.SlowGuiceRunner;
import org.obm.push.service.DateService;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.beans.GetItemEstimateSingleFolderResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(SlowGuiceRunner.class) @Slow
@GuiceModule(MailBackendTestModule.class)
public class MailBackendGetItemEstimateTest {

	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject GreenMail greenMail;
	@Inject ImapConnectionCounter imapConnectionCounter;
	@Inject PendingQueriesLock pendingQueries;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	
	private UnsynchronizedItemDao unsynchronizedItemDao;
	private ItemTrackingDao itemTrackingDao;
	private CollectionDao collectionDao;
	private DateService dateService;

	private ServerSetup smtpServerSetup;
	private GreenMailUser greenMailUser;
	private ImapHostManager imapHostManager;
	private OpushUser user;
	private String mailbox;
	private String inboxCollectionPath;
	private int inboxCollectionId;
	private String inboxCollectionIdAsString;

	@Before
	public void init() throws Exception {
		user = singleUserFixture.jaures;
		greenMail.start();
		smtpServerSetup = greenMail.getSmtp().getServerSetup();
		mailbox = user.user.getLoginAtDomain();
		greenMailUser = greenMail.setUser(mailbox, user.password);
		imapHostManager = greenMail.getManagers().getImapHostManager();
		imapHostManager.createMailbox(greenMailUser, "Trash");
		
		inboxCollectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(user);
		inboxCollectionId = 1234;
		inboxCollectionIdAsString = String.valueOf(inboxCollectionId);
		
		unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		collectionDao = classToInstanceMap.get(CollectionDao.class);
		dateService = classToInstanceMap.get(DateService.class);

		bindCollectionIdToPath();
	}

	private void bindCollectionIdToPath() throws Exception {
		expect(collectionDao.getCollectionPath(inboxCollectionId)).andReturn(inboxCollectionPath).anyTimes();
		
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		SyncCollection syncCollection = new SyncCollection(inboxCollectionId, inboxCollectionPath);
		expect(syncedCollectionDao.get(user.credentials, user.device, inboxCollectionId))
			.andReturn(syncCollection).anyTimes();
		
		syncedCollectionDao.put(eq(user.credentials), eq(user.device), anyObject(SyncCollection.class));
		expectLastCall().anyTimes();
	}

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testGetItemEstimateWithInvalidSyncKey() throws Exception {
		SyncKey invalidSyncKey = new SyncKey("456");
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		expect(collectionDao.findItemStateForKey(invalidSyncKey)).andReturn(null);
		expectUnsynchronizedItemToNeverHavePendingAdds();

		mocksControl.replay();
		
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		GetItemEstimateSingleFolderResponse itemEstimateResponse = opClient.getItemEstimateOnMailFolder(invalidSyncKey, inboxCollectionId);

		mocksControl.verify();
		
		assertThat(itemEstimateResponse.getStatus()).isEqualTo(GetItemEstimateStatus.INVALID_SYNC_KEY);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testGetItemEstimateNoChange() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey lastSyncKey = new SyncKey("789");
		int lastStateId = 3;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState lastSyncState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(lastSyncKey)
				.id(lastStateId)
				.build();
		
		expectInitialSyncWithTwoMails(initialSyncKey, firstAllocatedSyncKey, lastSyncKey);

		expect(dateService.getCurrentDate()).andReturn(DateUtils.getCurrentDate()).once();
		expect(collectionDao.findItemStateForKey(lastSyncKey)).andReturn(lastSyncState).once();
		expectUnsynchronizedItemToNeverHavePendingAdds();
		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();

		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);

		GetItemEstimateSingleFolderResponse itemEstimateResponse = opClient.getItemEstimateOnMailFolder(lastSyncKey, inboxCollectionId);

		mocksControl.verify();
		
		assertThat(itemEstimateResponse.getEstimate()).isEqualTo(0);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 2);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testGetItemEstimateNoChangeWithFilterTypeChanged() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey lastSyncKey = new SyncKey("789");
		int lastStateId = 3;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState lastSyncState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(lastSyncKey)
				.id(lastStateId)
				.build();
		
		expectInitialSyncWithTwoMails(initialSyncKey, firstAllocatedSyncKey, lastSyncKey);

		expect(dateService.getCurrentDate()).andReturn(DateUtils.getCurrentDate()).once();
		expect(collectionDao.findItemStateForKey(lastSyncKey)).andReturn(lastSyncState).once();
		expectUnsynchronizedItemToNeverHavePendingAdds();
		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();

		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 25);

		GetItemEstimateSingleFolderResponse itemEstimateResponse = opClient.getItemEstimateOnMailFolder(lastSyncKey, FilterType.ONE_MONTHS_BACK, inboxCollectionId);

		mocksControl.verify();
		
		assertThat(itemEstimateResponse.getStatus()).isEqualTo(GetItemEstimateStatus.INVALID_SYNC_KEY);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 2);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	@Test
	public void testGetItemEstimateWithChanges() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey firstAllocatedSyncKey = new SyncKey("456");
		SyncKey lastSyncKey = new SyncKey("789");
		int lastStateId = 3;
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		
		Date initialDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState lastSyncState = ItemSyncState.builder()
				.syncDate(initialDate)
				.syncKey(lastSyncKey)
				.id(lastStateId)
				.build();
		
		expectInitialSyncWithTwoMails(initialSyncKey, firstAllocatedSyncKey, lastSyncKey);

		expect(dateService.getCurrentDate()).andReturn(DateUtils.getCurrentDate()).once();
		expect(collectionDao.findItemStateForKey(lastSyncKey)).andReturn(lastSyncState).once();
		expectUnsynchronizedItemToNeverHavePendingAdds();
		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		mocksControl.replay();

		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		sendTwoEmailsToImapServer();
		opClient.syncEmail(initialSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);
		opClient.syncEmail(firstAllocatedSyncKey, inboxCollectionIdAsString, FilterType.THREE_DAYS_BACK, 100);

		sendTwoEmailsToImapServer();
		GetItemEstimateSingleFolderResponse itemEstimateResponse = opClient.getItemEstimateOnMailFolder(lastSyncKey, inboxCollectionId);

		mocksControl.verify();
		
		assertThat(itemEstimateResponse.getEstimate()).isEqualTo(2);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 4);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(2);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(0);
	}

	private void expectInitialSyncWithTwoMails(SyncKey initialSyncKey, SyncKey firstAllocatedSyncKey, SyncKey secondAllocatedSyncKey) throws Exception {
		int allocatedStateId = 3;
		int allocatedStateId2 = 4;
		
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
		expectCollectionDaoPerformInitialSync(initialSyncKey, firstAllocatedState);
		expectCollectionDaoPerformSync(firstAllocatedSyncKey, firstAllocatedState, allocatedState);
	}

	private void expectUnsynchronizedItemToNeverHavePendingAdds() {
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, inboxCollectionId))
				.andReturn(ImmutableList.<ItemChange>of()).anyTimes();
	}

	private void expectCollectionDaoPerformSync(SyncKey requestSyncKey, ItemSyncState allocatedState, ItemSyncState newItemSyncState)
			throws DaoException {
		expect(collectionDao.findItemStateForKey(requestSyncKey)).andReturn(allocatedState).times(2);
		expect(collectionDao.updateState(user.device, inboxCollectionId, newItemSyncState.getSyncKey(), newItemSyncState.getSyncDate()))
				.andReturn(newItemSyncState);
	}

	private void expectCollectionDaoPerformInitialSync(SyncKey initialSyncKey, ItemSyncState itemSyncState)
			throws DaoException {
		
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.updateState(user.device, inboxCollectionId, itemSyncState.getSyncKey(), itemSyncState.getSyncDate()))
			.andReturn(itemSyncState);
		collectionDao.resetCollection(user.device, inboxCollectionId);
		expectLastCall();
	}

	private void sendTwoEmailsToImapServer() throws InterruptedException {
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject", "body", smtpServerSetup);
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(2);
	}

	private void assertEmailCountInMailbox(String mailbox, Integer expectedNumberOfEmails) {
		MailFolder inboxFolder = imapHostManager.getFolder(greenMailUser, mailbox);
		Assertions.assertThat(inboxFolder.getMessageCount()).isEqualTo(expectedNumberOfEmails);
	}
}
