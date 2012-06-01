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

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChanges;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.locator.store.LocatorService;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.DataDeltaBuilder;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.Email;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.ItemChangesBuilder;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.EmailDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.common.base.Charsets;
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

@RunWith(SlowFilterRunner.class) @Slow
public class MailBackendHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailBackendHandlerTestModule.class);

	@Inject	@PortNumber int port;
	@Inject	SingleUserFixture singleUserFixture;
	@Inject	OpushServer opushServer;
	@Inject	ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject LocatorService locatorService;
	@Inject GreenMail greenMail;
	@Inject CollectionPathHelper collectionPathHelper;
	@Inject ImapClientProvider clientProvider;
	
	private String mailbox;
	private GreenMailUser user;
	private ImapHostManager imapHostManager;
	private Credentials credentials;
	private Device device;
	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() throws AuthorizationException, FolderException {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
		greenMail.start();
		mailbox = singleUserFixture.jaures.user.getLoginAtDomain();
		user = greenMail.setUser(mailbox, singleUserFixture.jaures.password);
		imapHostManager = greenMail.getManagers().getImapHostManager();
		imapHostManager.createMailbox(user, "Trash");
		credentials = new Credentials(singleUserFixture.jaures.user, singleUserFixture.jaures.password);
		device = new Device(singleUserFixture.jaures.hashCode(), singleUserFixture.jaures.deviceType, singleUserFixture.jaures.deviceId, new Properties());
	}

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testDeleteMail() throws Exception {
		String syncEmailSyncKey = "1";
		int serverId = 1234;
		String syncEmailId = ":2";
		SyncState syncState = new SyncState("sync state");
		DataDelta delta = new DataDeltaBuilder()
		.addChanges(
			new ItemChangesBuilder()
				.addItemChange(
					new ItemChangeBuilder().serverId(serverId + syncEmailId)
						.withApplicationData(applicationData("text", MSEmailBodyType.PlainText))))
		.withSyncDate(new Date()).build();
		
		mockHierarchyChanges(classToInstanceMap);
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockDao(serverId, syncState);
		
		bindCollectionIdToPath(serverId);
		bindChangedToDelta(delta);
		
		replayMocks(classToInstanceMap);
		opushServer.start();

		GreenMailUtil.sendTextEmailTest(mailbox, mailbox, "subject", "body");
		GreenMailUtil.sendTextEmailTest(mailbox, mailbox, "subject2", "body");
		greenMail.waitForIncomingEmail(2);

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		opClient.deleteEmail(syncEmailSyncKey, serverId, serverId + syncEmailId);

		assertEmailCountInMailbox(EmailConfiguration.IMAP_INBOX_NAME, 1);
		assertEmailCountInMailbox(EmailConfiguration.IMAP_TRASH_NAME, 1);
	}

	private void bindCollectionIdToPath(int syncEmailCollectionId) {
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		SyncCollection syncCollection = new SyncCollection(syncEmailCollectionId, IntegrationTestUtils.buildEmailInboxCollectionPath(singleUserFixture.jaures));
		expect(syncedCollectionDao.get(credentials, device, syncEmailCollectionId))
			.andReturn(syncCollection).anyTimes();
		
		syncedCollectionDao.put(eq(credentials), eq(device), anyObject(Collection.class));
		expectLastCall().anyTimes();
	}

	private void bindChangedToDelta(DataDelta delta) throws Exception {
		IContentsExporter contentsExporter = classToInstanceMap.get(IContentsExporter.class);
		expect(contentsExporter.getChanged(anyObject(UserDataRequest.class), anyObject(SyncCollection.class)))
			.andReturn(delta).once();
	}

	private void mockDao(int serverId, SyncState syncState) throws Exception {
		mockUnsynchronizedItemDao(serverId);
		mockCollectionDao(serverId, syncState);
		mockItemTrackingDao();
		mockEmailDao(serverId);
	}
	
	private void mockUnsynchronizedItemDao(int serverId) {
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(credentials, device, serverId))
			.andReturn(ImmutableSet.<ItemChange> of()).anyTimes();
		
		unsynchronizedItemDao.clearItemsToAdd(credentials, device, serverId);
		expectLastCall().anyTimes();
		
		expect(unsynchronizedItemDao.listItemsToRemove(credentials, device, serverId))
			.andReturn(ImmutableSet.<ItemChange> of()).anyTimes();
		
		unsynchronizedItemDao.clearItemsToRemove(credentials, device, serverId);
		expectLastCall().anyTimes();
		
		unsynchronizedItemDao.storeItemsToRemove(credentials, device, serverId, Lists.<ItemChange> newArrayList());
		expectLastCall().anyTimes();
	}
	
	private void mockCollectionDao(int serverId, SyncState syncState) throws Exception {
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(serverId))
			.andReturn(IntegrationTestUtils.buildEmailInboxCollectionPath(singleUserFixture.jaures)).anyTimes();
		
		expect(collectionDao.findStateForKey(anyObject(String.class)))
			.andReturn(syncState).anyTimes();
		
		int lastUpdateState = 1;
		expect(collectionDao.updateState(eq(device), eq(serverId), anyObject(SyncState.class)))
			.andReturn(lastUpdateState).anyTimes();
		
		expect(collectionDao.getCollectionMapping(eq(device), anyObject(String.class)))
			.andReturn(serverId).anyTimes();
		
		IntegrationTestUtils.expectUserCollectionsNeverChange(collectionDao, Sets.newHashSet(singleUserFixture.jaures));
	}

	private void mockItemTrackingDao() throws Exception {
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		itemTrackingDao.markAsSynced(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		itemTrackingDao.markAsDeleted(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		expect(itemTrackingDao.isServerIdSynced(anyObject(SyncState.class), anyObject(ServerId.class)))
			.andReturn(false).anyTimes();
	}

	private void mockEmailDao(int serverId) throws Exception {
		EmailDao emailDao = classToInstanceMap.get(EmailDao.class);
		emailDao.deleteSyncEmails(anyObject(Integer.class), eq(serverId), anyObject(Collection.class));
		expectLastCall().anyTimes();
		
		expect(emailDao.alreadySyncedEmails(eq(serverId), anyInt(), anyObject(Collection.class)))
			.andReturn(ImmutableSet.<Email> of()).anyTimes();
		
		emailDao.updateSyncEntriesStatus(anyObject(Integer.class), eq(serverId), anyObject(Set.class));
		expectLastCall().anyTimes();
		
		emailDao.createSyncEntries(anyObject(Integer.class), eq(serverId), anyObject(Set.class), anyObject(Date.class));
		expectLastCall().anyTimes();
	}
	
	private void assertEmailCountInMailbox(String mailbox, Integer expectedNumberOfEmails) {
		MailFolder inboxFolder = imapHostManager.getFolder(user, mailbox);
		Assertions.assertThat(inboxFolder.getMessageCount()).isEqualTo(expectedNumberOfEmails);
	}
	
	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return new MSEmail.MSEmailBuilder()
			.uid(1l)
			.header(new MSEmailHeader.Builder().build())
			.body(new MSEmailBody(new SerializableInputStream(
					new ByteArrayInputStream(message.getBytes())), emailBodyType, null, Charsets.UTF_8)).build();
	}
}
