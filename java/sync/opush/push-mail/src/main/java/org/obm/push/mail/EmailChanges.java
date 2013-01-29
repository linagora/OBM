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
package org.obm.push.mail;

import java.io.Serializable;
import java.util.Set;

import org.obm.push.mail.bean.Email;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class EmailChanges implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
	
		private Set<Email> deletions;
		private Set<Email> changes;
		private Set<Email> additions;
		
		private Builder() {
			super();
		}

		public Builder deletions(Set<Email> deletions) {
			this.deletions = deletions;
			return this;
		}

		public Builder changes(Set<Email> changes) {
			this.changes = changes;
			return this;
		}

		public Builder additions(Set<Email> additions) {
			this.additions = additions;
			return this;
		}
		
		public EmailChanges build() {
			if (deletions == null) {
				deletions = ImmutableSet.<Email>of();
			}
			if (changes == null) {
				changes = ImmutableSet.<Email>of();
			}
			if (additions == null) {
				additions = ImmutableSet.<Email>of();
			}
			return new EmailChanges(deletions, changes, additions);
		}
	}

	private final Set<Email> deletions;
	private final Set<Email> changes;
	private final Set<Email> additions;
	
	private EmailChanges(Set<Email> deletions, Set<Email> changes, Set<Email> additions) {
		this.deletions = deletions;
		this.changes = changes;
		this.additions = additions;
	}
	
	public Set<Email> deletions() {
		return deletions;
	}

	public Set<Email> changes() {
		return changes;
	}

	public Set<Email> additions() {
		return additions;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(deletions, changes, additions);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EmailChanges) {
			EmailChanges other = (EmailChanges) obj;
			return Objects.equal(this.deletions, other.deletions)
				&& Objects.equal(this.changes, other.changes)
				&& Objects.equal(this.additions, other.additions);
		}
		return false;
	}
}
