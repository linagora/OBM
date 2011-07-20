package org.obm.push;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.impl.Credentials;

public class UnSynchronizedItemImplTest {

	private ObjectStoreManager objectStoreManager;
	private UnsynchronizedItemImpl unSynchronizedItemImpl;
	private Credentials credentials;
	
	@Before
	public void init() {
		this.objectStoreManager = new ObjectStoreManager();
		this.unSynchronizedItemImpl = new UnsynchronizedItemImpl(objectStoreManager);
		this.credentials = new Credentials("login@domain", "password");
	}
	
	@Test
	public void list() {
		List<ItemChange> itemChanges = unSynchronizedItemImpl.list(credentials, 1);
		Assert.assertNotNull(itemChanges);
	}
	
	@Test
	public void add() {
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 1"));
		List<ItemChange> itemChanges = unSynchronizedItemImpl.list(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(1, itemChanges.size());
		Assert.assertEquals("test 1", itemChanges.get(0).getDisplayName());
	}
	
	@Test
	public void addTwoItemsOnTheSameCollection() {
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 1"));
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 2"));
		List<ItemChange> itemChanges = unSynchronizedItemImpl.list(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(2, itemChanges.size());
		Assert.assertEquals("test 1", itemChanges.get(0).getDisplayName());
		Assert.assertEquals("test 2", itemChanges.get(1).getDisplayName());
	}
	
	@Test
	public void addItemsOnTwoCollections() {
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 1.1"));
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 1.2"));
		unSynchronizedItemImpl.add(credentials, 2, buildItemChange("test 2.1"));
		
		List<ItemChange> itemChangesOneCollection = unSynchronizedItemImpl.list(credentials, 1);
		List<ItemChange> itemChangesTwoCollection = unSynchronizedItemImpl.list(credentials, 2);
		
		Assert.assertNotNull(itemChangesOneCollection);
		Assert.assertEquals(2, itemChangesOneCollection.size());
		Assert.assertEquals("test 1.1", itemChangesOneCollection.get(0).getDisplayName());
		Assert.assertEquals("test 1.2", itemChangesOneCollection.get(1).getDisplayName());
		
		Assert.assertNotNull(itemChangesTwoCollection);
		Assert.assertEquals(1, itemChangesTwoCollection.size());
		Assert.assertEquals("test 2.1", itemChangesTwoCollection.get(0).getDisplayName());	
	}
	
	@Test
	public void clear() {
		unSynchronizedItemImpl.add(credentials, 1, buildItemChange("test 1"));
		unSynchronizedItemImpl.clear(credentials, 1);		
		
		List<ItemChange> itemChanges = unSynchronizedItemImpl.list(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(0, itemChanges.size());
	}
	
	private ItemChange buildItemChange(String displayName) {
		ItemChange itemChange = new ItemChange();
		itemChange.setDisplayName(displayName);
		return itemChange;
	}
	
}
