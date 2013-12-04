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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.ITransactionAttributeBinder;
import org.obm.annotations.transactional.TransactionException;
import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.jdbc.DatabaseDriverConfiguration;
import org.obm.dbcp.jdbc.H2DriverConfiguration;
import org.obm.dbcp.jdbc.PostgresDriverConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class)
public class DatabaseConnectionProviderImplTest {
	
	private DatabaseConnectionProviderImpl testee;
	private ITransactionAttributeBinder transactionAttributeBinder;
	private DatabaseConfiguration databaseConfiguration;
	private IMocksControl control;
	private Logger logger;
	private DatabaseDriverConfigurationProvider databaseDriverConfigurationProvider;
	
	@Before
	public void setUp() {
		control = createControl();
		
		logger = control.createMock(Logger.class);
		logger.info(anyObject(String.class), anyObject(Object.class));
		expectLastCall().anyTimes();
		logger.info(anyObject(String.class), anyObject(Object.class), anyObject(Object.class));
		expectLastCall().anyTimes();
		
		transactionAttributeBinder = control.createMock(ITransactionAttributeBinder.class);
		databaseConfiguration = new DatabaseConfigurationFixturePostgreSQL();
		databaseDriverConfigurationProvider = new DatabaseDriverConfigurationProvider(ImmutableSet.<DatabaseDriverConfiguration>of(new PostgresDriverConfiguration()), databaseConfiguration);
	}
	
	@After
	public void tearDown() throws Exception {
		if (testee != null) {
			testee.shutdown();
		}
		control.verify();
	}
	
	@Test(expected=SQLException.class)
	public void testGetConnectionWhenNoConnectionAvailable() throws SQLException {
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		testee.getConnection();
	}
	
