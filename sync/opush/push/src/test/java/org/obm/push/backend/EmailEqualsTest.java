package org.obm.push.backend;

import org.junit.Test;
import org.obm.push.store.Email;

import junit.framework.Assert;

public class EmailEqualsTest {
	
	@Test
	public void testEquals(){
		Email e1 = new Email(10, true);
		Email e2 = new Email(10, true);
		Assert.assertEquals(e1, e2);
	}
	
	@Test
	public void testNotEqualsUid(){
		Email e1 = new Email(10, true);
		Email e2 = new Email(11, true);
		Assert.assertNotSame(e1, e2);
	}
	
	@Test
	public void testNotEqualsRead(){
		Email e1 = new Email(11, false);
		Email e2 = new Email(11, true);
		Assert.assertNotSame(e1, e2);
	}
	
}
