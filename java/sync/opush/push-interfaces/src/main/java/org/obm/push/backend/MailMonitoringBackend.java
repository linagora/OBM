package org.obm.push.backend;

import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;

public interface MailMonitoringBackend {

	void startMonitoringCollection(BackendSession bs, Integer collectionId, Set<ICollectionChangeListener> registeredListeners) throws CollectionNotFoundException, DaoException;
	
}
