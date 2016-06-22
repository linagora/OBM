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
package org.obm.domain.dao.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.utils.DBUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class ResourceLoader {

	public static class Builder {
		private final Set<Integer> ids;
		private final Set<String> emails;
		private Connection conn;

		private Builder() {
			ids = Sets.newHashSet();
			emails = Sets.newHashSet();
			conn = null;
		}

		public Builder connection(Connection conn) {
			this.conn = conn;
			return this;
		}

		public Builder ids(int... ids) {
			for (int id : ids) {
				this.ids.add(Integer.valueOf(id));
			}
			return this;
		}

		public Builder emails(String... emails) {
			for (String email : emails) {
				this.emails.add(email);
			}
			return this;
		}

		public ResourceLoader build() {
			Preconditions.checkState(conn != null, "The connection parameter is mandatory");
			Preconditions.checkState(!ids.isEmpty() || !emails.isEmpty(),
					"Either the emails or the ids parameter must be present");
			return new ResourceLoader(conn, ids, emails);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private final Connection conn;
	private final Set<Integer> ids;
	private final Set<String> emails;

	private ResourceLoader(Connection conn, Set<Integer> ids, Set<String> emails) {
		this.conn = conn;
		this.ids = ids;
		this.emails = emails;
	}

	public Collection<ResourceInfo> load() throws SQLException {
		IntegerSQLCollectionHelper idsHelper;
		if (!ids.isEmpty()) {
			idsHelper = new IntegerSQLCollectionHelper(ids);
		} else {
			idsHelper = null;
		}
		StringSQLCollectionHelper emailsHelper;
		if (!emails.isEmpty()) {
			emailsHelper = new StringSQLCollectionHelper(emails);
		}
		else {
			emailsHelper = null;
		}
		String query = buildQuery(idsHelper, emailsHelper);
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			stat = conn.prepareStatement(query);
			setParameters(stat, idsHelper, emailsHelper);
			rs = stat.executeQuery();
			return buildResources(rs);
		} finally {
			DBUtils.cleanup(stat, rs);
		}
	}

	private String buildQuery(IntegerSQLCollectionHelper idsHelper,
			StringSQLCollectionHelper emailsHelper) {
		Collection<String> filters = buildFilters(idsHelper, emailsHelper);
		return String.format("SELECT r.resource_id, r.resource_name, r.resource_email, d.domain_name " +
				"FROM Resource r JOIN Domain d ON r.resource_domain_id=d.domain_id " +
				"WHERE %s", Joiner.on(" AND ").join(filters));
	}

	private Collection<String> buildFilters(IntegerSQLCollectionHelper idsHelper,
			StringSQLCollectionHelper emailsHelper) {
		Collection<String> filters = Lists.newArrayList();
		if (idsHelper != null) {
			filters.add(String.format("r.resource_id IN (%s)", idsHelper.asPlaceHolders()));
		}
		if (emailsHelper != null) {
			filters.add(String.format("r.resource_email IN (%s)", emailsHelper.asPlaceHolders()));
		}
		return filters;
	}

	private void setParameters(PreparedStatement stat, IntegerSQLCollectionHelper idsHelper,
			StringSQLCollectionHelper emailsHelper) throws SQLException {
		int parameterCount = 1;
		if (idsHelper != null) {
			parameterCount = idsHelper.insertValues(stat, parameterCount);
		}
		if (emailsHelper != null) {
			parameterCount = emailsHelper.insertValues(stat, parameterCount);
		}
	}

	private Collection<ResourceInfo> buildResources(ResultSet rs) throws SQLException {
		Collection<ResourceInfo> resources = Lists.newArrayList();
		while (rs.next()) {
			ResourceInfo resource = buildResource(rs);
			resources.add(resource);
		}
		return resources;
	}

	private ResourceInfo buildResource(ResultSet rs) throws SQLException {
		return ResourceInfo.builder().id(rs.getInt(1)).name(rs.getString(2)).mail(rs.getString(3))
				.read(false).write(false).domainName(rs.getString(4)).build();
	}
}
