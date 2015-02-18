/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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
package fr.aliacom.obm.common.user;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class UserNomad {

	private static final UserNomad EMPTY = builder().build();

	public static class Builder {

		private Boolean enabled;
		private String email;
		private Boolean allowed;
		private Boolean localCopy;

		private Builder() {}

		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder allowed(boolean allowed) {
			this.allowed = allowed;
			return this;
		}

		public Builder localCopy(boolean localCopy) {
			this.localCopy = localCopy;
			return this;
		}

		public Builder from(UserNomad userNomad) {
			this.enabled = userNomad.enabled;
			this.email = userNomad.email;
			this.allowed = userNomad.allowed;
			this.localCopy = userNomad.localCopy;
			return this;
		}

		public UserNomad build() {
			this.enabled = Objects.firstNonNull(this.enabled, false);
			this.email = Strings.emptyToNull(email);
			this.allowed = Objects.firstNonNull(this.allowed, false);
			this.localCopy = Objects.firstNonNull(this.localCopy, false);
			// In theory, should ensure that enable is only on if email is not null, but who knows what crap we'll find in the database?
			// This may prevent us from loading existing users
			return new UserNomad(enabled, email, allowed, localCopy);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static UserNomad empty() {
		return EMPTY;
	}
	
	private final boolean enabled;
	private final String email;
	private final boolean allowed;
	private final boolean localCopy;

	private UserNomad(boolean enabled, String email, boolean allowed, boolean localCopy) {
		this.enabled = enabled;
		this.email = email;
		this.allowed = allowed;
		this.localCopy = localCopy;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getEmail() {
		return email;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public boolean hasLocalCopy() {
		return localCopy;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(enabled, email, allowed, localCopy);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof UserNomad) {
			UserNomad that = (UserNomad) object;
			return Objects.equal(this.enabled, that.enabled)
					&& Objects.equal(this.email, that.email)
					&& Objects.equal(this.allowed, that.allowed)
					&& Objects.equal(this.localCopy, that.localCopy);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("enabled", enabled)
				.add("email", email)
				.add("allowed", allowed)
				.add("localCopy", localCopy)
				.toString();
	}

}
