package org.obm.push.calendar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.dbcp.IDBCP;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEvent;
import org.obm.push.data.calendarenum.AttendeeStatus;
import org.obm.push.exception.FolderTypeNotFoundException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.FolderType;
import org.obm.push.store.IApplicationData;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncState;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.EventAlreadyExistException;
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
	public CalendarBackend(ISyncStorage storage, DeviceDao deviceDao,
			ConfigurationService configurationService, IDBCP dbcp)
			throws ConfigurationException {
		
		super(storage, deviceDao, configurationService, dbcp);
		converters = ImmutableMap.of(
				PIMDataType.CALENDAR, new EventConverter(),
				PIMDataType.TASKS, new TodoConverter());
	}

	public List<ItemChange> getHierarchyChanges(BackendSession bs) throws SQLException {

		if (!bs.checkHint("hint.multipleCalendars", false)) {
			return getDefaultCalendarItemChange(bs);
		} else {
			return getCalendarList(bs);
		}
	}

	private List<ItemChange> getCalendarList(BackendSession bs) {

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
				Integer collectionId = getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), col);
				ic.setServerId(getServerIdFor(collectionId));
				ic.setParentId("0");
				ic.setDisplayName(ci.getMail() + " calendar");
				if (bs.getLoginAtDomain().equalsIgnoreCase(ci.getMail())) {
					ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
				} else {
					ic.setItemType(FolderType.USER_CREATED_CALENDAR_FOLDER);
				}
				ret.add(ic);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			cc.logout(token);
		}
		return ret;
	}

	private List<ItemChange> getDefaultCalendarItemChange(BackendSession bs) throws SQLException {
		
		ItemChange ic = new ItemChange();
		String col = getDefaultCalendarName(bs);
		String serverId = "";
		try {
			Integer collectionId = getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), col);
			serverId = getServerIdFor(collectionId);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getLoginAtDomain(), bs.getDevId(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " calendar");
		ic.setItemType(FolderType.DEFAULT_CALENDAR_FOLDER);
		return ImmutableList.of(ic);
	}

	public List<ItemChange> getHierarchyTaskChanges(BackendSession bs) throws SQLException {
		List<ItemChange> ret = new ArrayList<ItemChange>(1);
		ItemChange ic = new ItemChange();
		String col = "obm:\\\\" + bs.getLoginAtDomain() + "\\tasks\\"
				+ bs.getLoginAtDomain();
		String serverId;
		try {
			Integer collectionId = getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), col);
			serverId = getServerIdFor(collectionId);
		} catch (ActiveSyncException e) {
			serverId = createCollectionMapping(bs.getLoginAtDomain(), bs.getDevId(), col);
			ic.setIsNew(true);
		}
		ic.setServerId(serverId);
		ic.setParentId("0");
		ic.setDisplayName(bs.getLoginAtDomain() + " tasks");
		ic.setItemType(FolderType.DEFAULT_TASKS_FOLDER);
		ret.add(ic);
		return ret;
	}

	public DataDelta getContentChanges(BackendSession bs, SyncState state, Integer collectionId) throws ActiveSyncException {
		
		final List<ItemChange> addUpd = new LinkedList<ItemChange>();
		final List<ItemChange> deletions = new LinkedList<ItemChange>();
		Date syncDate = null;
		
		final Date lastSyncDate = state.getLastSync();
	
		final AbstractEventSyncClient cc = getCalendarClient(bs, state.getDataType());
		final AccessToken token = login(cc, bs);
		
		final String collectionPath = getCollectionPathFor(collectionId);
		
		logger.info("Collection [ " + collectionPath + " ]");
		logger.info("LastSync [ " + lastSyncDate.toString() + ", " + lastSyncDate.getTime() +  " ]");
		
		final String calendar = parseCalendarName(collectionPath);
		try {
			
			EventChanges changes = null;
			if (state.isLastSyncFiltred()) {
				changes = cc.getSyncEventDate(token, calendar, lastSyncDate);
			} else {
				changes = cc.getSync(token, calendar, lastSyncDate);
			}
			
			logger.info("Event changes [ " + changes.getUpdated().length + " ]");
			logger.info("Event changes LastSync [ " + changes.getLastSync().toString() + " ]");
			
			final String userEmail = cc.getUserEmail(token);
			addOrRemoveEventFilter(addUpd, deletions, changes, userEmail, collectionId, bs);
			syncDate = changes.getLastSync();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			cc.logout(token);
		}
		
		logger.info("getContentChanges(" + calendar + ", " + collectionPath + ", lastSync: " + lastSyncDate + ") => " + addUpd.size() + " entries.");
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
				deletions.add(createItemChangeToRemove(collectionId, event.getUid()));
			}			
		}
		
		for (final String eventIdRemove : eventsIdRemoved) {
			deletions.add(createItemChangeToRemove(collectionId, eventIdRemove));
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

	public String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, IApplicationData data) throws ActiveSyncException {
		String collectionPath = getCollectionPathFor(collectionId);
		logger.info("createOrUpdate(" + bs.getLoginAtDomain() + ", "
				+ collectionPath + ", " + serverId + ")");
		AbstractEventSyncClient cc = getCalendarClient(bs, data.getType());
		AccessToken token = login(cc, bs);
		try {
			String id = null;
			Event oldEvent = null;
			if (serverId != null) {
				int idx = serverId.lastIndexOf(":");
				id = serverId.substring(idx + 1);
				try {
					oldEvent = cc.getEventFromId(token, bs.getLoginAtDomain(), id);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

			String email = bs.getLoginAtDomain();
			try {
				email = cc.getUserEmail(token);
			} catch (Exception e) {
				logger.error("Error finding email: " + e.getMessage(), e);
			}
			Boolean isInternal = EventConverter.isInternalEvent(oldEvent, true);
			Event event = null;
			if(isInternal){
				event = converters.get(data.getType()).convertAsInternal(bs, oldEvent, data);
			} else {
				event = converters.get(data.getType()).convertAsExternal(bs, oldEvent, data);
			}

			Attendee att = new Attendee();
			att.setEmail(email);
			if (id != null) {
				try {
					event.setUid(id);
					cc.modifyEvent(token, parseCalendarName(collectionPath), event,
							true, true);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				try {
					id = cc.createEvent(token, parseCalendarName(collectionPath),
							event, true);
				} catch (EventAlreadyExistException e) {
					id = getEventIdFromExtId(token, collectionPath, cc, event);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			return getServerIdFor(collectionId, id);
		} finally {
			cc.logout(token);
		}
	}

	private String getEventIdFromExtId(AccessToken token, String collectionPath, AbstractEventSyncClient cc, Event event) {
		try {
			Integer obmid = cc.getEventObmIdFromExtId(token, parseCalendarName(collectionPath), event.getExtId());
			if(obmid != null){
				return obmid.toString();
			}
		} catch (ServerFault e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public void delete(BackendSession bs, Integer collectionId, String serverId) throws ActiveSyncException {
		logger.info("delete serverId " + serverId);
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
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					bc.logout(token);
				}
			}
		}
	}

	public String handleMeetingResponse(BackendSession bs, MSEmail invitation, AttendeeStatus status) {
		MSEvent event = invitation.getInvitation();
		AbstractEventSyncClient calCli = getCalendarClient(bs, event.getType());
		AccessToken at = calCli.login(bs.getLoginAtDomain(), bs.getPassword(), "o-push");
		try {
			logger.info("handleMeetingResponse : " + event.getUID());
			Event obmEvent = createOrModifyInvitationEvent(bs, event, calCli, at);
			event.setObmUID(obmEvent.getUid());
			String serverId = updateUserStatus(bs, event, status, calCli, at);
			
			return serverId;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			calCli.logout(at);
		}
		return null;
	}

	private Event createOrModifyInvitationEvent(BackendSession bs, MSEvent event,
			AbstractEventSyncClient calCli, AccessToken at) throws AuthFault,
			ServerFault {
		Event obmEvent = calCli.getEventFromExtId(at, bs.getLoginAtDomain(), event.getUID());
		Boolean isInternal = EventConverter.isInternalEvent(obmEvent, false);
		Event newEvent = null;
		if(isInternal){
			newEvent = new EventConverter().convertAsInternal(bs, event);
		} else {
			newEvent = new EventConverter().convertAsExternal(bs, event);
		}
		if (obmEvent == null) {
			logger.info("createOrModifyInvitationEvent : create new event " + event.getUID());
			String id = calCli.createEvent(at, bs.getLoginAtDomain(), newEvent, isInternal);
			return calCli.getEventFromId(at, bs.getLoginAtDomain(), id);
		} else {
			newEvent.setUid(obmEvent.getUid());
			if(!obmEvent.isInternalEvent()){
				logger.info("createOrModifyInvitationEvent : update event " + event.getUID());
				obmEvent = calCli.modifyEvent(at, bs.getLoginAtDomain(), newEvent, true, false);
			}
			return obmEvent;
		}
	}

	private String updateUserStatus(BackendSession bs, MSEvent msEvent,
			AttendeeStatus status, AbstractEventSyncClient calCli,
			AccessToken at) throws ServerFault,
			CollectionNotFoundException, ActiveSyncException, SQLException {
		logger.info("update user status[" + status.toString()
					+ "] in calendar " + bs.getLoginAtDomain());
		ParticipationState participationStatus = EventConverter.status(null, status);
		calCli.changeParticipationState(at, bs.getLoginAtDomain(), msEvent.getUID(), participationStatus, msEvent.getObmSequence(), true);
		
		Integer collectionId = getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), getDefaultCalendarName(bs));
		return getServerIdFor(collectionId, msEvent.getObmUID());
	}

	public List<ItemChange> fetchItems(BackendSession bs,
			List<String> fetchServerIds) throws ObjectNotFoundException {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AbstractEventSyncClient calCli = getCalendarClient(bs,
				PIMDataType.CALENDAR);
		AccessToken token = login(calCli, bs);
		try {
			for (String serverId : fetchServerIds) {
				String id = getEventUidFromServerId(serverId);
				if (id != null) {
					Event e = calCli.getEventFromId(token,
							bs.getLoginAtDomain(), id);
					ItemChange ic = new ItemChange();
					ic.setServerId(serverId);
					IApplicationData ev = convertEvent(bs, e);
					ic.setData(ev);
					ret.add(ic);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ObjectNotFoundException();
		} finally {
			calCli.logout(token);
		}
		return ret;
	}
	
	public List<ItemChange> fetchItems(BackendSession bs, Integer collectionId, Collection<String> uids) {
		List<ItemChange> ret = new LinkedList<ItemChange>();
		AbstractEventSyncClient calCli = getCalendarClient(bs, PIMDataType.CALENDAR);
		AccessToken token = login(calCli, bs);
		try {
			for (String eventUid : uids) {
				Event e = calCli.getEventFromExtId(token, bs.getLoginAtDomain(), eventUid);
				if (e != null) {
					ItemChange ic = createItemChangeToAddFromEvent(bs, collectionId, e);
					ret.add(ic);
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			calCli.logout(token);
		}
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
		try {
			for (final String eventUid: uids) {
				Integer id = calCli.getEventObmIdFromExtId(token, bs.getLoginAtDomain(), eventUid);
				if (id != null) {
					ret.add( createItemChangeToRemove(collectionId, String.valueOf(id)) );
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			calCli.logout(token);
		}
		return ret;
	}
	
	public Integer getCollectionId (BackendSession bs) throws CollectionNotFoundException, SQLException{
		String calPath = getDefaultCalendarName(bs);
		Integer eventCollectionId = getCollectionIdFor(bs.getLoginAtDomain(), 
				bs.getDevId(), calPath);
		return eventCollectionId;
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
