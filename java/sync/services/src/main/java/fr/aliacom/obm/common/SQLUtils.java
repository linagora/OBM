/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class SQLUtils {

	/**
	 * Utility method building an AND condition for user emails.<br />
	 * The condition is the OR of all user emails using LIKE matching mode.
	 * 
	 * @param calendarEmails The {@link Collection} of user emails.
	 * 
	 * @return The built AND condition.
	 */
	public static String selectCalendarsCondition(Collection<String> calendarEmails) {
		if (calendarEmails == null || calendarEmails.isEmpty()) {
			return "";
		}
		
		StringBuilder builder = new StringBuilder("AND (");
		List<String> userEmails = Collections.nCopies(calendarEmails.size(), "userobm_email LIKE ?");
		
		builder.append(Joiner.on(" OR ").join(userEmails));
		// To close the AND condition
		// The last space(' ') character is important as this method will be called during the creation of a query with multiple
		// conditions and it will expect each condition to end with a space.
		builder.append(") ");
		
		return builder.toString();
	}

	public static String selectUsersMatchingPatternCondition(String pattern) {
		if (Strings.isNullOrEmpty(pattern)) {
			return "";
		}

		return "AND (LOWER(userobm_login) LIKE ? OR LOWER(userobm_lastname) LIKE ? OR LOWER(userobm_firstname) LIKE ?) ";
	}

	public static String selectResourcesMatchingPatternCondition(String pattern) {
		if (Strings.isNullOrEmpty(pattern)) {
			return "";
		}

		return "AND (LOWER(r.resource_name) LIKE ? OR LOWER(r.resource_description) LIKE ?) ";
	}

}
