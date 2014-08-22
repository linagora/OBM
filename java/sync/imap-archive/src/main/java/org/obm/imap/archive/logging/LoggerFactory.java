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

import org.obm.imap.archive.beans.ArchiveTreatmentRunId;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LoggerFactory {
	
	public final static String CHUNK_APPENDER_PREFIX = "Chunk";
	private final LoggerFileNameService loggerFileNameService;
	
	@Inject
	@VisibleForTesting LoggerFactory(LoggerFileNameService loggerFileNameService) {
		this.loggerFileNameService = loggerFileNameService;
	}
	
	public Logger create(ArchiveTreatmentRunId runId) {
		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(runId.serialize());
		
		logger.addAppender(fileAppender(runId));
		logger.addAppender(chunkedOutputAppender(runId));
		logger.setLevel(Level.INFO);
		
		return logger;
	}

	@VisibleForTesting FileAppender<ILoggingEvent> fileAppender(ArchiveTreatmentRunId runId) {
		LoggerContext loggerContext = getLoggerContext();
		
		FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
		fileAppender.setContext(loggerContext);
		fileAppender.setName(runId.serialize());
		fileAppender.setFile(loggerFileNameService.loggerFileName(runId));
		fileAppender.setEncoder(patternLayoutEncoder(loggerContext));
		
		return fileAppender;
	}
	
	@VisibleForTesting ChunkedOutputAppender chunkedOutputAppender(ArchiveTreatmentRunId runId) {
		ChunkedOutputAppender chunkedOutputAppender = new ChunkedOutputAppender();
		chunkedOutputAppender.setContext(getLoggerContext());
		chunkedOutputAppender.setName(CHUNK_APPENDER_PREFIX + runId.serialize());
		return chunkedOutputAppender;
	}

	private LoggerContext getLoggerContext() {
		return (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
	}

	private PatternLayoutEncoder patternLayoutEncoder(LoggerContext loggerContext) {
		PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
		patternLayoutEncoder.setContext(loggerContext);
		patternLayoutEncoder.setPattern("%msg%n");
		patternLayoutEncoder.start();
		return patternLayoutEncoder;
	}
}
