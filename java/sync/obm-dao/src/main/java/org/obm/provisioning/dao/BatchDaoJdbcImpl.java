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

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.DomainDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.push.utils.JDBCUtils;
import org.obm.utils.ObmHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class BatchDaoJdbcImpl implements BatchDao {

	public static final String FIELDS = "id, status, timecreate, timecommit, domain_uuid";

	private final DatabaseConnectionProvider dbcp;
	private final OperationDao operationDao;
	private final DomainDao domainDao;
	private final ObmHelper obmHelper;

	@Inject
	private BatchDaoJdbcImpl(DatabaseConnectionProvider dbcp, OperationDao operationDao, DomainDao domainDao, ObmHelper obmHelper) {
		this.dbcp = dbcp;
		this.operationDao = operationDao;
		this.domainDao = domainDao;
		this.obmHelper = obmHelper;
	}

	@Override
	public Batch get(Batch.Id id, ObmDomain domain) throws DaoException, BatchNotFoundException, DomainNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement(
					"SELECT " + FIELDS + " FROM batch " +
					"INNER JOIN Domain ON domain_id = domain " +
					"WHERE id = ? AND domain_id = ?");

			ps.setInt(1, id.getId());
			ps.setInt(2, domain.getId());

			rs = ps.executeQuery();

			if (rs.next()) {
				return batchFromCursor(rs);
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, rs);
		}

		throw new BatchNotFoundException(id);
	}

	@Override
	public Batch create(Batch batch) throws DaoException, BatchNotFoundException, DomainNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("INSERT INTO batch (status, domain) VALUES (?, ?)");

			ps.setObject(1, dbcp.getJdbcObject(ObmHelper.BATCH_STATUS, batch.getStatus().toString()));
			ps.setInt(2, batch.getDomain().getId());

			ps.executeUpdate();

			return get(Batch.Id.builder().id(obmHelper.lastInsertId(connection)).build(), batch.getDomain());
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	@Override
	public Batch update(Batch batch) throws DaoException, BatchNotFoundException, DomainNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("UPDATE batch SET status = ?, timecommit = ? WHERE id = ?");

			ps.setObject(1, dbcp.getJdbcObject(ObmHelper.BATCH_STATUS, batch.getStatus().toString()));

			Date timecommit = batch.getTimecommit();

			if (timecommit == null) {
				ps.setNull(2, Types.DATE);
			} else {
				ps.setTimestamp(2, new Timestamp(timecommit.getTime()));
			}
			ps.setInt(3, batch.getId().getId());

			int updateCount = ps.executeUpdate();
			
			if (updateCount != 1) {
				throw new BatchNotFoundException(String.format("No such batch: %s", batch.getId()));
			}

			for (Operation operation : batch.getOperations()) {
				try {
					operationDao.update(operation);
				}
				catch (Exception e) {
					throw new DaoException(e); // This should never happen...
				}
			}

			return get(batch.getId(), batch.getDomain());
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}
	}

	@Override
	public void delete(Batch.Id id) throws DaoException, BatchNotFoundException {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = dbcp.getConnection();
			ps = connection.prepareStatement("DELETE FROM batch WHERE id = ?");

			ps.setInt(1, id.getId());

			int updateCount = ps.executeUpdate();
			
			if (updateCount != 1) {
				throw new BatchNotFoundException(String.format("No such batch: %s", id));
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			JDBCUtils.cleanup(connection, ps, null);
		}	
	}

	@Override
	public Batch addOperation(Batch batch, Operation operation) throws DaoException, BatchNotFoundException, DomainNotFoundException {
		operationDao.create(batch, operation);

		return get(batch.getId(), batch.getDomain());
	}

	private Batch batchFromCursor(ResultSet rs) throws DaoException, SQLException, DomainNotFoundException {
		Batch.Id batchId = Batch.Id.builder().id(rs.getInt("id")).build();

		return Batch.builder()
				.id(batchId)
				.status(BatchStatus.valueOf(rs.getString("status")))
				.domain(domainDao.findDomainByUuid(ObmDomainUuid.of(rs.getString("domain_uuid"))))
				.timecreate(JDBCUtils.getDate(rs, "timecreate"))
				.timecommit(JDBCUtils.getDate(rs, "timecommit"))
				.operations(operationDao.getByBatchId(batchId))
				.build();
	}

}
