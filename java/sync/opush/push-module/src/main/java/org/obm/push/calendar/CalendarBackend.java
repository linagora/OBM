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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangeBuilder;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.HierarchyChangesException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
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
import org.obm.sync.client.login.LoginService;
import org.obm.sync.items.EventChanges;
import org.obm.sync.services.ICalendar;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

	@Inject
	@VisibleForTesting CalendarBackend(MappingService mappingService, 
			@Named(CalendarType.CALENDAR) ICalendar calendarClient, 
			EventConverter eventConverter, 
			EventService eventService,
			LoginService login,
			Provider<CollectionPath.Builder> collectionPathBuilderProvider) {
		super(mappingService, login, collectionPathBuilderProvider);
		this.calendarClient = calendarClient;
		this.eventConverter = eventConverter;
		this.eventService = eventService;
	}
	
	@Override
	public PIMDataType getPIMDataType() {
		return PIMDataType.CALENDAR;
	}
	
	@Override
	public HierarchyItemsChanges getHierarchyChanges(UserDataRequest udr, 
			FolderSyncState lastKnownState, FolderSyncState outgoingSyncState)
			throws DaoException {

		try {
			List<CollectionPath> contactsCollections = null;
			if (!udr.checkHint("hint.multipleCalendars", false)) {
				contactsCollections = getDefaultCalendarCollectionPaths(udr);
			} else {
				contactsCollections = getCalendarCollectionPaths(udr);
			}
			snapshotHierarchy(udr, contactsCollections, outgoingSyncState);
			return computeChanges(udr, lastKnownState, contactsCollections);
		} catch (CollectionNotFoundException e) {
			throw new HierarchyChangesException(e);
		}
	}

	private HierarchyItemsChanges computeChanges(UserDataRequest udr, FolderSyncState lastKnownState,
			Iterable<CollectionPath> contactsCollections) throws DaoException, CollectionNotFoundException {

		ImmutableSet<CollectionPath> actualContactsCollections = ImmutableSet.copyOf(contactsCollections);
		Set<CollectionPath> lastKnownCollections = lastKnownCollectionPath(udr, lastKnownState, getPIMDataType());
		
		Set<CollectionPath> newContactCollections = Sets.difference(actualContactsCollections, lastKnownCollections);
		Set<CollectionPath> deletedContactCollections = Sets.difference(lastKnownCollections, actualContactsCollections);

		return buildHierarchyItemsChanges(udr, newContactCollections, deletedContactCollections);
	}

	private List<CollectionPath> getCalendarCollectionPaths(UserDataRequest udr) {
		
		List<CollectionPath> ret = Lists.newLinkedList();
		AccessToken token = login(udr);
		try {
			CalendarInfo[] cals = calendarClient.listCalendars(token);
			for (CalendarInfo ci : cals) {
				ret.add(collectionPathOfCalendar(udr, ci.getMail()));
			}
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} finally {
			logout(token);
		}
		return ret;
	}

	private List<CollectionPath> getDefaultCalendarCollectionPaths(UserDataRequest udr) {
		
		String displayName = udr.getUser().getLoginAtDomain();
		return ImmutableList.of(collectionPathOfCalendar(udr, displayName));
	}

	@Override
	protected ItemChange createItemChange(UserDataRequest udr, CollectionPath collectionPath)
			throws CollectionNotFoundException, DaoException {
		
		Integer collectionId = mappingService.getCollectionIdFor(udr.getDevice(), collectionPath.collectionPath());
		
		ItemChange ic = new ItemChange();
		ic.setServerId(mappingService.collectionIdToString(collectionId));
		ic.setParentId(DEFAULT_CALENDAR_PARENT_ID);
		ic.setDisplayName(collectionPath.displayName() + DEFAULT_CALENDAR_DISPLAYNAME_SUFFIX);
		if (isDefaultCalendarCollectionPath(udr, collectionPath)) {
			ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		} else {
			ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
		}
		return ic;
	}

	private boolean isDefaultCalendarCollectionPath(UserDataRequest udr, CollectionPath collectionPath) {
		return udr.getUser().getLoginAtDomain().equalsIgnoreCase(collectionPath.displayName());
	}

	private String getDefaultCalendarName(UserDataRequest udr) {
		return collectionPathOfCalendar(udr, udr.getUser().getLoginAtDomain()).collectionPath();
	}

	private CollectionPath collectionPathOfCalendar(UserDataRequest udr, String calendar) {
		return collectionPathBuilderProvider.get()
			.userDataRequest(udr)
			.pimType(PIMDataType.CALENDAR)
			.displayName(calendar)
			.build();
	}

	@Override
	public int getItemEstimateSize(UserDataRequest udr, Integer collectionId, SyncState state, 
			SyncCollectionOptions syncCollectionOptions) throws CollectionNotFoundException, 
			DaoException, UnexpectedObmSyncServerException, ConversionException {
		
		DataDelta dataDelta = getChanged(udr, state, collectionId, syncCollectionOptions);
		return dataDelta.getItemEstimateSize();
	}
	
	@Override
	public DataDelta getChanged(UserDataRequest udr, SyncState state, Integer collectionId, 
			SyncCollectionOptions syncCollectionOptions) throws DaoException,
			CollectionNotFoundException, UnexpectedObmSyncServerException, ConversionException {
		
		AccessToken token = login(udr);
		
		String collectionPath = mappingService.getCollectionPathFor(collectionId);
		String calendar = parseCalendarName(collectionPath);

		state.updatingLastSync(syncCollectionOptions.getFilterType());
		try {
			
			EventChanges changes = null;
			if (state.isLastSyncFiltred()) {
				changes = calendarClient.getSyncEventDate(token, calendar, state.getLastSync());
			} else {
				changes = calendarClient.getSync(token, calendar, state.getLastSync());
			}
			
			logger.info("Event changes [ {} ]", changes.getUpdated().size());
			logger.info("Event changes LastSync [ {} ]", changes.getLastSync().toString());
		
			DataDelta delta = 
					buildDataDelta(udr, collectionId, token, changes);
			
			logger.info("getContentChanges( {}, {}, lastSync = {} ) => {}",
				new Object[]{calendar, collectionPath, state.getLastSync(), delta.statistics()});
			
			return delta;
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
		
		List<ItemChange> additions = addOrUpdateEventFilter(changes.getUpdated(), userEmail, collectionId, udr);
		List<ItemChange> deletions = removeEventFilter(changes.getDeletedEvents(), collectionId);
		Date syncDate = changes.getLastSync();
		
		return new DataDelta(additions, deletions, syncDate);
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
	
	private List<ItemChange> removeEventFilter(List<DeletedEvent> eventsRemoved, Integer collectionId) {
		
		List<ItemChange> deletions = Lists.newArrayList();
		for (final DeletedEvent eventRemove : eventsRemoved) {
			deletions.add(getItemChange(collectionId, eventRemove.getId()));
		}
		return deletions;
	}
	
	private ItemChange getItemChange(Integer collectionId,	EventObmId eventIdRemove) {
		return mappingService.getItemChange(collectionId, eventIdRemove.serializeToString());
	}

	private boolean checkIfEventCanBeAdded(Event event, String userEmail) {
		for (final Attendee attendee : event.getAttendees()) {
			if (userEmail.equals(attendee.getEmail()) && 
					Participation.DECLINED.equals(attendee.getParticipation())) {
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
			DaoException, UnexpectedObmSyncServerException, ItemNotFoundException, ConversionException {

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
				
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		} catch (EventAlreadyExistException e) {
			eventId = getEventIdFromExtId(token, collectionPath, calendarClient, event);
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(token);
		}
		
		return getServerIdFor(collectionId, eventId);
	}

	private void updateCalendarEntity(ICalendar cc, AccessToken token, String collectionPath, Event old, Event event) throws ServerFault {
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
			throws ServerFault, EventAlreadyExistException, DaoException {
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
			throws ServerFault, EventAlreadyExistException {
		return cc.createEvent(token, parseCalendarName(collectionPath), event, true);
	}

	private EventObmId createEvent(UserDataRequest udr, ICalendar cc,
			AccessToken token, String collectionPath, Event event, MSEvent msEvent)
			throws ServerFault, EventAlreadyExistException, DaoException {
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
			throws UnexpectedObmSyncServerException {
		
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
			ItemNotFoundException, ConversionException {
		
		MSEvent event = invitation.getInvitation();
		AccessToken at = login(udr);
		try {
			logger.info("handleMeetingResponse = {}", event.getUid());
			Event obmEvent = createOrModifyInvitationEvent(udr, event, at);
			event.setObmSequence(obmEvent.getSequence());
			return updateUserStatus(udr, event, obmEvent, status, calendarClient, at);
		} catch (UnexpectedObmSyncServerException e) {
			throw e;
		} catch (EventNotFoundException e) {
			throw new ItemNotFoundException(e);
		} finally {
			logout(at);
		}
	}

	private Event createOrModifyInvitationEvent(UserDataRequest udr, MSEvent event, AccessToken at) 
			throws UnexpectedObmSyncServerException, EventNotFoundException, ConversionException, DaoException {
		
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
			throws ServerFault {
		try {
			return calendarClient.getEventFromExtId(at, udr.getUser().getLoginAtDomain(), eventExtId);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}
	
	private String updateUserStatus(UserDataRequest udr, MSEvent msEvent, Event event, AttendeeStatus status, ICalendar calCli,
			AccessToken at) throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException {
		
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
	public List<ItemChange> fetch(UserDataRequest udr, List<String> itemIds, SyncCollectionOptions collectionOptions)
			throws DaoException, UnexpectedObmSyncServerException, ConversionException {
	
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AccessToken token = login(udr);
		for (String serverId : itemIds) {
			try {
				Event event = getEventFromServerId(token, udr.getUser().getLoginAtDomain(), serverId);
				if (event != null) {
					ItemChange ic = createItemChangeToAddFromEvent(udr, event, serverId);
					ret.add(ic);
				}
			} catch (EventNotFoundException e) {
				logger.error("event from serverId {} not found.", serverId);
			} catch (ServerFault e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
		logout(token);	
		return ret;
	}
	
	private Event getEventFromServerId(AccessToken token, String calendar, String serverId) throws ServerFault, EventNotFoundException {
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
