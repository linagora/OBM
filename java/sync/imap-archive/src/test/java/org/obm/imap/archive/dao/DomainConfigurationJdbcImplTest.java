/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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


package org.obm.imap.archive.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalTime;
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
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ExcludedUser;
import org.obm.imap.archive.beans.Mailing;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.sync.base.EmailAddress;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;

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
	
	private void play(Operation operation) {
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}

	private Operation delete() {
		return Operations.sequenceOf(
				Operations.deleteAllFrom(DomainConfigurationJdbcImpl.TABLE.NAME),
				Operations.deleteAllFrom(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME));
	}

	private Insert domainConfiguration() {
		return Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
			.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
					DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE,
					DomainConfigurationJdbcImpl.TABLE.FIELDS.ARCHIVE_MAIN_FOLDER,
					DomainConfigurationJdbcImpl.TABLE.FIELDS.EXCLUDED_FOLDER)
			.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32, "arChive", "excluded")
			.build();
	}	

	private Insert excludedUser(ExcludedUser excludedUser) {
		return Operations.insertInto(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME)
			.columns(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.DOMAIN_UUID, 
					DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_UUID,
					DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_LOGIN) 
			.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", excludedUser.serializeId(), excludedUser.getLogin())
			.build();
	}
	
	private Operation mailing(Mailing mailing) {
		ImmutableList.Builder<Insert> inserts = ImmutableList.builder();
		for (EmailAddress emailAddress : mailing.getEmailAddresses()) {
			inserts.add(Operations.insertInto(DomainConfigurationJdbcImpl.MAILING.TABLE.NAME)
				.columns(DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.DOMAIN_UUID, 
						DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.EMAIL) 
				.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", emailAddress.get())
				.build());
		}
		return Operations.sequenceOf(inserts.build());
	}
	
	@Test
	public void getShouldReturnStoredValueWhenDomainIdMatch() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration.getDomainId()).isEqualTo(uuid);
		assertThat(domainConfiguration.isEnabled()).isTrue();
		assertThat(domainConfiguration.getRepeatKind()).isEqualTo(RepeatKind.DAILY);
		assertThat(domainConfiguration.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
		assertThat(domainConfiguration.getDayOfMonth()).isEqualTo(DayOfMonth.of(10));
		assertThat(domainConfiguration.getDayOfYear()).isEqualTo(DayOfYear.of(355));
		assertThat(domainConfiguration.getHour()).isEqualTo(10);
		assertThat(domainConfiguration.getMinute()).isEqualTo(32);
		assertThat(domainConfiguration.getArchiveMainFolder()).isEqualTo("arChive");
		assertThat(domainConfiguration.getExcludedFolder()).isEqualTo("excluded");
		assertThat(domainConfiguration.getExcludedUsers()).isEmpty();
	}
	
	@Test
	public void getShouldReturnNullWhenDomainIdDoesntMatch() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("78d6e95b-ab6c-4625-b3bf-e86c68347e2d");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		assertThat(domainConfigurationJdbcImpl.get(domain)).isNull();
	}
	
	@Test
	public void getShouldLoadExcludedUsers() throws Exception {
		ExcludedUser excludedUser = ExcludedUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build();
		ExcludedUser excludedUser2 = ExcludedUser.builder()
				.id(UserExtId.valueOf("8e30e673-1c47-4ca8-85e8-4609d4228c10"))
				.login("userb")
				.build();
		play(Operations.sequenceOf(delete(), domainConfiguration(), excludedUser(excludedUser), excludedUser(excludedUser2)));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration.getExcludedUsers()).containsOnly(excludedUser, excludedUser2);
	}
	
	@Test
	public void getShouldLoadMailing() throws Exception {
		EmailAddress emailAddress = EmailAddress.loginAtDomain("user@mydomain.org");
		EmailAddress emailAddress2 = EmailAddress.loginAtDomain("user2@mydomain.org");
		Mailing mailing = Mailing.from(ImmutableList.of(emailAddress, emailAddress2));
		play(Operations.sequenceOf(delete(), domainConfiguration(), mailing(mailing)));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration.getMailing().getEmailAddresses()).containsOnly(emailAddress, emailAddress2);
	}

	@Test
	public void updateShouldThrowExceptionWhenDomainNotFound() throws Exception {
		expectedException.expect(DomainNotFoundException.class).hasMessage("844db7a6-6788-47a4-9f04-f5ed9f007a04");
		
		domainConfigurationJdbcImpl.update(DomainConfiguration.DEFAULT_VALUES_BUILDER
				.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("844db7a6-6788-47a4-9f04-f5ed9f007a04")).build())
				.build());
	}
	
	@Test
	public void updateShouldUpdateWhenDomainFound() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("anotherExcluded")
				.build();
		
		domainConfigurationJdbcImpl.update(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration).isEqualToComparingFieldByField(expectedDomainConfiguration);
	}
	
	@Test
	public void updateShouldUpdateExcludedUsers() throws Exception {
		ExcludedUser excludedUser = ExcludedUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build();
		ExcludedUser excludedUser2 = ExcludedUser.builder()
				.id(UserExtId.valueOf("8e30e673-1c47-4ca8-85e8-4609d4228c10"))
				.login("userb")
				.build();
		play(Operations.sequenceOf(delete(), domainConfiguration(), excludedUser(excludedUser), excludedUser(excludedUser2)));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		ExcludedUser excludedUser3 = ExcludedUser.builder()
				.id(UserExtId.valueOf("2d7a5942-46ab-4fad-9bd2-608bde249671"))
				.login("userc")
				.build();
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("anotherExcluded")
				.excludedUsers(ImmutableList.of(excludedUser, excludedUser3))
				.build();
		
		domainConfigurationJdbcImpl.update(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration.getExcludedUsers()).containsOnly(excludedUser, excludedUser3);
	}
	
	@Test
	public void updateShouldUpdateMailing() throws Exception {
		EmailAddress emailAddress = EmailAddress.loginAtDomain("user@mydomain.org");
		EmailAddress emailAddress2 = EmailAddress.loginAtDomain("user2@mydomain.org");
		Mailing mailing = Mailing.from(ImmutableList.of(emailAddress, emailAddress2));
		play(Operations.sequenceOf(delete(), domainConfiguration(), mailing(mailing)));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		EmailAddress emailAddress3 = EmailAddress.loginAtDomain("user3@mydomain.org");
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("anotherExcluded")
				.mailing(Mailing.from(ImmutableList.of(emailAddress, emailAddress3)))
				.build();
		
		domainConfigurationJdbcImpl.update(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain);
		assertThat(domainConfiguration.getMailing().getEmailAddresses()).containsOnly(emailAddress, emailAddress3);
	}
	
	@Test
	public void createShouldThrowExceptionWhenDomainConfigurationAlreadyExists() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		expectedException.expect(DaoException.class);
		
		domainConfigurationJdbcImpl.create(DomainConfiguration.DEFAULT_VALUES_BUILDER
				.domain(ObmDomain.builder().uuid(ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309")).build())
				.build());
	}
	
	@Test
	public void createShouldCreateWhenDomainFound() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("1383b12c-6d79-40c7-acf9-c79bcc673fff");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("excluded")
				.build();
		
		domainConfigurationJdbcImpl.create(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain); 
		assertThat(domainConfiguration).isEqualToComparingFieldByField(expectedDomainConfiguration);
	}
	
	@Test
	public void createShouldCreateExcludedUsers() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("1383b12c-6d79-40c7-acf9-c79bcc673fff");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		ExcludedUser excludedUser = ExcludedUser.builder()
				.id(UserExtId.valueOf("08607f19-05a4-42a2-9b02-6f11f3ceff3b"))
				.login("usera")
				.build();
		ExcludedUser excludedUser2 = ExcludedUser.builder()
				.id(UserExtId.valueOf("8e30e673-1c47-4ca8-85e8-4609d4228c10"))
				.login("userb")
				.build();
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("excluded")
				.excludedUsers(ImmutableList.of(excludedUser, excludedUser2))
				.build();
		
		domainConfigurationJdbcImpl.create(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain); 
		assertThat(domainConfiguration.getExcludedUsers()).containsOnly(excludedUser, excludedUser2);
	}
	
	@Test
	public void createShouldCreateMailing() throws Exception {
		play(Operations.sequenceOf(delete(), domainConfiguration()));
		
		ObmDomainUuid uuid = ObmDomainUuid.of("1383b12c-6d79-40c7-acf9-c79bcc673fff");
		ObmDomain domain = ObmDomain.builder().uuid(uuid).build();
		EmailAddress emailAddress = EmailAddress.loginAtDomain("user@mydomain.org");
		EmailAddress emailAddress2 = EmailAddress.loginAtDomain("user2@mydomain.org");
		DomainConfiguration expectedDomainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.DISABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.builder()
							.repeat(RepeatKind.YEARLY)
							.dayOfMonth(DayOfMonth.of(1))
							.dayOfWeek(DayOfWeek.MONDAY)
							.dayOfYear(DayOfYear.of(100))
							.build())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("ARcHIVE")
				.excludedFolder("excluded")
				.mailing(Mailing.from(ImmutableList.of(emailAddress, emailAddress2)))
				.build();
		
		domainConfigurationJdbcImpl.create(expectedDomainConfiguration);
		
		DomainConfiguration domainConfiguration = domainConfigurationJdbcImpl.get(domain); 
		assertThat(domainConfiguration.getMailing().getEmailAddresses()).containsOnly(emailAddress, emailAddress2);
	}
}
