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

import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.task.TaskBackend;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;

import com.google.common.collect.ImmutableList;

public class IntegrationPushTestUtils {

	public static void mockHierarchyChanges(ClassToInstanceAgregateView<Object> classToInstanceMap) throws DaoException, UnexpectedObmSyncServerException {
		mockAddressBook(classToInstanceMap);
		mockTask(classToInstanceMap);
		mockCalendar(classToInstanceMap);
	}
	
	public static void mockCalendar(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException {
		CalendarBackend calendarBackend = classToInstanceMap.get(CalendarBackend.class);
		expect(calendarBackend.getHierarchyChanges(anyObject(UserDataRequest.class)))
				.andReturn(ImmutableList.<ItemChange>of()).anyTimes();
	}
	
	public static void mockTask(ClassToInstanceAgregateView<Object> classToInstanceMap) {
		TaskBackend taskBackend = classToInstanceMap.get(TaskBackend.class);
		expect(taskBackend.getHierarchyChanges())
				.andReturn(ImmutableList.<ItemChange>of()).anyTimes();
	}
	
	public static void mockAddressBook(ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, UnexpectedObmSyncServerException {
		
		ContactsBackend contactsBackend = classToInstanceMap.get(ContactsBackend.class);
		expect(contactsBackend.getHierarchyChanges(anyObject(UserDataRequest.class), anyObject(Date.class)))
				.andReturn(new HierarchyItemsChanges(ImmutableList.<ItemChange>of(), ImmutableList.<ItemChange>of(), new Date())).anyTimes();	
	}

	public static void mockMonitoredCollectionDao(MonitoredCollectionDao monitoredCollectionDao) {
		monitoredCollectionDao.put(
				anyObject(Credentials.class), 
				anyObject(Device.class), 
				anyObject(Set.class));
		expectLastCall().anyTimes();
	}
	
}
