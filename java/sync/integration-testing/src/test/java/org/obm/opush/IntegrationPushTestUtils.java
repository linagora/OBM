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
package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.mail.MailBackend;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.task.TaskBackend;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;

import com.google.common.collect.Lists;

public class IntegrationPushTestUtils {

	public static void mockHierarchyChangesOnlyInbox(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		mockHierarchyChangesOnlyInbox(classToInstanceMap, new Date());
	}
	
	public static void mockHierarchyChangesOnlyInbox(ClassToInstanceAgregateView<Object> classToInstanceMap, Date newSyncDate)
			throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		HierarchyItemsChanges hierarchyItemsChanges = new HierarchyItemsChanges.Builder()
			.changes(Lists.newArrayList(buildInboxFolder()))
			.lastSync(newSyncDate).build();
		
		mockHierarchyChangesForMailboxes(classToInstanceMap, hierarchyItemsChanges);
	}

	public static void mockHierarchyChangesForMailboxes(ClassToInstanceAgregateView<Object> classToInstanceMap,
			HierarchyItemsChanges mailboxesChanges) throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		Date lastSync = mailboxesChanges.getLastSync();
		mockAddressBook(classToInstanceMap, lastSync);
		mockTask(classToInstanceMap, lastSync);
		mockCalendar(classToInstanceMap, lastSync);
		mockMailBackend(classToInstanceMap, mailboxesChanges);
	}

	public static void mockCalendar(ClassToInstanceAgregateView<Object> classToInstanceMap, Date newSyncDate)
			throws DaoException, UnexpectedObmSyncServerException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(calendarBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange(newSyncDate)).anyTimes();
	}
	
	public static void mockTask(ClassToInstanceAgregateView<Object> classToInstanceMap, Date newSyncDate) throws DaoException {
		TaskBackend taskBackend = classToInstanceMap.get(TaskBackend.class);
		expect(taskBackend.getPIMDataType()).andReturn(PIMDataType.TASKS).anyTimes();
		expect(taskBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange(newSyncDate)).anyTimes();
	}
	
	public static void mockAddressBook(ClassToInstanceAgregateView<Object> classToInstanceMap, Date newSyncDate)
			throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		ContactsBackend contactsBackend = classToInstanceMap.get(ContactsBackend.class);
		expect(contactsBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS).anyTimes();
		expect(contactsBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange(newSyncDate)).anyTimes();
	}
	
	public static void mockMailBackend(ClassToInstanceAgregateView<Object> classToInstanceMap, HierarchyItemsChanges hierarchyMailboxesChanges)
			throws DaoException, InvalidSyncKeyException {
		
		MailBackend mailBackend = classToInstanceMap.get(MailBackend.class);
		expect(mailBackend.getPIMDataType()).andReturn(PIMDataType.EMAIL).anyTimes();
		expect(mailBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(hierarchyMailboxesChanges).anyTimes();
	}

	private static HierarchyItemsChanges emptyChange(Date newSyncDate) {
		return new HierarchyItemsChanges.Builder().lastSync(newSyncDate).build();
	}
	
	public static void mockNextGeneratedSyncKey(ClassToInstanceAgregateView<Object> classToInstanceMap, String newSyncKey) {
		expect(classToInstanceMap.get(SyncKeyFactory.class).randomSyncKey()).andReturn(newSyncKey).once();
	}

	public static ItemChange buildInboxFolder() {
		return new ItemChange("1", "1", "INBOX", FolderType.DEFAULT_INBOX_FOLDER, true);
	}
	
	public static void mockMonitoredCollectionDao(MonitoredCollectionDao monitoredCollectionDao) {
		monitoredCollectionDao.put(
				anyObject(Credentials.class), 
				anyObject(Device.class), 
				anyObject(Set.class));
		expectLastCall().anyTimes();
	}
	
}
