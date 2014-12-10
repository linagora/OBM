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

import java.util.EnumSet;

import org.assertj.guava.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.ElementNotFoundException;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.beans.ArchiveRunningTreatment;
import org.obm.imap.archive.beans.ArchiveScheduledTreatment;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTerminatedTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun;
import org.obm.provisioning.dao.exceptions.DaoException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveTreatmentJdbcImplTest {

	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new DaoTestModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/mail_archive_run.sql"));

	@Inject H2InMemoryDatabase db;
	@Inject ArchiveTreatmentJdbcImpl testee;
	ObmDomainUuid domainUuid;
	
	@Before
	public void setUp() {
		domainUuid = ObmDomainUuid.of("633bdb12-bb8a-4943-9dd0-6a6e48051517");
		Operation operation = Operations.deleteAllFrom(MailArchiveRun.NAME);
		new DbSetup(H2Destination.from(db), operation).launch();
	}	

	@Test
	public void insertShouldThrowWhenDuplicateRunId() throws Exception {
		ArchiveScheduledTreatment treatment = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		expectedException.expect(DaoException.class);
		
		testee.insert(treatment);
		testee.insert(treatment);
	}
	
	@Test
	public void findAllScheduledOrRunningShouldReturnEmptyWhenNone() throws Exception {
		assertThat(testee.findAllScheduledOrRunning()).isEmpty();
	}
	
	@Test
	public void findAllScheduledOrRunningShouldReturnOnlyMatching() throws Exception {
		ArchiveScheduledTreatment scheduled = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();
		
		ArchiveRunningTreatment running = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.build();
		
		ArchiveTerminatedTreatment terminated = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a5ac1bc7-7c2d-415e-9933-00c073146d41")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(scheduled);
		testee.insert(running);
		testee.insert(terminated);
		
		assertThat(testee.findAllScheduledOrRunning())
			.containsOnlyOnce(scheduled, running);
	}
	
	@Test
	public void findAllScheduledOrRunningShouldReturnSortedByScheduleTime() throws Exception {
		ArchiveScheduledTreatment treatment1 = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("d7a88445-053c-49dc-964a-f38e867ae62a")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2056-01-02T11:11Z"))
				.build();
		ArchiveRunningTreatment treatment2 = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId("94c4856e-aae3-46d6-acd8-7c40d81ff309")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2056-01-01T10:22Z"))
				.startedAt(DateTime.parse("2056-02-01T10:22Z"))
				.build();
		ArchiveRunningTreatment treatment3 = ArchiveRunningTreatment
				.forDomain(ObmDomainUuid.of("72e2be30-ad54-4115-84f2-471fa2688805"))
				.runId("9d53ef2b-1853-48fe-93c5-e39627fb0c4a")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2056-01-03T20:20Z"))
				.startedAt(DateTime.parse("2056-02-02T10:22Z"))
				.build();
		ArchiveScheduledTreatment treatment4 = ArchiveScheduledTreatment
				.forDomain(ObmDomainUuid.of("72e2be30-ad54-4115-84f2-471fa2688805"))
				.runId("879e9046-ad73-446a-be66-824ef745de63")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2056-01-02T20:33Z"))
				.build();
		
		testee.insert(treatment1);
		testee.insert(treatment2);
		testee.insert(treatment3);
		testee.insert(treatment4);
		
		assertThat(testee.findAllScheduledOrRunning()).containsExactly(
				treatment2, treatment1, treatment4, treatment3);
	}
	
	@Test
	public void findByScheduledTimeShouldReturnEmptyWhenNone() throws Exception {
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(5))).isEmpty();
	}

	@Test
	public void findByScheduledTimeShouldReturnOnlyDomainEntries() throws Exception {
		ArchiveScheduledTreatment otherDomain = ArchiveScheduledTreatment
				.forDomain(ObmDomainUuid.of("254933bc-fad8-488e-98cd-f302c2a22fb3"))
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		ArchiveTerminatedTreatment expectedDomain = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(otherDomain);
		testee.insert(expectedDomain);
		
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(5))).containsOnlyOnce(expectedDomain);
	}
	
	@Test
	public void findByScheduledTimeShouldReturnRespectLimitParameter() throws Exception {
		ArchiveScheduledTreatment one = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveRunningTreatment three = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-01-01T00:03:00Z"))
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(2))).containsOnlyOnce(one, three);
	}
	
	@Test
	public void findByScheduledTimeShouldReturnSortedByScheduleTime() throws Exception {
		ArchiveScheduledTreatment one = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveRunningTreatment three = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-01-01T00:03:00Z"))
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(5))).containsOnlyOnce(two, three, one);
	}
	
	@Test
	public void findLastTerminatedShouldReturnEmptyWhenNone() throws Exception {
		assertThat(testee.findLastTerminated(domainUuid, Limit.from(3))).isEmpty();
	}

	@Test
	public void findLastTerminatedShouldReturnOnlyDomainEntries() throws Exception {
		ArchiveTerminatedTreatment otherDomain = ArchiveTerminatedTreatment
				.forDomain(ObmDomainUuid.of("254933bc-fad8-488e-98cd-f302c2a22fb3"))
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		ArchiveTerminatedTreatment expectedDomain = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(otherDomain);
		testee.insert(expectedDomain);
		
		assertThat(testee.findLastTerminated(domainUuid, Limit.from(2))).containsOnly(expectedDomain);
	}
	
	@Test
	public void findLastTerminatedShouldReturnOnlyTerminatedTreatments() throws Exception {
		ArchiveScheduledTreatment one = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveRunningTreatment three = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-01-01T00:03:00Z"))
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.findLastTerminated(domainUuid, Limit.from(3))).containsOnly(two);
	}
	
	@Test
	public void findLastTerminatedShouldReturnMoreRecentSortedByScheduleTime() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.findLastTerminated(domainUuid, Limit.from(3))).containsOnly(one, three, two);
	}
	
	@Test
	public void removeShouldDropTreatment() throws Exception {
		ArchiveScheduledTreatment treatment = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		testee.insert(treatment);
		testee.remove(treatment.getRunId());
		
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(5))).isEmpty();
	}
	
	@Test
	public void removeShouldTriggerExceptionWhenUuidNotFound() throws Exception {
		expectedException.expect(ElementNotFoundException.class);
		testee.remove(ArchiveTreatmentRunId.from("013f9981-9c51-400f-81a5-2dfc48e3925f"));
	}

	@Test
	public void updateShouldTriggerExceptionWhenUuidNotFound() throws Exception {
		expectedException.expect(ElementNotFoundException.class);
		
		testee.update(ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build());
	}
	
	@Test
	public void updateShouldChangeAllFields() throws Exception {
		ArchiveScheduledTreatment created = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-11-11T00:00:00Z"))
				.scheduledAt(DateTime.parse("2014-11-11T11:00:00Z"))
				.build();

		ArchiveTerminatedTreatment terminated = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-02-02T00:00:00Z"))
				.scheduledAt(DateTime.parse("2014-02-02T02:00:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		testee.insert(created);
		testee.update(terminated);
		
		assertThat(testee.findByScheduledTime(domainUuid, Limit.from(5))).containsOnlyOnce(terminated);
	}
	
	@Test(expected=NullPointerException.class)
	public void findShouldThrowNPEWhenNullRunId() throws DaoException {
		testee.find(null);
	}

	@Test
	public void findShouldReturnAbsentWhenNoMatch() throws DaoException {
		Assertions.assertThat(testee.find(ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367")))
			.isAbsent();
	}

	@Test
	public void findShouldReturnTreatmentIfScheduled() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367");
		ArchiveScheduledTreatment treatment = ArchiveScheduledTreatment
				.forDomain(ObmDomainUuid.of("254933bc-fad8-488e-98cd-f302c2a22fb3"))
				.runId(runId)
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.build();

		testee.insert(treatment);
		
		assertThat(testee.find(runId).get()).isEqualTo(treatment);
	}

	@Test
	public void findShouldReturnTreatmentIfRunning() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367");
		ArchiveRunningTreatment treatment = ArchiveRunningTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-01-01T00:03:00Z"))
				.build();

		testee.insert(treatment);
		
		assertThat(testee.find(runId).get()).isEqualTo(treatment);
	}

	@Test
	public void findShouldReturnTreatmentIfError() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367");
		ArchiveTerminatedTreatment treatment = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(false)
				.higherBoundary(DateTime.parse("2014-02-02T00:00:00Z"))
				.scheduledAt(DateTime.parse("2014-02-02T02:00:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(treatment);
		
		assertThat(testee.find(runId).get()).isEqualTo(treatment);
	}

	@Test
	public void findShouldReturnTreatmentIfSuccess() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367");
		ArchiveTerminatedTreatment treatment = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-02-02T00:00:00Z"))
				.scheduledAt(DateTime.parse("2014-02-02T02:00:00Z"))
				.startedAt(DateTime.parse("2014-07-06T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(treatment);
		
		assertThat(testee.find(runId).get()).isEqualTo(treatment);
	}
	
	@Test(expected=NullPointerException.class)
	public void historyShouldThrowWhenNoDomain() throws Exception {
		testee.history(null, ArchiveStatus.TERMINATED, Limit.unlimited(), Ordering.NONE);
	}
	
	@Test(expected=NullPointerException.class)
	public void historyShouldThrowWhenNoStatus() throws Exception {
		testee.history(domainUuid, null, Limit.unlimited(), Ordering.NONE);
	}
	
	@Test(expected=NullPointerException.class)
	public void historyShouldThrowWhenNoLimit() throws Exception {
		testee.history(domainUuid, ArchiveStatus.TERMINATED, null, Ordering.NONE);
	}
	
	@Test(expected=NullPointerException.class)
	public void historyShouldThrowWhenNoOrdering() throws Exception {
		testee.history(domainUuid, ArchiveStatus.TERMINATED, Limit.unlimited(), null);
	}

	@Test
	public void historyShouldReturnEmptyListWhenNoEntry() throws Exception {
		assertThat(testee.history(domainUuid, ArchiveStatus.TERMINATED, Limit.unlimited(), Ordering.NONE)).isEmpty();
	}
	
	@Test
	public void historyShouldFilterDomain() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(ObmDomainUuid.of("72e2be30-ad54-4115-84f2-471fa2688805"))
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.history(domainUuid, ArchiveStatus.TERMINATED, Limit.unlimited(), Ordering.NONE)).containsOnlyOnce(one, three);
	}
	
	@Test
	public void historyShouldFilterFailure() throws Exception {
		ArchiveScheduledTreatment one = ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SCHEDULED)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.history(domainUuid, EnumSet.of(ArchiveStatus.ERROR), Limit.unlimited(), Ordering.NONE)).containsOnlyOnce(three);
	}
	
	@Test
	public void historyShouldLimit() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.history(domainUuid, ArchiveStatus.TERMINATED, Limit.from(1), Ordering.NONE)).containsOnlyOnce(one);
	}
	
	@Test
	public void historyShouldSort() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid) 
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.history(domainUuid, ArchiveStatus.TERMINATED, Limit.unlimited(), Ordering.DESC)).containsExactly(one, three, two);
	}
	
	@Test
	public void historyShouldFilterAllFields() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid) 
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);
		
		assertThat(testee.history(domainUuid, EnumSet.of(ArchiveStatus.ERROR), Limit.from(2), Ordering.DESC)).containsExactly(one, two);
	}
	
	@Test(expected=NullPointerException.class)
	public void deleteAllShouldThrowWhenDomainIsNull() throws Exception {
		testee.deleteAll(null);
	}
	
	@Test
	public void deleteAllShouldDeleteAllRunFromAdomain() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		ObmDomainUuid domainUuid2 = ObmDomainUuid.of("72e2be30-ad54-4115-84f2-471fa2688805");
		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid2)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid) 
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);

		testee.deleteAll(domainUuid);
		assertThat(testee.findLastTerminated(domainUuid, Limit.unlimited())).isEmpty();
		assertThat(testee.findLastTerminated(domainUuid2, Limit.unlimited())).containsOnly(two);
	}
	
	@Test
	public void deleteAllShouldNotDeleteWhenNone() throws Exception {
		ArchiveTerminatedTreatment one = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("a860eecd-e608-4cbe-9d7a-6ef907b56367")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-05T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.ERROR)
				.build();

		ArchiveTerminatedTreatment two = ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("21d3c634-5f5a-4e4d-bf89-dec6e699f007")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-01T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		ArchiveTerminatedTreatment three = ArchiveTerminatedTreatment
				.forDomain(domainUuid) 
				.runId("31534c25-2012-45d7-9808-586a488e6c8b")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-07-01T00:03:00Z"))
				.scheduledAt(DateTime.parse("2014-07-02T00:03:00Z"))
				.startedAt(DateTime.parse("2014-11-11T00:03:00Z"))
				.terminatedAt(DateTime.parse("2014-07-06T11:11:00Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();

		testee.insert(one);
		testee.insert(two);
		testee.insert(three);

		ObmDomainUuid domainUuid2 = ObmDomainUuid.of("72e2be30-ad54-4115-84f2-471fa2688805");
		testee.deleteAll(domainUuid2);
		assertThat(testee.findLastTerminated(domainUuid, Limit.unlimited())).containsOnly(one, two, three);
		assertThat(testee.findLastTerminated(domainUuid2, Limit.unlimited())).isEmpty();
	}
}
