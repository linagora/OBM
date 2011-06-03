package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.ToolBox.getDefaultObmDomain;
import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static fr.aliacom.obm.ToolBox.getDefaultProducer;
import static fr.aliacom.obm.ToolBox.getDefaultSettings;
import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
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

import javax.jms.JMSException;

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
import com.google.common.collect.Lists;
import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;
import fr.aliacom.obm.utils.Ical4jHelper;

@RunWith(Suite.class)
@SuiteClasses({
	EventChangeHandlerTest.UpdateTests.class, 
	EventChangeHandlerTest.CreateTests.class, 
	EventChangeHandlerTest.DeleteTests.class, 
	EventChangeHandlerTest.UpdateParticipationTests.class})
public class EventChangeHandlerTest {
	
	private static final String ICS_DATA_ADD = "ics data add attendee";
	private static final String ICS_DATA_REMOVE = "ics data remove attendee";
	private static final String ICS_DATA_UPDATE = "ics data update attendee";
	private static final String ICS_DATA_REPLY = "ics data reply attendee";
	
	private static final Locale LOCALE = Locale.FRENCH;
	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	private static EventChangeHandler newEventChangeHandler(
			EventChangeMailer mailer, SettingsService settingsService, UserService userService,
			Producer producer, Ical4jHelper ical4jHelper) {
		
		return new EventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
	}
	
	private static EventChangeHandler updateEventChangeHandler(EventChangeMailer mailer, Ical4jHelper ical4jHelper) throws JMSException  {
		UserService userService = EasyMock.createMock(UserService.class);
		SettingsService settingsService = getDefaultSettingsService();
		Producer producer = getDefaultProducer();
		producer.write(ICS_DATA_ADD);
		producer.write(ICS_DATA_REMOVE);
		producer.write(ICS_DATA_UPDATE);
		EasyMock.replay(userService, settingsService);
		return newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
	}
	
	public static class CreateTests extends AbstractEventChangeHandlerTest {
		
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event, ObmUser obmUser) {
			eventChangeHandler.create(obmUser, event, true);
		}
		
		@Override
		public void testDefaultEvent() {
			super.testDefaultEvent();
		}
		
		@Override
		public void testEventInThePast() {
			super.testEventInThePast();
		}
		
		@Override
		public void testNoAttendee() {
			super.testNoAttendee();
		}
		
		@Override
		public void testOnlyOwnerIsAttendee() {
			super.testOnlyOwnerIsAttendee();
		}
		
		@Test
		public void testAcceptedAttendee() {
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee() {
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee() {
			super.testDeclinedAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(
				Attendee attendeeAccepted, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers( compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction, String icsData, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			mailer.notifyNeedActionNewUsers(compareCollections(ImmutableList.of(attendeeNeedAction)), 
					anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(
				Attendee attendeeDeclined, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees(Attendee attendeeAccepted, Attendee attendeeNotAccepted, 
				Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			mailer.notifyAcceptedNewUsers( compareCollections(ImmutableList.of(attendeeAccepted)), eq(event), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(compareCollections(ImmutableList.of(attendeeNotAccepted)), 
					eq(event), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee, Event event, ObmUser obmUser) {
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(compareCollections(ImmutableList.of(attendee)), 
					anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			EasyMock.expectLastCall().once();
			EasyMock.replay(mailer);
			return mailer;
		}

		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees,
				Event event, ObmUser obmUser) {
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(compareCollections(accpetedAttendees), eq(event), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(compareCollections(needActionAttendees), 
					eq(event), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;

		}
	}
	
	public static class DeleteTests extends AbstractEventChangeHandlerTest {
		
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event, ObmUser obmUser) {
			eventChangeHandler.delete(obmUser, event, true);
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
		public void testAcceptedAttendee() {
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee() {
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee() {
			super.testDeclinedAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(Attendee attendeeAccepted, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction, String icsData, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(compareCollections(ImmutableList.of(attendeeNeedAction)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(Attendee attendeeDeclined, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees( Attendee attendeeAccepted, Attendee attendeeNotAccepted, Event event,
				ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(compareCollections(ImmutableList.of(attendeeNotAccepted, attendeeAccepted )), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees,
				Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			List<Attendee> atts = Lists.newArrayList();
			atts.addAll(needActionAttendees);
			atts.addAll(accpetedAttendees);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(compareCollections(atts), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
	}

	public static class UpdateTests {

		@Test
		public void testDefaultEventNoChange() throws JMSException {
			Event event = new Event();
			event.setDate(after());
			ObmUser defaultObmUser = getDefaultObmUser();
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, event)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, event)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, event)).andReturn(ICS_DATA_UPDATE);
			replay(mailer, ical4jHelper);
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, event, event, true);
			verify(mailer, ical4jHelper);
		}

		@Test
		public void testDefaultEventDateChangeZeroUser() throws JMSException {
			Event event = new Event();
			event.setDate(after());
			Event eventAfter = new Event();
			eventAfter.setDate(longAfter());
			ObmUser defaultObmUser = getDefaultObmUser();
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, eventAfter)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, eventAfter)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, eventAfter)).andReturn(ICS_DATA_UPDATE);
			replay(mailer, ical4jHelper);
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			
			eventChangeHandler.update(defaultObmUser, event, eventAfter, true);
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();

			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(previousEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, previousEvent, true);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
		
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(previousEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, previousEvent, true);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.NEEDSACTION);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			ObmUser defaultObmUser = getDefaultObmUser();

			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(compareCollections(ImmutableList.of(addedAttendee)), 
					eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_ADD));
			expectLastCall().once();
			mailer.notifyNeedActionUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			expectLastCall().once();
			
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.ACCEPTED);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE));
			expectLastCall().once();
			mailer.notifyAcceptedUpdateUsers(compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE));
			expectLastCall().once();
			
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true);
			
			verify(mailer, ical4jHelper);
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
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationReply(event, attendeeUser)).andReturn(ICS_DATA_REPLY);
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_REPLY));
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
			Producer producer = getDefaultProducer();
			
			EasyMock.replay(userService, settingsService, settings, mailer, ical4jHelper);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED, true);
			
			verify(userService, settingsService, settings, mailer, ical4jHelper);
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
			
			Producer producer = getDefaultProducer();
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.replay(userService, settingsService, settings, mailer);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED, true);
			verify(userService, settingsService, settings, mailer);
		}
	}
}
