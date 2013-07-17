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
import java.util.Set;

import org.obm.annotations.transactional.Transactional;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.Batch.Builder;
import org.obm.provisioning.beans.Batch.Id;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.OperationProcessor;
import org.obm.satellite.client.Configuration;
import org.obm.satellite.client.SatelliteService;
import org.obm.sync.date.DateProvider;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.system.ObmSystemUser;

@Singleton
public class BatchProcessorImpl implements BatchProcessor {

	private static final String OBMSATELLITEREQUEST = "obmsatelliterequest";

	private final DateProvider dateProvider;
	private final SatelliteService satelliteService;
	private final Set<OperationProcessor> operationProcessors;
	private final BatchDao batchDao;
	private final UserSystemDao userSystemDao;
	private final Map<Batch.Id, Batch.Builder> runningBatches;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public BatchProcessorImpl(SatelliteService satelliteService, Set<OperationProcessor> operationProcessors,
			DateProvider dateProvider, BatchDao batchDao, UserSystemDao userSystemDao) {
		this.satelliteService = satelliteService;
		this.operationProcessors = operationProcessors;
		this.dateProvider = dateProvider;
		this.batchDao = batchDao;
		this.userSystemDao = userSystemDao;

		this.runningBatches = Maps.newConcurrentMap();
	}

	@Override
	@Transactional
	public void process(Batch batch) throws ProcessingException {
		Id batchId = batch.getId();
		Batch.Builder batchBuilder = Batch
				.builder()
				.from(batch)
				.status(BatchStatus.RUNNING)
				.timecommit(dateProvider.getDate());

		runningBatches.put(batchId, batchBuilder);

		try {
			for (Operation operation : batch.getOperations()) {
				batchBuilder.operation(processOperation(operation, batch));
			}

			Batch newBatch = batchBuilder
					.status(BatchStatus.SUCCESS)
					.build();

			postProcess(newBatch);
			persist(newBatch);
		}
		catch (Exception e) {
			logger.error(String.format("Error processing batch %s.", batchId), e);

			persist(batchBuilder
					.status(BatchStatus.ERROR)
					.build());
		}
		finally {
			runningBatches.remove(batchId);
		}
	}

	@Override
	public Batch getRunningBatch(Id id) {
		Builder batchBuilder = runningBatches.get(id);

		return batchBuilder != null ? batchBuilder.build() : null;
	}

	private void persist(Batch batch) {
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
			ServiceProperty mailSmtpIn = ServiceProperty
					.builder()
					.service("mail")
					.property("smtp_in")
					.build();

			if (Iterables.getFirst(domain.getHosts().get(mailSmtpIn), null) != null) {
				satelliteService.create(getSatelliteConfiguration(), domain).updateMTA();
			} else {
				logger.info(String.format("Not updating obm-satellite, the domain %s has no linked %s host.", domain.getName(), mailSmtpIn));
			}
		}
		catch (Exception e) {
			throw new ProcessingException("Cannot update obm-satellite.", e);
		}
	}

	private Configuration getSatelliteConfiguration() throws DaoException, SystemUserNotFoundException {
		return new SystemUserSatelliteConfiguration(userSystemDao.getByLogin(OBMSATELLITEREQUEST));
	}

	private Operation processOperation(Operation operation, Batch batch) {
		Operation.Builder opBuilder = Operation
				.builder()
				.from(operation)
				.timecommit(dateProvider.getDate());

		for (OperationProcessor operationProcessor : operationProcessors) {
			if (operationProcessor.acceptsOperation(operation)) {
				try {
					operationProcessor.process(operation);

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

	private static class SystemUserSatelliteConfiguration implements Configuration {

		private final ObmSystemUser systemUser;

		private SystemUserSatelliteConfiguration(ObmSystemUser systemUser) {
			this.systemUser = systemUser;
		}

		@Override
		public String getUsername() {
			return systemUser.getLogin();
		}

		@Override
		public String getPassword() {
			return systemUser.getPassword();
		}

		@Override
		public boolean isIMAPServerManaged() {
			return false; // For now...
		}

		@Override
		public int getSatellitePort() {
			return DEFAULT_SATELLITE_PORT;
		}

	}

}
