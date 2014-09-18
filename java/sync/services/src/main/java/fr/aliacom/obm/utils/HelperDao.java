/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package fr.aliacom.obm.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import org.obm.push.utils.jdbc.StringSQLCollectionHelper;
import org.obm.sync.Right;
import org.obm.sync.auth.AccessToken;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HelperDao {

	private final ObmHelper obmHelper;

	@Inject
	protected HelperDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	public Map<String, EnumSet<Right>> listRightsOnCalendars(AccessToken accessToken, Collection<String> logins) throws SQLException {
		if (logins.isEmpty()) {
			return ImmutableMap.of();
		}

		StringSQLCollectionHelper sqlHelper = new StringSQLCollectionHelper(logins);
		return executeRightsQuery(buildRightsQuery(sqlHelper), accessToken, sqlHelper);
	}

	private static String buildRightsQuery(StringSQLCollectionHelper sqlHelper) {
		// Use UNION ALL instead of UNION - no need to use the implicit DISTINCT in our case
		return MessageFormat.format(
		"SELECT userobm_login, SUM(entityright_access) AS access_rights, SUM(entityright_read) AS read_rights, "
					+ "SUM(entityright_write) AS write_rights "
				+ "FROM "
				+ "("
				+ "SELECT userobm_login, entityright_access, entityright_read, entityright_write "
				+ "FROM CalendarEntity "
				+ "INNER JOIN UserObm ON userobm_id=calendarentity_calendar_id "
				+ "INNER JOIN EntityRight ON calendarentity_entity_id=entityright_entity_id "
				+ "INNER JOIN UserEntity ON entityright_consumer_id=userentity_entity_id "
				+ "WHERE userentity_user_id=? AND userobm_login IN ({0}) "
				+ "AND NULLIF(userobm_email, '''') IS NOT NULL "
				+ "AND userobm_archive != 1 "
				+ "UNION ALL "
				// public cals
				+ "SELECT userobm_login, entityright_access, entityright_read, entityright_write "
				+ "FROM CalendarEntity "
				+ "INNER JOIN UserObm ON userobm_id=calendarentity_calendar_id "
				+ "INNER JOIN EntityRight ON calendarentity_entity_id=entityright_entity_id "
				+ "WHERE userobm_login IN ({0}) AND "
				+ // targetCalendar
				"entityright_consumer_id IS NULL AND NULLIF(userobm_email, '''') IS NOT NULL "
				+ "AND userobm_archive != 1 "

				+ "UNION ALL "
				// group rights
				+ "SELECT userobm_login, entityright_access, entityright_read, entityright_write "
				+ "FROM CalendarEntity "
				+ "INNER JOIN UserObm ON userobm_id=calendarentity_calendar_id "
				+ "INNER JOIN EntityRight ON calendarentity_entity_id=entityright_entity_id "
				+ "INNER JOIN GroupEntity ON entityright_consumer_id=groupentity_entity_id "
				+ "INNER JOIN of_usergroup ON of_usergroup_group_id = groupentity_group_id "
				+ "WHERE of_usergroup_user_id=? AND userobm_login IN ({0}) "
				+ "AND userobm_email IS NOT NULL AND NULLIF(userobm_email, '''') IS NOT NULL AND userobm_archive != 1"

				+ ") rights "
				+ "GROUP BY userobm_login",
				sqlHelper.asPlaceHolders());
	}

	private Map<String, EnumSet<Right>> executeRightsQuery(String query, AccessToken accessToken, StringSQLCollectionHelper sqlHelper)
			throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ImmutableMap.Builder<String, EnumSet<Right>> builder = ImmutableMap.builder();
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);
			int index = 1;
			ps.setInt(index++, accessToken.getObmId());
			index = sqlHelper.insertValues(ps, index);
			index = sqlHelper.insertValues(ps, index);
			ps.setInt(index++, accessToken.getObmId());
			index = sqlHelper.insertValues(ps, index);
			rs = ps.executeQuery();
			while (rs.next()) {
				String login = rs.getString("userobm_login");
				int accessRightsInt = rs.getInt("access_rights");
				int readRightsInt = rs.getInt("read_rights");
				int writeRightsInt = rs.getInt("write_rights");

				EnumSet<Right> rights = EnumSet.noneOf(Right.class);
				if (accessRightsInt > 0) {
					rights.add(Right.ACCESS);
				}
				if (readRightsInt > 0) {
					rights.add(Right.READ);
				}
				if (writeRightsInt > 0) {
					rights.add(Right.WRITE);
				}
				builder.put(login, rights);
			}
			return builder.build();
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}
}
