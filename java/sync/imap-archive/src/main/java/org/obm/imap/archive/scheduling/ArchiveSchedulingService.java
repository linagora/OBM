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
package org.obm.imap.archive.scheduling;

import org.joda.time.DateTime;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.services.SchedulingDatesService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.UUIDFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveSchedulingService {

	private final ArchiveScheduler scheduler;
	private final UUIDFactory uuidFactory;
	private final ArchiveDomainTask.Factory taskFactory;
	private final SchedulingDatesService schedulingDatesService;
	private final DomainConfigurationDao domainConfigDao;
	
	@Inject
	@VisibleForTesting ArchiveSchedulingService(
			ArchiveScheduler scheduler,
			ArchiveDomainTask.Factory taskFactory,
			UUIDFactory uuidFactory,
			SchedulingDatesService schedulingDatesService,
			DomainConfigurationDao domainConfigDao) {
		this.scheduler = scheduler;
		this.taskFactory = taskFactory;
		this.uuidFactory = uuidFactory;
		this.schedulingDatesService = schedulingDatesService;
		this.domainConfigDao = domainConfigDao;
	}
	
	@Transactional
	public ArchiveTreatmentRunId schedule(ObmDomainUuid domain, DateTime when, ArchiveTreatmentKind archiveTreatmentKind) throws DaoException {
		DomainConfiguration config = domainConfigDao.get(domain);
		return schedule(domain, when, config.getRepeatKind(), archiveTreatmentKind);
	}

	@Transactional
	public ArchiveTreatmentRunId schedule(DomainConfiguration domainConfiguration, ArchiveTreatmentKind archiveTreatmentKind) {
		DateTime when = schedulingDatesService.nextTreatmentDate(domainConfiguration.getSchedulingConfiguration());
		return schedule(domainConfiguration.getDomainId(), when, domainConfiguration.getRepeatKind(), archiveTreatmentKind);
	}

	private ArchiveTreatmentRunId schedule(ObmDomainUuid domain, DateTime when, RepeatKind repeatKink, ArchiveTreatmentKind archiveTreatmentKind) {
		DateTime higherBoundary = schedulingDatesService.higherBoundary(when, repeatKink);
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from(uuidFactory.randomUUID());
		scheduler.schedule(taskFactory.create(domain, when, higherBoundary, runId, archiveTreatmentKind));
		return runId;
	}
}
