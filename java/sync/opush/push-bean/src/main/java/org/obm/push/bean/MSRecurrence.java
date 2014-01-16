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
package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import com.google.common.base.Objects;


public class MSRecurrence implements Serializable {
	
	private static final long serialVersionUID = 8025103607638284568L;
	
	private Date until;
	private RecurrenceType type;
	private Integer weekOfMonth;
	private Integer monthOfYear;
	private Integer dayOfMonth;
	private Integer occurrences;
	private Integer interval;
	private Set<RecurrenceDayOfWeek> dayOfWeek;
	
	/*Task Attribut*/
	private Date start;
	private Boolean regenerate;
	private Boolean deadOccur;
	
	
	public RecurrenceType getType() {
		return type;
	}
	public void setType(RecurrenceType type) {
		this.type = type;
	}
	public Integer getWeekOfMonth() {
		return weekOfMonth;
	}
	public void setWeekOfMonth(Integer weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
	}
	public Integer getMonthOfYear() {
		return monthOfYear;
	}
	public void setMonthOfYear(Integer monthOfYear) {
		this.monthOfYear = monthOfYear;
	}
	public Integer getDayOfMonth() {
		return dayOfMonth;
	}
	public void setDayOfMonth(Integer dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	public Integer getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(Integer occurrences) {
		this.occurrences = occurrences;
	}
	public Integer getInterval() {
		return interval;
	}
	public void setInterval(Integer interval) {
		this.interval = interval;
	}
	public Set<RecurrenceDayOfWeek> getDayOfWeek() {
		return dayOfWeek;
	}
	public void setDayOfWeek(Set<RecurrenceDayOfWeek> dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	public Date getUntil() {
		return until;
	}
	public void setUntil(Date until) {
		this.until = until;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Boolean getRegenerate() {
		return regenerate;
	}
	public void setRegenerate(Boolean regenerate) {
		this.regenerate = regenerate;
	}
	public Boolean getDeadOccur() {
		return deadOccur;
	}
	public void setDeadOccur(Boolean deadOccur) {
		this.deadOccur = deadOccur;
	}

	public boolean hasOccurences() {
		return occurrences != null && occurrences > 0;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(until, type, weekOfMonth, monthOfYear, dayOfMonth, 
				occurrences, interval, dayOfWeek, start, regenerate, deadOccur);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSRecurrence) {
			MSRecurrence that = (MSRecurrence) object;
			return Objects.equal(this.until, that.until)
				&& Objects.equal(this.type, that.type)
				&& Objects.equal(this.weekOfMonth, that.weekOfMonth)
				&& Objects.equal(this.monthOfYear, that.monthOfYear)
				&& Objects.equal(this.dayOfMonth, that.dayOfMonth)
				&& Objects.equal(this.occurrences, that.occurrences)
				&& Objects.equal(this.interval, that.interval)
				&& Objects.equal(this.dayOfWeek, that.dayOfWeek)
				&& Objects.equal(this.start, that.start)
				&& Objects.equal(this.regenerate, that.regenerate)
				&& Objects.equal(this.deadOccur, that.deadOccur);
		}
		return false;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("until", until)
			.add("type", type)
			.add("weekOfMonth", weekOfMonth)
			.add("monthOfYear", monthOfYear)
			.add("dayOfMonth", dayOfMonth)
			.add("occurrences", occurrences)
			.add("interval", interval)
			.add("dayOfWeek", dayOfWeek)
			.add("start", start)
			.add("regenerate", regenerate)
			.add("deadOccur", deadOccur)
			.toString();
	}
	
}
