/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.opush;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

@Singleton
public class PendingQueriesLock {

	private final AtomicInteger nbClient;
	private final Semaphore lock;
	private CountDownLatch countDownLatch;
	private int nbLock;
	
	public PendingQueriesLock() {
		nbLock = 0;
		nbClient = new AtomicInteger(0);
		lock = new Semaphore(1);
		countDownLatch = new CountDownLatch(1);
	}
	
	public synchronized void countDown() {
		nbLock -= 1;
		Preconditions.checkState(nbLock >= 0);
		if (nbLock == 0) {
			lock.release();
		}
	}
	
	public void expectedQueriesBeforeUnlock(int count) {
		countDownLatch = new CountDownLatch(count);
	}
	
	public boolean waitingStart(long timeout, TimeUnit unit) throws InterruptedException {
		return countDownLatch.await(timeout, unit);
	}
	
	public boolean waitingClose(long timeout, TimeUnit unit) throws InterruptedException {
		return lock.tryAcquire(timeout, unit);
	}

	public void incrementLockCount() {
		nbClient.incrementAndGet();
	}

	public synchronized void start() {
		countDownLatch.countDown();
		nbLock += nbClient.get();
		if (nbLock > 0) {
			lock.tryAcquire();
		}
	}
}
