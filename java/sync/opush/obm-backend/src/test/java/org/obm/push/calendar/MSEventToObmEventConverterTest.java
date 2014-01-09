/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.calendar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventBuilder;
import org.obm.push.bean.MSEventCommon;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.UserAttendee;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;


public class MSEventToObmEventConverterTest {

	private MSEventToObmEventConverterImpl converter;

	private User user;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverterImpl();
		String mailbox = "user@domain";
	    user = User.Factory.create()
				.createUser(mailbox, mailbox, "display name");
	}

	@Ignore("We should find in OBMEvent the corresponding UID of MSEvent")
	@Test
	public void testConvertAttributeUID() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withUid(new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"))
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeAllDayFalse() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(false)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test
	public void testConvertAttributeAllDayTrue() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.isAllday()).isTrue();
	}

	@Test
	public void testConvertAttributeAllDayNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.isAllday()).isFalse();
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayFalseNeedsStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(false)
				.build();

		convertToOBMEvent(msEvent);
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayNullNeedsStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withAllDayEvent(null)
				.build();

		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayTrueNeedsStartTime() throws ConversionException {
	    MSEvent msEvent = new MSEventBuilder()
                .withStartTime(null)
                .withEndTime(date("2004-12-12T11:15:10Z"))
                .withAllDayEvent(true)
                .build();

	    convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeAllDayNullNeedsEndTime() throws ConversionException {
	    MSEvent msEvent = new MSEventBuilder()
                .withStartTime(date("2004-12-12T11:15:10Z"))
                .withEndTime(null)
                .withAllDayEvent(null)
                .build();

	    convertToOBMEvent(msEvent);
	}

	@Test(expected = ConversionException.class)
	public void testConvertAttributeAllDayFalseNeedsEndTime()
            throws ConversionException {
	    MSEvent msEvent = new MSEventBuilder()
                .withStartTime(date("2004-12-12T11:15:10Z"))
                .withEndTime(null)
                .withAllDayEvent(false)
                .build();

	    convertToOBMEvent(msEvent);
	}

	@Test(expected = ConversionException.class)
	public void testConvertAttributeAllDayTrueNeedsEndTime()
            throws ConversionException {
	    MSEvent msEvent = new MSEventBuilder()
                .withStartTime(date("2004-12-12T11:15:10Z"))
                .withEndTime(null)
                .withAllDayEvent(true)
                .build();

	    convertToOBMEvent(msEvent);
	}

    @Test
    public void testConvertAttributeAllDayFalseOneHourDuration()
            throws ConversionException {

        Date startTime = date("2004-12-12T11:15:10Z");

        Date endTime = new Date(startTime.getTime() + 3600 * 1000);

        MSEvent msEvent = new MSEventBuilder()
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withSubject("Any Subject")
                .withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
                .withAllDayEvent(false)
                .build();

        Event convertedEvent = convertToOBMEvent(msEvent);
        assertThat(convertedEvent.getDuration()).isEqualTo(3600);
    }

    @Test
    public void testConvertAttributeAllDayTrueOneDayDuration()
            throws ConversionException {

        Date startTime = date("2004-12-12T11:15:10Z");

        Date endTime = new Date(startTime.getTime() + Event.SECONDS_IN_A_DAY * 1000);

        MSEvent msEvent = new MSEventBuilder()
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withSubject("Any Subject")
                .withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
                .withAllDayEvent(true)
                .build();

        Event convertedEvent = convertToOBMEvent(msEvent);
        assertThat(convertedEvent.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
    }

    @Test
    public void testConvertAttributeAllDayTrueTwoDaysDuration()
            throws ConversionException {

        Date startTime = date("2004-12-12T11:15:10Z");

        Date endTime = new Date(startTime.getTime() + Event.SECONDS_IN_A_DAY * 2 * 1000);

        MSEvent msEvent = new MSEventBuilder()
                .withStartTime(startTime)
                .withEndTime(endTime)
                .withSubject("Any Subject")
                .withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
                .withAllDayEvent(true)
                .build();

        Event convertedEvent = convertToOBMEvent(msEvent);
        assertThat(convertedEvent.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond() * 2));
    }

	@Test
	public void testConvertAttributeBusyStatusFree() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.FREE)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.TRANSPARENT);
	}

	@Test
	public void testConvertAttributeBusyStatusBusy() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.BUSY)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusOutOfOffice() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withBusyStatus(CalendarBusyStatus.UNAVAILABLE)
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}
	
	@Test
	public void testConvertAttributeBusyStatusTentative() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeBusyStatusNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withBusyStatus(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getOpacity()).isEqualTo(EventOpacity.OPAQUE);
	}

	@Test
	public void testConvertAttributeCategoryEmpty() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(new ArrayList<String>())
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getCategory()).isNull();
	}

	@Test
	public void testConvertAttributeCategoryIsFirst() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Lists.newArrayList("category1", "category2"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getCategory()).isEqualTo("category1");
	}

	@Test
	public void testConvertAttributeCategoryNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getCategory()).isNull();
	}
	
	@Test
	public void testConvertAttributeCategoryBeyondThreeHundred() throws ConversionException {
		String[] tooMuchCategories = new String[301];
		Arrays.fill(tooMuchCategories, "a category");
		MSEvent msEvent = new MSEventBuilder()
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withCategories(Arrays.asList(tooMuchCategories))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event event = convertToOBMEvent(msEvent);
		assertThat(event.getCategory()).isNull();
	}

	@Test
	public void testRetrievePriorityFromOldEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event oldEvent = new Event();
		oldEvent.setPriority(2);
		
		Event convertedEvent = convertToOBMEventWithEditingEvent(msEvent, oldEvent);
		
		assertThat(convertedEvent.getPriority()).isEqualTo(2);
	}
	
	@Test
	public void testConvertAttributeDtStampJustCreated() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getTimeCreate()).isEqualTo(msEvent.getDtStamp());
		assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampAlreadyCreated() throws ConversionException {
		Date previousDtStampDate = date("2004-12-11T11:15:10Z");
		Event editingEvent = new Event();
		editingEvent.setTimeCreate(previousDtStampDate);
		editingEvent.setTimeUpdate(previousDtStampDate);
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(date("2004-12-10T12:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(convertedEvent.getTimeCreate()).isEqualTo(previousDtStampDate);
		assertThat(convertedEvent.getTimeUpdate()).isEqualTo(msEvent.getDtStamp());
	}

	@Test
	public void testConvertAttributeDtStampNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDtStamp(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getTimeCreate()).isNull();
		assertThat(convertedEvent.getTimeUpdate()).isNull();
	}

	@Test
	public void testConvertAttributeDescription() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription("any description")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getDescription()).isEqualTo(msEvent.getDescription());
	}
	
	@Test
	public void testConvertAttributeDescriptionNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withDescription(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getDescription()).isNull();
	}

	@Test
	public void testConvertAttributeTimezoneSpecific() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(TimeZone.getTimeZone("America/Tijuana"))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getTimezoneName()).isEqualTo("America/Tijuana");
	}
	
	@Test
	public void testConvertAttributeTimezoneNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withTimeZone(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getTimezoneName()).isNull();
	}
	
	@Test
	public void testConvertAttributeLocation() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation("Any location")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getLocation()).isEqualTo(msEvent.getLocation());
	}
	
	@Test
	public void testConvertAttributeLocationNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withLocation(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getLocation()).isNull();
	}

	@Test
	public void testConvertAttributeSubject() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getTitle()).isEqualTo(msEvent.getSubject());
	}

	@Test
	public void testConvertAttributeSubjectEmpty() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getTitle()).isNull();
	}

	@Test
	public void testConvertAttributeSubjectNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		assertThat(convertedEvent.getTitle()).isNull();
	}
	
	@Test
	public void testConvertAttributeReminder() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(150)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		int reminderInSecond = minuteToSecond(msEvent.getReminder());
		assertThat(convertedEvent.getAlert()).isEqualTo(reminderInSecond);
	}

	@Test
	public void testConvertAttributeReminderZero() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(0)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getAlert()).isEqualTo(0);
	}

	@Test
	public void testConvertAttributeReminderNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withReminder(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getAlert()).isNull();
	}

	@Test
	public void testConvertAttributeSensitivityNormal() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.NORMAL)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeSensitivityConfidential() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.CONFIDENTIAL);
	}

	@Test
	public void testConvertAttributeSensitivityPersonal() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PERSONAL)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}

	@Test
	public void testConvertAttributeSensitivityPrivate() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(CalendarSensitivity.PRIVATE)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PRIVATE);
	}

	@Test
	public void testConvertAttributeSensitivityNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any subject")
				.withSensitivity(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		assertThat(convertedEvent.getPrivacy()).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertAttributeOrganizerNameOnlyGetsTheUserOne() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event convertedEvent = convertToOBMEvent(msEvent);

		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(user.getDisplayName());
	}

	@Test
	public void testConvertAttributeOrganizerNameOnlyNullGetsTheUserOne() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(user.getDisplayName());
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail("email@domain")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		assertThat(convertedEventOrganizer.getDisplayName()).isNull();
		assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test
	public void testConvertAttributeOrganizerEmailOnlyNullGetFromSession() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerEmail(null)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(user.getDisplayName());
		assertThat(convertedEventOrganizer.getEmail()).isEqualTo(user.getEmail());
	}
	
	@Test
	public void testConvertAttributeOrganizerNameAndEmail() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withOrganizerName("Any Name")
				.withOrganizerEmail("user@domain")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Attendee convertedEventOrganizer = convertedEvent.findOrganizer();
		assertThat(convertedEventOrganizer.getDisplayName()).isEqualTo(msEvent.getOrganizerName());
		assertThat(convertedEventOrganizer.getEmail()).isEqualTo(msEvent.getOrganizerEmail());
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeStartTimeOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeStartTimeNullOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeNullOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeEndTimeNullAndStartTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(null)
				.withSubject("Any Subject")
				.build();
		
		convertToOBMEvent(msEvent);
	}
	
	@Test
	public void testConvertAttributeEndTimeWhenAllDay() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withAllDayEvent(true)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();

		Event converted = convertToOBMEvent(msEvent);

		Date oneDayLaterStartDate = DateUtils.getOneDayLater(msEvent.getStartTime());
		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getEndDate()).isEqualTo(oneDayLaterStartDate);
	}
	
	@Test
	public void testConvertAttributeStartAndEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
	}

	@Test
	public void testConvertAttributeMeetingStatusIsInMeeting() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusIsNotInMeeting() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.IS_NOT_A_MEETING);
	}

	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceledAndReceived() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED)
				.build();
	
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED_AND_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingReceived() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_RECEIVED);
	}
	
	@Test
	public void testConvertAttributeMeetingStatusMeetingCanceled() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isEqualTo(EventMeetingStatus.MEETING_IS_CANCELED);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttributeMeetingStatusNull() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(null)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getMeetingStatus()).isNull();
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2005-12-11T11:15:10Z"))
				.withSubject("Any Subject")
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getEndDate()).isEqualTo(msEvent.getEndTime());
		assertThat(converted.getDuration()).isEqualTo(getOneYearInSecond());
	}

	@Test
	public void testCalculatedAttributeDurationByStartAndEndTimeWhenAllDay() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withAllDayEvent(true)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);

		Date oneDayLaterStartDate = DateUtils.getOneDayLater(msEvent.getStartTime());
		assertThat(converted.isAllday()).isTrue();
		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getEndDate()).isEqualTo(oneDayLaterStartDate);
		assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayOnly() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}

	@Test
	public void testCalculatedAttributeDurationByAllDayWhenHasEndTime() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T12:15:10Z"))
				.withSubject("Any Subject")
				.withAllDayEvent(true)
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event converted = convertToOBMEvent(msEvent);
		
		assertThat(converted.getStartDate()).isEqualTo(msEvent.getStartTime());
		assertThat(converted.getDuration()).isEqualTo(Ints.checkedCast(getOneDayInSecond()));
	}

	@Test
	public void testWithOrganizerEmailAndNameButNoAttendee() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail("organizer@thilaire.lng.org")
				.withOrganizerName("organizer")
				.withAttendees(ImmutableSet.<MSAttendee>of())
				.build();
		
		Event editingEvent = new Event();
		editingEvent.setUid(new EventObmId(52));
		editingEvent.setExtId(new EventExtId("abc"));
		editingEvent.setAllday(false);
		editingEvent.setTimeCreate(date("2004-12-10T11:15:10Z"));
		editingEvent.setTimeUpdate(date("2004-12-10T11:15:10Z"));
		editingEvent.setStartDate(date("2004-12-11T11:15:10Z"));
		editingEvent.setDuration(7200);
		editingEvent.setTitle("Any Subject");
		editingEvent.setOwner("other organizer name");
		editingEvent.setOwnerEmail("organizer@thilaire.lng.org");
		editingEvent.addAttendee(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testWithOrganizerEmailAndNameButNoAttendeeNoEditingEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail("organizer@thilaire.lng.org")
				.withOrganizerName("organizer")
				.withAttendees(ImmutableSet.<MSAttendee>of())
				.build();
		
		Event editingEvent = null;

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testWithOrganizerEmailAndNameButAttendeeAndNoEditingEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail("organizer@thilaire.lng.org")
				.withOrganizerName("organizer")
				.withAttendees(ImmutableSet.of(
					MSAttendee.builder()
						.withName("invitee")
						.withEmail("invitee@thilaire.lng.org")
						.withType(AttendeeType.REQUIRED)
						.build()))
				.build();
		
		Event editingEvent = null;

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(2).containsOnly(
			UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build(),
			UserAttendee.builder()
				.asAttendee()
				.displayName("invitee")
				.email("invitee@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsAddedFromEditingEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.<MSAttendee>of())
				.build();
		
		Event editingEvent = new Event();
		editingEvent.setUid(new EventObmId(52));
		editingEvent.setExtId(new EventExtId("abc"));
		editingEvent.setAllday(false);
		editingEvent.setTimeCreate(date("2004-12-10T11:15:10Z"));
		editingEvent.setTimeUpdate(date("2004-12-10T11:15:10Z"));
		editingEvent.setStartDate(date("2004-12-11T11:15:10Z"));
		editingEvent.setDuration(7200);
		editingEvent.setTitle("Any Subject");
		editingEvent.setOwner("other organizer name");
		editingEvent.setOwnerEmail("organizer@thilaire.lng.org");
		editingEvent.addAttendee(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsTookFromEditingEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.of(
					MSAttendee.builder()
						.withName("organizer")
						.withEmail("organizer@thilaire.lng.org")
						.withType(AttendeeType.REQUIRED)
						.build()))
				.build();
		
		Event editingEvent = new Event();
		editingEvent.setUid(new EventObmId(52));
		editingEvent.setExtId(new EventExtId("abc"));
		editingEvent.setAllday(false);
		editingEvent.setTimeCreate(date("2004-12-10T11:15:10Z"));
		editingEvent.setTimeUpdate(date("2004-12-10T11:15:10Z"));
		editingEvent.setStartDate(date("2004-12-11T11:15:10Z"));
		editingEvent.setDuration(7200);
		editingEvent.setTitle("Any Subject");
		editingEvent.setOwner("other organizer name");
		editingEvent.setOwnerEmail("organizer@thilaire.lng.org");
		editingEvent.addAttendee(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsAddedToAttendeesFromEditingEvent() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.of(
					MSAttendee.builder()
						.withName("invitee")
						.withEmail("invitee@thilaire.lng.org")
						.withType(AttendeeType.OPTIONAL)
						.build()))
				.build();
		
		Event editingEvent = new Event();
		editingEvent.setUid(new EventObmId(52));
		editingEvent.setExtId(new EventExtId("abc"));
		editingEvent.setAllday(false);
		editingEvent.setTimeCreate(date("2004-12-10T11:15:10Z"));
		editingEvent.setTimeUpdate(date("2004-12-10T11:15:10Z"));
		editingEvent.setStartDate(date("2004-12-11T11:15:10Z"));
		editingEvent.setDuration(7200);
		editingEvent.setTitle("Any Subject");
		editingEvent.setOwner("other organizer name");
		editingEvent.setOwnerEmail("organizer@thilaire.lng.org");
		editingEvent.addAttendee(UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(2).containsOnly(
			UserAttendee.builder()
				.asAttendee()
				.displayName("invitee")
				.email("invitee@thilaire.lng.org")
				.participationRole(ParticipationRole.OPT)
				.participation(Participation.accepted())
				.build(),
			UserAttendee.builder()
				.asOrganizer()
				.displayName("organizer")
				.email("organizer@thilaire.lng.org")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsAddedFromUserRequest() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.<MSAttendee>of())
				.build();
		
		Event editingEvent = null;

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("display name")
				.email("user@domain")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsTookFromUserRequest() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.of(
					MSAttendee.builder()
						.withName("display name")
						.withEmail("user@domain")
						.withType(AttendeeType.REQUIRED)
						.build()))
				.build();

		Event editingEvent = null;

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(1).containsOnly(UserAttendee.builder()
				.asOrganizer()
				.displayName("display name")
				.email("user@domain")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}

	@Test
	public void testNoOrganizerEmailAndNameIsAddedToAttendeesFromUserRequest() throws ConversionException {
		MSEvent msEvent = new MSEventBuilder()
				.withSubject("Any Subject")
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-11T11:15:10Z"))
				.withMeetingStatus(CalendarMeetingStatus.IS_NOT_A_MEETING)
				.withOrganizerEmail(null)
				.withOrganizerName(null)
				.withAttendees(ImmutableSet.of(
					MSAttendee.builder()
						.withName("invitee")
						.withEmail("invitee@thilaire.lng.org")
						.withType(AttendeeType.OPTIONAL)
						.build()))
				.build();

		Event editingEvent = null;

		Event converted = convertToOBMEventWithEditingEvent(msEvent, editingEvent);

		assertThat(converted.getAttendees()).hasSize(2).containsOnly(
			UserAttendee.builder()
				.asAttendee()
				.displayName("invitee")
				.email("invitee@thilaire.lng.org")
				.participationRole(ParticipationRole.OPT)
				.participation(Participation.accepted())
				.build(),
			UserAttendee.builder()
				.asOrganizer()
				.displayName("display name")
				.email("user@domain")
				.participationRole(ParticipationRole.REQ)
				.participation(Participation.accepted())
				.build());
	}
	
	@Test
	public void testConvertExceptionAttributeDeletedTrue() throws ConversionException {
		MSEventException msEventException = new MSEventException();
		msEventException.setDtStamp(date("2004-12-10T11:15:10Z"));
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
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.withTimeZone(DateTimeZone.UTC.toTimeZone())
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);

		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		Set<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		assertThat(exceptions).hasSize(1);
		assertThat(exceptions).containsOnly(date("2004-10-11T00:00:00Z"));
		assertThat(eventExceptions).isEmpty();
	}

	@Test
	public void testConvertSensitivityToPrivacyKeepingOldValueWithoutPreviousValue() {
		MSEventCommon msEventCommon = new MSEvent();
		msEventCommon.setSensitivity(CalendarSensitivity.CONFIDENTIAL);
		
		EventPrivacy eventPrivacy = converter.convertSensitivityToPrivacyKeepingOldValue(msEventCommon, null);
		assertThat(eventPrivacy).isEqualTo(
				MSEventToObmEventConverterImpl.SENSITIVITY_TO_PRIVACY.get(msEventCommon.getSensitivity()));
	}

	@Test
	public void testConvertSensitivityToPrivacyKeepingOldValueWithPreviousValue() {
		MSEventCommon msEventCommon = new MSEvent();
		msEventCommon.setSensitivity(CalendarSensitivity.CONFIDENTIAL);
		
		Event eventFromDB = new Event();
		eventFromDB.setPrivacy(EventPrivacy.PUBLIC);
		EventPrivacy eventPrivacy = converter.convertSensitivityToPrivacyKeepingOldValue(msEventCommon, eventFromDB);
		assertThat(eventPrivacy).isEqualTo(eventFromDB.getPrivacy());
	}

	@Test
	public void testConvertSensitivityToPrivacyKeepingOldValueDefaultValue() {
		EventPrivacy eventPrivacy = converter.convertSensitivityToPrivacyKeepingOldValue(new MSEvent(), null);
		assertThat(eventPrivacy).isEqualTo(EventPrivacy.PUBLIC);
	}
	
	@Test
	public void testConvertExceptionAttributeDeletedFalse() throws ConversionException {
		MSEventException msEventException = new MSEventException();
		msEventException.setDtStamp(date("2004-12-10T11:15:10Z"));
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
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		
		Event convertedEvent = convertToOBMEvent(msEvent);
		
		Iterable<Date> exceptions = convertedEvent.getRecurrence().getExceptions();
		Set<Event> eventExceptions = convertedEvent.getRecurrence().getEventExceptions();
		assertThat(exceptions).isEmpty();
		assertThat(eventExceptions).hasSize(1);
		assertThat(Iterables.getOnlyElement(eventExceptions).getRecurrenceId()).isEqualTo(msEventException.getExceptionStartTime());
	}

	private MSRecurrence simpleRecurrence(RecurrenceType type) {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setInterval(1);
		recurrence.setType(type);
		return recurrence;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws ConversionException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}
	
	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws ConversionException {
		return converter.convert(user, editingEvent, msEvent, false);
	}

	private Date date(String date) {
		return org.obm.DateUtils.date(date);
	}

	private int getOneDayInSecond() {
		return (int) DateUtils.daysToSeconds(1);
	}

	private int getOneYearInSecond() {
		return (int) DateUtils.yearsToSeconds(1);
	}

	private int minuteToSecond(int minutes) {
		return DateUtils.minutesToSeconds(minutes);
	}
}
