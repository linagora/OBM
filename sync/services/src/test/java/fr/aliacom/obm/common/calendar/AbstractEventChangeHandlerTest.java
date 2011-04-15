package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.after;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.before;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendee;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;
import org.obm.sync.server.mailer.EventChangeMailer;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.UserService;


public abstract class AbstractEventChangeHandlerTest {

	public AbstractEventChangeHandlerTest() {
		super();
	}


	private EventChangeHandler newEventChangeHandler(EventChangeMailer mailer) {
		UserService userService = EasyMock.createMock(UserService.class);
		SettingsService defaultSettingsService = getDefaultSettingsService();
		EasyMock.replay(userService, defaultSettingsService);
		return new EventChangeHandler(mailer, defaultSettingsService, userService);
	}
	
	protected abstract void processEvent(EventChangeHandler eventChangeHandler, Event event) throws NotificationException;
	
	public void testDefaultEvent() {
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		Event event = new Event();
		event.setDate(after());
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testNoAttendee() {
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		Event event = new Event();
		event.setDate(after());
		event.setAttendees(new ArrayList<Attendee>());
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
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
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	public void testEventInThePast() {
		Event event = new Event();
		event.setDate(before());
		event.setOwnerEmail("user@test");
		event.addAttendee(createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED));
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}
	
	public void testAcceptedAttendee() {
		Attendee attendeeAccepted = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeAccepted);

		EventChangeMailer mailer = expectationAcceptedAttendees(attendeeAccepted);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationAcceptedAttendees(Attendee attendeeAccepted);
	
	public void testNeedActionAttendee() {
		Attendee attendeeNeedAction = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeNeedAction);

		EventChangeMailer mailer = expectationNeedActionAttendees(attendeeNeedAction);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationNeedActionAttendees(Attendee attendeeNeedAction);
	
	public void testDeclinedAttendee() {
		Attendee attendeeDeclined = createRequiredAttendee("attendee1@test", ParticipationState.DECLINED);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeDeclined);

		EventChangeMailer mailer = expectationDeclinedAttendees(attendeeDeclined);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationDeclinedAttendees(Attendee attendeeDeclined);

	public void testTwoAttendee() {
		Attendee attendeeAccepted = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
		
		Attendee attendeeNotAccepted = createRequiredAttendee("attendee2@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeAccepted);
		event.addAttendee(attendeeNotAccepted);
		
		EventChangeMailer mailer = expectationTwoAttendees(attendeeAccepted, attendeeNotAccepted);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationTwoAttendees(Attendee attendeeAccepted, Attendee attendeeNotAccepted);

	public void testSameAttendeeTwice() {
		Attendee attendeeOne = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeOne);
		event.addAttendee(attendeeOne);
		
		EventChangeMailer mailer = expectationSameAttendeeTwice(attendeeOne);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationSameAttendeeTwice(Attendee attendee);

	private List<Attendee> createRequiredAttendees(String prefix, String suffix, ParticipationState state, int start, int number) {
		ArrayList<Attendee> result = new ArrayList<Attendee>();
		for (int i = 0; i < number; ++i) {
			result.add(createRequiredAttendee(prefix + (start + i )+ suffix,state));
		}
		return result;
	}
	
	public void testManyAttendees() {
		List<Attendee> accpetedAttendees = createRequiredAttendees("attendee", "@test", ParticipationState.ACCEPTED,0,  5);
		List<Attendee> needActionAttendees = createRequiredAttendees("attendee", "@test", ParticipationState.NEEDSACTION, 5, 5);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendees(needActionAttendees);
		event.addAttendees(accpetedAttendees);
		
		EventChangeMailer mailer = expectationManyAttendee(needActionAttendees, accpetedAttendees);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees);
	
}