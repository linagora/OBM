package org.obm.push.backend;

import java.util.Date;

import org.junit.Test;
import org.obm.push.bean.Email;

import junit.framework.Assert;

public class EmailEqualsTest {
	
	@Test
	public void testEquals(){
		Email e1 = new Email(10, true, new Date());
		Email e2 = new Email(10, true, new Date());
		Assert.assertEquals(e1, e2);
	}
	
	@Test
	public void testNotEqualsUid(){
		Email e1 = new Email(10, true, new Date());
		Email e2 = new Email(11, true, new Date());
		Assert.assertNotSame(e1, e2);
	}
	
	@Test
	public void testNotEqualsRead(){
		Email e1 = new Email(11, false, new Date());
		Email e2 = new Email(11, true, new Date());
		Assert.assertNotSame(e1, e2);
	}
	
}
