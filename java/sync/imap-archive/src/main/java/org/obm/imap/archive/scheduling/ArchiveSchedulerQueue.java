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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.DelayQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSchedulerQueue.class);
	
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
			.get(scheduled.task().getArchiveConfiguration().getDomainId())
			.offer(scheduled);
	}

	@Override
	public boolean remove(ScheduledTask<ArchiveDomainTask> scheduled) {
		return domainTasks
			.get(scheduled.task().getArchiveConfiguration().getDomainId())
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

	@Override
	public void clear() {
		domainTasks.clear();
	}

	@Override
	public boolean hasAnyTask() {
		return domainTasks.hasAnyTask();
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
				return input.task().getArchiveConfiguration().getDomainId().equals(domain)
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

		public void clear() {
			for (Entry<ObmDomainUuid, Queue<ScheduledTask<ArchiveDomainTask>>> entry : map.entrySet()) {
				try {
					entry.getValue().clear();
				} catch (Exception e) {
					LOGGER.error("Error when emptying the queue", e);
				}
			}
		}
		
		public boolean hasAnyTask() {
			for (Queue<ScheduledTask<ArchiveDomainTask>> queue : map.values()) {
				if (!queue.isEmpty()) {
					return true;
				}
			}
			return false;
		}
	}
}
