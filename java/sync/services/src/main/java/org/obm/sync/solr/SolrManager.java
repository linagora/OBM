/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.solr;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

import org.obm.configuration.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SolrManager {
	private boolean solrAvailable;
	private Indexer indexer;
	private Timer checker;
	private int solrCheckingInterval;

	private final Timer debugTimer;
	private final LinkedBlockingDeque<SolrRequest> workQueue;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	@VisibleForTesting
	protected SolrManager(ConfigurationService configurationService) {
		debugTimer = new Timer();
		workQueue = new LinkedBlockingDeque<SolrRequest>();
		solrCheckingInterval = configurationService.solrCheckingInterval() * 1000;

		// We are supposedly available at startup for two reasons:
		// 1. This is backwards compatible with the previous implementation
		// 2. We need a SolR server instance in order to check its status, so we need at least one request in the queue
		setSolrAvailable(true);

		if (logger.isInfoEnabled()) {
			scheduleFixedRateDebugLog();
		}
	}

	private void scheduleFixedRateDebugLog() {
		debugTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				logger.info("SolR indexing queue size : " + workQueue.size() + ", SolR available: " + solrAvailable);
			}
		}, 10000, 10000);
	}

	public synchronized boolean isSolrAvailable() {
		return solrAvailable;
	}

	@VisibleForTesting
	protected synchronized void setSolrAvailable(boolean solrAvailable) {
		if (this.solrAvailable == solrAvailable) {
			return;
		}
		
		this.solrAvailable = solrAvailable;

		if (solrAvailable) {
			if (checker != null) {
				checker.cancel();
			}

			(indexer = new Indexer()).start();
		}
		else {
			if (indexer != null) {
				indexer.interrupt();
			}

			(checker = new Timer()).scheduleAtFixedRate(new Checker(), solrCheckingInterval, solrCheckingInterval);
		}
	}

	@VisibleForTesting
	protected void setSolrCheckingInterval(int solrCheckingInterval) {
		this.solrCheckingInterval = solrCheckingInterval;
	}

	public void process(SolrRequest request) {
		workQueue.offer(request);
	}

	private class Indexer extends Thread {
		@Override
		public void run() {
			while (!interrupted()) {
				try {
					SolrRequest request = workQueue.take();

					try {
						request.run();
					}
					catch (Exception e) {
						setSolrAvailable(false);
						workQueue.offerFirst(request);

						logger.warn("SolR server is unavailable, last request is kept in the queue.", e);

						break;
					}
				}
				catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	private class Checker extends TimerTask {
		@Override
		public void run() {
			SolrRequest request = workQueue.peek();

			// We should have at least one element in the queue if a Checker is running
			// because the manager is available at startup and as such, accepts requests
			// So even if SolR is down at startup, the checker will only run when the first request comes in
			if (request != null) {
				try {
					request.getServer().ping();

					logger.info("SolR server is available, resuming indexing.");
					setSolrAvailable(true);
				}
				catch (Exception e) {
				}
			}
		}
	}
}
