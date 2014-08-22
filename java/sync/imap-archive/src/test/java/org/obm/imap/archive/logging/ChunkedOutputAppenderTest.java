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
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.glassfish.jersey.server.ChunkedOutput;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.spi.LoggingEvent;


public class ChunkedOutputAppenderTest {

	private IMocksControl control;
	
	@Before
	public void setup() {
		control = createControl();
	}
	
	@Test
	public void appendShouldAddInMessages() {
		String message = "message";
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(message);
		
		String message2 = "message2";
		LoggingEvent loggingEvent2 = new LoggingEvent();
		loggingEvent2.setMessage(message2);
		
		ChunkedOutputAppender chunkedOutputAppender = new ChunkedOutputAppender();
		chunkedOutputAppender.append(loggingEvent);
		chunkedOutputAppender.append(loggingEvent2);
		
		assertThat(chunkedOutputAppender.messages).containsOnly(message, message2);
	}
	
	@Test
	public void stopShouldStopAllChunkedOutputs() throws Exception {
		String message = "message";
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(message);
		
		ChunkedOutputAppender chunkedOutputAppender = new ChunkedOutputAppender();
		ChunkedOutput<String> chunkedOutput = chunkedOutputAppender.chunk();
		ChunkedOutput<String> chunkedOutput2 = chunkedOutputAppender.chunk();
		
		chunkedOutputAppender.append(loggingEvent);
		chunkedOutputAppender.stop();
		
		assertThat(chunkedOutputAppender.isStarted()).isFalse();
		assertThat(chunkedOutput.isClosed()).isTrue();
		assertThat(chunkedOutput2.isClosed()).isTrue();
	}
	
	@Test 
	public void chunkShouldCopyAlreadyAppendedMessages() throws Exception {
		String message = "message";
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(message);
		
		String message2 = "message2";
		LoggingEvent loggingEvent2 = new LoggingEvent();
		loggingEvent2.setMessage(message2);
		
		ChunkedOutput<String> chunkedOutput = control.createMock(ChunkedOutput.class);
		chunkedOutput.write(message);
		expectLastCall();
		chunkedOutput.write(message2);
		expectLastCall();
		
		MockedChunkedOutputAppender chunkedOutputAppender = new MockedChunkedOutputAppender(chunkedOutput);
		chunkedOutputAppender.append(loggingEvent);
		chunkedOutputAppender.append(loggingEvent2);
		
		control.replay();
		chunkedOutputAppender.chunk();
		control.verify();
	}
	
	@Test 
	public void appendShouldWriteMessagesInChunedOutput() throws Exception {
		String message = "message";
		LoggingEvent loggingEvent = new LoggingEvent();
		loggingEvent.setMessage(message);
		
		String message2 = "message2";
		LoggingEvent loggingEvent2 = new LoggingEvent();
		loggingEvent2.setMessage(message2);
		
		ChunkedOutput<String> chunkedOutput = control.createMock(ChunkedOutput.class);
		chunkedOutput.write(message);
		expectLastCall();
		chunkedOutput.write(message2);
		expectLastCall();
		
		MockedChunkedOutputAppender chunkedOutputAppender = new MockedChunkedOutputAppender(chunkedOutput);
		chunkedOutputAppender.chunk();
		
		control.replay();
		chunkedOutputAppender.append(loggingEvent);
		chunkedOutputAppender.append(loggingEvent2);
		control.verify();
	}
	
	private static class MockedChunkedOutputAppender extends ChunkedOutputAppender {

		private ChunkedOutput<String> chunkedOutput;

		public MockedChunkedOutputAppender(ChunkedOutput<String> chunkedOutput) {
			this.chunkedOutput = chunkedOutput;
		}

		@Override
		ChunkedOutput<String> chunkedOutput() {
			return chunkedOutput;
		}
		
	}
}
