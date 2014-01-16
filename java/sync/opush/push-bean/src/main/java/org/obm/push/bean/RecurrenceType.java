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

import org.obm.push.exception.ConversionException;

import com.google.common.base.Objects;

/**
 * This enum is serialized, take care of changes done there for older version compatibility
 */
public enum RecurrenceType {
	DAILY(999), // 0
	WEEKLY(99), // 1
	MONTHLY(99), // 2
	MONTHLY_NDAY(99), // 3
	YEARLY(1), // 5
	YEARLY_NDAY(1); // 6

	private final int maxIntervalValue;

	private RecurrenceType(int maxIntervalValue) {
		this.maxIntervalValue = maxIntervalValue;
	}
	
	public int validIntervalOrException(Integer interval) throws ConversionException {
		int recurrentInterval = Objects.firstNonNull(interval, 1);
		
		if (maxIntervalValue < recurrentInterval) {
			String msg = String.format("Recurrence.Interval is higher than accepted value. " +
					"Type:%s MaxInterval:%d Interval:%d", name(), maxIntervalValue, recurrentInterval);
			throw new ConversionException(msg);
		}
		return recurrentInterval;
	}
	
	public String asIntString() {
		switch (this) {
		case DAILY:
			return "0";
		case MONTHLY:
			return "2";
		case MONTHLY_NDAY:
			return "3";
		case YEARLY:
			return "5";
		case YEARLY_NDAY:
			return "6";
		default:
		case WEEKLY:
			return "1";

		}
	}
	
}
