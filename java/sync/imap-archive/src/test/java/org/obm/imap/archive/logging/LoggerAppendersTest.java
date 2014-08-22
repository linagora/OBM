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
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


public class LoggerAppendersTest {

	@Rule public ExpectedException exception = ExpectedException.none();
	
	private IMocksControl control;
	
	private ArchiveTreatmentRunId runId;
	private Logger logger;
	private Appender<ILoggingEvent> fileAppender;
	private ChunkedOutputAppender chunkedOutputAppender;
	
	@Before
	public void setup() {
		runId = ArchiveTreatmentRunId.from("ccb0efbe-7e64-434a-9667-118f17dcad63");
		
		control = createControl();
		logger = new ch.qos.logback.classic.LoggerContext().getLogger("logger");
		
		fileAppender = control.createMock(Appender.class);
		expect(fileAppender.getName())
			.andReturn(runId.serialize()).anyTimes();
		logger.addAppender(fileAppender);
		
		chunkedOutputAppender = control.createMock(ChunkedOutputAppender.class);
		expect(chunkedOutputAppender.getName())
			.andReturn(org.obm.imap.archive.logging.LoggerFactory.CHUNK_APPENDER_PREFIX + runId.serialize()).anyTimes();
		logger.addAppender(chunkedOutputAppender);
	}
	
	@Test
	public void fromShouldThrowExceptionWhenNoFileAppender() {
		Logger logger = new ch.qos.logback.classic.LoggerContext().getLogger("noFileAppender");
		
		exception.expect(IllegalStateException.class);
		LoggerAppenders.from(runId, logger);
	}
	
	@Test
	public void fromShouldThrowExceptionWhenNoChunedOutputAppender() {
		Logger logger = new ch.qos.logback.classic.LoggerContext().getLogger("noChunedOutputAppender");
		logger.addAppender(fileAppender);
		
		exception.expect(IllegalStateException.class);
		LoggerAppenders.from(runId, logger);
	}
	
	@Test
	public void startAppendersShouldStartBothAppenders() {
		fileAppender.start();
		expectLastCall();
		chunkedOutputAppender.start();
		expectLastCall();
		
		control.replay();
		LoggerAppenders loggerAppenders = LoggerAppenders.from(runId, logger);
		loggerAppenders.startAppenders();
		control.verify();
	}
	
	@Test
	public void stopAppendersShouldDetachAndStopAppenders() {
		fileAppender.stop();
		expectLastCall();
		chunkedOutputAppender.stop();
		expectLastCall();
		
		control.replay();
		LoggerAppenders loggerAppenders = LoggerAppenders.from(runId, logger);
		loggerAppenders.stopAppenders();
		control.verify();
	}
	
	@Test
	public void getChunkAppenderShouldReturnChunkAppender() {
		control.replay();
		LoggerAppenders loggerAppenders = LoggerAppenders.from(runId, logger);
		ChunkedOutputAppender chunkAppender = loggerAppenders.getChunkAppender();
		control.verify();
		assertThat(chunkAppender).isEqualTo(chunkedOutputAppender);
	}
}
