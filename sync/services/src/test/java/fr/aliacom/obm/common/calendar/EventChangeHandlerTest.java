package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.ToolBox.getDefaultObmDomain;
import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
import static fr.aliacom.obm.ToolBox.getDefaultSettings;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.after;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.compareCollections;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendee;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.longAfter;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.EventChangeMailer;

import com.google.common.collect.ImmutableList;
import com.google.inject.internal.Lists;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;

@RunWith(Suite.class)
@SuiteClasses({
	EventChangeHandlerTest.UpdateTests.class, 
	EventChangeHandlerTest.CreateTests.class, 
	EventChangeHandlerTest.DeleteTests.class, 
	EventChangeHandlerTest.UpdateParticipationTests.class})
public class EventChangeHandlerTest {
	
	private static final Locale LOCALE = Locale.FRENCH;
	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	private static EventChangeHandler newEventChangeHandler(
			EventChangeMailer mailer, SettingsService settingsService, UserService userService) {
		
		return new EventChangeHandler(mailer, settingsService, userService);
	}
	
	private static EventChangeHandler newEventChangeHandler(EventChangeMailer mailer) {
		UserService userService = EasyMock.createMock(UserService.class);
		SettingsService settingsService = getDefaultSettingsService();
		EasyMock.replay(userService, settingsService);
		return newEventChangeHandler(mailer, settingsService, userService);
	}
	
	public static class CreateTests extends AbstractEventChangeHandlerTest {
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event) {
			eventChangeHandler.create(getDefaultObmUser(), event);
		}
		
		@Test
		public void testDefaultEvent() {
			super.testDefaultEvent();
		}
		
		@Test
		public void testNoAttendee() {
			super.testNoAttendee();
		}
		
		@Test
		public void testOnlyOwnerIsAttendee() {
			super.testOnlyOwnerIsAttendee();
		}
		
		@Test
		public void testEventInThePast() {
			super.testEventInThePast();
		}
		
		@Test
		public void testAcceptedAttendee(){
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee(){
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee(){
			super.testDeclinedAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(
				Attendee attendeeAccepted) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers( compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendeeNeedAction)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(
				Attendee attendeeDeclined) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees(Attendee attendeeAccepted, Attendee attendeeNotAccepted) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers( compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendeeNotAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			EasyMock.expectLastCall().once();
			EasyMock.replay(mailer);
			return mailer;
		}

		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(compareCollections(accpetedAttendees), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(anyObject(ObmUser.class), compareCollections(needActionAttendees), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;

		}
	}
	
	public static class DeleteTests extends AbstractEventChangeHandlerTest {
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event) {
			eventChangeHandler.delete(getDefaultObmUser(), event);
		}
		
		@Test
		public void testDefaultEvent() {
			super.testDefaultEvent();
		}
		
		@Test
		public void testNoAttendee() {
			super.testNoAttendee();
		}
		
		@Test
		public void testOnlyOwnerIsAttendee() {
			super.testOnlyOwnerIsAttendee();
		}
		
		@Test
		public void testEventInThePast() {
			super.testEventInThePast();
		}
		
		@Test
		public void testAcceptedAttendee(){
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee(){
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee(){
			super.testDeclinedAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(
				Attendee attendeeAccepted) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendeeNeedAction)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(
				Attendee attendeeDeclined) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees( Attendee attendeeAccepted, Attendee attendeeNotAccepted) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendeeNotAccepted, attendeeAccepted )), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			List<Attendee> atts = Lists.newArrayList();
			atts.addAll(needActionAttendees);
			atts.addAll(accpetedAttendees);
			mailer.notifyRemovedUsers(anyObject(ObmUser.class), compareCollections(atts), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
	}

	public static class UpdateTests {

		@Test
		public void testDefaultEventNoChange() {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			Event event = new Event();
			event.setDate(after());
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), event, event);
			verify(mailer);
		}

		@Test
		public void testDefaultEventDateChangeZeroUser() {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			Event event = new Event();
			event.setDate(after());
			Event eventAfter = new Event();
			eventAfter.setDate(longAfter());
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(),event, eventAfter);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventDateChangeOneNeedActionUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);

			mailer.notifyNeedActionUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, currentEvent);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventDateChangeOneAcceptedUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);

			mailer.notifyAcceptedUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, currentEvent);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventNoChangeOneNeedActionUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			mailer.notifyNeedActionUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(previousEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, previousEvent);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventNoChangeOneAcceptedUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			mailer.notifyAcceptedUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(previousEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, previousEvent);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventAddOneNeedActionUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.NEEDSACTION);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			
			mailer.notifyNeedActionNewUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyNeedActionUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, currentEvent);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventAddOneAcceptedUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.ACCEPTED);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			
			mailer.notifyAcceptedNewUsers(compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyAcceptedUpdateUsers(anyObject(ObmUser.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
			eventChangeHandler.update(getDefaultObmUser(), previousEvent, currentEvent);
			verify(mailer);
		}	
	
	}

	public static class UpdateParticipationTests {
		
		@Test
		public void testParticipationChangeWithOwnerExpectingEmails() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee organizer = createRequiredAttendee("organizer@test", ParticipationState.ACCEPTED);
			organizer.setOrganizer(true);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event event = new Event();
			event.setDate(after());
			event.addAttendee(attendee);
			event.addAttendee(organizer);
			
			ObmUser attendeeUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			ObmUser organizerUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			
			UserService userService = EasyMock.createMock(UserService.class);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(true).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			settingsService.getSettings(eq(attendeeUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			EasyMock.replay(userService, settingsService, settings, mailer);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED);
			verify(userService, settingsService, settings, mailer);
		}

		@Test
		public void testParticipationChangeWithOwnerNotExpectingEmails() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee organizer = createRequiredAttendee("organizer@test", ParticipationState.ACCEPTED);
			organizer.setOrganizer(true);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event event = new Event();
			event.setDate(after());
			event.addAttendee(attendee);
			event.addAttendee(organizer);
			
			ObmUser attendeeUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			ObmUser organizerUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			
			UserService userService = EasyMock.createMock(UserService.class);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(false).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			EasyMock.replay(userService, settingsService, settings, mailer);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED);
			verify(userService, settingsService, settings, mailer);
		}
	}
}
