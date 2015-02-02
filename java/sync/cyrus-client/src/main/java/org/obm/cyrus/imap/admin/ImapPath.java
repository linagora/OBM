/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.cyrus.imap.admin;

import java.util.List;

import org.parboiled.common.Preconditions;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class ImapPath {

	public final static String PATH_SEPARATOR = "/";
	public final static String DOMAIN_SEPARATOR = "@";

	public static class Builder {

		private String user;
		private String domain;
		ImmutableList.Builder<String> pathFragments;

		private Builder() {
			pathFragments = ImmutableList.builder();
		}

		public Builder user(String user) {
			this.user = user;
			return this;
		}
		
		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}

		public Builder pathFragment(String pathFragment) {
			if (pathFragment.contains(PATH_SEPARATOR)) {
				throw new IllegalArgumentException(String.format(
						"The path fragment %s should not "
								+ "contain the path separator %s",
						pathFragment, PATH_SEPARATOR));
			}
			pathFragments.add(pathFragment);
			return this;
		}

		public ImapPath build() {
			Preconditions.checkState(user != null);
			Preconditions.checkState(domain != null);
			
			return new ImapPath(user, domain, pathFragments.build());
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	final private String user;
	final private String domain;
	final private List<String> pathFragments;

	private ImapPath(String user, String domain, ImmutableList<String> pathFragments) {
		this.user = user;
		this.domain = domain;
		this.pathFragments = pathFragments;
	}

	public String getUser() {
		return user;
	}
	
	public String getDomain() {
		return domain;
	}

	public List<String> getPathFragments() {
		return pathFragments;
	}

	public String format() {
		return Joiner.on(PATH_SEPARATOR)
				.join(Iterables.concat(ImmutableList.of("user", user), pathFragments))
				.concat(DOMAIN_SEPARATOR)
				.concat(domain);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof ImapPath) {
			ImapPath that = (ImapPath) object;

			return Objects.equal(this.user, that.user)
					&& Objects.equal(this.domain, that.domain)
					&& Objects.equal(this.pathFragments, that.pathFragments);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(user, domain, pathFragments);
	}

	@Override
	public String toString() {
		return format();
	}

}
