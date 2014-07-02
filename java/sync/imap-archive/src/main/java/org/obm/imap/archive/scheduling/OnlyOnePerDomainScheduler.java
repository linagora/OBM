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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.Listener;
import com.linagora.scheduling.Listener.NoopListener;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class OnlyOnePerDomainScheduler extends NoopListener<ArchiveDomainTask> implements AutoCloseable {

	private final ArchiveDomainTask.Factory archiveTaskFactory;
	@VisibleForTesting final LockingResourcesScheduler lockingResourcesScheduler;

	@Inject
	private OnlyOnePerDomainScheduler(ArchiveDomainTask.Factory archiveTaskFactory, 
			DateTimeProvider dateTimeProvider,
			@Named("schedulerResolution") TimeUnit schedulerResolution) {
		
		this(archiveTaskFactory, Monitor.<ArchiveDomainTask>builder(), dateTimeProvider, schedulerResolution);
	}
	
	@VisibleForTesting OnlyOnePerDomainScheduler(ArchiveDomainTask.Factory archiveTaskFactory,
			Monitor.Builder<ArchiveDomainTask> monitorBuilder,
			DateTimeProvider timeProvider, TimeUnit resolution) {
		Monitor<ArchiveDomainTask> monitor = monitorBuilder.addListener(this).build();
		this.archiveTaskFactory = archiveTaskFactory;
		this.lockingResourcesScheduler = new LockingResourcesScheduler(monitor, timeProvider, resolution);
	}

	public ArchiveDomainTask scheduleDomainArchiving(ObmDomain domain, DateTime when, ArchiveTreatmentRunId runId) {
		ArchiveDomainTask task = archiveTaskFactory.create(domain, when, runId);
		lockingResourcesScheduler.schedule(task);
		return task;
	}

	public ArchiveDomainTask scheduleNowDomainArchiving(ObmDomain domain, DateTime now, ArchiveTreatmentRunId runId) {
		ArchiveDomainTask task = archiveTaskFactory.create(domain, now, runId);
		lockingResourcesScheduler.now(task);
		return task;
	}

	@Override
	public void canceled(ScheduledTask<ArchiveDomainTask> scheduledTask) {
		lockingResourcesScheduler.scheduleFromQueue(scheduledTask);
	}

	@Override
	public void failed(ScheduledTask<ArchiveDomainTask> scheduledTask, Throwable failure) {
		lockingResourcesScheduler.scheduleFromQueue(scheduledTask);
	}

	@Override
	public void terminated(ScheduledTask<ArchiveDomainTask> scheduledTask) {
		lockingResourcesScheduler.scheduleFromQueue(scheduledTask);
	}
	
	@Override
	public void close() throws Exception {
		getScheduler().stop();
	}

	@VisibleForTesting Monitor<ArchiveDomainTask> getMonitor() {
		return lockingResourcesScheduler.monitor;
	}
	
	@VisibleForTesting Scheduler<ArchiveDomainTask> getScheduler() {
		return lockingResourcesScheduler.scheduler;
	}
	
	private class LockingResourcesScheduler {

		private final TreeMultimap<ObmDomain, ArchiveDomainTask> domainEnqueuedTasks;
		private final Monitor<ArchiveDomainTask> monitor;
		private final Scheduler<ArchiveDomainTask> scheduler;

		private LockingResourcesScheduler(Monitor<ArchiveDomainTask> monitor,
				DateTimeProvider timeProvider, TimeUnit resolution) {
			this.monitor = monitor;
			this.domainEnqueuedTasks = TreeMultimap.create(ObmDomain.byUuidComparator(), ArchiveDomainTask.comparator());
			this.scheduler = Scheduler.<ArchiveDomainTask>builder().timeProvider(timeProvider).resolution(resolution).addListener(monitor).start();
		}

		private synchronized void scheduleFromQueue(ScheduledTask<ArchiveDomainTask> scheduledTask) {
			ObmDomain domainToDequeue = scheduledTask.task().getDomain();
			ArchiveDomainTask toSchedule = domainEnqueuedTasks.get(domainToDequeue).pollFirst();
			if (toSchedule != null) {
				schedule(toSchedule);
			}
		}

		private synchronized void schedule(ArchiveDomainTask toSchedule) {
			if (!hasScheduledTaskForDomain(toSchedule.getDomain())) {
			
				scheduler.schedule(toSchedule)
					.addListener(new OutputStreamCloserListener(toSchedule.getDeferredFileOutputStream()))
					.at(toSchedule.getWhen());
			} else {
				domainEnqueuedTasks.put(toSchedule.getDomain(), toSchedule);
			}
		}

		private void now(ArchiveDomainTask toSchedule) {
			scheduler.schedule(toSchedule)
				.addListener(new OutputStreamCloserListener(toSchedule.getDeferredFileOutputStream()))
				.now();
		}

		private synchronized boolean hasScheduledTaskForDomain(final ObmDomain domain) {
			return Iterables.any(monitor.all(), new Predicate<ScheduledTask<ArchiveDomainTask>>() {

				@Override
				public boolean apply(ScheduledTask<ArchiveDomainTask> input) {
					return input.task().getDomain().equals(domain);
				}
			});
		}
	}

	private final static class OutputStreamCloserListener extends Listener.NoopListener<ArchiveDomainTask> {
		private final static Logger logger = LoggerFactory.getLogger(OutputStreamCloserListener.class);
		private final DeferredFileOutputStream deferredFileOutputStream;

		private OutputStreamCloserListener(DeferredFileOutputStream deferredFileOutputStream) {
			this.deferredFileOutputStream = deferredFileOutputStream;
		}

		@Override
		public void canceled(ScheduledTask<ArchiveDomainTask> task) {
			closeStream(deferredFileOutputStream);
		}

		@Override
		public void failed(ScheduledTask<ArchiveDomainTask> task, Throwable failure) {
			closeStream(deferredFileOutputStream);
		}

		@Override
		public void terminated(ScheduledTask<ArchiveDomainTask> task) {
			closeStream(deferredFileOutputStream);
		}

		private void closeStream(DeferredFileOutputStream deferredFileOutputStream) {
			if (deferredFileOutputStream != null) {
				try (FileOutputStream fileOutputStream = new FileOutputStream(deferredFileOutputStream.getFile())) {
					deferredFileOutputStream.close();
					deferredFileOutputStream.writeTo(fileOutputStream);
				} catch (IOException e) {
					logger.error("Error closing stream", e);
				}
			}
		}
	}
}
