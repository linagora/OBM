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
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.MailBackend;
import org.obm.push.task.TaskBackend;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HierarchyExporter implements IHierarchyExporter {

	private final FolderBackend folderExporter;
	private final MailBackend mailExporter;
	private final CalendarBackend calendarExporter;
	private final ContactsBackend contactsBackend;
	private final TaskBackend taskBackend;

	@Inject
	private HierarchyExporter(FolderBackend folderExporter,
			MailBackend mailExporter, CalendarBackend calendarExporter,
			ContactsBackend contactsBackend, TaskBackend taskBackend) {
		
		this.folderExporter = folderExporter;
		this.mailExporter = mailExporter;
		this.calendarExporter = calendarExporter;
		this.contactsBackend = contactsBackend;
		this.taskBackend = taskBackend;
	}

	private List<ItemChange> getTasksChanges() {
		return taskBackend.getHierarchyChanges();
	}

	private List<ItemChange> getCalendarChanges(UserDataRequest udr) throws DaoException, UnexpectedObmSyncServerException {
		return calendarExporter.getHierarchyChanges(udr);
	}

	private List<ItemChange> getMailChanges(UserDataRequest udr) throws DaoException {
		return mailExporter.getHierarchyChanges(udr);
	}

	@Override
	public HierarchyItemsChanges getChanged(UserDataRequest udr, Date lastSync) throws DaoException, UnexpectedObmSyncServerException {
		LinkedList<ItemChange> allItemsChanged = new LinkedList<ItemChange>();
		
		allItemsChanged.addAll(getCalendarChanges(udr));
		allItemsChanged.addAll(getMailChanges(udr));
		
		HierarchyItemsChanges itemsContactChanged = listContactFoldersChanged(udr, lastSync);
		allItemsChanged.addAll(itemsContactChanged.getChangedItems());
		
		allItemsChanged.addAll(getTasksChanges());

		return new HierarchyItemsChanges.Builder()
			.changes(allItemsChanged)
			.deletions(itemsContactChanged.getDeletedItems())
			.lastSync(itemsContactChanged.getLastSync()).build();
	}
	
	@Override
	public HierarchyItemsChanges listContactFoldersChanged(UserDataRequest udr, Date lastSync) throws DaoException, UnexpectedObmSyncServerException {
		return contactsBackend.getHierarchyChanges(udr, lastSync);
	}
	
	@Override
	public int getRootFolderId(UserDataRequest udr) throws DaoException, CollectionNotFoundException {
		return folderExporter.getServerIdFor(udr);
	}

	@Override
	public String getRootFolderUrl(UserDataRequest udr) {
		return folderExporter.getColName(udr);
	}

}
