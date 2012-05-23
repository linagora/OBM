package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Date;
import java.util.Set;

import org.obm.push.bean.BackendSession;
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
		expect(calendarBackend.getHierarchyChanges(anyObject(BackendSession.class)))
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
		expect(contactsBackend.getHierarchyChanges(anyObject(BackendSession.class), anyObject(Date.class)))
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
