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
package org.obm.push.mail;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.change.item.MSEmailChanges;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.MailBackendSyncData.MailBackendSyncDataFactory;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class)
public class MailBackendImplTest {

	private UserDataRequest udr;
	private int collectionId;
	private String collectionPath;
	private Device device;
	private WindowingIndexKey windowingKey;

	private IMocksControl control;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private SnapshotService snapshotService;
	private EmailChangesFetcher serverEmailChangesBuilder;
	private MailBackendSyncDataFactory mailBackendSyncDataFactory;
	private WindowingService windowingService;

	private MailBackendImpl testee;
	private User user;

	@Before
	public void setup() throws Exception {
		collectionId = 13411;
		collectionPath = "mailboxCollectionPath";
		user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(new Credentials(user, "password"),  null, device);
		windowingKey = new WindowingIndexKey(udr.getUser(), udr.getDevId(), collectionId);
		
		control = createControl();
		mailboxService = control.createMock(MailboxService.class);
		snapshotService = control.createMock(SnapshotService.class);
		mappingService = control.createMock(MappingService.class);
		serverEmailChangesBuilder = control.createMock(EmailChangesFetcher.class);
		mailBackendSyncDataFactory = control.createMock(MailBackendSyncDataFactory.class);
		windowingService = control.createMock(WindowingService.class);
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath).anyTimes();
		
