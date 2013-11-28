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

import javax.inject.Inject;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.Group;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;

public class PGroupDaoJdbcImpl implements PGroupDao {

	private final DatabaseConnectionProvider dbcp;

	@Inject
	public PGroupDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public void insert(Group group) throws DaoException {
		Connection conn = null;
		try {
			conn = dbcp.getConnection();
			groupQuery(conn,
					"INSERT INTO P_UGroup " +
							"(   group_id, " +
							"    group_domain_id, " +
							"    group_timecreate, " +
							"    group_timeupdate, " +
							"    group_userupdate, " +
							"    group_usercreate, " +
							"    group_system, " +
							"    group_archive, " +
							"    group_privacy, " +
							"    group_local, " +
							"    group_ext_id, " +
							"    group_samba, " +
							"    group_gid, " +
							"    group_mailing, " +
							"    group_delegation, " +
							"    group_manager_id, " +
							"    group_name, " +
							"    group_desc, " +
							"    group_email " +
							") SELECT    group_id, " +
							"            group_domain_id, " +
							"            group_timecreate, " +
							"            group_timeupdate, " +
							"            group_userupdate, " +
							"            group_usercreate, " +
							"            group_system, " +
							"            group_archive, " +
							"            group_privacy, " +
							"            group_local, " +
							"            group_ext_id, " +
							"            group_samba, " +
							"            group_gid, " +
							"            group_mailing, " +
							"            group_delegation, " +
							"            group_manager_id, " +
							"            group_name, " +
							"            group_desc, " +
							"            group_email " +
							"  FROM UGroup " +
							"  WHERE group_id=?",
					group);
			groupQuery(conn,
					"INSERT INTO P_GroupEntity " +
							"   (   groupentity_entity_id, " +
							"       groupentity_group_id " +
							"   ) SELECT    groupentity_entity_id," +
							"               groupentity_group_id" +
							"     FROM GroupEntity" +
							"     WHERE groupentity_group_id=?",
					group);
			groupQuery(conn,
					"INSERT INTO P_CategoryLink " +
							"(   categorylink_category_id, " +
							"    categorylink_entity_id, " +
							"    categorylink_category " +
							") SELECT    categorylink_category_id, " +
							"            categorylink_entity_id, " +
							"            categorylink_category " +
							"  FROM CategoryLink " +
							"  WHERE categorylink_entity_id=(SELECT groupentity_entity_id " +
							"                                FROM GroupEntity " +
							"                                WHERE groupentity_group_id = ?)",
					group);
			groupQuery(conn,
					"INSERT INTO P_field " +
							"(   id, " +
							"    entity_id, " +
							"    field, " +
							"    value " +
							") SELECT    id, " +
							"            entity_id, " +
							"            field, " +
							"            value " +
							"  FROM field " +
							"  WHERE entity_id=(SELECT groupentity_entity_id " +
							"                                FROM GroupEntity " +
							"                                WHERE groupentity_group_id = ?)"
					, group);
			groupQuery(conn,
					"INSERT INTO P_of_usergroup " +
							"(   of_usergroup_group_id, " +
							"    of_usergroup_user_id " +
							") SELECT    of_usergroup_group_id, " +
							"            of_usergroup_user_id " +
							"  FROM of_usergroup " +
							"  WHERE of_usergroup_group_id=?",
					group);
			groupQuery(conn,
					"INSERT INTO P__contactgroup " +
							"(   contact_id, " +
							"    group_id " +
							") SELECT    contact_id, " +
							"            group_id " +
							"  FROM _contactgroup " +
							"  WHERE group_id=?",
					group);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, null, null);
		}
	}

	@Override
	public void delete(Group group) throws DaoException {
		Connection conn = null;
		try {
			conn = dbcp.getConnection();
			groupQuery(conn, "DELETE FROM P_of_usergroup WHERE of_usergroup_group_id=?", group);
			groupQuery(conn, "DELETE FROM P__contactgroup WHERE group_id=?", group);
			groupQuery(conn,
					"DELETE FROM P_CategoryLink " +
							"WHERE categorylink_entity_id=(SELECT groupentity_entity_id " +
							"FROM GroupEntity " +
							"WHERE groupentity_group_id = ?)",
					group);
			groupQuery(conn,
					"DELETE FROM P_field " +
							"WHERE entity_id=(SELECT groupentity_entity_id " +
							"FROM GroupEntity " +
							"WHERE groupentity_group_id = ?)",
					group);
			groupQuery(conn, "DELETE FROM P_GroupEntity WHERE groupentity_group_id=?", group);
			groupQuery(conn, "DELETE FROM P_UGroup WHERE group_id=?", group);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, null, null);
		}
	}

	private void groupQuery(Connection conn, String query, Group group) throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(query);
			statement.setInt(1, group.getUid().getId());
			statement.executeUpdate();
		} finally {
			JDBCUtils.cleanup(null, statement, null);
		}
	}

}
