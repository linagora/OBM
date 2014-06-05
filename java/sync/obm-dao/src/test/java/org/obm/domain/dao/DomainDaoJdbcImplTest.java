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
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomain.Builder;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@RunWith(GuiceRunner.class)
@GuiceModule(DomainDaoJdbcImplTest.Env.class)
public class DomainDaoJdbcImplTest implements H2TestClass {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(new DaoTestModule());
			bind(DomainDao.class);
		}

	}
	
	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}

	@Inject
	private DomainDao dao;
	
	@Test
	public void testCreateThenGet() {
		Builder domainBuilder = ObmDomain.builder()
			.uuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"))
			.name("mydomain")
			.label("my domain");
		dao.create(domainBuilder.build());
		assertThat(dao.findDomainByName("mydomain")).isEqualTo(domainBuilder.id(4).global(false).build());
	}

	@Test
	public void testGetFetchesDomainHosts() {
		ObmDomain domain = dao.findDomainByName("test.tlse.lng");
		ObmHost mailHost = ObmHost.builder()
				.id(1)
				.ip("1.2.3.4")
				.name("mail")
				.fqdn("mail.tlse.lng")
				.domainId(1)
				.build();
		ObmHost syncHost = ObmHost.builder()
				.id(2)
				.ip("1.2.3.5")
				.name("sync")
				.fqdn("sync.tlse.lng")
				.domainId(1)
				.build();
		ImmutableMultimap<ServiceProperty, ObmHost> hosts = ImmutableMultimap
				.<ServiceProperty, ObmHost>builder()
				.put(ServiceProperty.SMTP_IN, mailHost)
				.put(ServiceProperty.IMAP, mailHost)
				.put(ServiceProperty.OBM_SYNC, syncHost)
				.build();

		assertThat(domain.getHosts()).isEqualTo(hosts);
	}

	@Test(expected=DomainNotFoundException.class)
	public void testGetByUuidWhenDomainDoesntExist() throws DaoException, DomainNotFoundException {
		dao.findDomainByUuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"));
	}

	@Test
	public void testCreateThenGetByUuid() throws DaoException, DomainNotFoundException {
		ObmDomainUuid uuid = ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66");
		Builder domainBuilder = ObmDomain.builder()
			.uuid(uuid)
			.name("mydomain")
			.label("my domain")
			.global(false);

		dao.create(domainBuilder.build());

		assertThat(dao.findDomainByUuid(uuid)).isEqualTo(domainBuilder.id(4).global(false).build());
	}
	
	@Test
	public void testCreateThenList() throws DaoException {
		Builder domainBuilder = ObmDomain.builder()
			.uuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"))
			.name("mydomain")
			.label("my domain")
			.global(false);
		dao.create(domainBuilder.build());
		assertThat(dao.list()).containsOnly(
				domainBuilder.id(4).build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("00000000-1111-2222-3333-444444444444"))
					.label("Global")
					.name("global.virt")
					.id(3)
					.global(true)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
					.label("test2.tlse.lng")
					.name("test2.tlse.lng")
					.id(2)
					.global(false)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
					.label("test.tlse.lng")
					.name("test.tlse.lng")
					.id(1)
					.global(false)
					.build()
					);
	}
	
	@Test
	public void testCreateWithAliasThenList() throws DaoException {
		Builder domainBuilder = ObmDomain.builder()
			.uuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"))
			.name("mydomain")
			.label("my domain")
			.alias("myalias")
			.global(false);
		dao.create(domainBuilder.build());
		assertThat(dao.list()).containsOnly(
				domainBuilder.id(4).alias("myalias").build(), 
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("00000000-1111-2222-3333-444444444444"))
					.label("Global")
					.name("global.virt")
					.id(3)
					.global(true)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
					.label("test2.tlse.lng")
					.name("test2.tlse.lng")
					.id(2)
					.global(false)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
					.label("test.tlse.lng")
					.name("test.tlse.lng")
					.id(1)
					.global(false)
					.build()
					);
	}
	
	@Test
	public void testCreateWithEmptyAliasThenGet() throws DaoException, DomainNotFoundException {
		final ObmDomainUuid uuid = ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66");
		Builder domainBuilder = ObmDomain.builder()
			.uuid(uuid)
			.name("mydomain")
			.label("my domain")
			.alias("")
			.global(false);
		dao.create(domainBuilder.build());
		final ObmDomain domainFromDao = dao.findDomainByUuid(uuid);
		assertThat(domainFromDao.getAliases()).isEmpty();
	}

	@Test
	public void testCreateWithAliasesThenList() throws DaoException {
		Builder domainBuilder = ObmDomain.builder()
			.uuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"))
			.name("mydomain")
			.label("my domain")
			.global(false)
			.aliases(ImmutableList.of("myalias1", "myalias2"));
		dao.create(domainBuilder.build());
		assertThat(dao.list()).containsOnly(
				domainBuilder.id(4).aliases(ImmutableList.of("myalias2", "myalias1")).build(), 
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("00000000-1111-2222-3333-444444444444"))
					.label("Global")
					.name("global.virt")
					.id(3)
					.global(true)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
					.label("test2.tlse.lng")
					.name("test2.tlse.lng")
					.id(2)
					.global(false)
					.build(),
				ObmDomain.builder()
					.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
					.label("test.tlse.lng")
					.name("test.tlse.lng")
					.global(false)
					.id(1)
					.build()
				);
	}
	
	@Test
	public void testFindDomainByNameIsGlobal() {
		ObmDomain domain = dao.findDomainByName("global.virt");

		assertThat(domain.isGlobal()).isTrue();
	}
	
	@Test
	public void testFindDomainByNameIsNotGlobal() {
		ObmDomain domain = dao.findDomainByName("test.tlse.lng");

		assertThat(domain.isGlobal()).isFalse();
	}
}