	@Test(expected=SQLException.class)
	public void testGetConnectionExceptionOnFirstStatement() throws Exception {
		PreparedStatement preparedStatement = control.createMock(PreparedStatement.class);
		expect(preparedStatement.executeUpdate())
			.andThrow(new SQLException());
		
		Connection connection = control.createMock(Connection.class);
		expect(connection.prepareStatement(anyObject(String.class)))
			.andReturn(preparedStatement);
		boolean readOnly = true;
		expect(connection.isReadOnly())
			.andReturn(readOnly);
		connection.close();
		expectLastCall();
		
		DatabaseDriverConfiguration databaseDriverConfiguration = databaseDriverConfigurationProvider.get();
		PoolingDataSourceDecorator poolingDataSource = control.createMock(PoolingDataSourceDecorator.class);
		expect(poolingDataSource.getConnection())
			.andReturn(connection);
		expect(poolingDataSource.getDatabaseDriverConfiguration())
			.andReturn(databaseDriverConfiguration).anyTimes();
		poolingDataSource.close();
		expectLastCall();
		
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction())
			.andReturn(transactional).once();
		expect(transactional.readOnly())
			.andReturn(readOnly).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource);
		testee.getConnection();
	}
	
	@Test
	public void testGetConnection() throws Exception {
		PreparedStatement preparedStatement = control.createMock(PreparedStatement.class);
		expect(preparedStatement.executeUpdate())
			.andReturn(0);
		
		Connection connection = control.createMock(Connection.class);
		expect(connection.prepareStatement(anyObject(String.class)))
			.andReturn(preparedStatement);
		boolean readOnly = true;
		expect(connection.isReadOnly())
			.andReturn(readOnly);
		
		DatabaseDriverConfiguration databaseDriverConfiguration = databaseDriverConfigurationProvider.get();
		PoolingDataSourceDecorator poolingDataSource = control.createMock(PoolingDataSourceDecorator.class);
		expect(poolingDataSource.getConnection())
			.andReturn(connection);
		expect(poolingDataSource.getDatabaseDriverConfiguration())
			.andReturn(databaseDriverConfiguration).anyTimes();
		poolingDataSource.close();
		expectLastCall();
		
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction())
			.andReturn(transactional).once();
		expect(transactional.readOnly())
			.andReturn(readOnly).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource);
		testee.getConnection();
	}

	@Test
	public void testIsReadOnlyTransactionWhenTrue() throws TransactionException {
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
		expect(transactional.readOnly()).andReturn(true).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		assertThat(testee.isReadOnlyTransaction()).isTrue();
	}

	@Test
	public void testIsReadOnlyTransactionWhenFalse() throws TransactionException {
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
		expect(transactional.readOnly()).andReturn(false).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		assertThat(testee.isReadOnlyTransaction()).isFalse();
	}

	@Test
	public void testIsReadOnlyTransactionWhenNoTransactional() throws TransactionException {
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(null).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		assertThat(testee.isReadOnlyTransaction()).isTrue();
	}

	@Test
	public void testSetConnectionReadOnlyIfNecessaryOnTransactionReadOnlyButConnectionReadWrite() throws TransactionException, SQLException {
		Connection connection = control.createMock(Connection.class);
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction())
			.andReturn(transactional).once();
		expect(transactional.readOnly())
			.andReturn(true).once();
		expect(connection.isReadOnly())
			.andReturn(false).once();
		connection.setReadOnly(true);
		expectLastCall().once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		testee.setConnectionReadOnlyIfNecessary(connection);
	}

	@Test
	public void testSetConnectionReadOnlyIfNecessaryOnTransactionReadWriteButConnectionReadOnly() throws TransactionException, SQLException {
		Connection connection = control.createMock(Connection.class);
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
		expect(transactional.readOnly()).andReturn(false).once();
		expect(connection.isReadOnly()).andReturn(true).once();
		connection.setReadOnly(false);
		expectLastCall().once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		testee.setConnectionReadOnlyIfNecessary(connection);
	}
	
	@Test
	public void testGetJdbcObjectWhenH2() throws SQLException {
		databaseConfiguration = new DatabaseConfigurationFixtureH2();
		databaseDriverConfigurationProvider = new DatabaseDriverConfigurationProvider(ImmutableSet.<DatabaseDriverConfiguration>of(new H2DriverConfiguration()), databaseConfiguration);
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		Object aTree = testee.getJdbcObject("type", "aChristmasTree");
		assertThat(aTree).isEqualTo("aChristmasTree");
	}
	
	@Test
	public void testGetJdbcObjectWhenPGSQL() throws SQLException {
		PGobject expectedObject = new PGobject();
		expectedObject.setType("type");
		expectedObject.setValue("value");
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		Object value = testee.getJdbcObject("type", "value");
		assertThat(value).isEqualTo(expectedObject);
	}

	private void databaseConnectionProvider(PoolingDataSourceDecorator poolingDataSource) {
		testee = new DatabaseConnectionProviderImpl(
				transactionAttributeBinder, poolingDataSource);
	}

	private PoolingDataSourceDecorator poolingDataSource() {
		return new PoolingDataSourceDecorator(databaseDriverConfigurationProvider, databaseConfiguration, logger);
	}

	@Test
	public void testSetConnectionReadOnlyIfNecessaryOnTransactionReadOnlyAndConnectionReadOnly() throws TransactionException, SQLException {
		Connection connection = control.createMock(Connection.class);
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
		expect(transactional.readOnly()).andReturn(true).once();
		expect(connection.isReadOnly()).andReturn(true).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		testee.setConnectionReadOnlyIfNecessary(connection);
	}

	@Test
	public void testSetConnectionReadOnlyIfNecessaryOnTransactionReadWriteAndConnectionReadWrite() throws TransactionException, SQLException {
		Connection connection = control.createMock(Connection.class);
		Transactional transactional = control.createMock(Transactional.class);
		expect(transactionAttributeBinder.getTransactionalInCurrentTransaction()).andReturn(transactional).once();
		expect(transactional.readOnly()).andReturn(false).once();
		expect(connection.isReadOnly()).andReturn(false).once();
		
		control.replay();
		databaseConnectionProvider(poolingDataSource());
		testee.setConnectionReadOnlyIfNecessary(connection);
	}	
}
