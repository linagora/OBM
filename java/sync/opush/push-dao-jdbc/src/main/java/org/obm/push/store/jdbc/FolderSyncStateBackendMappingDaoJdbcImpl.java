/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.obm.breakdownduration.bean.Watch;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.BreakdownGroups;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.utils.DateUtils;
import org.obm.sync.date.DateProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Watch(BreakdownGroups.SQL)
public class FolderSyncStateBackendMappingDaoJdbcImpl extends AbstractJdbcImpl implements FolderSyncStateBackendMappingDao {

	private final DateProvider dateProvider;

	@Inject
	/* allow cglib proxy */ FolderSyncStateBackendMappingDaoJdbcImpl(DatabaseConnectionProvider dbcp, DateProvider dateProvider) {
		super(dbcp);
		this.dateProvider = dateProvider;
	}

	@Override
	public Date getLastSyncDate(PIMDataType dataType, FolderSyncState folderSyncState)	throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"SELECT MAX(last_sync) FROM opush_folder_sync_state_backend_mapping " +
					"INNER JOIN opush_folder_sync_state ON opush_folder_sync_state.id = folder_sync_state_id " +
					"WHERE data_type = ? " + 
					"AND opush_folder_sync_state.sync_key = ?"); 
			ps.setObject(1, dbcp.getJdbcObject(dataType.getDbFieldName(), dataType.getDbValue()));
			ps.setString(2, folderSyncState.getSyncKey().getSyncKey());

			rs = ps.executeQuery();
			if (rs.next()) {
				return OpushJDBCUtils.getDate(rs, 1);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	@Override
	public void createMapping(PIMDataType dataType, FolderSyncState folderSyncState) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_folder_sync_state_backend_mapping " + 
						"(data_type, folder_sync_state_id, last_sync) VALUES (?, ?, ?)");
			ps.setObject(1, dbcp.getJdbcObject(dataType.getDbFieldName(), dataType.getDbValue()));
			ps.setInt(2, folderSyncState.getId());
 			ps.setTimestamp(3, DateUtils.toTimestamp(dateProvider.getDate()));
			if (ps.executeUpdate() == 0) {
				throw new DaoException("No SyncState inserted");
			} 
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
	}
}
