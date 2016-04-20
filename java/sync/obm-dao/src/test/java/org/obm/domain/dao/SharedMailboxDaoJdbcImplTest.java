/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.sync.host.ObmHost;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.mailshare.SharedMailbox;

@RunWith(GuiceRunner.class)
@GuiceModule(SharedMailboxDaoJdbcImplTest.Env.class)
public class SharedMailboxDaoJdbcImplTest implements H2TestClass {

	private final ObmDomain domain = ObmDomain
			.builder()
			.id(1)
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.name("domain")
			.build();
	
	private final ObmHost mailHost = ObmHost
			.builder()
			.id(1)
			.name("mail")
			.fqdn("mail.tlse.lng")
			.ip("1.2.3.4")
			.domainId(domain.getId())
			.build();
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(new DaoTestModule());
			bind(SharedMailboxDao.class).to(SharedMailboxDaoJdbcImpl.class);
		}

	}

	@Inject
	private SharedMailboxDaoJdbcImpl dao;

	@Test
	public void testFindSharedMailboxById() throws Exception {
		SharedMailbox sharedMailbox = SharedMailbox.builder()
				.id(3)
				.domain(domain)
				.name("name")
				.archive(true)
				.quota(2)
				.server(mailHost)
				.delegation("delegation")
				.description("description")
				.email("email")
				.build();

		assertThat(dao.findSharedMailboxById(3, domain)).isEqualTo(sharedMailbox);
	}

	@Test
	public void testFindSharedMailboxByName() throws Exception {
		SharedMailbox sharedMailbox = SharedMailbox.builder()
				.id(3)
				.domain(domain)
				.name("name")
				.archive(true)
				.quota(2)
				.server(mailHost)
				.delegation("delegation")
				.description("description")
				.email("email")
				.build();

		assertThat(dao.findSharedMailboxByName("name", domain)).isEqualTo(sharedMailbox);
	}
}
