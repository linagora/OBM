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

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.glassfish.jersey.server.ChunkedOutput;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.exception.DomainConfigurationException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.Task;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveServiceImpl implements ArchiveService {

	private final static Logger logger = LoggerFactory.getLogger(ArchiveServiceImpl.class);
	
	private final static int NUMBER_OF_ITERATIONS = 2;
	
	private final DomainConfigurationDao domainConfigurationDao;
	private final DateTimeProvider dateTimeProvider;
	private final LogFileService logFileService;
	private final boolean endlessTask;
	private final Map<ArchiveTreatmentRunId, LogWriter> runIdToPeriodicTaskMap;

	@Inject
	@VisibleForTesting ArchiveServiceImpl(
			DomainConfigurationDao domainConfigurationDao,
			DateTimeProvider dateTimeProvider,
			LogFileService logFileService,
			@Named("endlessTask") Boolean endlessTask) {
		
		this.domainConfigurationDao = domainConfigurationDao;
		this.dateTimeProvider = dateTimeProvider;
		this.logFileService = logFileService;
		this.endlessTask = endlessTask;
		
		this.runIdToPeriodicTaskMap = Maps.newHashMap();
	}
	
	@Override
	@Transactional
	public void archive(ObmDomainUuid domain, ArchiveTreatmentRunId runId, DeferredFileOutputStream deferredFileOutputStream) {
		try {
			checkConfiguration(domain);
			
			LogWriter logWriter = new LogWriter(runId, 
					new ChunkedOutput<String>(String.class), 
					deferredFileOutputStream,
					endlessTask);
			runIdToPeriodicTaskMap.put(runId, logWriter);
			logWriter.run();
		} catch (DaoException e) {
			logger.error("Error on archive treatment", e);
			Throwables.propagate(e);
		}
	}

	private DomainConfiguration checkConfiguration(ObmDomainUuid domain) throws DaoException {
		DomainConfiguration domainConfiguration = domainConfigurationDao.get(domain);
		if (domainConfiguration == null) {
			throw new DomainConfigurationException("The IMAP Archive configuration is not defined for the domain: '" + domain.get() + "'");
		}
		if (!domainConfiguration.isEnabled()) {
			throw new DomainConfigurationException("The IMAP Archive service is disable for the domain: '" + domain.get() + "'");
		}
		return domainConfiguration;
	}
	
	public final class LogWriter implements Task {
		private final ArchiveTreatmentRunId runId;
		private final ChunkedOutput<String> chunkedOutput;
		private final DeferredFileOutputStream deferredFileOutputStream;
		private int numberOfIterations;
		private boolean isEndlessTask;

		private LogWriter(ArchiveTreatmentRunId runId, ChunkedOutput<String> chunkedOutput, DeferredFileOutputStream deferredFileOutputStream, boolean isEndlessTask) {
			this.runId = runId;
			this.chunkedOutput = chunkedOutput;
			this.deferredFileOutputStream = deferredFileOutputStream;
			this.isEndlessTask = isEndlessTask;
			this.numberOfIterations = 0;
		}

		@Override
		public void run() {
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					try {
						if (!isEndlessTask && numberOfIterations++ >= NUMBER_OF_ITERATIONS) {
							cancel();
							timer.cancel();
							timer.purge();
							chunkedOutput.close();
							return;
						}

						String output = dateTimeProvider.now().toString() + System.lineSeparator();
						
						deferredFileOutputStream.write(output.getBytes());
						deferredFileOutputStream.flush();
						chunkedOutput.write(output);
					} catch (IOException e) {
						logger.error("Error on timer task", e);
						Throwables.propagate(e);
					}
				}
			}, 0, 1000);
			
		}

		@Override
		public String taskName() {
			return runId.toString();
		}
	}
	
	@Override
	public ChunkedOutput<String> runningProcessLogs(final ArchiveTreatmentRunId runId) {
		LogWriter logWriter = runIdToPeriodicTaskMap.get(runId);
		if (logWriter == null) {
			return chunkLogFile(runId);
		}
		ChunkedOutput<String> chunkedOutput = logWriter.chunkedOutput;
		if (chunkedOutput.isClosed()) {
			return chunkLogFile(runId);
		}
		return chunkedOutput;
	}

	private ChunkedOutput<String> chunkLogFile(ArchiveTreatmentRunId runId) {
		try {
			return logFileService.chunkLogFile(runId);
		} catch (NoSuchFileException e) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}
}
