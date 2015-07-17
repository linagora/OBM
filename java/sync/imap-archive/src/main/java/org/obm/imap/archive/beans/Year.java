/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.beans;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.push.mail.bean.InternalDate;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Year {

	private static final int MINIMAL_YEAR = 1970;
	
	public static Year from(int year) {
		Preconditions.checkArgument(year >= MINIMAL_YEAR);
		return new Year(year);
	}
	
	public static Year from(InternalDate date) {
		Preconditions.checkNotNull(date);
		return new Year(date.getYear());
	}
	
	private final int year;
	
	private Year(int year) {
		this.year = year;
	}

	public int toInt() {
		return year;
	}
	
	public String serialize() {
		return String.valueOf(year);
	}
	
	public Year previous() {
		return Year.from(year - 1);
	}
	
	public Year next() {
		return Year.from(year + 1);
	}
	
	public Date toDate() {
		return new DateTime(DateTimeZone.UTC)
			.withYear(year)
			.withMonthOfYear(1)
			.withDayOfMonth(1)
			.withHourOfDay(0)
			.withMinuteOfHour(0)
			.withSecondOfMinute(0)
			.withMillisOfSecond(0)
			.toDate();
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(year);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Year) {
			Year that = (Year) object;
			return Objects.equal(this.year, that.year);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("year", year)
			.toString();
	}
}
