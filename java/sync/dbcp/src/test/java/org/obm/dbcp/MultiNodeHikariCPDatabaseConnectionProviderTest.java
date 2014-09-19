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
package org.obm.dbcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.easymock.IExpectationSetters;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.Propagation;
import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.IniFileMultiNodeDatabaseConfiguration;
import org.obm.configuration.MultiNodeDatabaseConfiguration;
import org.obm.dbcp.MultiNodeDatabaseConnectionProvider.ProviderFactory;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

@RunWith(GuiceRunner.class)
@GuiceModule(MultiNodeHikariCPDatabaseConnectionProviderTest.Env.class)
public class MultiNodeHikariCPDatabaseConnectionProviderTest {

	private static final int ITERATIONS = 30;

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			IMocksControl control = createControl();
			final Map<String, DatabaseConnectionProvider> providers = ImmutableMap.of(
					"master", control.createMock(DatabaseConnectionProvider.class),
					"slave1", control.createMock(DatabaseConnectionProvider.class),
					"slave2", control.createMock(DatabaseConnectionProvider.class));

			bind(IMocksControl.class).toInstance(control);
			bind(ITransactionAttributeBinder.class).toInstance(control.createMock(ITransactionAttributeBinder.class));
			bind(ProviderFactory.class).toInstance(new ProviderFactory() {

				@Override
				public DatabaseConnectionProvider create(
						String name,
						ITransactionAttributeBinder transactionAttributeBinder,
						MultiNodeDatabaseDriverConfigurationProvider dbDriverConfigurationFactory,
						DatabaseConfiguration dbConfiguration) {
					return providers.get(name);
				}

			});

			install(new DatabaseDriversModule());

			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
			bind(MultiNodeDatabaseConfiguration.class).to(IniFileMultiNodeDatabaseConfiguration.class);
			bind(MultiNodeDatabaseConnectionProviderSelector.class).to(RoundRobinMultiNodeDatabaseConnectionProviderSelector.class);

			bind(String.class).annotatedWith(Names.named("dbConfigurationFile")).toInstance(Resources.getResource("db1Master2Slaves.ini").getFile());

			bind(DatabaseConnectionProvider.class).annotatedWith(Names.named("master")).toInstance(providers.get("master"));
			bind(DatabaseConnectionProvider.class).annotatedWith(Names.named("slave1")).toInstance(providers.get("slave1"));
			bind(DatabaseConnectionProvider.class).annotatedWith(Names.named("slave2")).toInstance(providers.get("slave2"));
		}

	}

	@Inject
	private IMocksControl control;
	@Inject
	private ITransactionAttributeBinder binder;
	@Inject
	private MultiNodeHikariCPDatabaseConnectionProvider dbcp;

	@Inject
	@Named("master")
	private DatabaseConnectionProvider masterProvider;
	@Inject
	@Named("slave1")
	private DatabaseConnectionProvider slave1Provider;
	@Inject
	@Named("slave2")
	private DatabaseConnectionProvider slave2Provider;

	@After
	public void teardown() {
		control.verify();
	}

	@Test
	public void testGetConnectionShouldAskMasterPoolWhenReadWrite() throws Exception {
		expectReadWriteTransaction();
		expect(masterProvider.getConnection()).andReturn(null);
		control.replay();

		assertThat(dbcp.getConnection()).isNull();
	}

	@Test
	public void testGetConnectionShouldRoundRobinOnAllPoolsWhenReadOnly() throws Exception {
		expectReadOnlyTransaction().times(ITERATIONS);
		expect(masterProvider.getConnection()).andReturn(null).times(ITERATIONS / 3);
		expect(slave1Provider.getConnection()).andReturn(null).times(ITERATIONS / 3);
		expect(slave2Provider.getConnection()).andReturn(null).times(ITERATIONS / 3);
		control.replay();

		for (int i = 0; i < ITERATIONS; i++) {
			dbcp.getConnection();
		}
	}

	private IExpectationSetters<Transactional> expectReadOnlyTransaction() throws Exception {
		return expect(binder.getTransactionalInCurrentTransaction()).andReturn(newTransactional(true));
	}

	private IExpectationSetters<Transactional> expectReadWriteTransaction() throws Exception {
		return expect(binder.getTransactionalInCurrentTransaction()).andReturn(newTransactional(false));
	}

	private Transactional newTransactional(final boolean readOnly) {
		return new Transactional() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return null;
			}

			@Override
			public boolean readOnly() {
				return readOnly;
			}

			@Override
			public Propagation propagation() {
				return null;
			}

			@Override
			public Class<? extends Exception>[] noRollbackOn() {
				return null;
			}

		};
	}

}
