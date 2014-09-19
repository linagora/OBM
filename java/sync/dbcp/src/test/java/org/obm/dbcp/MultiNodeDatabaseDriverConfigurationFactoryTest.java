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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;
import org.obm.configuration.MultiNodeDatabaseConfiguration;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.MySQLDriverConfiguration;
import org.obm.dbcp.jdbc.PostgresDriverConfiguration;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


public class MultiNodeDatabaseDriverConfigurationFactoryTest {

	private final DatabaseDriverConfiguration postgresConfiguration = new PostgresDriverConfiguration();
	private final DatabaseDriverConfiguration mysqlConfiguration = new MySQLDriverConfiguration();
	private final DatabaseDriverConfiguration h2Configuration = new H2DriverConfiguration();
	private final Set<DatabaseDriverConfiguration> drivers = ImmutableSet.of(postgresConfiguration, mysqlConfiguration, h2Configuration);

	private IMocksControl control;
	private MultiNodeDatabaseConfiguration configs;
	private DatabaseConfiguration nodeConfig;

	@Before
	public void setUp() {
		control = createControl();
		configs = control.createMock(MultiNodeDatabaseConfiguration.class);
		nodeConfig = control.createMock(DatabaseConfiguration.class);
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testConstructorShouldFetchSingleConfiguration() {
		expect(configs.getDatabaseConfigurations()).andReturn(ImmutableMap.of("default", nodeConfig));
		expect(nodeConfig.getDatabaseSystem()).andReturn(DatabaseFlavour.PGSQL);
		control.replay();

		assertThat(new MultiNodeDatabaseDriverConfigurationFactory(drivers, configs).create()).isEqualTo(postgresConfiguration);
	}

	@Test
	public void testConstructorShouldFetchAnyConfigurationWhenSeveralExist() {
		expect(configs.getDatabaseConfigurations()).andReturn(ImmutableMap.of("default", nodeConfig, "master", nodeConfig));
		expect(nodeConfig.getDatabaseSystem()).andReturn(DatabaseFlavour.MYSQL);
		control.replay();

		assertThat(new MultiNodeDatabaseDriverConfigurationFactory(drivers, configs).create()).isEqualTo(mysqlConfiguration);
	}

}
