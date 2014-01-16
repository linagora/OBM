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
package org.obm.sync.calendar;

import java.io.Serializable;

import org.obm.push.utils.UUIDFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class EventExtId implements Serializable {

	@Singleton
	public static class Factory {
		
		private final UUIDFactory uuidFactory;

		@Inject
		@VisibleForTesting Factory(UUIDFactory uuidFactory) {
			this.uuidFactory = uuidFactory;
		}
		
		public EventExtId create(String extId) {
			return new EventExtId(extId);
		}
		
		public EventExtId generate() {
			return new EventExtId(uuidFactory.randomUUID().toString());
		}
	}
	
	private final String extId;
	
	public EventExtId(String extId) {
		this.extId = Strings.emptyToNull(extId);
	}
	
	public String getExtId() {
		return extId;
	}

	public String serializeToString() {
		return extId;
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EventExtId) {
			EventExtId other = (EventExtId) obj;
			return Objects.equal(extId, other.extId);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(extId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("extId", extId)
			.toString();
	}
	
}
