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
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Fail;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.linagora.scheduling.Listener.NoopListener;
import com.linagora.scheduling.Scheduler.Builder;
import com.linagora.scheduling.TaskQueue.DelayedQueue;

public class SchedulerTest {

	private Scheduler<Task> testee;
	private DateTime now;
	private TestDateTimeProvider dateTimeProvider;
	private Builder<Task> testeeBuilder;

	public static class TestDateTimeProvider implements DateTimeProvider {
		
		private DateTime current;

		public TestDateTimeProvider(DateTime start) {
			this.current = start;
		}
		
		public void setCurrent(DateTime current) {
			this.current = current;
		}
		
		@Override
		public DateTime now() {
			return current;
		}
	}
	
	@Before
	public void setup() {
		now = DateTime.parse("2014-12-2T11:35");
		dateTimeProvider = new TestDateTimeProvider(now);
		testeeBuilder = Scheduler.builder().resolution(10, TimeUnit.MILLISECONDS).timeProvider(dateTimeProvider);
	}
	
	@After
	public void shutdown() throws Exception {
		if (testee != null) {
			testee.stop();
		}
	}

	private void start() {
		testee = testeeBuilder.start();
	}
	
	@Test
	public void stopShouldBeCallableMoreThanOnce() throws Exception {
		start();
		testee.stop();
		testee.stop();
	}
	
	@Test
	public void startShouldBeCallableMoreThanOnce() {
		start();
		testee.start();
	}
	
	@Test
	public void pollingTheQueueShouldNotFail() {
		DelayedQueue<Task> queue = new TaskQueue.DelayedQueue<Task>();
		testeeBuilder.queue(queue);
		start();
		
		assertThat(queue.poll()).isEmpty();
	}
	
