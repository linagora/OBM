package org.obm.push.backend;

import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.activesync.ActiveSyncException;
import org.obm.push.protocol.provisioning.Policy;
import org.obm.push.exception.DaoException;

public interface IBackend {

	String getWasteBasket();

	Policy getDevicePolicy(BackendSession bs);

	/**
	 * Push support
	 * 
	 * @param ccl
	 * @return a registration that the caller can use to cancel monitor of a
	 *         ressource
	 */
	IListenerRegistration addChangeListener(ICollectionChangeListener ccl);

	void startEmailMonitoring(BackendSession bs, Integer collectionId)
			throws ActiveSyncException;

	void resetCollection(BackendSession bs, Integer collectionId) throws DaoException;

	boolean validatePassword(String userID, String password);

	Set<SyncCollection> getChangesSyncCollections(
			CollectionChangeListener collectionChangeListener) throws DaoException;
	
}
