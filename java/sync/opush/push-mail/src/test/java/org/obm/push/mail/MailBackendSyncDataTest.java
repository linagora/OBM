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

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.MailBackendSyncData.MailBackendSyncDataFactory;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.service.DateService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ranges;

@RunWith(SlowFilterRunner.class)
public class MailBackendSyncDataTest {

	private UserDataRequest udr;
	private int collectionId;
	private String collectionPath;
	private Device device;

	private IMocksControl control;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private SnapshotService snapshotService;
	private EmailChangesComputer emailChangesComputer;
	private DateService dateService;
	private MailBackendSyncDataFactory testee;

	@Before
	public void setup() throws Exception {
		collectionId = 13411;
		collectionPath = "mailboxCollectionPath";
		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(null,  null, device);
		
		control = createControl();
		mailboxService = control.createMock(MailboxService.class);
		snapshotService = control.createMock(SnapshotService.class);
		mappingService = control.createMock(MappingService.class);
		emailChangesComputer = control.createMock(EmailChangesComputer.class);
		dateService = control.createMock(DateService.class);
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath).anyTimes();
		
		testee = new MailBackendSyncDataFactory(dateService, mappingService, mailboxService, snapshotService, emailChangesComputer);
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
				.syncKey(new SyncKey("156"))
				.build();

		expectDeleteCollectionSnapshots();
				
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
				MessageSet.builder().add(snapedEmailUID)
					.add(deletedEmailUID).add(previousUIDNext).add(newEmailUID).add(currentUIDNext).build()))
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
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectDeleteCollectionSnapshots();
		
		control.replay();
		testee.create(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
	}

	private void expectDeleteCollectionSnapshots() {
		snapshotService.deleteSnapshotAndSyncKeys(device.getDevId(), collectionId);
		expectLastCall();
	}
	
	@Test
	public void testMailBackendSyncDataCreationInitialWhenNoChange() throws Exception {
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
		MailBackendSyncData syncData = testee.create(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(syncData.getDataDeltaDate()).isEqualTo(fromDate);
		assertThat(syncData.getCollectionPath()).isEqualTo(collectionPath);
		assertThat(syncData.getCurrentUIDNext()).isEqualTo(uidNext);
		assertThat(syncData.getPreviousStateSnapshot()).isEqualTo(null);
		assertThat(syncData.getManagedEmails()).isEqualTo(previousEmailsInServer);
		assertThat(syncData.getNewManagedEmails()).isEqualTo(actualEmailsInServer);
		assertThat(syncData.getEmailChanges()).isEqualTo(emailChanges);
	}

	@Test
	public void testMailBackendSyncDataCreationInitialWhithChanges() throws Exception {
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
		MailBackendSyncData syncData = testee.create(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(syncData.getDataDeltaDate()).isEqualTo(fromDate);
		assertThat(syncData.getCollectionPath()).isEqualTo(collectionPath);
		assertThat(syncData.getCurrentUIDNext()).isEqualTo(uidNext);
		assertThat(syncData.getPreviousStateSnapshot()).isEqualTo(null);
		assertThat(syncData.getManagedEmails()).isEqualTo(previousEmailsInServer);
		assertThat(syncData.getNewManagedEmails()).isEqualTo(actualEmailsInServer);
		assertThat(syncData.getEmailChanges()).isEqualTo(emailChanges);
	}

	@Test
	public void testMailBackendSyncDataCreationNoChange() throws Exception {
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
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);

		MessageSet messages = MessageSet.builder().add(Ranges.closed(2l, 10l)).build();
		expect(mailboxService.fetchEmails(udr, collectionPath, messages)).andReturn(emailsInServer).once();
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
	
		expectEmailsDiff(ImmutableList.copyOf(emailsInServer), emailsInServer, emailChanges);
		
		control.replay();
		MailBackendSyncData syncData = testee.create(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(syncData.getDataDeltaDate()).isEqualTo(fromDate);
		assertThat(syncData.getCollectionPath()).isEqualTo(collectionPath);
		assertThat(syncData.getCurrentUIDNext()).isEqualTo(uidNext);
		assertThat(syncData.getPreviousStateSnapshot()).isEqualTo(snapshot);
		assertThat(syncData.getManagedEmails()).isEqualTo(ImmutableList.copyOf(emailsInServer));
		assertThat(syncData.getNewManagedEmails()).isEqualTo(emailsInServer);
		assertThat(syncData.getEmailChanges()).isEqualTo(emailChanges);
	}

	@Test
	public void testMailBackendSyncDataCreationWithChanges() throws Exception {
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
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);
		MessageSet messages = MessageSet.builder().add(Ranges.closed(2l, 10l)).build();
		expect(mailboxService.fetchEmails(udr, collectionPath, messages)).andReturn(actualEmailsInServer).once();
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
		expectEmailsDiff(ImmutableList.copyOf(previousEmailsInServer), actualEmailsInServer, emailChanges);
		
		control.replay();
		MailBackendSyncData syncData = testee.create(udr, ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncKey)
				.build(), 
				collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(syncData.getDataDeltaDate()).isEqualTo(fromDate);
		assertThat(syncData.getCollectionPath()).isEqualTo(collectionPath);
		assertThat(syncData.getCurrentUIDNext()).isEqualTo(uidNext);
		assertThat(syncData.getPreviousStateSnapshot()).isEqualTo(snapshot);
		assertThat(syncData.getManagedEmails()).isEqualTo(ImmutableList.copyOf(previousEmailsInServer));
		assertThat(syncData.getNewManagedEmails()).isEqualTo(actualEmailsInServer);
		assertThat(syncData.getEmailChanges()).isEqualTo(emailChanges);
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
	
	private void expectActualEmailServerStateByDate(Set<Email> emailsInServer, Date fromDate, long uidNext) {
		expect(mailboxService.fetchEmails(udr, collectionPath, fromDate))
			.andReturn(emailsInServer);
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
	}

	private void expectSnapshotDaoHasNoEntry(SyncKey syncKey) {
		expect(snapshotService.getSnapshot(device.getDevId(), syncKey, collectionId)).andReturn(null);
	}

	private void expectSnapshotDaoHasEntry(SyncKey syncKey, Snapshot snapshot) {
		expect(snapshotService.getSnapshot(device.getDevId(), syncKey, collectionId)).andReturn(snapshot);
	}
	
	private void expectEmailsDiff(Collection<Email> previousEmailsInServer, Collection<Email> actualEmailsInServer, EmailChanges diff) {
		expect(emailChangesComputer.computeChanges(previousEmailsInServer, actualEmailsInServer)).andReturn(diff);
	}
	
}
