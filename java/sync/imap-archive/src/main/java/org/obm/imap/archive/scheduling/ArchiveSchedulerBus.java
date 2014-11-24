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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.Listener;
import com.linagora.scheduling.ScheduledTask;

@Singleton
public class ArchiveSchedulerBus implements Listener<ArchiveDomainTask> {

	private final EventBus bus;

	@Inject
	@VisibleForTesting ArchiveSchedulerBus() {
		this(new EventBus("ArchiveSchedulerBus"));
	}
	
	@VisibleForTesting ArchiveSchedulerBus(EventBus bus) {
		this.bus = bus;
	}
	
	@Override
	public void canceled(ScheduledTask<ArchiveDomainTask> task) {
		postTaskStatusChange(task);
	}

	@Override
	public void failed(ScheduledTask<ArchiveDomainTask> task, Throwable failure) {
		postTaskStatusChange(task);
	}

	@Override
	public void running(ScheduledTask<ArchiveDomainTask> task) {
		postTaskStatusChange(task);
	}

	@Override
	public void scheduled(ScheduledTask<ArchiveDomainTask> task) {
		postTaskStatusChange(task);
	}

	@Override
	public void terminated(ScheduledTask<ArchiveDomainTask> task) {
		postTaskStatusChange(task);
	}

	private void postTaskStatusChange(ScheduledTask<ArchiveDomainTask> task) {
		bus.post(task.task().createStatusChangeEvent(task.state()));
	}
	
	/* package */ void register(ArchiveSchedulerBus.Client client) {
		bus.register(client);
	}
	
	public interface Client {}
	
	public interface Events {

		public abstract class TaskStatusChanged {

			interface Factory {
				TaskStatusChanged create(ScheduledTask.State state, ArchiveDomainTask archiveDomainTask);
			}
			
			private final ArchiveDomainTask task;
			private final ScheduledTask.State state;

			protected TaskStatusChanged(ScheduledTask.State state, ArchiveDomainTask task) {
				this.state = state;
				this.task = task;
			}

			public ArchiveDomainTask task() {
				return task;
			}
			
			public ScheduledTask.State state() {
				return state;
			}
		}
		
		public class DryRunTaskStatusChanged extends TaskStatusChanged {
			
			public static class Factory implements TaskStatusChanged.Factory {
				@Override
				public TaskStatusChanged create(ScheduledTask.State state, ArchiveDomainTask archiveDomainTask) {
					return new DryRunTaskStatusChanged(state, archiveDomainTask);
				}
			}
			
			public DryRunTaskStatusChanged(ScheduledTask.State state, ArchiveDomainTask task) {
				super(state, task);
			}
		}
		
		public class RealRunTaskStatusChanged extends TaskStatusChanged {
			
			public static class Factory implements TaskStatusChanged.Factory {
				@Override
				public TaskStatusChanged create(ScheduledTask.State state, ArchiveDomainTask archiveDomainTask) {
					return new RealRunTaskStatusChanged(state, archiveDomainTask);
				}
			}
			
			public RealRunTaskStatusChanged(ScheduledTask.State state, ArchiveDomainTask task) {
				super(state, task);
			}
		}
	}
}