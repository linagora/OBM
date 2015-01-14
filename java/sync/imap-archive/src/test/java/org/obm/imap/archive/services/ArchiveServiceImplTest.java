/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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

package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.io.File;

import org.easymock.IMocksControl;
import org.glassfish.jersey.server.ChunkedOutput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.logging.ChunkedOutputAppender;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.logging.LoggerFileNameService;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;

import com.google.common.base.Optional;


public class ArchiveServiceImplTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	private IMocksControl control;
	
	private ScheduledArchivingTracker scheduledArchivingTracker;
	private LoggerFileNameService loggerFileNameService;
	private ArchiveServiceImpl testee;
	
	@Before
	public void setup() {
		control = createControl();
		
		scheduledArchivingTracker = control.createMock(ScheduledArchivingTracker.class);
		loggerFileNameService = control.createMock(LoggerFileNameService.class);
		
		testee = new ArchiveServiceImpl(scheduledArchivingTracker, loggerFileNameService);
	}
	
	@Test
	public void archiveTreatmentLogsShouldGetLogsFromTaskWhenTaskIsPresent() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("3918bb86-a21c-4b5d-b8da-e5ab88c464b8");
		
		ChunkedOutput<String> expectedChunkedOutput = control.createMock(ChunkedOutput.class);
		ChunkedOutputAppender chunkedOutputAppender = control.createMock(ChunkedOutputAppender.class);
		expect(chunkedOutputAppender.chunk())
			.andReturn(expectedChunkedOutput);
		
		LoggerAppenders loggerAppenders = control.createMock(LoggerAppenders.class);
		expect(loggerAppenders.getChunkAppender())
			.andReturn(chunkedOutputAppender);
		
		ArchiveConfiguration archiveConfiguration = control.createMock(ArchiveConfiguration.class);
		expect(archiveConfiguration.getLoggerAppenders())
			.andReturn(loggerAppenders);
		
		ArchiveDomainTask archiveDomainTask = control.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.getArchiveConfiguration())
			.andReturn(archiveConfiguration);
		
		expect(scheduledArchivingTracker.get(runId))
			.andReturn(Optional.of(archiveDomainTask));
		
		control.replay();
		Optional<Object> archiveTreatmentLogs = testee.archiveTreatmentLogs(runId);
		control.verify();
		
		assertThat(archiveTreatmentLogs).isPresent();
		assertThat(archiveTreatmentLogs.get()).isEqualTo(expectedChunkedOutput);
	}

	@Test
	public void archiveTreatmentLogsShouldGetLogsFromFileWhenTaskIsAbsent() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("3918bb86-a21c-4b5d-b8da-e5ab88c464b8");
		
		expect(scheduledArchivingTracker.get(runId))
			.andReturn(Optional.<ArchiveDomainTask> absent());

		File expectedFile = temporaryFolder.newFile();
		expect(loggerFileNameService.loggerFileName(runId))
			.andReturn(expectedFile.getAbsolutePath());
		
		control.replay();
		Optional<Object> archiveTreatmentLogs = testee.archiveTreatmentLogs(runId);
		control.verify();
		
		assertThat(archiveTreatmentLogs).isPresent();
		assertThat(archiveTreatmentLogs.get()).isEqualTo(expectedFile);
	}
}
