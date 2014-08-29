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

import javax.inject.Inject;

import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.module.LoggerModule;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.PersistedResult;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class DomainConfigurationService {

	private final Logger logger;
	private final DomainConfigurationDao domainConfigurationDao;
	private final ArchiveSchedulingService schedulingService;
	private final ArchiveScheduler scheduler;
	
	@Inject
	@VisibleForTesting DomainConfigurationService(
			@Named(LoggerModule.CONFIGURATION) Logger logger,
			DomainConfigurationDao domainConfigurationDao,
			ArchiveSchedulingService schedulingService,
			ArchiveScheduler scheduler) {
		this.logger = logger;
		this.domainConfigurationDao = domainConfigurationDao;
		this.schedulingService = schedulingService;
		this.scheduler = scheduler;
	}
	
	@Transactional
	public PersistedResult updateOrCreate(DomainConfiguration domainConfiguration, ObmDomainUuid domain) throws DaoException, DomainNotFoundException {
		if (domainConfigurationDao.get(domain) == null) {
			return create(domainConfiguration);
		} else {
			return update(domainConfiguration);
		}
	}

	private PersistedResult create(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		domainConfigurationDao.create(domainConfiguration);
		logger.info("A domain configuration has been created: {}", domainConfiguration);
		rescheduleTask(domainConfiguration);
		return PersistedResult.create(domainConfiguration);
	}

	private PersistedResult update(DomainConfiguration domainConfiguration) throws DaoException, DomainNotFoundException {
		domainConfigurationDao.update(domainConfiguration);
		logger.info("A domain configuration has been updated: {}", domainConfiguration);
		rescheduleTask(domainConfiguration);
		return PersistedResult.update();
	}

	private void rescheduleTask(DomainConfiguration domainConfiguration) {
		scheduler.clearDomain(domainConfiguration.getDomainId());
		logger.info("Domain scheduled tasks have been canceled {} ", domainConfiguration.getDomainId());
		if (domainConfiguration.isEnabled()) {
			ArchiveTreatmentRunId runId = schedulingService.schedule(domainConfiguration, ArchiveTreatmentKind.REAL_RUN);
			logger.info("The task {} has been scheduled for domain {} ", runId, domainConfiguration.getDomainId());
		}
	}
}
