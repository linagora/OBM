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
package org.obm.provisioning;

import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;
import org.obm.provisioning.processing.OperationProcessor;
import org.obm.provisioning.processing.impl.BatchTrackerImpl;
import org.obm.provisioning.processing.impl.ParallelBatchProcessor;
import org.obm.provisioning.processing.impl.events.CreateEventOperationProcessor;
import org.obm.provisioning.processing.impl.groups.AddSubgroupToGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.AddUserToGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.CreateGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.DeleteGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.DeleteSubgroupFromGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.DeleteUserFromGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.PatchGroupOperationProcessor;
import org.obm.provisioning.processing.impl.groups.PutGroupOperationProcessor;
import org.obm.provisioning.processing.impl.users.CreateUserOperationProcessor;
import org.obm.provisioning.processing.impl.users.DeleteUserOperationProcessor;
import org.obm.provisioning.processing.impl.users.PatchUserOperationProcessor;
import org.obm.provisioning.processing.impl.users.PutUserOperationProcessor;
import org.obm.provisioning.processing.impl.users.sieve.SieveClientFactory;
import org.obm.provisioning.processing.impl.users.sieve.SieveScriptUpdaterFactory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class BatchProcessingModule extends AbstractModule {

	private static final Integer NB_PARALLEL_BATCHES = 1;

	@Override
	protected void configure() {
		Multibinder<OperationProcessor> multibinder = Multibinder.newSetBinder(binder(), OperationProcessor.class);

		bindConstant().annotatedWith(Names.named("nbParallelBatches")).to(NB_PARALLEL_BATCHES);
		bind(BatchProcessor.class).to(ParallelBatchProcessor.class);
		bind(BatchTracker.class).to(BatchTrackerImpl.class);
		bind(SieveClientFactory.class);
		bind(SieveScriptUpdaterFactory.class);

		multibinder.addBinding().to(CreateUserOperationProcessor.class);
		multibinder.addBinding().to(DeleteUserOperationProcessor.class);
		multibinder.addBinding().to(PutUserOperationProcessor.class);
		multibinder.addBinding().to(PatchUserOperationProcessor.class);
		multibinder.addBinding().to(DeleteGroupOperationProcessor.class);
		multibinder.addBinding().to(AddUserToGroupOperationProcessor.class);
		multibinder.addBinding().to(DeleteUserFromGroupOperationProcessor.class);
		multibinder.addBinding().to(AddSubgroupToGroupOperationProcessor.class);
		multibinder.addBinding().to(DeleteSubgroupFromGroupOperationProcessor.class);
		multibinder.addBinding().to(PutGroupOperationProcessor.class);
		multibinder.addBinding().to(PatchGroupOperationProcessor.class);
		multibinder.addBinding().to(CreateGroupOperationProcessor.class);
		multibinder.addBinding().to(CreateEventOperationProcessor.class);
	}

}
