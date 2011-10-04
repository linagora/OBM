package org.obm.push.tnefconverter.test;

import java.io.InputStream;

import junit.framework.TestCase;
import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;

import org.obm.push.tnefconverter.ScheduleMeeting.ScheduleMeeting;

public class TnefTest extends TestCase {

	public void testExtract() {
		InputStream in = loadDataFile("excptRecur.tnef");
		assertNotNull(in);
		try {
			TNEFInputStream tnef = new TNEFInputStream(in);
			Message tnefMsg = new Message(tnef);
			ScheduleMeeting ics = new ScheduleMeeting(tnefMsg);
			assertNotNull(ics);
			assertNotNull(ics.getMethod());
			assertNotNull(ics.getUID());
			assertNotNull(ics.getStartDate());
			assertNotNull(ics.getEndDate());
			assertNotNull(ics.getResponseRequested());
			assertNotNull(ics.getDescription());
			assertNotNull(ics.getClazz());
			assertNotNull(ics.getLocation());
			assertNotNull(ics.isAllDay());
			assertNotNull(ics.isRecurring());
			assertNotNull(ics.getOldRecurrenceType());
			assertNotNull(ics.getInterval());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testExtract2() {
		InputStream in = loadDataFile("acpInv.tnef");
		assertNotNull(in);
		try {
			TNEFInputStream tnef = new TNEFInputStream(in);
			Message tnefMsg = new Message(tnef);
			ScheduleMeeting ics = new ScheduleMeeting(tnefMsg);
			assertNotNull(ics);
			assertNotNull(ics.getMethod());
			assertNotNull(ics.getUID());
			assertNotNull(ics.getStartDate());
			assertNotNull(ics.getEndDate());
			assertNotNull(ics.getResponseRequested());
			assertNotNull(ics.getDescription());
			assertNotNull(ics.getClazz());
			assertNotNull(ics.getLocation());
			assertNotNull(ics.isAllDay());
			assertNotNull(ics.isRecurring());
			assertNotNull(ics.getOldRecurrenceType());
			assertNotNull(ics.getInterval());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/tnef/" + name);
	}
	
}
