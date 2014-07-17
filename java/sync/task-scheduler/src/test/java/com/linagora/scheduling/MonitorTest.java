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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.guava.api.Assertions;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.linagora.scheduling.ScheduledTask.State;


public class MonitorTest {

	DateTimeProvider timeProvider;
	FutureTestListener<Task> monitorListener;
	Monitor<Task> monitor;
	Scheduler<Task> scheduler;

	@Before
	public void setup() {
		monitorListener = new FutureTestListener<>();
		monitor = Monitor.builder().addListener(monitorListener).build();
		timeProvider = DateTimeProvider.SYSTEM_UTC;
		scheduler = Scheduler.builder()
				.resolution(TimeUnit.MILLISECONDS)
				.timeProvider(timeProvider)
				.addListener(monitor).start();
	}
	
	@After
	public void shutdown() throws Exception {
		scheduler.stop();
	}
	
	@Test
	public void allShouldReturnEmptyListAtStart() {
		assertThat(monitor.all()).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void allShouldReturnScheduledTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		RemotelyControlledTask remotlyControlledTask = new RemotelyControlledTask();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(remotlyControlledTask).addListener(futureListener).at(timeProvider.now());
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitor.all()).containsExactly(scheduledTask);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(monitor.all()).containsExactly(scheduledTask);
		remotlyControlledTask.terminate();
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).isEmpty();
	}
	
	@Test
	public void allShouldNotReturnCanceledTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(new DummyTask()).addListener(futureListener).in(Period.days(1));
		assertThat(scheduledTask.cancel()).isTrue();
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.CANCELED);
		assertThat(monitor.all()).isEmpty();
	}
	
	@Test
	public void allShouldNotReturnFailingTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		scheduler.schedule(new FailingTask(Duration.millis(1))).addListener(futureListener).at(timeProvider.now());
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.FAILED);
		assertThat(monitor.all()).isEmpty();
	}
	
	@Test
	public void findByIdShouldFindAScheduledTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(new DummyTask()).addListener(futureListener).in(Period.days(1));
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitor.findById(scheduledTask.id()).get()).isEqualTo(scheduledTask);
	}
	
	@Test
	public void findByIdShouldFindARunningTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		RemotelyControlledTask remotlyControlledTask = new RemotelyControlledTask();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(remotlyControlledTask).addListener(futureListener).at(timeProvider.now());
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		Assertions.assertThat(monitor.findById(scheduledTask.id())).isPresent().contains(scheduledTask);
	}
	
	@Test
	public void findByIdShouldNotFindAFinishedTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		RemotelyControlledTask remotlyControlledTask = new RemotelyControlledTask();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(remotlyControlledTask).addListener(futureListener).at(timeProvider.now());
		remotlyControlledTask.terminate();
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.TERMINATED);
		Assertions.assertThat(monitor.findById(scheduledTask.id())).isAbsent();
	}
	
	@Test
	public void findByIdShouldNotFindACanceledTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(new DummyTask()).addListener(futureListener).in(Period.days(1));
		assertThat(scheduledTask.cancel()).isTrue();
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.CANCELED);
		Assertions.assertThat(monitor.findById(scheduledTask.id())).isAbsent();
	}
	
	@Test
	public void findByIdShouldNotFindAFailingTasks() throws Exception {
		FutureTestListener<Task> futureListener = new FutureTestListener<>();
		ScheduledTask<Task> scheduledTask = scheduler.schedule(new FailingTask(Duration.millis(1))).addListener(futureListener).at(timeProvider.now());
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(futureListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.FAILED);
		Assertions.assertThat(monitor.findById(scheduledTask.id())).isAbsent();
	}
	
	@Test
	public void scheduledShouldNotifyListenerAsScheduled() throws Exception {
		monitor.scheduled(ScheduledTask.builder().schedule(scheduler));
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
	}
	
	@Test
	public void runningShouldNotifyListenerAsRunning() throws Exception {
		monitor.running(ScheduledTask.builder().schedule(scheduler));
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
	}
	
	@Test
	public void terminatedShouldNotifyListenerAsTerminated() throws Exception {
		monitor.terminated(ScheduledTask.builder().schedule(scheduler));
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.TERMINATED);
	}
	
	@Test
	public void canceledShouldNotifyListenerAsCanceled() throws Exception {
		monitor.canceled(ScheduledTask.builder().schedule(scheduler));
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.CANCELED);
	}
	
	@Test
	public void failedShouldNotifyListenerAsFailed() throws Exception {
		Throwable expectedException = new RuntimeException("expected");
		monitor.failed(ScheduledTask.builder().schedule(scheduler), expectedException);
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.FAILED);
		assertThat(monitorListener.failure).isEqualTo(expectedException);
	}
	
	@Test
	public void schedulerNotificationsShouldBePropagated() throws Exception {
		RemotelyControlledTask remotlyControlledTask = new RemotelyControlledTask();
		scheduler.schedule(remotlyControlledTask).at(timeProvider.now());
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		remotlyControlledTask.terminate();
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.TERMINATED);
	}
	
	@Test
	public void schedulerFailedNotificationShouldBePropagated() throws Exception {
		scheduler.schedule(new FailingTask(Duration.millis(1))).at(timeProvider.now());
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(monitorListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(State.FAILED);
	}
}