	@Test
	public void schedulerShouldBuildWithoutAnyParam() throws Exception {
		try (Scheduler<Task> scheduler = Scheduler.builder().start()) {
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void schedulerShouldNotBuildWithNegativeResolution() throws Exception {
		try (Scheduler<Task> scheduler = Scheduler.builder().resolution(-1, TimeUnit.SECONDS).start()) {
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void schedulerShouldNotBuildWithZeroResolution() throws Exception {
		try (Scheduler<Task> scheduler = Scheduler.builder().resolution(0, TimeUnit.SECONDS).start()) {
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void schedulerShouldNotBuildWithNullResolution() throws Exception {
		try (Scheduler<Task> scheduler = Scheduler.builder().resolution(1, null).start()) {
		}
	}
	@Test(expected=NullPointerException.class)
	public void schedulerShouldNotBuildWithNullTimeProvider() throws Exception {
		try (Scheduler<Task> scheduler = Scheduler.builder().timeProvider(null).start()) {
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void scheduleShouldThrowOnNullTask() {
		start();
		assertThat(testee.schedule((Task)null));
	}
	
	@Test(expected=NullPointerException.class)
	public void scheduleAtShouldThrowOnNullDateTime() {
		start();
		assertThat(testee.schedule(new DummyTask()).at(null));
	}
	
	@Test
	public void scheduleAtCanScheduleInThePast() throws Exception {
		start();
		DateTime targetTime = now.minusHours(1);
		Task task = new DummyTask();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		ScheduledTask<Task> actual = testee.schedule(task).addListener(testListener).at(targetTime);
		assertThat(actual.scheduledTime()).isEqualTo(targetTime);
		assertThat(actual.task()).isEqualTo(task);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
	}

	@Test
	public void scheduleAtShouldRegisterATaskForNextHour() {
		start();
		DateTime targetTime = now.plusHours(1);
		Task task = new DummyTask();
		ScheduledTask<Task> actual = testee.schedule(task).at(targetTime);
		assertThat(actual.scheduledTime()).isEqualTo(targetTime);
		assertThat(actual.task()).isEqualTo(task);
		assertThat(actual.state()).isEqualTo(ScheduledTask.State.WAITING);
	}
	
	@Test(expected=NullPointerException.class)
	public void scheduleInShouldThrowOnTaskScheduledInThePast() {
		start();
		testee.schedule(new DummyTask()).in(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void scheduleShouldThrowOnNullListener() {
		start();
		testee.schedule(new DummyTask()).addListener(null);
	}
	
	@Test
	public void scheduleInShouldRegisterATaskForNextHour() {
		start();
		Period waitingPeriod = Period.hours(1);
		Task task = new DummyTask();
		ScheduledTask<Task> actual = testee.schedule(task).in(waitingPeriod);
		assertThat(actual.scheduledTime()).isEqualTo(now.plus(waitingPeriod));
		assertThat(actual.task()).isEqualTo(task);
		assertThat(actual.state()).isEqualTo(ScheduledTask.State.WAITING);
	}

	@Test
	public void scheduleInShouldRunATaskInOneHour() throws Exception {
		start();
		Period waitingPeriod = Period.hours(1);
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void scheduleInShouldNotRunACanceldedTaskInOneHour() throws Exception {
		start();
		Period waitingPeriod = Period.hours(1);
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		ScheduledTask<Task> actual = testee.schedule(new DummyTask()).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(actual.cancel()).isTrue();
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.CANCELED);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		try {
			testListener.getNextState(100, TimeUnit.MILLISECONDS);
			Fail.failBecauseExceptionWasNotThrown(TimeoutException.class);
		} catch (TimeoutException e) {
		}
	}

	@Test
	public void cancelShouldNotAbortARunningTask() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		ScheduledTask<Task> actual = testee.schedule(new DummyTask(Duration.millis(100))).addListener(testListener).at(now);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(actual.cancel()).isFalse();
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void cancelShouldNotFailWhenTerminatedTask() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		ScheduledTask<Task> actual = testee.schedule(new DummyTask(Duration.millis(100))).addListener(testListener).at(now);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
		assertThat(actual.cancel()).isFalse();
	}
	
	@Test
	public void scheduleInShouldRunATaskInOneYearAnd2Hours() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		Period waitingPeriod = Period.years(1).plusHours(2);
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).in(waitingPeriod);
		dateTimeProvider.setCurrent(now.plusYears(1));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void scheduleInShouldRunATaskOnceWhenHourJumpBack() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		Period waitingPeriod = Period.hours(2);
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
		dateTimeProvider.setCurrent(now.minusHours(2));
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		try {
			testListener.getNextState(100, TimeUnit.MILLISECONDS);
			Fail.failBecauseExceptionWasNotThrown(TimeoutException.class);
		} catch (TimeoutException e) {
		}
	}
	
	@Test
	public void scheduleAtShouldRunATaskOnceWhenHourJumpBack() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		DateTime targetDate = now.plusHours(2);
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).at(targetDate);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(targetDate);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
		dateTimeProvider.setCurrent(now.minusHours(2));
		dateTimeProvider.setCurrent(targetDate);
		try {
			testListener.getNextState(100, TimeUnit.MILLISECONDS);
			Fail.failBecauseExceptionWasNotThrown(TimeoutException.class);
		} catch (TimeoutException e) {
		}
	}
	
	@Test
	public void scheduleInShouldRunATaskOnceWhenHourJumpForward() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).in(Period.hours(2));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plusHours(3));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void scheduleAtShouldRunATaskOnceWhenHourJumpForward() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee.schedule(new DummyTask(Duration.millis(10))).addListener(testListener).at(now.plusHours(2));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plusHours(3));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void scheduleInShouldRunNothingAfterStop() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		Period waitingPeriod = Period.hours(2);
		testee.schedule(new DummyTask()).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		testee.stop();
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		try {
			testListener.getNextState(100, TimeUnit.MILLISECONDS);
			Fail.failBecauseExceptionWasNotThrown(TimeoutException.class);
		} catch (TimeoutException e) {
		}
	}
	

	@Test
	public void schedulerListenerFailureShouldNotBreakNotification() throws Exception {
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee = testeeBuilder
			.addListener(new NoopListener<Task>() {
				@Override
				public void scheduled(ScheduledTask<Task> task) {
					throw new RuntimeException();
				}
				
				@Override
				public void running(ScheduledTask<Task> task) {
					throw new RuntimeException();
				}
				
				@Override
				public void terminated(ScheduledTask<Task> task) {
					throw new RuntimeException();
				}
			})
			.addListener(testListener)
			.start();
		Period waitingPeriod = Period.hours(1);
		testee.schedule(new DummyTask(Duration.millis(100))).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}
	
	@Test
	public void taskListenerFailureShouldNotBreakNotification() throws Exception {
		start();
		Period waitingPeriod = Period.hours(1);
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee.schedule(new DummyTask(Duration.millis(100))).addListener(new NoopListener<Task>() {
			@Override
			public void scheduled(ScheduledTask<Task> task) {
				throw new RuntimeException();
			}
			
			@Override
			public void running(ScheduledTask<Task> task) {
				throw new RuntimeException();
			}
			
			@Override
			public void terminated(ScheduledTask<Task> task) {
				throw new RuntimeException();
			}
		}).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.TERMINATED);
	}

