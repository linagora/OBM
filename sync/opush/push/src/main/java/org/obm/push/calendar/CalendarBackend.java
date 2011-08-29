package org.obm.push.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ObmConfigurationService;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.FolderTypeNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.items.EventChanges;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CalendarBackend extends ObmSyncBackend {

	private final ImmutableMap<PIMDataType, ObmSyncCalendarConverter> converters;

	@Inject
	public CalendarBackend(	ObmConfigurationService configurationService, CollectionDao collectionDao)
			throws ConfigurationException {
		
		super(configurationService, collectionDao);
		converters = ImmutableMap.of(
				PIMDataType.CALENDAR, new EventConverter(),
				PIMDataType.TASKS, new TodoConverter());
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {

		if (!bs.checkHint("hint.multipleCalendars", false)) {
			return getDefaultCalendarItemChange(bs);
		} else {
			return getCalendarList(bs);
		}
	}

	private List<ItemChange> getCalendarList(BackendSession bs) throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {

		List<ItemChange> ret = new LinkedList<ItemChange>();
		AbstractEventSyncClient cc = getCalendarClient(bs, null);
		AccessToken token = login(cc, bs);
		try {
			CalendarInfo[] cals = cc.listCalendars(token);

			int idx = bs.getLoginAtDomain().indexOf("@");
			String domain = bs.getLoginAtDomain().substring(idx);

			for (CalendarInfo ci : cals) {
				ItemChange ic = new ItemChange();
				String col = "obm:\\\\" + bs.getLoginAtDomain()
						+ "\\calendar\\" + ci.getUid() + domain;
				Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
				ic.setServerId(collectionIdToString(collectionId));
				ic.setParentId("0");
				ic.setDisplayName(ci.getMail() + " calendar");
				if (bs.getLoginAtDomain().equalsIgnoreCase(ci.getMail())) {
					ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
				} else {
					ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
				}
				ret.add(ic);
			}
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			cc.logout(token);
		}
		return ret;
	}

	private List<ItemChange> getDefaultCalendarItemChange(BackendSession bs) throws DaoException {
		
		ItemChange ic = new ItemChange();
		String col = getDefaultCalendarName(bs);
		String serverId = "";
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
			serverId = collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = createCollectionMapping(bs.getDevice(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " calendar");
		ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		return ImmutableList.of(ic);
	}

	public List<ItemChange> getHierarchyTaskChanges(BackendSession bs) throws DaoException {
		List<ItemChange> ret = new ArrayList<ItemChange>(1);
		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\tasks\\"
				+ bs.getLoginAtDomain();
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getDevice(), col);
			serverId = collectionIdToString(collectionId);
		} catch (CollectionNotFoundException e) {
			serverId = createCollectionMapping(bs.getDevice(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " tasks");
		ic.setItemType(FolderType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filterType) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		
		AbstractEventSyncClient cc = getCalendarClient(bs, state.getDataType());
		AccessToken token = login(cc, bs);
		
		List<ItemChange> addUpd = new LinkedList<ItemChange>();
		List<ItemChange> deletions = new LinkedList<ItemChange>();
		Date syncDate = null;
		
		String collectionPath = getCollectionPathFor(collectionId);
		String calendar = parseCalendarName(collectionPath);

		state.updatingLastSync(filterType);
		try {
			
			EventChanges changes = null;
			if (state.isLastSyncFiltred()) {
				changes = cc.getSyncEventDate(token, calendar, state.getLastSync());
			} else {
				changes = cc.getSync(token, calendar, state.getLastSync());
			}
			
			logger.info("Event changes [ {} ]", changes.getUpdated().length);
			logger.info("Event changes LastSync [ {} ]", changes.getLastSync().toString());
			
			final String userEmail = cc.getUserEmail(token);
			addOrRemoveEventFilter(addUpd, deletions, changes, userEmail, collectionId, bs);
			syncDate = changes.getLastSync();
			
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} finally {
			cc.logout(token);
		}
		logger.info("getContentChanges( {}, {}, lastSync = {} ) => {} entries.",
				new Object[]{calendar, collectionPath, state.getLastSync(), addUpd.size()});
		return new DataDelta(addUpd, deletions, syncDate);
	}

	private void addOrUpdateEventFilter(List<ItemChange> addUpd, Event[] events, String userEmail, 
			Integer collectionId, BackendSession bs) {
		
		for (final Event event : events) {
			if (checkIfEventCanBeAdded(event, userEmail) && event.getRecurrenceId() == null) {
				ItemChange change = createItemChangeToAddFromEvent(bs, collectionId, event);
				addUpd.add(change);
			}			
		}
	}
	
	private void removeEventFilter(List<ItemChange> deletions, Event[] events, String[] eventsIdRemoved, 
			String userEmail, Integer collectionId) {
		
		for (final Event event : events) {
			if (!checkIfEventCanBeAdded(event, userEmail)) {
				deletions.add(getItemChange(collectionId, event.getUid()));
			}			
		}
		
		for (final String eventIdRemove : eventsIdRemoved) {
			deletions.add(getItemChange(collectionId, eventIdRemove));
		}
	}
	
	private void addOrRemoveEventFilter(List<ItemChange> addUpd, List<ItemChange> deletions, 
			EventChanges eventChanges, String userEmail, Integer collectionId, BackendSession bs) {
		
		addOrUpdateEventFilter(addUpd, eventChanges.getUpdated(), userEmail, collectionId, bs);
		removeEventFilter(deletions, eventChanges.getUpdated(), eventChanges.getRemoved(), userEmail, collectionId);
	}

	private boolean checkIfEventCanBeAdded(Event event, String userEmail) {
		for (final Attendee attendee : event.getAttendees()) {
			if (userEmail.equals(attendee.getEmail()) && 
					ParticipationState.DECLINED.equals(attendee.getState())) {
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

	private ItemChange createItemChangeToAddFromEvent(final BackendSession bs, final Integer collectionId, final Event event) {
		ItemChange ic = new ItemChange();
		ic.setServerId(getServerIdFor(collectionId, event.getUid()));
		IApplicationData ev = convertEvent(bs, event);
		ic.setData(ev);
		return ic;
	}

	public String createOrUpdate(BackendSession bs, Integer collectionId, String serverId, IApplicationData data) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {

		AbstractEventSyncClient cc = getCalendarClient(bs, data.getType());
		AccessToken token = login(cc, bs);
		
		final String collectionPath = getCollectionPathFor(collectionId);
		logger.info("createOrUpdate( collectionPath = {}, serverId = {} )", new Object[]{collectionPath, serverId});
		
		String eventId = null;
		Event event = null;
		try {
		
			
			Event oldEvent = null;
			if (serverId != null) {
				int idx = serverId.lastIndexOf(":");
				eventId = serverId.substring(idx + 1);
				oldEvent = cc.getEventFromId(token, bs.getLoginAtDomain(), eventId);	
			}

			Boolean isInternal = EventConverter.isInternalEvent(oldEvent, true);
			
			if(isInternal){
				event = converters.get(data.getType()).convertAsInternal(bs, oldEvent, data);
			} else {
				event = converters.get(data.getType()).convertAsExternal(bs, oldEvent, data);
			}

			Attendee att = new Attendee();
			att.setEmail( cc.getUserEmail(token) );
			
			if (eventId != null) {
				event.setUid(eventId);
				cc.modifyEvent(token, parseCalendarName(collectionPath), event, true, true);
			} else {
				eventId = cc.createEvent(token, parseCalendarName(collectionPath), event, true);
			}
				
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} catch (EventAlreadyExistException e) {
			eventId = getEventIdFromExtId(token, collectionPath, cc, event);
		} finally {
			cc.logout(token);
		}
		
		return getServerIdFor(collectionId, eventId);
	}

	private String getEventIdFromExtId(AccessToken token, String collectionPath, AbstractEventSyncClient cc, Event event) 
			throws UnknownObmSyncServerException {
		
		try {
			Integer obmid = cc.getEventObmIdFromExtId(token, parseCalendarName(collectionPath), event.getExtId());
			if (obmid != null) {
				return obmid.toString();
			}
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return null;
	}

	public void delete(BackendSession bs, Integer collectionId, String serverId) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		
		logger.info("delete serverId {}", serverId);
		String collectionPath = getCollectionPathFor(collectionId);
		if (serverId != null) {
			String id = getEventUidFromServerId(serverId);
			if (id != null) {
				AbstractEventSyncClient bc = getCalendarClient(bs,
						PIMDataType.CALENDAR);
				AccessToken token = login(bc, bs);
				try {
					Event evr = bc.getEventFromId(token, bs.getLoginAtDomain(),
							id);
					if (evr != null) {
						if (bs.getLoginAtDomain().equals(evr.getOwnerEmail())) {
							bc.removeEvent(token,
									parseCalendarName(collectionPath), id, evr.getSequence(), true);
						} else {
							//using calendar backend, we retrieve MSEvent
							MSEvent mser = (MSEvent) convertEvent(bs, evr);
							updateUserStatus(bs, mser, AttendeeStatus.DECLINE, bc, token);
						}
					}
				} catch (ServerFault e) {
					throw new UnknownObmSyncServerException(e);
				} finally {
					bc.logout(token);
				}
			}
		}
	}

	public String handleMeetingResponse(BackendSession bs, MSEmail invitation, AttendeeStatus status) 
			throws UnknownObmSyncServerException, CollectionNotFoundException, DaoException {
		
		MSEvent event = invitation.getInvitation();
		AbstractEventSyncClient calCli = getCalendarClient(bs, event.getType());
		AccessToken at = calCli.login(bs.getLoginAtDomain(), bs.getPassword(), "o-push");
		try {
			logger.info("handleMeetingResponse = {}", event.getUID());
			Event obmEvent = createOrModifyInvitationEvent(bs, event, calCli, at);
			event.setObmUID(obmEvent.getUid());
			event.setObmSequence(obmEvent.getSequence());
			return updateUserStatus(bs, event, status, calCli, at);
		} catch (UnknownObmSyncServerException e) {
			throw e;
		} finally {
			calCli.logout(at);
		}
	}

	private Event createOrModifyInvitationEvent(BackendSession bs, MSEvent event, AbstractEventSyncClient calCli, AccessToken at) 
			throws UnknownObmSyncServerException {
		
		try {
			Event obmEvent = getEventFromExtId(bs, event, calCli, at);
			
			boolean isInternal = EventConverter.isInternalEvent(obmEvent, false);
			Event newEvent = null;
			if (isInternal) {
				newEvent = new EventConverter().convertAsInternal(bs, event);
			} else {
				newEvent = new EventConverter().convertAsExternal(bs, event);
			}
			
			if (obmEvent == null) {
				
				String id = null;
				try {
					id = calCli.createEvent(at, bs.getLoginAtDomain(), newEvent, isInternal);
				} catch (EventAlreadyExistException e) {
					throw new UnknownObmSyncServerException("it's not possible because getEventFromExtId == null");
				}
				logger.info("createOrModifyInvitationEvent : create new event {}", event.getUID());
				return calCli.getEventFromId(at, bs.getLoginAtDomain(), id);
				
			} else {
			
				newEvent.setUid(obmEvent.getUid());
				newEvent.setSequence(obmEvent.getSequence());
				if(!obmEvent.isInternalEvent()){
					logger.info("createOrModifyInvitationEvent : update event {}", event.getUID());
					obmEvent = calCli.modifyEvent(at, bs.getLoginAtDomain(), newEvent, true, false);
				}
				return obmEvent;
			}	
			
		} catch (ServerFault fault) {
			throw new UnknownObmSyncServerException(fault);
		}		
	}

	private Event getEventFromExtId(BackendSession bs, MSEvent event, AbstractEventSyncClient calCli, AccessToken at) 
			throws ServerFault {
		Event obmEvent = null;
		try {
			obmEvent = calCli.getEventFromExtId(at, bs.getLoginAtDomain(), event.getUID());
		} catch (EventNotFoundException e) {
			logger.info(e.getMessage());
		}
		return obmEvent;
	}

	private String updateUserStatus(BackendSession bs, MSEvent msEvent, AttendeeStatus status, AbstractEventSyncClient calCli,
			AccessToken at) throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		
		logger.info("update user status[ {} in calendar ]", status.toString());
		ParticipationState participationStatus = EventConverter.status(null, status);
		try {
			calCli.changeParticipationState(at, bs.getLoginAtDomain(), msEvent.getUID(), participationStatus, msEvent.getObmSequence(), true);
			Integer collectionId = getCollectionIdFor(bs.getDevice(), getDefaultCalendarName(bs));
			return getServerIdFor(collectionId, msEvent.getObmUID());
		} catch (ServerFault e) {
			throw new UnknownObmSyncServerException(e);
		}
	}

	public List<ItemChange> fetchItems(BackendSession bs, List<String> fetchServerIds) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AbstractEventSyncClient calCli = getCalendarClient(bs, PIMDataType.CALENDAR);
		AccessToken token = login(calCli, bs);
		for (String serverId : fetchServerIds) {
			String id = getEventUidFromServerId(serverId);
			if (id != null) {
				Event e;
				try {
					e = calCli.getEventFromId(token, bs.getLoginAtDomain(), id);
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					IApplicationData ev = convertEvent(bs, e);
					ic.setData(ev);
					ret.add(ic);
				} catch (ServerFault e1) {
					logger.error("event from id {} not found !", id);
				}
			}
		}
		calCli.logout(token);	
		return ret;
	}
	
	public List<ItemChange> fetchItems(BackendSession bs, Integer collectionId, Collection<String> uids) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AbstractEventSyncClient calCli = getCalendarClient(bs, PIMDataType.CALENDAR);
		AccessToken token = login(calCli, bs);
		for (String eventUid : uids) {
			try {
				Event e = calCli.getEventFromExtId(token, bs.getLoginAtDomain(), eventUid);
				if (e != null) {
					ret.add( createItemChangeToAddFromEvent(bs, collectionId, e) );
				}
			} catch (ServerFault e) {
				logger.error(e.getMessage(), e);
			} catch (EventNotFoundException e) {
				logger.error("fetchItems : event from extId {} not found", eventUid);
			}
		}
		calCli.logout(token);
		return ret;
	}

	private IApplicationData convertEvent(BackendSession bs, Event e) {
		if (EventType.VTODO.equals(e.getType())) {
			return converters.get(PIMDataType.TASKS).convert(bs, e);
		} else {
			return converters.get(PIMDataType.CALENDAR).convert(bs, e);
		}
	}

	public List<ItemChange> fetchDeletedItems(BackendSession bs, Integer collectionId, Collection<String> uids) {
		final List<ItemChange> ret = Lists.newArrayListWithCapacity(uids.size());
		final AbstractEventSyncClient calCli = getCalendarClient(bs, PIMDataType.CALENDAR);
		final AccessToken token = login(calCli, bs);
		for (final String eventUid : uids) {
			Integer id;
			try {
				id = calCli.getEventObmIdFromExtId(token, bs.getLoginAtDomain(), eventUid);
				if (id != null) {
					ret.add(getItemChange(collectionId, String.valueOf(id)));
				}
			} catch (ServerFault e) {
				logger.error(e.getMessage(), e);
			} catch (EventNotFoundException e) {
				logger.error("fetchDeletedItems : event from extId {} not found", eventUid);
			}
		}
		calCli.logout(token);
		return ret;
	}
	
	public Integer getCollectionId (BackendSession bs) throws CollectionNotFoundException, DaoException {
		String calPath = getDefaultCalendarName(bs);
		return getCollectionIdFor(bs.getDevice(), calPath);
	}

	public String getEventUidFromServerId(String serverId) {
		Integer uid = getItemIdFor(serverId);
		if(uid == null){
			return null;
		}
		return uid.toString();
	}
	
	/**
	 *  obm:\\adrien@test.tlse.lng\calendar\adrien@test.tlse.lng
	 *  obm:\\adrien@test.tlse.lng\task\adrien@test.tlse.lng
	 */
	public FolderType getFolderType(String collectionPath) throws FolderTypeNotFoundException {
		if (collectionPath != null) {
			if(collectionPath.contains("calendar")){
				return FolderType.DEFAULT_CALENDAR_FOLDER;
			} 
			if(collectionPath.contains("task")){
				return FolderType.DEFAULT_TASKS_FOLDER;
			}
		}
		throw new FolderTypeNotFoundException("The collection's path[ " + collectionPath + "] is invalid");
	}

}
