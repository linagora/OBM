package org.obm.opush.env;

import org.obm.push.store.CalendarDao;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.EmailDao;
import org.obm.push.store.HearbeatDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;

public final class DaoModule extends AbstractOverrideModule {

	public DaoModule() {
		super();
	}

	@Override
	protected void configureImpl() {
		bindWithMock(CollectionDao.class);
		bindWithMock(DeviceDao.class);
		bindWithMock(EmailDao.class);
		bindWithMock(HearbeatDao.class);
		bindWithMock(MonitoredCollectionDao.class);
		bindWithMock(SyncedCollectionDao.class);
		bindWithMock(UnsynchronizedItemDao.class);
		bindWithMock(CalendarDao.class);
		bindWithMock(ItemTrackingDao.class);
	}
}