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
	
	public static final String CHUNK_APPENDER_PREFIX = "Chunk";
	private static final Level DEFAULT_LOG_LEVEL = Level.INFO;
	private final LoggerFileNameService loggerFileNameService;
	
	@Inject
	@VisibleForTesting LoggerFactory(LoggerFileNameService loggerFileNameService) {
		this.loggerFileNameService = loggerFileNameService;
	}
	
	public Logger create(ArchiveTreatmentRunId runId) {
		Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(runId.serialize());
		
		logger.addAppender(fileAppender(runId));
		logger.addAppender(chunkedOutputAppender(runId));
		logger.setLevel(getLevel());
		
		return logger;
	}
	
	private Level getLevel() {
		Level level = ((Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).getLevel();
		if (level.isGreaterOrEqual(DEFAULT_LOG_LEVEL)) {
			return DEFAULT_LOG_LEVEL;
		}
		return level;
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
