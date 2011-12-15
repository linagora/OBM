package org.obm.opush;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Set;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.store.MonitoredCollectionDao;

public class IntegrationPushTestUtils {


	public static void mockMonitoredCollectionDao(MonitoredCollectionDao monitoredCollectionDao) {
		monitoredCollectionDao.put(
				anyObject(Credentials.class), 
				anyObject(Device.class), 
				anyObject(Set.class));
		expectLastCall().anyTimes();
	}
	
}
