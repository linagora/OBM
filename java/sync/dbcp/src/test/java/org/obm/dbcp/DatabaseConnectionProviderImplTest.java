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

package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.TransactionException;
import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.PostgresDriverConfiguration;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;


public class DatabaseConnectionProviderImplTest {

	private DatabaseConnectionProviderImpl dbConnProvider;
	private ITransactionAttributeBinder transactionAttributeBinder;
	private DatabaseConfiguration databaseConfiguration;

	@Before
	public void setUp() {
		Logger logger = EasyMock.createNiceMock(Logger.class);
		transactionAttributeBinder = EasyMock.createMock(ITransactionAttributeBinder.class);
		databaseConfiguration = new DatabaseConfigurationFixturePostgreSQL();
		dbConnProvider = new DatabaseConnectionProviderImpl(
				ImmutableSet.<DatabaseDriverConfiguration>of(new PostgresDriverConfiguration()),
				transactionAttributeBinder, databaseConfiguration, logger);
	}

	@Test(expected=SQLException.class)
	public void testGetConnection() throws SQLException {
			dbConnProvider.getConnection();
	}
	
	@Test
	public void testIsReadOnlyTransactionWhenTrue() throws TransactionException {
	    Transactional transactional = EasyMock.createMock(Transactional.class);
	    EasyMock.expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
	    EasyMock.expect(transactional.readOnly()).andReturn(true).once();
	    EasyMock.replay(transactionAttributeBinder, transactional);
	    Assert.assertTrue(dbConnProvider.isReadOnlyTransaction());
	}
	
	@Test
	public void testSetConnectionReadOnlyIfNecessaryOnTransactionReadOnlyButConnectionReadWrite() throws TransactionException, SQLException {
	    Connection connection = EasyMock.createMock(Connection.class);
	    Transactional transactional = EasyMock.createMock(Transactional.class);
	    EasyMock.expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
	    EasyMock.expect(transactional.readOnly()).andReturn(true).once();
	    EasyMock.expect(connection.isReadOnly()).andReturn(false).once();
	    connection.setReadOnly(true);
	    EasyMock.expectLastCall().once();
	}
	
	@After
	public void tearDown() {
	    dbConnProvider.cleanup();
	}
}
