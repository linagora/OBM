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
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomain.Builder;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.domain.Samba;

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

	private ObmDomain.Builder globalVirtBuilder = ObmDomain
			.builder()
			.uuid(ObmDomainUuid.of("00000000-1111-2222-3333-444444444444"))
			.label("Global")
			.name("global.virt")
			.id(3)
			.global(true);
	private ObmDomain.Builder testTlseBuilder = ObmDomain
			.builder()
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.label("test.tlse.lng")
			.name("test.tlse.lng")
			.id(1)
			.global(false);
	private ObmDomain.Builder testTlse2Builder = ObmDomain
			.builder()
			.uuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf"))
			.label("test2.tlse.lng")
			.name("test2.tlse.lng")
			.id(2)
			.global(false)
			.samba(Samba.builder()
				.sid("S-1-5-21-1895063688-3870457350-1790443141")
				.profile("\\\\samba\\chemin\\profile")
				.home("\\\\samba\\chemin\\profile\\%u")
				.drive("E")
				.build());
	private ObmDomain.Builder withOneAliasBuilder = ObmDomain
			.builder()
			.uuid(ObmDomainUuid.of("3b7da76a-ff7c-46f6-bd5b-700cfb21c5e3"))
			.label("WithOneAlias")
			.name("domain.with.one.alias")
			.alias("one.alias.alias.1")
			.id(4)
			.global(false);
	private ObmDomain.Builder withThreeAliasBuilder = ObmDomain
			.builder()
			.uuid(ObmDomainUuid.of("227f8925-e95c-464d-9a37-032d2b045c7b"))
			.label("WithThreeAlias")
			.name("domain.with.three.alias")
			.aliases(ImmutableSet.of("three.alias.alias.1", "three.alias.alias.2", "three.alias.alias.3"))
			.id(5)
			.global(false);

	private ObmHost.Builder mailHostBuilder = ObmHost
			.builder()
			.id(1)
			.ip("1.2.3.4")
			.name("mail")
			.fqdn("mail.tlse.lng")
			.domainId(1);
	private ObmHost.Builder syncHostBuilder = ObmHost
			.builder()
			.id(2)
			.ip("1.2.3.5")
			.name("sync")
			.fqdn("sync.tlse.lng")
			.domainId(1);
	
	@Test
	public void testCreateThenGet() {
		Builder domainBuilder = ObmDomain
				.builder()
				.uuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"))
				.name("mydomain")
				.label("my domain");

		dao.create(domainBuilder.build());

		assertThat(dao.findDomainByName("mydomain")).isEqualTo(domainBuilder.id(6).global(false).build());
	}

	@Test
	public void testGetFetchesDomainHosts() {
		ObmDomain domain = dao.findDomainByName("test.tlse.lng");
		ObmHost mailHost = mailHostBuilder.build();
		ObmHost syncHost = syncHostBuilder.build();
		ImmutableMultimap<ServiceProperty, ObmHost> hosts = ImmutableMultimap
				.<ServiceProperty, ObmHost>builder()
				.put(ServiceProperty.SMTP_IN, mailHost)
				.put(ServiceProperty.IMAP, mailHost)
				.put(ServiceProperty.OBM_SYNC, syncHost)
				.build();

		assertThat(domain.getHosts()).isEqualTo(hosts);
	}

	@Test
	public void findDomainShouldFetchesSambaProperties() {
		ObmDomain domain = dao.findDomainByName("test2.tlse.lng");
		Samba samba = domain.getSamba().get();
		assertThat(samba).isNotNull();
		assertThat(samba.getSid()).isEqualTo("S-1-5-21-1895063688-3870457350-1790443141");
		assertThat(samba.getProfile()).isEqualTo("\\\\samba\\chemin\\profile");
		assertThat(samba.getHome()).isEqualTo("\\\\samba\\chemin\\profile\\%u");
		assertThat(samba.getDrive()).isEqualTo("E");
	}

	@Test(expected=DomainNotFoundException.class)
	public void testGetByUuidWhenDomainDoesntExist() throws DaoException, DomainNotFoundException {
		dao.findDomainByUuid(ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66"));
	}

	@Test
	public void testCreateThenGetByUuid() throws DaoException, DomainNotFoundException {
		ObmDomainUuid uuid = ObmDomainUuid.of("dcf3a388-6dc4-4ac1-bf4f-88c5e4457a66");
		Builder domainBuilder = ObmDomain
				.builder()
				.uuid(uuid)
				.name("mydomain")
				.label("my domain")
				.global(false);

		dao.create(domainBuilder.build());

		assertThat(dao.findDomainByUuid(uuid)).isEqualTo(domainBuilder.id(6).global(false).build());
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
				domainBuilder.id(6).build(),
				globalVirtBuilder.build(),
				testTlseBuilder.build(),
				testTlse2Builder.build(),
				withOneAliasBuilder.build(),
				withThreeAliasBuilder.build());
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
				domainBuilder.id(6).build(),
				globalVirtBuilder.build(),
				testTlseBuilder.build(),
				testTlse2Builder.build(),
				withOneAliasBuilder.build(),
				withThreeAliasBuilder.build());
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
				domainBuilder.id(6).alias("myalias1").alias("myalias2").build(),
				globalVirtBuilder.build(),
				testTlseBuilder.build(),
				testTlse2Builder.build(),
				withOneAliasBuilder.build(),
				withThreeAliasBuilder.build());
	}
	
	@Test
	public void testFindDomainByNameIsGlobal() {
		assertThat(dao.findDomainByName("global.virt").isGlobal()).isTrue();
	}
	
	@Test
	public void testFindDomainByNameIsNotGlobal() {
		assertThat(dao.findDomainByName("test.tlse.lng").isGlobal()).isFalse();
	}

	@Test
	public void testFindDomainByNameShouldSucceedWhenDomainExists() {
		assertThat(dao.findDomainByName("test.tlse.lng")).isEqualTo(testTlseBuilder.build());
	}

	@Test
	public void testFindDomainByNameShouldReturnNullWhenDomainDoesntExist() {
		assertThat(dao.findDomainByName("i.dont.exist")).isNull();
	}

	@Test
	public void testFindDomainByNameShouldSucceedWithOneAlias() {
		assertThat(dao.findDomainByName("one.alias.alias.1")).isEqualTo(withOneAliasBuilder.build());
	}

	@Test
	public void testFindDomainByNameShouldSucceedWithMultipleAliases() {
		assertThat(dao.findDomainByName("three.alias.alias.3")).isEqualTo(withThreeAliasBuilder.build());
	}

	@Test
	public void testFindDomainByNameShouldFetchMailChooserHookIdWhenDefined() {
		assertThat(dao.findDomainByName("test.tlse.lng").getMailChooserHookId()).isEqualTo(98);
	}

	@Test
	public void testFindDomainByNameShouldNotFetchMailChooserHookIdWhenNotDefined() {
		assertThat(dao.findDomainByName("domain.with.one.alias").getMailChooserHookId()).isNull();
	}

	@Test
	public void testFindDomainByUUIDShouldFetchMailChooserHookIdWhenDefined() throws Exception {
		assertThat(dao.findDomainByUuid(ObmDomainUuid.of("3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf")).getMailChooserHookId()).isEqualTo(99);
	}

	@Test
	public void testFindDomainByUUIDShouldNotFetchMailChooserHookIdWhenNotDefined() throws Exception {
		assertThat(dao.findDomainByUuid(ObmDomainUuid.of("3b7da76a-ff7c-46f6-bd5b-700cfb21c5e3")).getMailChooserHookId()).isNull();
	}

}
