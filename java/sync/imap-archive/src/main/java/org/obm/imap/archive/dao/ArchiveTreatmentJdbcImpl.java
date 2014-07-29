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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.obm.ElementNotFoundException;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.ArchiveTreatmentJdbcImpl.TABLE.FIELDS;
import org.obm.imap.archive.dao.ArchiveTreatmentJdbcImpl.TABLE.TYPES;
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
	
	interface TABLE {
		
		String NAME = "mail_archive_run";

		interface TYPES {
			String STATUS = "mail_archive_status";
		}
		
		interface FIELDS {
			String UUID = "mail_archive_run_uuid";
			String DOMAIN_UUID = "mail_archive_run_domain_uuid";
			String STATUS = "mail_archive_run_status";
			String SCHEDULE = "mail_archive_run_schedule";
			String START = "mail_archive_run_start";
			String END = "mail_archive_run_end";
			String HIGHER_BOUNDARY = "mail_archive_run_higher_boundary";
			
			String ALL = Joiner.on(", ").join(UUID, DOMAIN_UUID, STATUS, SCHEDULE, START, END, HIGHER_BOUNDARY);
		}
	}
	
	interface REQUESTS {
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?)", TABLE.NAME, FIELDS.ALL);

		String UPDATE = String.format(
				"UPDATE %s SET (%s) = (?, ?, ?, ?, ?, ?, ?) WHERE %s = ?", TABLE.NAME, FIELDS.ALL, FIELDS.UUID);
		
		String REMOVE = String.format(
				"DELETE FROM %s WHERE %s = ?", TABLE.NAME, FIELDS.UUID);

		String SELECT_SCHEDULED_OR_RUNNING = String.format(
				"SELECT %s FROM %s WHERE %s IN('%s', '%s') ORDER BY %s ASC", 
				FIELDS.ALL, TABLE.NAME, FIELDS.STATUS, ArchiveStatus.SCHEDULED, ArchiveStatus.RUNNING, FIELDS.SCHEDULE);
		
		String SELECT_LAST = String.format(
				"SELECT %s FROM %s WHERE %s = ? ORDER BY %s ASC LIMIT ?", 
				FIELDS.ALL, TABLE.NAME, FIELDS.DOMAIN_UUID, FIELDS.SCHEDULE);
		
		String SELECT_BY_RUN_ID = String.format(
				"SELECT %s FROM %s WHERE %s = ?", 
				FIELDS.ALL, TABLE.NAME, FIELDS.UUID);
		
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
			ps.setObject(idx++, obmHelper.getDBCP().getJdbcObject(TYPES.STATUS, treatment.getArchiveStatus().asSpecificationValue()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getScheduledTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getStartTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getEndTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getHigherBoundary()));

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
			ps.setObject(idx++, obmHelper.getDBCP().getJdbcObject(TYPES.STATUS, treatment.getArchiveStatus().asSpecificationValue()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getScheduledTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getStartTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getEndTime()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(treatment.getHigherBoundary()));
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
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT_SCHEDULED_OR_RUNNING)) {

			ImmutableList.Builder<ArchiveTreatment> list = ImmutableList.builder();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(treatmentFromResultSet(rs));
			}
			return list.build();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public List<ArchiveTreatment> findByScheduledTime(ObmDomainUuid domain, int limit) throws DaoException {
		Preconditions.checkArgument(limit > 0);
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT_LAST)) {

			ps.setString(1, domain.get());
			ps.setInt(2, limit);
			
			ImmutableList.Builder<ArchiveTreatment> list = ImmutableList.builder();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				list.add(treatmentFromResultSet(rs));
			}
			return list.build();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public Optional<ArchiveTreatment> find(ArchiveTreatmentRunId runId) throws DaoException {
		Preconditions.checkNotNull(runId);
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT_BY_RUN_ID)) {

			ps.setString(1, runId.serialize());

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
		ObmDomainUuid domain = ObmDomainUuid.of(rs.getString(FIELDS.DOMAIN_UUID));
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from(rs.getString(FIELDS.UUID));
		ArchiveStatus status = ArchiveStatus.fromSpecificationValue(rs.getString(FIELDS.STATUS));
		DateTime scheduleTime = JDBCUtils.getDateTime(rs, FIELDS.SCHEDULE, DateTimeZone.UTC);
		DateTime startTime = JDBCUtils.getDateTime(rs, FIELDS.START, DateTimeZone.UTC);
		DateTime endTime = JDBCUtils.getDateTime(rs, FIELDS.END, DateTimeZone.UTC);
		DateTime higherBoundary = JDBCUtils.getDateTime(rs, FIELDS.HIGHER_BOUNDARY, DateTimeZone.UTC);
		
		return ArchiveTreatment
				.builder(domain)
				.runId(runId)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduleTime)
				.startedAt(startTime)
				.terminatedAt(endTime)
				.status(status)
				.build();
	}
}
