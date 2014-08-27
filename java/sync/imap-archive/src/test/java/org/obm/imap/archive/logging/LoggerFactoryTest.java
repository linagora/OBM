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

package org.obm.imap.archive.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;


public class LoggerFactoryTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	private Logger rootLogger;

	@Before
	public void setup() {
		rootLogger = (Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		rootLogger.setLevel(Level.INFO);
	}
	
	@Test
	public void loggerShouldHaveAFileAppender() {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1");
		
		Logger logger = new LoggerFactory(new TemporaryLoggerFileNameService(temporaryFolder)).create(runId);
		Appender<ILoggingEvent> appender = logger.getAppender(runId.serialize());
		assertThat(appender).isNotNull();
		assertThat(appender).isInstanceOf(FileAppender.class);
	}

	@Test
	public void loggerShouldHaveAChunkedOutputAppender() {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1");
		
		Logger logger = new LoggerFactory(new TemporaryLoggerFileNameService(temporaryFolder)).create(runId);
		Appender<ILoggingEvent> appender = logger.getAppender(LoggerFactory.CHUNK_APPENDER_PREFIX + runId.serialize());
		assertThat(appender).isNotNull();
		assertThat(appender).isInstanceOf(ChunkedOutputAppender.class);
	}
	
	@Test
	public void LogLevelShouldBeAtLeastInfo() {
		rootLogger.setLevel(Level.ERROR);
		Logger logger = new LoggerFactory(new TemporaryLoggerFileNameService(temporaryFolder))
			.create(ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1"));
		
		assertThat(logger.getLevel()).isEqualTo(Level.INFO);
	}
	
	@Test
	public void LogLevelShouldBeTheSameAsRootLogger() {
		rootLogger.setLevel(Level.TRACE);
		Logger logger = new LoggerFactory(new TemporaryLoggerFileNameService(temporaryFolder))
			.create(ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1"));
		
		assertThat(logger.getLevel()).isEqualTo(Level.TRACE);
	}
	
	@Test
	public void fileAppenderShouldProvideASuitableFileAppender() {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1");
		TemporaryLoggerFileNameService loggerFileNameService = new TemporaryLoggerFileNameService(temporaryFolder);
		
		FileAppender<ILoggingEvent> fileAppender = new LoggerFactory(loggerFileNameService)
			.fileAppender(runId);
		
		assertThat(fileAppender.getFile()).isEqualTo(loggerFileNameService.loggerFileName(runId));
		assertThat(fileAppender.getName()).isEqualTo(runId.serialize());
	}
	
	@Test
	public void chunkedOutputAppenderShouldProvideASuitableFileAppender() {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("c336b597-fc9a-40f1-bda0-23179a9e67a1");
		TemporaryLoggerFileNameService loggerFileNameService = new TemporaryLoggerFileNameService(temporaryFolder);
		
		ChunkedOutputAppender chunkedOutputAppender = new LoggerFactory(loggerFileNameService)
			.chunkedOutputAppender(runId);
		
		assertThat(chunkedOutputAppender.getName()).isEqualTo("Chunk" + runId.serialize());
	}
}
