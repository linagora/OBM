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
import java.sql.SQLException;
import java.util.Set;

import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class UserPatternDaoJdbcImpl implements UserPatternDao {

	private final ObmHelper obmHelper;

	@Inject
	private UserPatternDaoJdbcImpl(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	@Override
	public void updateUserIndex(ObmUser user) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		String query = "INSERT INTO _userpattern (id, pattern) VALUES (?, LOWER(?))";

		try {
			con = obmHelper.getConnection();

			deleteUserIndex(con, user);
			Set<String> patterns = getUserPatterns(user);

			ps = con.prepareStatement(query);

			for (String pattern : patterns) {
				ps.setInt(1, user.getUid());
				ps.setString(2, pattern);
				ps.addBatch();
			}

			ps.executeBatch();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	@VisibleForTesting
	Set<String> getUserPatterns(ObmUser user) {
		Set<String> patterns = Sets.newTreeSet();

		patterns.add(user.getLogin());
		if (user.getLastName() != null) {
			Iterables.addAll(patterns, splitWords(user.getLastName()));
		}
		if (user.getFirstName() != null) {
			Iterables.addAll(patterns, splitWords(user.getFirstName()));
		}
		if (user.isEmailAvailable()) {
			patterns.add(user.getEmail());
			patterns.addAll(user.getEmailAlias());
		}

		return patterns;
	}

	private Iterable<? extends String> splitWords(String words) {
		return Splitter
				.on(' ')
				.omitEmptyStrings()
				.split(words);
	}

	private void deleteUserIndex(Connection con, ObmUser user) throws SQLException {
		PreparedStatement ps = null;
		String query = "DELETE FROM _userpattern WHERE id = ?";

		try {
			ps = con.prepareStatement(query);

			ps.setInt(1, user.getUid());
			ps.executeUpdate();
		}
		finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

}
