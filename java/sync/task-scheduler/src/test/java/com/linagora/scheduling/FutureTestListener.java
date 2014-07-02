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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Atomics;
import com.google.common.util.concurrent.SettableFuture;
import com.linagora.scheduling.ScheduledTask.Listener;
import com.linagora.scheduling.ScheduledTask.State;

class FutureTestListener extends Listener {

	AtomicReference<SettableFuture<State>> stateRef = Atomics.newReference(SettableFuture.<State>create());
	Throwable failure;

	Future<State> getFutureState() {
		return stateRef.get();
	}
	
	State getNextState(int timeout, TimeUnit unit) throws Exception {
		Stopwatch start = Stopwatch.createStarted();
		try {
			return stateRef.get().get(timeout, unit);
		} finally {
			System.out.println("next state in : " + start.elapsed(TimeUnit.MILLISECONDS));
		}
	}

	void notifyOnce(State state) {
		stateRef.getAndSet(SettableFuture.<State>create()).set(state);
	}

	@Override
	public void canceled() {
		notifyOnce(State.CANCELED);
	}
	
	@Override
	public void running() {
		notifyOnce(State.RUNNING);
	}

	@Override
	public void terminated() {
		notifyOnce(State.TERMINATED);
	}
	
	@Override
	public void failed(Throwable failure) {
		this.failure = failure;
		notifyOnce(State.FAILED);
	}
	
	@Override
	public void scheduled() {
		notifyOnce(State.WAITING);
	}
}