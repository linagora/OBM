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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public interface UserDao {

	public static final String DB_INNER_FIELD_SEPARATOR = "\r\n";
	public static final int DEFAULT_GID = 1000;
	public static final int FIRST_UID = 1001;

	ObmUser findUser(String email, ObmDomain domain);

	ObmUser findUserByLogin(String login, ObmDomain domain);

	ObmUser findUserById(int id, ObmDomain domain);

	ObmUser getByExtId(UserExtId userExtId, ObmDomain domain) throws SQLException, UserNotFoundException;

	ObmUser getByExtIdWithGroups(UserExtId userExtId, ObmDomain domain) throws SQLException, UserNotFoundException;

	List<ObmUser> list(ObmDomain domain) throws SQLException;

	ObmUser create(ObmUser user) throws SQLException, DaoException;

	ObmUser update(ObmUser user) throws SQLException, UserNotFoundException;

	void delete(ObmUser user) throws SQLException, UserNotFoundException;

	void archive(ObmUser user) throws SQLException, UserNotFoundException;

	Map<String, String> loadUserProperties(int userObmId);

	Integer userIdFromEmail(Connection con, String email, Integer domainId) throws SQLException;

	ImmutableSet<String> getAllEmailsFrom(ObmDomain domain) throws SQLException;

}