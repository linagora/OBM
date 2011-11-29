package org.obm.sync.calendar;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EventRecurrenceTest {
	
	@Test
	public void testGetEventExceptionWithAttributeChangesWithoutChanges() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e2));
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertTrue(list.isEmpty());
	}
	
	@Test
	public void testGetEventExceptionWithAttributeChangesWithoutEventChanges() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		Event e3 = e2.clone();
		e3.setTitle("Modif"+e3.getTitle());
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e3));
		
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertEquals(1, list.size());
	}
	
	@Test
	public void testGetEventExceptionWithAttributeChangesWithNewEvent() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		EventRecurrence rec2 = rec1.clone();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		Event e3 =createEventException(3, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		rec2.setEventExceptions(Lists.newArrayList(e1, e2, e3));
		
		
		List<Event> list = rec2.getEventExceptionWithChangesExceptedOnException(rec1);
		Assert.assertEquals(1, list.size());
	}
	
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithoutExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(new Date());
		Assert.assertNull(exception);
	}
	
	@Test
	public void testGetEventExceptionWithRecurrenceIdWithExistedEventException() {
		EventRecurrence rec1 = getOneDailyEventRecurence();
		
		Event e1 = createEventException(1, 2);
		Event e2 = createEventException(2, 3);
		
		rec1.setEventExceptions(Lists.newArrayList(e1, e2));
		
		Event exception = rec1.getEventExceptionWithRecurrenceId(e1.getRecurrenceId());
		Assert.assertNotNull(exception);
		Assert.assertEquals(e1, exception);
	}
	
	private EventRecurrence getOneDailyEventRecurence() {
		EventRecurrence rec = new EventRecurrence();
		rec.setKind(RecurrenceKind.daily);
		rec.setFrequence(2);
		
		return rec;
	}
	
	private Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal.getTime();
	}

	private Event createEventException(int id, int nbAttendees) {
		Event event = new Event();
		event.setAlert(10);
		event.setAllday(true);
		event.setAttendees( createAttendees(nbAttendees) );
		event.setCategory("category");
		event.setCompletion(new Date());
		event.setDescription("description");
		event.setDomain("domain");
		event.setDuration(10);
		event.setEntityId(1);
		event.setExtId(new EventExtId("1"));
		event.setInternalEvent(true);
		event.setLocation("location");
		event.setOpacity(EventOpacity.OPAQUE);
		event.setOwner("owner");
		event.setOwnerDisplayName("owner displayname");
		event.setOwnerEmail("owner email");
		event.setPercent(1);
		event.setPriority(1);
		event.setPrivacy(1);
		event.setSequence(0);
		event.setTimeCreate(new Date());
		event.setTimeUpdate(new Date());
		event.setTimezoneName("timeZone");
		event.setTitle("title"+id);
		event.setType(EventType.VEVENT);
		event.setUid(new EventObmId(id));
		
		Date d1 = getDate(2011, 12, 20);
		event.setRecurrenceId(d1);
		event.setDate(d1);
		return event;
	}
	
	private List<Attendee> createAttendees(int count) {
		List<Attendee> attendees = new ArrayList<Attendee>();
		for (int i = 0; i < count; i++) {
			Attendee attendee = new Attendee();
			attendee.setCanWriteOnCalendar(false);
			attendee.setDisplayName("DisplayName" + i);
			attendee.setEmail("email" + i + "@email.com");
			attendee.setObmUser(true);
			attendee.setOrganizer(isOrganizer(i));
			attendee.setPercent(1);
			attendee.setRequired(ParticipationRole.REQ);
			attendee.setState(ParticipationState.NEEDSACTION);
		}
		return attendees;
	}
	
	private boolean isOrganizer(int id) {
		return id == 0 ? true : false;
	}
}