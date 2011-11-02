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
package org.obm.push;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
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

	private DataDelta getContactsChanges(BackendSession bs, SyncState state, Integer collectionId) throws UnknownObmSyncServerException {
		return contactsBackend.getContentChanges(bs, state, collectionId);
	}

	private DataDelta getTasksChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filterType) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException  {
		return this.calBackend.getContentChanges(bs, state, collectionId, filterType);
	}

	private DataDelta getCalendarChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filterType) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		return calBackend.getContentChanges(bs, state, collectionId, filterType);
	}

	private int getItemEstimateSize(BackendSession bs, SyncState syncState, FilterType filterType, Integer collectionId)
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
		DataDelta dataDelta = getChanged(bs, syncState, filterType, collectionId);
		return getCount(bs, syncState, collectionId, dataDelta);
	}
	
	private int getItemEmailEstimateSize(BackendSession bs, SyncState syncState, FilterType filterType, Integer collectionId) 
			throws CollectionNotFoundException, ProcessingEmailException, DaoException {
		
		DataDelta unfilteredDelta = mailBackend.getMailChanges(bs, syncState, collectionId, filterType);
		DataDelta filteredDelta = invitationFilterManager.filterInvitation(bs, syncState, collectionId, unfilteredDelta);
		return getCount(bs, syncState, collectionId, filteredDelta);
	}

	private int getCount(BackendSession bs, SyncState syncState, Integer collectionId, DataDelta delta) throws DaoException {
		int filterCount = invitationFilterManager.getCountFilterChanges(bs, syncState.getKey(), syncState.getDataType(), collectionId);
		int count = delta.getItemEstimateSize() + filterCount;
		logger.info("{} email(s) changes", count);
		return count;
	}
	
	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
		DataDelta delta = null;
		switch (state.getDataType()) {
		case CALENDAR:
			delta = getCalendarChanges(bs, state, collectionId, filterType);
			invitationFilterManager.filterEvent(bs, state, collectionId, delta);
			break;
		case CONTACTS:
			delta = getContactsChanges(bs, state, collectionId);
			break;
		case EMAIL:
			DataDelta unfilteredChanges = mailBackend.getAndUpdateEmailChanges(bs, state, collectionId, filterType);
			delta = invitationFilterManager.filterInvitation(bs, state, collectionId, unfilteredChanges);
			invitationFilterManager.createOrUpdateInvitation(bs, state, collectionId, unfilteredChanges);
			break;
		case TASKS:
			delta = getTasksChanges(bs, state, collectionId, filterType);
			break;
		case FOLDER:
			break;
		}
		return delta;
	}
	
	@Override
	public List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType, List<String> fetchServerIds) 
			throws CollectionNotFoundException, DaoException, ProcessingEmailException {
		
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
	public MSAttachementData getEmailAttachement(BackendSession bs, String attachmentId) 
			throws AttachementNotFoundException, CollectionNotFoundException, DaoException, ProcessingEmailException {
		return mailBackend.getAttachment(bs, attachmentId);
	}

	@Override
	public List<ItemChange> fetchEmails(BackendSession bs, Integer collectionId, Collection<Long> uids) 
			throws DaoException, CollectionNotFoundException, ProcessingEmailException {
		return mailBackend.fetchItems(bs, collectionId, uids);
	}

	@Override
	public boolean getFilterChanges(BackendSession bs, SyncCollection collection) throws DaoException {
		return invitationFilterManager.getCountFilterChanges(bs, collection.getSyncKey(), collection.getDataType(), collection.getCollectionId()) > 0;
	}

	@Override
	public int getItemEstimateSize(BackendSession bs, FilterType filterType, Integer collectionId, SyncState state) throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnknownObmSyncServerException {
		if (state.getDataType() != null) {
			switch (state.getDataType()) {
			case CALENDAR:
				return getItemEstimateSize(bs, state, filterType, collectionId);
			case CONTACTS:
				return getItemEstimateSize(bs, state, filterType, collectionId);
			case EMAIL:
				return getItemEmailEstimateSize(bs, state, filterType, collectionId);
			case FOLDER:
				return getItemEstimateSize(bs, state, filterType, collectionId);
			case TASKS:
				return getItemEstimateSize(bs, state, filterType, collectionId);
			}
		}
		return 0;
	}
	
}
