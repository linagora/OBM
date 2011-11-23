package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ItemTrackingDaoJdbcImpl extends AbstractJdbcImpl implements ItemTrackingDao {
	
	@Inject
	private ItemTrackingDaoJdbcImpl(IDBCP dbcp) {
		super(dbcp);
	}
	
	@Override
	public void markAsSynced(SyncState syncState, Set<ServerId> serverIds) throws DaoException {
		markItems(syncState, serverIds, true);
	}


	@Override
	public void markAsDeleted(SyncState syncState, Set<ServerId> serverIds)
			throws DaoException {
		markItems(syncState, serverIds, false);
	}
	
	private void markItems(SyncState syncState, Set<ServerId> serverIds, boolean addition)
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
				insert.setInt(1, syncState.getId());
				insert.setInt(2, serverId.getItemId());
				insert.setBoolean(3, addition);
				insert.addBatch();
			}
			insert.executeBatch();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	private void checkServerId(ServerId serverId) {
		Integer itemId = serverId.getItemId();
		if (itemId == null) {
			throw new IllegalArgumentException("serverId must reference an item");
		}
	}

	@Override
	public Set<ServerId> getSyncedServerIds(final SyncState syncState, Set<ServerId> serverIds) throws DaoException {
		HashSet<ServerId> filteredSet = new HashSet<ServerId>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			
			PreparedStatement select = selectServerId(con);
		
			for (ServerId serverId: serverIds) {
				checkServerId(serverId);
				if (isServerIdSynced(select, syncState, serverId)) {
					filteredSet.add(serverId);
				}
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return filteredSet;
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
	public boolean isServerIdSynced(SyncState syncState, ServerId serverId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			return isServerIdSynced(selectServerId(con), syncState, serverId);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	private boolean isServerIdSynced(PreparedStatement select, SyncState syncState, ServerId serverId) throws SQLException {
		
		select.setInt(1, serverId.getItemId());
		select.setInt(2, syncState.getId());
		ResultSet resultSet = select.executeQuery();
		if (resultSet.next()) {
			return resultSet.getBoolean("addition");
		} else {
			return false;
		}
	}
}
