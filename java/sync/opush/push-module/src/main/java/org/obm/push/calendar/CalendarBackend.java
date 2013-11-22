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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.BackendWindowingService;
import org.obm.push.backend.BackendWindowingService.BackendChangesProvider;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.OpushCollection;
import org.obm.push.backend.PIMBackend;
import org.obm.push.backend.PathsToCollections;
import org.obm.push.backend.PathsToCollections.Builder;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.HierarchyChangesException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.resource.ResourcesUtils;
import org.obm.push.service.ClientIdService;
import org.obm.push.service.EventService;
import org.obm.push.service.impl.MappingService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.ConsistencyEventChangesLogger;
import org.obm.sync.items.EventChanges;
import org.obm.sync.services.ICalendar;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CalendarBackend extends ObmSyncBackend implements PIMBackend {


	private static final String DEFAULT_CALENDAR_PARENT_ID = "0";
	private static final String DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX = " calendar";
	
	private final EventConverter eventConverter;
	private final EventService eventService;
	private final CalendarClient.Factory calendarClientFactory;
	private final ConsistencyEventChangesLogger consistencyLogger;
	private final EventExtId.Factory eventExtIdFactory;
	private final BackendWindowingService backendWindowingService;
	private final ClientIdService clientIdService;
	@Inject
	@VisibleForTesting CalendarBackend(MappingService mappingService, 
			CalendarClient.Factory calendarClientFactory, 
			EventConverter eventConverter, 
			EventService eventService,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider, ConsistencyEventChangesLogger consistencyLogger,
			EventExtId.Factory eventExtIdFactory,
			BackendWindowingService backendWindowingService,
			ClientIdService clientIdService) {
		
		super(mappingService, collectionPathBuilderProvider);
		this.calendarClientFactory = calendarClientFactory;
		this.eventConverter = eventConverter;
		this.eventService = eventService;
		this.consistencyLogger = consistencyLogger;
		this.eventExtIdFactory = eventExtIdFactory;
		this.backendWindowingService = backendWindowingService;
		this.clientIdService = clientIdService;
	}
	
	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.CALENDAR;
	}
	
	@Override
	public HierarchyCollectionChanges getHierarchyChanges(UserDataRequest udr, 
			FolderSyncState lastKnownState, FolderSyncState outgoingSyncState)
			throws DaoException {

		try {
			PathsToCollections contactsCollections = null;
			if (!udr.checkHint("hint.multipleCalendars", false)) {
				contactsCollections = getDefaultCalendarCollectionPaths(udr);
			} else {
				contactsCollections = getCalendarCollectionPaths(udr);
			}
			snapshotHierarchy(udr, contactsCollections.pathKeys(), outgoingSyncState);
			return computeChanges(udr, lastKnownState, contactsCollections);
		} catch (CollectionNotFoundException e) {
			throw new HierarchyChangesException(e);
		}
	}

	private HierarchyCollectionChanges computeChanges(UserDataRequest udr, FolderSyncState lastKnownState,
			PathsToCollections contactsCollections) throws DaoException, CollectionNotFoundException {

		Set<CollectionPath> lastKnownCollections = lastKnownCollectionPath(udr, lastKnownState, getPIMDataType());
		
		Set<CollectionPath> deletedContactCollections = Sets.difference(lastKnownCollections, contactsCollections.pathKeys());
		Iterable<OpushCollection> newContactCollections = addedCollections(lastKnownCollections, contactsCollections);

		return buildHierarchyItemsChanges(udr, newContactCollections, deletedContactCollections);
	}

	private PathsToCollections getCalendarCollectionPaths(UserDataRequest udr) {
		
		Builder builder = PathsToCollections.builder();
		AccessToken token = getAccessToken(udr);
		try {
			CalendarInfo[] cals = getCalendarClient(udr).listCalendars(token, null, null, null);
			for (CalendarInfo ci : cals) {
				CollectionPath collectionPath = collectionPathOfCalendar(udr, ci.getUid());
				builder.put(collectionPath, OpushCollection.builder()
							.collectionPath(collectionPath)
							.displayName(ci.getUid() + DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX)
							.build());
			}
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
		return builder.build();
	}

	private PathsToCollections getDefaultCalendarCollectionPaths(UserDataRequest udr) {
		CollectionPath collectionPath = defaultCalendar(udr);
		return PathsToCollections.builder()
				.put(collectionPath, OpushCollection.builder()
						.collectionPath(collectionPath)
						.displayName(udr.getUser().getLogin() + DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX)
						.build())
				.build();
	}

	@Override
	protected CollectionChange createCollectionChange(UserDataRequest udr, OpushCollection collection)
			throws CollectionNotFoundException, DaoException {
		
		CollectionPath collectionPath = collection.collectionPath();
		Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath.collectionPath());
		
		return CollectionChange.builder()
				.collectionId(mappingService.collectionIdToString(collectionId))
				.parentCollectionId(DEFAULT_CALENDAR_PARENT_ID)
				.displayName(collection.displayName())
				.folderType(getCollectionFolderType(udr, collectionPath))
				.isNew(true)
				.build();
	}

	private FolderType getCollectionFolderType(UserDataRequest udr, CollectionPath collectionPath) {
		if (isDefaultCalendarCollectionPath(udr, collectionPath)) {
			return FolderType.DEFAULT_CALENDAR_FOLDER;
		} else {
			return FolderType.USER_CREATED_CALENDAR_FOLDER;
		}
	}

	@Override
	protected CollectionDeletion createCollectionDeletion(UserDataRequest udr, CollectionPath collectionPath)
			throws CollectionNotFoundException, DaoException {
		
		Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath.collectionPath());
		return CollectionDeletion.builder()
				.collectionId(mappingService.collectionIdToString(collectionId))
				.build();
	}
	
	private boolean isDefaultCalendarCollectionPath(UserDataRequest udr, CollectionPath collectionPath) {
		return udr.getUser().getLogin().equalsIgnoreCase(collectionPath.backendName());
	}

	private CollectionPath defaultCalendar(UserDataRequest udr) {
		return collectionPathOfCalendar(udr, udr.getUser().getLogin());
	}

	private CollectionPath collectionPathOfCalendar(UserDataRequest udr, String calendar) {
		return collectionPathBuilderProvider.get()
			.userDataRequest(udr)
			.pimType(PIMDataType.CALENDAR)
			.backendName(calendar)
			.build();
	}

	@Override
	public int getItemEstimateSize(UserDataRequest udr, ItemSyncState state, Integer collectionId, 
				SyncCollectionOptions collectionOptions) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, 
				ConversionException, HierarchyChangedException {
		
		DataDelta dataDelta = getAllChanges(udr, state, collectionId, collectionOptions, state.getSyncKey());
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(final UserDataRequest udr, final ItemSyncState itemSyncState, final AnalysedSyncCollection syncCollection, 
			SyncClientCommands clientCommands, final SyncKey newSyncKey)
		throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException,
			ConversionException, HierarchyChangedException {

		return backendWindowingService.windowedChanges(udr, itemSyncState, syncCollection, clientCommands, newSyncKey, new BackendChangesProvider() {
			
			@Override
			public DataDelta getAllChanges() {
				return CalendarBackend.this.getAllChanges(udr, itemSyncState, syncCollection.getCollectionId(), 
						syncCollection.getOptions(), newSyncKey);
			}
		});
	}

	@VisibleForTesting DataDelta getAllChanges(UserDataRequest udr, ItemSyncState state,
			Integer collectionId, SyncCollectionOptions collectionOptions, SyncKey newSyncKey) {
		
		CollectionPath collectionPath = buildCollectionPath(udr, collectionId);
		AccessToken token = getAccessToken(udr);
		
		DataDelta delta = null;

		try {
			
			EventChanges changes = null;
			Date filteredSyncDate = state.getFilteredSyncDate(collectionOptions.getFilterType());
			boolean syncFiltered = filteredSyncDate != state.getSyncDate();
			if (state.isInitial()) {
				changes = initialSync(collectionPath, token, filteredSyncDate, syncFiltered, udr);
			} else {
				changes = sync(collectionPath, token, filteredSyncDate, syncFiltered, udr);
			}
			
			consistencyLogger.log(logger, changes);
			logger.info("Event changes [ {} ]", changes.getUpdated().size());
			logger.info("Event changes LastSync [ {} ]", changes.getLastSync().toString());
			
			delta = buildDataDelta(udr, collectionId, token, changes, newSyncKey);
			
			logger.info("getContentChanges( {}, lastSync = {} ) => {}",
				collectionPath.backendName(), filteredSyncDate, delta.statistics());
			
			return delta;
		} catch (org.obm.sync.NotAllowedException e) {
			logger.warn(e.getMessage(), e);
			return delta;
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
	}

	private EventChanges initialSync(CollectionPath collectionPath, AccessToken token, Date filteredSyncDate, boolean syncFiltered, UserDataRequest udr)
			throws ServerFault, org.obm.sync.NotAllowedException {
		
		if (syncFiltered) {
			return getCalendarClient(udr).getFirstSyncEventDate(token, collectionPath.backendName(), filteredSyncDate);
		} 
		return getCalendarClient(udr).getFirstSync(token, collectionPath.backendName(), filteredSyncDate);
	}

	private EventChanges sync(CollectionPath collectionPath, AccessToken token, Date filteredSyncDate, boolean syncFiltered, UserDataRequest udr) 
			throws ServerFault, org.obm.sync.NotAllowedException {
		
		if (syncFiltered) {
			return getCalendarClient(udr).getSyncEventDate(token, collectionPath.backendName(), filteredSyncDate);
		} 
		return getCalendarClient(udr).getSync(token, collectionPath.backendName(), filteredSyncDate);
	}
	
	@VisibleForTesting DataDelta buildDataDelta(UserDataRequest udr, Integer collectionId,
			AccessToken token, EventChanges changes, SyncKey newSyncKey) throws ServerFault,
			DaoException, ConversionException {
		final String userEmail = getCalendarClient(udr).getUserEmail(token);
		Preconditions.checkNotNull(userEmail, "User has no email address");

		return DataDelta.builder()
				.changes(addOrUpdateEventFilter(changes.getUpdated(), userEmail, collectionId, udr))
				.deletions(removeEventFilter(changes.getDeletedEvents(), collectionId))
				.syncDate(changes.getLastSync())
				.syncKey(newSyncKey)
				.build();
	}

	private List<ItemChange> addOrUpdateEventFilter(Set<Event> events, String userEmail,
			Integer collectionId, UserDataRequest udr) throws DaoException, ConversionException {
		
		List<ItemChange> items = Lists.newArrayList();
		for (final Event event : events) {
			if (checkIfEventCanBeAdded(event, userEmail) && event.getRecurrenceId() == null) {
				String serverId = getServerIdFor(collectionId, event.getObmId());
				ItemChange change = createItemChangeToAddFromEvent(udr, event, serverId);
				items.add(change);
			}	
		}
		return items;
	}
	
	private List<ItemDeletion> removeEventFilter(Iterable<DeletedEvent> eventsRemoved, Integer collectionId) {
		
		List<ItemDeletion> deletions = Lists.newArrayList();
		for (final DeletedEvent eventRemove : eventsRemoved) {
			deletions.add(ItemDeletion.builder()
					.serverId(ServerId.buildServerIdString(collectionId, eventRemove.getId().getObmId()))
					.build());
		}
		return deletions;
	}

	private boolean checkIfEventCanBeAdded(Event event, String userEmail) {
		for (final Attendee attendee : event.getAttendees()) {
			if (userEmail.equals(attendee.getEmail()) && 
					Participation.declined().equals(attendee.getParticipation())) {
				return false;
			}
		}
		return true;
	}
	
	private CollectionPath buildCollectionPath(UserDataRequest udr, int collectionId) {
		return collectionPathBuilderProvider
			.get()
			.userDataRequest(udr)
			.fullyQualifiedCollectionPath(mappingService.getCollectionPathFor(collectionId))
			.build();
	}

	private ItemChange createItemChangeToAddFromEvent(final UserDataRequest udr, final Event event, String serverId)
			throws DaoException, ConversionException {
		
		IApplicationData ev = eventService.convertEventToMSEvent(udr, event);
		ItemChange ic = new ItemChangeBuilder()
			.serverId(serverId)
			.withApplicationData(ev)
			.build();
		return ic;
	}

	private String getServerIdFor(Integer collectionId, EventObmId uid) {
		return ServerId.buildServerIdString(collectionId, uid.getObmId());
	}

	@Override
	public String createOrUpdate(UserDataRequest udr, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, 
			DaoException, UnexpectedObmSyncServerException, ItemNotFoundException, ConversionException, HierarchyChangedException {

		MSEvent msEvent = (MSEvent) data;

		CollectionPath collectionPath = buildCollectionPath(udr, collectionId);
		AccessToken token = getAccessToken(udr);
		
		logger.info("createOrUpdate( calendar = {}, serverId = {} )", collectionPath.backendName(), serverId);
		
		try {
			EventExtId eventExtId = getEventExtId(udr, msEvent);
			Event oldEvent = fetchReferenceEvent(token, serverId, eventExtId, collectionPath, udr);
			EventObmId eventId = getEventId(oldEvent);
			
			EventObmId newEventId = chooseBackendChange(udr, msEvent, collectionPath, token, eventExtId, oldEvent, eventId, clientId);
			
			return getServerIdFor(collectionId, newEventId);
		} catch (org.obm.sync.NotAllowedException e) {
			logger.warn(e.getMessage(), e);
			throw new ItemNotFoundException(e);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}

	private EventExtId getEventExtId(UserDataRequest udr, MSEvent msEvent) {
		try {
			return eventService.getEventExtIdFor(msEvent.getUid(), udr.getDevice());
		} catch (EventNotFoundException e) {
			return null;
		}
	}

	private EventObmId chooseBackendChange(UserDataRequest udr, MSEvent msEvent,
			CollectionPath collectionPath, AccessToken token,
			EventExtId eventExtId, Event oldEvent, final EventObmId eventId, String clientId)
			throws org.obm.sync.NotAllowedException, ServerFault {
		
		if (isParticipationChangeUpdate(collectionPath, oldEvent)) {
			updateUserStatus(oldEvent, AttendeeStatus.ACCEPT, token, collectionPath, udr);
			return eventId;
		} else if (isEventModification(eventId)){
			updateEvent(token, udr, collectionPath, oldEvent, eventExtId, msEvent);
			return eventId;
		} else {
			return createEvent(udr, token, collectionPath, oldEvent, msEvent, eventExtId, clientId);
		}
	}

	private boolean isEventModification(EventObmId eventId) {
		return eventId != null;
	}

	private EventObmId getEventId(Event oldEvent) {
		if (oldEvent != null) {
			return oldEvent.getObmId();
		}
		return null;
	}

	private Event fetchReferenceEvent(AccessToken token,
			String serverId, EventExtId eventExtId, CollectionPath collectionPath, UserDataRequest udr)
					throws ServerFault, EventNotFoundException, org.obm.sync.NotAllowedException {
		if (serverId != null) {
			EventObmId id = convertServerIdToEventObmId(serverId);
			return getCalendarClient(udr).getEventFromId(token, collectionPath.backendName(), id);	
		} else if (eventExtId != null && !Strings.isNullOrEmpty(eventExtId.getExtId())) {
			return getEventFromExtId(token, eventExtId, collectionPath, udr);
		}
		return null;
	}

	@VisibleForTesting boolean isParticipationChangeUpdate(CollectionPath collectionPath, Event oldEvent) {
		return oldEvent != null && !belongsToCalendar(oldEvent, collectionPath.backendName());
	}

	@VisibleForTesting boolean belongsToCalendar(Event oldEvent, String calendarName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(calendarName));
		return calendarName.equalsIgnoreCase(oldEvent.getOwner());
	}

	private void updateEvent(AccessToken token, UserDataRequest udr, 
			CollectionPath collectionPath, Event oldEvent, 
			EventExtId eventExtId, MSEvent msEvent) throws ServerFault, org.obm.sync.NotAllowedException {
		
		boolean isInternal = eventConverter.isInternalEvent(oldEvent, eventExtId);
		Event event = convertMSObjectToObmObject(udr, msEvent, oldEvent, isInternal);
		event.setUid(oldEvent.getObmId());
		setSequence(oldEvent, event);
		if (event.getExtId() == null || event.getExtId().getExtId() == null) {
			event.setExtId(oldEvent.getExtId());
		}
		getCalendarClient(udr).modifyEvent(token, collectionPath.backendName(), event, true, true);
	}

	private void setSequence(Event oldEvent, Event event) {
		if (event.hasImportantChanges(oldEvent)) {
			event.setSequence(oldEvent.getSequence() + 1);
		} else {
			event.setSequence(oldEvent.getSequence());
		}
	}

	private EventObmId createEvent(UserDataRequest udr, AccessToken token,
			CollectionPath collectionPath, Event oldEvent, MSEvent msEvent, EventExtId eventExtId, String clientId)
			throws ServerFault, DaoException, org.obm.sync.NotAllowedException {
		
		boolean isInternal = eventConverter.isInternalEvent(oldEvent, eventExtId);
		Event event = convertMSObjectToObmObject(udr, msEvent, oldEvent, isInternal);
		assignExtId(udr, msEvent, eventExtId, event);
		try { 
			return getCalendarClient(udr).createEvent(token, collectionPath.backendName(), event, true, clientIdService.hash(udr, clientId));
		} catch (EventAlreadyExistException e) {
			return getEventIdFromExtId(token, collectionPath, event, udr);
		}
	}

	private void assignExtId(UserDataRequest udr, MSEvent msEvent, EventExtId eventExtId, Event event) {
		if (eventExtId == null) {
			EventExtId newEventExtId = eventExtIdFactory.generate();
			eventService.trackEventExtIdMSEventUidTranslation(newEventExtId, msEvent.getUid(), udr.getDevice());
			event.setExtId(newEventExtId);
		} else {
			event.setExtId(eventExtId);
		}
	}
	
	private EventObmId convertServerIdToEventObmId(String serverId) {
		return new EventObmId(mappingService.getItemIdFromServerId(serverId));
	}

	private Event convertMSObjectToObmObject(UserDataRequest udr,
			MSEvent data, Event oldEvent, boolean isInternal) throws ConversionException {
		return eventConverter.convert(udr.getUser(), oldEvent, data, isInternal);
	}
	
	private EventObmId getEventIdFromExtId(AccessToken token, CollectionPath collectionPath, Event event, UserDataRequest udr)
			throws UnexpectedObmSyncServerException, org.obm.sync.NotAllowedException {
		
		try {
			return getCalendarClient(udr).getEventObmIdFromExtId(token, collectionPath.backendName(), event.getExtId());
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}

	@Override
	public void delete(UserDataRequest udr, Integer collectionId, String serverId, Boolean moveToTrash) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, ItemNotFoundException {

		CollectionPath collectionPath = buildCollectionPath(udr, collectionId);
		if (serverId != null) {

			AccessToken token = getAccessToken(udr);
			try {
				logger.info("Delete event serverId {} in calendar {}", serverId, collectionPath.backendName());
				//FIXME: not transactional
				Event evr = getEventFromServerId(token, collectionPath, serverId, udr);
				getCalendarClient(udr).removeEventById(token, collectionPath.backendName(), evr.getObmId(), evr.getSequence(), true);
			} catch (ServerFault e) {
				throw new UnexpectedObmSyncServerException(e);
			} catch (EventNotFoundException e) {
				throw new ItemNotFoundException(e);
			} catch (org.obm.sync.NotAllowedException e) {
				logger.warn(e.getMessage(), e);
				throw new ItemNotFoundException(e);
			}
		}
	}

	public String handleMeetingResponse(UserDataRequest udr, MSEmail invitation, AttendeeStatus status) 
			throws UnexpectedObmSyncServerException, CollectionNotFoundException, DaoException,
			ItemNotFoundException, ConversionException, HierarchyChangedException {
		
		MSEvent event = invitation.getInvitation();
		CollectionPath collectionPath = defaultCalendar(udr);
		
		AccessToken at = getAccessToken(udr);
		try {
			logger.info("handleMeetingResponse = {}", event.getUid());
			Event obmEvent = createOrModifyInvitationEvent(udr, at, event, collectionPath);
			updateUserStatus(obmEvent, status, at, collectionPath, udr);
			Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath.collectionPath());
			return getServerIdFor(collectionId, obmEvent.getObmId());
		} catch (org.obm.sync.NotAllowedException e) {
			logger.warn(e.getMessage(), e);
			throw new ItemNotFoundException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw e;
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		}
	}

	private Event createOrModifyInvitationEvent(UserDataRequest udr, AccessToken at, 
			MSEvent event, CollectionPath collectionPath) 
		throws UnexpectedObmSyncServerException, EventNotFoundException, 
			ConversionException, DaoException, org.obm.sync.NotAllowedException {
		
		try {
			EventExtId extId = getEventExtId(udr, event);
			Event previousEvent = getEventFromExtId(at, extId, collectionPath, udr);
						
			boolean isInternal = eventConverter.isInternalEvent(previousEvent, false);
			Event newEvent = convertMSObjectToObmObject(udr, event, previousEvent, isInternal);
			newEvent.setExtId(extId);
			
			if (previousEvent == null) {
				try {
					logger.info("createOrModifyInvitationEvent : create new event {}", newEvent.getObmId());
					EventObmId id = getCalendarClient(udr).createEvent(at, collectionPath.backendName(), newEvent, isInternal, null);
					return getCalendarClient(udr).getEventFromId(at, collectionPath.backendName(), id);
				} catch (EventAlreadyExistException e) {
					throw new UnexpectedObmSyncServerException("it's not possible because getEventFromExtId == null");
				}
				
			} else {
			
				newEvent.setUid(previousEvent.getObmId());
				newEvent.setSequence(previousEvent.getSequence());
				if (!previousEvent.isInternalEvent()) {
					logger.info("createOrModifyInvitationEvent : update event {}", newEvent.getObmId());
					previousEvent = getCalendarClient(udr).modifyEvent(at, collectionPath.backendName(), newEvent, true, false);
				}
				return previousEvent;
			}	
			
		} catch (ServerFault fault) {
			throw new UnexpectedObmSyncServerException(fault);
		}		
	}

	private Event getEventFromExtId(AccessToken at, EventExtId eventExtId, 
			CollectionPath collectionPath, UserDataRequest udr) 
		throws ServerFault, org.obm.sync.NotAllowedException {
		
		try {
			return getCalendarClient(udr).getEventFromExtId(at, collectionPath.backendName(), eventExtId);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}
	
	private void updateUserStatus(Event event, AttendeeStatus status, AccessToken at, CollectionPath collectionPath, UserDataRequest udr)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, org.obm.sync.NotAllowedException {
		
		logger.info("update user status {} in calendar {}", status, collectionPath.backendName());
		Participation participationStatus = eventConverter.getParticipation(status);
		try {
			getCalendarClient(udr).changeParticipationState(at, collectionPath.backendName(), event.getExtId(), 
					participationStatus, event.getSequence(), true);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
	}

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds, SyncCollectionOptions syncCollectionOptions,
				ItemSyncState previousItemSyncState)
			throws DaoException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
	
		return fetch(udr, collectionId, fetchServerIds, syncCollectionOptions);
	}
	
	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds, SyncCollectionOptions syncCollectionOptions)
			throws DaoException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
	
		CollectionPath collectionPath = buildCollectionPath(udr, collectionId);
		
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AccessToken token = getAccessToken(udr);
		for (String serverId : fetchServerIds) {
			try {
				Event event = getEventFromServerId(token, collectionPath, serverId, udr);
				if (event != null) {
					ItemChange ic = createItemChangeToAddFromEvent(udr, event, serverId);
					ret.add(ic);
				}
			} catch (org.obm.sync.NotAllowedException e) {
				logger.warn(e.getMessage(), e);
			} catch (EventNotFoundException e) {
				logger.error("event from serverId {} not found.", serverId);
			} catch (ServerFault e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		return ret;
	}
	
	private Event getEventFromServerId(AccessToken token, CollectionPath collectionPath, String serverId, UserDataRequest udr) throws ServerFault, EventNotFoundException, org.obm.sync.NotAllowedException {
		Integer itemId = mappingService.getItemIdFromServerId(serverId);
		if (itemId == null) {
			return null;
		}
		return getCalendarClient(udr).getEventFromId(token, collectionPath.backendName(), new EventObmId(itemId));
	}

	@Override
	public String move(UserDataRequest udr, String srcFolder, String dstFolder,
			String messageId) throws CollectionNotFoundException,
			ProcessingEmailException {
		return null;
	}

	@Override
	public void emptyFolderContent(UserDataRequest udr, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException {
		throw new NotAllowedException(
				"emptyFolderContent is only supported for emails, collection was "
						+ collectionPath);
	}

	private ICalendar getCalendarClient(UserDataRequest udr) {
		return calendarClientFactory.create(ResourcesUtils.getHttpClient(udr));
	}
}
