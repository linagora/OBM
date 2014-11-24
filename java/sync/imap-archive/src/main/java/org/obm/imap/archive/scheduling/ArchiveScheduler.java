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

package org.obm.imap.archive.scheduling;

import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveScheduler implements AutoCloseable {

	private final ArchiveSchedulerQueue queue;
	private final Scheduler<ArchiveDomainTask> scheduler;

	@Inject
	@VisibleForTesting ArchiveScheduler(
			ArchiveSchedulerQueue queue,
			ArchiveSchedulerBus bus,
			ArchiveSchedulerLoggerListener loggerListener,
			DateTimeProvider dateTimeProvider,
			@Named("schedulerResolution") TimeUnit schedulerResolution) {
		
		this.queue = queue;
		this.scheduler = Scheduler.<ArchiveDomainTask>builder()
				.queue(this.queue)
				.timeProvider(dateTimeProvider)
				.resolution(schedulerResolution)
				.addListener(queue.getListener())
				.addListener(bus)
				.addListener(loggerListener)
				.start();
	}
	
	@Override
	public void close() throws Exception {
		scheduler.stop();
	}

	public ScheduledTask<ArchiveDomainTask> schedule(ArchiveDomainTask task) {
		return scheduler.schedule(task).at(task.getArchiveConfiguration().getWhen());
	}

	
	public void clearDomain(ObmDomainUuid domain) {
		for (ScheduledTask<ArchiveDomainTask> task : 
				Iterables.filter(queue.getDomainTasks(domain), onlyScheduledTasksPredicate())) {
			task.cancel();
		}
	}

	private Predicate<ScheduledTask<ArchiveDomainTask>> onlyScheduledTasksPredicate() {
		return new Predicate<ScheduledTask<ArchiveDomainTask>>() {

			@Override
			public boolean apply(ScheduledTask<ArchiveDomainTask> input) {
				return input.state() == State.NEW
					|| input.state() == State.WAITING;
			}
		};
	}
	
}
