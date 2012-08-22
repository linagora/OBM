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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
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
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class CalendarBackendTest {

	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	
	private MappingService mappingService;
	private CalendarClient calendarClient;
	private EventConverter eventConverter;
	private EventService eventService;
	private LoginService loginService;
	private CollectionPathHelper collectionPathHelper;
	private Provider<CollectionPath.Builder> collectionPathBuilderProvider;
	
	private CalendarBackend calendarBackend;
	
	@Before
	public void setUp() {
		this.user = Factory.create().createUser("test@test", "test@domain", "displayName");
		this.device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		this.userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
		
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
	public void testDefaultCalendarChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		String defaultCalendarName = "obm:\\\\test@test\\calendar\\test@test";
		
		expectMappingServiceCollectionIdsBehavior(defaultCalendarName, 1);
		
		CollectionPath collectionPath = expectCollectionPath();
		CollectionPath.Builder collectionPathBuilder = expectCollectionPathBuilder(collectionPath);
		expectCollectionPathBuilderPovider(collectionPathBuilder);

		replay(mappingService, collectionPath, collectionPathBuilder, collectionPathBuilderProvider);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, null);
		
		verify(mappingService, collectionPath, collectionPathBuilder, collectionPathBuilderProvider);
		
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(1);
		assertThat(hierarchyItemsChanges.getChangedItems()).contains(new ItemChange("calendar 1", "0", "test@test calendar", FolderType.DEFAULT_CALENDAR_FOLDER, false));
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}

	@Test
	public void testCalendarListChanges() throws Exception {
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		AccessToken token = new AccessToken(0, "OBM");
		String defaultCalendarName = "obm:\\\\test@domain\\calendar\\test@domain";
		
		device = new Device.Factory().create(null, "MultipleCalendarsDevice", "iOs 5", "my phone");
		userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);

		expectLoginBehavior(token);
		
		expectMappingServiceCollectionIdsBehavior(defaultCalendarName, 1, 2);
		
		CollectionPath collectionPath = expectCollectionPath();
		CollectionPath.Builder collectionPathBuilder = expectCollectionPathBuilder(collectionPath);
		expectCollectionPathBuilderPovider(collectionPathBuilder);

		expectListCalendars(token);

		expectBuildCollectionPaths(defaultCalendarName, "1test", "2test");
		
		replay(loginService, mappingService, collectionPath, collectionPathBuilder, collectionPathBuilderProvider, calendarClient, collectionPathHelper);
		
		HierarchyItemsChanges hierarchyItemsChanges = calendarBackend.getHierarchyChanges(userDataRequest, lastKnownState, null);
		
		verify(loginService, mappingService, collectionPath, collectionPathBuilder, collectionPathBuilderProvider, calendarClient, collectionPathHelper);
		
		assertThat(hierarchyItemsChanges.getChangedItems()).hasSize(2);
		assertThat(hierarchyItemsChanges.getChangedItems()).contains(new ItemChange("calendar 1", "0", "test@test calendar", FolderType.DEFAULT_CALENDAR_FOLDER, false));
		assertThat(hierarchyItemsChanges.getChangedItems()).contains(new ItemChange("calendar 2", "0", "test@test calendar", FolderType.DEFAULT_CALENDAR_FOLDER, false));
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}

	private void expectBuildCollectionPaths(String defaultCalendarName, String...imapFolders) {
		for (String imapFolder : imapFolders) {
			expect(collectionPathHelper.buildCollectionPath(userDataRequest, PIMDataType.CALENDAR, imapFolder))
				.andReturn(defaultCalendarName).once();
		}
	}

	private CollectionPath expectCollectionPath() {
		CollectionPath collectionPath = createMock(CollectionPath.class);
		expect(collectionPath.collectionPath())
			.andReturn("calendar").anyTimes();
		
		return collectionPath;
	}

	private CollectionPath.Builder expectCollectionPathBuilder(CollectionPath collectionPath) {
		CollectionPath.Builder collectionPathBuilder = createMock(CollectionPath.Builder.class);
		expect(collectionPathBuilder.userDataRequest(userDataRequest))
			.andReturn(collectionPathBuilder).anyTimes();
		
		expect(collectionPathBuilder.pimType(PIMDataType.CALENDAR))
			.andReturn(collectionPathBuilder).anyTimes();
		
		expect(collectionPathBuilder.displayName(userDataRequest.getUser().getLoginAtDomain() + " calendar"))
			.andReturn(collectionPathBuilder).anyTimes();
		
		expect(collectionPathBuilder.build())
			.andReturn(collectionPath).anyTimes();
		
		return collectionPathBuilder;
	}

	private void expectCollectionPathBuilderPovider(CollectionPath.Builder collectionPathBuilder) {
			expect(collectionPathBuilderProvider.get())
				.andReturn(collectionPathBuilder).anyTimes();
	}
	
	private void expectListCalendars(AccessToken token) throws ServerFault {
		CalendarInfo calendarInfo1 = newCalendarInfoObject("1", "test@test");
		CalendarInfo calendarInfo2 = newCalendarInfoObject("2", "test@test");
		
		expect(calendarClient.listCalendars(token))
			.andReturn(new CalendarInfo[] { calendarInfo1, calendarInfo2 }).once();
	}

	private CalendarInfo newCalendarInfoObject(String uid, String mail) {
		CalendarInfo calendarInfo = new CalendarInfo();
		calendarInfo.setUid(uid);
		calendarInfo.setMail(mail);
		return calendarInfo;
	}

	private void expectMappingServiceCollectionIdsBehavior(String defaultCalendarName, Integer...collectionIds)
		throws CollectionNotFoundException, DaoException {
		
		for (Integer collectionId : collectionIds) {
			expect(mappingService.getCollectionIdFor(device, defaultCalendarName))
				.andReturn(collectionId).once();
			
			expect(mappingService.collectionIdToString(collectionId))
				.andReturn("calendar " + collectionId).once();
		}
		
		expect(mappingService.createCollectionMapping(device, "calendar", null))
			.andReturn("1").anyTimes();
	}

	@Test
	public void testGetEstimateSize() throws Exception {
		Date currentDate = DateUtils.getCurrentDate();
		FolderSyncState lastKnownState = new FolderSyncState("1234567890a");
		lastKnownState.setLastSync(currentDate);
		AccessToken token = new AccessToken(0, "OBM");
		int collectionId = 1;

		expectLoginBehavior(token);

		expectMappingServiceCollectionPathFor(collectionId);
		
		EventChanges eventChanges = expectTwoDeletedAndTwoUpdatedEventChanges(currentDate, mappingService, collectionId);
		
		expect(calendarClient.getSync(token, "test", currentDate))
			.andReturn(eventChanges).once();
		
		expect(calendarClient.getUserEmail(token))
			.andReturn("test@test").anyTimes();

		expectConvertUpdatedEventsToMSEvents(eventChanges);
		
		replay(loginService, mappingService, calendarClient, eventService);
		
		BodyPreference.Builder bodyPreferenceBuilder = new BodyPreference.Builder();
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
		AccessToken token = new AccessToken(0, "OBM");
		int collectionId = 1;

		expectLoginBehavior(token);

		expectMappingServiceCollectionPathFor(collectionId);
		
		EventChanges eventChanges = expectTwoDeletedAndTwoUpdatedEventChanges(currentDate, mappingService, collectionId);
		
		expect(calendarClient.getSync(token, "test", currentDate))
			.andReturn(eventChanges).once();
		
		expect(calendarClient.getUserEmail(token))
			.andReturn("test@test").anyTimes();

		expectConvertUpdatedEventsToMSEvents(eventChanges);
		
		replay(loginService, mappingService, calendarClient, eventService);
		
		BodyPreference.Builder bodyPreferenceBuilder = new BodyPreference.Builder();
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
		AccessToken token = new AccessToken(0, "OBM");

		expectLoginBehavior(token);
		
		expectMappingServiceCollectionPathFor(collectionId);
		expect(mappingService.getServerIdFor(collectionId, serverId))
			.andReturn(serverId).once();
		
		Event event = new Event();
		expectGetAndModifyEvent(serverId, token, event);

		expectEventConvertion(event, true);
		
		replay(loginService, mappingService, calendarClient, eventConverter);
		
		String serverIdFor = calendarBackend.createOrUpdate(userDataRequest, collectionId, serverId, clientId, data);
		
		verify(loginService, mappingService, calendarClient, eventConverter);
		
		assertThat(serverIdFor).isEqualTo(serverId);
	}

	private void expectGetAndModifyEvent(String serverId, AccessToken token, Event event) 
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
		AccessToken token = new AccessToken(0, "OBM");

		expectLoginBehavior(token);
		
		expectMappingServiceCollectionPathFor(collectionId);
		expect(mappingService.getItemIdFromServerId(serverId))
			.andReturn(itemId).once();
		
		expectGetAndRemoveEventFromId(itemId, token);
		
		replay(loginService, mappingService, calendarClient);
		
		calendarBackend.delete(userDataRequest, collectionId, serverId, true);
		
		verify(mappingService, loginService, calendarClient);
	}

	private void expectGetAndRemoveEventFromId(int itemId, AccessToken token)
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
		AccessToken token = new AccessToken(0, "OBM");
		String defaultCalendarName = "obm:\\\\test@test\\calendar\\test@test";
		
		MSEventUid msEventUid = new MSEventUid("1");
		MSEvent msEvent = new MSEvent();
		msEvent.setUid(msEventUid);
		MSEmail invitation  = new MSEmail();
		invitation.setInvitation(msEvent, MSMessageClass.NOTE);

		expectLoginBehavior(token);
		
		EventExtId eventExtId = new EventExtId("1");
		expect(eventService.getEventExtIdFor(msEventUid, device))
			.andReturn(eventExtId).once();

		Event event = new Event();
		event.setUid(new EventObmId(1));
		expectGetAndModifyEvent(token, eventExtId, event);
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
		
		replay(loginService, eventService, calendarClient, eventConverter, mappingService);

		String serverIdResponse = calendarBackend.handleMeetingResponse(userDataRequest, invitation, AttendeeStatus.ACCEPT);
		
		verify(loginService, eventService, calendarClient, eventConverter, mappingService);
		assertThat(serverIdResponse).isEqualTo(serverId);
	}
	
	private void expectGetAndModifyEvent(AccessToken token, EventExtId eventExtId, Event event) 
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
		AccessToken token = new AccessToken(0, "OBM");
		String serverId1 = "1";
		String serverId2 = "2";
		Integer itemId1 = 1;
		Integer itemId2 = 2;
		
		expectLoginBehavior(token);
		
		expectGetItemIdFromServerId(serverId1, itemId1);
		expectGetItemIdFromServerId(serverId2, itemId2);
		
		Event event1 = expectGetEventFromId(token, itemId1);
		Event event2 = expectGetEventFromId(token, itemId2);

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

	private Event expectGetEventFromId(AccessToken token, Integer itemId) 
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

	private void expectLoginBehavior(AccessToken token) throws AuthFault {
		expect(loginService.login(userDataRequest.getUser().getLoginAtDomain(), userDataRequest.getPassword()))
			.andReturn(token).once();
		
		loginService.logout(token);
		expectLastCall().once();
	}
}
