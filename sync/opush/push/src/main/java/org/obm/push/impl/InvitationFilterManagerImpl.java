package org.obm.push.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.obm.push.IInvitationFilterManager;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.MSEmail;
import org.obm.push.backend.MSEvent;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.mail.MailBackend;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.InvitationStatus;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InvitationFilterManagerImpl implements IInvitationFilterManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ISyncStorage storage;
	private CalendarBackend calendarBackend;
	private MailBackend mailBackend;

	@Inject
	private InvitationFilterManagerImpl(ISyncStorage storage,
			CalendarBackend calendarBackend, MailBackend mailMabBackend) {
		
		this.storage = storage;
		this.calendarBackend = calendarBackend;
		this.mailBackend = mailMabBackend;
	}

	@Override
	public void handleMeetingResponse(BackendSession bs, Integer emailCollectionId, MSEmail invitation) {
		try {
			Integer eventCollectionId = calendarBackend.getCollectionId(bs);
			storage.updateInvitationStatus(InvitationStatus.EMAIL_TO_DELETED, emailCollectionId, ImmutableList.of(invitation.getUid()));
			storage.createOrUpdateInvitationEventAsMustSynced(eventCollectionId, invitation.getInvitation().getUID(), 
					invitation.getInvitation().getDtStamp());
		} catch (CollectionNotFoundException e) {
			logger.info(e.getMessage(), e);
		}
	}

	@Override
	public DataDelta filterEvent(final BackendSession bs, final SyncState state, final Integer eventCollectionId, final DataDelta delta) {
		final String syncKey = state.getKey();
		
		final List<ItemChange> toSynced = mergeChangesAndToSyncedEvent(bs, eventCollectionId, syncKey, delta.getChanges());
		processFilterEvent(eventCollectionId, syncKey, toSynced);

		final List<ItemChange> updated = proccessEventMustSynced(bs, eventCollectionId, syncKey);
		final List<ItemChange> removed = getEventToDeleted(bs, eventCollectionId, syncKey);

		delta.getChanges().clear();
		delta.getChanges().addAll(toSynced);
		delta.getChanges().addAll(updated);

		delta.getDeletions().addAll(removed);
		return delta;
	}

	private List<ItemChange> mergeChangesAndToSyncedEvent(BackendSession bs, Integer eventCollectionId, String syncKey, List<ItemChange> changes) {
		List<ItemChange> its = Lists.newArrayList(changes.iterator());
		try {
			List<String> eventToSynced = storage.getEventToSynced(eventCollectionId, syncKey);
			for (final ItemChange ic : changes) {
				if (ic.getData() instanceof MSEvent) {
					MSEvent event = (MSEvent) ic.getData();
					eventToSynced.remove(event.getUID());
				}
			}
			its.addAll(calendarBackend.fetchItems(bs, eventCollectionId, eventToSynced));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return its;
	}

	private List<ItemChange> getEventToDeleted(BackendSession bs, Integer eventCollectionId, String syncKey) {
		final List<String> eventUIDToDeleted = storage.getEventToDeleted(eventCollectionId, syncKey);
		if (eventUIDToDeleted.isEmpty()) {
			return ImmutableList.of();
		}

		final List<ItemChange> eventToDeleted = calendarBackend.fetchDeletedItems(bs, eventCollectionId, eventUIDToDeleted);
		logger.info(eventToDeleted.size()+  " event(s) will be deleted on the PDA");
		storage.updateInvitationEventStatus(InvitationStatus.DELETED, syncKey, eventCollectionId, eventUIDToDeleted);

		return eventToDeleted;
	}

	private List<ItemChange> proccessEventMustSynced(BackendSession bs, Integer eventCollectionId, String syncKey) {
		List<String> eventsToSync = storage.getInvitationEventMustSynced(eventCollectionId);
		List<ItemChange> itemsChange = calendarBackend.fetchItems(bs, eventCollectionId, eventsToSync);
		storage.updateInvitationEventStatus(InvitationStatus.EVENT_SYNCED, syncKey, eventCollectionId, eventsToSync);
		return itemsChange;
	}

	private void processFilterEvent(Integer eventCollectionId, String syncKey, List<ItemChange> toSynced) {
		for (final Iterator<ItemChange> it = toSynced.iterator(); it.hasNext();) {
			final ItemChange ic = it.next();
			if (ic.getData() instanceof MSEvent) {
				final MSEvent event = (MSEvent) ic.getData();
				setEventStatus(eventCollectionId, syncKey, event);
			}
		}
	}

	private void setEventStatus(final Integer eventCollectionId, final String syncKey, final MSEvent event) {
		if (!storage.isMostRecentInvitation(eventCollectionId, event.getUID(), event.getDtStamp())) {
			logger.info("A more recent event or email is synchronized on phone. The event[ " + event.getUID() + ", " + event.getSubject()
					+ "] will not synced");
		} else {
			boolean isInvitationAlreadySynced = storage.invitationIsAlreadySynced(eventCollectionId, event.getUID());
			if (isInvitationAlreadySynced) {
				storage.createOrUpdateInvitationEvent(eventCollectionId, event.getUID(), event.getDtStamp(), InvitationStatus.EVENT_TO_SYNCED);
			} else {
				storage.createOrUpdateInvitationEvent(eventCollectionId, event.getUID(), event.getDtStamp(), InvitationStatus.EVENT_SYNCED, syncKey);
				storage.setInvitationStatusAtToDelete(eventCollectionId, event.getUID());
			}
		}
	}

	@Override
	public void filterInvitation(BackendSession bs, SyncState state, Integer emailCollectionId, DataDelta delta) {
		try {
			final Map<String, ItemChange> syncedItem = new HashMap<String, ItemChange>();
			final List<ItemChange> itemToSync = mergeChangesAndToSyncedEmail(bs, state, emailCollectionId,  delta.getChanges());
			
			for (final Iterator<ItemChange> it = itemToSync.iterator(); it.hasNext();) {
				final ItemChange ic = it.next();
				if (ic.getData() instanceof MSEmail) {
					final MSEmail mail = (MSEmail) ic.getData();
					if (mail.getInvitation() != null) {
						final Integer eventCollectionId = calendarBackend.getCollectionId(bs);
						setInvitationStatus(emailCollectionId, eventCollectionId, syncedItem, mail, state, ic);
					} else {
						syncedItem.put(ic.getServerId(), ic);
					}
				}
			}
			
			final List<Long> emailUidToDeleted = storage.getEmailToDeleted(emailCollectionId, state.getKey());
			final List<ItemChange> itemsToDeleted = mailBackend.createItemsChangeToDeletedFromUidsInvitation(emailCollectionId, emailUidToDeleted);
			filterToDeletedEvent(itemsToDeleted, syncedItem);
			
			delta.getDeletions().addAll(itemsToDeleted);
			delta.getChanges().clear();
			delta.getChanges().addAll(syncedItem.values());
		
			logger.info(emailUidToDeleted.size() + " email(s) will be deleted on the PDA");
		} catch (ActiveSyncException e) {
			logger.info(e.getMessage(), e);
		}
	}

	private void setInvitationStatus(final Integer emailCollectionId, final Integer eventCollectionId, final Map<String, ItemChange> syncedItem, 
			final MSEmail mail, final SyncState state, final ItemChange ic) {
		if (!storage.isMostRecentInvitation(eventCollectionId, mail.getInvitation().getUID(), mail.getInvitation().getLastUpdate())) {
			logger.info("A more recent event or email is synchronized on phone. The email[UID: " + mail.getUid() + "dtstam: "
					+ mail.getInvitation().getDtStamp() + "] will not synced");
		} else {
			
			InvitationStatus status = null;
			String stateKey = null;
			boolean isAlreadyEventSynced = storage.eventIsAlreadySynced(eventCollectionId, mail.getInvitation().getUID());
			if (isAlreadyEventSynced) {
				status = InvitationStatus.EMAIL_TO_DELETED;
			} else {
				status = InvitationStatus.EMAIL_SYNCED;
				syncedItem.put(ic.getServerId(), ic);
				stateKey = state.getKey();
				storage.setEventStatusAtToDelete(eventCollectionId, mail.getInvitation().getUID());
			}
			
			storage.createOrUpdateInvitation(eventCollectionId, mail.getInvitation().getUID(), emailCollectionId, mail.getUid(), 
					mail.getInvitation().getDtStamp(), status, stateKey);
			
			logger.info("setInvitationStatus [ uid:" + mail.getUid() + ", eventCollId:" + eventCollectionId + ", isAlreadyEventSynced:" + isAlreadyEventSynced + " ]");
		}
	}

	private void filterToDeletedEvent(List<ItemChange> itemsToDeleted, Map<String, ItemChange> syncedItem) {
		for (final ItemChange ic: itemsToDeleted) {
            syncedItem.remove(ic.getServerId());
		}
	}

	private List<ItemChange> mergeChangesAndToSyncedEmail(BackendSession bs, SyncState state, Integer emailCollectionId, 
			List<ItemChange> changes) throws ActiveSyncException {
		final List<ItemChange> its = Lists.newArrayList(changes.iterator());
		final List<Long> emailToSync = storage.getEmailToSynced(emailCollectionId, state.getKey());
		for (final ItemChange ic : changes) {
			if (ic.getData() instanceof MSEmail) {
				MSEmail email = (MSEmail) ic.getData();
				emailToSync.remove(email.getUid());
			}
		}
		its.addAll(mailBackend.fetchItems(bs, emailCollectionId, emailToSync));
		return its;
	}

	@Override
	public int getCountFilterChanges(BackendSession bs, String syncKey, PIMDataType dataType, Integer collectionId) {
		switch (dataType) {
		case CALENDAR:
			return storage.getCountEventFilterChanges(collectionId, syncKey);
		case EMAIL:
			return storage.getCountEmailFilterChanges(collectionId, syncKey);
		default:
			return 0;
		}
	}

	@Override
	public void deleteFilteredEvent(Integer collectionId, String eventUid) {
		storage.updateInvitationEventStatus(InvitationStatus.DELETED, collectionId, ImmutableList.of(eventUid));
	}

	@Override
	public void deleteFilteredEmail(Integer collectionId, Long emailUid) {
		storage.updateInvitationStatus(InvitationStatus.DELETED, collectionId, ImmutableList.of(emailUid));
	}
	
}
