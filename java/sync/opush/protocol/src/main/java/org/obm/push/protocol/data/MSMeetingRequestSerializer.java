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
package org.obm.push.protocol.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceDayOfWeek;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.IntEncoder;
import org.obm.push.utils.IntEncoder.Capacity;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

public class MSMeetingRequestSerializer {

	private final IntEncoder intEncoder;
	private final Element parentElement;
	private Element meetingRequestElement;
	private final MSMeetingRequest meetingRequest;
	
	public MSMeetingRequestSerializer(IntEncoder intEncoder, Element parentElement,
			MSMeetingRequest meetingRequest) {
		
		this.intEncoder = intEncoder;
		this.parentElement = parentElement;
		this.meetingRequest = meetingRequest;
	}

	public void serializeMSMeetingRequest(String timeZone) {
		Preconditions.checkNotNull(meetingRequest, "The meeting request is required");
		meetingRequestElement = DOMUtils.createElement(parentElement, ASEmail.MEETING_REQUEST.asASValue());
		
		serializeAllDayEvent();
		serializeStartTime();
		serializeEndTime();
		serializeDTStamp();
		serializeInstanceType();
		serializeLocation();
		serializeOrganizer();
		serializeReminder();
		serializeResponseRequested();
		serializeSensitivity();
		serializeBusyStatus();
		serializeTimeZone(timeZone);
		serializeGlobalId();
		serializeRecurrenceId();

		serializeMSMeetingRequestRecurrence();
	}

	private void serializeAllDayEvent() {
		DOMUtils.createElementAndText(meetingRequestElement, 
				ASEmail.ALL_DAY_EVENT.asASValue(), meetingRequest.isAllDayEvent());
	}
	
