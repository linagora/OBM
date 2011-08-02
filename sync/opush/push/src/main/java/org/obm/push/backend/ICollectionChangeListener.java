package org.obm.push.backend;

import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.SyncCollection;

/**
 * This interface is used in the push process to wait for changes.
 * 
 * The backend will use the {@link IContinuation} to wake up the caller.
 * 
 * @author tom
 * 
 */
public interface ICollectionChangeListener {

	Set<SyncCollection> getMonitoredCollections();

	BackendSession getSession();

	IContinuation getContinuation();

	/**
	 * Called by backend when a sync is needed.
	 * 
	 */
	void changesDetected();

	boolean monitorOneOf(ChangedCollections changedCollections);

}
