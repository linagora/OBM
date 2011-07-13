package org.obm.push.backend;

import java.sql.SQLException;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.dbcp.DBCP;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.ISyncStorage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderBackend extends ObmSyncBackend {

	@Inject
	private FolderBackend(ISyncStorage storage, DeviceDao deviceDao,
			ConfigurationService configurationService, DBCP dbcp)
			throws ConfigurationException {
		
		super(storage, deviceDao, configurationService, dbcp);
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
