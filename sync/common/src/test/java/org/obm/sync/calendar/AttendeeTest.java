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
	
	@Test
	public void testEqualCase() {
		Attendee att1 = new Attendee();
		att1.setEmail("test@test.tlse.lng");
		Attendee att2 = new Attendee();
		att2.setEmail("TeSt@tEsT.TlsE.lNg");
		
		Assert.assertTrue(att1.equals(att2));
		Assert.assertTrue(att2.equals(att1));
	}

        @Test
        public void testEqualAttendeeAlert() {
                Attendee att1 = new Attendee();
                att1.setEmail("test@test.tlse.lng");
                Attendee att2 = new AttendeeAlert();
                att2.setEmail("test@test.tlse.lng");

                Assert.assertTrue(att1.equals(att2));
        }


	
	@Test
	public void testHashCodeCase() {
		Attendee att1 = new Attendee();
		att1.setEmail("test@test.tlse.lng");
		Attendee att2 = new Attendee();
		att2.setEmail("TeSt@tEsT.TlsE.lNg");
		
		Assert.assertEquals(att1.hashCode(), att2.hashCode());
	}
	
	public void testHashCode(){
		Attendee att1 = new Attendee();
		att1.setDisplayName("test");
		att1.setEmail("test@obm.lng.org");
		att1.setObmUser(true);
		att1.setOrganizer(false);
		att1.setPercent(0);
		att1.setState(ParticipationState.ACCEPTED);
		
		Attendee att2 = new Attendee();
		att1.setDisplayName("test2");
		att1.setEmail("test@obm.lng.org");
		att1.setObmUser(false);
		att1.setOrganizer(true);
		att1.setPercent(1);
		att1.setState(ParticipationState.NEEDSACTION);
		
		Assert.assertEquals(att1.hashCode(), att2.hashCode());
	}
	
}
