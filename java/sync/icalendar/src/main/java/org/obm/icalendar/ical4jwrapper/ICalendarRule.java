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
package org.obm.icalendar.ical4jwrapper;

import java.util.Collection;

import com.google.common.collect.Iterables;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.RRule;


public class ICalendarRule {

	private final RRule rRule;
	
	public ICalendarRule(VEvent vEvent) {
		this.rRule = (RRule) vEvent.getProperties().getProperty(Property.RRULE);
	}
	
	public String frequency() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				return recur.getFrequency();
			}
		}
		return null;
	}

	public Integer interval() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				return recur.getInterval();
			}
		}
		return null;
	}

	public Long until() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null && recur.getUntil() != null) {
				return recur.getUntil().getTime();
			}
		}
		return null;
	}
	
	public WeekDayList dayList() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				return recur.getDayList();
			}
		}
		return null;
	}
	
	public Integer byMonthDay() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				Collection<Integer> monthDayList = recur.getMonthDayList();
				if (monthDayList != null && !monthDayList.isEmpty()) {
					return Iterables.getOnlyElement(monthDayList);
				}
			}
		}
		return null;
	}
	
	public Integer byMonth() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				Collection<Integer> monthList = recur.getMonthList();
				if (monthList != null && !monthList.isEmpty()) {
					return Iterables.getOnlyElement(monthList);
				}
			}
		}
		return null;
	}

	public Integer bySetPos() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				Collection<Integer> setPosList = recur.getSetPosList();
				if (setPosList != null && !setPosList.isEmpty()) {
					return Iterables.getOnlyElement(setPosList);
				}
			}
		}
		return null;
	}
	
	public Integer count() {
		if (rRule != null) {
			Recur recur = rRule.getRecur();
			if (recur != null) {
				return recur.getCount();
			}
		}
		return null;
	}
	
	public boolean isRRule() {
		return rRule != null;
	}
}
