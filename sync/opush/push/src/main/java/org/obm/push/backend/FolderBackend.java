package org.obm.push.backend;

import java.sql.SQLException;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderBackend extends ObmSyncBackend {

	@Inject
	private FolderBackend(DeviceDao deviceDao,
			ConfigurationService configurationService, CollectionDao collectionDao)
			throws ConfigurationException {
		
		super(deviceDao, configurationService, collectionDao);
	}

	public void synchronize(BackendSession bs) throws SQLException {
		try {
			getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), getColName(bs));
		} catch (ActiveSyncException e) {
			createCollectionMapping(bs.getLoginAtDomain(), bs.getDevId(), getColName(bs));
		}
	}

	public int getServerIdFor(BackendSession bs) throws ActiveSyncException, SQLException {
		return getCollectionIdFor(bs.getLoginAtDomain(), bs.getDevId(), getColName(bs));
	}
	
	public String getColName(BackendSession bs){
		return "obm:\\\\" + bs.getLoginAtDomain();
	}

}
