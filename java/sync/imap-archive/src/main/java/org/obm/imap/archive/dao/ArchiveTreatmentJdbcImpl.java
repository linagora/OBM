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

import org.joda.time.DateTimeZone;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.ArchiveTreatmentJdbcImpl.TABLE.FIELDS;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveTreatmentJdbcImpl implements ArchiveTreatmentDao {

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;
	
	interface TABLE {
		
		String NAME = "mail_archive_run";
		
		interface FIELDS {
			String UUID = "mail_archive_run_uuid";
			String DOMAIN_UUID = "mail_archive_run_domain_uuid";
			String STATUS = "mail_archive_run_status";
			String START = "mail_archive_run_start";
			String END = "mail_archive_run_end";
			String LOWER_BOUNDARY = "mail_archive_run_lower_boundary";
			String HIGHER_BOUNDARY = "mail_archive_run_higher_boundary";
			
			String ALL = Joiner.on(", ").join(UUID, DOMAIN_UUID, STATUS, START, END, LOWER_BOUNDARY, HIGHER_BOUNDARY);
		}
	}
	
	interface REQUESTS {
		
		String SELECT_ORDERED = String.format(
				"SELECT %s FROM %s WHERE %s = ? ORDER BY id DESC", FIELDS.ALL, TABLE.NAME, FIELDS.DOMAIN_UUID);
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?)", TABLE.NAME, FIELDS.ALL);
	}
	
	@Inject
	@VisibleForTesting ArchiveTreatmentJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
	}

	@Override
	public Optional<ArchiveTreatment> getLastArchiveTreatment(ObmDomainUuid domainId) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT_ORDERED)) {

			ps.setString(1, domainId.toString());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return Optional.of(archiveTreatmentFromResultSet(rs));
			} else {
				return Optional.absent();
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private ArchiveTreatment archiveTreatmentFromResultSet(ResultSet rs) throws SQLException {
		return ArchiveTreatment.builder()
				.runId(ArchiveTreatmentRunId.from(rs.getString(FIELDS.UUID)))
				.domainId(ObmDomainUuid.of(rs.getString(FIELDS.DOMAIN_UUID)))
				.archiveStatus(ArchiveStatus.valueOf(rs.getString(FIELDS.STATUS)))
				.start(JDBCUtils.getDateTime(rs, FIELDS.START, DateTimeZone.UTC))
				.end(JDBCUtils.getDateTime(rs, FIELDS.END, DateTimeZone.UTC))
				.lowerBoundary(JDBCUtils.getDateTime(rs, FIELDS.LOWER_BOUNDARY, DateTimeZone.UTC))
				.higherBoundary(JDBCUtils.getDateTime(rs, FIELDS.HIGHER_BOUNDARY, DateTimeZone.UTC))
				.build();
	}

	@Override
	public void insert(ArchiveTreatment archiveTreatment) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.INSERT)) {

			int idx = 1;
			ps.setString(idx++, archiveTreatment.getRunId().serialize());
			ps.setString(idx++, archiveTreatment.getDomainId().get());
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject(FIELDS.STATUS, archiveTreatment.getArchiveStatus().toString()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(archiveTreatment.getStart()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(archiveTreatment.getEnd()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(archiveTreatment.getLowerBoundary()));
			ps.setTimestamp(idx++, JDBCUtils.toTimestamp(archiveTreatment.getHigherBoundary()));

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
}
