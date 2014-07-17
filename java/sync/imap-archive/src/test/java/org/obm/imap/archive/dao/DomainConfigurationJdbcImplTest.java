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

import org.joda.time.LocalTime;
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
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

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
	
	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setUp() {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom(DomainConfigurationJdbcImpl.TABLE.NAME),
						Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
						.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
								DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE)
						.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32)
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}	
	
	@Test
	public void getShouldReturnStoredValueWhenDomainIdMatch() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(uuid);
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
	public void getShouldReturnNullWhenDomainIdDoesntMatch() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("78d6e95b-ab6c-4625-b3bf-e86c68347e2d");
		assertThat(domainConfigurationJdbcImpl.get(uuid)).isNull();
	}
	
	@Test
	public void updateShouldThrowExceptionWhenDomainNotFound() throws Exception {
		expectedException.expect(DomainNotFoundException.class).hasMessage("844db7a6-6788-47a4-9f04-f5ed9f007a04");
		
		domainConfigurationJdbcImpl.update(DomainConfiguration.DEFAULT_VALUES_BUILDER
				.domainId(ObmDomainUuid.of("844db7a6-6788-47a4-9f04-f5ed9f007a04"))
				.build());
	}
	
	@Test
	public void updateShouldUpdateWhenDomainFound() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domainId(uuid)
				.enabled(false)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		
		domainConfigurationJdbcImpl.update(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(uuid);
		assertThat(domainConfiguration).isEqualToComparingFieldByField(expectedDomainConfiguration);
	}
	
	@Test
	public void createShouldThrowExceptionWhenDomainConfigurationAlreadyExists() throws Exception {
		expectedException.expect(DaoException.class);
		
		domainConfigurationJdbcImpl.create(DomainConfiguration.DEFAULT_VALUES_BUILDER
				.domainId(ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309"))
				.build());
	}
	
	@Test
	public void createShouldCreateWhenDomainFound() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("1383b12c-6d79-40c7-acf9-c79bcc673fff");
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domainId(uuid)
				.enabled(false)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		
		domainConfigurationJdbcImpl.create(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(uuid); 
		assertThat(domainConfiguration).isEqualToComparingFieldByField(expectedDomainConfiguration);
	}
}
