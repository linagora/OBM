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
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ChunkedOutput;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class LogFileService {

	private final static Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);

	private final static String LOG_PATH = "/var/log/obm-imap-archive";
	private final static String LOG_EXTENSION = ".log";

	private final TimeUnit schedulerResolution;

	@Inject
	protected LogFileService(@Named("schedulerResolution") TimeUnit schedulerResolution) {
		this.schedulerResolution = schedulerResolution;
	}
	
	public File getFile(ArchiveTreatmentRunId runId) {
		return new File(Paths.get(LOG_PATH, runId.serialize() + LOG_EXTENSION).toUri());	
	}
	
	public ChunkedOutput<String> chunkLogFile(ArchiveTreatmentRunId runId) throws NoSuchFileException {
		File file = getFile(runId);
		if (!file.exists()) {
			throw new NoSuchFileException(file.getAbsolutePath());
		}
		
		ChunkedOutput<String> chunkedOutput = new ChunkedOutput<String>(String.class);
		copyFileToChunk(chunkedOutput, file);
		return chunkedOutput;
	}

	private void copyFileToChunk(final ChunkedOutput<String> chunkedOutput, final File logFile) {
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				try {
					Files.readLines(logFile, Charsets.UTF_8, new LineProcessor<ChunkedOutput<String>>() {

						@Override
						public boolean processLine(String line) throws IOException {
							chunkedOutput.write(line);
							return true;
						}

						@Override
						public ChunkedOutput<String> getResult() {
							return chunkedOutput;
						}
					}).close();
					
					cancel();
				} catch (IOException e) {
					logger.error("Error when retrieving log file", e);
					throw new WebApplicationException(Status.NO_CONTENT);
				}
			}
		}, schedulerResolution.toMillis(1));
	}
}
