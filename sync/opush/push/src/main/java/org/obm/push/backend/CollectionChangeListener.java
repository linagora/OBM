package org.obm.push.backend;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.SyncCollection;

import com.google.common.collect.ComparisonChain;
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
		continuation.resume();
	}

	@Override
	public boolean monitorOneOf(ChangedCollections changedCollections) {
		TreeSet<SyncCollection> collectionPathSet = convertSetToComparePath(changedCollections);
		return !Sets.intersection(getMonitoredCollections(), collectionPathSet).isEmpty();
	}

	private TreeSet<SyncCollection> convertSetToComparePath(ChangedCollections changedCollections) {
		
		TreeSet<SyncCollection> collectionPathSet = Sets.newTreeSet(new Comparator<SyncCollection>() {

			@Override
			public int compare(SyncCollection o1, SyncCollection o2) {
				return ComparisonChain.start()
						.compare(o1.getCollectionPath(), o2.getCollectionPath())
						.result();
			}
		});
		collectionPathSet.addAll(changedCollections.getChanges());
		return collectionPathSet;
	}
	
}
