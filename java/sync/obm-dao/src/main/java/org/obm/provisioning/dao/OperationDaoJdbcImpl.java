/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
import org.obm.provisioning.dao.exceptions.OperationNotFoundException;
import org.obm.push.utils.JDBCUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OperationDaoJdbcImpl implements OperationDao {

	private DatabaseConnectionProvider dbcp;

	@Inject
	private OperationDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}

	@Override
	public Operation get(Integer operationId) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("SELECT * FROM batch_operation WHERE id = ?");

			ps.setInt(1, operationId);

			rs = ps.executeQuery();

			if (rs.next()) {
				return operationFromCursor(rs);
			}
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}

		return null;
	}

	@Override
	public List<Operation> getByBatchId(Integer batchId) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<Operation> operations = Lists.newArrayList();

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("SELECT * FROM batch_operation WHERE batch = ? ORDER BY id ASC");

			ps.setInt(1, batchId);

			rs = ps.executeQuery();

			while (rs.next()) {
				operations.add(operationFromCursor(rs));
			}
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}

		return operations;
	}

	@Override
	public Operation create(Batch batch, Operation operation) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("INSERT INTO batch_operation (status, url, body, verb, entity_type, batch) VALUES (?, ?, ?, ?, ?, ?)");

			ps.setString(1, operation.getStatus().toString());
			ps.setString(2, operation.getRequest().getUrl());
			ps.setString(3, operation.getRequest().getBody());
			ps.setString(4, operation.getRequest().getVerb().toString());
			ps.setString(5, operation.getEntityType().toString());
			ps.setInt(6, batch.getId());

			ps.executeUpdate();

			int operationId = JDBCUtils.lastInsertId(connection);

			insertOperationParameters(operation, operationId);

			return get(operationId);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	@Override
	public Operation update(Operation operation) throws SQLException, OperationNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("UPDATE batch_operation SET status = ?, timecommit = ?, error = ? WHERE id = ?");

			ps.setString(1, operation.getStatus().toString());

			Date timecommit = operation.getTimecommit();

			if (timecommit == null) {
				ps.setNull(2, Types.DATE);
			} else {
				ps.setTimestamp(2, new Timestamp(timecommit.getTime()));
			}
			ps.setString(3, operation.getError());
			ps.setInt(4, operation.getId());

			if (ps.executeUpdate() != 1) {
				throw new OperationNotFoundException(String.format("Operation %d not found", operation.getId()));
			}

			return get(operation.getId());
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	private void insertOperationParameters(Operation operation, int operationId) throws SQLException {
		Connection connection = null;
		PreparedStatement psParams = null;

		try {
			connection = dbcp.getConnection();
			psParams = connection.prepareStatement("INSERT INTO batch_operation_param (key, value, operation) VALUES (?, ?, ?)");

			for (Map.Entry<String, String> param : operation.getRequest().getParams().entrySet()) {
				psParams.setString(1, param.getKey());
				psParams.setString(2, param.getValue());
				psParams.setInt(3, operationId);

				psParams.addBatch();
			}

			psParams.executeBatch();
		}
		finally {
			JDBCUtils.cleanup(connection, psParams, null);
		}
	}

	private Operation operationFromCursor(ResultSet rs) throws SQLException {
		int operationId = rs.getInt("id");

		Request.Builder requestBuilder = Request.builder()
				.url(rs.getString("url"))
				.body(rs.getString("body"))
				.verb(HttpVerb.valueOf(rs.getString("verb")));

		return Operation.builder()
				.id(operationId)
				.status(BatchStatus.valueOf(rs.getString("status")))
				.entityType(BatchEntityType.valueOf(rs.getString("entity_type")))
				.timecreate(JDBCUtils.getDate(rs, "timecreate"))
				.timecommit(JDBCUtils.getDate(rs, "timecommit"))
				.error(rs.getString("error"))
				.request(fetchOperationParameters(operationId, requestBuilder).build())
				.build();
	}

	private Request.Builder fetchOperationParameters(Integer operationId, Request.Builder requestBuilder) throws SQLException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("SELECT key, value FROM batch_operation_param WHERE operation = ?");

			ps.setInt(1, operationId);

			rs = ps.executeQuery();

			while (rs.next()) {
				requestBuilder.param(rs.getString("key"), rs.getString("value"));
			}
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}

		return requestBuilder;
	}

}
