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
package org.obm.push.protocol.data.ms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestIntDBusyStatus;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceDayOfWeek;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestSensitivity;
import org.obm.push.exception.ConversionException;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.data.ASEmail;
import org.obm.push.protocol.data.ASTimeZoneConverter;
import org.obm.push.protocol.data.ActiveSyncDecoder;
import org.obm.push.protocol.data.Base64ASTimeZoneDecoder;
import org.obm.push.protocol.data.MSEmailEncoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class MSMeetingRequestDecoder extends ActiveSyncDecoder {

	private final Base64ASTimeZoneDecoder base64asTimeZoneDecoder;
	private final ASTimeZoneConverter asTimeZoneConverter;
	
	private final SimpleDateFormat utcDateFormat;
	private final SimpleDateFormat utcRecurrenceDateFormat;
	
	@Inject
	protected MSMeetingRequestDecoder(Base64ASTimeZoneDecoder base64asTimeZoneDecoder, ASTimeZoneConverter asTimeZoneConverter) {
		this.base64asTimeZoneDecoder = base64asTimeZoneDecoder;
		this.asTimeZoneConverter = asTimeZoneConverter;
		utcDateFormat = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_PATTERN);
		utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		utcRecurrenceDateFormat = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_NO_PUNCTUATION_PATTERN);
		utcRecurrenceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public MSMeetingRequest decode(Element data) throws ConversionException {
		try {
			return MSMeetingRequest.builder()
					.allDayEvent(uniqueBooleanFieldValue(data, ASEmail.ALL_DAY_EVENT, false))
					.dtStamp(date(uniqueStringFieldValue(data, ASEmail.DTSTAMP)))
					.startTime(date(uniqueStringFieldValue(data, ASEmail.START_TIME)))
					.endTime(date(uniqueStringFieldValue(data, ASEmail.END_TIME)))
					.timeZone(timeZone(uniqueStringFieldValue(data, ASEmail.TIME_ZONE)))
					.instanceType(instanceType(uniqueIntegerFieldValue(data, ASEmail.INSTANCE_TYPE)))
					.intDBusyStatus(busyStatus(uniqueIntegerFieldValue(data, ASEmail.INT_DB_BUSY_STATUS)))
					.organizer(uniqueStringFieldValue(data, ASEmail.ORGANIZER))
					.location(uniqueStringFieldValue(data, ASEmail.LOCATION))
					.reminder(uniqueLongFieldValue(data, ASEmail.REMINDER))
					.responseRequested(uniqueBooleanFieldValue(data, ASEmail.RESPONSE_REQUESTED, false))
					.msEventUid(new MSEventUid(uniqueStringFieldValue(data, ASEmail.GLOBAL_OBJ_ID)))
					.sensitivity(sensitivity(uniqueIntegerFieldValue(data, ASEmail.SENSITIVITY)))
					.recurrenceId(recurrenceDate(uniqueStringFieldValue(data, ASEmail.RECURRENCE_ID)))
					.recurrences(recurrences(DOMUtils.getUniqueElement(data, ASEmail.RECURRENCES.getName())))
					.build();
		} catch (ParseException e) {
			throw new ConversionException("A date field is not valid", e);
		}
	}

	@VisibleForTesting List<MSMeetingRequestRecurrence> recurrences(Element recurrencesElement)
			throws ParseException, ConversionException {
		
		List<MSMeetingRequestRecurrence> recurrences = Lists.newArrayList();
		if (recurrencesElement != null) {
			for (Node recurrence : DOMUtils.getElementsByName(recurrencesElement, ASEmail.RECURRENCE.getName())) {
				Element recurrenceEl = (Element) recurrence;
				recurrences.add(MSMeetingRequestRecurrence.builder()
					.dayOfMonth(uniqueIntegerFieldValue(recurrenceEl, ASEmail.DAY_OF_MONTH))
					.dayOfWeek(dayOfWeek(uniqueIntegerFieldValue(recurrenceEl, ASEmail.DAY_OF_WEEK)))
					.weekOfMonth(uniqueIntegerFieldValue(recurrenceEl, ASEmail.WEEK_OF_MONTH))
					.monthOfYear(uniqueIntegerFieldValue(recurrenceEl, ASEmail.MONTH_OF_YEAR))
					.interval(interval(uniqueIntegerFieldValue(recurrenceEl, ASEmail.INTERVAL)))
					.occurrences(uniqueIntegerFieldValue(recurrenceEl, ASEmail.OCCURRENCES))
					.type(recurrenceType(uniqueIntegerFieldValue(recurrenceEl, ASEmail.TYPE)))
					.until(recurrenceDate(uniqueStringFieldValue(recurrenceEl, ASEmail.UNTIL)))
					.build());
			}
		}
		return recurrences;
	}

	private Integer interval(Integer interval) throws ConversionException {
		if (interval != null) {
			return interval;
		}
		throw new ConversionException("Interval is required");
	}

	private MSMeetingRequestRecurrenceType recurrenceType(Integer recurrenceType) throws ConversionException {
		if (recurrenceType != null) {
			return MSMeetingRequestRecurrenceType.getValueOf(recurrenceType);
		}
		throw new ConversionException("RecurrenceType is required");
	}

	private List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek(Integer integer) {
		return MSMeetingRequestRecurrenceDayOfWeek.getValuesOf(integer);
	}

	private MSMeetingRequestSensitivity sensitivity(Integer value) {
		return Objects.firstNonNull(MSMeetingRequestSensitivity.getValueOf(value), MSMeetingRequestSensitivity.NORMAL);
	}

	private MSMeetingRequestIntDBusyStatus busyStatus(Integer busyStatus) {
		return MSMeetingRequestIntDBusyStatus.getValueOf(busyStatus);
	}

	private MSMeetingRequestInstanceType instanceType(Integer instanceType) throws ConversionException {
		if (instanceType != null) {
			return MSMeetingRequestInstanceType.getValueOf(instanceType);
		}
		throw new ConversionException("instanceType is required, found : " + instanceType);
	}

	@VisibleForTesting Date recurrenceDate(String dateAsString) throws ParseException {
		if (!Strings.isNullOrEmpty(dateAsString)) {
			return utcRecurrenceDateFormat.parse(dateAsString);
		}
		return null;
	}

	@VisibleForTesting Date date(String dateAsString) throws ParseException, ConversionException {
		if (!Strings.isNullOrEmpty(dateAsString)) {
			return utcDateFormat.parse(dateAsString);
		}
		throw new ConversionException("A required date is missing");
	}

	@VisibleForTesting TimeZone timeZone(String timeZone) throws ConversionException {
		if (!Strings.isNullOrEmpty(timeZone)) {
			byte[] tzInBase64 = timeZone.getBytes(Charsets.UTF_8);
			ASTimeZone asTimeZone = base64asTimeZoneDecoder.decode(tzInBase64);
			if (asTimeZone != null) {
				return asTimeZoneConverter.convert(asTimeZone);
			} else {
				throw new ConversionException("TimeZone format is invalid or not supported : " + timeZone);
			}
		}
		throw new ConversionException("The timeZone is required");
	}
}
