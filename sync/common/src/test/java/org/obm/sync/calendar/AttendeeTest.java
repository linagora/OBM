package org.obm.sync.calendar;

import org.junit.Assert;
import org.junit.Test;


public class AttendeeTest {

	@Test
	public void testEqualAttendee() {
		Attendee att1 = new Attendee();
		att1.setEmail("test@test.tlse.lng");
		Attendee att2 = new Attendee();
		att2.setEmail("test@test.tlse.lng");
		
		Assert.assertTrue(att1.equals(att2));
	}
	
	@Test
	public void testNotEqual() {
		Attendee att1 = new Attendee();
		att1.setEmail("test1@test.tlse.lng");
		Attendee att2 = new Attendee();
		att2.setEmail("test2@test.tlse.lng");
		
		Assert.assertFalse(att1.equals(att2));
	}
	
	
	@Test
	public void testNotEqualNullEmail() {
		Attendee att1 = new Attendee();
		att1.setEmail(null);
		Attendee att2 = new Attendee();
		att2.setEmail("test@test.tlse.lng");
		
		Assert.assertFalse(att1.equals(att2));
		Assert.assertFalse(att2.equals(att1));
	}
	
}
