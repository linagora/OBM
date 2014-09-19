/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
package org.obm.sync;

import java.sql.Connection;
import java.sql.SQLException;

import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.configuration.MultiNodeDatabaseConfiguration;
import org.obm.dbcp.MultiNodeDatabaseConnectionProvider;
import org.obm.dbcp.MultiNodeDatabaseConnectionProviderSelector;
import org.obm.dbcp.MultiNodeDatabaseDriverConfigurationProvider;
import org.obm.dbcp.MultiNodeHikariCPDatabaseConnectionProvider;
import org.obm.servlet.filter.resource.ResourcesHolder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class RequestScopedDatabaseConnectionProvider extends MultiNodeHikariCPDatabaseConnectionProvider {

	private final Provider<ResourcesHolder> resourcesHolderProvider;

	@Inject
	public RequestScopedDatabaseConnectionProvider(
			ITransactionAttributeBinder transactionAttributeBinder,
			MultiNodeDatabaseDriverConfigurationProvider databaseDriverConfigurationProvider, 
			MultiNodeDatabaseConfiguration databaseConfiguration,
			MultiNodeDatabaseConnectionProviderSelector selector,
			MultiNodeDatabaseConnectionProvider.ProviderFactory providerFactory,
			Provider<ResourcesHolder> resourcesHolderProvider) {
		super(transactionAttributeBinder, databaseDriverConfigurationProvider, databaseConfiguration, selector, providerFactory);

		this.resourcesHolderProvider = resourcesHolderProvider;
	}

	@Override
	public Connection getConnection() throws SQLException {
		ResourcesHolder resourcesHolder = resourcesHolderProvider.get();
		ConnectionResource connection = resourcesHolder.get(ConnectionResource.class);
		if (connection != null) {
			return connection;
		} else {
			ConnectionResource newConnection = ConnectionResource.wrap(super.getConnection());
			resourcesHolder.put(ConnectionResource.class, newConnection);
			return newConnection;
		}
	}
}
