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

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.imap.archive.dao.UserJdbcImpl.TABLE.FIELDS;
import org.obm.provisioning.dao.exceptions.DaoException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.UserExtId;

// TODO: to be removed OBMFULL-6170
@Singleton
public class UserJdbcImpl implements UserDao {

	private final DatabaseConnectionProvider dbcp;
	
	public interface TABLE {
		
		String NAME = "UserObm";
		
		interface FIELDS {
			String USEROBM_EXT_ID = "userobm_ext_id";
			String USEROBM_LOGIN = "userobm_login";
		}
	}
	
	interface REQUESTS {
		
		String SELECT = String.format(
				"SELECT %s FROM %s WHERE %s = ?", FIELDS.USEROBM_LOGIN, TABLE.NAME, FIELDS.USEROBM_EXT_ID);
	}
	
	@Inject
	@VisibleForTesting UserJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public Optional<String> getUserLogin(UserExtId userExtId) throws DaoException {
		try (Connection connection = dbcp.getConnection();
				PreparedStatement ps = connection.prepareStatement(REQUESTS.SELECT)) {

			ps.setString(1, userExtId.getExtId());

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return Optional.of(rs.getString(TABLE.FIELDS.USEROBM_LOGIN));
			}
			return Optional.absent();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
	}
}
