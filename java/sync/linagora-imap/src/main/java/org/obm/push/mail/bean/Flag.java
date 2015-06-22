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

package org.obm.push.mail.bean;

import java.util.Arrays;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;


public class Flag {

	public static final Flag SEEN = new Flag("Seen", true); 
	public static final Flag DRAFT = new Flag("Draft", true); 
	public static final Flag DELETED = new Flag("Deleted", true); 
	public static final Flag FLAGGED = new Flag("Flagged", true); 
	public static final Flag ANSWERED = new Flag("Answered", true);
	public static final Flag[] FLAGS = new Flag[] { SEEN, DRAFT, DELETED, FLAGGED, ANSWERED };

	public static Flag from(String value) {
		Optional<Flag> systemFlag = isSystemFlag(value);
		if (systemFlag.isPresent()) {
			return systemFlag.get();
		}
		return new Flag(value, false);
	}

	private static Optional<Flag> isSystemFlag(final String value) {
		return FluentIterable.from(Arrays.asList(FLAGS))
				.firstMatch(new Predicate<Flag>() {

					@Override
					public boolean apply(Flag flag) {
						return flag.get().equalsIgnoreCase(value);
					}
				});
	}

	private final String value;
	private final boolean system;
	
	private Flag(String value, boolean system) {
		this.value = value;
		this.system = system;
	}
	
	public String get() {
		return value;
	}
	
	@VisibleForTesting boolean isSystem() {
		return system;
	}
	
	public String asCommandValue() {
		return (system ? "\\" : "") + value;
	}

	public static Flag[] values() {
		return FLAGS;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(value, system);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Flag) {
			Flag that = (Flag) object;
				return Objects.equal(this.value, that.value)
					&& Objects.equal(this.system, that.system);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("value", value)
			.add("system", system)
			.toString();
	}
}