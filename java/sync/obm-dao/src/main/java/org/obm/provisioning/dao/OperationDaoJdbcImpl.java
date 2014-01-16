/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.provisioning.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.OperationNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OperationDaoJdbcImpl implements OperationDao {

	private static final String FIELDS = 
			"o.id," +
			"o.status, " +
			"o.entity_type, " +
			"o.timecreate, " +
			"o.timecommit, " +
			"o.error, " +
			"o.resource_path, " +
			"o.body, " +
			"o.verb, " +
			"p.param_key, " +
			"p.value";

	private final DatabaseConnectionProvider dbcp;
	private final ObmHelper obmHelper;

	@Inject
	private OperationDaoJdbcImpl(DatabaseConnectionProvider dbcp, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.obmHelper = obmHelper;
	}

	@Override
	public Operation get(Operation.Id operationId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM batch_operation o " +
					"LEFT JOIN batch_operation_param p ON p.operation = o.id " +
					"WHERE o.id = ? " +
					"ORDER BY o.id ASC");

			ps.setInt(1, operationId.getId());

			rs = ps.executeQuery();

			return Iterables.getFirst(operationsFromCursor(rs), null);
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}
	}

	@Override
	public List<Operation> getByBatchId(Batch.Id batchId) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM batch_operation o " +
					"LEFT JOIN batch_operation_param p ON p.operation = o.id " +
					"WHERE o.batch = ? " +
					"ORDER BY o.id ASC");

			ps.setInt(1, batchId.getId());

			rs = ps.executeQuery();

			return operationsFromCursor(rs);
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}
	}

	@Override
	public Operation create(Batch batch, Operation operation) throws DaoException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("INSERT INTO batch_operation (status, resource_path, body, verb, entity_type, batch) VALUES (?, ?, ?, ?, ?, ?)");

			ps.setObject(1, dbcp.getJdbcObject(ObmHelper.BATCH_STATUS, operation.getStatus().toString()));
			ps.setString(2, operation.getRequest().getResourcePath());
			ps.setString(3, operation.getRequest().getBody());
			ps.setObject(4, dbcp.getJdbcObject(ObmHelper.HTTP_VERB, operation.getRequest().getVerb().toString()));
			ps.setObject(5, dbcp.getJdbcObject(ObmHelper.BATCH_ENTITY_TYPE, operation.getEntityType().toString()));
			ps.setInt(6, batch.getId().getId());

			ps.executeUpdate();

			int operationId = obmHelper.lastInsertId(connection);

			insertOperationParameters(operation, operationId);

			return get(Operation.Id.builder().id(operationId).build());
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	@Override
	public Operation update(Operation operation) throws DaoException, OperationNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("UPDATE batch_operation SET status = ?, timecommit = ?, error = ? WHERE id = ?");

			ps.setObject(1, dbcp.getJdbcObject(ObmHelper.BATCH_STATUS, operation.getStatus().toString()));

			Date timecommit = operation.getTimecommit();

			if (timecommit == null) {
				ps.setNull(2, Types.DATE);
			} else {
				ps.setTimestamp(2, new Timestamp(timecommit.getTime()));
			}
			ps.setString(3, operation.getError());
			ps.setInt(4, operation.getId().getId());

			if (ps.executeUpdate() != 1) {
				throw new OperationNotFoundException(String.format("Operation %s not found", operation.getId()));
			}

			return get(operation.getId());
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	private void insertOperationParameters(Operation operation, int operationId) throws DaoException {
		Connection connection = null;
		PreparedStatement psParams = null;

		try {
			connection = dbcp.getConnection();
			psParams = connection.prepareStatement("INSERT INTO batch_operation_param (param_key, value, operation) VALUES (?, ?, ?)");

			for (Map.Entry<String, String> param : operation.getRequest().getParams().entrySet()) {
				psParams.setString(1, param.getKey());
				psParams.setString(2, param.getValue());
				psParams.setInt(3, operationId);

				psParams.addBatch();
			}

			psParams.executeBatch();
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, psParams, null);
		}
	}

	private List<Operation> operationsFromCursor(ResultSet rs) throws SQLException {
		int operationId = -1;
		Operation.Builder operationBuilder = null;
		Request.Builder requestBuilder = null;
		List<Operation> operations = Lists.newArrayList();

		while (rs.next()) {
			int id = rs.getInt("id");

			if (id != operationId) {
				if (operationBuilder != null && requestBuilder != null) {
					operations.add(operationBuilder
							.request(requestBuilder.build())
							.build());
				}

				operationId = id;
				operationBuilder = operationBuilderFromCursor(rs);
				requestBuilder = requestBuilderFromCursor(rs);
			} else {
				String key = rs.getString("param_key");

				if (requestBuilder != null && key != null) {
					requestBuilder.param(key, rs.getString("value"));
				}
			}
		}

		if (operationBuilder != null && requestBuilder != null) {
			operations.add(operationBuilder
					.request(requestBuilder.build())
					.build());
		}

		return operations;
	}

	private Operation.Builder operationBuilderFromCursor(ResultSet rs) throws SQLException {
		return Operation.builder()
				.id(Operation.Id.builder().id(rs.getInt("id")).build())
				.status(BatchStatus.valueOf(rs.getString("status")))
				.entityType(BatchEntityType.valueOf(rs.getString("entity_type")))
				.timecreate(JDBCUtils.getDate(rs, "timecreate"))
				.timecommit(JDBCUtils.getDate(rs, "timecommit"))
				.error(rs.getString("error"));
	}

	private Request.Builder requestBuilderFromCursor(ResultSet rs) throws SQLException {
		String key = rs.getString("param_key");
		Request.Builder builder =  Request.builder()
				.resourcePath(rs.getString("resource_path"))
				.body(rs.getString("body"))
				.verb(HttpVerb.valueOf(rs.getString("verb")));

		if (key != null) {
			builder.param(key, rs.getString("value"));
		}

		return builder;
	}

}
