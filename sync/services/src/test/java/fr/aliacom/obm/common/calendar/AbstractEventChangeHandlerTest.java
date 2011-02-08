package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.after;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.before;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendee;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.server.mailer.EventChangeMailer;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;


public abstract class AbstractEventChangeHandlerTest {

	public AbstractEventChangeHandlerTest() {
		super();
	}
	
	protected AccessToken getMockAccessToken(){
		AccessToken at = new AccessToken(1, 1, "unitTest");
		at.setDomain("test.tlse.lng");
		return at;
	}
	
	protected abstract void processEvent(EventChangeHandler eventChangeHandler, Event event) throws NotificationException;
	
	public void testDefaultEvent() {
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		Event event = new Event();
		event.setDate(after());
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testNoAttendee() {
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		Event event = new Event();
		event.setDate(after());
		event.setAttendees(new ArrayList<Attendee>());
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testOnlyOwnerIsAttendee() {
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@domain.net";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail));
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testEventInThePast() {
		Event event = new Event();
		event.setDate(before());
		event.setOwnerEmail("user@test");
		event.addAttendee(createRequiredAttendee("attendee1@test"));
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testOneAttendee() {
		Attendee attendeeOne = createRequiredAttendee("attendee1@test");
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail));
		event.addAttendee(attendeeOne);
		
		EventChangeMailer mailer = expectationOneAttendee(attendeeOne);
		
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationOneAttendee(Attendee attendee);

	public void testSameAttendeeTwice() {
		Attendee attendeeOne = createRequiredAttendee("attendee1@test");
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail));
		event.addAttendee(attendeeOne);
		event.addAttendee(attendeeOne);
		
		EventChangeMailer mailer = expectationSameAttendeeTwice(attendeeOne);
		
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationSameAttendeeTwice(Attendee attendee);

	private List<Attendee> createRequiredAttendees(String prefix, String suffix, int number) {
		ArrayList<Attendee> result = new ArrayList<Attendee>();
		for (int i = 0; i < number; ++i) {
			result.add(createRequiredAttendee(prefix + i + suffix));
		}
		return result;
	}
	
	public void testManyAttendees() {
		int numberOfAttendees = 10;
		List<Attendee> attendees = createRequiredAttendees("attendee", "@test", numberOfAttendees);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail));
		event.setAttendees(attendees);
		
		EventChangeMailer mailer = expectationManyAttendee(attendees);
		
		EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationManyAttendee(List<Attendee> atteendees);
	
}