/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class DomainConfigurationJdbcImplTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new DaoTestModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/mail_archive.sql"));

	@Inject
	private H2InMemoryDatabase db;
	
	@Inject
	private DomainConfigurationJdbcImpl domainConfigurationJdbcImpl;
	
	@Before
	public void setUp() {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom("mail_archive", "domain"),
						Operations.insertInto("domain")
						.columns("domain_id", "domain_name", "domain_label", "domain_uuid")
						.values(654, "my_domain_name", "my_domain.local", "a6af9131-60b6-4e3a-a9f3-df5b43a89309")
						.build(),
						Operations.insertInto("mail_archive")
						.columns("mail_archive_domain_id", 
								"mail_archive_activated", 
								"mail_archive_repeat_kind", 
								"mail_archive_day_of_week", 
								"mail_archive_day_of_month", 
								"mail_archive_day_of_year", 
								"mail_archive_hour", 
								"mail_archive_minute")
						.values(654, Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32)
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}	
	
	@Test
	public void getDomainConfigurationShouldReturnStoredValueWhenDomainIdMatch() throws Exception {
		UUID uuid = UUID.fromString("d0bad16f-7b79-4b08-aaf2-af2f2764673d");
		ObmDomain domain = ObmDomain.builder()
				.id(654)
				.uuid(ObmDomainUuid.of(uuid))
				.build();
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.getDomainConfiguration(domain);
		assertThat(domainConfiguration.getDomainId()).isEqualTo(uuid);
		assertThat(domainConfiguration.isEnabled()).isTrue();
		assertThat(domainConfiguration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(domainConfiguration.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
		assertThat(domainConfiguration.getDayOfMonth()).isEqualTo(DayOfMonth.of(10));
		assertThat(domainConfiguration.getDayOfYear()).isEqualTo(DayOfYear.of(355));
		assertThat(domainConfiguration.getHour()).isEqualTo(10);
		assertThat(domainConfiguration.getMinute()).isEqualTo(32);
	}
	
	@Test
	public void getDomainConfigurationShouldReturnNullWhenDomainIdDoesntMatch() throws Exception {
		UUID uuid = UUID.fromString("d0bad16f-7b79-4b08-aaf2-af2f2764673d");
		ObmDomain domain = ObmDomain.builder()
				.id(666)
				.uuid(ObmDomainUuid.of(uuid))
				.build();
		assertThat(domainConfigurationJdbcImpl.getDomainConfiguration(domain)).isNull();
	}
}
