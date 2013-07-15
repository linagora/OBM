/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ImapPath {

	public final static String PATH_SEPARATOR = "/";

	public static class Builder {

		private String user;
		List<String> pathFragments;

		private Builder() {
			pathFragments = Lists.newArrayList();
		}

		public Builder user(String user) {
			this.user = user;
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
			ImmutableList<String> immutableFragments = ImmutableList
					.copyOf(pathFragments);
			return new ImapPath(user, immutableFragments);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	private String user;
	ImmutableList<String> pathFragments;

	private ImapPath(String user, ImmutableList<String> pathFragments) {
		this.user = user;
		this.pathFragments = pathFragments;
	}

	public String getUser() {
		return user;
	}

	public ImmutableList<String> getPathFragments() {
		return pathFragments;
	}

	public String format() {
		if (pathFragments.size() > 0) {
			String path = StringUtils.join(pathFragments, PATH_SEPARATOR);
			return String.format("user%s%s%s%s", PATH_SEPARATOR, user,
					PATH_SEPARATOR, path);
		} else {
			return String.format("user%s%s", PATH_SEPARATOR, user);
		}
	}
}
