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

public class Scheduler<T extends Task> implements AutoCloseable {

	public static <T extends Task> Builder<T> builder() {
		return new Builder<T>();
	}
	
	public static class Builder<T extends Task> {
		
		private ImmutableList.Builder<Listener<T>> listeners;
		private TimeUnit unit;
		private Integer resolution;
		private DateTimeProvider timeProvider;

		private Builder() {
			listeners = ImmutableList.builder();
		}

		public Builder<T> resolution(int resolution, TimeUnit unit) {
			Preconditions.checkNotNull(unit);
			Preconditions.checkArgument(resolution > 0);
			this.unit = unit;
			this.resolution = resolution;
			return this;
		}
		
		public Builder<T> resolution(TimeUnit unit) {
			return resolution(1, unit);
		}
		
		public Builder<T> timeProvider(DateTimeProvider timeProvider) {
			Preconditions.checkNotNull(timeProvider);
			this.timeProvider = timeProvider;
			return this;
		}
		
		public Builder<T> addListener(Listener<T> listener) {
			listeners.add(listener);
			return this;
		}
		
		public Scheduler<T> start() {
			Scheduler<T> scheduler = new Scheduler<>(
				Objects.firstNonNull(timeProvider, DateTimeProvider.SYSTEM_UTC),
				Objects.firstNonNull(resolution, 1),
				Objects.firstNonNull(unit, TimeUnit.MINUTES),
				listeners.build());
			scheduler.start();
			return scheduler;
		}
	}
	
	private final DateTimeProvider dateTimeProvider;
	private final int resolution;
	private final TimeUnit unit;
	private final ImmutableList<Listener<T>> listeners;
	private ActualScheduler actualScheduler;
	
	private Scheduler(DateTimeProvider dateTimeProvider, int resolution, TimeUnit unit, ImmutableList<Listener<T>> listeners) {
		this.dateTimeProvider = dateTimeProvider;
		this.resolution = resolution;
		this.unit = unit;
		this.listeners = listeners;
	}

	public synchronized Scheduler<T> start() {
		if (actualScheduler == null) {
			actualScheduler = new ActualScheduler();
			actualScheduler.startAsync();
		}
		return this;
	}

	public synchronized Scheduler<T> stop() throws Exception {
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
	
	public TaskToSchedule schedule(T task) {
		Preconditions.checkNotNull(task);
		TaskToSchedule taskToSchedule = new TaskToSchedule(task);
		taskToSchedule.addListeners(listeners);
		return taskToSchedule;
	}
	
	/* package */ ScheduledTask<T> schedule(ScheduledTask<T> scheduledTask) {
		return actualScheduler.submit(scheduledTask);
	}
	
	/* package */ DateTime now() {
		return dateTimeProvider.now();
	}
	

	public boolean cancel(ScheduledTask<T> scheduledTask) {
		return actualScheduler.remove(scheduledTask);
	}
	
	private class ActualScheduler extends AbstractScheduledService {
		
		private final DelayQueue<ScheduledTask<T>> tasks;
		private final ExecutorService workers;
		
		public ActualScheduler() {
			super();
			tasks = new DelayQueue<>();
			workers = Executors.newCachedThreadPool();
		}
		
		@Override
		protected void runOneIteration() throws Exception {
			ScheduledTask<T> task = tasks.poll();
			if (task != null) {
				workers.execute(task.runnable());
			}
		}
		
		public ScheduledTask<T> submit(ScheduledTask<T> scheduledTask) {
			tasks.put(scheduledTask);
			return scheduledTask;
		}

		public boolean remove(ScheduledTask<T> scheduledTask) {
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
		
		private ScheduledTask.Builder<T> taskBuilder;

		public TaskToSchedule(T task) {
			taskBuilder = ScheduledTask.<T>builder().task(task);
		}
		
		public ScheduledTask<T> at(DateTime when) {
			Preconditions.checkNotNull(when);
			return taskBuilder.scheduledTime(when).schedule(Scheduler.this);
		}

		public ScheduledTask<T> in(ReadablePeriod period) {
			Preconditions.checkNotNull(period);
			DateTime when = dateTimeProvider.now().plus(period);
			return taskBuilder.scheduledTime(when).schedule(Scheduler.this);
		}

		public TaskToSchedule addListener(Listener<T> listener) {
			Preconditions.checkNotNull(listener);
			taskBuilder.addListener(listener);
			return this;
		}

		public TaskToSchedule addListeners(List<Listener<T>> listeners) {
			Preconditions.checkNotNull(listeners);
			taskBuilder.addListeners(listeners);
			return this;
		}
	}
	
}
