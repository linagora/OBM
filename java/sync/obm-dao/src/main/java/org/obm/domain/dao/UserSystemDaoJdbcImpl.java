/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.domain.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.UserPassword;

@Singleton
public class UserSystemDaoJdbcImpl implements UserSystemDao {

	private static final String FIELDS = "usersystem_id, usersystem_login, usersystem_password";

	private final DatabaseConnectionProvider dbcp;

	@Inject
	private UserSystemDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public ObmSystemUser getByLogin(String login) throws DaoException, SystemUserNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("SELECT " + FIELDS + " FROM UserSystem WHERE usersystem_login = ?");

			ps.setString(1, login);

			rs = ps.executeQuery();

			if (rs.next()) {
				return systemUserFromCursor(rs);
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}

		throw new SystemUserNotFoundException(String.format("No such system user '%s'", login));
	}

	private ObmSystemUser systemUserFromCursor(ResultSet rs) throws SQLException {
		return ObmSystemUser
				.builder()
				.id(rs.getInt("usersystem_id"))
				.login(rs.getString("usersystem_login"))
				.password(UserPassword.valueOf(rs.getString("usersystem_password")))
				.build();
	}
}
