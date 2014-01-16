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

import static fr.aliacom.obm.common.system.ObmSystemUser.OBM_SATELLITE_REQUEST;

import java.util.Set;

import javax.swing.event.EventListenerList;

import org.obm.annotations.transactional.Transactional;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.conf.SystemUserSatelliteConfiguration;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.BatchProcessingListener;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.OperationProcessor;
import org.obm.satellite.client.Configuration;
import org.obm.satellite.client.SatelliteService;
import org.obm.sync.date.DateProvider;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class BatchProcessorImpl implements BatchProcessor {

	private final DateProvider dateProvider;
	private final SatelliteService satelliteService;
	private final Set<OperationProcessor> operationProcessors;
	private final BatchDao batchDao;
	private final UserSystemDao userSystemDao;
	private final EventListenerList listenerList;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public BatchProcessorImpl(SatelliteService satelliteService, Set<OperationProcessor> operationProcessors,
			DateProvider dateProvider, BatchDao batchDao, UserSystemDao userSystemDao) {
		this.satelliteService = satelliteService;
		this.operationProcessors = operationProcessors;
		this.dateProvider = dateProvider;
		this.batchDao = batchDao;
		this.userSystemDao = userSystemDao;

		this.listenerList = new EventListenerList();
	}

	@Override
	public void process(Batch batch) throws ProcessingException {
		Batch.Builder batchBuilder = Batch
				.builder()
				.from(batch)
				.status(BatchStatus.RUNNING)
				.timecommit(dateProvider.getDate());

		fireProcessingStarted(batchBuilder.build());

		try {
			for (Operation operation : batch.getOperations()) {
				batchBuilder.operation(processOperation(operation, batch));

				fireProcessingProgressed(batchBuilder.build());
			}

			Batch newBatch = batchBuilder
					.status(BatchStatus.SUCCESS)
					.build();

			postProcess(newBatch);
			persist(newBatch);

			fireProcessingComplete(newBatch, null);
		}
		catch (Exception e) {
			Batch newBatch = batchBuilder
					.status(BatchStatus.ERROR)
					.build();

			persist(newBatch);

			fireProcessingComplete(newBatch, e);
		}
	}

	@Override
	public void addBatchProcessingListener(BatchProcessingListener listener) {
		listenerList.add(BatchProcessingListener.class, listener);
	}

	@Override
	public void removeBatchProcessingListener(BatchProcessingListener listener) {
		listenerList.remove(BatchProcessingListener.class, listener);
	}

	@Transactional
	void persist(Batch batch) {
		try {
			batchDao.update(batch);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot update batch %s in database.", batch.getId()), e);
		}
	}

	private void postProcess(Batch batch) {
		try {
			ObmDomain domain = batch.getDomain();

			if (Iterables.getFirst(domain.getHosts().get(ServiceProperty.SMTP_IN), null) != null) {
				satelliteService.create(getSatelliteConfiguration(), domain).updateMTA();
			} else {
				logger.info(String.format("Not updating obm-satellite, the domain %s has no linked %s host.", domain.getName(), ServiceProperty.SMTP_IN));
			}
		}
		catch (Exception e) {
			throw new ProcessingException("Cannot update obm-satellite.", e);
		}
	}

	private Configuration getSatelliteConfiguration() throws DaoException, SystemUserNotFoundException {
		return new SystemUserSatelliteConfiguration(userSystemDao.getByLogin(OBM_SATELLITE_REQUEST));
	}

	private Operation processOperation(Operation operation, Batch batch) {
		Operation.Builder opBuilder = Operation
				.builder()
				.from(operation)
				.timecommit(dateProvider.getDate());

		for (OperationProcessor operationProcessor : operationProcessors) {
			if (operationProcessor.acceptsOperation(operation)) {
				try {
					operationProcessor.process(operation, batch);

					return opBuilder
							.status(BatchStatus.SUCCESS)
							.build();
				}
				catch (ProcessingException e) {
					logger.error(String.format("Error processing operation %s of batch %s.", operation.getId(), batch.getId()), e);

					return opBuilder
							.status(BatchStatus.ERROR)
							.error(e.toString())
							.build();
				}
			}
		}

		return opBuilder
				.status(BatchStatus.ERROR)
				.error(String.format("Cannot handle %s", operation))
				.build();
	}

	private void fireProcessingStarted(Batch batch) {
		for (BatchProcessingListener l : listenerList.getListeners(BatchProcessingListener.class)) {
			l.processingStarted(batch);
		}
	}

	private void fireProcessingComplete(Batch batch, Throwable throwable) {
		for (BatchProcessingListener l : listenerList.getListeners(BatchProcessingListener.class)) {
			l.processingComplete(batch, throwable);
		}
	}

	private void fireProcessingProgressed(Batch batch) {
		for (BatchProcessingListener l : listenerList.getListeners(BatchProcessingListener.class)) {
			l.processingProgressed(batch);
		}
	}

}
