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
import java.sql.SQLException;

import javax.inject.Inject;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;

import fr.aliacom.obm.common.user.ObmUser;

public class PUserDaoJdbcImpl implements PUserDao {

	private DatabaseConnectionProvider dbcp;

	@Inject
	public PUserDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public void insert(ObmUser user) throws DaoException {
		Connection conn = null;

		try {
			conn = dbcp.getConnection();
			String userObmQuery = "INSERT INTO P_UserObm " +
					"(userobm_id, " +
					"userobm_domain_id, " +
					"userobm_timecreate, " +
					"userobm_userupdate, " +
					"userobm_usercreate, " +
					"userobm_local, " +
					"userobm_ext_id, " +
					"userobm_system, " +
					"userobm_archive, " +
					"userobm_timelastaccess, " +
					"userobm_login, " +
					"userobm_nb_login_failed, " +
					"userobm_password_type, " +
					"userobm_password, " +
					"userobm_password_dateexp, " +
					"userobm_account_dateexp, " +
					"userobm_perms, " +
					"userobm_delegation_target, " +
					"userobm_delegation, " +
					"userobm_calendar_version, " +
					"userobm_uid, " +
					"userobm_gid, " +
					"userobm_datebegin, " +
					"userobm_hidden, " +
					"userobm_kind, " +
					"userobm_commonname, " +
					"userobm_lastname, " +
					"userobm_firstname, " +
					"userobm_title, " +
					"userobm_sound, " +
					"userobm_company, " +
					"userobm_direction, " +
					"userobm_service, " +
					"userobm_address1, " +
					"userobm_address2, " +
					"userobm_address3, " +
					"userobm_zipcode, " +
					"userobm_town, " +
					"userobm_expresspostal, " +
					"userobm_country_iso3166, " +
					"userobm_phone, " +
					"userobm_phone2, " +
					"userobm_mobile, " +
					"userobm_fax, " +
					"userobm_fax2, " +
					"userobm_web_perms, " +
					"userobm_web_list, " +
					"userobm_web_all, " +
					"userobm_mail_perms, " +
					"userobm_mail_ext_perms, " +
					"userobm_email, " +
					"userobm_mail_server_id, " +
					"userobm_mail_quota, " +
					"userobm_mail_quota_use, " +
					"userobm_mail_login_date, " +
					"userobm_nomade_perms, " +
					"userobm_nomade_enable, " +
					"userobm_nomade_local_copy, " +
					"userobm_email_nomade, " +
					"userobm_vacation_enable, " +
					"userobm_vacation_datebegin, " +
					"userobm_vacation_dateend, " +
					"userobm_vacation_message, " +
					"userobm_samba_perms, " +
					"userobm_samba_home, " +
					"userobm_samba_home_drive, " +
					"userobm_samba_logon_script, " +
					"userobm_status, " +
					"userobm_host_id, " +
					"userobm_description, " +
					"userobm_location, " +
					"userobm_education, " +
					"userobm_photo_id " +
					") SELECT    userobm_id, " +
					"userobm_domain_id, " +
					"userobm_timecreate, " +
					"userobm_userupdate, " +
					"userobm_usercreate, " +
					"userobm_local, " +
					"userobm_ext_id, " +
					"userobm_system, " +
					"userobm_archive, " +
					"userobm_timelastaccess, " +
					"userobm_login, " +
					"userobm_nb_login_failed, " +
					"userobm_password_type, " +
					"userobm_password, " +
					"userobm_password_dateexp, " +
					"userobm_account_dateexp, " +
					"userobm_perms, " +
					"userobm_delegation_target, " +
					"userobm_delegation, " +
					"userobm_calendar_version, " +
					"userobm_uid, " +
					"userobm_gid, " +
					"userobm_datebegin, " +
					"userobm_hidden, " +
					"userobm_kind, " +
					"userobm_commonname, " +
					"userobm_lastname, " +
					"userobm_firstname, " +
					"userobm_title, " +
					"userobm_sound, " +
					"userobm_company, " +
					"userobm_direction, " +
					"userobm_service, " +
					"userobm_address1, " +
					"userobm_address2, " +
					"userobm_address3, " +
					"userobm_zipcode, " +
					"userobm_town, " +
					"userobm_expresspostal, " +
					"userobm_country_iso3166, " +
					"userobm_phone, " +
					"userobm_phone2, " +
					"userobm_mobile, " +
					"userobm_fax, " +
					"userobm_fax2, " +
					"userobm_web_perms, " +
					"userobm_web_list, " +
					"userobm_web_all, " +
					"userobm_mail_perms, " +
					"userobm_mail_ext_perms, " +
					"userobm_email, " +
					"userobm_mail_server_id, " +
					"userobm_mail_quota, " +
					"userobm_mail_quota_use, " +
					"userobm_mail_login_date, " +
					"userobm_nomade_perms, " +
					"userobm_nomade_enable, " +
					"userobm_nomade_local_copy, " +
					"userobm_email_nomade, " +
					"userobm_vacation_enable, " +
					"userobm_vacation_datebegin, " +
					"userobm_vacation_dateend, " +
					"userobm_vacation_message, " +
					"userobm_samba_perms, " +
					"userobm_samba_home, " +
					"userobm_samba_home_drive, " +
					"userobm_samba_logon_script, " +
					"userobm_status, " +
					"userobm_host_id, " +
					"userobm_description, " +
					"userobm_location, " +
					"userobm_education, " +
					"userobm_photo_id " +
					"FROM UserObm " +
					"WHERE userobm_id=?";
			userQuery(conn, userObmQuery, user);
			userQuery(conn,
					"INSERT INTO P_UserEntity " +
							"(   userentity_entity_id, " +
							"    userentity_user_id " +
							") SELECT    userentity_entity_id, " +
							"            userentity_user_id " +
							"  FROM UserEntity " +
							"  WHERE userentity_user_id=?",
					user);
			userQuery(conn,
					"INSERT INTO P_CategoryLink " +
							"(   categorylink_category_id, " +
							"   categorylink_entity_id, " +
							"    categorylink_category " +
							") SELECT    categorylink_category_id, " +
							"            categorylink_entity_id, " +
							"            categorylink_category " +
							"  FROM CategoryLink " +
							"  WHERE categorylink_entity_id=(SELECT userentity_entity_id " +
							"                                FROM UserEntity " +
							"                                WHERE userentity_user_id = ?)",
					user);
			userQuery(conn,
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
							"  WHERE entity_id=(SELECT userentity_entity_id " +
							"                                FROM UserEntity " +
							"                                WHERE userentity_user_id = ?)",
					user);
			userQuery(conn,
					"INSERT INTO P_MailboxEntity " +
							"(   mailboxentity_entity_id, " +
							"    mailboxentity_mailbox_id " +
							") SELECT    mailboxentity_entity_id, " +
							"            mailboxentity_mailbox_id " +
							"  FROM MailboxEntity " +
							"  WHERE mailboxentity_mailbox_id = ?",
					user);
			userQuery(conn,
					"INSERT INTO P_EntityRight " +
							"(   entityright_id, " +
							"    entityright_entity_id, " +
							"    entityright_consumer_id, " +
							"    entityright_read, " +
							"    entityright_write, " +
							"    entityright_admin, " +
							"    entityright_access " +
							") SELECT    entityright_id, " +
							"            entityright_entity_id, " +
							"            entityright_consumer_id, " +
							"            entityright_read, " +
							"            entityright_write, " +
							"            entityright_admin, " +
							"            entityright_access " +
							"  FROM EntityRight " +
							"  WHERE entityright_entity_id=(SELECT mailboxentity_entity_id " +
							"                                FROM MailboxEntity " +
							"                                WHERE mailboxentity_mailbox_id = ?)",
					user);
			userQuery(conn,
					"INSERT INTO P_of_usergroup " +
							"(   of_usergroup_group_id, " +
							"    of_usergroup_user_id " +
							") SELECT    of_usergroup_group_id, " +
							"            of_usergroup_user_id " +
							"  FROM of_usergroup " +
							"  WHERE of_usergroup_user_id=?", user);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, null, null);
		}
	}

	@Override
	public void delete(ObmUser user)
			throws DaoException {
		Connection conn = null;
		try {
			conn = dbcp.getConnection();
			userQuery(conn,
					"DELETE FROM P_EntityRight " +
							"WHERE entityright_entity_id=(SELECT mailboxentity_entity_id " +
							"                                FROM P_MailboxEntity " +
							"                                WHERE mailboxentity_mailbox_id = ?)",
					user);
			userQuery(conn,
					"DELETE FROM P_MailboxEntity WHERE mailboxentity_mailbox_id = ?",
					user);
			userQuery(conn,
					"DELETE FROM P_CategoryLink " +
							"WHERE categorylink_entity_id=(SELECT userentity_entity_id " +
							"                                FROM UserEntity " +
							"                                WHERE userentity_user_id = ?)",
					user);
			userQuery(conn,
					"DELETE FROM P_field " +
							"WHERE entity_id=(SELECT userentity_entity_id " +
							"                                FROM UserEntity " +
							"                                WHERE userentity_user_id = ?)",
					user);
			userQuery(conn,
					"DELETE FROM P_UserEntity WHERE userentity_user_id=?",
					user);
			userQuery(conn,
					"DELETE FROM P_UserObm WHERE userobm_id=?", user);
			userQuery(conn,
					"DELETE FROM P_of_usergroup WHERE of_usergroup_user_id=?", user);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, null, null);
		}
	}
	
	@Override
	public void archive(ObmUser user) throws DaoException {
		Connection conn = null;

		try {
			conn = dbcp.getConnection();
			userQuery(conn, "UPDATE P_UserObm SET userobm_archive = 1 WHERE userobm_id=?", user);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(conn, null, null);
		}
	}

	private void userQuery(Connection conn, String query, ObmUser user)
			throws SQLException {
		PreparedStatement statement = null;
		try {
			statement = conn.prepareStatement(query);
			statement.setInt(1, user.getUid());
			statement.executeUpdate();
		} finally {
			JDBCUtils.cleanup(null, statement, null);
		}
	}

}
