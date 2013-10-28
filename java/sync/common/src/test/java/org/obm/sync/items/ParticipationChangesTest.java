package org.obm.sync.items;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;

import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class ParticipationChangesTest {

	private static final String EXTID = "123";
	private static final int OBMID = 123;
	private static final String RECURRENCEID = "recurrenceId";
	private Iterable<Attendee> attendees;
	
	@Before
	public void setUp() {
		Attendee attendee1 = ContactAttendee.builder().email("test@obm.org").build();
		Attendee attendee2 = ContactAttendee.builder().email("test2@obm.org").build();
		attendees = Sets.newHashSet(attendee1, attendee2);
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildNull() { 
		ParticipationChanges.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void BuilderWithNullEventObmId() {
		ParticipationChanges.builder().eventExtId(EXTID).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void BuilderWithNullEventExtId() {
		ParticipationChanges.builder().eventObmId(OBMID).build();
	}
	
	@Test
	public void BuilderParticipationChangesWithoutRecurrenceIdOrAttendees() {
		ParticipationChanges participationChanges =
				ParticipationChanges.builder().eventObmId(OBMID).eventExtId(EXTID).build();
		
		assertThat(participationChanges.getEventId().getObmId()).isEqualTo(OBMID);
		assertThat(participationChanges.getEventExtId().getExtId()).isEqualTo(EXTID);
	}
	
	@Test
	public void BuilderParticipationChangesWithoutRecurrenceId() {
		ParticipationChanges participationChanges =
				ParticipationChanges.builder()
					.eventObmId(OBMID)
					.eventExtId(EXTID)
					.attendees(attendees)
					.build();
		
		assertThat(participationChanges.getEventId().getObmId()).isEqualTo(OBMID);
		assertThat(participationChanges.getEventExtId().getExtId()).isEqualTo(EXTID);
		assertThat(participationChanges.getAttendees()).isEqualTo(attendees);
	}
	
	@Test
	public void BuilderParticipationChangesWithoutAttendees() {
		ParticipationChanges participationChanges =
				ParticipationChanges.builder()
					.eventObmId(OBMID)
					.eventExtId(EXTID)
					.attendees(attendees)
					.build();
		
		assertThat(participationChanges.getEventId().getObmId()).isEqualTo(OBMID);
		assertThat(participationChanges.getEventExtId().getExtId()).isEqualTo(EXTID);
		assertThat(participationChanges.getAttendees()).isEqualTo(attendees);
	}
	
	@Test
	public void BuilderParticipationChangesWithAllFields() {
		ParticipationChanges participationChanges =
				ParticipationChanges.builder()
					.eventObmId(OBMID)
					.eventExtId(EXTID)
					.recurrenceId(RECURRENCEID)
					.attendees(attendees)
					.build();
		
		assertThat(participationChanges.getEventId().getObmId()).isEqualTo(OBMID);
		assertThat(participationChanges.getEventExtId().getExtId()).isEqualTo(EXTID);
		assertThat(participationChanges.getRecurrenceId().getRecurrenceId()).isEqualTo(RECURRENCEID);
		assertThat(participationChanges.getAttendees()).isEqualTo(attendees);
	}
	
	@Test(expected=NullPointerException.class)
	public void BuilderParticipationChangesWithNullAttendees() {
		ParticipationChanges.builder()
			.eventObmId(OBMID)
			.eventExtId(EXTID)
			.recurrenceId(RECURRENCEID)
			.attendees(null)
			.build();
	}
}
