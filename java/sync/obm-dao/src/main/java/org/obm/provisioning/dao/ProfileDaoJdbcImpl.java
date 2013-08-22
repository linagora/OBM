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

package org.obm.provisioning.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.Right;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.profile.CheckBoxState;
import fr.aliacom.obm.common.profile.Module;
import fr.aliacom.obm.common.profile.ModuleCheckBoxStates;
import fr.aliacom.obm.common.profile.Profile;
import fr.aliacom.obm.common.profile.Profile.AccessRestriction;
import fr.aliacom.obm.common.profile.Profile.AdminRealm;
import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class ProfileDaoJdbcImpl implements ProfileDao {

	private static final String LEVEL = "level";
	private static final String MANAGE_PEERS = "level_managepeers";
	private static final String ACCESS_RESTRICTION = "access_restriction";
	private static final String ACCESS_EXCEPTIONS = "access_exceptions";
	private static final String ADMIN_REALM = "admin_realm";
	private static final String DEFAULT_RIGHT = "default_right";
	private static final String MAX_MAIL_QUOTA = "mail_quota_max";
	private static final String DEFAULT_MAIL_QUOTA = "mail_quota_default";

	/**
	 * Modules, in order, appearing in the "default rights" profile property
	 */
	private static final Module[] defaultRightModules = {
		Module.CALENDAR,
		Module.MAILBOX,
		Module.MAILSHARE,
		Module.RESOURCE,
		Module.CONTACTS
	};

	private DatabaseConnectionProvider connectionProvider;

	@Inject
	private ProfileDaoJdbcImpl(DatabaseConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}

	@Override
	public Set<ProfileEntry> getProfileEntries(ObmDomainUuid domainUuid) throws DaoException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Builder<ProfileEntry> profiles = ImmutableSet.builder();

		try {
			conn = connectionProvider.getConnection();
			ps = conn.prepareStatement(
					"		SELECT profile_id " +
					"		  FROM Profile " +
					"	INNER JOIN Domain ON profile_domain_id = domain_id " +
					"		 WHERE domain_uuid = ? OR domain_global IS TRUE");
			ps.setString(1, domainUuid.get());

			rs = ps.executeQuery();

			while (rs.next()) {
				profiles.add(ProfileEntry.builder()
						.id(rs.getLong("profile_id"))
						.domainUuid(domainUuid)
						.build());
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(conn, ps, rs);
		}

		return profiles.build();
	}

	@Override
	public ProfileName getProfileName(ObmDomainUuid domainUuid, ProfileId profileId) throws DaoException, ProfileNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = connectionProvider.getConnection();
			ps = conn.prepareStatement(
					"		SELECT profile_name " +
					"		  FROM Profile " +
					"	INNER JOIN Domain ON profile_domain_id = domain_id " +
					"		 WHERE (domain_uuid = ? OR domain_global IS TRUE) " +
					"		   AND profile_id = ?");
			ps.setString(1, domainUuid.get());
			ps.setLong(2, profileId.getId());
			rs = ps.executeQuery();

			if (rs.next()) {
				return ProfileName.builder().name(rs.getString("profile_name")).build();
			}
			throw new ProfileNotFoundException(profileId.getId());
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(conn, ps, rs);
		}
	}

	@Override
	public ProfileName getUserProfileName(String login, ObmDomainUuid domainId)
			throws DaoException, UserNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = connectionProvider.getConnection();
			ps = conn.prepareStatement(
					"SELECT userobm_perms " +
					"FROM UserObm " +
					"INNER JOIN Domain ON userobm_domain_id = domain_id " +
					"WHERE domain_uuid = ? " +
					"AND userobm_login = ?");
			ps.setString(1, domainId.get());
			ps.setString(2, login);
			rs = ps.executeQuery();
		
			if (rs.next()) {
				return ProfileName.builder().name(rs.getString("userobm_perms")).build();
			}
			throw new UserNotFoundException(login, domainId);
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(conn, ps, rs);
		}
	}

	@Override
	public Profile get(ProfileId id, ObmDomain domain) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = connectionProvider.getConnection();
			ps = con.prepareStatement(
					"SELECT profile_name, profile_timecreate, profile_timeupdate " +
					"FROM Profile " +
					"WHERE profile_id = ?");

			ps.setLong(1, id.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				Profile.Builder profileBuilder = Profile
						.builder()
						.id(id)
						.domain(domain)
						.name(ProfileName.valueOf(rs.getString("profile_name")))
						.timecreate(JDBCUtils.getDate(rs, "profile_timecreate"))
						.timeupdate(JDBCUtils.getDate(rs, "profile_timeupdate"));

				return fetchProfileProperties(con, id, profileBuilder).build();
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return null;
	}

	@Override
	public Profile getUserProfile(ObmUser user) throws DaoException, UserNotFoundException {
		try {
			return get(getUserProfileId(user), user.getDomain());
		} catch (SQLException e) {
			throw new DaoException(e);
		}
	}

	private ProfileId getUserProfileId(ObmUser user) throws SQLException, UserNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = connectionProvider.getConnection();
			ps = con.prepareStatement(
					"SELECT profile_id " +
					"FROM Profile " +
					"INNER JOIN UserObm ON userobm_perms = profile_name " +
					"WHERE userobm_id = ?");

			ps.setInt(1, user.getUid());
			rs = ps.executeQuery();

			if (rs.next()) {
				return ProfileId.builder().id(rs.getInt("profile_id")).build();
			}
		}
		finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		throw new UserNotFoundException(user.getLogin());
	}

	private Profile.Builder fetchProfileProperties(Connection con, ProfileId id, Profile.Builder builder) throws SQLException {
		builder.level(Integer.parseInt(fetchProfileProperty(con, id, LEVEL)));
		builder.managePeers(Integer.parseInt(fetchProfileProperty(con, id, MANAGE_PEERS)) == 1);
		builder.accessRestriction(AccessRestriction.valueOf(fetchProfileProperty(con, id, ACCESS_RESTRICTION).toUpperCase()));
		builder.accessExceptions(fetchProfileProperty(con, id, ACCESS_EXCEPTIONS));
		builder.adminRealms(parseAdminRealms(fetchProfileProperty(con, id, ADMIN_REALM).toUpperCase()));
		builder.defaultMailQuota(Integer.parseInt(fetchProfileProperty(con, id, DEFAULT_MAIL_QUOTA)));
		builder.maxMailQuota(Integer.parseInt(fetchProfileProperty(con, id, MAX_MAIL_QUOTA)));

		fetchDefaultCheckBoxStates(con, id, builder);

		return builder;
	}

	private AdminRealm[] parseAdminRealms(String adminRealmsStr) {
		List<String> adminRealmsStrs;
		
		if (Strings.isNullOrEmpty(adminRealmsStr)) {
			return new AdminRealm[0];
		}
		
		adminRealmsStrs = Lists.newArrayList(adminRealmsStr.split(","));
		List<AdminRealm> realms =  Lists.transform(adminRealmsStrs, new Function<String, AdminRealm>() {
			@Override
			public AdminRealm apply(String input) {
				return AdminRealm.valueOf(input);
			}
			
		});
		
		return realms.toArray(new AdminRealm[realms.size()]);
	}

	private String fetchProfileProperty(Connection con, ProfileId id, String name) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(
					"SELECT profileproperty_value " +
					"FROM ProfileProperty " +
					"WHERE profileproperty_profile_id = ? AND profileproperty_name = ?");

			ps.setLong(1, id.getId());
			ps.setString(2, name);
			rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getString("profileproperty_value");
			}
		}
		finally {
			JDBCUtils.cleanup(null, ps, rs);
		}

		return null;
	}

	private Profile.Builder fetchDefaultCheckBoxStates(Connection con, ProfileId id, Profile.Builder builder) throws SQLException {
		String defaultRightsStr = fetchProfileProperty(con, id, DEFAULT_RIGHT);
		Iterator<String> defaultRights = Splitter.on(',').split(defaultRightsStr).iterator();

		for (Module module : defaultRightModules) {
			builder.defaultCheckBoxState(module, ModuleCheckBoxStates
					.builder()
					.module(module)
					.checkBoxState(Right.ACCESS, CheckBoxState.fromValue(Integer.parseInt(defaultRights.next())))
					.checkBoxState(Right.READ, CheckBoxState.fromValue(Integer.parseInt(defaultRights.next())))
					.checkBoxState(Right.WRITE, CheckBoxState.fromValue(Integer.parseInt(defaultRights.next())))
					.build());
		}

		return builder;
	}

}
