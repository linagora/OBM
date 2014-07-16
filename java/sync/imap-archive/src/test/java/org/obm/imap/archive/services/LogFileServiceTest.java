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

package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.server.ChunkedOutput;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;

import pl.wkr.fluentrule.api.FluentExpectedException;


public class LogFileServiceTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	@Test
	public void chunkLogFileShouldThrowWhenNoLogFile() throws Exception {
		expectedException.expect(NoSuchFileException.class);
		
		new LogFileService(TimeUnit.MILLISECONDS).chunkLogFile(ArchiveTreatmentRunId.from("2abf5fbb-c187-466f-9932-5cf7796566eb"));
	}
	
	@Test
	public void chunkLogFileShouldCopyDataToChunkedOutputWhenDone() throws Exception {
		File logFile = temporaryFolder.newFile();
		
		String expectedData = "expected String";
		FileUtils.write(logFile, expectedData);
		
		ChunkedOutput<String> chunkedOutput = new MyLogFileService(logFile)
			.chunkLogFile(ArchiveTreatmentRunId.from("2abf5fbb-c187-466f-9932-5cf7796566eb"));
		
		long oneSecond = 1000;
		Thread.sleep(oneSecond);
		assertThat(chunkedOutput).isNotNull();
		assertThat(chunkedOutput.isClosed()).isTrue();
	}
	
	private static class MyLogFileService extends LogFileService {

		private final File logFile;

		public MyLogFileService(File logFile) {
			super(TimeUnit.MILLISECONDS);
			this.logFile = logFile;
		}
		
		@Override
		public File getFile(ArchiveTreatmentRunId runId) {
			return logFile;
		}
		
	}
}
