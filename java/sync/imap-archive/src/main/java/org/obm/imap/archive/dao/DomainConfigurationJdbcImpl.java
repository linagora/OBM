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

import org.joda.time.LocalTime;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.Mailing;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.beans.ScopeUser;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl.TABLE.FIELDS;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.sync.base.EmailAddress;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;

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
			String ARCHIVE_MAIN_FOLDER = "mail_archive_main_folder";
			String EXCLUDED_FOLDER = "mail_archive_excluded_folder";
			String SCOPE_INCLUDES = "mail_archive_scope_includes";
			String MOVE_ENABLED = "mail_archive_move";
			
			String ALL = Joiner.on(", ").join(DOMAIN_UUID, ACTIVATED, REPEAT_KIND, DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, HOUR, MINUTE, ARCHIVE_MAIN_FOLDER, EXCLUDED_FOLDER, SCOPE_INCLUDES, MOVE_ENABLED);
			String UPDATABLE = Joiner.on(" = ?, ").join(ACTIVATED, REPEAT_KIND, DAY_OF_WEEK, DAY_OF_MONTH, DAY_OF_YEAR, HOUR, MINUTE, ARCHIVE_MAIN_FOLDER, EXCLUDED_FOLDER, SCOPE_INCLUDES, MOVE_ENABLED);
		}
	}
	
	interface REQUESTS {
		
		String SELECT = String.format(
				"SELECT %s FROM %s WHERE %s = ?", FIELDS.ALL, TABLE.NAME, FIELDS.DOMAIN_UUID);
		
		String UPDATE = String.format(
				"UPDATE %s SET %s = ? WHERE %s = ?", TABLE.NAME, FIELDS.UPDATABLE, FIELDS.DOMAIN_UUID);
		
		String INSERT = String.format(
				"INSERT INTO %s (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", TABLE.NAME, FIELDS.ALL);
	}
	
	public interface SCOPE_USERS {
		interface TABLE {
			
			String NAME = "mail_archive_scope_users";
			
			interface FIELDS {
				String DOMAIN_UUID = "mail_archive_scope_users_domain_uuid";
				String USER_UUID = "mail_archive_scope_users_user_uuid";
				String USER_LOGIN = "mail_archive_scope_users_user_login";
				
				String ALL = Joiner.on(", ").join(DOMAIN_UUID, USER_UUID, USER_LOGIN);
			}
		}
				
		interface REQUESTS {
			String SELECT = String.format(
					"SELECT %s FROM %s WHERE %s = ?", TABLE.FIELDS.ALL, TABLE.NAME, TABLE.FIELDS.DOMAIN_UUID);
			
			String DELETE = String.format(
					"DELETE FROM %s WHERE %s = ?", TABLE.NAME, TABLE.FIELDS.DOMAIN_UUID);
			
			String INSERT = String.format(
					"INSERT INTO %s (%s) VALUES (?, ?, ?)", TABLE.NAME, TABLE.FIELDS.ALL);
		}
	}
		
	public interface MAILING {
		interface TABLE {
			
			String NAME = "mail_archive_mailing";
			
			interface FIELDS {
				String DOMAIN_UUID = "mail_archive_mailing_domain_uuid";
				String EMAIL = "mail_archive_mailing_email";
				
				String ALL = Joiner.on(", ").join(DOMAIN_UUID, EMAIL);
			}
		}
		
		interface REQUESTS {
			String SELECT = String.format(
					"SELECT %s FROM %s WHERE %s = ?", TABLE.FIELDS.ALL, TABLE.NAME, TABLE.FIELDS.DOMAIN_UUID);
			
			String DELETE = String.format(
					"DELETE FROM %s WHERE %s = ?", TABLE.NAME, TABLE.FIELDS.DOMAIN_UUID);
			
			String INSERT = String.format(
					"INSERT INTO %s (%s) VALUES (?, ?)", TABLE.NAME, TABLE.FIELDS.ALL);
		}
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
				return domainConfigurationFromResultSet(connection, rs, domain);
			} else {
				return null;
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private DomainConfiguration domainConfigurationFromResultSet(Connection connection, ResultSet rs, ObmDomain domain) throws SQLException, DaoException {
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
				.archiveMainFolder(rs.getString(FIELDS.ARCHIVE_MAIN_FOLDER))
				.excludedFolder(rs.getString(FIELDS.EXCLUDED_FOLDER))
				.scopeIncludes(rs.getBoolean(FIELDS.SCOPE_INCLUDES))
				.scopeUsers(get(connection, domain.getUuid()))
				.mailing(getMailing(connection, domain.getUuid()))
				.moveEnabled(rs.getBoolean(FIELDS.MOVE_ENABLED))
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
			ps.setString(idx++, domainConfiguration.getArchiveMainFolder());
			ps.setString(idx++, domainConfiguration.getExcludedFolder());
			ps.setBoolean(idx++, domainConfiguration.isScopeIncludes());
			ps.setBoolean(idx++, domainConfiguration.isMoveEnabled());
			ps.setString(idx++, domainConfiguration.getDomainId().toString());

			if (ps.executeUpdate() < 1) {
				throw new DomainNotFoundException(domainConfiguration.getDomainId());
			}
			
			update(connection, domainConfiguration.getDomainId(), domainConfiguration.getScopeUsers());
			update(connection, domainConfiguration.getDomainId(), domainConfiguration.getMailing());
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
			ps.setString(idx++, domainConfiguration.getArchiveMainFolder());
			ps.setString(idx++, domainConfiguration.getExcludedFolder());
			ps.setBoolean(idx++, domainConfiguration.isScopeIncludes());
			ps.setBoolean(idx++, domainConfiguration.isMoveEnabled());

			ps.executeUpdate();
			
			update(connection, domainConfiguration.getDomainId(), domainConfiguration.getScopeUsers());
			update(connection, domainConfiguration.getDomainId(), domainConfiguration.getMailing());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
	
	private List<ScopeUser> get(Connection connection, ObmDomainUuid domainId) throws DaoException {
		try (PreparedStatement ps = connection.prepareStatement(SCOPE_USERS.REQUESTS.SELECT)) {

			ps.setString(1, domainId.get());

			ResultSet rs = ps.executeQuery();
			
			ImmutableList.Builder<ScopeUser> builder = ImmutableList.builder();
			while (rs.next()) {
				builder.add(ScopeUser.builder()
						.id(UserExtId.valueOf(rs.getString(SCOPE_USERS.TABLE.FIELDS.USER_UUID)))
						.login(rs.getString(SCOPE_USERS.TABLE.FIELDS.USER_LOGIN))
						.build());
			}
			return builder.build();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private void update(Connection connection, ObmDomainUuid domainId, List<ScopeUser> users) throws DaoException {
		try (PreparedStatement psDelete = connection.prepareStatement(SCOPE_USERS.REQUESTS.DELETE);
				PreparedStatement psInsert = connection.prepareStatement(SCOPE_USERS.REQUESTS.INSERT)) {

			psDelete.setString(1, domainId.get());
			psDelete.executeUpdate();
			
			for (ScopeUser scopeUser : users) {
				int idx = 1;
				psInsert.setString(idx++, domainId.get());
				psInsert.setString(idx++, scopeUser.serializeId());
				psInsert.setString(idx++, scopeUser.getLogin());
				
				psInsert.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
				
	private Mailing getMailing(Connection connection, ObmDomainUuid domainId) throws DaoException {
		try (PreparedStatement ps = connection.prepareStatement(MAILING.REQUESTS.SELECT)) {
	
			ps.setString(1, domainId.get());
	
			ResultSet rs = ps.executeQuery();
	
			Mailing.Builder builder = Mailing.builder();
			while (rs.next()) {
				builder.add(EmailAddress.loginAtDomain(rs.getString(MAILING.TABLE.FIELDS.EMAIL)));
			}
			return builder.build();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}
	
	private void update(Connection connection, ObmDomainUuid domainId, Mailing mailing) throws DaoException {
		try (PreparedStatement psDelete = connection.prepareStatement(MAILING.REQUESTS.DELETE);
				PreparedStatement psInsert = connection.prepareStatement(MAILING.REQUESTS.INSERT)) {

			psDelete.setString(1, domainId.get());
			psDelete.execute();
			
			for (EmailAddress emailAddress : mailing.getEmailAddresses()) {
				int idx = 1;
				psInsert.setString(idx++, domainId.get());
				psInsert.setString(idx++, emailAddress.get());
	
				psInsert.executeUpdate();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}
}
