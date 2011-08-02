package org.obm.push.backend;

import java.util.Set;
import java.sql.SQLException;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.protocol.provisioning.Policy;

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

	void resetCollection(BackendSession bs, Integer collectionId) throws SQLException;

	boolean validatePassword(String userID, String password);

	Set<SyncCollection> getChangesSyncCollections(
			CollectionChangeListener collectionChangeListener) throws SQLException;
	
}
