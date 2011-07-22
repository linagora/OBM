package org.obm.push.backend;

import java.util.Set;

import org.obm.push.impl.ChangedCollections;
import org.obm.push.store.SyncCollection;

import com.google.common.collect.Sets;

public class CollectionChangeListener implements
		ICollectionChangeListener {

	private BackendSession bs;
	private Set<SyncCollection> monitoredCollections;
	private IContinuation continuation;

	public CollectionChangeListener(BackendSession bs,
			IContinuation c, Set<SyncCollection> monitoredCollections) {
		this.bs = bs;
		this.monitoredCollections = monitoredCollections;
		this.continuation = c;
	}

	@Override
	public IContinuation getContinuation() {
		return continuation;
	}

	@Override
	public Set<SyncCollection> getMonitoredCollections() {
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

	@Override
	public boolean monitorOneOf(ChangedCollections changedCollections) {
		return !Sets.intersection(getMonitoredCollections(), changedCollections.getChanged()).isEmpty();
	}
	
}
