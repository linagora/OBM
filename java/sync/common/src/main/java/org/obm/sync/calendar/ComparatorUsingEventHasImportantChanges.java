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

import java.util.Comparator;
import java.util.Date;

import com.google.common.base.Strings;

public class ComparatorUsingEventHasImportantChanges implements Comparator<Event> {

	private final static int SAME = 0;
	private final static int NOT_SAME = -1;
	
	@Override
	public int compare(Event o1, Event o2) {
		int locationCompare = compare(o1.getLocation(), o2.getLocation());
		if (locationCompare != 0) {
			return locationCompare;
		}
		int dateCompare = compare(o1.getStartDate(), o2.getStartDate());
		if (dateCompare != 0) {
			return dateCompare;
		}
		int durationCompare = compare(o1.getDuration(), o2.getDuration());
		if (durationCompare != 0) {
			return durationCompare;
		}
		int recurrenceIdCompare = compare(o1.getRecurrenceId(), o2.getRecurrenceId());
		if (recurrenceIdCompare != 0) {
			return recurrenceIdCompare;
		}
		return compare(o1.getType(), o2.getType());
	}
	
	private int compare(String o1, String o2) {
		if (!StringsAreNullOrEmpty(o1, o2)) {
			return o1.compareTo(o2);
		}
		int compare = NOT_SAME;
		if (Strings.isNullOrEmpty(o1) && Strings.isNullOrEmpty(o2)) {
			compare = SAME;
		}
		return compare;
	}
	
	private int compare(Date o1, Date o2) {
		if (objectsAreNotNull(o1, o2)) {
			return o1.compareTo(o2);
		}
		return compareToNull(o1, o2);
	}
	
	private int compare(Enum<?> o1, Enum<?> o2) {
		if (objectsAreNotNull(o1, o2)) {
			return compare(o1.ordinal(), o2.ordinal());
		}
		return compareToNull(o1, o2);
	}

	private int compare(int o1, int o2) {
		return o1 - o2;
	}
	
	private boolean StringsAreNullOrEmpty(String o1, String o2) {
		if (Strings.isNullOrEmpty(o1) || Strings.isNullOrEmpty(o2)) {
			return true;
		}
		return false;
	}

	private boolean objectsAreNotNull(Object o1, Object o2) {
		if (o1 != null && o2 != null) {
			return true;
		}
		return false;
	}
	
	private int compareToNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return SAME;
		}
		return NOT_SAME;
	}
	
	public boolean equals(Event o1, Event o2) {
		int compare = compare(o1, o2);
		if (compare == 0) {
			return true;
		}
		return false;
	}

}
