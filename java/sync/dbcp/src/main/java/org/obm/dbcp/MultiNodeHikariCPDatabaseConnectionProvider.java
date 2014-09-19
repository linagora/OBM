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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.MultiNodeDatabaseConfiguration;
import org.obm.logger.LoggerModule;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class MultiNodeHikariCPDatabaseConnectionProvider
	extends DatabaseConnectionProviderImpl
	implements MultiNodeDatabaseConnectionProvider {

	@Singleton
	public static class HikariCPProviderFactory implements ProviderFactory {

		private final Logger configurationLogger;

		@Inject
		private HikariCPProviderFactory(@Named(LoggerModule.CONFIGURATION) Logger configurationLogger) {
			this.configurationLogger = configurationLogger;
		}

		@Override
		public DatabaseConnectionProvider create(
				String name,
				ITransactionAttributeBinder transactionAttributeBinder,
				MultiNodeDatabaseDriverConfigurationFactory dbDriverConfigurationFactory,
				DatabaseConfiguration dbConfiguration) {
			HikariCPDatabaseConnectionProvider provider = new HikariCPDatabaseConnectionProvider(dbDriverConfigurationFactory, dbConfiguration, configurationLogger);

			provider.getPool().setReadOnly(dbConfiguration.isReadOnly());
			provider.getPool().setPoolName(name);

			return provider;
		}

	}

	private final DatabaseConnectionProvider readWriteProvider;
	private final List<DatabaseConnectionProvider> allProviders;
	private final MultiNodeDatabaseConnectionProviderSelector selector;

	@Inject
	protected MultiNodeHikariCPDatabaseConnectionProvider(
			ITransactionAttributeBinder transactionAttributeBinder,
			MultiNodeDatabaseDriverConfigurationFactory databaseDriverConfigurationFactory,
			MultiNodeDatabaseConfiguration databaseConfiguration,
			MultiNodeDatabaseConnectionProviderSelector selector,
			MultiNodeDatabaseConnectionProvider.ProviderFactory providerFactory) {
		super(databaseDriverConfigurationFactory.create(), transactionAttributeBinder);

		DatabaseConnectionProvider rwProvider = null;
		ImmutableList.Builder<DatabaseConnectionProvider> builder = ImmutableList.builder();

		for (Entry<String, DatabaseConfiguration> entry : databaseConfiguration.getDatabaseConfigurations().entrySet()) {
			String name = entry.getKey();
			DatabaseConfiguration nodeConfig = entry.getValue();
			DatabaseConnectionProvider provider = providerFactory.create(name, transactionAttributeBinder, databaseDriverConfigurationFactory, nodeConfig);

			builder.add(provider);
			if (!nodeConfig.isReadOnly()) {
				rwProvider = provider;
			}
		}

		this.readWriteProvider = rwProvider;
		this.allProviders = builder.build();
		this.selector = selector;
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (isReadOnlyTransaction()) {
			return selector.select(allProviders).getConnection();
		}

		return readWriteProvider.getConnection();
	}

	@Override
	boolean isReadOnlyTransaction() {
		try {
			return super.isReadOnlyTransaction();
		} catch (Exception e) {
			logger.warn("Error while reading transaction read-only status. This most likely"
					+ " indicates a database access outside of a transaction, which is probably an error."
					+ " The following stacktrace is here for easier troubleshooting.", e);
			return true;
		}
	}

	@Override
	public void cleanup() {
		for (DatabaseConnectionProvider provider : allProviders) {
			provider.cleanup();
		}
	}

}
