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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.backend.FolderBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.MailBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HierarchyExporter implements IHierarchyExporter {

	private static final Logger logger = LoggerFactory.getLogger(HierarchyExporter.class);

	private final FolderBackend folderExporter;
	private final MailBackend mailExporter;
	private final CalendarBackend calendarExporter;
	private final ContactsBackend contactsBackend;

	@Inject
	private HierarchyExporter(FolderBackend folderExporter,
			MailBackend mailExporter, CalendarBackend calendarExporter,
			ContactsBackend contactsBackend) {
		
		this.folderExporter = folderExporter;
		this.mailExporter = mailExporter;
		this.calendarExporter = calendarExporter;
		this.contactsBackend = contactsBackend;
	}

	@Override
	public void configure(SyncState state, String dataClass,
			Integer filterType, int i, int j) {
		logger.info("configure(bs, " + dataClass + ", " + filterType + ", "
				+ state + ", " + i + ", " + j + ")");
		if (dataClass != null) {
			state.setDataType(PIMDataType.valueOf(dataClass.toUpperCase()));
		} else {
			state.setDataType(null);
		}
	}

	private HierarchyItemsChanges getContactsChanges(BackendSession bs, Date lastSync) throws DaoException {
		return contactsBackend.getHierarchyChanges(bs, lastSync);
	}

	private List<ItemChange> getTasksChanges(BackendSession bs) throws DaoException {
		return calendarExporter.getHierarchyTaskChanges(bs);
	}

	private List<ItemChange> getCalendarChanges(BackendSession bs) throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {
		return calendarExporter.getHierarchyChanges(bs);
	}

	private List<ItemChange> getMailChanges(BackendSession bs) throws DaoException {
		return mailExporter.getHierarchyChanges(bs);
	}

	@Override
	public HierarchyItemsChanges getChanged(BackendSession bs, Date lastSync) throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {
		LinkedList<ItemChange> allItemsChanged = new LinkedList<ItemChange>();
		
		allItemsChanged.addAll(getCalendarChanges(bs));
		allItemsChanged.addAll(getMailChanges(bs));
		
		HierarchyItemsChanges itemsContactChanged = getContactsChanges(bs, lastSync);
		allItemsChanged.addAll(itemsContactChanged.getItemsAddedOrUpdated());
		
		allItemsChanged.addAll(getTasksChanges(bs));

		return new HierarchyItemsChanges(
				allItemsChanged, itemsContactChanged.getItemsDeleted());
	}

	@Override
	public int getRootFolderId(BackendSession bs) throws DaoException, CollectionNotFoundException {
		return folderExporter.getServerIdFor(bs);
	}

	@Override
	public String getRootFolderUrl(BackendSession bs) {
		return folderExporter.getColName(bs);
	}

}
