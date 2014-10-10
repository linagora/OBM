/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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
package org.obm.imap.archive.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.ElementNotFoundException;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun.Fields;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun.Types;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveTreatmentJdbcImpl implements ArchiveTreatmentDao {

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;
	
	interface REQUESTS {
		
		String ALL = Joiner.on(", ").join(Fields.ALL);
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", MailArchiveRun.NAME, ALL);

		String UPDATE = String.format(
				"UPDATE %s SET (%s) = (?, ?, ?, ?, ?, ?, ?, ?) WHERE %s = ?", MailArchiveRun.NAME, ALL, Fields.UUID);
		
		String REMOVE = String.format(
				"DELETE FROM %s WHERE %s = ?", MailArchiveRun.NAME, Fields.UUID);

		
	}
	
	private SelectArchiveTreatmentQueryBuilder selectArchiveTreatment() {
		return new SelectArchiveTreatmentQueryBuilder(obmHelper);
	}
	
	@Inject
	@VisibleForTesting ArchiveTreatmentJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
	}

	@Override
	public void insert(ArchiveTreatment treatment) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.INSERT)) {

			int idx = 1;
			ps.setString(idx++, treatment.getRunId().serialize());
			ps.setString(idx++, treatment.getDomainUuid().get());
			ps.setObject(idx++, obmHelper.getDBCP().getJdbcObject(Types.STATUS, treatment.getArchiveStatus().asSpecificationValue()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getScheduledTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getStartTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getEndTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getHigherBoundary()));
			ps.setBoolean(idx++, treatment.isRecurrent());

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void update(ArchiveTreatment treatment) throws DaoException, ElementNotFoundException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.UPDATE)) {

			int idx = 1;
			ps.setString(idx++, treatment.getRunId().serialize());
			ps.setString(idx++, treatment.getDomainUuid().get());
			ps.setObject(idx++, obmHelper.getDBCP().getJdbcObject(Types.STATUS, treatment.getArchiveStatus().asSpecificationValue()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getScheduledTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getStartTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getEndTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getHigherBoundary()));
			ps.setBoolean(idx++, treatment.isRecurrent());
			ps.setString(idx++, treatment.getRunId().serialize());

			if (ps.executeUpdate() == 0) {
				throw new ElementNotFoundException(String.format(
						"Treatment with UUID %s not found", treatment.getRunId().serialize()));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void remove(ArchiveTreatmentRunId runId) throws DaoException, ElementNotFoundException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.REMOVE)) {

			ps.setString(1, runId.serialize());

			if (ps.executeUpdate() == 0) {
				throw new ElementNotFoundException(String.format(
						"Treatment with UUID %s not found", runId.serialize()));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<ArchiveTreatment> findAllScheduledOrRunning() throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = selectArchiveTreatment()
					.where(ArchiveStatus.SCHEDULED_OR_RUNNING)
					.orderBy(Fields.SCHEDULE, Ordering.ASC)
					.prepareStatement(connection)) {

			return toList(ps.executeQuery());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<ArchiveTreatment> findByScheduledTime(ObmDomainUuid domain, Limit limit) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = 
						selectArchiveTreatment()
							.where(domain)
							.limit(limit)
							.orderBy(Fields.SCHEDULE, Ordering.DESC)
							.prepareStatement(connection)) {

			return toList(ps.executeQuery());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<ArchiveTreatment> findLastTerminated(ObmDomainUuid domain, Limit max) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = 
						selectArchiveTreatment()
							.where(domain)
							.where(ArchiveStatus.TERMINATED)
							.limit(max)
							.orderBy(Fields.SCHEDULE, Ordering.DESC)
							.prepareStatement(connection)) {

			return toList(ps.executeQuery());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private List<ArchiveTreatment> toList(ResultSet rs) throws SQLException {
		ImmutableList.Builder<ArchiveTreatment> list = ImmutableList.builder();
		while (rs.next()) {
			list.add(treatmentFromResultSet(rs));
		}
		return list.build();
	}

	@Override
	public Optional<ArchiveTreatment> find(ArchiveTreatmentRunId runId) throws DaoException {
		Preconditions.checkNotNull(runId);
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = selectArchiveTreatment().where(runId).prepareStatement(connection)) {

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return Optional.of(treatmentFromResultSet(rs));
			}
			return Optional.absent();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private ArchiveTreatment treatmentFromResultSet(ResultSet rs) throws SQLException {
		ObmDomainUuid domain = ObmDomainUuid.of(rs.getString(Fields.DOMAIN_UUID));
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from(rs.getString(Fields.UUID));
		ArchiveStatus status = ArchiveStatus.fromSpecificationValue(rs.getString(Fields.STATUS));
		boolean isRecurrent = rs.getBoolean(Fields.RECURRENT);
		DateTime scheduleTime = JDBCUtils.getDateTime(rs, Fields.SCHEDULE, DateTimeZone.UTC);
		DateTime startTime = JDBCUtils.getDateTime(rs, Fields.START, DateTimeZone.UTC);
		DateTime endTime = JDBCUtils.getDateTime(rs, Fields.END, DateTimeZone.UTC);
		DateTime higherBoundary = JDBCUtils.getDateTime(rs, Fields.HIGHER_BOUNDARY, DateTimeZone.UTC);
		
		return ArchiveTreatment
				.builder(domain)
				.runId(runId)
				.higherBoundary(higherBoundary)
				.recurrent(isRecurrent)
				.scheduledAt(scheduleTime)
				.startedAt(startTime)
				.terminatedAt(endTime)
				.status(status)
				.build();
	}

	@Override
	public List<ArchiveTreatment> history(ObmDomainUuid domain, Set<ArchiveStatus> statuses, Limit limit, Ordering ordering) throws DaoException {
		Preconditions.checkNotNull(domain);
		Preconditions.checkNotNull(statuses);
		Preconditions.checkNotNull(limit);
		Preconditions.checkNotNull(ordering);
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = 
						historyQueryBuilder(domain, statuses, limit, ordering).prepareStatement(connection)) {

			return toList(ps.executeQuery());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private SelectArchiveTreatmentQueryBuilder historyQueryBuilder(ObmDomainUuid domain, Set<ArchiveStatus> statuses, Limit limit, Ordering ordering) {
		return selectArchiveTreatment()
			.where(domain)
			.where(statuses)
			.orderBy(Fields.SCHEDULE, ordering)
			.limit(limit);
	}
}
