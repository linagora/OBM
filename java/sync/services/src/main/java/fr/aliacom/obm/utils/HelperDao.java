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

import org.obm.sync.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class HelperDao {

	private static final Logger logger = LoggerFactory.getLogger(HelperDao.class);

	private final ObmHelper obmHelper;

	@Inject
	protected HelperDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	public boolean canWriteOnCalendar(AccessToken accessToken, String login) {
		boolean ret = false;
		String q =
		// direct rights
		"select entityright_write "
				+ "from EntityRight "
				+ "inner join UserEntity on entityright_consumer_id=userentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userentity_user_id=? and userobm_login=?  "
				+ "and entityright_write=1 and userobm_email is not null "
				+ "and userobm_email != '' and userobm_archive != 1"
				+ " union "
				// public cals
				+ "select entityright_write "
				+ "from EntityRight "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userobm_login=? AND "
				+ // targetCalendar
				"entityright_consumer_id is null and entityright_write=1 and userobm_email is not null and userobm_email != '' "
				+ " and userobm_archive != 1 "

				+ " union "
				// group rights
				+ "select entityright_write "
				+ "from EntityRight "
				+ "inner join GroupEntity on entityright_consumer_id=groupentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = groupentity_group_id "
				+ "where of_usergroup_user_id=? and entityright_write=1  and userobm_login=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, accessToken.getObmId());
			ps.setString(2, login);
			ps.setString(3, login);
			ps.setInt(4, accessToken.getObmId());
			ps.setString(5, login);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret = ret || rs.getBoolean(1);
			}
		} catch (SQLException t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	/**
	 * Returns true if the logged in user can writer on the given user_login's
	 * calendar
	 */
	public boolean canReadCalendar(AccessToken accessToken, String login) {
		boolean ret = false;
		String q =
		// direct rights
		"select entityright_read "
				+ "from EntityRight "
				+ "inner join UserEntity on entityright_consumer_id=userentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userentity_user_id=? and userobm_login=?  "
				+ "and entityright_read=1 and userobm_email is not null "
				+ "and userobm_email != '' and userobm_archive != 1"
				+ " union "
				// public cals
				+ "select entityright_read "
				+ "from EntityRight "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "where userobm_login=? AND "
				+ // targetCalendar
				"entityright_consumer_id is null and entityright_read=1 and userobm_email is not null and userobm_email != '' "
				+ " and userobm_archive != 1 "

				+ " union "
				// group rights
				+ "select entityright_read "
				+ "from EntityRight "
				+ "inner join GroupEntity on entityright_consumer_id=groupentity_entity_id "
				+ "inner join CalendarEntity on calendarentity_entity_id=entityright_entity_id "
				+ "inner join UserObm on userobm_id=calendarentity_calendar_id "
				+ "inner join of_usergroup on of_usergroup_group_id = groupentity_group_id "
				+ "where of_usergroup_user_id=? and entityright_read=1  and userobm_login=? "
				+ "and userobm_email is not null and userobm_email != '' and userobm_archive != 1";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(1, accessToken.getObmId());
			ps.setString(2, login);
			ps.setString(3, login);
			ps.setInt(4, accessToken.getObmId());
			ps.setString(5, login);
			rs = ps.executeQuery();
			while (rs.next()) {
				ret = ret || rs.getBoolean(1);
			}
		} catch (SQLException t) {
			logger.error(t.getMessage(), t);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

}
