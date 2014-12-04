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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class ChunkedOutputAppender extends AppenderBase<ILoggingEvent> {

	private final static Logger logger = LoggerFactory.getLogger(ChunkedOutputAppender.class);
	
	private List<ChunkedOutput<String>> chunkedOutputs;
	@VisibleForTesting ConcurrentLinkedDeque<String> messages;

	private final Set<ClassicConverter> converters;

	public ChunkedOutputAppender() {
		chunkedOutputs = Lists.newArrayList();
		messages = new ConcurrentLinkedDeque<String>();
		converters = ImmutableSet.of(messageConverter(), lineSeperatorConverter(), rootCauseFirstThrowableProxyConverter());
	}

	private MessageConverter messageConverter() {
		MessageConverter messageConverter = new MessageConverter();
		messageConverter.start();
		return messageConverter;
	}

	private LineSeperatorConverter lineSeperatorConverter() {
		LineSeperatorConverter lineSeperatorConverter = new LineSeperatorConverter();
		lineSeperatorConverter.start();
		return lineSeperatorConverter;
	}

	private RootCauseFirstThrowableProxyConverter rootCauseFirstThrowableProxyConverter() {
		RootCauseFirstThrowableProxyConverter rootCauseFirstThrowableProxyConverter = new RootCauseFirstThrowableProxyConverter();
		rootCauseFirstThrowableProxyConverter.start();
		return rootCauseFirstThrowableProxyConverter;
	}
	
	private static class LineSeperatorConverter extends ClassicConverter {
		
		  public String convert(ILoggingEvent event) {
			  return CoreConstants.LINE_SEPARATOR;
		  }
	}
	
	@Override
	protected void append(final ILoggingEvent event) {
		final StringBuilder buffer = new StringBuilder();
		for (ClassicConverter converter : converters) {
			converter.write(buffer, event);
		}
		String formattedMessage = buffer.toString();
		messages.add(formattedMessage);
		
		for (ChunkedOutput<String> chunkedOutput : chunkedOutputs) {
			try {
				writeToChunk(chunkedOutput, formattedMessage);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void stop() {
		try {
			super.stop();
			for (ChunkedOutput<String> chunkedOutput : chunkedOutputs) {
				try {
					chunkedOutput.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} finally {
			stopConverters();
		}
	}

	private void stopConverters() {
		for (ClassicConverter converter : converters) {
			try {
				converter.stop();
			} catch (Exception e) {
				logger.error("Error while stoping converter", e);
			}
		}
	}

	public ChunkedOutput<String> chunk() throws IOException {
		ChunkedOutput<String> chunkedOutput = chunkedOutput();
		chunkedOutputs.add(chunkedOutput);
		for (String message : messages) {
			writeToChunk(chunkedOutput, message);
		}
		return chunkedOutput;
	}
	
	@VisibleForTesting ChunkedOutput<String> chunkedOutput() {
		return new ChunkedOutput<String>(String.class);
	}
	
	private void writeToChunk(ChunkedOutput<String> chunkedOutput, String message) throws IOException {
		chunkedOutput.write(message);
		if (!message.endsWith(System.lineSeparator())) {
			chunkedOutput.write(System.lineSeparator());
		}
	}
}
