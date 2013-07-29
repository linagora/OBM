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

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dao.utils.H2ConnectionProvider;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(UserPatternDaoJdbcImplTest.Env.class)
public class UserPatternDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
		}

	}

	@Inject
	private UserPatternDaoJdbcImpl dao;

	@Rule
	@Inject
	public H2InMemoryDatabase db;

	@Test
	public void testGetUserPatterns() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login("jdoe")
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.emailAndAliases("jdoe\r\njohn.doe")
				.domain(ToolBox.getDefaultObmDomain())
				.build();
		Set<String> patterns = ImmutableSet.of(
				"jdoe", // Login + first email (single occurence in the set)
				"john.doe", // Second email
				"John", // Firstname
				"Doe"); // Lastname

		assertThat(dao.getUserPatterns(user)).isEqualTo(patterns);
	}

	@Test
	public void testGetUserPatternsWithLoginOnly() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login("jdoe")
				.domain(ToolBox.getDefaultObmDomain())
				.build();
		Set<String> patterns = ImmutableSet.of("jdoe");

		assertThat(dao.getUserPatterns(user)).isEqualTo(patterns);
	}

	@Test
	public void testUpdateUserIndex() throws Exception {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login("jdoe")
				.lastName("Doe")
				.firstName("John")
				.commonName("J. Doe")
				.emailAndAliases("jdoe\r\njohn.doe")
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		dao.updateUserIndex(user);

		Set<String> patterns = Sets.newHashSet();
		Set<String> expectedPatterns = ImmutableSet.of("jdoe", "john.doe", "John", "Doe");
		ResultSet rs = db.execute("SELECT pattern FROM _userpattern WHERE id = ?", user.getUid());

		while (rs.next()) {
			patterns.add(rs.getString(1));
		}

		assertThat(patterns).isEqualTo(expectedPatterns);
	}

	@Test
	public void testUpdateUserIndexClearsOldIndex() throws Exception {
		db.executeUpdate("INSERT INTO _userpattern (id, pattern) VALUES (1, 'p1'), (1, 'p2')");

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login("jdoe")
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		dao.updateUserIndex(user);

		Set<String> patterns = Sets.newHashSet();
		Set<String> expectedPatterns = ImmutableSet.of("jdoe");
		ResultSet rs = db.execute("SELECT pattern FROM _userpattern WHERE id = ?", user.getUid());

		while (rs.next()) {
			patterns.add(rs.getString(1));
		}

		assertThat(patterns).isEqualTo(expectedPatterns);
	}

}
