package org.obm.push.bean;

import org.junit.Assert;
import org.junit.Test;
import org.obm.push.exception.activesync.InvalidServerId;

public class ServerIdTest {

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testEmptyString() throws InvalidServerId {
		new ServerId("");
	}

	@SuppressWarnings("unused")
	@Test(expected=NullPointerException.class)
	public void testNullString() throws InvalidServerId {
		new ServerId(null);
	}

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntCollectionIdString() throws InvalidServerId {
		new ServerId("azd");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntCollectionIdString2() throws InvalidServerId {
		new ServerId("azd:123");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooLargeIntCollectionIdString() throws InvalidServerId {
		new ServerId("123456789123456");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testNonIntItemIdString() throws InvalidServerId {
		new ServerId("123:abc");
	}

	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooLargeIntItemIdString() throws InvalidServerId {
		new ServerId("123:123456789123456");
	}

	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testTooManyPartsString() throws InvalidServerId {
		new ServerId("123:123:123");
	}
	
	@SuppressWarnings("unused")
	@Test(expected=InvalidServerId.class)
	public void testWeirdString() throws InvalidServerId {
		new ServerId(":123:123");
	}
	
	@Test
	public void testSimpleCollectionIdString() throws InvalidServerId {
		ServerId serverId = new ServerId("123");
		Assert.assertEquals(123, serverId.getCollectionId());
		Assert.assertNull(serverId.getItemId());
	}
	
	@Test
	public void testSimpleServerIdString() throws InvalidServerId {
		ServerId serverId = new ServerId("123:345");
		Assert.assertEquals(123, serverId.getCollectionId());
		Assert.assertEquals(Integer.valueOf(345), serverId.getItemId());
	}
	
	@Test
	public void testSimpleEquals() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123:345");
		ServerId serverId2 = new ServerId("123:345");
		Assert.assertTrue(serverId1.equals(serverId2));
		Assert.assertTrue(serverId2.equals(serverId1));
		Assert.assertEquals(serverId1.hashCode(), serverId2.hashCode());
	}
	
	@Test
	public void testNotEquals() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123:456");
		ServerId serverId2 = new ServerId("123:345");
		Assert.assertFalse(serverId1.equals(serverId2));
		Assert.assertFalse(serverId2.equals(serverId1));
		Assert.assertFalse(serverId1.hashCode() == serverId2.hashCode());
	}
	
	@Test
	public void testNotEquals2() throws InvalidServerId {
		ServerId serverId1 = new ServerId("123");
		ServerId serverId2 = new ServerId("123:345");
		Assert.assertFalse(serverId1.equals(serverId2));
		Assert.assertFalse(serverId2.equals(serverId1));
		Assert.assertFalse(serverId1.hashCode() == serverId2.hashCode());
	}
}
