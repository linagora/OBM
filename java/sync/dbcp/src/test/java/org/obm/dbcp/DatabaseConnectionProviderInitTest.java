/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.dbcp;

import static org.easymock.EasyMock.createControl;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.slf4j.Logger;

public class DatabaseConnectionProviderInitTest {

	private IMocksControl control;
	private DatabaseConnectionProviderImpl testee;
	private H2DriverConfiguration h2Driver;
	
	@Before
	public void setup() {
		control = createControl();
		ITransactionAttributeBinder transactionAttributeBinder = control.createMock(ITransactionAttributeBinder.class);
		Logger logger = control.createMock(Logger.class);
		h2Driver = new H2DriverConfiguration();
		testee = new DatabaseConnectionProviderImpl(
				transactionAttributeBinder, new DatabaseConfigurationFixtureH2(), h2Driver, logger);
	}

	@Test
	public void testGetConnection() throws SQLException {
		Connection connection = testee.getConnection();
		ResultSet result = connection.createStatement().executeQuery("SELECT 1");
		assertThat(result.first()).isTrue();
		assertThat(result.getInt(1)).isEqualTo(1);
	}
	
	
}
