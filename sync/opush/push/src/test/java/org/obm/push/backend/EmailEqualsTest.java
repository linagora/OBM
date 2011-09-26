package org.obm.push.backend;

import java.util.Date;

import org.junit.Test;
import org.obm.push.bean.Email;

import junit.framework.Assert;

public class EmailEqualsTest {
	
	@Test
	public void testEquals(){
		Date date = new Date();
		Email e1 = new Email(10, true, date);
		Email e2 = new Email(10, true, date);
		Assert.assertEquals(e1, e2);
	}
	
	@Test
	public void testNotEqualsUid(){
		Date date = new Date();
		Email e1 = new Email(10, true, date);
		Email e2 = new Email(11, true, date);
		Assert.assertNotSame(e1, e2);
	}
	
	@Test
	public void testNotEqualsRead(){
		Date date = new Date();
		Email e1 = new Email(11, false, date);
		Email e2 = new Email(11, true, date);
		Assert.assertNotSame(e1, e2);
	}
	
}
