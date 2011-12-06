package org.obm.push.store.ehcache;

import java.util.Collection;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.store.StoreNotFoundException;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.User;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.User.Factory;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.Lists;

public class SyncedCollectionDaoEhcacheImplTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager objectStoreManager;
	private SyncedCollectionDaoEhcacheImpl syncedCollectionStoreServiceImpl;
	private Credentials credentials;
	private TransactionManager transactionManager;
	
	@Before
	public void init() throws StoreNotFoundException, NotSupportedException, SystemException {
		this.transactionManager = TransactionManagerServices.getTransactionManager();
		transactionManager.begin();
		this.objectStoreManager = new ObjectStoreManager( super.initConfigurationServiceMock() );
		this.syncedCollectionStoreServiceImpl = new SyncedCollectionDaoEhcacheImpl(objectStoreManager);
		User user = Factory.create().createUser("login@domain", "email@domain");
		this.credentials = new Credentials(user, "password");
	}
	
	@After
	public void cleanup() throws IllegalStateException, SecurityException, SystemException {
		transactionManager.rollback();
	}
	
	@Test
	public void get() {
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNull(syncCollection);
	}
	
	@Test
	public void put() {
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1));
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
	}
	
	@Test
	public void putUpdatedCollection() {
		Collection<SyncCollection> cols = buildListCollection(1);
		cols.iterator().next().setCollectionPath("PATH1");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), cols);
		cols.iterator().next().setCollectionPath("PATH1CHANGE");
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), cols);
		
		SyncCollection syncCollection = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		Assert.assertNotNull(syncCollection);
		Assert.assertEquals(new Integer(1), syncCollection.getCollectionId());
		Assert.assertEquals("PATH1CHANGE", syncCollection.getCollectionPath());
	}
	
	@Test
	public void putList() {
		syncedCollectionStoreServiceImpl.put(credentials, getFakeDeviceId(), buildListCollection(1,2,3));
		SyncCollection syncCollection1 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 1);
		SyncCollection syncCollection2 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 2);
		SyncCollection syncCollection3 = syncedCollectionStoreServiceImpl.get(credentials, getFakeDeviceId(), 3);
		
		Assert.assertNotNull(syncCollection1);
		Assert.assertEquals(new Integer(1), syncCollection1.getCollectionId());
		Assert.assertNotNull(syncCollection2);
		Assert.assertEquals(new Integer(2), syncCollection2.getCollectionId());
		Assert.assertNotNull(syncCollection3);
		Assert.assertEquals(new Integer(3), syncCollection3.getCollectionId());
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
	
	private Device getFakeDeviceId(){
		return new Device(1, "DevType", "DevId", null);
	}
	
}
