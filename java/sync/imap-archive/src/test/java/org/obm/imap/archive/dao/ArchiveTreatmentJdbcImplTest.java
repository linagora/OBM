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
import static org.assertj.guava.api.Assertions.assertThat;

import org.joda.time.DateTime;
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
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.provisioning.dao.exceptions.DaoException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveTreatmentJdbcImplTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new DaoTestModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/mail_archive_run.sql"));

	@Inject
	private H2InMemoryDatabase db;
	
	@Inject
	private ArchiveTreatmentJdbcImpl archiveTreatmentJdbcImpl;
	
	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setUp() {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom(ArchiveTreatmentJdbcImpl.TABLE.NAME),
						Operations.insertInto(ArchiveTreatmentJdbcImpl.TABLE.NAME)
						.columns(ArchiveTreatmentJdbcImpl.TABLE.FIELDS.UUID,
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.STATUS, 
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.START, 
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.END, 
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.LOWER_BOUNDARY, 
								ArchiveTreatmentJdbcImpl.TABLE.FIELDS.HIGHER_BOUNDARY)
						.values("c3c5cb24-f5df-45ed-8918-99c7555a02c4",
								"633bdb12-bb8a-4943-9dd0-6a6e48051517", 
								ArchiveStatus.WARNING, 
								DateTime.parse("2014-06-01T00:00:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:01:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:02:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:03:00.000Z").toDate())
						.values("a860eecd-e608-4cbe-9d7a-6ef907b56367",
								"633bdb12-bb8a-4943-9dd0-6a6e48051517", 
								ArchiveStatus.SUCCESS, 
								DateTime.parse("2014-07-01T00:00:00.000Z").toDate(), 
								DateTime.parse("2014-07-01T00:01:00.000Z").toDate(), 
								DateTime.parse("2014-07-01T00:02:00.000Z").toDate(), 
								DateTime.parse("2014-07-01T00:03:00.000Z").toDate())
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}	
	
	@Test
	public void getLastArchiveTreatmentShouldReturnLastStoredValueWhenDomainIdMatch() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("633bdb12-bb8a-4943-9dd0-6a6e48051517");
		ArchiveTreatment archiveTreatment = archiveTreatmentJdbcImpl.getLastArchiveTreatment(uuid).get();
		assertThat(archiveTreatment.getRunId()).isEqualTo(ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367"));
		assertThat(archiveTreatment.getDomainId()).isEqualTo(uuid);
		assertThat(archiveTreatment.getArchiveStatus()).isEqualTo(ArchiveStatus.SUCCESS);
		assertThat(archiveTreatment.getStart()).isEqualTo(DateTime.parse("2014-07-01T00:00:00.000Z"));
		assertThat(archiveTreatment.getEnd()).isEqualTo(DateTime.parse("2014-07-01T00:01:00.000Z"));
		assertThat(archiveTreatment.getLowerBoundary()).isEqualTo(DateTime.parse("2014-07-01T00:02:00.000Z"));
		assertThat(archiveTreatment.getHigherBoundary()).isEqualTo(DateTime.parse("2014-07-01T00:03:00.000Z"));
	}
	
	@Test
	public void getLastArchiveTreatmentShouldReturnNullWhenDomainIdDoesntMatch() throws Exception {
		ObmDomainUuid uuid = ObmDomainUuid.of("07c48baf-cf15-4f27-bc04-f227a9dbf71f");
		assertThat(archiveTreatmentJdbcImpl.getLastArchiveTreatment(uuid)).isAbsent();
	}
	
	@Test
	public void insertShouldReturnMatchingValues() throws Exception {
		ArchiveTreatment expectedArchiveTreatment = ArchiveTreatment.builder()
				.runId(ArchiveTreatmentRunId.from("bc139d31-5ffb-4174-a5e3-ac33d0b9f204"))
				.domainId(ObmDomainUuid.of("74c66801-44f1-4bb2-b334-08053cb4ad53"))
				.archiveStatus(ArchiveStatus.ERROR)
				.start(DateTime.parse("2014-07-02T00:00:00.000Z"))
				.end(DateTime.parse("2014-07-02T00:01:00.000Z"))
				.lowerBoundary(DateTime.parse("2014-07-02T00:02:00.000Z"))
				.higherBoundary(DateTime.parse("2014-07-02T00:03:00.000Z"))
				.build();
		
		archiveTreatmentJdbcImpl.insert(expectedArchiveTreatment);
		ArchiveTreatment archiveTreatment = archiveTreatmentJdbcImpl.getLastArchiveTreatment(expectedArchiveTreatment.getDomainId()).get();
		assertThat(archiveTreatment).isEqualToComparingFieldByField(expectedArchiveTreatment);
	}
	
	@Test
	public void insertShouldThrowWhenDuplicateRunId() throws Exception {
		ArchiveTreatment expectedArchiveTreatment = ArchiveTreatment.builder()
				.runId(ArchiveTreatmentRunId.from("a860eecd-e608-4cbe-9d7a-6ef907b56367"))
				.domainId(ObmDomainUuid.of("633bdb12-bb8a-4943-9dd0-6a6e48051517"))
				.archiveStatus(ArchiveStatus.ERROR)
				.start(DateTime.parse("2014-07-02T00:00:00.000Z"))
				.end(DateTime.parse("2014-07-02T00:01:00.000Z"))
				.lowerBoundary(DateTime.parse("2014-07-02T00:02:00.000Z"))
				.higherBoundary(DateTime.parse("2014-07-02T00:03:00.000Z"))
				.build();
		
		expectedException.expect(DaoException.class);
		
		archiveTreatmentJdbcImpl.insert(expectedArchiveTreatment);
	}
}
