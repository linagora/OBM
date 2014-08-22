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

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.glassfish.jersey.server.ChunkedOutput;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.logging.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Singleton;

@Singleton
class FileChunker {

	private final static Logger logger = org.slf4j.LoggerFactory.getLogger(FileChunker.class);
	
	public ChunkedOutput<String> chunk(final ArchiveTreatmentRunId runId) {
		final ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String> (String.class);
		// This code is used to work around the https://java.net/jira/browse/JERSEY-2558 issue
		// delay closing the chunk for 10 seconds, in order to let the client received it before.
		long delay = TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				String absolutePath = LoggerFactory.LOG_PATH + runId.serialize() + LoggerFactory.LOG_EXTENSION;
				try {
					File inputFile = new File(absolutePath);
					Files.readLines(inputFile, Charsets.UTF_8, new LineProcessor<ChunkedOutput<String>>() {

						@Override
						public boolean processLine(String line) throws IOException {
							chunkedOutput.write(line);
							chunkedOutput.write(System.lineSeparator());
							return true;
						}

						@Override
						public ChunkedOutput<String> getResult() {
							return chunkedOutput;
						}
					});
				} catch (IOException e) {
					logger.error("Error when reading file: {}", absolutePath);
					Throwables.propagate(e);
				} finally {
					try {
						chunkedOutput.close();
					} catch (IOException e) {
						logger.error("Error when reading file: {}", absolutePath);
						Throwables.propagate(e);
					}
				}
			}
		
		}, delay);
		return chunkedOutput;
	}
}
