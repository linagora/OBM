package org.obm.push.backend;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.obm.push.impl.ChangedCollections;
import org.obm.push.store.SyncCollection;

import com.google.common.collect.ImmutableSet;

public class CollectionChangeListenerTest {

	@Test
	public void testMonitorOf() {
		String matchString = "mypath";
		ImmutableSet<SyncCollection> monitored = ImmutableSet.of(new SyncCollection(1, matchString), new SyncCollection(2, "dontmatch"));
		ImmutableSet<SyncCollection> notify = ImmutableSet.of(new SyncCollection(0, matchString));
		CollectionChangeListener collectionChangeListener = new CollectionChangeListener(null, null, monitored);
		ChangedCollections changed = new ChangedCollections(new Date(), notify);
		boolean result = collectionChangeListener.monitorOneOf(changed);
		assertTrue(result);
	}

	@Test
	public void testMonitorOfDontMatch() {
		ImmutableSet<SyncCollection> monitored = ImmutableSet.of(new SyncCollection(1, "mypath"), new SyncCollection(2, "dontmatch"));
		ImmutableSet<SyncCollection> notify = ImmutableSet.of(new SyncCollection(0, "anotherpath"));
		CollectionChangeListener collectionChangeListener = new CollectionChangeListener(null, null, monitored);
		ChangedCollections changed = new ChangedCollections(new Date(), notify);
		boolean result = collectionChangeListener.monitorOneOf(changed);
		assertTrue(!result);
	}
}
