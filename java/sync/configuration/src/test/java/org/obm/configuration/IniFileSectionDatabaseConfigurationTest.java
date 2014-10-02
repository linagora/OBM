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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.utils.IniFile;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(IniFileSectionDatabaseConfigurationTest.Env.class)
public class IniFileSectionDatabaseConfigurationTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
		}

	}

	@Inject
	private IniFile.Factory iniFileFactory;

	@Test
	public void testGetDatabaseMinConnectionPoolSizeShouldReturn1WhenMinAndMaxAreNotSet() {
		assertThat(newEmptyConf().getDatabaseMinConnectionPoolSize()).isEqualTo(1);
	}

	@Test
	public void testGetDatabaseMinConnectionPoolSizeShouldReturn1WhenMaxIsLowerThan10() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseMinConnectionPoolSize()).isEqualTo(1);
	}

	@Test
	public void testGetDatabaseMinConnectionPoolSizeShouldReturn5WhenMaxIs50AndMinIsNotSet() {
		assertThat(newConf("dbMasterWithMax50.ini", "master").getDatabaseMinConnectionPoolSize()).isEqualTo(5);
	}

	@Test
	public void testGetDatabaseMinConnectionPoolSize() {
		assertThat(newConf("dbMasterWithMinSize.ini", "master").getDatabaseMinConnectionPoolSize()).isEqualTo(20);
	}

	@Test
	public void testGetDatabaseMaxConnectionPoolSizeShouldReturnDefaultWhenNotSet() {
		assertThat(newEmptyConf().getDatabaseMaxConnectionPoolSize()).isEqualTo(10);
	}

	@Test
	public void testGetDatabaseMaxConnectionPoolSize() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseMaxConnectionPoolSize()).isEqualTo(5);
	}

	@Test(expected = NullPointerException.class)
	public void testGetDatabaseSystemShouldFailWhenNotSet() {
		assertThat(newEmptyConf().getDatabaseSystem()).isEqualTo(DatabaseFlavour.PGSQL);
	}

	@Test
	public void testGetDatabaseSystem() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseSystem()).isEqualTo(DatabaseFlavour.PGSQL);
	}

	@Test
	public void testGetDatabaseNameShouldReturnNullWhenNotSet() {
		assertThat(newEmptyConf().getDatabaseName()).isNull();
	}

	@Test
	public void testGetDatabaseName() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseName()).isEqualTo("obmdb");
	}

	@Test
	public void testGetDatabaseLoginShouldReturnNullWhenNotSet() {
		assertThat(newEmptyConf().getDatabaseLogin()).isNull();
	}

	@Test
	public void testGetDatabaseLogin() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseLogin()).isEqualTo("obm");
	}

	@Test
	public void testGetDatabaseHostShouldReturnNullWhenNotSet() {
		assertThat(newEmptyConf().getDatabaseHost()).isNull();
	}

	@Test
	public void testGetDatabaseHost() {
		assertThat(newConf("dbMaster.ini", "master").getDatabaseHost()).isEqualTo("localhost");
	}

	@Test
	public void testGetDatabasePortShouldReturnNullWhenNotSet() {
		assertThat(newEmptyConf().getDatabasePort()).isNull();
	}

	@Test
	public void testGetDatabasePort() {
		assertThat(newConf("dbMaster.ini", "master").getDatabasePort()).isEqualTo(5432);
	}

	@Test
	public void testGetDatabasePasswordShouldReturnNullWhenNotSet() {
		assertThat(newEmptyConf().getDatabasePassword()).isNull();
	}

	@Test
	public void testGetDatabasePassword() {
		assertThat(newConf("dbMaster.ini", "master").getDatabasePassword()).isEqualTo("obmpwd");
	}

	@Test
	public void testIsPostgresSSLEnabledShouldReturnFalseWhenNotSet() {
		assertThat(newEmptyConf().isPostgresSSLEnabled()).isFalse();
	}

	@Test
	public void testIsPostgresSSLEnabled() {
		assertThat(newConf("dbMaster.ini", "master").isPostgresSSLEnabled()).isTrue();
	}

	@Test
	public void testIsPostgresSSLNonValidatingShouldReturnFalseWhenNotSet() {
		assertThat(newEmptyConf().isPostgresSSLNonValidating()).isFalse();
	}

	@Test
	public void testIsPostgresSSLNonValidating() {
		assertThat(newConf("dbMaster.ini", "master").isPostgresSSLNonValidating()).isTrue();
	}

	@Test
	public void testIsReadOnlyShouldReturnFalseWhenNotSet() {
		assertThat(newEmptyConf().isReadOnly()).isFalse();
	}

	@Test
	public void testIsReadOnly() {
		assertThat(newConf("dbSlave.ini", "slave").isReadOnly()).isTrue();
	}

	private IniFileSectionDatabaseConfiguration newConf(String file, String section) {
		return new IniFileSectionDatabaseConfiguration(iniFileFactory.build(Resources.getResource(file).getFile()), section);
	}

	private IniFileSectionDatabaseConfiguration newEmptyConf() {
		return newConf("emptyDb.ini", "master");
	}

}
