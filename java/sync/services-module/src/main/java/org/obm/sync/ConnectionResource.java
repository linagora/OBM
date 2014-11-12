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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.obm.servlet.filter.resource.Resource;

import com.google.common.base.Throwables;

public class ConnectionResource implements Resource, Connection {
	
	public static ConnectionResource wrap(Connection connection) {
		return new ConnectionResource(connection);
	}
	
	private final Connection connection;

	public ConnectionResource(Connection connection) {
		this.connection = connection;
	}
	
	@Override
	public void close() throws SQLException {
		//Do nothing
	}
	
	@Override
	public void closeResource() {
		try {
			connection.close();
		} catch (SQLException e) {
			Throwables.propagate(e);
		}
	}

	//auto-generated stuff starts here
	
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return connection.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return connection.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		return connection.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql)
			throws SQLException {
		return connection.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return connection.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return connection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		connection.commit();
	}

	@Override
	public void rollback() throws SQLException {
		connection.rollback();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.createStatement(resultSetType,
				resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql,
			int resultSetType, int resultSetConcurrency)
			throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		return connection.prepareCall(sql, resultSetType,
				resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		connection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return connection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return connection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.createStatement(resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql,
			int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		return connection.prepareStatement(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		return connection.prepareCall(sql, resultSetType,
				resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql,
			int autoGeneratedKeys) throws SQLException {
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql,
			int[] columnIndexes) throws SQLException {
		return connection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql,
			String[] columnNames) throws SQLException {
		return connection.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return connection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		connection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		connection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return connection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return connection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return connection.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		connection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		return connection.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		connection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		connection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return connection.getNetworkTimeout();
	}
	
}