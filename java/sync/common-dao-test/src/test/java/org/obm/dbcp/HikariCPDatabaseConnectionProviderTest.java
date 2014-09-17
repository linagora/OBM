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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import com.google.common.collect.ImmutableSet;

public class HikariCPDatabaseConnectionProviderTest {

	private HikariCPDatabaseConnectionProvider dbcp;
	private H2DriverConfiguration h2Driver;

	private Logger logger = NOPLogger.NOP_LOGGER;

	@Before
	public void setup() {
		h2Driver = new H2DriverConfiguration();
	}

	@After
	public void teardown() {
		dbcp.shutdown();
	}

	@Test
	public void testGetConnection() throws SQLException {
		Connection connection = newDBCP(new DatabaseConfigurationFixtureH2("", 1, "Hikari")).getConnection();
		ResultSet result = connection.createStatement().executeQuery("SELECT 1");

		assertThat(result.first()).isTrue();
		assertThat(result.getInt(1)).isEqualTo(1);

		connection.close();
	}

	@Test
	public void testGetConnectionShouldSetAutocommitFalse() throws SQLException {
		Connection connection = newDBCP(new DatabaseConfigurationFixtureH2("", 1, "Hikari")).getConnection();

		assertThat(connection.getAutoCommit()).isFalse();

		connection.close();
	}

	@Test(expected = SQLException.class)
	public void testGetConnectionShouldFailWhenDatabaseIsNotStarted() throws SQLException {
		newDBCP(new DatabaseConfigurationFixtureH2(";IFEXISTS=TRUE", 1, "HikariNonExistentDB")).getConnection();
	}

	@Test
	public void testGetConnectionShouldSucceedWhenDatabaseIsStartingLater() throws SQLException {
		Connection connection = null;

		newDBCP(new DatabaseConfigurationFixtureH2(";IFEXISTS=TRUE", 0, "Hikari"));

		try {
			dbcp.getConnection(); // fail getting a connection because database doesn't exist yet
		} catch (SQLException e) {
			// make database exists
			DatabaseConfigurationFixtureH2 configuration = new DatabaseConfigurationFixtureH2("", 1, "Hikari");
			DriverManager.getConnection(h2Driver.getDriverProperties(configuration).get("URL"), configuration.getDatabaseLogin(), configuration.getDatabasePassword());

			connection = dbcp.getConnection();
			connection.close();
		}

		assertThat(connection).isNotNull();
	}

	private HikariCPDatabaseConnectionProvider newDBCP(DatabaseConfiguration databaseConfiguration) {
		DatabaseDriverConfigurationProvider databaseDriverConfigurationProvider = new DatabaseDriverConfigurationProvider(ImmutableSet.<DatabaseDriverConfiguration> of(h2Driver), databaseConfiguration);

		dbcp = new HikariCPDatabaseConnectionProvider(databaseDriverConfigurationProvider, databaseConfiguration, logger);
		dbcp.getPool().setConnectionTimeout(1000); // Could be less I guess

		return dbcp;
	}

}
