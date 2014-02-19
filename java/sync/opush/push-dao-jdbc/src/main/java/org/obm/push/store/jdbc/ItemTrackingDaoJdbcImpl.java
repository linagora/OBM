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
import java.util.Set;

import org.obm.breakdownduration.bean.Watch;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.BreakdownGroups;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.ServerId;
import org.obm.push.exception.DaoException;
import org.obm.push.store.ItemTrackingDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Watch(BreakdownGroups.SQL)
public class ItemTrackingDaoJdbcImpl extends AbstractJdbcImpl implements ItemTrackingDao {
	
	@Inject
	/* allow cglib proxy */ ItemTrackingDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		super(dbcp);
	}
	
	@Override
	public void markAsSynced(ItemSyncState itemSyncState, Set<ServerId> serverIds) throws DaoException {
		markItems(itemSyncState, serverIds, true);
	}


	@Override
	public void markAsDeleted(ItemSyncState itemSyncState, Set<ServerId> serverIds)
			throws DaoException {
		markItems(itemSyncState, serverIds, false);
	}
	
	private void markItems(ItemSyncState itemSyncState, Set<ServerId> serverIds, boolean addition)
			throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			
			PreparedStatement insert = con.prepareStatement(
					"INSERT INTO opush_synced_item (sync_state_id, item_id, addition) VALUES (?, ?, ?)");
			for (ServerId serverId: serverIds) {
				checkServerId(serverId);
				insert.setInt(1, itemSyncState.getId());
				insert.setInt(2, serverId.getItemId());
				insert.setBoolean(3, addition);
				insert.addBatch();
			}
			insert.executeBatch();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}

	private void checkServerId(ServerId serverId) {
		Integer itemId = serverId.getItemId();
		if (itemId == null) {
			throw new IllegalArgumentException("serverId must reference an item");
		}
	}


	private PreparedStatement selectServerId(Connection con)
			throws SQLException {
		PreparedStatement select = con.prepareStatement(
				"SELECT item.addition FROM opush_sync_state " +
				"INNER JOIN opush_sync_state AS states ON " +
				"(states.last_sync <= opush_sync_state.last_sync " +
				"AND states.collection_id = opush_sync_state.collection_id " +
				"AND states.device_id = opush_sync_state.device_id) " +
				"INNER JOIN opush_synced_item AS item ON (states.id = item.sync_state_id) " +
				"WHERE item.item_id = ? " +
				"AND states.id = item.sync_state_id " +
				"AND opush_sync_state.id = ? " +
				"ORDER BY (states.last_sync) DESC LIMIT 1");
		return select;
	}
	
	@Override
	public boolean isServerIdSynced(ItemSyncState itemSyncState, ServerId serverId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			return isServerIdSynced(selectServerId(con), itemSyncState, serverId);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	private boolean isServerIdSynced(PreparedStatement select, ItemSyncState itemSyncState, ServerId serverId) throws SQLException {
		
		select.setInt(1, serverId.getItemId());
		select.setInt(2, itemSyncState.getId());
		ResultSet resultSet = select.executeQuery();
		try {
			if (resultSet.next()) {
				return resultSet.getBoolean("addition");
			} else {
				return false;
			}
		}
		finally {
			resultSet.close();
		}
	}
}
