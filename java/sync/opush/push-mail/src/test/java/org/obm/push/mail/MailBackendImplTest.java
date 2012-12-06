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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.change.item.MSEmailChanges;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.service.DateService;
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

	private IMocksControl control;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private SnapshotService snapshotService;
	private EmailChangesComputer emailChangesComputer;
	private EmailChangesFetcher serverEmailChangesBuilder;
	private MailBackendImpl testee;
	private DateService dateService;

	@Before
	public void setup() throws Exception {
		collectionId = 13411;
		collectionPath = "mailboxCollectionPath";
		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", new DeviceId("my phone"));
		udr = new UserDataRequest(null,  null, device, null);
		
		control = createControl();
		mailboxService = control.createMock(MailboxService.class);
		snapshotService = control.createMock(SnapshotService.class);
		mappingService = control.createMock(MappingService.class);
		emailChangesComputer = control.createMock(EmailChangesComputer.class);
		serverEmailChangesBuilder = control.createMock(EmailChangesFetcher.class);
		dateService = control.createMock(DateService.class);
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath).anyTimes();
		
		testee = new MailBackendImpl(mailboxService, null, null, null, null, snapshotService,
				emailChangesComputer, serverEmailChangesBuilder, mappingService, null, dateService, null, null);
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
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions, newSyncKey);
		control.verify();
		
		assertThat(actual.getDeletions()).isEmpty();
		assertThat(actual.getChanges()).containsOnly(itemChange1, itemChange2);
	}
	
	@Test
	public void testInitialWhenNoChange() throws Exception {
		SyncKey syncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey newSyncKey = new SyncKey("1234");
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of();
		EmailChanges emailChanges = EmailChanges.builder().build();

		MSEmailChanges itemChanges = MSEmailChanges.builder().build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions, newSyncKey);
		control.verify();

		assertThat(actual.getDeletions()).isEmpty();
		assertThat(actual.getChanges()).isEmpty();
	}
	
	@Test(expected=FilterTypeChangedException.class)
	public void testSyncByDateWhenFilterTypeChanged() throws Exception {
		SyncKey syncKey = new SyncKey("1234");
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Email email = Email.builder()
				.uid(5)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build();
		Set<Email> previousEmailsInServer = ImmutableSet.of(email);
		Set<Email> actualEmailsInServer = ImmutableSet.of(email);
		
		Snapshot snapshot = Snapshot.builder()
				.emails(previousEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.THREE_DAYS_BACK)
				.uidNext(5000)
				.syncKey(syncKey)
				.build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);
		expectSnapshotDaoDelete(collectionId);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		
		control.replay();
		testee.getChanged(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions, new SyncKey("5678"));
	}
	
	private void expectSnapshotDaoDelete(int collectionId) {
		snapshotService.deleteSnapshotAndSyncKeys(device.getDevId(), collectionId);
		expectLastCall();
	}

	@Test
	public void testNotInitial() throws Exception {
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
		expect(mailboxService.fetchUIDNext(udr, collectionPath))
			.andReturn(currentUIDNext).once();
		ImmutableList<Email> fetchedEmails = ImmutableList.of(modifiedEmail, newEmail);
		expect(mailboxService.fetchEmails(udr, collectionPath, 
				ImmutableList.<Long> of(snapedEmailUID, deletedEmailUID, previousUIDNext, newEmailUID, currentUIDNext)))
			.andReturn(fetchedEmails).once();
		
		ImmutableList<Email> previousEmailsInServer = ImmutableList.of(snapedEmail, deletedEmail);
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, Snapshot.builder()
				.emails(previousEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.uidNext(previousUIDNext)
				.syncKey(syncKey)
				.build());
		expectSnapshotDaoRecordOneSnapshot(newSyncKey, currentUIDNext, syncCollectionOptions, fetchedEmails);
		
		EmailChanges emailChanges = EmailChanges.builder()
				.changes(ImmutableSet.<Email> of(modifiedEmail))
				.additions(ImmutableSet.<Email> of(newEmail))
				.deletions(ImmutableSet.<Email> of(deletedEmail))
				.build();
		expect(emailChangesComputer.computeChanges(previousEmailsInServer, fetchedEmails))
			.andReturn(emailChanges).once();

		expectServerItemChanges(bodyPreferences, emailChanges, modifiedEmail, newEmail, deletedEmail);
		
		control.replay();
		testee.getChanged(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions, newSyncKey);
		
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
	public void testGetManagedEmailsIsEmptyForNull() {
		control.replay();
		assertThat(testee.getManagedEmails(null)).isEmpty();
		control.verify();
	}
	
	@Test
	public void testGetManagedEmailsAreTookFromSnapshot() {
		Snapshot snapshot = Snapshot.builder()
			.addEmail(Email.builder()
				.uid(5)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build())
			.addEmail(Email.builder()
				.uid(15)
				.date(date("2014-12-14T22:00:00"))
				.read(true)
				.answered(true)
				.build())
			.collectionId(collectionId)
			.deviceId(device.getDevId())
			.filterType(FilterType.ALL_ITEMS)
			.uidNext(5000)
			.syncKey(new SyncKey("156"))
			.build();

		control.replay();
		assertThat(testee.getManagedEmails(snapshot)).containsOnly(
			Email.builder()
				.uid(5)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build(),
			Email.builder()
				.uid(15)
				.date(date("2014-12-14T22:00:00"))
				.read(true)
				.answered(true)
				.build());
		control.verify();
	}
	
	@Test
	public void testMustSyncByDateIsTrueWhenNoSnapshot() {
		control.replay();
		assertThat(testee.mustSyncByDate(null)).isTrue();
		control.verify();
	}
	
	@Test
	public void testMustSyncByDateIsFalseWhenPreviousSnapshot() {
		Snapshot snapshot = Snapshot.builder()
				.addEmail(Email.builder()
					.uid(5)
					.date(date("2004-12-14T22:00:00"))
					.read(false)
					.answered(false)
					.build())
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(5000)
				.syncKey(new SyncKey("156"))
				.build();

		control.replay();
		assertThat(testee.mustSyncByDate(snapshot)).isFalse();
		control.verify();
	}
	
	@Test
	public void testSearchEmailsToManagerIsByDateForNullWithoutEmails() throws FilterTypeChangedException {
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		Date fromDate = options.getFilterType().getFilteredDateTodayAtMidnight();
		
		Set<Email> emailsExpected = ImmutableSet.of();
		expect(mailboxService.fetchEmails(udr, collectionPath, fromDate))
			.andReturn(emailsExpected);

		control.replay();
		Collection<Email> result = testee.searchEmailsToManage(udr, collectionId, collectionPath, null, options, date("2004-10-14T22:00:00"), 0);
		control.verify();
		
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testSearchEmailsToManagerIsByDateForNullWithEmails() throws FilterTypeChangedException {
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());
		Date fromDate = options.getFilterType().getFilteredDateTodayAtMidnight();
		
		expect(mailboxService.fetchEmails(udr, collectionPath, fromDate))
			.andReturn(ImmutableSet.of(
					Email.builder()
					.uid(5)
					.date(date("2004-12-14T22:00:00"))
					.read(false)
					.answered(false)
					.build()));

		control.replay();
		Collection<Email> result = testee.searchEmailsToManage(udr, collectionId, collectionPath, null, options, date("2004-10-14T22:00:00"), 0);
		control.verify();
		
		assertThat(result).containsOnly(
			Email.builder()
				.uid(5)
				.date(date("2004-12-14T22:00:00"))
				.read(false)
				.answered(false)
				.build());
	}
	
	@Test(expected=FilterTypeChangedException.class)
	public void testSearchEmailsToManagerThrowExecptionWhenDifferentFolderType() throws FilterTypeChangedException {
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setFilterType(FilterType.ALL_ITEMS);
		options.setBodyPreferences(ImmutableList.<BodyPreference>of());

		SyncKey syncKey = new SyncKey("156");
		Snapshot snapshot = Snapshot.builder()
				.addEmail(Email.builder()
					.uid(5)
					.date(date("2004-12-14T22:00:00"))
					.read(false)
					.answered(false)
					.build())
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ONE_MONTHS_BACK)
				.uidNext(5000)
				.syncKey(syncKey)
				.build();

		expectSnapshotDaoDelete(collectionId);
		control.replay();
		testee.searchEmailsToManage(udr, collectionId, collectionPath, snapshot, options, date("2004-10-14T22:00:00"), 0);
	}
	
	@Test
	public void testSearchEmailsToManagerIsByUIDsWhenPreviousSnapshot() throws FilterTypeChangedException {
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

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
		ImmutableList<Email> expectedEmails = ImmutableList.of(modifiedEmail, newEmail);
		expect(mailboxService.fetchEmails(udr, collectionPath, 
				ImmutableList.<Long> of(snapedEmailUID, deletedEmailUID, previousUIDNext, newEmailUID, currentUIDNext)))
			.andReturn(expectedEmails).once();

		Snapshot snapshot = Snapshot.builder()
				.addEmail(snapedEmail)
				.addEmail(deletedEmail)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(FilterType.ALL_ITEMS)
				.uidNext(previousUIDNext)
				.syncKey(new SyncKey("156"))
				.build();
		
		control.replay();
		Collection<Email> searchEmailsToManage = testee.searchEmailsToManage(udr, collectionId, collectionPath, snapshot, syncCollectionOptions, date("2004-10-14T22:00:00"), currentUIDNext);
		
		control.verify();
		assertThat(searchEmailsToManage).isEqualTo(expectedEmails);
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
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
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

		Email email1 = Email.builder().uid(245).read(false).date(date("2004-12-14T22:00:00")).build();
		Email email2 = Email.builder().uid(546).read(true).date(date("2012-12-12T23:59:00")).build();
		
		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of(email1, email2);
		EmailChanges emailChanges = EmailChanges.builder().additions(actualEmailsInServer).build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
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
		
		ImmutableList<Long> uids = ImmutableList.<Long> of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
		
		EmailChanges emailChanges = EmailChanges.builder().build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);
		expectActualEmailServerStateByUid(emailsInServer, uids, uidNext);
		expectEmailsDiff(ImmutableList.copyOf(emailsInServer), emailsInServer, emailChanges);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
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
		
		ImmutableList<Long> uids = ImmutableList.<Long> of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
		
		EmailChanges emailChanges = EmailChanges.builder()
				.additions(ImmutableSet.<Email>of(newEmail))
				.changes(ImmutableSet.<Email>of(modifiedEmail2))
				.deletions(ImmutableSet.<Email>of(deletedEmail))
				.build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);
		expectActualEmailServerStateByUid(actualEmailsInServer, uids, uidNext);
		expectEmailsDiff(ImmutableList.copyOf(previousEmailsInServer), actualEmailsInServer, emailChanges);
		
		control.replay();
		int itemEstimateSize = testee.getItemEstimateSize(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(itemEstimateSize).isEqualTo(3);
	}

	@Test
	public void testSearchEmailsFromDateNoFilterType() {
		DateTime dateTime = new DateTime(DateUtils.getCurrentDate()).withZone(DateTimeZone.UTC);
		Date expectedDate = dateTime.minusDays(3).toDate();

		control.replay();
		Date searchEmailsFromDate = testee.searchEmailsFromDate(FilterType.THREE_DAYS_BACK, dateTime.toDate());
		
		control.verify();
		assertThat(searchEmailsFromDate).isEqualTo(expectedDate);
	}

	@Test
	public void testSearchEmailsFromDateWithFilterType() {
		Date date = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		
		control.replay();
		Date searchEmailsFromDate = testee.searchEmailsFromDate(null, null);
		
		control.verify();
		assertThat(searchEmailsFromDate).isEqualTo(date);
	}
	
	private void expectBuildItemChangesByFetchingMSEmailsData(List<BodyPreference> bodyPreferences,
			EmailChanges emailChanges, MSEmailChanges itemChanges)
					throws EmailViewPartsFetcherException, DaoException {
		
		expect(serverEmailChangesBuilder.fetch(udr, collectionId, collectionPath, bodyPreferences, emailChanges))
			.andReturn(itemChanges);
	}

	private void expectEmailsDiff(Collection<Email> previousEmailsInServer, Collection<Email> actualEmailsInServer, EmailChanges diff) {
		expect(emailChangesComputer.computeChanges(previousEmailsInServer, actualEmailsInServer)).andReturn(diff);
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

	private void expectActualEmailServerStateByDate(Set<Email> emailsInServer, Date fromDate, long uidNext) {
		expect(mailboxService.fetchEmails(udr, collectionPath, fromDate))
			.andReturn(emailsInServer);
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
	}

	private void expectActualEmailServerStateByUid(Set<Email> emailsInServer, Collection<Long> uids, long uidNext) {
		expect(mailboxService.fetchEmails(udr, collectionPath, uids))
			.andReturn(emailsInServer).once();
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
	}

	private void expectSnapshotDaoHasNoEntry(SyncKey syncKey) {
		expect(snapshotService.getSnapshot(device.getDevId(), syncKey, collectionId)).andReturn(null);
	}

	private void expectSnapshotDaoHasEntry(SyncKey syncKey, Snapshot snapshot) {
		expect(snapshotService.getSnapshot(device.getDevId(), syncKey, collectionId)).andReturn(snapshot);
	}
	
}
