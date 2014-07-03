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
package com.linagora.scheduling;

import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.linagora.scheduling.ScheduledTask.Listener;

public class Scheduler implements AutoCloseable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ImmutableList.Builder<Listener> listeners;
		private TimeUnit unit;
		private Integer resolution;
		private DateTimeProvider timeProvider;

		private Builder() {
			listeners = ImmutableList.builder();
		}

		public Builder resolution(int resolution, TimeUnit unit) {
			Preconditions.checkNotNull(unit);
			Preconditions.checkArgument(resolution > 0);
			this.unit = unit;
			this.resolution = resolution;
			return this;
		}
		
		public Builder resolution(TimeUnit unit) {
			return resolution(1, unit);
		}
		
		public Builder timeProvider(DateTimeProvider timeProvider) {
			Preconditions.checkNotNull(timeProvider);
			this.timeProvider = timeProvider;
			return this;
		}
		
		public Builder addListener(Listener listener) {
			listeners.add(listener);
			return this;
		}
		
		public Scheduler start() {
			Scheduler scheduler = new Scheduler(
				Objects.firstNonNull(timeProvider, DateTimeProvider.SYSTEM_UTC),
				Objects.firstNonNull(resolution, 1),
				Objects.firstNonNull(unit, TimeUnit.MINUTES),
				listeners.build()
				);
			scheduler.start();
			return scheduler;
		}
	}
	
	private final DateTimeProvider dateTimeProvider;
	private final int resolution;
	private final TimeUnit unit;
	private final ImmutableList<Listener> listeners;
	private ActualScheduler actualScheduler;
	
	private Scheduler(DateTimeProvider dateTimeProvider, int resolution, TimeUnit unit, ImmutableList<Listener> listeners) {
		this.dateTimeProvider = dateTimeProvider;
		this.resolution = resolution;
		this.unit = unit;
		this.listeners = listeners;
	}

	public synchronized Scheduler start() {
		if (actualScheduler == null) {
			actualScheduler = new ActualScheduler();
			actualScheduler.startAsync();
		}
		return this;
	}

	public synchronized Scheduler stop() throws Exception {
		if (actualScheduler != null) {
			actualScheduler.shutDown();
			actualScheduler = null;
		}
		return this;
	}
	
	@Override
	public void close() throws Exception {
		stop();
	}
	
	public TaskToSchedule schedule(Task task) {
		Preconditions.checkNotNull(task);
		TaskToSchedule taskToSchedule = new TaskToSchedule(task);
		taskToSchedule.addListeners(listeners);
		return taskToSchedule;
	}
	
	/* package */ ScheduledTask schedule(ScheduledTask scheduledTask) {
		return actualScheduler.submit(scheduledTask);
	}
	
	/* package */ DateTime now() {
		return dateTimeProvider.now();
	}
	

	public boolean cancel(ScheduledTask scheduledTask) {
		return actualScheduler.remove(scheduledTask);
	}
	
	private class ActualScheduler extends AbstractScheduledService {
		
		private final DelayQueue<ScheduledTask> tasks;
		private final ExecutorService workers;
		
		public ActualScheduler() {
			super();
			tasks = new DelayQueue<>();
			workers = Executors.newCachedThreadPool();
		}
		
		@Override
		protected void runOneIteration() throws Exception {
			ScheduledTask task = tasks.poll();
			if (task != null) {
				workers.execute(task.runnable());
			}
		}
		
		public ScheduledTask submit(ScheduledTask scheduledTask) {
			tasks.put(scheduledTask);
			return scheduledTask;
		}

		public boolean remove(ScheduledTask scheduledTask) {
			return tasks.remove(scheduledTask);
		}
		
		@Override
		protected Scheduler scheduler() {
			return Scheduler.newFixedRateSchedule(resolution, resolution, unit);
		}
		
		@Override
		protected void shutDown() throws Exception {
			workers.shutdownNow();
			super.shutDown();
		}
		
	}
	
	public class TaskToSchedule {
		
		private ScheduledTask.Builder taskBuilder;

		public TaskToSchedule(Task task) {
			taskBuilder = ScheduledTask.builder().task(task);
		}
		
		public ScheduledTask at(DateTime when) {
			Preconditions.checkNotNull(when);
			if (dateTimeProvider.now().isAfter(when)) {
				throw new TaskInThePastException();
			}
			return taskBuilder.scheduledTime(when).schedule(Scheduler.this);
		}

		public ScheduledTask in(ReadablePeriod period) {
			Preconditions.checkNotNull(period);
			DateTime when = dateTimeProvider.now().plus(period);
			return taskBuilder.scheduledTime(when).schedule(Scheduler.this);
		}

		public ScheduledTask now() {
			return taskBuilder.scheduledTime(dateTimeProvider.now()).schedule(Scheduler.this);
		}

		public TaskToSchedule addListener(Listener listener) {
			Preconditions.checkNotNull(listener);
			taskBuilder.addListener(listener);
			return this;
		}

		public TaskToSchedule addListeners(List<Listener> listeners) {
			Preconditions.checkNotNull(listeners);
			taskBuilder.addListeners(listeners);
			return this;
		}
	}
	
}
