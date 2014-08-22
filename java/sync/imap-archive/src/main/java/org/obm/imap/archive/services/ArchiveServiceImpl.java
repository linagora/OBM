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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.exception.DomainConfigurationException;
import org.obm.imap.archive.logging.LoggerFileNameService;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveServiceImpl implements ArchiveService {

	private final static int NUMBER_OF_ITERATIONS = 2;
	
	private final DomainConfigurationDao domainConfigurationDao;
	private final RunningArchiveTracking runningArchiveTracking;
	private final LoggerFileNameService loggerFileNameService;
	private final DateTimeProvider dateTimeProvider;
	private final boolean endlessTask;



	@Inject
	@VisibleForTesting ArchiveServiceImpl(DomainConfigurationDao domainConfigurationDao, 
			RunningArchiveTracking runningArchiveTracking,
			LoggerFileNameService loggerFileNameService,
			DateTimeProvider dateTimeProvider,
			@Named("endlessTask") Boolean endlessTask) {
		
		this.domainConfigurationDao = domainConfigurationDao;
		this.runningArchiveTracking = runningArchiveTracking;
		this.loggerFileNameService = loggerFileNameService;
		this.dateTimeProvider = dateTimeProvider;
		this.endlessTask = endlessTask;
	}
	
	@Override
	@Transactional
	public void archive(final ArchiveDomainTask archiveDomainTask) {
		final Logger logger = archiveDomainTask.getLogger();
		ArchiveTreatmentRunId runId = archiveDomainTask.getRunId();
		ObmDomainUuid domain = archiveDomainTask.getDomain();
		try {
			logger.info("Started {}", runId.serialize());
			
			checkConfiguration(domain);
			Stopwatch stopwatch = Stopwatch.createStarted();
			int numberOfIterations = 0;
			long previousElapsed = 0;
			while (true) {
				long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
				if ((elapsed - previousElapsed) == 1) {
					logger.info(dateTimeProvider.now().toString() + System.lineSeparator());
					numberOfIterations++;
				}
				previousElapsed = elapsed;
				if (!endlessTask && numberOfIterations >= NUMBER_OF_ITERATIONS) {
					break;
				}
			}
			
			logger.info("Ended {}", runId.serialize());
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
	
	@Override
	public Response runningProcessLogs(final ArchiveTreatmentRunId runId) {
		try {
			Optional<ArchiveDomainTask> optional = runningArchiveTracking.get(runId);
			if (optional.isPresent()) {
				return Response.ok(optional.get().getChunkAppender().chunk()).build();
			}
			
			File loggerFile = new File(loggerFileNameService.loggerFileName(runId));
			if (!loggerFile.exists()) {
				return Response.status(Status.NOT_FOUND).build();
			}
			return Response.ok(loggerFile).build();
		} catch (IOException e) {
			Throwables.propagate(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
