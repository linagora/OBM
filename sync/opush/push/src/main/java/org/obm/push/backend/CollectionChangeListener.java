package org.obm.push.backend;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.store.SyncCollection;

public class CollectionChangeListener implements
		ICollectionChangeListener {

	private BackendSession bs;
	private Collection<SyncCollection> monitoredCollections;
	private Set<SyncCollection> dirtyCollections;
	private IContinuation continuation;

	public CollectionChangeListener(BackendSession bs,
			IContinuation c, Collection<SyncCollection> monitoredCollections) {
		this.bs = bs;
		this.monitoredCollections = monitoredCollections;
		this.continuation = c;
		this.dirtyCollections = new HashSet<SyncCollection>(0);
	}

	@Override
	public IContinuation getContinuation() {
		return continuation;
	}

	@Override
	public Set<SyncCollection> getDirtyCollections() {
		return dirtyCollections;
	}

	@Override
	public Collection<SyncCollection> getMonitoredCollections() {
		return monitoredCollections;
	}

	@Override
	public BackendSession getSession() {
		return bs;
	}
	
	public void changesDetected(Set<SyncCollection> dirtyCollections) {
		this.dirtyCollections = dirtyCollections;
		synchronized (bs) {
			continuation.setBackendSession(bs);
			continuation.resume();
		}
	}

}
