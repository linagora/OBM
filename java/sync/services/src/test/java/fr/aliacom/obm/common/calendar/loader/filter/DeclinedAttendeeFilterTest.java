package fr.aliacom.obm.common.calendar.loader.filter;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class DeclinedAttendeeFilterTest {

	private Attendee kimPhilby(Participation participation) {
		Attendee philby = new Attendee();
		philby.setEmail("kim.philby@mi6.gov.uk");
		philby.setParticipation(participation);
		return philby;
	}

	private Attendee guyBurgess(Participation participation) {
		Attendee burgess = new Attendee();
		burgess.setEmail("guy.burgess@mi6.gov.uk");
		burgess.setParticipation(participation);
		return burgess;
	}

	@Test
	public void testNonRecurrentEvent() {
		Attendee acceptingPhilby = kimPhilby(Participation.ACCEPTED);
		Attendee decliningPhilby = kimPhilby(Participation.DECLINED);
		Attendee burgess = guyBurgess(Participation.ACCEPTED);

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithoutPhilbyId = new EventObmId(2);
		Event evWithoutPhilby = new Event();
		evWithoutPhilby.setUid(evWithoutPhilbyId);
		evWithoutPhilby.addAttendee(decliningPhilby);
		evWithPhilby.addAttendee(burgess);

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>().put(
				evWithPhilbyId, evWithPhilby).build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithoutPhilbyId, evWithoutPhilby).build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}

	@Test
	public void testNoDeclinedEvent() {
		Attendee acceptingPhilby = kimPhilby(Participation.ACCEPTED);
		Attendee burgess = guyBurgess(Participation.ACCEPTED);

		EventObmId evWithPhilby1Id = new EventObmId(1);
		Event evWithPhilby1 = new Event();
		evWithPhilby1.setUid(evWithPhilby1Id);
		evWithPhilby1.addAttendee(acceptingPhilby);
		evWithPhilby1.addAttendee(burgess);

		EventObmId evWithPhilbyId2 = new EventObmId(2);
		Event evWithPhilby2 = new Event();
		evWithPhilby2.setUid(evWithPhilbyId2);
		evWithPhilby2.addAttendee(acceptingPhilby);
		evWithPhilby2.addAttendee(burgess);

		Map<EventObmId, Event> events = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilby1Id, evWithPhilby1).put(evWithPhilbyId2, evWithPhilby2).build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(events)).isEqualTo(events);
	}

	@Test
	public void testRecurrentEvent() {
		Date currentDate = new Date();

		Attendee acceptingPhilby = kimPhilby(Participation.ACCEPTED);
		Attendee decliningPhilby = kimPhilby(Participation.DECLINED);
		Attendee burgess = guyBurgess(Participation.ACCEPTED);

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithSomePhilbyId = new EventObmId(2);
		Event evWithSomePhilby = new Event();
		evWithSomePhilby.setUid(evWithSomePhilbyId);
		evWithSomePhilby.addAttendee(acceptingPhilby);
		evWithSomePhilby.addAttendee(burgess);
		evWithSomePhilby.getRecurrence().setKind(RecurrenceKind.weekly);

		EventObmId evExWithoutPhilbyId = new EventObmId(3);
		Event evExWithoutPhilby = new Event();
		evExWithoutPhilby.setUid(evExWithoutPhilbyId);
		evExWithoutPhilby.addAttendee(decliningPhilby);
		evExWithoutPhilby.addAttendee(burgess);

		EventObmId evExWithPhilbyId = new EventObmId(4);
		Event evExWithPhilby = new Event();
		evExWithPhilby.setUid(evExWithPhilbyId);
		evExWithPhilby.addAttendee(acceptingPhilby);
		evExWithPhilby.addAttendee(burgess);
		evExWithPhilby.setRecurrenceId(currentDate);

		evWithSomePhilby.getRecurrence().setEventExceptions(
				Lists.newArrayList(evExWithoutPhilby, evExWithPhilby));

		Event cloneOfEvWithSomePhilby = evWithSomePhilby.clone();
		cloneOfEvWithSomePhilby.getRecurrence().setEventExceptions(
				Lists.newArrayList(evExWithPhilby));
		cloneOfEvWithSomePhilby.getRecurrence().setExceptions(
				Lists.newArrayList(evExWithoutPhilby.getRecurrenceId()));

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, cloneOfEvWithSomePhilby)
				.build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, evWithSomePhilby)
				.build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}

	@Test
	public void testDeclinedRecurrentEvent() {
		Date currentDate = new Date();

		Attendee acceptingPhilby = kimPhilby(Participation.ACCEPTED);
		Attendee decliningPhilby = kimPhilby(Participation.DECLINED);
		Attendee burgess = guyBurgess(Participation.ACCEPTED);

		EventObmId evWithPhilbyId = new EventObmId(1);
		Event evWithPhilby = new Event();
		evWithPhilby.setUid(evWithPhilbyId);
		evWithPhilby.addAttendee(acceptingPhilby);
		evWithPhilby.addAttendee(burgess);

		EventObmId evWithSomePhilbyId = new EventObmId(2);
		Event evWithSomePhilby = new Event();
		evWithSomePhilby.setUid(evWithSomePhilbyId);
		evWithSomePhilby.addAttendee(decliningPhilby);
		evWithSomePhilby.addAttendee(burgess);
		evWithSomePhilby.getRecurrence().setKind(RecurrenceKind.weekly);

		EventObmId evExWithoutPhilbyId = new EventObmId(3);
		Event evExWithoutPhilby = new Event();
		evExWithoutPhilby.setUid(evExWithoutPhilbyId);
		evExWithoutPhilby.addAttendee(decliningPhilby);
		evExWithoutPhilby.addAttendee(burgess);

		EventObmId evExWithPhilbyId = new EventObmId(4);
		Event evExWithPhilby = new Event();
		evExWithPhilby.setUid(evExWithPhilbyId);
		evExWithPhilby.addAttendee(acceptingPhilby);
		evExWithPhilby.addAttendee(burgess);
		evExWithPhilby.setRecurrenceId(currentDate);

		evWithSomePhilby.getRecurrence().setEventExceptions(
				Lists.newArrayList(evExWithoutPhilby, evExWithPhilby));

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evExWithPhilbyId, evExWithPhilby).build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(evWithPhilbyId, evWithPhilby).put(evWithSomePhilbyId, evWithSomePhilby)
				.build();

		EventFilter filter = new DeclinedAttendeeFilter(acceptingPhilby);
		assertThat(filter.filter(unfilteredEvents)).isEqualTo(expectedEvents);
	}
}
