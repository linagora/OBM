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

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.joda.time.DateTime;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.Listener.NoopListener;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class OnlyOnePerDomainScheduler extends NoopListener<ArchiveDomainTask> implements AutoCloseable {

	private final ArchiveDomainTask.Factory archiveTaskFactory;
	private final TreeMultimap<ObmDomain, ArchiveDomainTask> domainEnqueuedTasks;
	@VisibleForTesting final Monitor<ArchiveDomainTask> monitor;
	@VisibleForTesting final Scheduler<ArchiveDomainTask> scheduler;

	@Inject
	private OnlyOnePerDomainScheduler(ArchiveDomainTask.Factory archiveTaskFactory) {
		this(archiveTaskFactory, Monitor.<ArchiveDomainTask>builder(), DateTimeProvider.SYSTEM_UTC, TimeUnit.MINUTES);
	}
	
	@VisibleForTesting OnlyOnePerDomainScheduler(ArchiveDomainTask.Factory archiveTaskFactory,
			Monitor.Builder<ArchiveDomainTask> monitorBuilder,
			DateTimeProvider timeProvider, TimeUnit resolution) {
		this.monitor = monitorBuilder.addListener(this).build();
		this.scheduler = Scheduler.<ArchiveDomainTask>builder().timeProvider(timeProvider).resolution(resolution).addListener(monitor).start();
		this.archiveTaskFactory = archiveTaskFactory;
		this.domainEnqueuedTasks = TreeMultimap.create(ObmDomain.byUuidComparator(), ArchiveDomainTask.comparator());
	}

	public synchronized ArchiveDomainTask scheduleDomainArchiving(ObmDomain domain, DateTime when) {
		ArchiveDomainTask task = archiveTaskFactory.create(domain, when);
		schedule(task);
		return task;
	}

	private void schedule(ArchiveDomainTask toSchedule) {
		if (!hasScheduledTaskForDomain(toSchedule.getDomain())) {
			scheduler.schedule(toSchedule).at(toSchedule.getWhen());
		} else {
			domainEnqueuedTasks.put(toSchedule.getDomain(), toSchedule);
		}
	}

	@Override
	public synchronized void canceled(ScheduledTask<ArchiveDomainTask> scheduledTask) {
		scheduleFromQueue(scheduledTask);
	}

	@Override
	public synchronized void failed(ScheduledTask<ArchiveDomainTask> scheduledTask, Throwable failure) {
		scheduleFromQueue(scheduledTask);
	}

	@Override
	public synchronized void terminated(ScheduledTask<ArchiveDomainTask> scheduledTask) {
		scheduleFromQueue(scheduledTask);
	}

	private void scheduleFromQueue(ScheduledTask<ArchiveDomainTask> scheduledTask) {
		ObmDomain domainToDequeue = scheduledTask.task().getDomain();
		ArchiveDomainTask toSchedule = domainEnqueuedTasks.get(domainToDequeue).pollFirst();
		if (toSchedule != null) {
			schedule(toSchedule);
		}
	}

	private boolean hasScheduledTaskForDomain(final ObmDomain domain) {
		return Iterables.any(monitor.all(), new Predicate<ScheduledTask<ArchiveDomainTask>>() {

			@Override
			public boolean apply(ScheduledTask<ArchiveDomainTask> input) {
				return input.task().getDomain().equals(domain);
			}
		});
	}
	
	@Override
	public void close() throws Exception {
		scheduler.stop();
	}
}
