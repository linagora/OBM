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
import java.util.UUID;

import org.joda.time.LocalTime;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.push.utils.JDBCUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class DomainConfigurationJdbcImpl implements DomainConfigurationDao {

	private final DatabaseConnectionProvider dbcp;
	private static final String TABLE = "mail_archive";
	private static final String FIELDS = 
			"mail_archive_domain_id, " +
			"mail_archive_activated, " +
			"mail_archive_repeat_kind, " +
			"mail_archive_day_of_week, " +
			"mail_archive_day_of_month, " +
			"mail_archive_day_of_year, " +
			"mail_archive_hour, " +
			"mail_archive_minute ";
	
	@Inject
	@VisibleForTesting DomainConfigurationJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public DomainConfiguration getDomainConfiguration(ObmDomain domain) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM " + TABLE + " " +
					"WHERE mail_archive_domain_id = ?");

			ps.setInt(1, domain.getId());

			rs = ps.executeQuery();

			UUID domainUUId = domain.getUuid().getUUID();
			if (rs.next()) {
				return domainConfigurationFromCursor(rs, domainUUId);
			} else {
				return null;
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}
	}

	private DomainConfiguration domainConfigurationFromCursor(ResultSet rs, UUID domainUUId) throws SQLException {
		return DomainConfiguration.builder()
				.domainId(domainUUId)
				.enabled(rs.getBoolean("mail_archive_activated"))
				.recurrence(ArchiveRecurrence.builder()
						.dayOfMonth(DayOfMonth.of(rs.getInt("mail_archive_day_of_month")))
						.dayOfWeek(DayOfWeek.fromSpecificationValue(rs.getInt("mail_archive_day_of_week")))
						.dayOfYear(DayOfYear.of(rs.getInt("mail_archive_day_of_year")))
						.repeat(RepeatKind.valueOf(rs.getString("mail_archive_repeat_kind")))
						.build())
				.time(new LocalTime(rs.getInt("mail_archive_hour"), rs.getInt("mail_archive_minute")))
				.build();
	}

	@Override
	public void updateDomainConfiguration(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"UPDATE " + TABLE +
					" SET mail_archive_activated = ?, mail_archive_repeat_kind = ?, " + 
					"mail_archive_day_of_week = ?, mail_archive_day_of_month = ?, mail_archive_day_of_year = ?, mail_archive_hour = ?, mail_archive_minute = ?" +
					" WHERE  mail_archive_domain_id = (SELECT domain_id FROM Domain WHERE domain_uuid = ?)");

			int idx = 1;
			ps.setBoolean(idx++, domainConfiguration.isEnabled());
			ps.setString(idx++, domainConfiguration.getRepeatKind().name());
			ps.setInt(idx++, domainConfiguration.getDayOfWeek().getSpecificationValue());
			ps.setInt(idx++, domainConfiguration.getDayOfMonth().getDayIndex());
			ps.setInt(idx++, domainConfiguration.getDayOfYear().getDayOfYear());
			ps.setInt(idx++, domainConfiguration.getHour());
			ps.setInt(idx++, domainConfiguration.getMinute());
			ps.setString(idx++, domainConfiguration.getDomainId().toString());

			if (ps.executeUpdate() < 1) {
				throw new DomainNotFoundException(domainConfiguration.getDomainId());
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}
}
