/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.mail.MailBackend;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.task.TaskBackend;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;

import com.google.common.collect.Lists;

public class IntegrationPushTestUtils {

	public static void mockHierarchyChangesOnlyInbox(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		HierarchyCollectionChanges hierarchyItemsChanges = HierarchyCollectionChanges.builder()
			.changes(Lists.newArrayList(buildInboxFolder()))
			.build();
		
		mockHierarchyChangesForMailboxes(classToInstanceMap, hierarchyItemsChanges);
	}

	public static void mockHierarchyChangesForMailboxes(ClassToInstanceAgregateView<Object> classToInstanceMap,
			HierarchyCollectionChanges mailboxesChanges) throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		mockAddressBook(classToInstanceMap);
		mockTask(classToInstanceMap);
		mockCalendar(classToInstanceMap);
		mockMailBackend(classToInstanceMap, mailboxesChanges);
	}

	public static void mockCalendar(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(calendarBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange()).anyTimes();
	}
	
	public static void mockTask(ClassToInstanceAgregateView<Object> classToInstanceMap) throws DaoException {
		TaskBackend taskBackend = classToInstanceMap.get(TaskBackend.class);
		expect(taskBackend.getPIMDataType()).andReturn(PIMDataType.TASKS).anyTimes();
		expect(taskBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange()).anyTimes();
	}
	
	public static void mockAddressBook(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException, InvalidSyncKeyException {
		
		ContactsBackend contactsBackend = classToInstanceMap.get(ContactsBackend.class);
		expect(contactsBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS).anyTimes();
		expect(contactsBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(emptyChange()).anyTimes();
	}
	
	public static void mockMailBackend(ClassToInstanceAgregateView<Object> classToInstanceMap, HierarchyCollectionChanges hierarchyMailboxesChanges)
			throws DaoException, InvalidSyncKeyException {
		
		MailBackend mailBackend = classToInstanceMap.get(MailBackend.class);
		expect(mailBackend.getPIMDataType()).andReturn(PIMDataType.EMAIL).anyTimes();
		expect(mailBackend.getHierarchyChanges(anyObject(UserDataRequest.class),
				anyObject(FolderSyncState.class), anyObject(FolderSyncState.class)))
			.andReturn(hierarchyMailboxesChanges).anyTimes();
	}

	private static HierarchyCollectionChanges emptyChange() {
		return HierarchyCollectionChanges.builder().build();
	}
	
	public static void mockNextGeneratedSyncKey(
			ClassToInstanceAgregateView<Object> classToInstanceMap, SyncKey...nextSyncKeys) {
		
		SyncKeyFactory syncKeyFactory = classToInstanceMap.get(SyncKeyFactory.class);
		for (SyncKey nextSyncKey : nextSyncKeys) {
			expect(syncKeyFactory.randomSyncKey()).andReturn(nextSyncKey).once();
		}
	}

	public static CollectionChange buildInboxFolder() {
		return CollectionChange.builder()
				.collectionId("1")
				.parentCollectionId("1")
				.displayName("INBOX")
				.folderType(FolderType.DEFAULT_INBOX_FOLDER)
				.isNew(true)
				.build();
	}
}
