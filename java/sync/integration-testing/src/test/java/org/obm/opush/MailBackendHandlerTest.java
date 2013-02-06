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
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.io.ByteArrayInputStream;
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
import org.obm.locator.store.LocatorService;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.Configuration;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemChangesBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.mail.imap.GuiceModule;
import org.obm.push.mail.imap.SlowGuiceRunner;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(SlowGuiceRunner.class) @Slow
@GuiceModule(MailBackendHandlerTestModule.class)
public class MailBackendHandlerTest {

	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject LocatorService locatorService;
	@Inject GreenMail greenMail;
	@Inject CollectionPathHelper collectionPathHelper;
	@Inject ImapConnectionCounter imapConnectionCounter;
	@Inject PendingQueriesLock pendingQueries;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	
	private ServerSetup smtpServerSetup;
	private String mailbox;
	private GreenMailUser greenMailUser;
	private ImapHostManager imapHostManager;
	private OpushUser user;

	@Before
	public void init() throws AuthorizationException, FolderException {
		user = singleUserFixture.jaures;
		greenMail.start();
		smtpServerSetup = greenMail.getSmtp().getServerSetup();
		mailbox = singleUserFixture.jaures.user.getLoginAtDomain();
		greenMailUser = greenMail.setUser(mailbox, singleUserFixture.jaures.password);
		imapHostManager = greenMail.getManagers().getImapHostManager();
		imapHostManager.createMailbox(greenMailUser, "Trash");
	}

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testDeleteMail() throws Exception {
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int serverId = 1234;
		String syncEmailId = ":2";
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getCurrentDate())
				.syncKey(new SyncKey("sync state"))
				.build();
		DataDelta delta = DataDelta.builder()
			.changes(new ItemChangesBuilder()
				.addItemChange(new ItemChangeBuilder()
					.serverId(serverId + syncEmailId)
					.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
				.build())
			.syncDate(new Date())
			.syncKey(syncState.getSyncKey())
			.build();
		
		mockUsersAccess(classToInstanceMap, Arrays.asList(user));
		mockDao(serverId, syncState);
		
		bindCollectionIdToPath(serverId);
		bindChangedToDelta(delta);
		
		mocksControl.replay();
		opushServer.start();

		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject", "body", smtpServerSetup);
		GreenMailUtil.sendTextEmail(mailbox, mailbox, "subject2", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(2);

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.deleteEmail(syncEmailSyncKey, serverId, serverId + syncEmailId);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
		assertEmailCountInMailbox(EmailConfiguration.IMAP_TRASH_NAME, 1);
		assertThat(pendingQueries.waitingClose(10, TimeUnit.SECONDS)).isTrue();
		assertThat(imapConnectionCounter.loginCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.closeCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.selectCounter.get()).isEqualTo(1);
		assertThat(imapConnectionCounter.listMailboxesCounter.get()).isEqualTo(1);
	}

	private void bindCollectionIdToPath(int syncEmailCollectionId) {
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		SyncCollection syncCollection = new SyncCollection(syncEmailCollectionId, IntegrationTestUtils.buildEmailInboxCollectionPath(singleUserFixture.jaures));
		expect(syncedCollectionDao.get(user.credentials, user.device, syncEmailCollectionId))
			.andReturn(syncCollection).anyTimes();
		
		syncedCollectionDao.put(eq(user.credentials), eq(user.device), anyObject(SyncCollection.class));
		expectLastCall().anyTimes();
	}

	private void bindChangedToDelta(DataDelta delta) throws Exception {
		IContentsExporter contentsExporter = classToInstanceMap.get(IContentsExporter.class);
		expect(contentsExporter.getChanged(
				anyObject(UserDataRequest.class), 
				anyObject(SyncCollection.class), 
				anyObject(SyncClientCommands.class),
				anyObject(SyncKey.class)))
			.andReturn(delta).once();
	}

	private void mockDao(int serverId, ItemSyncState syncState) throws Exception {
		mockUnsynchronizedItemDao(serverId);
		mockSyncedCollectionDaoToReturnSyncCollection(serverId);
		mockCollectionDao(serverId, syncState);
		mockItemTrackingDao();
	}
	
	private void mockSyncedCollectionDaoToReturnSyncCollection(int serverId) {
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(user.credentials, user.device, serverId)).andReturn(new SyncCollection());
	}

	private void mockUnsynchronizedItemDao(int serverId) {
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, serverId))
			.andReturn(ImmutableSet.<ItemChange> of()).anyTimes();
		
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, serverId);
		expectLastCall().anyTimes();
		
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, serverId))
			.andReturn(ImmutableSet.<ItemDeletion>of()).anyTimes();
		
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, serverId);
		expectLastCall().anyTimes();
		
		unsynchronizedItemDao.storeItemsToRemove(user.credentials, user.device, serverId, Lists.<ItemDeletion>newArrayList());
		expectLastCall().anyTimes();
	}
	
	private void mockCollectionDao(int serverId, ItemSyncState syncState) throws Exception {
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(serverId))
			.andReturn(IntegrationTestUtils.buildEmailInboxCollectionPath(singleUserFixture.jaures)).anyTimes();
		
		expect(collectionDao.findItemStateForKey(anyObject(SyncKey.class)))
			.andReturn(syncState).anyTimes();
		
		expect(collectionDao.updateState(eq(user.device), eq(serverId), anyObject(SyncKey.class), anyObject(Date.class)))
			.andReturn(syncState).anyTimes();
		
		expect(collectionDao.getCollectionMapping(eq(user.device), anyObject(String.class)))
			.andReturn(serverId).anyTimes();
		
		IntegrationTestUtils.expectUserCollectionsNeverChange(collectionDao, Sets.newHashSet(singleUserFixture.jaures), ImmutableList.of(serverId));
	}

	private void mockItemTrackingDao() throws Exception {
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		itemTrackingDao.markAsDeleted(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		expect(itemTrackingDao.isServerIdSynced(anyObject(ItemSyncState.class), anyObject(ServerId.class)))
			.andReturn(false).anyTimes();
	}
	
	private void assertEmailCountInMailbox(String mailbox, Integer expectedNumberOfEmails) {
		MailFolder inboxFolder = imapHostManager.getFolder(greenMailUser, mailbox);
		Assertions.assertThat(inboxFolder.getMessageCount()).isEqualTo(expectedNumberOfEmails);
	}
	
	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream(message.getBytes())))
					.bodyType(emailBodyType)
					.estimatedDataSize(0)
					.charset(Charsets.UTF_8)
					.truncated(false)
					.build())
			.build();
	}
}