		testee = new MailBackendImpl(mailboxService, null, null, null, null, snapshotService,
				serverEmailChangesBuilder, mappingService, null, null, null, mailBackendSyncDataFactory,
				windowingService);
	}
	
	@Test
	public void testInitialGetChangesWithInitialSyncKey() throws Exception {
		testInitialGetChangesUsingSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY, new SyncKey("1234"));
	}
	
	@Test
	public void testInitialGetChangesWithNotInitialSyncKey() throws Exception {
		testInitialGetChangesUsingSyncKey(new SyncKey("1234"), new SyncKey("5678"));
	}

	private void testInitialGetChangesUsingSyncKey(SyncKey syncKey, SyncKey newSyncKey) throws Exception {
		long uidNext = 45612;
		int windowSize = 10;
		
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		MSEmail email1Data = control.createMock(MSEmail.class);
		MSEmail email2Data = control.createMock(MSEmail.class);
		
		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of(email1, email2);
		EmailChanges emailChanges = EmailChanges.builder().additions(actualEmailsInServer).build();

		ItemChange itemChange1 = new ItemChangeBuilder().serverId(collectionId + ":" + 245).withNewFlag(true).withApplicationData(email1Data).build();
		ItemChange itemChange2 = new ItemChangeBuilder().serverId(collectionId + ":" + 546).withNewFlag(true).withApplicationData(email2Data).build();
		MSEmailChanges itemChanges = MSEmailChanges.builder()
			.changes(ImmutableList.of(itemChange1, itemChange2))
			.build();

		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();
		
		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setItemSyncState(syncState);
		collection.setOptions(syncCollectionOptions);
		collection.setWindowSize(windowSize);
		collection.setSyncKey(syncKey);
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		
		expect(windowingService.hasPendingElements(windowingKey, syncKey)).andReturn(false);
		expect(windowingService.hasPendingElements(windowingKey, newSyncKey)).andReturn(false);
		expectMailBackendSyncData(uidNext, syncCollectionOptions, null, previousEmailsInServer,
				actualEmailsInServer, emailChanges, fromDate, syncState);

		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, collection, SyncClientCommands.empty(), newSyncKey);
		control.verify();
		
		assertThat(actual.getDeletions()).isEmpty();
		assertThat(actual.getChanges()).containsOnly(itemChange1, itemChange2);
	}

	@Test
	public void testInitialWhenNoChange() throws Exception {
		SyncKey syncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey newSyncKey = new SyncKey("1234");
		long uidNext = 45612;
		int windowSize = 10;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of();
		EmailChanges emailChanges = EmailChanges.builder().build();

		MSEmailChanges itemChanges = MSEmailChanges.builder().build();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();

		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setItemSyncState(syncState);
		collection.setOptions(syncCollectionOptions);
		collection.setWindowSize(windowSize);
		collection.setSyncKey(syncKey);
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(windowingService.hasPendingElements(windowingKey, syncKey)).andReturn(false);
		expect(windowingService.hasPendingElements(windowingKey, newSyncKey)).andReturn(false);
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		expectMailBackendSyncData(uidNext, syncCollectionOptions, null, previousEmailsInServer, actualEmailsInServer, emailChanges, fromDate, syncState);
		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, collection, SyncClientCommands.empty(), newSyncKey);
		control.verify();

		assertThat(actual.getDeletions()).isEmpty();
		assertThat(actual.getChanges()).isEmpty();
	}
	
	@Test
	public void testNotInitial() throws Exception {
		int windowSize = 10;
		SyncKey syncKey = new SyncKey("1234");
		SyncKey newSyncKey = new SyncKey("5678");
		ImmutableList<BodyPreference> bodyPreferences = ImmutableList.<BodyPreference>of();
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(bodyPreferences);

		long snapedEmailUID = 5;
		long deletedEmailUID = 6;
		Email snapedEmail = Email.builder()
				.uid(snapedEmailUID)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build();
		Email modifiedEmail = Email.builder()
				.uid(snapedEmailUID)
				.date(date("2004-12-14T22:00:00"))
				.read(true)
				.answered(false)
				.build();
		Email deletedEmail = Email.builder()
				.uid(deletedEmailUID)
				.date(date("2004-12-14T22:00:00"))
				.read(true)
				.answered(false)
				.build();
		
		long newEmailUID = 9;
		Email newEmail = Email.builder()
				.uid(newEmailUID)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build();
		
		long previousUIDNext = 8;
		long currentUIDNext = 10;

		Set<Email> fetchedEmails = ImmutableSet.of(modifiedEmail, newEmail);
		Set<Email> previousEmailsInServer = ImmutableSet.of(snapedEmail, deletedEmail);
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();

		Snapshot existingSnapshot = Snapshot.builder()
			.emails(previousEmailsInServer)
			.collectionId(collectionId)
			.deviceId(device.getDevId())
			.filterType(syncCollectionOptions.getFilterType())
			.uidNext(previousUIDNext)
			.syncKey(syncKey)
			.build();
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, currentUIDNext, syncCollectionOptions, fetchedEmails);
		
		EmailChanges emailChanges = EmailChanges.builder()
				.changes(ImmutableSet.<Email> of(modifiedEmail))
				.additions(ImmutableSet.<Email> of(newEmail))
				.deletions(ImmutableSet.<Email> of(deletedEmail))
				.build();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();

		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setItemSyncState(syncState);
		collection.setOptions(syncCollectionOptions);
		collection.setWindowSize(windowSize);
		collection.setSyncKey(syncKey);

		expect(windowingService.hasPendingElements(windowingKey, syncKey)).andReturn(false);
		expect(windowingService.hasPendingElements(windowingKey, newSyncKey)).andReturn(false);
		expectMailBackendSyncData(currentUIDNext, syncCollectionOptions, existingSnapshot, previousEmailsInServer, fetchedEmails, emailChanges, fromDate, syncState);

		expectServerItemChanges(bodyPreferences, emailChanges, modifiedEmail, newEmail, deletedEmail);
		
		control.replay();
		testee.getChanged(udr, collection, SyncClientCommands.empty(), newSyncKey);
		
		control.verify();
	}

	private void expectServerItemChanges(ImmutableList<BodyPreference> bodyPreferences, EmailChanges emailChanges, Email modifiedEmail, Email newEmail, Email deletedEmail)
			throws EmailViewPartsFetcherException, DaoException {
		
		ImmutableList<ItemChange> itemChanges = itemChanges(modifiedEmail, newEmail);
		ImmutableList<ItemDeletion> itemDeletions = itemDeletions(deletedEmail);
		expect(serverEmailChangesBuilder.fetch(udr, collectionId, collectionPath, bodyPreferences, emailChanges))
			.andReturn(MSEmailChanges.builder()
					.changes(itemChanges)
					.deletions(itemDeletions)
					.build()).once();
	}

	private ImmutableList<ItemChange> itemChanges(Email modifiedEmail, Email newEmail) {
		ItemChange changeItemChange = new ItemChangeBuilder()
			.serverId(collectionPath + ":" + modifiedEmail.getUid())
			.build();
		ItemChange newItemChange = new ItemChangeBuilder()
			.serverId(collectionPath + ":" + newEmail.getUid())
			.build();
		ImmutableList<ItemChange> itemChanges = ImmutableList.<ItemChange> of(changeItemChange, newItemChange);
		return itemChanges;
	}

	private ImmutableList<ItemDeletion> itemDeletions(Email deletedEmail) {
		ItemDeletion deletedItemDeletion = ItemDeletion.builder()
				.serverId(collectionPath + ":" + deletedEmail.getUid())
				.build();
		ImmutableList<ItemDeletion> itemDeletions = ImmutableList.<ItemDeletion> of(deletedItemDeletion);
		return itemDeletions;
	}
	
	@Test
	public void testGetItemEstimateInitialWhenNoChange() throws Exception {
		SyncKey syncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of();
		EmailChanges emailChanges = EmailChanges.builder().build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();

		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setOptions(syncCollectionOptions);
		
		expectMailBackendSyncData(uidNext, syncCollectionOptions, null, previousEmailsInServer, actualEmailsInServer, emailChanges, fromDate, syncState);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, syncState, collection);
		control.verify();
		
		assertThat(itemEstimateSize).isEqualTo(0);
	}

	@Test
	public void testGetItemEstimateInitialWhithChanges() throws Exception {
		SyncKey syncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();
		
		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setOptions(syncCollectionOptions);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		
		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of(email1, email2);
		EmailChanges emailChanges = EmailChanges.builder().additions(actualEmailsInServer).build();
		
		expectMailBackendSyncData(uidNext, syncCollectionOptions, null, previousEmailsInServer, actualEmailsInServer, emailChanges, fromDate, syncState);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, syncState, collection);
		control.verify();
		
		assertThat(itemEstimateSize).isEqualTo(2);
	}

	@Test
	public void testGetItemEstimateNoChange() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		long uidNext = 10;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Email email = Email.builder().uid(2).read(false).date(date("2004-12-14T22:00:00")).build();
		Set<Email> emailsInServer = ImmutableSet.of(email);
		
		Snapshot snapshot = Snapshot.builder()
				.emails(emailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(2)
				.syncKey(syncKey)
				.build();
		
		EmailChanges emailChanges = EmailChanges.builder().build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();
		
		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setOptions(syncCollectionOptions);
		
		expectMailBackendSyncData(uidNext, syncCollectionOptions, snapshot, emailsInServer, emailsInServer, emailChanges, fromDate, syncState);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, syncState, collection);
		control.verify();
		
		assertThat(itemEstimateSize).isEqualTo(0);
	}

	@Test
	public void testGetItemEstimateWithChanges() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		long uidNext = 10;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Email deletedEmail = Email.builder().uid(2).read(false).date(date("2004-12-14T22:00:00")).build();
		Email modifiedEmail = Email.builder().uid(3).read(false).date(date("2004-12-14T22:00:00")).build();
		Email modifiedEmail2 = Email.builder().uid(3).read(true).date(date("2004-12-14T22:00:00")).build();
		Email newEmail = Email.builder().uid(4).read(false).date(date("2004-12-14T22:00:00")).build();
		Set<Email> previousEmailsInServer = ImmutableSet.of(deletedEmail, modifiedEmail);
		Set<Email> actualEmailsInServer = ImmutableSet.of(modifiedEmail2, newEmail);
		
		Snapshot snapshot = Snapshot.builder()
				.emails(previousEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(2)
				.syncKey(syncKey)
				.build();
		
		EmailChanges emailChanges = EmailChanges.builder()
				.additions(ImmutableSet.<Email>of(newEmail))
				.changes(ImmutableSet.<Email>of(modifiedEmail2))
				.deletions(ImmutableSet.<Email>of(deletedEmail))
				.build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build();

		SyncCollection collection = new SyncCollection(collectionId, collectionPath);
		collection.setOptions(syncCollectionOptions);
		
		expectMailBackendSyncData(uidNext, syncCollectionOptions, snapshot, previousEmailsInServer, actualEmailsInServer, emailChanges, fromDate, syncState);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, syncState, collection);
		control.verify();
		
		assertThat(itemEstimateSize).isEqualTo(3);
	}

	@Test
	public void testFetchEmailCallsServiceWithExpectedParameters() {
		List<Long> emailsToFetchUids = ImmutableList.of(15l);
		
		org.obm.push.bean.MSEmail msEmail = control.createMock(org.obm.push.bean.MSEmail.class);
		MailMessageLoader mailLoader = control.createMock(MailMessageLoader.class);
		expect(mailLoader.fetch(collectionPath, collectionId, 15l, udr))
			.andReturn(msEmail);
		
		control.replay();
		List<org.obm.push.bean.MSEmail> fetchMails = 
				testee.fetchMails(mailLoader, udr, collectionId, collectionPath, emailsToFetchUids);
		control.verify();
		
		assertThat(fetchMails).containsOnly(msEmail);
	}
	
	@Test
	public void testGetChangedNoPendingResponseFittingWindowSize() throws Exception {
		long uidNext = 45612;
		int windowSize = 10;
		SyncKey previousSyncKey = new SyncKey("132");
		SyncKey newSyncKey = new SyncKey("546");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(date("2004-12-14T22:00:00")).syncKey(previousSyncKey).build();
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(previousSyncKey);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setWindowSize(windowSize);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		Set<Email> previousEmails = ImmutableSet.of();
		Set<Email> actualEmails = ImmutableSet.of(email1, email2);
		EmailChanges allChanges = EmailChanges.builder().additions(actualEmails).build();
		EmailChanges fittingChanges = allChanges;

		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(false);
		expect(windowingService.hasPendingElements(windowingKey, newSyncKey)).andReturn(false);
		
		Snapshot previousSnapshot = Snapshot.builder()
				.emails(previousEmails)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(uidNext)
				.syncKey(previousSyncKey)
				.build();
		
		Date syncDataDate = options.getFilterType().getFilteredDateTodayAtMidnight();
		expectMailBackendSyncData(uidNext, options, previousSnapshot, previousEmails, actualEmails, allChanges, syncDataDate, syncState);
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, options, actualEmails);
		
		MSEmail itemChangeData1 = control.createMock(MSEmail.class);
		MSEmail itemChangeData2 = control.createMock(MSEmail.class);
		ItemChange itemChange1 = new ItemChangeBuilder().serverId(collectionId + ":245").withApplicationData(itemChangeData1).build();
		ItemChange itemChange2 = new ItemChangeBuilder().serverId(collectionId + ":546").withApplicationData(itemChangeData2).build();
		MSEmailChanges itemChanges = MSEmailChanges.builder()
				.changes(ImmutableList.of(itemChange1, itemChange2))
				.build();
		expectBuildItemChangesByFetchingMSEmailsData(options.getBodyPreferences(), fittingChanges, itemChanges);
		
		control.replay();
		DataDelta windowedChanges = testee.getChanged(udr, syncCollection, SyncClientCommands.empty(), newSyncKey);
		control.verify();
		
		assertThat(windowedChanges).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.of(itemChange1, itemChange2))
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(syncDataDate)
				.syncKey(newSyncKey)
				.moreAvailable(false)
				.build());
	}
	
	@Test
	public void testGetChangedNoPendingResponseNotFittingWindowSize() throws Exception {
		long uidNext = 45612;
		int windowSize = 1;
		SyncKey previousSyncKey = new SyncKey("132");
		SyncKey newSyncKey = new SyncKey("546");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(date("2004-12-14T22:00:00")).syncKey(previousSyncKey).build();
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(previousSyncKey);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setWindowSize(windowSize);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		Set<Email> previousEmails = ImmutableSet.of();
		Set<Email> actualEmails = ImmutableSet.of(email1, email2);
		EmailChanges allChanges = EmailChanges.builder().additions(actualEmails).build();
		EmailChanges fittingChanges = EmailChanges.builder().additions(ImmutableSet.of(email1)).build();

		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(false);
		expect(windowingService.hasPendingElements(windowingKey, newSyncKey)).andReturn(true);
		
		Snapshot previousSnapshot = Snapshot.builder()
				.emails(previousEmails)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(uidNext)
				.syncKey(previousSyncKey)
				.build();
		
		Date syncDataDate = options.getFilterType().getFilteredDateTodayAtMidnight();
		expectMailBackendSyncData(uidNext, options, previousSnapshot, previousEmails, actualEmails, allChanges, syncDataDate, syncState);
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, options, actualEmails);
		windowingService.pushPendingElements(windowingKey, newSyncKey, allChanges, windowSize);
		expectLastCall();
		expect(windowingService.popNextPendingElements(windowingKey, windowSize)).andReturn(fittingChanges);
		
		MSEmail itemChangeData1 = control.createMock(MSEmail.class);
		ItemChange itemChange1 = new ItemChangeBuilder().serverId(collectionId + ":245").withApplicationData(itemChangeData1).build();
		MSEmailChanges itemChanges = MSEmailChanges.builder()
				.changes(ImmutableList.of(itemChange1))
				.build();
		expectBuildItemChangesByFetchingMSEmailsData(options.getBodyPreferences(), fittingChanges, itemChanges);
		
		control.replay();
		DataDelta windowedChanges = testee.getChanged(udr, syncCollection, SyncClientCommands.empty(), newSyncKey);
		control.verify();
		

		assertThat(windowedChanges).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.of(itemChange1))
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(syncDataDate)
				.syncKey(newSyncKey)
				.moreAvailable(true)
				.build());
	}
	
	@Test
	public void testGetChangedPendingResponseFittingWindowSize() throws Exception {
		int windowSize = 10;
		SyncKey previousSyncKey = new SyncKey("132");
		SyncKey newSyncKey = new SyncKey("546");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(date("2004-12-14T22:00:00")).syncKey(previousSyncKey).build();
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(previousSyncKey);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setWindowSize(windowSize);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		Set<Email> actualEmails = ImmutableSet.of(email1, email2);
		EmailChanges allChanges = EmailChanges.builder().additions(actualEmails).build();
		EmailChanges fittingChanges = allChanges;

		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(true);
		expect(windowingService.popNextPendingElements(windowingKey, windowSize)).andReturn(allChanges);
		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(false);

		MSEmail itemChangeData1 = control.createMock(MSEmail.class);
		MSEmail itemChangeData2 = control.createMock(MSEmail.class);
		ItemChange itemChange1 = new ItemChangeBuilder().serverId(collectionId + ":245").withApplicationData(itemChangeData1).build();
		ItemChange itemChange2 = new ItemChangeBuilder().serverId(collectionId + ":546").withApplicationData(itemChangeData2).build();
		MSEmailChanges itemChanges = MSEmailChanges.builder()
				.changes(ImmutableList.of(itemChange1, itemChange2))
				.build();
		expectBuildItemChangesByFetchingMSEmailsData(options.getBodyPreferences(), fittingChanges, itemChanges);
		
		control.replay();
		DataDelta windowedChanges = testee.getChanged(udr, syncCollection, SyncClientCommands.empty(), newSyncKey);
		control.verify();
		
		assertThat(windowedChanges).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.of(itemChange1, itemChange2))
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(syncState.getSyncDate())
				.syncKey(previousSyncKey)
				.moreAvailable(false)
				.build());
	}
	
	@Test
	public void testGetChangedPendingResponseNotFittingWindowSize() throws Exception {
		int windowSize = 1;
		SyncKey previousSyncKey = new SyncKey("132");
		SyncKey newSyncKey = new SyncKey("546");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(date("2004-12-14T22:00:00")).syncKey(previousSyncKey).build();
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(previousSyncKey);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setWindowSize(windowSize);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		EmailChanges fittingChanges = EmailChanges.builder().additions(ImmutableSet.of(email1)).build();

		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(true);
		expect(windowingService.popNextPendingElements(windowingKey, windowSize)).andReturn(fittingChanges);
		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(true);
		
		MSEmail itemChangeData1 = control.createMock(MSEmail.class);
		ItemChange itemChange1 = new ItemChangeBuilder().serverId(collectionId + ":245").withApplicationData(itemChangeData1).build();
		MSEmailChanges itemChanges = MSEmailChanges.builder()
				.changes(ImmutableList.of(itemChange1))
				.build();
		expectBuildItemChangesByFetchingMSEmailsData(options.getBodyPreferences(), fittingChanges, itemChanges);
		
		control.replay();
		DataDelta windowedChanges = testee.getChanged(udr, syncCollection, SyncClientCommands.empty(), newSyncKey);
		control.verify();

		assertThat(windowedChanges).isEqualTo(DataDelta.builder()
				.changes(ImmutableList.of(itemChange1))
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(syncState.getSyncDate())
				.syncKey(previousSyncKey)
				.moreAvailable(true)
				.build());
	}
	
	@Test(expected=ProcessingEmailException.class)
	public void testGetChangedFetchingTriggersException() throws Exception {
		int windowSize = 1;
		SyncKey previousSyncKey = new SyncKey("132");
		SyncKey newSyncKey = new SyncKey("546");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(date("2004-12-14T22:00:00")).syncKey(previousSyncKey).build();
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		SyncCollection syncCollection = new SyncCollection(collectionId, collectionPath);
		syncCollection.setOptions(options);
		syncCollection.setSyncKey(previousSyncKey);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setWindowSize(windowSize);

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		EmailChanges fittingChanges = EmailChanges.builder().additions(ImmutableSet.of(email1)).build();

		expect(windowingService.hasPendingElements(windowingKey, previousSyncKey)).andReturn(true);
		expect(windowingService.popNextPendingElements(windowingKey, windowSize)).andReturn(fittingChanges);

		expect(serverEmailChangesBuilder.fetch(udr, collectionId, collectionPath, options.getBodyPreferences(), fittingChanges))
			.andThrow(new EmailViewPartsFetcherException("error"));
		
		control.replay();
		try {
			testee.getChanged(udr, syncCollection, SyncClientCommands.empty(), newSyncKey);
		} catch (ProcessingEmailException e) {
			control.verify();
			throw e;
		}
	}
	
	private void expectBuildItemChangesByFetchingMSEmailsData(List<BodyPreference> bodyPreferences,
			EmailChanges emailChanges, MSEmailChanges itemChanges)
					throws EmailViewPartsFetcherException, DaoException {
		
		expect(serverEmailChangesBuilder.fetch(udr, collectionId, collectionPath, bodyPreferences, emailChanges))
			.andReturn(itemChanges);
	}

	private void expectMailBackendSyncData(long uidNext,
			SyncCollectionOptions syncCollectionOptions,
			Snapshot snapshot,
			Set<Email> previousEmailsInServer, Set<Email> actualEmailsInServer,
			EmailChanges emailChanges, Date fromDate, ItemSyncState syncState)
			throws Exception {
		MailBackendSyncData syncData = new MailBackendSyncData(fromDate, collectionPath, uidNext, snapshot, previousEmailsInServer, actualEmailsInServer, emailChanges);
		expect(mailBackendSyncDataFactory.create(udr, syncState, collectionId, syncCollectionOptions))
			.andReturn(syncData).once();
	}
	
	private void expectSnapshotDaoRecordOneSnapshot(SyncKey syncKey, long uidNext,
			SyncCollectionOptions syncCollectionOptions, Collection<Email> actualEmailsInServer) {
		
		snapshotService.storeSnapshot(Snapshot.builder()
				.emails(actualEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.uidNext(uidNext)
				.syncKey(syncKey)
				.build());
		expectLastCall();
	}
}
