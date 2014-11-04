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
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.dao.SqlTables.MailArchiveRun;
import org.obm.provisioning.dao.exceptions.DaoException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

public class ProcessedFolderJdbcImplTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new AbstractModule() {
				
				@Override
				protected void configure() {
					install(new DaoTestModule());
					bind(ImapFolderDao.class).to(ImapFolderJdbcImpl.class);
				}
			}))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/mail_archive_processed_folder.sql"));

	@Inject
	private H2InMemoryDatabase db;
	
	@Inject
	private ProcessedFolderJdbcImpl processedFolderJdbcImpl;
	
	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Before
	public void setUp() {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom(ImapFolderJdbcImpl.TABLE.NAME),
						Operations.insertInto(ImapFolderJdbcImpl.TABLE.NAME)
							.columns(ImapFolderJdbcImpl.TABLE.FIELDS.FOLDER)
							.values("user/usera/Test@mydomain.org")
							.build(),
						Operations.deleteAllFrom(MailArchiveRun.NAME),
						Operations.insertInto(MailArchiveRun.NAME)
						.columns(MailArchiveRun.Fields.UUID,
								MailArchiveRun.Fields.DOMAIN_UUID,
								MailArchiveRun.Fields.STATUS, 
								MailArchiveRun.Fields.SCHEDULE,
								MailArchiveRun.Fields.START, 
								MailArchiveRun.Fields.END, 
								MailArchiveRun.Fields.HIGHER_BOUNDARY)
						.values("c3c5cb24-f5df-45ed-8918-99c7555a02c4",
								"633bdb12-bb8a-4943-9dd0-6a6e48051517", 
								ArchiveStatus.SCHEDULED, 
								DateTime.parse("2014-06-01T00:00:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:01:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:02:00.000Z").toDate(), 
								DateTime.parse("2014-06-01T00:03:00.000Z").toDate())
						.build(),
						Operations.deleteAllFrom(ProcessedFolderJdbcImpl.TABLE.NAME),
						Operations.insertInto(ProcessedFolderJdbcImpl.TABLE.NAME)
						.columns(ProcessedFolderJdbcImpl.TABLE.FIELDS.RUN_ID,
								ProcessedFolderJdbcImpl.TABLE.FIELDS.FOLDER_ID,
								ProcessedFolderJdbcImpl.TABLE.FIELDS.LASTUID,
								ProcessedFolderJdbcImpl.TABLE.FIELDS.START,
								ProcessedFolderJdbcImpl.TABLE.FIELDS.END,
								ProcessedFolderJdbcImpl.TABLE.FIELDS.STATUS)
						.values("c3c5cb24-f5df-45ed-8918-99c7555a02c4",
								1,
								12,
								DateTime.parse("2014-06-01T00:02:02.000Z").toDate(),
								DateTime.parse("2014-06-01T00:02:04.000Z").toDate(),
								ArchiveStatus.SUCCESS)
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}
	
	@Test
	public void getShouldReturnWhenMatchingKey() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c3c5cb24-f5df-45ed-8918-99c7555a02c4");
		ImapFolder imapFolder = ImapFolder.from("user/usera/Test@mydomain.org");
		
		Optional<ProcessedFolder> optionProcessedFolder = processedFolderJdbcImpl.get(runId, imapFolder);
		assertThat(optionProcessedFolder).isPresent();
		
		ProcessedFolder processedFolder = optionProcessedFolder.get();
		assertThat(processedFolder.getRunId()).isEqualTo(runId);
		assertThat(processedFolder.getFolder()).isEqualTo(imapFolder);
		assertThat(processedFolder.getLastUid()).isEqualTo(12);
		assertThat(processedFolder.getStart()).isEqualTo(DateTime.parse("2014-06-01T00:02:02.000Z"));
		assertThat(processedFolder.getEnd()).isEqualTo(DateTime.parse("2014-06-01T00:02:04.000Z"));
		assertThat(processedFolder.getStatus()).isEqualTo(ArchiveStatus.SUCCESS);
	}
	
	@Test
	public void getShouldReturnAbsentWhenRunIdDoesntMatch() throws Exception {
		assertThat(processedFolderJdbcImpl.get(
				ArchiveTreatmentRunId.from("3094f38e-e339-43a1-8c16-dc330ce775a6"), ImapFolder.from("user/usera/Test@mydomain.org")))
			.isAbsent();
	}
	
	@Test
	public void getShouldReturnAbsentWhenFolderDoesntMatch() throws Exception {
		assertThat(processedFolderJdbcImpl.get(
				ArchiveTreatmentRunId.from("c3c5cb24-f5df-45ed-8918-99c7555a02c4"), ImapFolder.from("unknow")))
			.isAbsent();
	}
	
	@Test
	public void insertShouldReturnMatchingValues() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c3c5cb24-f5df-45ed-8918-99c7555a02c4");
		ImapFolder imapFolder = ImapFolder.from("user/usera/NewOne@mydomain.org");
		ProcessedFolder expectedProcessedFolder = ProcessedFolder.builder()
				.runId(runId)
				.folder(imapFolder)
				.lastUid(56l)
				.start(DateTime.parse("2014-06-02T00:02:02.000Z"))
				.end(DateTime.parse("2014-06-02T00:02:32.000Z"))
				.status(ArchiveStatus.ERROR)
				.build();
		
		processedFolderJdbcImpl.insert(expectedProcessedFolder);
		ProcessedFolder processedFolder = processedFolderJdbcImpl.get(runId, imapFolder).get();
		assertThat(processedFolder).isEqualToComparingFieldByField(expectedProcessedFolder);
	}
	
	@Test
	public void insertShouldThrowWhenDuplicateRunId() throws Exception {
		ProcessedFolder processedFolder = ProcessedFolder.builder()
				.runId(ArchiveTreatmentRunId.from("c3c5cb24-f5df-45ed-8918-99c7555a02c4"))
				.folder(ImapFolder.from("user/usera/Test@mydomain.org"))
				.lastUid(56l)
				.start(DateTime.parse("2014-06-02T00:02:02.000Z"))
				.end(DateTime.parse("2014-06-02T00:02:32.000Z"))
				.status(ArchiveStatus.ERROR)
				.build();
		
		expectedException.expect(DaoException.class);
		
		processedFolderJdbcImpl.insert(processedFolder);
	}
}
