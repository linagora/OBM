package org.obm.push.backend;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.dbcp.DBCP;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.ISyncStorage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderBackend extends ObmSyncBackend {

	@Inject
	private FolderBackend(ISyncStorage storage,
			ConfigurationService configurationService, DBCP dbcp)
			throws ConfigurationException {
		
		super(storage, configurationService, dbcp);
	}

	public void synchronize(BackendSession bs) {
		try {
			getCollectionIdFor(bs.getDevId(), getColName(bs));
		} catch (ActiveSyncException e) {
			createCollectionMapping(bs.getDevId(), getColName(bs));
		}
	}

	public int getServerIdFor(BackendSession bs) throws ActiveSyncException {
		return getCollectionIdFor(bs.getDevId(), getColName(bs));
	}
	
	public String getColName(BackendSession bs){
		return "obm:\\\\" + bs.getLoginAtDomain();
	}

}
