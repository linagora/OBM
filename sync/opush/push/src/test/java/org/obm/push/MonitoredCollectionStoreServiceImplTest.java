package org.obm.push;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

import com.google.common.collect.Lists;

public class MonitoredCollectionStoreServiceImplTest {

	private ObjectStoreManager objectStoreManager;
	private MonitoredCollectionStoreServiceImpl monitoredCollectionStoreServiceImpl;
	private Credentials credentials;
	
	@Before
	public void init() {
		this.objectStoreManager = new ObjectStoreManager();
		this.monitoredCollectionStoreServiceImpl = new MonitoredCollectionStoreServiceImpl(objectStoreManager);
		this.credentials = new Credentials("login@domain", "password");
	}
	
	@Test
	public void testList() {
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials);
		Assert.assertNotNull(syncCollections);
	}
	
	@Test
	public void testReplace() {
		monitoredCollectionStoreServiceImpl.put(credentials, buildListCollection(1));
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials);
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(1, syncCollections.size());
		containsCollectionWithId(syncCollections, 1);
		
		
		monitoredCollectionStoreServiceImpl.put(credentials, buildListCollection(2, 3));
		syncCollections = monitoredCollectionStoreServiceImpl.list(credentials);
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(2, syncCollections.size());
		containsCollectionWithId(syncCollections, 2);
		containsCollectionWithId(syncCollections, 3);
		
		monitoredCollectionStoreServiceImpl.put(credentials, buildListCollection());
		syncCollections = monitoredCollectionStoreServiceImpl.list(credentials);
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(0, syncCollections.size());
	}
	
	private void containsCollectionWithId(
			Collection<SyncCollection> syncCollections, Integer id) {
		boolean find = false;
		for(SyncCollection col : syncCollections){
			if(col.getCollectionId().equals(id)){
				find = true;
			}
		}
		Assert.assertTrue(find);
	}

	private Collection<SyncCollection> buildListCollection(Integer... ids) {
		List<SyncCollection> cols = Lists.newLinkedList();
		for(Integer id : ids){
			SyncCollection col = new SyncCollection();
			col.setCollectionId(id);
			cols.add(col);
		}
		return cols;
	}
}
