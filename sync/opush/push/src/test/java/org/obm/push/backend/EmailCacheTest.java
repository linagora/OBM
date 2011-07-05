package org.obm.push.backend;

import org.junit.Test;
import org.obm.push.store.EmailCache;

import junit.framework.Assert;

public class EmailCacheTest {
	
	@Test
	public void testEquals(){
		EmailCache e1 = new EmailCache(10, true);
		EmailCache e2 = new EmailCache(10, true);
		Assert.assertEquals(e1, e2);
	}
	
	@Test
	public void testNotEqualsUid(){
		EmailCache e1 = new EmailCache(10, true);
		EmailCache e2 = new EmailCache(11, true);
		Assert.assertNotSame(e1, e2);
	}
	
	@Test
	public void testNotEqualsRead(){
		EmailCache e1 = new EmailCache(11, false);
		EmailCache e2 = new EmailCache(11, true);
		Assert.assertNotSame(e1, e2);
	}
	
}
