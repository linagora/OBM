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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Queues;
import com.linagora.scheduling.Listener;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.Task;

class FutureTestListener<T extends Task> implements Listener<T> {

	ArrayBlockingQueue<State> states = Queues.newArrayBlockingQueue(10);
	Throwable failure;

	State getNextState(int timeout, TimeUnit unit) throws Exception {
		Stopwatch start = Stopwatch.createStarted();
		try {
			State state = states.poll(timeout, unit);
			if (state == null) {
				throw new TimeoutException();
			}
			return state;
		} finally {
			System.out.println("next state in : " + start.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	void notifyOnce(State state) {
		try {
			states.put(state);
		} catch (InterruptedException e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public void canceled(ScheduledTask<T> task) {
		notifyOnce(State.CANCELED);
	}
	
	@Override
	public void running(ScheduledTask<T> task) {
		notifyOnce(State.RUNNING);
	}

	@Override
	public void terminated(ScheduledTask<T> task) {
		notifyOnce(State.TERMINATED);
	}
	
	@Override
	public void failed(ScheduledTask<T> task, Throwable failure) {
		this.failure = failure;
		notifyOnce(State.FAILED);
	}
	
	@Override
	public void scheduled(ScheduledTask<T> task) {
		notifyOnce(State.WAITING);
	}
}