package org.obm.push;

import java.util.Set;

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
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Assert.assertNotNull(itemChanges);
	}
	
	@Test
	public void add() {
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, buildItemChange("test 1"));
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(1, itemChanges.size());
		Assert.assertEquals("test 1", itemChanges.iterator().next().getDisplayName());
	}
	
	@Test
	public void addTwoItemsOnTheSameCollection() {
		ItemChange ItemChange1 = buildItemChange("test 1");
		ItemChange ItemChange2 = buildItemChange("test 2");
		ItemChange ItemChange3 = buildItemChange("test 3");
		
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, ItemChange1);
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, ItemChange2);
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(2, itemChanges.size());
		
		Assert.assertTrue(itemChanges.contains(ItemChange1));
		Assert.assertTrue(itemChanges.contains(ItemChange2));
		Assert.assertFalse(itemChanges.contains(ItemChange3));
	}
	
	@Test
	public void addItemsOnTwoCollections() {
		ItemChange ItemChange1 = buildItemChange("test 1.1");
		ItemChange ItemChange2 = buildItemChange("test 1.2");
		ItemChange ItemChange21 = buildItemChange("test 2.1");
		
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, ItemChange1);
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, ItemChange2);
		unSynchronizedItemImpl.storeItemToAdd(credentials, 2, ItemChange21);
		
		Set<ItemChange> itemChangesOneCollection = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Set<ItemChange> itemChangesTwoCollection = unSynchronizedItemImpl.listItemToAdd(credentials, 2);
		
		Assert.assertNotNull(itemChangesOneCollection);
		Assert.assertEquals(2, itemChangesOneCollection.size());
		Assert.assertTrue(itemChangesOneCollection.contains(ItemChange1));
		Assert.assertTrue(itemChangesOneCollection.contains(ItemChange2));
		
		Assert.assertNotNull(itemChangesTwoCollection);
		Assert.assertEquals(1, itemChangesTwoCollection.size());
		Assert.assertTrue(itemChangesTwoCollection.contains(ItemChange21));	
	}
	
	@Test
	public void addTwoItemsDifferentTypeOnTheSameCollection() {
		ItemChange ItemChange1 = buildItemChange("test 1");
		ItemChange ItemChange2 = buildItemChange("test 2");
		ItemChange ItemChange3 = buildItemChange("test 3");
		
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, ItemChange1);
		unSynchronizedItemImpl.storeItemToRemove(credentials, 1, ItemChange2);
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(1, itemChanges.size());
		
		Assert.assertTrue(itemChanges.contains(ItemChange1));
		Assert.assertFalse(itemChanges.contains(ItemChange2));
		Assert.assertFalse(itemChanges.contains(ItemChange3));
	}
	
	@Test
	public void clear() {
		unSynchronizedItemImpl.storeItemToAdd(credentials, 1, buildItemChange("test 1"));
		unSynchronizedItemImpl.clearItemToAdd(credentials, 1);		
		
		Set<ItemChange> itemChanges = unSynchronizedItemImpl.listItemToAdd(credentials, 1);
		Assert.assertNotNull(itemChanges);
		Assert.assertEquals(0, itemChanges.size());
	}
	
	private ItemChange buildItemChange(String displayName) {
		ItemChange itemChange = new ItemChange();
		itemChange.setDisplayName(displayName);
		return itemChange;
	}
	
}
