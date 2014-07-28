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

import java.util.Set;

import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.TaskStatusChanged;

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
	@VisibleForTesting ArchiveSchedulerBus(Set<ArchiveSchedulerBus.Client> clients) {
		this(new EventBus("ArchiveSchedulerBus"), clients);
		
	}
	
	@VisibleForTesting ArchiveSchedulerBus(EventBus bus, Set<? extends ArchiveSchedulerBus.Client> clients) {
		this.bus = bus;
		for (ArchiveSchedulerBus.Client client : clients) {
			bus.register(client);
		}
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
		bus.post(new TaskStatusChanged(task));
	}
	
	public static interface Client {}
	
	public static interface Events {

		static class TaskStatusChanged {
			
			private final ScheduledTask<ArchiveDomainTask> task;

			TaskStatusChanged(ScheduledTask<ArchiveDomainTask> task) {
				this.task = task;
			}

			public ScheduledTask<ArchiveDomainTask> getTask() {
				return task;
			}
			
		}
	}
}