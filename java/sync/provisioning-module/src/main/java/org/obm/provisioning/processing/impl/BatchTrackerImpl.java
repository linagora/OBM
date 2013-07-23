/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.provisioning.processing.impl;

import java.util.Map;

import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.Batch.Id;
import org.obm.provisioning.processing.BatchProcessingListener;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BatchTrackerImpl implements BatchProcessingListener, BatchTracker {

	private final Map<Batch.Id, Batch> runningBatches;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public BatchTrackerImpl(BatchProcessor processor) {
		this.runningBatches = Maps.newConcurrentMap();

		processor.addBatchProcessingListener(this);
	}

	@Override
	public void processingStarted(Batch batch) {
		logger.info(String.format("Processing of batch %s (%d operation(s)) has started.", batch.getId(), batch.getOperationsCount()));

		runningBatches.put(batch.getId(), batch);
	}

	@Override
	public void processingProgressed(Batch batch) {
		logger.debug(String.format("Processing of batch %s: %d/%d operation(s) done.", batch.getId(), batch.getOperationsDoneCount(), batch.getOperationsCount()));

		runningBatches.put(batch.getId(), batch);
	}

	@Override
	public void processingComplete(Batch batch, Throwable throwable) {
		if (throwable == null) {
			logger.info(String.format("Processing of batch %s is complete.", batch.getId()));
		} else {
			logger.error(String.format("The processing of batch %s has failed.", batch.getId()), throwable);
		}

		runningBatches.remove(batch.getId());
	}

	@Override
	public Batch getTrackedBatch(Id id) {
		return runningBatches.get(id);
	}
}
