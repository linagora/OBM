/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun.Fields;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun.Types;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

class SelectArchiveTreatmentQueryBuilder {

	private final ObmHelper obmHelper;
	private ArchiveTreatmentRunId runId;
	private List<ArchiveStatus> statuses;
	private ObmDomainUuid domainId;
	private Ordering ordering;
	private String orderingField;
	private Integer limit;
	private List<String> fields;

	SelectArchiveTreatmentQueryBuilder(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
		ordering = Ordering.NONE;
		statuses = ImmutableList.of();
		fields = MailArchiveRun.Fields.ALL;
	}
	
	SelectArchiveTreatmentQueryBuilder where(ArchiveTreatmentRunId runId) {
		Preconditions.checkNotNull(runId);
		this.runId = runId;
		return this;
	}
	
	SelectArchiveTreatmentQueryBuilder where(ArchiveStatus... status) {
		Preconditions.checkNotNull(status);
		this.statuses = ImmutableList.copyOf(status);
		return this;
	}
	
	SelectArchiveTreatmentQueryBuilder where(ObmDomainUuid domainId) {
		Preconditions.checkNotNull(domainId);
		this.domainId = domainId;
		return this;
	}
	
	SelectArchiveTreatmentQueryBuilder orderBy(String orderingField, Ordering ordering) {
		Preconditions.checkNotNull(orderingField);
		Preconditions.checkNotNull(ordering);
		Preconditions.checkArgument(ordering != Ordering.NONE);
		this.ordering = ordering;
		this.orderingField = orderingField;
		return this;
	}
	
	SelectArchiveTreatmentQueryBuilder limit(int limit) {
		this.limit = limit;
		return this;
	}

	private List<String> filters() {
		ImmutableList.Builder<String> filters = ImmutableList.builder();
		if (runId != null) {
			filters.add(Fields.UUID + " = ?");
		}
		if (domainId != null) {
			filters.add(Fields.DOMAIN_UUID + " = ?");
		}
		if (!statuses.isEmpty()) {
			filters.add(String.format(Fields.STATUS + " IN (%s)", Joiner.on(',').join(Collections.nCopies(statuses.size(), "?"))));
		}
		return filters.build();
	}
	
	@VisibleForTesting String buildQueryString() {
		StringBuilder queryString = new StringBuilder("SELECT ").append(fields()).append(" FROM ").append(MailArchiveRun.NAME);
		List<String> filters = filters();
		if (!filters.isEmpty()) {
			queryString.append(" WHERE ").append(Joiner.on(" AND ").join(filters));
		}
		if (ordering != Ordering.NONE) {
			queryString.append(" ORDER BY ").append(orderingField).append(" ").append(ordering.name());
		}
		if (limit != null) {
			queryString.append(" LIMIT ").append(limit);
		}
		return queryString.toString();
	}
	
	private String fields() {
		return Joiner.on(", ").join(fields);
	}

	PreparedStatement prepareStatement(Connection connection) throws SQLException {
		Preconditions.checkNotNull(connection);
		PreparedStatement statement = connection.prepareStatement(buildQueryString());
		int fieldPosition = 1;
		if (runId != null) {
			statement.setString(fieldPosition++, runId.serialize());
		}
		if (domainId != null) {
			statement.setString(fieldPosition++, domainId.get());
		}
		if (statuses != null) {
			for (ArchiveStatus archiveStatus : statuses) {
				statement.setObject(fieldPosition++, obmHelper.getDBCP().getJdbcObject(Types.STATUS, archiveStatus.asSpecificationValue()));
			}
		}
		return statement;
	}
	
}