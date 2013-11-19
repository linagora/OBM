/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.sync.calendar;

import static org.obm.DateUtils.date;

import java.util.Comparator;

import com.google.common.base.Objects;

public class CalendarUtils {

	public static Comparator<? super Event> ignoreDatabaseElementsComparator() {
		return new Comparator<Event>() {
	
			@Override
			public int compare(Event one, Event two) {
				boolean equalityIgnoringDatabaseElements = 
					Objects.equal(one.getTitle(), two.getTitle())
					&& Objects.equal(one.getDescription(), two.getDescription())
					&& Objects.equal(one.getExtId(), two.getExtId())
					&& Objects.equal(one.getPrivacy(), two.getPrivacy())
					&& Objects.equal(one.getMeetingStatus(), two.getMeetingStatus())
					&& Objects.equal(one.getOwner(), two.getOwner())
					&& Objects.equal(one.getOwnerDisplayName(), two.getOwnerDisplayName())
					&& Objects.equal(one.getOwnerEmail(), two.getOwnerEmail())
					&& Objects.equal(one.getCreatorDisplayName(), two.getCreatorDisplayName())
					&& Objects.equal(one.getCreatorEmail(), two.getCreatorEmail())
					&& Objects.equal(one.getLocation(), two.getLocation())
					&& Objects.equal(one.getStartDate(), two.getStartDate())
					&& Objects.equal(one.getDuration(), two.getDuration())
					&& Objects.equal(one.getAlert(), two.getAlert())
					&& Objects.equal(one.getCategory(), two.getCategory())
					&& Objects.equal(one.getPriority(), two.getPriority())
					&& Objects.equal(one.isAllday(), two.isAllday())
					&& Objects.equal(one.getAttendees(), two.getAttendees())
					&& Objects.equal(one.getRecurrence(), two.getRecurrence())
					&& Objects.equal(one.getType(), two.getType())
					&& Objects.equal(one.getOpacity(), two.getOpacity())
					&& Objects.equal(one.getEntityId(), two.getEntityId())
					&& Objects.equal(one.getTimezoneName(), two.getTimezoneName())
					&& Objects.equal(one.getRecurrenceId(), two.getRecurrenceId())
					&& Objects.equal(one.isInternalEvent(), two.isInternalEvent())
					&& Objects.equal(one.getSequence(), two.getSequence());
					
				if (equalityIgnoringDatabaseElements) {
					return 0;
				}
				return 1;
			}
		};
	}

	public static CalendarInfo makeTestUserCalendarInfo(String suffix, boolean read, boolean write) {
		return makeCalendarInfo("user" + suffix, "Firstname", "Lastname_" + suffix, read, write);
	}

	public static CalendarInfo makeCalendarInfo(String uid, String firstname, String lastname, boolean read, boolean write) {
		CalendarInfo info = new CalendarInfo();

		info.setUid(uid);
		info.setFirstname(firstname);
		info.setLastname(lastname);
		info.setRead(read);
		info.setWrite(write);

		return info;
	}

	public static ResourceInfo makeTestResourceInfo(int id, String suffix, boolean read, boolean write) {
		return ResourceInfo
				.builder()
				.id(id)
				.name("res" + suffix)
				.description("description of res" + suffix)
				.mail("res-" + suffix + "@domain.org")
				.domainName("domain.org")
				.read(read)
				.write(write)
				.build();
	}

	public static Event newEvent(String calendar, String owner, String extId) {
		Event event = new Event();

		event.setTitle("Title_" + extId);
		event.setOwner(owner);
		event.setOwnerDisplayName(owner);
		event.setCategory("");
		event.setDescription("");
		event.setLocation("");
		event.setPriority(0);
		event.setInternalEvent(true);
		event.setOwnerEmail(calendar);
		event.setExtId(new EventExtId(extId));
		event.setStartDate(date("2013-06-01T12:00:00"));
		event.addAttendee(UnidentifiedAttendee.builder().email(calendar).participation(Participation.accepted()).asOrganizer().build());

		return event;
	}
}
