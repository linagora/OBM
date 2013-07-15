/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.configuration.TransactionConfiguration;
import org.obm.configuration.module.LoggerModule;
import org.obm.dbcp.DatabaseConfigurationFixtureH2;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.dbcp.DatabaseConnectionProviderImpl;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.ObmInfoDaoJdbcImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.name.Names;

public class TestingProvisioningModule extends ProvisioningService {

	@Override
	protected void configureServlets() {
		super.configureServlets();

		install(new TransactionalModule());
		
		bind(DatabaseConnectionProvider.class).to(DatabaseConnectionProviderImpl.class);
		bind(TransactionConfiguration.class).to(TestTransactionConfiguration.class);
		bind(DatabaseDriverConfiguration.class).to(H2DriverConfiguration.class);
		bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixtureH2.class);
		bind(ObmInfoDao.class).to(ObmInfoDaoJdbcImpl.class);
		
		bind(Logger.class).annotatedWith(Names.named(LoggerModule.CONFIGURATION))
			.toInstance(LoggerFactory.getLogger(ProvisioningService.class));
	}
}
