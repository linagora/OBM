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
import org.obm.push.bean.SyncState;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.MailBackend;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContentsExporter implements IContentsExporter {

	private final MailBackend mailBackend;
	private final CalendarBackend calBackend;
	private final ContactsBackend contactsBackend;

	@Inject
	private ContentsExporter(MailBackend mailBackend,
			CalendarBackend calendarExporter, ContactsBackend contactsBackend) {
		
		this.mailBackend = mailBackend;
		this.calBackend = calendarExporter;
		this.contactsBackend = contactsBackend;
	}

	private DataDelta getTasksChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filterType, 
			PIMDataType dataType) throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException  {
		
		return this.calBackend.getContentChanges(bs, state, collectionId, filterType, dataType);
	}

	private DataDelta getCalendarChanges(BackendSession bs, SyncState state, Integer collectionId, FilterType filterType, 
			PIMDataType dataType) throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException {
		
		return calBackend.getContentChanges(bs, state, collectionId, filterType, dataType);
	}

	private int getItemEstimateSize(BackendSession bs, SyncState syncState, FilterType filterType, Integer collectionId, 
			PIMDataType dataType) throws DaoException, CollectionNotFoundException, 
			UnknownObmSyncServerException, ProcessingEmailException {
		
		DataDelta dataDelta = getChanged(bs, syncState, filterType, collectionId, dataType);
		return dataDelta.getItemEstimateSize();
	}
	

	private int getItemEmailEstimateSize(BackendSession bs, SyncState syncState, FilterType filterType, Integer collectionId) 
			throws CollectionNotFoundException, ProcessingEmailException {
		DataDelta changes = mailBackend.getMailChanges(bs, syncState, collectionId, filterType);
		return changes.getItemEstimateSize();
	}

	@Override
	public DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId, PIMDataType dataType) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
		switch (dataType) {
		case CALENDAR:
			return getCalendarChanges(bs, state, collectionId, filterType, dataType);
		case CONTACTS:
			return contactsBackend.getContactsChanges(bs, state, collectionId);
		case EMAIL:
			return mailBackend.getAndUpdateEmailChanges(bs, state, collectionId, filterType);
		case TASKS:
			return getTasksChanges(bs, state, collectionId, filterType, dataType);
		}
		return null;
	}
	
	@Override
	public List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType, List<String> fetchServerIds) 
			throws CollectionNotFoundException, DaoException, ProcessingEmailException, UnknownObmSyncServerException {
		
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
	public int getItemEstimateSize(BackendSession bs, FilterType filterType, Integer collectionId, SyncState state, PIMDataType dataType)
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnknownObmSyncServerException {
		
		if (dataType != null) {
			switch (dataType) {
			case CALENDAR:
				return getItemEstimateSize(bs, state, filterType, collectionId, dataType);
			case CONTACTS:
				return getItemEstimateSize(bs, state, filterType, collectionId, dataType);
			case EMAIL:
				return getItemEmailEstimateSize(bs, state, filterType, collectionId);
			case TASKS:
				return getItemEstimateSize(bs, state, filterType, collectionId, dataType);
			}
		}
		return 0;
	}
	
}
