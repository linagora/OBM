package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.services.IAddressBook;

public class IntegrationPushTestUtils {

	public static void mockAddressBook(SingleUserFixture singleUserFixture, 
			ClassToInstanceAgregateView<Object> classToInstanceMap) throws ServerFault {
		
		mockAddressBook(singleUserFixture, classToInstanceMap, new FolderChanges());
	}
	
	public static void mockAddressBook(SingleUserFixture singleUserFixture, 
			ClassToInstanceAgregateView<Object> classToInstanceMap, FolderChanges folderChanges) throws ServerFault {
		
		IAddressBook iAddressBook = classToInstanceMap.get(IAddressBook.class);
		expect(
				iAddressBook.listAddressBooksChanged(
						eq(singleUserFixture.jaures.accessToken), anyObject(Date.class)))
						.andReturn(folderChanges);
	}
	
	public static void mockMonitoredCollectionDao(MonitoredCollectionDao monitoredCollectionDao) {
		monitoredCollectionDao.put(
				anyObject(Credentials.class), 
				anyObject(Device.class), 
				anyObject(Set.class));
		expectLastCall().anyTimes();
	}
	
}
