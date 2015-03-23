/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2013-2014 Linagora
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

import java.sql.SQLException;
import java.util.Set;

import org.obm.provisioning.Group;
import org.obm.provisioning.Group.Id;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupExistsException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public interface GroupDao {
    /**
     * Retrieve group information by group id. This will only retrieve group information, not the
     * members of the group.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group.
     * @return                          Group information without expanded users or groups.
     * @throws GroupNotFoundException   If the group with the passed extId is not found.
     * @throws DaoException             If an exception occurred retrieving the data.
     */
    Group get(ObmDomain domain, GroupExtId extId) throws GroupNotFoundException, DaoException;
    Group get(Id id) throws GroupNotFoundException, DaoException;

    /**
     * Recursively get group information and members.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group.
     * @param includeUsers              If true, users will be included in returned group information.
     * @param groupDepth                The search depth for nested groups.
     *                                    -1 means infinite, 0 means no groups
     * @return                          Group information with expanded users and groups.
     * @throws DaoException             If the group with the passed extId is not found.
     * @throws GroupNotFoundException   If an exception occurred retrieving the data.
     */
    Group getRecursive(ObmDomain domain, GroupExtId extId, boolean includeUsers, int groupDepth) throws DaoException, GroupNotFoundException;

    /**
     * Create a group with the specified information
     *
     * @param info                      The group information to use for creation.
     * @return                          The created group.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupExistsException     If the group already exists.
     */
    Group create(ObmDomain domain, Group info) throws DaoException, GroupExistsException;

    /**
     * Update the group with the specified information.
     *
     * @param domain                    The domain of the group.
     * @param info                      The group information to use for updating.
     * @return                          The updated group.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group is not found.
     */
    Group update(ObmDomain domain, Group info) throws DaoException, GroupNotFoundException;

    /**
     * Remove a group from the database.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group to remove.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group is not found.
     */
    void delete(ObmDomain domain, GroupExtId extId) throws DaoException, GroupNotFoundException;

    /**
     * Add a user to a group. The user must exist.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group.
     * @param user                      The user to add to the group
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group is not found.
     * @throws UserNotFoundException    If the user is not found.
     */
    void addUser(ObmDomain domain, GroupExtId extId, ObmUser user) throws DaoException, GroupNotFoundException, UserNotFoundException;

    /**
     * Adds a subgroup to a group. The subgroup must exist.
     *
     * @param domain                    The domain of the group.
     * @param group                     The external id of the group.
     * @param subgroup                  The external id of the subgroup.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group or subgroup is not found.
     * @throws GroupRecursionException  Adding the group would cause illegal recursion.
     */
    void addSubgroup(ObmDomain domain, GroupExtId group, GroupExtId subgroup) throws DaoException, GroupNotFoundException, GroupRecursionException;

    /**
     * Remove a user from a group.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group.
     * @param user                      The user to remove from the group.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group is not found.
     * @throws UserNotFoundException    If the user is not found.
     */
    void removeUser(ObmDomain domain, GroupExtId extId, ObmUser user) throws DaoException, GroupNotFoundException, UserNotFoundException;

    /**
     * Remove a subgroup from a group.
     *
     * @param domain                    The domain of the group.
     * @param extId                     The external id of the group.
     * @param subgroup                  The id of the subgroup to remove from the group.
     * @throws DaoException             If an exception occurred retrieving the data.
     * @throws GroupNotFoundException   If the group is not found.
     */
    void removeSubgroup(ObmDomain domain, GroupExtId extId, GroupExtId subgroup) throws DaoException, GroupNotFoundException;

    void addUser(ObmDomain domain, Group.Id groupId, ObmUser user) throws DaoException;

    Group getByGid(ObmDomain domain, int gid) throws DaoException;

    Set<Group> listPublicGroups(ObmDomain domain) throws DaoException;

    Set<Group> getAllPublicGroupsForUserExtId(ObmDomain domain, UserExtId userExtId) throws SQLException;

    /**
     * Recursively get parent group id of group
     *
     * @param domain						The domain of the group.
     * @param extId				 			The external id of the group.
     * @return                         			Set of parent Group Id
     * @throws DaoException             				If any dao error occurs
     * @throws GroupNotFoundException		If group id doesn't exist
     *
     */
    Set<Id> listParents(ObmDomain domain, GroupExtId groupId) throws DaoException, GroupNotFoundException;

}
