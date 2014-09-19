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
package org.obm.configuration;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.utils.IniFile;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(IniFileMultiNodeDatabaseConfigurationTest.Env.class)
public class IniFileMultiNodeDatabaseConfigurationTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			bind(DatabaseConfiguration.class).toInstance(createNiceMock(DatabaseConfiguration.class));
		}

	}

	@Inject
	private DatabaseConfiguration fallbackConfiguration;
	@Inject
	private IniFile.Factory iniFileFactory;

	private DatabaseConfiguration masterConfig;
	private DatabaseConfiguration slaveConfig;

	@Before
	public void setUp() {
		masterConfig = newNodeConf("dbMaster.ini", "master");
		slaveConfig = newNodeConf("dbSlave.ini", "slave");

		replay(fallbackConfiguration);
	}

	@Test
	public void testGetDatabaseConfigurations() {
		Map<String, DatabaseConfiguration> configs = newConf("dbMaster.ini").getDatabaseConfigurations();

		assertThat(configs).isEqualTo(ImmutableMap.of("master", masterConfig));
	}

	@Test
	public void testGetDatabaseConfigurationsShouldSupportMultipleNodes() {
		Map<String, DatabaseConfiguration> configs = newConf("dbMasterSlave.ini").getDatabaseConfigurations();

		assertThat(configs).isEqualTo(ImmutableMap.of("master", masterConfig, "slave", slaveConfig));
	}

	@Test
	public void testGetDatabaseConfigurationsShouldFallbackToDefaultConfigurationWhenNotSet() {
		Map<String, DatabaseConfiguration> configs = newEmptyConf().getDatabaseConfigurations();

		assertThat(configs).isEqualTo(ImmutableMap.of("default", fallbackConfiguration));
	}

	private IniFileMultiNodeDatabaseConfiguration newConf(String file) {
		return new IniFileMultiNodeDatabaseConfiguration(iniFileFactory, Resources.getResource(file).getFile(), fallbackConfiguration);
	}

	private IniFileMultiNodeDatabaseConfiguration newEmptyConf() {
		return new IniFileMultiNodeDatabaseConfiguration(iniFileFactory, "idontexist", fallbackConfiguration);
	}

	private IniFileSectionDatabaseConfiguration newNodeConf(String file, String section) {
		return new IniFileSectionDatabaseConfiguration(iniFileFactory.build(Resources.getResource(file).getFile()), section);
	}

}
