package org.obm.push.backend;

import java.util.Collection;

import org.obm.push.store.SyncCollection;

/**
 * This interface is used in the push process to wait for changes.
 * 
 * The backend will use the {@link IContinuation} to wake up the caller.
 * 
 * @author tom
 * 
 */
public interface ICollectionChangeListener {

	Collection<SyncCollection> getMonitoredCollections();

	BackendSession getSession();

	IContinuation getContinuation();

	/**
	 * Called by backend when a sync is needed.
	 * 
	 */
	void changesDetected();

}
