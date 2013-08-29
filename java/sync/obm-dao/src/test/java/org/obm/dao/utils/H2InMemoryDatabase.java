/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012 Linagora
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
 * <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.dao.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.h2.tools.RunScript;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An {@link ExternalResource} that loads an in-memory H2 database before a test and shut it down afterwards.<br />
 * Typical usage is to instantiate it and annotate it with a {@link Rule} annotation.
 */
@Singleton
public class H2InMemoryDatabase extends ExternalResource {
	public static final String DB_URL = "jdbc:h2:mem:daotest";

	private final String schema;
	private final Set<Connection> openedConnections;

	static {
		try {
			Class.forName("org.h2.Driver");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Builds a new {@link H2InMemoryDatabase}.
	 * 
	 * @param initialSql The initial SQL script to load when the DB is started.
	 */
	@Inject
	public H2InMemoryDatabase(String initialSql) {
		this.schema = initialSql;
		this.openedConnections = new HashSet<Connection>();
	}

	@Override
	protected void before() throws Throwable {
		resetDatabase();
		if (schema != null)
			importSchema();
	}

	@Override
	protected void after() {
		closeConnections();
	}

	/**
	 * Closes all opened connections to the in-memory database.
	 */
	private void closeConnections() {
		Iterator<Connection> connections = openedConnections.iterator();

		// Close all opened db connections
		while (connections.hasNext()) {
			try {
				connections.next().close();
			}
			catch (Exception e) {
				// Ignored
			}

			connections.remove();
		}
	}

	private void resetDatabase() throws Exception {
		Statement stat = getConnection().createStatement();
		try {
			stat.execute("DROP ALL OBJECTS");
		}
		finally {
			stat.close();
		}
	}

	/**
	 * Imports the SQL schema in the database.<br />
	 * This is done through the help of H2's {@link RunScript} tool.
	 * 
	 * @throws Exception If an error occurs while importing the schema.
	 */
	protected void importSchema() throws Exception {
		Reader reader = null;
		InputStream stream = getClass().getClassLoader().getResourceAsStream(schema);

		try {
			RunScript.execute(getConnection(), reader = new InputStreamReader(stream));
		}
		finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(stream);
		}
	}

	/**
	 * Creates and returns a new {@link Connection} instance to the embedded database.
	 * 
	 * @return The created {@link Connection} instance.
	 * 
	 * @throws Exception If the connection couldn't be created.
	 */
	public Connection getConnection() throws Exception {
		Connection connection = DriverManager.getConnection(DB_URL);

		openedConnections.add(connection);

		return connection;
	}

	private PreparedStatement prepareStatement(Connection connection, String sql, Object... parameters) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);

		// Populate parameters in prepared statement
		// This should infer the actual SQL type of each parameter automatically
		if (!ArrayUtils.isEmpty(parameters)) {
			for (int i = 0; i < parameters.length; i++)
				statement.setObject(i + 1, parameters[i]);
		}

		return statement;
	}

	/**
	 * Executes a SQL query and returns the resulting {@link ResultSet}.
	 * 
	 * @param connection The SQL {@link Connection} to use.
	 * @param sql The SQL query to execute.
	 * @param parameters The optional SQL parameters.
	 * 
	 * @return The {@link ResultSet} resulting from the query or {@code null} if no results are available.
	 * 
	 * @throws Exception If any error occurs while executing the query.
	 */
	public ResultSet execute(Connection connection, String sql, Object... parameters) throws Exception {
		ResultSet resultSet = null;
		PreparedStatement statement = prepareStatement(connection, sql, parameters);

		if (statement.execute()) {
			resultSet = statement.getResultSet();
		}

		return resultSet;
	}

	public int executeUpdate(Connection connection, String sql, Object... parameters) throws Exception {
		PreparedStatement statement = prepareStatement(connection, sql, parameters);

		statement.execute();

		return statement.getUpdateCount();
	}

	/**
	 * Executes a SQL query and returns the resulting {@link ResultSet}.<br />
	 * This simply delegates to the {@link #execute(Connection, String, Object...)} method.
	 * 
	 * @param sql The SQL query to execute.
	 * @param parameters The optional SQL parameters.
	 * 
	 * @return The {@link ResultSet} resulting from the query or {@code null} if no results are available.
	 * 
	 * @throws Exception If any error occurs while executing the query.
	 */
	public ResultSet execute(String sql, Object... parameters) throws Exception {
		return execute(getConnection(), sql, parameters);
	}

	/**
	 * Executes a SQL UPDATE, INSERT or DELETE query.
	 * 
	 * @param sql The SQL query.
	 * @param parameters The optional query parameters.
	 * 
	 * @throws Exception If any error occurs.
	 */
	public int executeUpdate(String sql, Object... parameters) throws Exception {
		return executeUpdate(getConnection(), sql, parameters);
	}
}