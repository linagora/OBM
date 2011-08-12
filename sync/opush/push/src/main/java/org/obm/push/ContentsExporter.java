package org.obm.push;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ObjectNotFoundException;
import org.obm.push.mail.MailBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContentsExporter implements IContentsExporter {

	private static final Logger logger = LoggerFactory.getLogger(ContentsExporter.class);

	private final IInvitationFilterManager invitationFilterManager;
	
	private final MailBackend mailBackend;
	private final CalendarBackend calBackend;
	private final ContactsBackend contactsBackend;

	@Inject
	private ContentsExporter(MailBackend mailBackend,
			CalendarBackend calendarExporter, ContactsBackend contactsBackend, 
			IInvitationFilterManager invitationFilterManager) {
		
		this.mailBackend = mailBackend;
		this.calBackend = calendarExporter;
		this.contactsBackend = contactsBackend;
		this.invitationFilterManager = invitationFilterManager;
		
	}

	private void proccessFilterType(SyncState state, FilterType filterType) {
	
		if (filterType != null) {
			
			// FILTER_BY_NO_INCOMPLETE_TASKS;//8
			
			final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			switch (filterType) {
			case ONE_DAY_BACK:
				cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 1);
				break;
			case THREE_DAYS_BACK:
				cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 3);
				break;
			case ONE_WEEK_BACK:
				cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) - 1);
				break;
			case TWO_WEEKS_BACK:
				cal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR) - 2);
				break;
			case ONE_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
				break;
			case THREE_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 3);
				break;
			case SIX_MONTHS_BACK:
				cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 6);
				break;
			default:
			case ALL_ITEMS:
				cal.setTimeInMillis(0);
				break;
			}

			if (state.getLastSync() != null	&& cal.getTime().after(state.getLastSync())) {
				state.setLastSync(cal.getTime());
				state.setLastSyncFiltred(true);
			}
			
		}
	}

	private DataDelta getContactsChanges(BackendSession bs, SyncState state, Integer collectionId) {
		return contactsBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getTasksChanges(BackendSession bs, SyncState state, Integer collectionId) 
			throws CollectionNotFoundException, DaoException  {
		return this.calBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getCalendarChanges(BackendSession bs, SyncState state, Integer collectionId) 
			throws CollectionNotFoundException, DaoException {
		return calBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getMailChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filter) 
			throws CollectionNotFoundException, DaoException {
		return mailBackend.getContentChanges(bs, state, collectionId, filter);
	}
	
	@Override
	public int getCount(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId)
			throws DaoException, CollectionNotFoundException {
		
		DataDelta dd = getChanged(bs, state, filterType, collectionId);
		Integer filterCount = invitationFilterManager.getCountFilterChanges(bs, state.getKey(), state.getDataType(), collectionId);
		return dd.getChanges().size() + dd.getDeletions().size() + filterCount;
	}
	
	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state,
			FilterType filter, Integer collectionId) throws DaoException, CollectionNotFoundException {
		
		DataDelta delta = null;
		switch (state.getDataType()) {
		case CALENDAR:
			proccessFilterType(state, filter);
			delta = getCalendarChanges(bs, state, collectionId);
			invitationFilterManager.filterEvent(bs, state, collectionId, delta);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs, state, collectionId);
			break;
		case EMAIL:
			proccessFilterType(state, filter);
			delta = getMailChanges(bs, state, collectionId, filter);
			invitationFilterManager.filterInvitation(bs, state, collectionId, delta);
			break;
		case TASKS:
			delta = getTasksChanges(bs, state, collectionId);
			break;
		case FOLDER:
			break;
		}
		logger.info("Get changed from " + state.getLastSync() + " on collectionPath [ " + collectionId + " ]");
		
		return delta;
	}
	
	@Override
	public List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType,
			List<String> fetchServerIds) throws ObjectNotFoundException, CollectionNotFoundException, DaoException {
		
		LinkedList<ItemChange> changes = new LinkedList<ItemChange>();
		switch (getDataType) {
		case CONTACTS:
			changes.addAll(contactsBackend.fetchItems(bs, fetchServerIds));
			break;
		case EMAIL:
			changes.addAll(mailBackend.fetchItems(bs, fetchServerIds));
			break;
		case CALENDAR:
		case TASKS:
			changes.addAll(calBackend.fetchItems(bs, fetchServerIds));
			break;
		case FOLDER:
			break;
		}
		return changes;
	}

	@Override
	public MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentId) throws ObjectNotFoundException {
		return mailBackend.getAttachment(bs, attachmentId);
	}

	@Override
	public boolean validatePassword(String loginAtDomain, String password) {
		return calBackend.validatePassword(loginAtDomain, password);
	}

	@Override
	public List<ItemChange> fetchCalendars(BackendSession bs, Integer collectionId, Collection<String> uids) {
		return calBackend.fetchItems(bs, collectionId, uids);
	}
	
	@Override
	public List<ItemChange> fetchEmails(BackendSession bs, Integer collectionId, Collection<Long> uids) 
			throws DaoException, CollectionNotFoundException {
		return mailBackend.fetchItems(bs, collectionId, uids);
	}

	@Override
	public List<ItemChange> fetchCalendarDeletedItems(BackendSession bs, Integer collectionId, Collection<String> uids) {
		return calBackend.fetchDeletedItems(bs, collectionId, uids);
	}

	@Override
	public boolean getFilterChanges(BackendSession bs, SyncCollection collection) throws DaoException {
		return invitationFilterManager.getCountFilterChanges(bs, collection.getSyncKey(), collection.getDataType(), collection.getCollectionId()) > 0;
	}
	
}
