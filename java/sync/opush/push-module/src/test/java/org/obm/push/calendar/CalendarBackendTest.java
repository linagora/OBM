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
package org.obm.push.calendar;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSMessageClass;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;
import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class CalendarBackendTest {

	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	private AccessToken token;
	
	private MappingService mappingService;
	private CalendarClient calendarClient;
	private EventConverter eventConverter;
	private EventService eventService;
	private LoginService loginService;
	private Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	
	private CalendarBackend calendarBackend;
	
	@Before
	public void setUp() {
		this.user = Factory.create().createUser("test@test", "test@domain", "displayName");
		this.device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		this.userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
		this.token = new AccessToken(0, "OBM");
		
		this.mappingService = createMock(MappingService.class);
		this.calendarClient = createMock(CalendarClient.class);
		this.eventConverter = createMock(EventConverter.class);
		this.eventService = createMock(EventService.class);
		this.loginService = createMock(LoginService.class);
		this.collectionPathBuilderProvider = createMock(Provider.class);
		
		this.calendarBackend = new CalendarBackend(mappingService, 
				calendarClient, 
				eventConverter, 
				eventService, 
				loginService, 
				collectionPathBuilderProvider);
	}
	
	@Test
	public void testGetPIMDataType() {
		assertThat(calendarBackend.getPIMDataType()).isEqualTo(PIMDataType.CALENDAR);
	}
	
	@Test
	public void testInitialCalendarChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b");
		String calendarDisplayName = userDataRequest.getUser().getLoginAtDomain();
		String defaultCalendarName = "obm:\\\\test@test\\calendar\\" + calendarDisplayName;
		
		int collectionMappingId = 1;
		List<CollectionPath> knownCollections = ImmutableList.of(); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceSearchThenCreateCollection(defaultCalendarName, collectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(collectionMappingId));
		expectMappingServiceLookupCollection(defaultCalendarName, collectionMappingId);
		
		Builder collectionPathBuilder = expectBuildCollectionPath(calendarDisplayName, defaultCalendarName);

		replay(mappingService, collectionPathBuilder, collectionPathBuilderProvider);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		verify(mappingService, collectionPathBuilder, collectionPathBuilderProvider);
		
		ItemChange expectedItemChange = new ItemChangeBuilder()
				.serverId(String.valueOf(collectionMappingId))
				.parentId("0")
				.displayName(calendarDisplayName + " calendar")
				.itemType(FolderType.DEFAULT_CALENDAR_FOLDER)
				.withNewFlag(true)
				.build();
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(expectedItemChange);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testNoCalendarChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b");
		String calendarDisplayName = userDataRequest.getUser().getLoginAtDomain();
		String defaultCalendarName = "obm:\\\\test@test\\calendar\\" + calendarDisplayName;
		
		int collectionMappingId = 1;
		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new CollectionPathTest(defaultCalendarName, PIMDataType.CALENDAR, calendarDisplayName));
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceFindCollection(defaultCalendarName, collectionMappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(collectionMappingId));
		
		Builder collectionPathBuilder = expectBuildCollectionPath(calendarDisplayName, defaultCalendarName);

		replay(mappingService, collectionPathBuilder, collectionPathBuilderProvider);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		verify(mappingService, collectionPathBuilder, collectionPathBuilderProvider);
		
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}

	@Test
	public void testInitialCalendarChangesWhenMultipleCalendar() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b");
		String rootCalendarPath = "obm:\\\\test@domain\\calendar\\";

		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", "my phone");
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);

		int calendar1MappingId = 1;
		String calendar1DisplayName = "test@test";
		String calendar1CollectionPath = rootCalendarPath + calendar1DisplayName;
		int calendar2MappingId = 2;
		String calendar2DisplayName = "alias@test";
		String calendar2CollectionPath = rootCalendarPath + calendar2DisplayName;

		expectLoginBehavior();
		expectObmSyncCalendarChanges(
				newCalendarInfo("1", calendar1DisplayName),
				newCalendarInfo("2", calendar2DisplayName));
		
		Builder collectionPathBuilder1 = expectBuildCollectionPath(calendar1DisplayName, calendar1CollectionPath);
		Builder collectionPathBuilder2 = expectBuildCollectionPath(calendar2DisplayName, calendar2CollectionPath);
		
		List<CollectionPath> knownCollections = ImmutableList.of(); 
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceSearchThenCreateCollection(calendar1CollectionPath, calendar1MappingId);
		expectMappingServiceSearchThenCreateCollection(calendar2CollectionPath, calendar2MappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(calendar1MappingId, calendar2MappingId));
		expectMappingServiceLookupCollection(calendar1CollectionPath, calendar1MappingId);
		expectMappingServiceLookupCollection(calendar2CollectionPath, calendar2MappingId);
		
		replay(loginService, mappingService, collectionPathBuilder1, collectionPathBuilder2,
				collectionPathBuilderProvider, calendarClient);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		verify(loginService, mappingService, collectionPathBuilder1, collectionPathBuilder2,
				collectionPathBuilderProvider, calendarClient);

		ItemChange expectedItemChange1 = new ItemChangeBuilder()
				.serverId(String.valueOf(calendar1MappingId))
				.parentId("0")
				.displayName(calendar1DisplayName + " calendar")
				.itemType(FolderType.DEFAULT_CALENDAR_FOLDER)
				.withNewFlag(true)
				.build();
		ItemChange expectedItemChange2 = new ItemChangeBuilder()
				.serverId(String.valueOf(calendar2MappingId))
				.parentId("0")
				.displayName(calendar2DisplayName + " calendar")
				.itemType(FolderType.USER_CREATED_CALENDAR_FOLDER)
				.withNewFlag(true)
				.build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(2);
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(expectedItemChange1, expectedItemChange2);
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}

	@Test
	public void testDeletionWhenMultipleCalendar() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b");
		String rootCalendarPath = "obm:\\\\test@domain\\calendar\\";

		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", "my phone");
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);

		int calendar1MappingId = 1;
		String calendar1DisplayName = "added@test";
		String calendar1CollectionPath = rootCalendarPath + calendar1DisplayName;
		int calendar2MappingId = 2;
		String calendar2DisplayName = "deleted@test";
		String calendar2CollectionPath = rootCalendarPath + calendar2DisplayName;

		expectLoginBehavior();
		expectObmSyncCalendarChanges(newCalendarInfo("1", calendar1DisplayName));
		
		Builder collectionPathBuilder1 = expectBuildCollectionPath(calendar1DisplayName, calendar1CollectionPath);

		List<CollectionPath> knownCollections = ImmutableList.<CollectionPath>of(
				new CollectionPathTest(calendar2CollectionPath, PIMDataType.CALENDAR, calendar2DisplayName));
		expectMappingServiceListLastKnowCollection(lastKnownState, knownCollections);
		expectMappingServiceSearchThenCreateCollection(calendar1CollectionPath, calendar1MappingId);
		expectMappingServiceSnapshot(outgoingSyncState, ImmutableSet.of(calendar1MappingId));
		expectMappingServiceLookupCollection(calendar1CollectionPath, calendar1MappingId);
		expectMappingServiceLookupCollection(calendar2CollectionPath, calendar2MappingId);
		
		replay(loginService, mappingService, collectionPathBuilder1, collectionPathBuilderProvider, calendarClient);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, outgoingSyncState);
		
		verify(loginService, mappingService, collectionPathBuilder1, collectionPathBuilderProvider, calendarClient);

		ItemChange expectedItemChange1 = new ItemChangeBuilder()
				.serverId(String.valueOf(calendar1MappingId))
				.parentId("0")
				.displayName(calendar1DisplayName + " calendar")
				.itemType(FolderType.USER_CREATED_CALENDAR_FOLDER)
				.withNewFlag(true)
				.build();
		ItemChange expectedItemChange2 = new ItemChangeBuilder()
				.serverId(String.valueOf(calendar2MappingId))
				.parentId("0")
				.displayName(calendar2DisplayName + " calendar")
				.itemType(FolderType.USER_CREATED_CALENDAR_FOLDER)
				.withNewFlag(false)
				.build();
		
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getChangedItems()).containsOnly(expectedItemChange1);
		assertThat(hierarchyItemsChanges.getDeletedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(expectedItemChange2);
	}

	private Builder expectBuildCollectionPath(String displayName, String fullCollectionPath) {
		CollectionPath collectionPath = new CollectionPathTest(fullCollectionPath, PIMDataType.CALENDAR, displayName);
		CollectionPath.Builder collectionPathBuilder = expectCollectionPathBuilder(collectionPath, displayName);
		expectCollectionPathBuilderPovider(collectionPathBuilder);
		return collectionPathBuilder;
	}

	private CollectionPath.Builder expectCollectionPathBuilder(CollectionPath collectionPath, String displayName) {
		CollectionPath.Builder collectionPathBuilder = createMock(CollectionPath.Builder.class);
		expect(collectionPathBuilder.userDataRequest(userDataRequest))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.pimType(PIMDataType.CALENDAR))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.displayName(displayName))
			.andReturn(collectionPathBuilder).once();
		
		expect(collectionPathBuilder.build())
			.andReturn(collectionPath).once();
		
		return collectionPathBuilder;
	}

	private void expectCollectionPathBuilderPovider(CollectionPath.Builder collectionPathBuilder) {
			expect(collectionPathBuilderProvider.get())
				.andReturn(collectionPathBuilder).once();
	}
	
	private void expectObmSyncCalendarChanges(CalendarInfo...calendarInfos) throws ServerFault {
		expect(calendarClient.listCalendars(token))
			.andReturn(calendarInfos).once();
	}

	private CalendarInfo newCalendarInfo(String uid, String mail) {
		CalendarInfo calendarInfo = new CalendarInfo();
		calendarInfo.setUid(uid);
		calendarInfo.setMail(mail);
		return calendarInfo;
	}

	private void expectMappingServiceSnapshot(FolderSyncState outgoingSyncState, Set<Integer> collectionIds)
			throws DaoException {

		mappingService.snapshotCollections(outgoingSyncState, collectionIds);
		expectLastCall();
	}

	private void expectMappingServiceListLastKnowCollection(FolderSyncState incomingSyncState,
			List<CollectionPath> collectionPaths) throws DaoException {
		
		expect(mappingService.listCollections(userDataRequest, incomingSyncState))
			.andReturn(collectionPaths).once();
	}

	private void expectMappingServiceFindCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceSearchThenCreateCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expect(mappingService.getCollectionIdFor(device, collectionPath))
			.andThrow(new CollectionNotFoundException()).once();
		
		expect(mappingService.createCollectionMapping(device, collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceLookupCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expectMappingServiceFindCollection(collectionPath, collectionId);
		expect(mappingService.collectionIdToString(collectionId))
			.andReturn(String.valueOf(collectionId)).once();
	}

	@Test
	public void testGetEstimateSize() throws Exception {
		Date currentDate = DateUtils.getCurrentDate();
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		lastKnownState.setLastSync(currentDate);
		int collectionId = 1;

		expectLoginBehavior();

		expectMappingServiceCollectionPathFor(collectionId);
		
		EventChanges eventChanges = expectTwoDeletedAndTwoUpdatedEventChanges(currentDate, mappingService, collectionId);
		
		expect(calendarClient.getSync(token, "test", currentDate))
			.andReturn(eventChanges).once();
		
		expect(calendarClient.getUserEmail(token))
			.andReturn("test@test").anyTimes();

		expectConvertUpdatedEventsToMSEvents(eventChanges);
		
		replay(loginService, mappingService, calendarClient, eventService);
		
		BodyPreference.Builder bodyPreferenceBuilder = BodyPreference.builder();
		BodyPreference bodyPreference = bodyPreferenceBuilder.build();
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(ImmutableList.<BodyPreference> of(bodyPreference));
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		
		int itemEstimateSize = calendarBackend.getItemEstimateSize(userDataRequest, collectionId, lastKnownState, syncCollectionOptions);
		
		verify(loginService, mappingService, calendarClient, eventService);
		
		assertThat(itemEstimateSize).isEqualTo(4);
	}

	private void expectMappingServiceCollectionPathFor(int collectionId) throws CollectionNotFoundException, DaoException {
		expect(mappingService.getCollectionPathFor(collectionId))
			.andReturn("obm:\\\\test@test\\calendar\\test@test");
	}
	
	@Test 
	public void testGetChanged() throws Exception {
		Date currentDate = DateUtils.getCurrentDate();
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		lastKnownState.setLastSync(currentDate);
		int collectionId = 1;

		expectLoginBehavior();

		expectMappingServiceCollectionPathFor(collectionId);
		
		EventChanges eventChanges = expectTwoDeletedAndTwoUpdatedEventChanges(currentDate, mappingService, collectionId);
		
		expect(calendarClient.getSync(token, "test", currentDate))
			.andReturn(eventChanges).once();
		
		expect(calendarClient.getUserEmail(token))
			.andReturn("test@test").anyTimes();

		expectConvertUpdatedEventsToMSEvents(eventChanges);
		
		replay(loginService, mappingService, calendarClient, eventService);
		
		BodyPreference.Builder bodyPreferenceBuilder = BodyPreference.builder();
		BodyPreference bodyPreference = bodyPreferenceBuilder.build();
		SyncCollectionOptions syncCollectionOptions = new SyncCollectionOptions(ImmutableList.<BodyPreference> of(bodyPreference));
		syncCollectionOptions.setFilterType(FilterType.ALL_ITEMS);
		
		DataDelta dataDelta = calendarBackend.getChanged(userDataRequest, lastKnownState, collectionId, syncCollectionOptions);
		
		verify(loginService, mappingService, calendarClient, eventService);
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(currentDate);
		assertThat(dataDelta.getDeletions()).hasSize(2);
		assertThat(dataDelta.getChanges()).hasSize(2);
	}

	private EventChanges expectTwoDeletedAndTwoUpdatedEventChanges(Date currentDate, MappingService mappingService, int collectionId) {
		EventChanges eventChanges = new EventChanges();
		List<DeletedEvent> deletedEvents = new ArrayList<DeletedEvent>();
		deletedEvents.add(createDeletedEvent(new EventObmId(11), new EventExtId("11")));
		deletedEvents.add(createDeletedEvent(new EventObmId(12), new EventExtId("12")));
		eventChanges.setDeletedEvents(deletedEvents);
		
		eventChanges.setParticipationUpdated(ImmutableList.<ParticipationChanges> of());
		
		List<Event> updated = new ArrayList<Event>();
		updated.add(createEvent(21));
		updated.add(createEvent(22));
		eventChanges.setUpdated(updated);
		
		eventChanges.setLastSync(currentDate);
		
		for (Event event : updated) {
			expect(mappingService.getServerIdFor(collectionId, "" + event.getObmId().getObmId()))
			.andReturn("" + event.getObmId().getObmId());
		}
		
		for (DeletedEvent deletedEvent : deletedEvents) {
			expect(mappingService.getItemChange(collectionId, deletedEvent.getId().serializeToString()))
				.andReturn(new ItemChange(deletedEvent.getId().serializeToString()));
		}
		
		return eventChanges;
	}

	private Event createEvent(int obmId) {
		Event event = new Event();
		event.setUid(new EventObmId(obmId));
		return event;
	}

	private DeletedEvent createDeletedEvent(EventObmId eventObmId, EventExtId eventExtId) {
		return new DeletedEvent(eventObmId, eventExtId);
	}
	
	private void expectConvertUpdatedEventsToMSEvents(EventChanges eventChanges) throws DaoException, ConversionException {
		for (Event event : eventChanges.getUpdated()) {
			expect(eventService.convertEventToMSEvent(userDataRequest, event))
				.andReturn(null).once();
		}
	}

	@Test
	public void testCreateOrUpdate() throws Exception {
		int collectionId = 1;
		String serverId = "2";
		String clientId = "3";
		IApplicationData data = null;

		expectLoginBehavior();
		
		expectMappingServiceCollectionPathFor(collectionId);
		expect(mappingService.getServerIdFor(collectionId, serverId))
			.andReturn(serverId).once();
		
		Event event = new Event();
		expectGetAndModifyEvent(serverId, event);

		expectEventConvertion(event, true);
		
		replay(loginService, mappingService, calendarClient, eventConverter);
		
		String serverIdFor = calendarBackend.createOrUpdate(userDataRequest, collectionId, serverId, clientId, data);
		
		verify(loginService, mappingService, calendarClient, eventConverter);
		
		assertThat(serverIdFor).isEqualTo(serverId);
	}

	private void expectGetAndModifyEvent(String serverId, Event event) 
			throws ServerFault, EventNotFoundException {
		
		expect(calendarClient.getEventFromId(token, userDataRequest.getUser().getLoginAtDomain(), new EventObmId(serverId)))
			.andReturn(event).once();
		
		expect(calendarClient.modifyEvent(token, "test", event, true, true))
			.andReturn(event).once();
	}

	@Test
	public void testDelete() throws Exception {
		int collectionId = 1;
		String serverId = "2";
		int itemId = 3;

		expectLoginBehavior();
		
		expectMappingServiceCollectionPathFor(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId))
			.andReturn(itemId).once();
		
		expectGetAndRemoveEventFromId(itemId);
		
		replay(loginService, mappingService, calendarClient);
		
		calendarBackend.delete(userDataRequest, collectionId, serverId, true);
		
		verify(mappingService, loginService, calendarClient);
	}

	private void expectGetAndRemoveEventFromId(int itemId)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		EventObmId eventObmId = new EventObmId(itemId);
		Event event = new Event();
		event.setUid(eventObmId);
		expect(calendarClient.getEventFromId(token, userDataRequest.getUser().getLogin(), eventObmId))
			.andReturn(event).once();
		
		calendarClient.removeEventById(token, userDataRequest.getUser().getLogin(), event.getObmId(), event.getSequence(), true);
		expectLastCall();
	}
	
	@Test
	public void testHandleMettingResponse() throws Exception {
		String calendarDisplayName = userDataRequest.getUser().getLoginAtDomain();
		String defaultCalendarName = "obm:\\\\test@test\\calendar\\" + calendarDisplayName;
		
		MSEventUid msEventUid = new MSEventUid("1");
		MSEvent msEvent = new MSEvent();
		msEvent.setUid(msEventUid);
		MSEmail invitation  = new MSEmail();
		invitation.setInvitation(msEvent, MSMessageClass.NOTE);

		expectLoginBehavior();
		
		EventExtId eventExtId = new EventExtId("1");
		expect(eventService.getEventExtIdFor(msEventUid, device))
			.andReturn(eventExtId).once();

		Event event = new Event();
		event.setUid(new EventObmId(1));
		expectGetAndModifyEvent(eventExtId, event);
		expect(calendarClient.changeParticipationState(token, userDataRequest.getUser().getLoginAtDomain(), eventExtId, null, 0, true))
			.andReturn(true);
		
		expectEventConvertion(event, false);
		expect(eventConverter.getParticipationState(null, AttendeeStatus.ACCEPT))
			.andReturn(null).once();
		
		String serverId = "123";
		expect(mappingService.getCollectionIdFor(device, defaultCalendarName))
			.andReturn(1).once();
		expect(mappingService.getServerIdFor(1, "1"))
			.andReturn(serverId);
		
		Builder collectionPathBuilder = expectBuildCollectionPath(calendarDisplayName, defaultCalendarName);
		
		replay(loginService, eventService, calendarClient, eventConverter, mappingService,
				collectionPathBuilderProvider, collectionPathBuilder);

		String serverIdResponse = calendarBackend.handleMeetingResponse(userDataRequest, invitation, AttendeeStatus.ACCEPT);
		
		verify(loginService, eventService, calendarClient, eventConverter, mappingService, 
				collectionPathBuilderProvider, collectionPathBuilder);
		assertThat(serverIdResponse).isEqualTo(serverId);
	}
	
	private void expectGetAndModifyEvent(EventExtId eventExtId, Event event) 
			throws ServerFault, EventNotFoundException {
		
		expect(calendarClient.getEventFromExtId(token, userDataRequest.getUser().getLoginAtDomain(), eventExtId))
			.andReturn(event).once();
		
		expect(calendarClient.modifyEvent(token, userDataRequest.getUser().getLoginAtDomain(), event, true, false))
			.andReturn(event).once();
	}
	
	private void expectEventConvertion(Event event, boolean defaultValue) throws ConversionException {
		expect(eventConverter.isInternalEvent(event, defaultValue))
			.andReturn(false).once();
		
		expect(eventConverter.convert(eq(userDataRequest.getUser()), eq(event), anyObject(MSEvent.class), eq(false)))
			.andReturn(event).once();
	}
	
	@Test
	public void testFetch() throws Exception {
		String serverId1 = "1";
		String serverId2 = "2";
		Integer itemId1 = 1;
		Integer itemId2 = 2;
		
		expectLoginBehavior();
		
		expectGetItemIdFromServerId(serverId1, itemId1);
		expectGetItemIdFromServerId(serverId2, itemId2);
		
		Event event1 = expectGetEventFromId(itemId1);
		Event event2 = expectGetEventFromId(itemId2);

		expectConvertEventToMSEvent(serverId1, event1);
		expectConvertEventToMSEvent(serverId2, event2);
		
		replay(loginService, mappingService, calendarClient, eventService);
		
		List<String> itemIds = ImmutableList.<String> of(serverId1, serverId2);

		List<ItemChange> itemChanges = calendarBackend.fetch(userDataRequest, itemIds, null);
		
		verify(loginService, mappingService, calendarClient, eventService);
		
		assertThat(itemChanges).hasSize(2);
	}

	private void expectGetItemIdFromServerId(String serverId, Integer itemId) {
		expect(mappingService.getItemIdFromServerId(serverId))
			.andReturn(itemId).once();
	}

	private Event expectGetEventFromId(Integer itemId) 
			throws ServerFault, EventNotFoundException {
		
		EventObmId eventObmId = new EventObmId(itemId);
		Event event = new Event();
		event.setUid(eventObmId);
		expect(calendarClient.getEventFromId(token, userDataRequest.getUser().getLoginAtDomain(), eventObmId))
			.andReturn(event).once();
		
		return event;
	}

	private void expectConvertEventToMSEvent(String serverId, Event event) 
			throws DaoException, ConversionException {
		
		MSEvent msEvent = new MSEvent();
		msEvent.setUid(new MSEventUid(serverId));
		expect(eventService.convertEventToMSEvent(userDataRequest, event))
			.andReturn(msEvent).once();
	}

	private void expectLoginBehavior() throws AuthFault {
		expect(loginService.login(userDataRequest.getUser().getLoginAtDomain(), userDataRequest.getPassword()))
			.andReturn(token).once();
		
		loginService.logout(token);
		expectLastCall().once();
	}
	
	private static class CollectionPathTest extends CollectionPath {

		public CollectionPathTest(String collectionPath, PIMDataType pimType, String displayName) {
			super(collectionPath, pimType, displayName);
		}
		
	}
}
