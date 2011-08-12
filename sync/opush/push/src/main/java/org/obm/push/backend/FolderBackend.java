package org.obm.push.backend;

import javax.naming.ConfigurationException;

import org.obm.configuration.ObmConfigurationService;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.store.CollectionDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderBackend extends ObmSyncBackend {

	@Inject
	private FolderBackend(ObmConfigurationService configurationService, CollectionDao collectionDao)
			throws ConfigurationException {
		
		super(configurationService, collectionDao);
	}

	public void synchronize(BackendSession bs) throws DaoException {
		try {
			getCollectionIdFor(bs.getDevice(), getColName(bs));
		} catch (CollectionNotFoundException e) {
			createCollectionMapping(bs.getDevice(), getColName(bs));
		}
	}

	public int getServerIdFor(BackendSession bs) throws DaoException, CollectionNotFoundException {
		return getCollectionIdFor(bs.getDevice(), getColName(bs));
	}
	
	public String getColName(BackendSession bs){
		return "obm:\\\\" + bs.getLoginAtDomain();
	}

}
