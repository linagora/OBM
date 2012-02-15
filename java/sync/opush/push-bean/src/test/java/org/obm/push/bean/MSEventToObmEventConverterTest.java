package org.obm.push.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.MSEventToObmEventConverter;
import org.obm.push.exception.IllegalMSEventStateException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public class MSEventToObmEventConverterTest {

	private MSEventToObmEventConverter converter;

	private BackendSession bs;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverter();
		String mailbox = "user@domain";
		String password = "password";
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}

	@Ignore("We should find in OBMEvent the corresponding UID of MSEvent")
	@Test
	public void testConvertAttributeUID() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withUid(new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"))
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeAllDayFalse() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayFalseNeedStartTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeAllDayNullNeedStartTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeBusyStatusFree() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.FREE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.withSubject("Any Subject")
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(new ArrayList<String>())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Lists.newArrayList("category1", "category2"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getCategory()).isNull();
	}
	
	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeCategoryBeyondThreeHundred() throws IllegalMSEventStateException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Arrays.asList(tooMuchCategories))
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttributeDtStampJustCreated() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(msEvent.getDtStamp());
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws IllegalMSEventStateException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.build();
		
		Event convertedEvent = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isEqualTo(previousDtStampDate);
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Assertions.assertThat(convertedEvent.getTimeCreate()).isNull();
		Assertions.assertThat(convertedEvent.getTimeUpdate()).isNull();
	}

	@Test
	public void testConvertAttributeDescription() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription("any description")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isEqualTo(msEvent.getDescription());
	}
	
	@Test
	public void testConvertAttributeDescriptionNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getDescription()).isNull();
	}

	@Test
	public void testConvertAttributeTimezoneSpecific() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(TimeZone.getTimeZone("America/Tijuana"))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isEqualTo("America/Tijuana");
	}
	
	@Test
	public void testConvertAttributeTimezoneNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTimezoneName()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocation() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation("Any location")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getLocation()).isEqualTo(msEvent.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getLocation()).isNull();
	}

	@Test
	public void testConvertAttributeSubject() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeSubjectEmpty() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeSubjectNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject(null)
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeReminder() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(150)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		int reminderInSecond = (int) minuteToSecond(msEvent.getReminder());
		Assertions.assertThat(convertedEvent.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(0)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeSensitivityNormal() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.NORMAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidential() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPersonal() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PERSONAL)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityPrivate() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PRIVATE)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeOrganizerNameOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(msEvent.getOrganizerName());
	}

	@Test
	public void testConvertAttributeOrganizerNameOnlyNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail("email@domain")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnlyNullGetFromSession() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail(null)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isNull();
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(bs.getUser().getEmail());
	}
	
	@Test
	public void testConvertAttributeOrganizerNameAndEmail() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.withOrganizerEmail("user@domain")
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		Assertions.assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(msEvent.getOrganizerName());
		Assertions.assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeStartTimeNullOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=IllegalMSEventStateException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeStartAndEndTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		Assertions.assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
	}

	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test
	public void testConvertAttributeMeetingStatusNull() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(null)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Assertions.assertThat(converted.getMeetingStatus()).isNull();
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		Assertions.assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		Assertions.assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
		Assertions.assertThat(converted.getDuration()).isEqualTo((int) getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Date midnightOfDay = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEvent.getStartTime());
		Assertions.assertThat(converted.getStartDate()).isEqualTo(midnightOfDay);
		Assertions.assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws IllegalMSEventStateException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		Date midnightOfDay = org.obm.push.utils.DateUtils.getMidnightOfDayEarly(msEvent.getStartTime());
		Assertions.assertThat(converted.getStartDate()).isEqualTo(midnightOfDay);
		Assertions.assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}
	
	@Test
	public void testConvertExceptionAttributeDeletedTrue() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventException();
		msEventException.setStartTime(date("2004-12-11T11:15:10Z"));
		msEventException.setEndTime(date("2004-12-12T11:15:10Z"));
		msEventException.setExceptionStartTime(date("2004-10-11T11:15:10Z"));
		msEventException.setDeleted(true);
		msEventException.setSubject("Any Subject");
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).hasSize(1);
		Assertions.assertThat(exceptions).containsOnly(msEventException.getExceptionStartTime());
		Assertions.assertThat(eventExceptions).isEmpty();
	}

	@Test
	public void testConvertExceptionAttributeDeletedFalse() throws IllegalMSEventStateException {
		MSEventException msEventException = new MSEventException();
		msEventException.setStartTime(date("2004-12-11T11:15:10Z"));
		msEventException.setEndTime(date("2004-12-12T11:15:10Z"));
		msEventException.setExceptionStartTime(date("2004-10-11T11:15:10Z"));
		msEventException.setDeleted(false);
		msEventException.setSubject("Any Subject");
		
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withRecurrence(simpleRecurrence(RecurrenceType.DAILY))
				.withExceptions(Lists.newArrayList(msEventException))
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		List<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		Assertions.assertThat(exceptions).isEmpty();
		Assertions.assertThat(eventExceptions).hasSize(1);
		Assertions.assertThat(eventExceptions.get(0).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	private MSRecurrence simpleRecurrence(RecurrenceType type) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		return recurrence;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws IllegalMSEventStateException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}

	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws IllegalMSEventStateException {
		return converter.convert(bs, editingEvent, msEvent, false);
	}

	private Date date(String date) {
		return org.obm.DateUtils.date(date);
	}

	private long getOneDayInSecond() {
		return DateUtils.daysToSeconds(1);
	}

	private long getOneYearInSecond() {
		return DateUtils.yearsToSeconds(1);
	}

	private long minuteToSecond(int minutes) {
		return DateUtils.minutesToSeconds(minutes);
	}
}
