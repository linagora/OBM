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
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl.TABLE.FIELDS;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class DomainConfigurationJdbcImpl implements DomainConfigurationDao {

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;
	
	public interface TABLE {
		
		String NAME = "mail_archive";
		
		interface FIELDS {
			String DOMAIN_UUID = "mail_archive_domain_uuid";
			String ACTIVATED = "mail_archive_activated";
			String REPEAT_KIND = "mail_archive_repeat_kind";
			String DAY_OF_WEEK = "mail_archive_day_of_week";
			String DAY_OF_MONTH = "mail_archive_day_of_month";
			String DAY_OF_YEAR = "mail_archive_day_of_year";
			String HOUR = "mail_archive_hour";
			String MINUTE = "mail_archive_minute";
			String EXCLUDED_FOLDER = "mail_archive_excluded_folder";
			
			String ALL = Joiner.on(", ").join(DOMAIN_UUID, ACTIVATED, REPEAT_KIND, DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, HOUR, MINUTE, EXCLUDED_FOLDER);
			String UPDATABLE = Joiner.on(" = ?, ").join(ACTIVATED, REPEAT_KIND, DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, HOUR, MINUTE, EXCLUDED_FOLDER);
		}
	}
	
	interface REQUESTS {
		
		String SELECT = String.format(
				"SELECT %s FROM %s WHERE %s = ?", FIELDS.ALL, TABLE.NAME, FIELDS.DOMAIN_UUID);
		
		String UPDATE = String.format(
				"UPDATE %s SET %s = ? WHERE %s = ?", TABLE.NAME, FIELDS.UPDATABLE, FIELDS.DOMAIN_UUID);
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE.NAME, FIELDS.ALL);
	}
	
	@Inject
	@VisibleForTesting DomainConfigurationJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
	}

	@Override
	public DomainConfiguration get(ObmDomain domain) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT)) {

			ps.setString(1, domain.getUuid().toString());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return domainConfigurationFromResultSet(rs, domain);
			} else {
				return null;
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private DomainConfiguration domainConfigurationFromResultSet(ResultSet rs, ObmDomain domain) throws SQLException {
		return DomainConfiguration.builder()
				.domain(domain)
				.state(rs.getBoolean(FIELDS.ACTIVATED) ? ConfigurationState.ENABLE : ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.dayOfMonth(DayOfMonth.of(rs.getInt(FIELDS.DAY_OF_MONTH)))
							.dayOfWeek(DayOfWeek.fromSpecificationValue(rs.getInt(FIELDS.DAY_OF_WEEK)))
							.dayOfYear(DayOfYear.of(rs.getInt(FIELDS.DAY_OF_YEAR)))
							.repeat(RepeatKind.valueOf(rs.getString(FIELDS.REPEAT_KIND)))
							.build())
						.time(new LocalTime(rs.getInt(FIELDS.HOUR), rs.getInt(FIELDS.MINUTE)))
						.build())
				.excludedFolder(rs.getString(FIELDS.EXCLUDED_FOLDER))
				.build();
	}

	@Override
	public void update(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.UPDATE)) {

			int idx = 1;
			ps.setBoolean(idx++, domainConfiguration.isEnabled());
			ps.setObject(idx++, obmHelper.getDBCP()
					.getJdbcObject("repeat_kind", domainConfiguration.getRepeatKind().toString()));
			ps.setInt(idx++, domainConfiguration.getDayOfWeek().getSpecificationValue());
			ps.setInt(idx++, domainConfiguration.getDayOfMonth().getDayIndex());
			ps.setInt(idx++, domainConfiguration.getDayOfYear().getDayOfYear());
			ps.setInt(idx++, domainConfiguration.getHour());
			ps.setInt(idx++, domainConfiguration.getMinute());
			ps.setString(idx++, domainConfiguration.getExcludedFolder());
			ps.setString(idx++, domainConfiguration.getDomainId().toString());

			if (ps.executeUpdate() < 1) {
				throw new DomainNotFoundException(domainConfiguration.getDomainId());
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	@Override
	public void create(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.INSERT)) {

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
			ps.setString(idx++, domainConfiguration.getExcludedFolder());

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
}
