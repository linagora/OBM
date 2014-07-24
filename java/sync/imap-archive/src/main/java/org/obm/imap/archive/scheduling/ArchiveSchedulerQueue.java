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

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.DelayQueue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.Listener;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.TaskQueue;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ArchiveSchedulerQueue implements TaskQueue<ArchiveDomainTask> {

	@VisibleForTesting final Monitor<ArchiveDomainTask> monitor;
	@VisibleForTesting final DelayQueueMultimap domainTasks;

	@Inject
	@VisibleForTesting ArchiveSchedulerQueue(OnlyOnePerDomainMonitorFactory monitorFactory) {
		this.monitor = monitorFactory.create();
		this.domainTasks = new DelayQueueMultimap();
	}
	
	@Override
	public void put(ScheduledTask<ArchiveDomainTask> scheduled) {
		domainTasks
			.get(scheduled.task().getDomain())
			.offer(scheduled);
	}

	@Override
	public boolean remove(ScheduledTask<ArchiveDomainTask> scheduled) {
		return domainTasks
			.get(scheduled.task().getDomain())
			.remove(scheduled);
	}

	@Override
	public Collection<ScheduledTask<ArchiveDomainTask>> poll() {
		Builder<ScheduledTask<ArchiveDomainTask>> polledTasks = ImmutableList.builder();
		for (ObmDomainUuid domain : domainTasks.domains()) {
			if (domainHasNoRunningTask(domain)) {
				ScheduledTask<ArchiveDomainTask> task = domainTasks.get(domain).poll();
				if (task != null) {
					polledTasks.add(task);
				}
			}
		}
		return polledTasks.build();
	}
	
	/* package */ Collection<ScheduledTask<ArchiveDomainTask>> getDomainTasks(ObmDomainUuid domain) {
		return ImmutableList.copyOf(domainTasks.get(domain));
	}
	
	/* package */ Listener<ArchiveDomainTask> getListener() {
		return monitor;
	}
	
	private boolean domainHasNoRunningTask(ObmDomainUuid domain) {
		return !Iterables.any(monitor.all(), onlyRunningPredicate(domain));
	}

	private Predicate<ScheduledTask<ArchiveDomainTask>> onlyRunningPredicate(final ObmDomainUuid domain) {
		return new Predicate<ScheduledTask<ArchiveDomainTask>>() {

			@Override
			public boolean apply(ScheduledTask<ArchiveDomainTask> input) {
				return input.task().getDomain().equals(domain)
					&& input.state() == State.RUNNING;
			}
		};
	}
	
	@VisibleForTesting static class DelayQueueMultimap {
		
		private Map<ObmDomainUuid, Queue<ScheduledTask<ArchiveDomainTask>>> map;

		public DelayQueueMultimap() {
			map = new MapMaker().makeMap();
		}

		public Set<ObmDomainUuid> domains() {
			return map.keySet();
		}

		public Queue<ScheduledTask<ArchiveDomainTask>> get(ObmDomainUuid domain) {
			Queue<ScheduledTask<ArchiveDomainTask>> domainQueue = map.get(domain);
			if (domainQueue == null) {
				domainQueue = Queues.synchronizedQueue(new DelayQueue<ScheduledTask<ArchiveDomainTask>>());
				map.put(domain, domainQueue);
			}
			return domainQueue;
		}
	}
}
