package fr.aliacom.obm.common.calendar;

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

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;

import com.google.common.collect.ImmutableList;

@RunWith(Suite.class)
@SuiteClasses({EventChangeHandlerTest.UpdateTests.class, EventChangeHandlerTest.CreateTests.class, EventChangeHandlerTest.DeleteTests.class})
public class EventChangeHandlerTest {
	
	private static final Locale LOCALE = Locale.FRENCH;
	
	public static class CreateTests extends AbstractEventChangeHandlerTest {
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event) {
			eventChangeHandler.create(getMockAccessToken(), event, LOCALE);
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
		public void testOneAttendee() {
			super.testOneAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationOneAttendee(Attendee attendee) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNewUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE));
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
			mailer.notifyNewUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE));
			EasyMock.expectLastCall().once();
			EasyMock.replay(mailer);
			return mailer;
		}

		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> atteendees) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNewUsers(anyObject(AccessToken.class), compareCollections(atteendees), anyObject(Event.class), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			return mailer;

		}
		
	}
	
	public static class DeleteTests extends AbstractEventChangeHandlerTest {
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event) {
			eventChangeHandler.delete(getMockAccessToken(), event, LOCALE);
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
		public void testOneAttendee() {
			super.testOneAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationOneAttendee( Attendee attendee) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE));
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
			mailer.notifyRemovedUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> atteendees) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyRemovedUsers(anyObject(AccessToken.class), compareCollections(atteendees), anyObject(Event.class), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
	}

	public static class UpdateTests {
		
		private AccessToken getMockAccessToken(){
			AccessToken at = new AccessToken(1, 1, "unitTest");
			at.setDomain("test.tlse.lng");
			return at;
		}
		
		@Test
		public void testDefaultEventNoChange() {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			Event event = new Event();
			event.setDate(after());
			EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
			eventChangeHandler.update(getMockAccessToken(),event, event, LOCALE);
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
			EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
			eventChangeHandler.update(getMockAccessToken(),event, eventAfter, LOCALE);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventDateChangeOneUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test");
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);

			mailer.notifyUpdateUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
			eventChangeHandler.update(getMockAccessToken(), previousEvent, currentEvent, LOCALE);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventNoChangeOneUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test");
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			mailer.notifyUpdateUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(previousEvent), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
			eventChangeHandler.update(getMockAccessToken(), previousEvent, previousEvent, LOCALE);
			verify(mailer);
		}
		
		@Test
		public void testDefaultEventAddOneUser() {
			Attendee attendee = createRequiredAttendee("attendee1@test");
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test");
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			
			mailer.notifyNewUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE));
			expectLastCall().once();
			mailer.notifyUpdateUsers(anyObject(AccessToken.class), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent) , eq(currentEvent), eq(LOCALE));
			expectLastCall().once();
			replay(mailer);
			
			EventChangeHandler eventChangeHandler = new EventChangeHandler(mailer);
			eventChangeHandler.update(getMockAccessToken(), previousEvent, currentEvent, LOCALE);
			verify(mailer);
		}
	}
}