	@Test
	public void failingTaskShouldNotifyTaskListener() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		Period waitingPeriod = Period.hours(1);
		testee.schedule(new FailingTask(Duration.millis(100))).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.FAILED);
		assertThat(testListener.failure).isExactlyInstanceOf(RuntimeException.class).hasMessage("failing task message");
	}
	
	@Test
	public void failingTaskShouldNotifySchedulerListener() throws Exception {
		Period waitingPeriod = Period.hours(1);
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee = testeeBuilder.addListener(testListener).start();
		testee.schedule(new FailingTask(Duration.millis(100))).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.FAILED);
		assertThat(testListener.failure).isExactlyInstanceOf(RuntimeException.class).hasMessage("failing task message");
	}
	
	@Test
	public void failedTaskListenerShouldNotBreakNotification() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		Period waitingPeriod = Period.hours(1);
		testee.schedule(new FailingTask(Duration.millis(100))).addListener(new NoopListener<Task>() {
			@Override
			public void failed(ScheduledTask<Task> task, Throwable failure) {
				throw new RuntimeException();
			}
		}).addListener(testListener).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.FAILED);
	}
	
	@Test
	public void failedSchedulerListenerShouldNotBreakNotification() throws Exception {
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee = testeeBuilder
				.addListener(new NoopListener<Task>() {
					@Override
					public void failed(ScheduledTask<Task> task, Throwable failure) {
						throw new RuntimeException();
					}
				})
				.addListener(testListener)
				.start();
		Period waitingPeriod = Period.hours(1);
		testee.schedule(new FailingTask(Duration.millis(100))).in(waitingPeriod);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		dateTimeProvider.setCurrent(now.plus(waitingPeriod));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.RUNNING);
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.FAILED);
	}
	
	@Test
	public void canceledTaskListenerShouldNotBreakNotification() throws Exception {
		start();
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		ScheduledTask<Task> scheduled = testee.schedule(new DummyTask(Duration.millis(100))).addListener(new NoopListener<Task>() {
			@Override
			public void canceled(ScheduledTask<Task> task) {
				throw new RuntimeException();
			}
		}).addListener(testListener).in(Period.hours(1));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(scheduled.cancel()).isTrue();
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.CANCELED);
	}
	
	@Test
	public void canceledSchedulerListenerShouldNotBreakNotification() throws Exception {
		FutureTestListener<Task> testListener = new FutureTestListener<>();
		testee = testeeBuilder
				.addListener(new NoopListener<Task>() {
					@Override
					public void canceled(ScheduledTask<Task> task) {
						throw new RuntimeException();
					}
				})
				.addListener(testListener)
				.start();
		ScheduledTask<Task> scheduled = testee.schedule(new DummyTask(Duration.millis(100))).in(Period.hours(1));
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.WAITING);
		assertThat(scheduled.cancel()).isTrue();
		assertThat(testListener.getNextState(1500, TimeUnit.MILLISECONDS)).isEqualTo(ScheduledTask.State.CANCELED);
	}
}