	private void serializeStartTime() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.START_TIME.asASValue(), formatDate(meetingRequest.getStartTime()));
	}
	
	private void serializeEndTime() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.END_TIME.asASValue(), formatDate(meetingRequest.getEndTime()));
	}
	
	private void serializeDTStamp() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.DTSTAMP.asASValue(), formatDate(meetingRequest.getDtStamp()));
	}
	
	private void serializeInstanceType() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.INSTANCE_TYPE.asASValue(), meetingRequest.getInstanceType().specificationValue());
	}
	
	private void serializeLocation() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.LOCATION.asASValue(), meetingRequest.getLocation());
	}
	
	private void serializeOrganizer() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.ORGANIZER.asASValue(), meetingRequest.getOrganizer());
	}
	
	private void serializeReminder() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.REMINDER.asASValue(), meetingRequest.getReminder());
	}
	
	private void serializeResponseRequested() {
		if (meetingRequest.isResponseRequested()) {
			DOMUtils.createElementAndText(meetingRequestElement, 
					ASEmail.RESPONSE_REQUESTED.asASValue(), meetingRequest.isResponseRequested());
		}
	}
	
	private void serializeSensitivity() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.SENSITIVITY.asASValue(), meetingRequest.getSensitivity().specificationValue());
	}
	
	private void serializeBusyStatus() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.INT_DB_BUSY_STATUS.asASValue(), meetingRequest.getIntDBusyStatus().specificationValue());
	}

	private void serializeTimeZone(String timeZone) {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement,
				ASEmail.TIME_ZONE.asASValue(), timeZone);
	}

	private void serializeGlobalId() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.GLOBAL_OBJ_ID.asASValue(), msEventUidToGlobalObjId(meetingRequest.getMsEventUid(), intEncoder));
	}
	
	private void serializeRecurrenceId() {
		DOMUtils.createElementAndTextIfNotNull(meetingRequestElement, 
				ASEmail.RECURRENCE_ID.asASValue(), formatRecurrenceDate(meetingRequest.getRecurrenceId()));
	}
	
	private void serializeMSMeetingRequestRecurrence() {
		List<MSMeetingRequestRecurrence> recurrences = meetingRequest.getRecurrences();
		if (recurrences != null && !recurrences.isEmpty()) {
			Element recurrencesElement = DOMUtils.createElement(meetingRequestElement, ASEmail.RECURRENCES.asASValue());
			for (MSMeetingRequestRecurrence recurrence: recurrences) {
				Element recurrenceElement = DOMUtils.createElement(recurrencesElement, ASEmail.RECURRENCE.asASValue());
				serializeInterval(recurrence, recurrenceElement);
				serializeUntil(recurrence, recurrenceElement);
				serializeOccurrences(recurrence, recurrenceElement);
				serializeType(recurrence, recurrenceElement);
				serializeDayOfMonth(recurrence, recurrenceElement);
				serializeMonthOfYears(recurrence, recurrenceElement);
				serializeWeekOfMonth(recurrence, recurrenceElement);
				serializeDayOfWeek(recurrence, recurrenceElement);
			}
		}
	}

	private void serializeDayOfWeek(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		List<MSMeetingRequestRecurrenceDayOfWeek> dayOfWeek = recurrence.getDayOfWeek();
		if (dayOfWeek != null && !dayOfWeek.isEmpty()) {
			int computedValue = 0;
			for (MSMeetingRequestRecurrenceDayOfWeek day : dayOfWeek) {
				computedValue += day.asXmlValue();
			}
			DOMUtils.createElementAndText(parentElement, ASEmail.DAY_OF_WEEK.asASValue(), computedValue);
		}
	}

	private void serializeInterval(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.INTERVAL.asASValue(), recurrence.getInterval());
	}
	
	private void serializeUntil(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.UNTIL.asASValue(), formatRecurrenceDate(recurrence.getUntil()));
	}
	
	private void serializeOccurrences(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.OCCURRENCES.asASValue(), recurrence.getOccurrences());
	}
	
	private void serializeType(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.TYPE.asASValue(), recurrence.getType().specificationValue());
	}
	
	private void serializeDayOfMonth(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.DAY_OF_MONTH.asASValue(), recurrence.getDayOfMonth());
	}

	private void serializeMonthOfYears(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.MONTH_OF_YEAR.asASValue(), recurrence.getMonthOfYear());
	}

	private void serializeWeekOfMonth(MSMeetingRequestRecurrence recurrence, Element parentElement) {
		DOMUtils.createElementAndTextIfNotNull(parentElement, 
				ASEmail.WEEK_OF_MONTH.asASValue(), recurrence.getWeekOfMonth());
	}

	@VisibleForTesting String formatDate(Date date) {
		return formatDate(date, MSEmailEncoder.UTC_DATE_PATTERN);
	}
	
	@VisibleForTesting String formatRecurrenceDate(Date date) {
		return formatDate(date, MSEmailEncoder.UTC_DATE_NO_PUNCTUATION_PATTERN);
	}
	
	private String formatDate(Date date, String dateFormat) {
		if (date != null) {
			return new SimpleDateFormat(dateFormat).format(date);
		} else {
			return null;
		}
	}
	
	public static String msEventUidToGlobalObjId(MSEventUid msEventUid, IntEncoder intEncoder) {
		byte[] eventUidAsBytes = msEventUid.serializeToString().getBytes(Charsets.US_ASCII);
		byte[] preambule = buildByteSequence(
				0x4, 0x0, 0x0, 0x0, 0x82, 0x0, 0xE0, 0, 0x74, 0xC5, 0xB7, 0x10, 0x1A, 0x82, 0xE0, 0x08,
				0x0, 0x0, 0x0, 0x0,
				0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0);
		
		byte[] marker = "vCal-Uid".getBytes(Charsets.US_ASCII);
		byte[] markerEnd = buildByteSequence(0x1, 0x0, 0x0, 0x0);
		byte[] dataEnd = buildByteSequence(0x0);
		byte[] length = intEncoder
				.capacity(Capacity.FOUR)
				.toByteArray(marker.length + markerEnd.length + eventUidAsBytes.length + dataEnd.length);
		
		byte[] result = Bytes.concat(preambule, length, marker, markerEnd, eventUidAsBytes, dataEnd);
		return Base64.encodeBase64String(result);
	}

	@VisibleForTesting static byte[] buildByteSequence(int... bytes) {
		byte[] byteArray = new byte[bytes.length];
		int i = 0;
		for (int b: bytes) {
			byteArray[i++] = (byte) b;
		}
		return byteArray;
	}
}
