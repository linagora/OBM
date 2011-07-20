package org.obm.push.backend;

import java.util.Collection;

import org.obm.push.store.SyncCollection;

public class CollectionChangeListener implements
		ICollectionChangeListener {

	private BackendSession bs;
	private Collection<SyncCollection> monitoredCollections;
	private IContinuation continuation;

	public CollectionChangeListener(BackendSession bs,
			IContinuation c, Collection<SyncCollection> monitoredCollections) {
		this.bs = bs;
		this.monitoredCollections = monitoredCollections;
		this.continuation = c;
	}

	@Override
	public IContinuation getContinuation() {
		return continuation;
	}

	@Override
	public Collection<SyncCollection> getMonitoredCollections() {
		return monitoredCollections;
	}

	@Override
	public BackendSession getSession() {
		return bs;
	}
	
	public void changesDetected() {
		continuation.setBackendSession(bs);
		continuation.resume();
	}

}
