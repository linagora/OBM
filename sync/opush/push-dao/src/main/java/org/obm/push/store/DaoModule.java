package org.obm.push.store;

import org.obm.push.store.jdbc.CollectionDaoJdbcImpl;
import org.obm.push.store.jdbc.DeviceDaoJdbcImpl;
import org.obm.push.store.jdbc.EmailDaoJdbcImpl;
import org.obm.push.store.jdbc.FiltrageInvitationDaoJdbcImpl;

import com.google.inject.AbstractModule;

public class DaoModule extends AbstractModule{

	@Override
	protected void configure() {

		bind(CollectionDao.class).to(CollectionDaoJdbcImpl.class);
		bind(DeviceDao.class).to(DeviceDaoJdbcImpl.class);
		bind(EmailDao.class).to(EmailDaoJdbcImpl.class);
		bind(FiltrageInvitationDao.class).to(FiltrageInvitationDaoJdbcImpl.class);

	}

}
