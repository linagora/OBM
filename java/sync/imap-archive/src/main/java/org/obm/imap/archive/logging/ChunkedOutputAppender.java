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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

public class ChunkedOutputAppender extends AppenderBase<ILoggingEvent> {

	private final static Logger logger = LoggerFactory.getLogger(ChunkedOutputAppender.class);
	
	private List<ChunkedOutput<String>> chunkedOutputs;
	@VisibleForTesting ConcurrentLinkedDeque<String> messages;

	
	public ChunkedOutputAppender() {
		chunkedOutputs = Lists.newArrayList();
		messages = new ConcurrentLinkedDeque<String>();
	}
	
	@Override
	protected void append(ILoggingEvent event) {
		String formattedMessage = event.getFormattedMessage();
		messages.add(formattedMessage);
		
		for (ChunkedOutput<String> chunkedOutput : chunkedOutputs) {
			try {
				chunkedOutput.write(formattedMessage);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void stop() {
		super.stop();
		for (ChunkedOutput<String> chunkedOutput : chunkedOutputs) {
			try {
				chunkedOutput.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public ChunkedOutput<String> chunk() throws IOException {
		ChunkedOutput<String> chunkedOutput = chunkedOutput();
		chunkedOutputs.add(chunkedOutput);
		for (String message : messages) {
			chunkedOutput.write(message);
		}
		return chunkedOutput;
	}
	
	@VisibleForTesting ChunkedOutput<String> chunkedOutput() {
		return new ChunkedOutput<String>(String.class);
	}
}
