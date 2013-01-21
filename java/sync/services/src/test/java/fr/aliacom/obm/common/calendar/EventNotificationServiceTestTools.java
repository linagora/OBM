/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.UserAttendee;

public class EventNotificationServiceTestTools {

	public static <T, C extends Collection<T>> C compareCollections(C collection) {
		return EasyMock.cmp(collection, new Comparator<C>() {
			public int compare(C o1, C o2) {
				if (o1.size() != o2.size()) {
					return -1;
				}
				Iterator<T> it1 = o1.iterator();
				Iterator<T> it2 = o2.iterator();
				while (it1.hasNext()) {
					if (!it1.next().equals(it2.next())) {
						return -1;
					}
				}
				return 0;
			}
		}, LogicalOperator.EQUAL);
	}
	
	static Date before() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}

	static Date after() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTime();
	}

	static Date longAfter() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		return cal.getTime();
	}
	
	static Attendee createRequiredAttendee(String email, Participation state) {
		return UserAttendee
				.builder()
				.email(email)
				.participationRole(ParticipationRole.REQ)
				.participation(state)
				.canWriteOnCalendar(false)
				.build();
	}

	static List<Attendee> createRequiredAttendees(String prefix, String suffix, Participation state, int start, int number) {
		ArrayList<Attendee> result = new ArrayList<Attendee>();
		for (int i = 0; i < number; ++i) {
			result.add(createRequiredAttendee(prefix + (start + i)+ suffix,state));
		}
		return result;
	}
}
