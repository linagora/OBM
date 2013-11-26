/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.BatchProcessingListener;
import org.obm.provisioning.processing.BatchProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class ParallelBatchProcessor implements BatchProcessor {

	private final ExecutorService pool;
	private final BatchProcessorImpl delegate;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public ParallelBatchProcessor(@Named("nbParallelBatches") Integer nbParallelBatches, BatchProcessorImpl delegate) {
		this.pool = Executors.newFixedThreadPool(nbParallelBatches);
		this.delegate = delegate;
	}

	@Override
	public void process(final Batch toProcess) throws ProcessingException {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					delegate.process(toProcess);
				}
				catch (Exception e) {
					logger.error(String.format("Error processing batch %s.", toProcess.getId()), e);
				}
			}

		});
	}

	@Override
	public void addBatchProcessingListener(BatchProcessingListener listener) {
		delegate.addBatchProcessingListener(listener);
	}

	@Override
	public void removeBatchProcessingListener(BatchProcessingListener listener) {
		delegate.removeBatchProcessingListener(listener);
	}

}
