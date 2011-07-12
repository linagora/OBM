package org.obm.push.backend;

import java.util.Set;

import org.obm.push.provisioning.Policy;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.SyncCollection;

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

	void resetCollection(BackendSession bs, Integer collectionId);

	boolean validatePassword(String userID, String password);

	Set<SyncCollection> getChangesSyncCollections(
			CollectionChangeListener collectionChangeListener);
	
}
