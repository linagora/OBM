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
import java.util.Set;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.beans.ProfileId;
import org.obm.provisioning.beans.ProfileName;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.obm.push.utils.JDBCUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ProfileDaoJdbcImpl implements ProfileDao {

	private DatabaseConnectionProvider connectionProvider;

	@Inject
	private ProfileDaoJdbcImpl(DatabaseConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}

	@Override
	public Set<ProfileEntry> getProfiles(ObmDomainUuid domainUuid) throws DaoException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Builder<ProfileEntry> profiles = ImmutableSet.builder();

		try {
			conn = connectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT profile_id FROM Profile INNER JOIN Domain ON profile_domain_id = domain_id WHERE	domain_uuid = ?");
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
	public ProfileName getProfile(ProfileId profileId) throws DaoException, ProfileNotFoundException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = connectionProvider.getConnection();
			ps = conn.prepareStatement("SELECT profile_name FROM Profile WHERE profile_id = ?");
			ps.setLong(1, profileId.getId());
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
}
