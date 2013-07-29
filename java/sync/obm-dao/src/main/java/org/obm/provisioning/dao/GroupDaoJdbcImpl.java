/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2013 Linagora
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.UserDao;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupExistsException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

/**
 * A JDBC backed implementation of the GroupDao.
 */
@Singleton
public class GroupDaoJdbcImpl implements GroupDao {

	private static final String FIELDS = "group_id, group_name, group_desc, group_timecreate, group_timeupdate, " +
			"group_privacy, group_archive, group_email, group_gid";

    /** The first group_gid to use. UI code assumes 1000 here */
    private final int firstGidUser = 1000;

    private DatabaseConnectionProvider connectionProvider;
    private final UserDao userDao;
    private final ObmHelper obmHelper;

    @Inject
    private GroupDaoJdbcImpl(DatabaseConnectionProvider connectionProvider, UserDao userDao, ObmHelper obmHelper) {
        this.connectionProvider = connectionProvider;
        this.userDao = userDao;
        this.obmHelper = obmHelper;
    }

    @Override
    public Group get(ObmDomain domain, GroupExtId id) throws DaoException, GroupNotFoundException {
        Connection conn = null;
        try {
            conn = connectionProvider.getConnection();
            return this.getGroupBuilder(conn, domain, id).build();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, null, null);
        }
    }

    @Override
    public Group getRecursive(ObmDomain domain, GroupExtId id, boolean includeUsers, int groupDepth) throws DaoException, GroupNotFoundException {
        Connection conn = null;
        Set<GroupExtId> recursedGroups = Sets.newHashSet();

        try {
            conn = connectionProvider.getConnection();
            return buildRecursiveGroup(conn, getGroupBuilder(conn, domain, id), domain, id, includeUsers, groupDepth, recursedGroups);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, null, null);
        }
    }

    @Override
    public Group create(ObmDomain domain, Group info) throws DaoException, GroupExistsException {
        Connection conn = null;
        PreparedStatement ps = null;
        GroupExtId extId = info.getExtId();
        try {
        	int idx = 1;

            conn =  connectionProvider.getConnection();
            if (extIdExists(conn, extId)) {
                throw new GroupExistsException(extId);
            }

            ps = conn.prepareStatement(
                    " INSERT INTO UGroup" +
                    "             (group_domain_id, group_ext_id, group_name," +
                    "              group_desc, group_gid, group_privacy, group_email)" +
                    "      VALUES " +
                    "             (SELECT domain_id FROM Domain WHERE domain_uuid = ?," +
                    "              ?, ?, ?, ?, ?, ?)");

            ps.setString(idx++, domain.getUuid().get());
            ps.setString(idx++, extId.getId());
            ps.setString(idx++, info.getName());
            ps.setString(idx++, info.getDescription());
            ps.setInt(idx++, getNextFreeGid(conn));
            ps.setBoolean(idx++, info.isPrivateGroup());
            ps.setString(idx++, info.getEmail());
            ps.executeUpdate();

            // Make sure the linked entity is created
            int groupId = obmHelper.lastInsertId(conn);
            obmHelper.linkEntity(conn, "GroupEntity", "group_id", groupId);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }

        try {
            return get(domain, info.getExtId());
        } catch (GroupNotFoundException e) {
            // This shouldn't happen
            return null;
        }
    }

    @Override
    public Group update(ObmDomain domain, Group info) throws DaoException, GroupNotFoundException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
        	int idx = 1;

            conn =  connectionProvider.getConnection();
            ps = conn.prepareStatement(
                    "      UPDATE UGroup" +
                    "         SET group_name = ?, group_desc = ?, group_timeupdate = ?, group_archive = ?, group_privacy = ?, group_email = ?" +
                    "       WHERE group_ext_id = ?" +
                    "         AND group_domain_id = (SELECT domain_id FROM Domain WHERE domain_uuid = ?)");

            ps.setString(idx++, info.getName());
            ps.setString(idx++, info.getDescription());
            ps.setTimestamp(idx++, new Timestamp(obmHelper.selectNow(conn).getTime()));
            ps.setBoolean(idx++, info.isArchive());
            ps.setBoolean(idx++, info.isPrivateGroup());
            ps.setString(idx++, info.getEmail());
            ps.setString(idx++, info.getExtId().getId());
            ps.setString(idx++, domain.getUuid().get());

            if (ps.executeUpdate() < 1) {
                throw new GroupNotFoundException(info.getExtId());
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }

        return get(domain, info.getExtId());
    }

    @Override
    public void delete(ObmDomain domain, GroupExtId id) throws DaoException, GroupNotFoundException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn =  connectionProvider.getConnection();
            ps = conn.prepareStatement(
                    " DELETE FROM UGroup" +
                    "       WHERE group_ext_id = ?" +
                    "         AND group_domain_id = (SELECT domain_id FROM Domain WHERE domain_uuid = ?)");

            ps.setString(1, id.getId());
            ps.setString(2, domain.getUuid().get());

            if (ps.executeUpdate() < 1) {
                throw new GroupNotFoundException(id);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }
    }

    @Override
    public void addUser(ObmDomain domain, GroupExtId id, ObmUser user) throws DaoException, GroupNotFoundException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = connectionProvider.getConnection();
            int internalId = getInternalGroupId(conn, domain, id);
            ps = conn.prepareStatement(
                    " INSERT INTO UserObmGroup" +
                    "              (userobmgroup_group_id, userobmgroup_userobm_id)" +
                    "      VALUES (?,?)");

            ps.setInt(1, internalId);
            ps.setInt(2, user.getUid());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }
    }

    @Override
    public void addSubgroup(ObmDomain domain, GroupExtId group, GroupExtId subgroup) throws DaoException, GroupNotFoundException, GroupRecursionException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = connectionProvider.getConnection();
            GroupExtId foundParent = hasAncestorWithId(conn, domain, subgroup, group);
            if (foundParent != null) {
                // Cyclic group
                throw new GroupRecursionException(subgroup, foundParent);
            }

            ps = conn.prepareStatement(
                    " INSERT INTO GroupGroup" +
                    "             (groupgroup_parent_id, groupgroup_child_id)" +
                    "      VALUES (?,?)");

            ps.setInt(1, getInternalGroupId(conn, domain, group));
            ps.setInt(2, getInternalGroupId(conn, domain, subgroup));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }
    }

    @Override
    public void removeUser(ObmDomain domain, GroupExtId id, ObmUser user)  throws DaoException, GroupNotFoundException, UserNotFoundException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = connectionProvider.getConnection();
            int internalId = getInternalGroupId(conn, domain, id);
            ps = conn.prepareStatement(
                    " DELETE FROM UserObmGroup" +
                    "        WHERE userobmgroup_group_id = ?" +
                    "         AND userobmgroup_userobm_id = ?");

            ps.setInt(1, internalId);
            ps.setInt(2, user.getUid());
            if (ps.executeUpdate() < 1) {
                // The GroupNotFound case is already handled in
                // getInternalGroupId
                throw new UserNotFoundException(user.getExtId());
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }
    }

    @Override
    public void removeSubgroup(ObmDomain domain, GroupExtId extId, GroupExtId subgroup)  throws DaoException, GroupNotFoundException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = connectionProvider.getConnection();
            int internalId = getInternalGroupId(conn, domain, extId);
            ps = conn.prepareStatement(
                    " DELETE FROM GroupGroup" +
                    "        WHERE groupgroup_parent_id = ?" +
                    "         AND groupgroup_child_id = ?");

            ps.setInt(1, internalId);
            ps.setInt(2, getInternalGroupId(conn, domain, subgroup));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JDBCUtils.cleanup(conn, ps, null);
        }
    }

    /**
     * Creates a builder with the basic group information set. Can be used to
     * build subgroups and users for a group.
     *
     * @param conn                      The connection to use for building.
     * @param domain                    The domain to query on.
     * @param id                        The GroupExtId to query for.
     * @return                          A builder with basic group information set.
     * @throws SQLException             If an SQL error occurred.
     * @throws GroupNotFoundException   If the group was not found.
     */
    private Group.Builder getGroupBuilder(Connection conn, ObmDomain domain, GroupExtId id) throws SQLException, GroupNotFoundException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "      SELECT " + FIELDS +
                    "        FROM UGroup " +
                    "  INNER JOIN Domain ON group_domain_id = domain_id" +
                    "       WHERE domain_uuid = ?" +
                    "         AND group_ext_id = ?" +
                    "       LIMIT 1");
            ps.setString(1, domain.getUuid().get());
            ps.setString(2, id.getId());
            rs = ps.executeQuery();

            if (rs.next()) {
				return Group
						.builder()
						.uid(Group.Id.valueOf(rs.getInt("group_id")))
						.gid(JDBCUtils.getInteger(rs, "group_gid"))
						.timecreate(JDBCUtils.getDate(rs, "group_timecreate"))
						.timeupdate(JDBCUtils.getDate(rs, "group_timeupdate"))
						.archive(rs.getBoolean("group_archive"))
						.privateGroup(rs.getBoolean("group_privacy"))
						.email(rs.getString("group_email"))
						.extId(id)
						.name(rs.getString("group_name"))
						.description(rs.getString("group_desc"));
            } else {
                throw new GroupNotFoundException(id);
            }
        } finally {
            JDBCUtils.cleanup(null, ps, rs);
        }
    }

    /**
     * Builds a group recursively, adding users and subgroups. Basic information
     * must already be set. If a cyclic group is detected, expansion is stopped.
     *
     * @param conn                      The connection to use for building.
     * @param groupBuilder              The builder with basic information set.
     * @param domain                    The domain to query on.
     * @param id                        The extId to query for.
     * @param includeUsers              True, if users should be added to the built group
     * @param groupDepth                The depth subgroups should be expanded to.
     * @param recursedGroups            The set of previously expanded groups, may be an empty set.
     * @return                          The built group containing all requested users and subgroups.
     * @throws SQLException             If an SQL exception occurred.
     * @throws GroupNotFoundException   If the queried for group was not found.
     */
    private Group buildRecursiveGroup(Connection conn, Group.Builder groupBuilder, ObmDomain domain, GroupExtId id, boolean includeUsers, int groupDepth, Set<GroupExtId> recursedGroups) throws SQLException, GroupNotFoundException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        int internalGroupId = getInternalGroupId(conn, domain, id);
        recursedGroups.add(id);

        if (includeUsers) {
            try {
                ps = conn.prepareStatement(
                         "      SELECT userobmgroup_userobm_id" +
                         "        FROM UserObmGroup" +
                         "  INNER JOIN UGroup  ON userobmgroup_group_id = group_id" +
                         "  INNER JOIN Domain  ON group_domain_id = domain_id" +
                         "       WHERE group_id = ?" +
                         "         AND domain_uuid = ?");
                ps.setInt(1, internalGroupId);
                ps.setString(2, domain.getUuid().get());
                rs = ps.executeQuery();

                 while (rs.next()) {
                     ObmUser user = userDao.findUserById(rs.getInt("userobmgroup_userobm_id"), domain);
                     if (user != null) {
                         groupBuilder.user(user);
                     }
                 }
            } finally {
                JDBCUtils.cleanup(null, ps, rs);
            }
        }

        if (groupDepth != 0) {
            int childDepth = (groupDepth > 0 ? groupDepth - 1 : -1);

            try {
                 ps = conn.prepareStatement(
                         "      SELECT cgroup.group_ext_id" +
                         "        FROM GroupGroup " +
                         "  INNER JOIN UGroup AS pgroup ON groupgroup_parent_id = pgroup.group_id" +
                         "  INNER JOIN UGroup AS cgroup ON groupgroup_child_id = cgroup.group_id" +
                         "  INNER JOIN Domain ON pgroup.group_domain_id = domain_id" +
                         "       WHERE pgroup.group_id = ?" +
                         "         AND domain_uuid = ?" +
                         "    ORDER BY cgroup.group_ext_id" +
                         (groupDepth > 0 ? " LIMIT " + String.valueOf(groupDepth) : ""));

                 ps.setInt(1, internalGroupId);
                 ps.setString(2, domain.getUuid().get());
                 rs = ps.executeQuery();

                 while (rs.next()) {
                     GroupExtId subid = GroupExtId.valueOf(rs.getString("group_ext_id"));
                     Group.Builder subgroupBuilder = getGroupBuilder(conn, domain, subid);

                     // Same sub-subgroups for multiple subgroups are ok (i.e group1 has children
                     // group2 and groups3, which both have child group4), but group1 -> group2
                     // -> group1 is not. Create a copy of the set and use that per subgroup.
                     Set<GroupExtId> childRecursedGroups = Sets.newHashSet(recursedGroups.iterator());

                     if (recursedGroups.contains(subid)) {
                         // If we have a cyclic subgroup, just show its info but not its subgroups again
                         childDepth = 0;
                     }
                     Group subgroup = buildRecursiveGroup(conn, subgroupBuilder, domain, subid,
                                                          includeUsers, childDepth, childRecursedGroups);
                     groupBuilder.subgroup(subgroup);
                 }
            } finally {
                JDBCUtils.cleanup(null, ps, rs);
            }
        }

        return groupBuilder.build();
    }

    /**
     * Builds the uid_max_used field in the ObmInfo table for use with getNextFreeGid.
     *
     * @param conn           The connection to use for building.
     * @throws SQLException  If an SQL error occurred.
     */
    private void buildNextUidField(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(
                    "INSERT INTO ObmInfo (obminfo_name, obminfo_value) " +
                    "     VALUES ('uid_max_used', " +
                    "       SELECT MAX(id) FROM (" +
                    "              SELECT MAX(host_uid) AS id FROM `Host` WHERE host_uid > ?1" +
                    "              UNION" +
                    "              SELECT MAX(userobm_uid) AS id FROM UserObm WHERE userobm_uid > ?1" +
                    "              UNION" +
                    "              SELECT MAX(group_gid) AS id FROM UGroup WHERE group_gid > ?1" +
                    "              UNION" +
                    "              SELECT 1000 AS id" +
                    "       ) AS t" +
                    "     )");
            ps.setInt(1, firstGidUser);
            ps.executeUpdate();
        } finally {
            JDBCUtils.cleanup(null, ps, null);
        }
    }

    /**
     * Retrieves the next free group_gid and reserves it for use.
     *
     * @param conn              The connection to use for retrieval.
     * @return                  The next free group id.
     * @throws SQLException     If an SQL error occurred.
     */
    private int getNextFreeGid(Connection conn) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "SELECT obminfo_value " +
                    "  FROM ObmInfo " +
                    " WHERE obminfo_name='uid_max_used' " +
                    "   FOR UPDATE");
            rs = ps.executeQuery();

            int next_gid = 0;
            if (rs.next()) {
                next_gid = rs.getInt(1) + 1;
                JDBCUtils.cleanup(null, ps, rs);

                ps = conn.prepareStatement(
                        "UPDATE ObmInfo " +
                        "   SET obminfo_value = ? " +
                        " WHERE obminfo_name = 'uid_max_used'");
                ps.setInt(1, next_gid);
                ps.executeUpdate();
            } else {
                // Field not found, build it and then try again.
                buildNextUidField(conn);
                next_gid = getNextFreeGid(conn);
            }
            return next_gid;

        } finally {
            JDBCUtils.cleanup(null, ps, rs);
        }
    }

    /**
     * Check if the GroupExtId exists in the database
     *
     * @param conn          The connection to use for checking.
     * @param id            The id to check for.
     * @return              True, if it exists.
     * @throws SQLException If an SQL error occurred.
     */
    private boolean extIdExists(Connection conn, GroupExtId id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    " SELECT 1" +
                    "   FROM UGroup" +
                    "  WHERE group_ext_id = ?" +
                    "  LIMIT 1");
            ps.setString(1, id.getId());
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            JDBCUtils.cleanup(null, ps, rs);
        }
    }

    /**
     * Retrieve the internal group_id for the given group_ext_id.
     *
     * @param conn                          The SQL connection to use
     * @param domain                        The domain to query for
     * @param extId                         The group_ext_id to query for
     * @return                              The internal group_id
     * @throws SQLException                 If an SQL error occurred
     * @throws GroupNotFoundException       If the group was not found
     */
    private int getInternalGroupId(Connection conn, ObmDomain domain, GroupExtId extId) throws SQLException, GroupNotFoundException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(
                    "      SELECT group_id " +
                    "        FROM UGroup " +
                    "  INNER JOIN Domain ON group_domain_id = domain_id" +
                    "       WHERE group_ext_id = ?" +
                    "         AND domain_uuid = ?" +
                    "       LIMIT 1");

            ps.setString(1, extId.getId());
            ps.setString(2, domain.getUuid().get());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("group_id");
            } else {
                throw new GroupNotFoundException(extId);
            }
        } finally {
            JDBCUtils.cleanup(null, ps, rs);
        }
    }

    /**
     * Check if the given group has an ancestor with the given ancestor id
     *
     * @param conn              The SQL connection to use
     * @param domain            The domain to query on
     * @param groupId           The group id to check (child)
     * @param ancestorId        The ancestor id to check (parent, grandparent, ...)
     * @return                  The ancestor group containing the id
     * @throws SQLException
     */
    private GroupExtId hasAncestorWithId(Connection conn, ObmDomain domain, GroupExtId groupId, GroupExtId ancestorId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<String> rowIds = new ArrayList<String>();

        // First of all, adding a group to itself should fail
        if (groupId == ancestorId) {
            return ancestorId;
        }

        try {
            ps = conn.prepareStatement(
                 "      SELECT pgroup.group_ext_id" +
                 "        FROM GroupGroup " +
                 "  INNER JOIN UGroup AS pgroup ON groupgroup_parent_id = pgroup.group_id" +
                 "  INNER JOIN UGroup AS cgroup ON groupgroup_child_id = cgroup.group_id" +
                 "  INNER JOIN Domain ON pgroup.group_domain_id = domain_id" +
                 "       WHERE cgroup.group_ext_id = ?" +
                 "         AND domain_uuid = ?");
            ps.setString(1, groupId.getId()); // cgroup.group_ext_id
            ps.setString(2, domain.getUuid().get()); // domain_uuid

            rs = ps.executeQuery();

            // First go through all the ids to check if the direct parent
            // matches the parentId. This is faster in case there are a lot
            // of parents.

            while (rs.next()) {
                String extId = rs.getString("group_ext_id");
                if (extId == ancestorId.getId()) {
                    // Found a parent with the requested id
                    return ancestorId;
                }
                rowIds.add(extId);
            }
        } finally {
            JDBCUtils.cleanup(null, ps, rs);
        }

        // If we reach these lines, then the direct parent doesn't contain the
        // id (or there were no ids). Recursively check all parents.
        for (String rowId : rowIds) {
            GroupExtId foundParent = hasAncestorWithId(conn, domain,
                                                     GroupExtId.valueOf(rowId), ancestorId);
            if (foundParent != null) {
                return foundParent;
            }
        }

        // Nothing found, looks like we are good.
        return null;
    }
}
