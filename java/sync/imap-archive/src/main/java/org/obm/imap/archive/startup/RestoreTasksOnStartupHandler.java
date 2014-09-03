/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.startup;

import jersey.repackaged.com.google.common.base.Throwables;

import org.obm.ElementNotFoundException;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.ImapArchiveModule.LoggerModule;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveDomainTaskFactory;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.server.LifeCycleHandler;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class RestoreTasksOnStartupHandler implements LifeCycleHandler {
	
	private final Logger logger;
	private final ArchiveScheduler scheduler;
	private final ArchiveTreatmentDao archiveTreatmentDao;
	private final ArchiveDomainTaskFactory taskFactory;

	@Inject
	@VisibleForTesting RestoreTasksOnStartupHandler(
			@Named(LoggerModule.TASK) Logger logger,
			ArchiveScheduler scheduler,
			ArchiveTreatmentDao archiveTreatmentDao,
			ArchiveDomainTaskFactory taskFactory) {
		this.logger = logger;
		this.scheduler = scheduler;
		this.archiveTreatmentDao = archiveTreatmentDao;
		this.taskFactory = taskFactory;
	}
	
	@Transactional
	@Override
	public void starting() {
		try {
			restoreScheduledTasks();
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}
	
	private void restoreScheduledTasks() throws DaoException, ElementNotFoundException {
		for (ArchiveTreatment treatment: archiveTreatmentDao.findAllScheduledOrRunning()) {
			switch (treatment.getArchiveStatus()) {
			case SCHEDULED:
				reSchedule(treatment);
				break;
			case RUNNING:
				markAsFailed(treatment);
				break;
			case SUCCESS:
			case ERROR:
				logger.error("The treatment:{} has an unexpected status:{}",
					treatment.getRunId().serialize(), treatment.getArchiveStatus().asSpecificationValue());
			}
		}
	}

	private void reSchedule(ArchiveTreatment treatment) {
		logger.info("Re-schedule task uuid:{} for domain:{} at:{}", 
				treatment.getRunId().serialize(), treatment.getDomainUuid().get(), treatment.getScheduledTime());
		
		scheduler.schedule(taskFactory.createAsRecurrent(
				treatment.getDomainUuid(), 
				treatment.getScheduledTime(), 
				treatment.getHigherBoundary(), 
				treatment.getRunId()));
	}

	private void markAsFailed(ArchiveTreatment treatment) throws DaoException, ElementNotFoundException {
		logger.warn("Mark as failed task uuid:{} for domain:{}", 
				treatment.getRunId().serialize(), treatment.getDomainUuid().get());
		
		archiveTreatmentDao.update(treatment.asError(ArchiveTreatment.FAILED_AT_UNKOWN_DATE));
	}
}
