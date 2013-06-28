/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the ‚ÄúOBM, Free
 * Communication by Linagora‚Äù Logo with the ‚ÄúYou are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !‚Äù infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression ‚ÄúEnterprise offer‚Äù and pro.obm.org, and (iii) refrain
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
package org.obm.provisioning.dao;

import org.obm.provisioning.beans.Group;
import org.obm.provisioning.beans.GroupExtId;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public interface GroupDao {
	
	/**
	 * Retrieve group information by group id. This will only retrieve group information, not the
	 * members of the group.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group
	 * @return							Group information without expanded users or groups
	 * @throws GroupNotFoundException	If the group with the passed extId is not found
	 * @throws DaoException				If an exception occurred retrieving the data
	 */
	Group get(ObmDomain domain, GroupExtId extId) throws GroupNotFoundException, DaoException;
	
	/**
	 * Recursively get group information and members.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group
	 * @param includeUsers				If true, users will be included in returned group information
	 * @param groupDepth				The search depth for nested groups. 
	 * 									  -1 means infinite, 0 means no groups
	 * @return							Group information with expanded users and groups.
	 * @throws DaoException				If the group with the passed extId is not found.
	 * @throws GroupNotFoundException	If an exception occurred retrieving the data.
	 */
	Group getRecursive(ObmDomain domain, GroupExtId extId, boolean includeUsers, int groupDepth) throws DaoException, GroupNotFoundException;

	/**
	 * Create a group with the specified information
	 * 
	 * @param info						The group information to use for creation.
	 * @return							The created group.
	 * @throws DaoException				If an exception occurred retrieving the data.
	 */
	Group create(ObmDomain domain, Group info) throws DaoException;
	
	/**
	 * Update the group with the specified information.
	 * 
	 * @param domain					The domain of the group
	 * @param info						The group information to use for updating.
	 * @return							The updated group.
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group is not found.
	 */
	Group update(ObmDomain domain, Group info) throws DaoException, GroupNotFoundException;
	
	/**
	 * Remove a group from the database.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group to remove.
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group is not found.
	 */
	void delete(ObmDomain domain, GroupExtId extId) throws DaoException, GroupNotFoundException;

	/**
	 * Add a user to a group. The user must exist.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group.		
	 * @param user						The user to add to the group
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group is not found.
	 * @throws UserNotFoundException	If the user is not found.
	 */
	void addUser(ObmDomain domain, GroupExtId extId, ObmUser user) throws DaoException, GroupNotFoundException, UserNotFoundException;
	
	/**
	 * Adds a subgroup to a group. The subgroup must exist.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group.
	 * @param subgroup					The subgroup to add.
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group or subgroup is not found
	 */
	void addSubgroup(ObmDomain domain, GroupExtId extId, Group subgroup) throws DaoException, GroupNotFoundException;
	
	/**
	 * Remove a user from a group.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group.
	 * @param user						The user to remove from the group.
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group is not found.
	 * @throws UserNotFoundException	If the user is not found.
	 */
	void removeUser(ObmDomain domain, GroupExtId extId, ObmUser user) throws DaoException, GroupNotFoundException, UserNotFoundException;
	
	/**
	 * Remove a subgroup from a group.
	 * 
	 * @param domain					The domain of the group
	 * @param extId						The external id of the group.
	 * @param subgroup					The subgroup to remove from the group
	 * @throws DaoException				If an exception occurred retrieving the data.
	 * @throws GroupNotFoundException	If the group is not found.
	 */
	void removeSubgroup(ObmDomain domain, GroupExtId extId, Group subgroup) throws DaoException, GroupNotFoundException;	
}
