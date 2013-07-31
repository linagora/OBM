/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
import java.sql.Types;
import java.util.Set;

import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.sync.Right;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntityRightDaoJdbcImpl implements EntityRightDao {

	private static final int ALLOWED = 1;
	private static final String FIELDS = "entityright_access, entityright_read, entityright_write, entityright_admin ";

	private final ObmHelper obmHelper;

	@Inject
	private EntityRightDaoJdbcImpl(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	@Override
	public Set<Right> getPublicRights(Integer entityId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = obmHelper.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM EntityRight " +
					"WHERE entityright_entity_id = ? AND entityright_consumer_id IS NULL");

			ps.setInt(1, entityId);

			rs = ps.executeQuery();

			if (rs.next()) {
				return rightsFromCursor(rs);
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(connection, ps, rs);
		}

		return ImmutableSet.of();
	}

	@Override
	public Set<Right> getRights(Integer entityId, Integer consumerId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = obmHelper.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM EntityRight " +
					"WHERE entityright_entity_id = ? AND entityright_consumer_id = ?");

			ps.setInt(1, entityId);
			ps.setInt(2, consumerId);

			rs = ps.executeQuery();

			if (rs.next()) {
				return rightsFromCursor(rs);
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(connection, ps, rs);
		}

		return ImmutableSet.of();
	}

	private Set<Right> rightsFromCursor(ResultSet rs) throws SQLException {
		ImmutableSet.Builder<Right> rights = ImmutableSet.builder();

		if (rs.getInt("entityright_access") == ALLOWED) {
			rights.add(Right.ACCESS);
		}
		if (rs.getInt("entityright_read") == ALLOWED) {
			rights.add(Right.READ);
		}
		if (rs.getInt("entityright_write") == ALLOWED) {
			rights.add(Right.ACCESS);
		}
		if (rs.getInt("entityright_admin") == ALLOWED) {
			rights.add(Right.ADMIN);
		}

		return rights.build();
	}

	@Override
	public void grantRights(Integer entityId, Integer consumerId, Set<Right> rights) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			int idx = 1;

			connection = obmHelper.getConnection();
			ps = connection.prepareStatement(
					"INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, " + FIELDS + ") " +
					"VALUES (?, ?, ?, ?, ?, ?)");

			ps.setInt(idx++, entityId);
			if (consumerId != null) {
				ps.setInt(idx++, consumerId);
			} else {
				ps.setNull(idx++, Types.INTEGER);
			}
			ps.setInt(idx++, rights.contains(Right.ACCESS) ? 1 : 0);
			ps.setInt(idx++, rights.contains(Right.READ) ? 1 : 0);
			ps.setInt(idx++, rights.contains(Right.WRITE) ? 1 : 0);
			ps.setInt(idx++, rights.contains(Right.ADMIN) ? 1 : 0);

			ps.executeUpdate();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(connection, ps, null);
		}
	}

	@Override
	public void deleteRights(Integer entityId, Integer consumerId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = obmHelper.getConnection();
			ps = connection.prepareStatement(
					"DELETE FROM EntityRight " +
					"WHERE entityright_entity_id = ? AND entityright_consumer_id = ?");

			ps.setInt(1, entityId);
			ps.setInt(2, consumerId);

			ps.executeUpdate();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(connection, ps, null);
		}
	}

	@Override
	public void deletePublicRights(Integer entityId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = obmHelper.getConnection();
			ps = connection.prepareStatement(
					"DELETE FROM EntityRight " +
					"WHERE entityright_entity_id = ? AND entityright_consumer_id IS NULL");

			ps.setInt(1, entityId);

			ps.executeUpdate();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(connection, ps, null);
		}
	}

}
