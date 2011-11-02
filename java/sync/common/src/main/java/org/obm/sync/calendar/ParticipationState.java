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

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.obm.sync.base.ObmDbType;

public enum ParticipationState {

	// grr, '-' in java enums not allowed
	NEEDSACTION, ACCEPTED, DECLINED, TENTATIVE, DELEGATED, COMPLETED, INPROGRESS;

	public Object getJdbcObject(ObmDbType type) throws SQLException {
		if (type == ObmDbType.PGSQL) {
			try {
				Object o = Class.forName("org.postgresql.util.PGobject")
						.newInstance();
				Method setType = o.getClass()
						.getMethod("setType", String.class);
				Method setValue = o.getClass().getMethod("setValue",
						String.class);

				setType.invoke(o, "vpartstat");
				setValue.invoke(o, toString());
				return o;
			} catch (Throwable e) {
				throw new SQLException(e.getMessage(), e);
			}
		}
		return toString();
	}

	public static final ParticipationState getValueOf(String s) {
		if ("NEEDS-ACTION".equals(s)) {
			return NEEDSACTION;
		} else if ("IN-PROGRESS".equals(s)) {
			return INPROGRESS;
		} else {
			try {
				return ParticipationState.valueOf(s);
			} catch (IllegalArgumentException iae) {
				return ACCEPTED;
			}
		}
	}

	@Override
	public String toString() {
		if (NEEDSACTION == this) {
			return "NEEDS-ACTION";
		} else if (INPROGRESS == this) {
			return "IN-PROGRESS";
		} else {
			return super.toString();
		}
	}
}
