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

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.OpushCollection;
import org.obm.push.backend.PIMBackend;
import org.obm.push.backend.PathsToCollections;
import org.obm.push.backend.PathsToCollections.Builder;
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
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.calendar.ConsistencyEventChangesLogger;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.EventChanges;
import org.obm.sync.services.ICalendar;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class CalendarBackend extends ObmSyncBackend implements PIMBackend {


	private static final String DEFAULT_CALENDAR_PARENT_ID = "0";
	private static final String DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX = " calendar";
	
	private final EventConverter eventConverter;
	private final EventService eventService;
	private final ICalendar calendarClient;
	private final ConsistencyEventChangesLogger consistencyLogger;

	@Inject
	@VisibleForTesting CalendarBackend(MappingService mappingService, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			EventConverter eventConverter, 
			EventService eventService,
			LoginService login,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider, ConsistencyEventChangesLogger consistencyLogger) {
		super(mappingService, login, collectionPathBuilderProvider);
		this.calendarClient = calendarClient;
		this.eventConverter = eventConverter;
		this.eventService = eventService;
		this.consistencyLogger = consistencyLogger;
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
		AccessToken token = login(udr);
		try {
			CalendarInfo[] cals = calendarClient.listCalendars(token);
			for (CalendarInfo ci : cals) {
				CollectionPath collectionPath = collectionPathOfCalendar(udr, ci.getMail());
				builder.put(collectionPath, OpushCollection.builder()
							.collectionPath(collectionPath)
							.displayName(ci.getMail() + DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX)
							.build());
			}
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
		return builder.build();
	}

	private PathsToCollections getDefaultCalendarCollectionPaths(UserDataRequest udr) {
		CollectionPath collectionPath = collectionPathOfCalendar(udr, udr.getUser().getLoginAtDomain());
		return PathsToCollections.builder()
				.put(collectionPath, OpushCollection.builder()
						.collectionPath(collectionPath)
						.displayName(udr.getUser().getLoginAtDomain() + DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX)
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
		return udr.getUser().getLoginAtDomain().equalsIgnoreCase(collectionPath.backendName());
	}

	private String getDefaultCalendarName(UserDataRequest udr) {
		return collectionPathOfCalendar(udr, udr.getUser().getLoginAtDomain()).collectionPath();
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
			SyncCollectionOptions syncCollectionOptions) throws CollectionNotFoundException, 
			DaoException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
		
		DataDelta dataDelta = getChanged(udr, state, collectionId, syncCollectionOptions, state.getSyncKey());
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(UserDataRequest udr, ItemSyncState state, Integer collectionId, 
			SyncCollectionOptions syncCollectionOptions, SyncKey newSyncKey) throws DaoException,
			CollectionNotFoundException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
		
		AccessToken token = login(udr);
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		String calendar = parseCalendarName(collectionPath);

		ItemSyncState newState = state.newWindowedSyncState(syncCollectionOptions.getFilterType());
		try {
			
			EventChanges changes = null;
			if (newState.isSyncFiltred()) {
				changes = calendarClient.getSyncEventDate(token, calendar, newState.getSyncDate());
			} else {
				changes = calendarClient.getSync(token, calendar, newState.getSyncDate());
			}
			
			consistencyLogger.log(logger, changes);
			logger.info("Event changes [ {} ]", changes.getUpdated().size());
			logger.info("Event changes LastSync [ {} ]", changes.getLastSync().toString());
		
			DataDelta delta = 
					buildDataDelta(udr, collectionId, token, changes);
			
			logger.info("getContentChanges( {}, {}, lastSync = {} ) => {}",
				new Object[]{calendar, collectionPath, newState.getSyncDate(), delta.statistics()});
			
			return delta;
		} catch (org.obm.sync.NotAllowedException e) {
			throw new HierarchyChangedException(e);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
	}

	@VisibleForTesting DataDelta buildDataDelta(UserDataRequest udr, Integer collectionId,
			AccessToken token, EventChanges changes) throws ServerFault,
			DaoException, ConversionException {
		final String userEmail = calendarClient.getUserEmail(token);
		Preconditions.checkNotNull(userEmail, "User has no email address");

		return DataDelta.builder()
				.changes(addOrUpdateEventFilter(changes.getUpdated(), userEmail, collectionId, udr))
				.deletions(removeEventFilter(changes.getDeletedEvents(), collectionId))
				.syncDate(changes.getLastSync())
				.build();
	}

	private List<ItemChange> addOrUpdateEventFilter(List<Event> events, String userEmail,
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
	
	private String parseCalendarName(String collectionPath) {
		// parse obm:\\thomas@zz.com\calendar\sylvaing@zz.com
		int slash = collectionPath.lastIndexOf("\\");
		int at = collectionPath.lastIndexOf("@");
		return collectionPath.substring(slash + 1, at);
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
		return mappingService.getServerIdFor(collectionId, uid.serializeToString());
	}

	@Override
	public String createOrUpdate(UserDataRequest udr, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws CollectionNotFoundException, ProcessingEmailException, 
			DaoException, UnexpectedObmSyncServerException, ItemNotFoundException, ConversionException, HierarchyChangedException {

		AccessToken token = login(udr);
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		logger.info("createOrUpdate( collectionPath = {}, serverId = {} )", new Object[]{collectionPath, serverId});
		
		EventObmId eventId = null;
		Event event = null;
		try {
			
			Event oldEvent = null;
			if (serverId != null) {
				eventId = convertServerIdToEventObmId(serverId);
				oldEvent = calendarClient.getEventFromId(token, udr.getUser().getLoginAtDomain(), eventId);	
			}

			boolean isInternal = eventConverter.isInternalEvent(oldEvent, true);
			event = convertMSObjectToObmObject(udr, data, oldEvent, isInternal);

			if (eventId != null) {
				event.setUid(eventId);
				setSequence(oldEvent, event);
				updateCalendarEntity(calendarClient, token, collectionPath, oldEvent, event);
			} else {
				eventId = createCalendarEntity(udr, calendarClient, token, collectionPath, event, data);
			}
		} catch (org.obm.sync.NotAllowedException e) {
			throw new HierarchyChangedException(e);
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventAlreadyExistException e) {
			try {
				eventId = getEventIdFromExtId(token, collectionPath, calendarClient, event);
			}
			catch (org.obm.sync.NotAllowedException nae) {
				throw new HierarchyChangedException(nae);
			}
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(token);
		}
		
		return getServerIdFor(collectionId, eventId);
	}

	private void updateCalendarEntity(ICalendar cc, AccessToken token, String collectionPath, Event old, Event event) throws ServerFault, org.obm.sync.NotAllowedException {
		if (event.getExtId() == null || event.getExtId().getExtId() == null) {
			event.setExtId(old.getExtId());
		}
		cc.modifyEvent(token, parseCalendarName(collectionPath), event, true, true);
	}

	private void setSequence(Event oldEvent, Event event) {
		if (event.hasImportantChanges(oldEvent)) {
			event.setSequence(oldEvent.getSequence() + 1);
		} else {
			event.setSequence(oldEvent.getSequence());
		}
	}

	private EventObmId createCalendarEntity(UserDataRequest udr, ICalendar cc,
			AccessToken token, String collectionPath, Event event, IApplicationData data)
			throws ServerFault, EventAlreadyExistException, DaoException, org.obm.sync.NotAllowedException {
		switch (event.getType()) {
		case VEVENT:
			return createEvent(udr, cc, token, collectionPath, event, (MSEvent) data);
		case VTODO:
			return createTodo(cc, token, collectionPath, event);
		default:
			throw new InvalidParameterException("unsupported type " + event.getType());
		}
	}

	private EventObmId createTodo(ICalendar cc,
			AccessToken token, String collectionPath, Event event)
			throws ServerFault, EventAlreadyExistException, org.obm.sync.NotAllowedException {
		return cc.createEvent(token, parseCalendarName(collectionPath), event, true);
	}

	private EventObmId createEvent(UserDataRequest udr, ICalendar cc,
			AccessToken token, String collectionPath, Event event, MSEvent msEvent)
			throws ServerFault, EventAlreadyExistException, DaoException, org.obm.sync.NotAllowedException {
		EventExtId eventExtId = generateExtId();
		event.setExtId(eventExtId);
		eventService.trackEventExtIdMSEventUidTranslation(eventExtId, msEvent.getUid(), udr.getDevice());
		EventObmId eventId = cc.createEvent(token, parseCalendarName(collectionPath), event, true);
		return eventId;
	}

	private EventExtId generateExtId() {
		UUID uuid = UUID.randomUUID();
		return new EventExtId(uuid.toString());
	}
	
	private EventObmId convertServerIdToEventObmId(String serverId) {
		int idx = serverId.lastIndexOf(":");
		return new EventObmId(serverId.substring(idx + 1));
	}

	private Event convertMSObjectToObmObject(UserDataRequest udr,
			IApplicationData data, Event oldEvent, boolean isInternal) throws ConversionException {
		return eventConverter.convert(udr.getUser(), oldEvent, (MSEvent) data, isInternal);
	}
	
	private EventObmId getEventIdFromExtId(AccessToken token, String collectionPath, ICalendar cc, Event event)
			throws UnexpectedObmSyncServerException, org.obm.sync.NotAllowedException {
		
		try {
			return cc.getEventObmIdFromExtId(token, parseCalendarName(collectionPath), event.getExtId());
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
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		if (serverId != null) {

			AccessToken token = login(udr);
			try {
				logger.info("Delete event serverId {}", serverId);
				//FIXME: not transactional
				String calendarName = parseCalendarName(collectionPath);
				Event evr = getEventFromServerId(token, calendarName, serverId);
				calendarClient.removeEventById(token, calendarName, evr.getObmId(), evr.getSequence(), true);
			} catch (ServerFault e) {
				throw new UnexpectedObmSyncServerException(e);
			} catch (EventNotFoundException e) {
				throw new ItemNotFoundException(e);
			} catch (org.obm.sync.NotAllowedException e) {
				logger.error(e.getMessage(), e);
			} finally {
				logout(token);
			}
		}
	}

	public String handleMeetingResponse(UserDataRequest udr, MSEmail invitation, AttendeeStatus status) 
			throws UnexpectedObmSyncServerException, CollectionNotFoundException, DaoException,
			ItemNotFoundException, ConversionException, HierarchyChangedException {
		
		MSEvent event = invitation.getInvitation();
		AccessToken at = login(udr);
		try {
			logger.info("handleMeetingResponse = {}", event.getUid());
			Event obmEvent = createOrModifyInvitationEvent(udr, event, at);
			event.setObmSequence(obmEvent.getSequence());
			return updateUserStatus(udr, event, obmEvent, status, calendarClient, at);
		} catch (org.obm.sync.NotAllowedException e) {
			throw new HierarchyChangedException(e);
		} catch (UnexpectedObmSyncServerException e) {
			throw e;
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(at);
		}
	}

	private Event createOrModifyInvitationEvent(UserDataRequest udr, MSEvent event, AccessToken at) 
			throws UnexpectedObmSyncServerException, EventNotFoundException, ConversionException, DaoException, org.obm.sync.NotAllowedException {
		
		try {
			
			EventExtId extId = eventService.getEventExtIdFor(event.getUid(), udr.getDevice());
			Event previousEvent = getEventFromExtId(udr, extId, at);
			
			boolean isInternal = eventConverter.isInternalEvent(previousEvent, false);
			Event newEvent = convertMSObjectToObmObject(udr, event, previousEvent, isInternal);
			newEvent.setExtId(extId);
			
			if (previousEvent == null) {
				try {
					logger.info("createOrModifyInvitationEvent : create new event {}", newEvent.getObmId());
					EventObmId id = calendarClient.createEvent(at, udr.getUser().getLoginAtDomain(), newEvent, isInternal);
					return calendarClient.getEventFromId(at, udr.getUser().getLoginAtDomain(), id);
				} catch (EventAlreadyExistException e) {
					throw new UnexpectedObmSyncServerException("it's not possible because getEventFromExtId == null");
				}
				
			} else {
			
				newEvent.setUid(previousEvent.getObmId());
				newEvent.setSequence(previousEvent.getSequence());
				if (!previousEvent.isInternalEvent()) {
					logger.info("createOrModifyInvitationEvent : update event {}", newEvent.getObmId());
					previousEvent = calendarClient.modifyEvent(at, udr.getUser().getLoginAtDomain(), newEvent, true, false);
				}
				return previousEvent;
			}	
			
		} catch (ServerFault fault) {
			throw new UnexpectedObmSyncServerException(fault);
		}		
	}

	private Event getEventFromExtId(UserDataRequest udr, EventExtId eventExtId, AccessToken at) 
			throws ServerFault, org.obm.sync.NotAllowedException {
		try {
			return calendarClient.getEventFromExtId(at, udr.getUser().getLoginAtDomain(), eventExtId);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}
	
	private String updateUserStatus(UserDataRequest udr, MSEvent msEvent, Event event, AttendeeStatus status, ICalendar calCli,
			AccessToken at) throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException, org.obm.sync.NotAllowedException {
		
		logger.info("update user status[ {} in calendar ]", status);
		Participation participationStatus = eventConverter.getParticipation(null, status);
		try {
			String calendar = udr.getUser().getLoginAtDomain();
			calCli.changeParticipationState(at, calendar, event.getExtId(), participationStatus, msEvent.getObmSequence(), true);
			Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), getDefaultCalendarName(udr));
			return getServerIdFor(collectionId, event.getObmId());
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
	}

	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds, SyncCollectionOptions syncCollectionOptions,
				ItemSyncState previousItemSyncState, SyncKey newSyncKey)
			throws DaoException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
	
		return fetch(udr, collectionId, fetchServerIds, syncCollectionOptions);
	}
	
	@Override
	public List<ItemChange> fetch(UserDataRequest udr, int collectionId, List<String> fetchServerIds, SyncCollectionOptions syncCollectionOptions)
			throws DaoException, UnexpectedObmSyncServerException, ConversionException, HierarchyChangedException {
	
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AccessToken token = login(udr);
		for (String serverId : fetchServerIds) {
			try {
				Event event = getEventFromServerId(token, udr.getUser().getLoginAtDomain(), serverId);
				if (event != null) {
					ItemChange ic = createItemChangeToAddFromEvent(udr, event, serverId);
					ret.add(ic);
				}
			} catch (org.obm.sync.NotAllowedException e) {
				throw new HierarchyChangedException(e);
			} catch (EventNotFoundException e) {
				logger.error("event from serverId {} not found.", serverId);
			} catch (ServerFault e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		logout(token);	
		return ret;
	}
	
	private Event getEventFromServerId(AccessToken token, String calendar, String serverId) throws ServerFault, EventNotFoundException, org.obm.sync.NotAllowedException {
		Integer itemId = mappingService.getItemIdFromServerId(serverId);
		if (itemId == null) {
			return null;
		}
		return calendarClient.getEventFromId(token, calendar, new EventObmId(itemId));
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

}
