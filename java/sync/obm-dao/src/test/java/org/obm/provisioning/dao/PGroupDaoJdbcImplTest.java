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
package org.obm.provisioning.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.dao.utils.TestUtils;
import org.obm.domain.dao.PGroupDao;
import org.obm.domain.dao.PGroupDaoJdbcImpl;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.Group;

import com.google.inject.Inject;


@Slow
@RunWith(SlowGuiceRunner.class)
@GuiceModule(PGroupDaoJdbcImplTest.Env.class)
public class PGroupDaoJdbcImplTest implements H2TestClass {

	public static class Env extends DaoTestModule {

		@Override
		protected void configureImpl() {
			bind(PGroupDao.class).to(PGroupDaoJdbcImpl.class);
		}

	}

	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;
	
	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	@Inject
	private PGroupDao dao;

	@Inject
	private TestUtils utils;

	@Before
	public void setUp() throws Exception {
		db.executeUpdate("INSERT INTO Category (category_domain_id, category_code) VALUES (1, 'CAT')");
		db.executeUpdate("INSERT INTO of_usergroup (of_usergroup_group_id,of_usergroup_user_id) VALUES (1, 1)");
		db.executeUpdate(
				"INSERT INTO CategoryLink " +
						"(   categorylink_category_id, " +
						"    categorylink_entity_id " +
						") " +
						"  SELECT 1, * " +
						"  FROM (SELECT groupentity_entity_id " +
						"                                FROM GroupEntity " +
						"                                WHERE groupentity_group_id = 1)");
		db.executeUpdate(
				"INSERT INTO Field " +
						"(   entity_id, " +
						"    field, " +
						"    value " +
						") " +
						"  SELECT *, 'field', 'value' " +
						"  FROM (SELECT groupentity_entity_id " +
						"                                FROM GroupEntity " +
						"                                WHERE groupentity_group_id = 1)");
		db.executeUpdate("INSERT INTO Contact (contact_domain_id, contact_lastname, contact_origin) VALUES (1, 'john.doe', 'test')");
		db.executeUpdate("INSERT INTO _contactgroup (contact_id, group_id) VALUES (1, 1)");
	}

	@Test
	public void testInsert() throws Exception {
		Group group = Group.builder().uid(Group.Id.valueOf(1)).build();

		dao.insert(group);

		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UGroup")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_GroupEntity")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_CategoryLink")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_of_usergroup")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_Field")).isEqualTo(1);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P__contactgroup")).isEqualTo(1);
	}

	@Test
	public void testDelete() throws Exception {
		db.executeUpdate(
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
							"  WHERE group_id=1");
			db.executeUpdate(
					"INSERT INTO P_GroupEntity " +
							"   (   groupentity_entity_id, " +
							"       groupentity_group_id " +
							"   ) SELECT    groupentity_entity_id," +
							"               groupentity_group_id" +
							"     FROM GroupEntity" +
							"     WHERE groupentity_group_id=1");
			db.executeUpdate(
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
							"                                WHERE groupentity_group_id = 1)");
			db.executeUpdate(
					"INSERT INTO P_of_usergroup " +
							"(   of_usergroup_group_id, " +
							"    of_usergroup_user_id " +
							") SELECT    of_usergroup_group_id, " +
							"            of_usergroup_user_id " +
							"  FROM of_usergroup " +
							"  WHERE of_usergroup_group_id=1");
			db.executeUpdate(
					"INSERT INTO P__contactgroup " +
							"(   contact_id, " +
							"    group_id " +
							") SELECT    contact_id, " +
							"            group_id " +
							"  FROM _contactgroup  WHERE group_id=1");

		Group group = Group.builder().uid(Group.Id.valueOf(1)).build();

		dao.delete(group);

		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_UGroup")).isEqualTo(0);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_GroupEntity")).isEqualTo(0);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_CategoryLink")).isEqualTo(0);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_of_usergroup")).isEqualTo(0);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P_Field")).isEqualTo(0);
		assertThat(utils.getIntFromQuery("SELECT COUNT(*) FROM P__contactgroup")).isEqualTo(0);
	}
}
