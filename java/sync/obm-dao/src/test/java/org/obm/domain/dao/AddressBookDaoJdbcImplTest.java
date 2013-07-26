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
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.AddressBook.Builder;
import org.obm.sync.book.AddressBook.Id;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Names;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(AddressBookDaoJdbcImplTest.Env.class)
public class AddressBookDaoJdbcImplTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bindConstant().annotatedWith(Names.named("initialSchema")).to("sql/initial.sql");

			bind(DatabaseConnectionProvider.class).to(H2ConnectionProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
		}

	}

	@Inject
	private AddressBookDaoJdbcImpl dao;

	@Rule
	@Inject
	public H2InMemoryDatabase db;

	@Test
	public void testGet() throws Exception {
		db.executeUpdate("INSERT INTO AddressBook (domain_id, origin, name, owner) VALUES (?, ?, ?, ?)", 1, "tests", "bookOfUser1", 1);

		AddressBook book = AddressBook
				.builder()
				.uid(Id.valueOf(1))
				.name("bookOfUser1")
				.origin("tests")
				.syncable(true)
				.build();

		assertThat(dao.get(Id.valueOf(1))).isEqualTo(book);
	}

	@Test
	public void testGetWhenNotFound() throws Exception {
		assertThat(dao.get(Id.valueOf(1))).isNull();
	}

	@Test
	public void testGetAfterCreate() throws Exception {
		Builder builder = AddressBook
				.builder()
				.name("bookOfUser1")
				.origin("tests")
				.syncable(false)
				.defaultBook(true);
		ObmUser owner = ToolBox.getDefaultObmUser();

		dao.create(builder.build(), owner);

		assertThat(dao.get(Id.valueOf(1))).isEqualTo(builder
				.uid(Id.valueOf(1))
				.build());
	}

	@Test(expected = DaoException.class)
	public void testCreateWhenOwnerDoesntExist() throws Exception {
		AddressBook book = AddressBook
				.builder()
				.uid(Id.valueOf(1))
				.name("bookOfUser1")
				.origin("tests")
				.syncable(false)
				.defaultBook(true)
				.build();
		ObmUser owner = ObmUser
				.builder()
				.uid(9999)
				.domain(ToolBox.getDefaultObmDomain())
				.login("login")
				.build();

		dao.create(book, owner);
	}

	@Test
	public void testEnableAddressBookSynchronization() throws Exception {
		Builder builder = AddressBook
				.builder()
				.name("bookOfUser1")
				.origin("tests")
				.syncable(false)
				.defaultBook(true);
		ObmUser owner = ToolBox.getDefaultObmUser();
		AddressBook book = dao.create(builder.build(), owner);

		dao.enableAddressBookSynchronization(book.getUid(), owner);

		ResultSet rs = db.execute("SELECT COUNT(*) FROM SyncedAddressBook WHERE user_id = ? and addressbook_id = ?", owner.getUid(), book.getUid().getId());

		assertThat(rs.next()).isTrue();
		assertThat(rs.getInt(1)).isEqualTo(1);
	}

}
