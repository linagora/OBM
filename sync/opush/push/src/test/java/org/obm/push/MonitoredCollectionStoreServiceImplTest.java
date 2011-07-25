package org.obm.push;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.store.StoreNotFoundException;
import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.google.common.collect.Sets;

public class MonitoredCollectionStoreServiceImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private MonitoredCollectionStoreServiceImpl monitoredCollectionStoreServiceImpl;
	private Credentials credentials;
	private UserTransactionManager transactionManager;
	
	@Before
	public void init() throws FileNotFoundException, StoreNotFoundException, NotSupportedException, SystemException {
		this.transactionManager = new UserTransactionManager();
		transactionManager.begin();
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock() );
		this.monitoredCollectionStoreServiceImpl = new MonitoredCollectionStoreServiceImpl(objectStoreManager);
		this.credentials = new Credentials("login@domain", "password");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		transactionManager.rollback();
	}
	
	@Test
	public void testList() {
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
	}
	
	@Test
	public void testSimplePut() {
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(1, syncCollections.size());
		containsCollectionWithId(syncCollections, 1);
	}
	
	@Test
	public void testPutNewItems() {
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		monitoredCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(2, 3));

		Collection<SyncCollection> syncCollections = monitoredCollectionStoreServiceImpl.list(credentials, getFakeDeviceId());
		Assert.assertNotNull(syncCollections);
		Assert.assertEquals(2, syncCollections.size());
		containsCollectionWithId(syncCollections, 2);
		containsCollectionWithId(syncCollections, 3);
		
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

	private Set<SyncCollection> buildListCollection(Integer... ids) {
		Set<SyncCollection> cols = Sets.newHashSet();
		for(Integer id : ids){
			SyncCollection col = new SyncCollection();
			col.setCollectionId(id);
			cols.add(col);
		}
		return cols;
	}
	
	private Device getFakeDeviceId(){
		return new Device("DevType", "DevId", null);
	}
}
