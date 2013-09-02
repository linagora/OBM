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
package org.obm.provisioning.dao;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.TestUtils;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.PUserDao;
import org.obm.domain.dao.PUserDaoJdbcImpl;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(PUserDaoJdbcImplTest.Env.class)
public class PUserDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(PUserDao.class).to(PUserDaoJdbcImpl.class);
		}

	}

	@Inject
	private PUserDao dao;

	@Rule
	public H2InMemoryDatabase db = new H2InMemoryDatabase("sql/initial.sql");;

	@Inject
	private TestUtils utils;

	@Before
	public void setUp() throws Exception {
		db.executeUpdate("INSERT INTO Entity (entity_mailing) VALUES (TRUE)");
		db.executeUpdate(
				"INSERT INTO MailboxEntity (mailboxentity_entity_id, mailboxentity_mailbox_id) " +
						"SELECT *, 2 " +
						"FROM (SELECT MAX(entity_id) FROM Entity)");
		db.executeUpdate("INSERT INTO Entity (entity_mailing) VALUES (TRUE)");
		db.executeUpdate(
				"INSERT INTO MailboxEntity (mailboxentity_entity_id, mailboxentity_mailbox_id) " +
						"SELECT * " +
						"FROM (SELECT MAX(entity_id) FROM Entity), " +
						"(SELECT userobm_id FROM userobm WHERE userobm_ext_id='6')");

		db.executeUpdate(
				"INSERT INTO EntityRight "
						+
						"(   "
						+
						"    entityright_entity_id, "
						+
						"    entityright_consumer_id, "
						+
						"    entityright_read, "
						+
						"    entityright_write, "
						+
						"    entityright_admin, "
						+
						"    entityright_access) "
						+
						"SELECT *, 1, 1, 1, 0, 0 "
						+
						"FROM (SELECT mailboxentity_entity_id FROM MailboxEntity WHERE mailboxentity_mailbox_id=2)");
		db.executeUpdate(
				"INSERT INTO EntityRight "
						+
						"(   "
						+
						"    entityright_entity_id, "
						+
						"    entityright_consumer_id, "
						+
						"    entityright_read, "
						+
						"    entityright_write, "
						+
						"    entityright_admin, "
						+
						"    entityright_access) "
						+
						"SELECT *, 1, 1, 1, 0, 0 "
						+
						"FROM (SELECT mailboxentity_entity_id FROM MailboxEntity WHERE mailboxentity_mailbox_id=6)");
		db.executeUpdate("INSERT INTO Category (category_domain_id, category_code) VALUES (1, 'CAT')");
		db.executeUpdate(
				"INSERT INTO CategoryLink " +
						"(   categorylink_category_id, " +
						"    categorylink_entity_id " +
						") " +
						"  SELECT 1, * " +
						"  FROM (SELECT userentity_entity_id " +
						"                                FROM UserEntity " +
						"                                WHERE userentity_user_id = 4)");
		db.executeUpdate(
				"INSERT INTO CategoryLink " +
						"(   categorylink_category_id, " +
						"    categorylink_entity_id " +
						") " +
						"  SELECT 1, * " +
						"  FROM (SELECT userentity_entity_id " +
						"                                FROM UserEntity " +
						"                                WHERE userentity_user_id = 6)");
		db.executeUpdate(
				"INSERT INTO Field " +
						"(   entity_id, " +
						"    field, " +
						"    value " +
						") " +
						"  SELECT *, 'field', 'value' " +
						"  FROM (SELECT userentity_entity_id " +
						"                                FROM UserEntity " +
						"                                WHERE userentity_user_id = 1)");
		db.executeUpdate(
				"INSERT INTO Field " +
						"(   entity_id, " +
						"    field, " +
						"    value " +
						") " +
						"  SELECT *, 'field', 'value' " +
						"  FROM (SELECT userentity_entity_id " +
						"                                FROM UserEntity " +
						"                                WHERE userentity_user_id = 6)");
		db.executeUpdate(
				"INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id)" +
				"VALUES" +
				"	(1, 1)," +
				"	(1, 2)," +
				"	(1, 3)," +
				"	(1, 4)," +
				"	(1, 5)," +
				"	(1, 6)");
	}

	@Test
	public void testInsertByUserExtIds() throws Exception {
		dao.insert(obmUser(1, "1"));
		dao.insert(obmUser(2, "2"));
		dao.insert(obmUser(3, "3"));
		dao.insert(obmUser(4, "4"));
		dao.insert(obmUser(5, "5"));

		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UserObm")).isEqualTo(5);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UserEntity")).isEqualTo(5);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_MailboxEntity")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_EntityRight")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_CategoryLink")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_Field")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_of_usergroup")).isEqualTo(5);
	}

	@Test
	public void testDeleteByUserExtIds() throws Exception {
		db.executeUpdate("INSERT INTO P_UserObm " +
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
				"FROM UserObm");
		db.executeUpdate("INSERT INTO P_UserEntity SELECT * FROM UserEntity");
		db.executeUpdate("INSERT INTO P_MailboxEntity SELECT * FROM MailboxEntity");
		db.executeUpdate("INSERT INTO P_EntityRight SELECT * FROM EntityRight");
		db.executeUpdate("INSERT INTO P_CategoryLink SELECT * FROM CategoryLink");
		db.executeUpdate("INSERT INTO P_Field SELECT * FROM Field");
		db.executeUpdate("INSERT INTO P_of_usergroup SELECT * FROM of_usergroup");

		dao.delete(obmUser(1, "1"));
		dao.delete(obmUser(2, "2"));
		dao.delete(obmUser(3, "3"));
		dao.delete(obmUser(4, "4"));
		dao.delete(obmUser(5, "5"));


		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UserObm")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UserEntity")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_MailboxEntity")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_EntityRight")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_CategoryLink")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_Field")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_of_usergroup")).isEqualTo(1);
	}
	
	@Test
	public void testArchive() throws Exception {
		db.executeUpdate("INSERT INTO P_UserObm " +
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
				"FROM UserObm");
		
		dao.archive(obmUser(1, "1"));
		dao.archive(obmUser(2, "2"));
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UserObm WHERE userobm_archive=1")).isEqualTo(2);
	}

	private ObmUser obmUser(int id, String extId) {
		return ObmUser.builder()
				.login("dummy")
				.uid(id)
				.extId(UserExtId.valueOf(extId))
				.domain(ObmDomain.builder().build())
				.build();
	}
}
