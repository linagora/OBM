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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
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
import org.obm.push.bean.change.item.ServerItemChanges;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.service.DateService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.store.SnapshotDao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;

@Ignore("Implementation has not yet been done")
@RunWith(SlowFilterRunner.class)
public class MailBackendImplTest {

	private UserDataRequest udr;
	private int collectionId;
	private String collectionPath;
	private Device device;

	private IMocksControl control;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private SnapshotDao snapshotDao;
	private EmailChangesComputer emailChangesComputer;
	private ServerEmailChangesBuilder serverEmailChangesBuilder;
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
		snapshotDao = control.createMock(SnapshotDao.class);
		mappingService = control.createMock(MappingService.class);
		emailChangesComputer = control.createMock(EmailChangesComputer.class);
		serverEmailChangesBuilder = control.createMock(ServerEmailChangesBuilder.class);
		dateService = control.createMock(DateService.class);
		expect(mappingService.getCollectionPathFor(collectionId)).andReturn(collectionPath).anyTimes();
		
		testee = new MailBackendImpl(mailboxService, null, null, null, null, null, 
				null, mappingService, null, null, null);
	}
	
	@Test
	public void testInitialGetChangesWithInitialSyncKey() throws Exception {
		testInitialGetChangesUsingSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);
	}
	
	@Test
	public void testInitialGetChangesWithNotInitialSyncKey() throws Exception {
		testInitialGetChangesUsingSyncKey(new SyncKey("1234"));
	}

	private void testInitialGetChangesUsingSyncKey(SyncKey syncKey) throws Exception {
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
		ServerItemChanges itemChanges = ServerItemChanges.builder()
			.changes(ImmutableList.of(itemChange1, itemChange2))
			.build();
		
		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectSnapshotDaoRecordOneSnapshot(syncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, new ItemSyncState(syncKey), collectionId, syncCollectionOptions);
		control.verify();
		
		assertThat(actual.getDeletions()).isEmpty();
		assertThat(actual.getChanges()).containsOnly(itemChange1, itemChange2);
	}
	
	@Test
	public void testInitialWhenNoChange() throws Exception {
		SyncKey syncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());

		Set<Email> previousEmailsInServer = ImmutableSet.of();
		Set<Email> actualEmailsInServer = ImmutableSet.of();
		EmailChanges emailChanges = EmailChanges.builder().build();

		ServerItemChanges itemChanges = ServerItemChanges.builder().build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasNoEntry(syncKey);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		expectSnapshotDaoRecordOneSnapshot(syncKey, uidNext, syncCollectionOptions, actualEmailsInServer);
		expectEmailsDiff(previousEmailsInServer, actualEmailsInServer, emailChanges);
		expectBuildItemChangesByFetchingMSEmailsData(syncCollectionOptions.getBodyPreferences(), emailChanges, itemChanges);
		
		control.replay();
		DataDelta actual = testee.getChanged(udr, new ItemSyncState(syncKey), collectionId, syncCollectionOptions);
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
				.syncKey(new SyncKey("156"))
				.build();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, snapshot);
		expectActualEmailServerStateByDate(actualEmailsInServer, fromDate, uidNext);
		
		control.replay();
		testee.getChanged(udr, new ItemSyncState(syncKey), collectionId, syncCollectionOptions);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testNotInitial() throws Exception {
		SyncKey syncKey = new SyncKey("1234");
		long uidNext = 45612;
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions();
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		syncCollectionOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());
		
		Set<Email> previousEmailsInServer = ImmutableSet.of();

		Date fromDate = syncCollectionOptions.getFilterType().getFilteredDateTodayAtMidnight();
		expect(dateService.getCurrentDate()).andReturn(fromDate);
		expectSnapshotDaoHasEntry(syncKey, Snapshot.builder()
				.emails(previousEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.uidNext(Ints.checkedCast(uidNext))
				.syncKey(syncKey)
				.build());
		
		control.replay();
		testee.getChanged(udr, new ItemSyncState(syncKey), collectionId, syncCollectionOptions);
	}

	private void expectBuildItemChangesByFetchingMSEmailsData(List<BodyPreference> bodyPreferences,
			EmailChanges emailChanges, ServerItemChanges itemChanges)
					throws EmailViewPartsFetcherException, DaoException {
		
		expect(serverEmailChangesBuilder.build(udr, collectionId, collectionPath, bodyPreferences, emailChanges))
			.andReturn(itemChanges);
	}

	private void expectEmailsDiff(Set<Email> previousEmailsInServer, Set<Email> actualEmailsInServer, EmailChanges diff) {
		expect(emailChangesComputer.computeChanges(previousEmailsInServer, actualEmailsInServer)).andReturn(diff);
	}

	private void expectSnapshotDaoRecordOneSnapshot(SyncKey syncKey, long uidNext,
			SyncCollectionOptions syncCollectionOptions, Set<Email> actualEmailsInServer) {
		
		snapshotDao.put(Snapshot.builder()
				.emails(actualEmailsInServer)
				.collectionId(collectionId)
				.deviceId(device.getDevId())
				.filterType(syncCollectionOptions.getFilterType())
				.uidNext(Ints.checkedCast(uidNext))
				.syncKey(syncKey)
				.build());
		expectLastCall();
	}

	private void expectActualEmailServerStateByDate(Set<Email> emailsInServer, Date fromDate, long uidNext) {
		expect(mailboxService.fetchEmails(udr, collectionPath, fromDate))
			.andReturn(emailsInServer);
		expect(mailboxService.fetchUIDNext(udr, collectionPath)).andReturn(uidNext);
	}

	private void expectSnapshotDaoHasNoEntry(SyncKey syncKey) {
		expect(snapshotDao.get(device.getDevId(), syncKey, collectionId)).andReturn(null);
	}

	private void expectSnapshotDaoHasEntry(SyncKey syncKey, Snapshot snapshot) {
		expect(snapshotDao.get(device.getDevId(), syncKey, collectionId)).andReturn(snapshot);
	}
	
}
