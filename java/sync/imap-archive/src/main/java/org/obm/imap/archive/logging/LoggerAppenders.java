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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

import com.google.common.base.Preconditions;

public class LoggerAppenders {

	public static LoggerAppenders from(ArchiveTreatmentRunId runId, Logger logger) {
		String baseAppenderName = runId.serialize();
		
		Appender<ILoggingEvent> fileAppender = logger.getAppender(baseAppenderName);
		Preconditions.checkState(fileAppender != null);
		ChunkedOutputAppender chunkedOutputAppender = (ChunkedOutputAppender) logger.getAppender(LoggerFactory.CHUNK_APPENDER_PREFIX + baseAppenderName);
		Preconditions.checkState(chunkedOutputAppender != null);
		
		return new LoggerAppenders(logger, fileAppender, chunkedOutputAppender);
	}

	private final Logger logger;
	private final Appender<ILoggingEvent> fileAppender;
	private final ChunkedOutputAppender chunkedOutputAppender;

	private LoggerAppenders(Logger logger, Appender<ILoggingEvent> fileAppender, ChunkedOutputAppender chunkedOutputAppender) {
		this.logger = logger;
		this.fileAppender = fileAppender;
		this.chunkedOutputAppender = chunkedOutputAppender;
	}
	
	public void startAppenders() {
		fileAppender.start();
		chunkedOutputAppender.start();
	}
	
	public void stopAppenders() {
		logger.detachAndStopAllAppenders();
	}

	public ChunkedOutputAppender getChunkAppender() {
		return chunkedOutputAppender;
	}
}
