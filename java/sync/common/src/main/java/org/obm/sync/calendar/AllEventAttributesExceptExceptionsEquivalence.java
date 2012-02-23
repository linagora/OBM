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
package org.obm.sync.calendar;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;


public class AllEventAttributesExceptExceptionsEquivalence extends Equivalence<Event> {

	@Override
	public boolean doEquivalent(Event o1, Event o2) {
		
		if (!same(o1.getRecurrence(), o2.getRecurrence())) {
			return false;
		}
		if (!Objects.equal(o1.isAllday(), o2.isAllday())) {
			return false;
		}
		if (!Objects.equal(o1.getAlert(), o2.getAlert())) {
			return false;
		}
		if (!Objects.equal(o1.getAttendees(), o2.getAttendees())) {
			return false;
		}
		if (!Objects.equal(o1.getCategory(), o2.getCategory())) {
			return false;
		}
		if (!Objects.equal(o1.getCompletion(), o2.getCompletion())) {
			return false;
		}
		if (!Objects.equal(o1.getDescription(), o2.getDescription())) {
			return false;
		}
		if (!Objects.equal(o1.getEndDate(), o2.getEndDate())) {
			return false;
		}
		if (!Objects.equal(o1.getOpacity(), o2.getOpacity())) {
			return false;
		}
		if (!Objects.equal(o1.getPercent(), o2.getPercent())) {
			return false;
		}
		if (!Objects.equal(o1.getPriority(), o2.getPriority())) {
			return false;
		}
		if (!Objects.equal(o1.getPrivacy(), o2.getPrivacy())) {
			return false;
		}
		if (!Objects.equal(o1.getTitle(), o2.getTitle())) {
			return false;
		}
		if (!Objects.equal(o1.getLocation(), o2.getLocation())) {
			return false;
		}
		if (!Objects.equal(o1.getDate(), o2.getDate())) {
			return false;
		}
		if (!Objects.equal(o1.getRecurrenceId(), o1.getRecurrenceId())) {
			return false;
		}
		if (!Objects.equal(o1.getType(), o2.getType())) {
			return false;
		}
		
		return true;
	}

	private boolean same(EventRecurrence recurrence, EventRecurrence recurrence2) {
	
		if (objectsAreNotNull(recurrence, recurrence2)) {
			if (!Objects.equal(recurrence.getKind(), recurrence2.getKind())) {
				return false;
			}
			if(!RecurrenceKind.none.equals(recurrence.getKind())){
				if (!Objects.equal(recurrence.getDays(), recurrence2.getDays())) {
					return false;
				}
			
				if (!Objects.equal(recurrence.getEnd(), recurrence2.getEnd())) {
					return false;
				}
			
				if (!Objects.equal(recurrence.getFrequence(), recurrence2.getFrequence())) {
					return false;
				}
			}
			return true;
		}
		return sameToNull(recurrence, recurrence2);
	}
	
	public boolean sameToNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean objectsAreNotNull(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			return true;
		}
		return false;
	}

	@Override
	protected int doHash(Event t) {
		return Objects.hashCode(t.getTitle(), t.getDomain(), t.getDescription(),
				t.getObmId(), t.getExtId(), t.getPrivacy(), t.getOwner(), t.getOwnerDisplayName(), t.getOwnerEmail(),
				t.getLocation(), t.getDate(), t.getDuration(), t.getAlert(), t.getCategory(), t.getPriority(), t.isAllday(),
				t.getAttendees(), t.getType(), t.getCompletion(), t.getPercent(), t.getOpacity(),
				t.getEntityId(), t.getTimeUpdate(), t.getTimeCreate(), t.getTimezoneName(), t.getRecurrenceId(),
				t.isInternalEvent(), t.getSequence());
	}

}
