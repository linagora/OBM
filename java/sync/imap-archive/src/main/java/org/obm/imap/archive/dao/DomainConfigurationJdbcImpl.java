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

import org.joda.time.LocalTime;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class DomainConfigurationJdbcImpl implements DomainConfigurationDao {

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;
	
	public static final String TABLE = "mail_archive";
	public static final String MAIL_ARCHIVE_DOMAIN_UUID = "mail_archive_domain_uuid";
	public static final String MAIL_ARCHIVE_ACTIVATED = "mail_archive_activated";
	public static final String MAIL_ARCHIVE_REPEAT_KIND = "mail_archive_repeat_kind";
	public static final String MAIL_ARCHIVE_DAY_OF_WEEK = "mail_archive_day_of_week";
	public static final String MAIL_ARCHIVE_DAY_OF_MONTH = "mail_archive_day_of_month";
	public static final String MAIL_ARCHIVE_DAY_OF_YEAR = "mail_archive_day_of_year";
	public static final String MAIL_ARCHIVE_HOUR = "mail_archive_hour";
	public static final String MAIL_ARCHIVE_MINUTE = "mail_archive_minute";
	
	private static final String FIELDS =Joiner.on(", ")
			.join(MAIL_ARCHIVE_DOMAIN_UUID, 
				MAIL_ARCHIVE_ACTIVATED, 
				MAIL_ARCHIVE_REPEAT_KIND, 
				MAIL_ARCHIVE_DAY_OF_WEEK, 
				MAIL_ARCHIVE_DAY_OF_MONTH, 
				MAIL_ARCHIVE_DAY_OF_YEAR, 
				MAIL_ARCHIVE_HOUR, 
				MAIL_ARCHIVE_MINUTE);
	
	@Inject
	@VisibleForTesting DomainConfigurationJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
	}

	@Override
	public DomainConfiguration getDomainConfiguration(ObmDomainUuid domainId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM " + TABLE + " " +
					"WHERE mail_archive_domain_uuid = ?");

			ps.setString(1, domainId.toString());

			rs = ps.executeQuery();

			if (rs.next()) {
				return domainConfigurationFromCursor(rs);
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

	private DomainConfiguration domainConfigurationFromCursor(ResultSet rs) throws SQLException {
		return DomainConfiguration.builder()
				.domainId(ObmDomainUuid.of(rs.getString(MAIL_ARCHIVE_DOMAIN_UUID)))
				.enabled(rs.getBoolean(MAIL_ARCHIVE_ACTIVATED))
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.dayOfMonth(DayOfMonth.of(rs.getInt(MAIL_ARCHIVE_DAY_OF_MONTH)))
							.dayOfWeek(DayOfWeek.fromSpecificationValue(rs.getInt(MAIL_ARCHIVE_DAY_OF_WEEK)))
							.dayOfYear(DayOfYear.of(rs.getInt(MAIL_ARCHIVE_DAY_OF_YEAR)))
							.repeat(RepeatKind.valueOf(rs.getString(MAIL_ARCHIVE_REPEAT_KIND)))
							.build())
						.time(new LocalTime(rs.getInt(MAIL_ARCHIVE_HOUR), rs.getInt(MAIL_ARCHIVE_MINUTE)))
						.build())
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
					" SET " + Joiner.on(" = ?, ")
					.join(MAIL_ARCHIVE_ACTIVATED, 
						MAIL_ARCHIVE_REPEAT_KIND, 
						MAIL_ARCHIVE_DAY_OF_WEEK, 
						MAIL_ARCHIVE_DAY_OF_MONTH, 
						MAIL_ARCHIVE_DAY_OF_YEAR, 
						MAIL_ARCHIVE_HOUR, 
						MAIL_ARCHIVE_MINUTE) + 
					" = ?" +
					" WHERE mail_archive_domain_uuid = ?");

			int idx = 1;
			ps.setBoolean(idx++, domainConfiguration.isEnabled());
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject("repeat_kind", domainConfiguration.getRepeatKind().toString()));
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

	@Override
	public DomainConfiguration createDomainConfiguration(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"INSERT INTO " + TABLE +
					" (" + FIELDS + ")" +
					" VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			int idx = 1;
			ps.setString(idx++, domainConfiguration.getDomainId().toString());
			ps.setBoolean(idx++, domainConfiguration.isEnabled());
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject("repeat_kind", domainConfiguration.getRepeatKind().toString()));
			ps.setInt(idx++, domainConfiguration.getDayOfWeek().getSpecificationValue());
			ps.setInt(idx++, domainConfiguration.getDayOfMonth().getDayIndex());
			ps.setInt(idx++, domainConfiguration.getDayOfYear().getDayOfYear());
			ps.setInt(idx++, domainConfiguration.getHour());
			ps.setInt(idx++, domainConfiguration.getMinute());

			ps.executeUpdate();
			
			return getDomainConfiguration(domainConfiguration.getDomainId());
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}
	}
}
