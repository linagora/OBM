package org.obm.push.store;

import org.obm.dbcp.DBCP;
import org.obm.dbcp.IDBCP;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.SyncedCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl;
import org.obm.push.store.jdbc.CalendarDaoJdbcImpl;
import org.obm.push.store.jdbc.CollectionDaoJdbcImpl;
import org.obm.push.store.jdbc.DeviceDaoJdbcImpl;
import org.obm.push.store.jdbc.EmailDaoJdbcImpl;
import org.obm.push.store.jdbc.FiltrageInvitationDaoJdbcImpl;
import org.obm.push.store.jdbc.HearbeatDaoJdbcDaoImpl;

import com.google.inject.AbstractModule;

public class DaoModule extends AbstractModule{

	@Override
	protected void configure() {

		bind(IDBCP.class).to(DBCP.class);
		bind(CollectionDao.class).to(CollectionDaoJdbcImpl.class);
		bind(DeviceDao.class).to(DeviceDaoJdbcImpl.class);
		bind(EmailDao.class).to(EmailDaoJdbcImpl.class);
		bind(FiltrageInvitationDao.class).to(FiltrageInvitationDaoJdbcImpl.class);
		bind(HearbeatDao.class).to(HearbeatDaoJdbcDaoImpl.class);
		bind(MonitoredCollectionDao.class).to(MonitoredCollectionDaoEhcacheImpl.class);
		bind(SyncedCollectionDao.class).to(SyncedCollectionDaoEhcacheImpl.class);
		bind(UnsynchronizedItemDao.class).to(UnsynchronizedItemDaoEhcacheImpl.class);
		bind(CalendarDao.class).to(CalendarDaoJdbcImpl.class);
	}

}
