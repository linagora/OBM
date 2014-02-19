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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.obm.breakdownduration.bean.Watch;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.BreakdownGroups;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.sync.calendar.EventType;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@Watch(BreakdownGroups.SQL)
public class CollectionDaoJdbcImpl extends AbstractJdbcImpl implements CollectionDao {

	private static final String SYNC_STATE_ITEM_TABLE = "opush_sync_state";
	private static final String SYNC_STATE_FOLDER_TABLE = "opush_folder_sync_state";
	private static final String SYNC_STATE_FIELDS = 
			Joiner.on(',').join("id", "last_sync", "sync_key");
	private static final String SYNC_STATE_FOLDER_FIELDS = 
			Joiner.on(',').join("id", "sync_key");
	
	@Inject
	/* allow cglib proxy */ CollectionDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		super(dbcp);
	}

	@Override
	public List<String> getUserCollections(FolderSyncState folderSyncState) throws DaoException {
		List<String> userCollections = Lists.newArrayList();
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT collection FROM opush_folder_mapping " +
					"INNER JOIN opush_folder_snapshot ON opush_folder_snapshot.collection_id = opush_folder_mapping.id " +
					"INNER JOIN opush_folder_sync_state ON opush_folder_sync_state.id = opush_folder_snapshot.folder_sync_state_id " +
					"WHERE opush_folder_sync_state.sync_key = ?");
			ps.setString(1, folderSyncState.getSyncKey().getSyncKey());
			ResultSet resultSet = ps.executeQuery();

			while (resultSet.next()) {
				userCollections.add(resultSet.getString("collection"));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
		return userCollections;
	}

 	@Override
	public int addCollectionMapping(Device device, String collection) throws DaoException {
		Integer id = device.getDatabaseId();
		Integer ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		try{
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_folder_mapping (device_id, collection) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setString(2, collection);
			ps.executeUpdate();
			ret = dbcp.lastInsertId(con);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}
	
	@Override
	public void resetCollection(Device device, Integer collectionId) throws DaoException {
		final Integer devDbId = device.getDatabaseId();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			ps.setInt(1, devDbId);
			ps.setInt(2, collectionId);
			Stopwatch stopwatch = Stopwatch.createStarted();
			ps.executeUpdate();

			logger.warn("mappings & states cleared for sync of collection {} of device {}",
					collectionId, device.getDevId());
			logger.warn("Deletion time: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException, DaoException {
		String ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT collection FROM opush_folder_mapping WHERE id=?");
			ps.setInt(1, collectionId);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException("Collection with id["
					+ collectionId + "] can not be found.");
		}
		return ret;
	}

	@Override
	public ItemSyncState updateState(Device device, Integer collectionId, SyncKey syncKey, Date syncDate) throws DaoException {
		final Integer devDbId = device.getDatabaseId();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, syncKey.getSyncKey());
			ps.setInt(2, devDbId);
			ps.setTimestamp(3, new Timestamp(syncDate.getTime()));
			ps.setInt(4, collectionId);
			if (ps.executeUpdate() == 0) {
				throw new DaoException("No SyncState inserted");
			} else {
				return ItemSyncState.builder()
						.syncDate(syncDate)
						.syncKey(syncKey)
						.id(dbcp.lastInsertId(con))
						.build();
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public FolderSyncState allocateNewFolderSyncState(Device device, SyncKey newSyncKey) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_folder_sync_state" +
					" (sync_key, device_id) VALUES (?, ?)");
			ps.setString(1, newSyncKey.getSyncKey());
			ps.setInt(2, device.getDatabaseId());
			ps.executeUpdate();
			FolderSyncState folderSyncState = FolderSyncState.builder()
					.syncKey(newSyncKey)
					.id(dbcp.lastInsertId(con))
					.build();
			return folderSyncState;
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public ItemSyncState findItemStateForKey(SyncKey syncKey) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"SELECT " + SYNC_STATE_FIELDS 
					+ " FROM " + SYNC_STATE_ITEM_TABLE + " WHERE sync_key=?");
			ps.setString(1, syncKey.getSyncKey());

			rs = ps.executeQuery();
			if (rs.next()) {
				return buildItemSyncState(rs);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	@Override
	public FolderSyncState findFolderStateForKey(SyncKey syncKey) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"SELECT " + SYNC_STATE_FOLDER_FIELDS 
					+ " FROM " + SYNC_STATE_FOLDER_TABLE + " WHERE sync_key=?");
			ps.setString(1, syncKey.getSyncKey());

			rs = ps.executeQuery();
			if (rs.next()) {
				SyncKey rsSyncKey = new SyncKey(rs.getString("sync_key"));
				FolderSyncState folderSyncState = FolderSyncState.builder()
						.syncKey(rsSyncKey)
						.id(rs.getInt("id"))
						.build();
				return folderSyncState;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}

	@Override
	public ItemSyncState lastKnownState(Device device, Integer collectionId) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"SELECT " + SYNC_STATE_FIELDS + " FROM opush_sync_state " +
					"WHERE device_id=? AND collection_id=? ORDER BY last_sync DESC LIMIT 1");
			ps.setInt(1, device.getDatabaseId());
			ps.setInt(2, collectionId);

			rs = ps.executeQuery();
			if (rs.next()) {
				return buildItemSyncState(rs);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	private ItemSyncState buildItemSyncState(ResultSet rs) throws SQLException {
		Date lastSync = OpushJDBCUtils.getDate(rs, "last_sync");
		SyncKey syncKey = new SyncKey(rs.getString("sync_key"));
		ItemSyncState syncState = ItemSyncState.builder()
				.syncKey(syncKey)
				.syncDate(lastSync)
				.id(rs.getInt("id"))
				.build();
		return syncState;
	}

	@Override
	public Integer getCollectionMapping(Device device, String collection) throws DaoException {
		Integer devDbId = device.getDatabaseId();
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT opush_folder_mapping.id FROM opush_folder_mapping " +
					"WHERE device_id = ? " +
					"AND collection = ?");
			ps.setInt(1, devDbId);
			ps.setString(2, collection);
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public ChangedCollections getCalendarChangedCollections(Date lastSync) throws DaoException {
		final String query = "select "
				+ "userobm_login, domain_name, now(), e.event_type as type "
				+ "from EventLink "
				+ "inner join UserEntity ON userentity_entity_id=eventlink_entity_id "
				+ "inner join UserObm on userobm_id=userentity_user_id "
				+ "inner join Domain on userobm_domain_id=domain_id "
				+ "inner join Event e on eventlink_event_id=e.event_id "
				+ "where eventlink_timeupdate >= ? OR eventlink_timecreate >= ? OR event_timeupdate >= ? OR event_timecreate >= ? "
				+ "UNION " + "select "
				+ "userobm_login, domain_name, now(), deletedevent_type as type "
				+ "from DeletedEvent "
				+ "inner join UserObm on deletedevent_user_id=userobm_id "
				+ "inner join Domain on userobm_domain_id=domain_id "
				+ "where deletedevent_timestamp >= ? ";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Timestamp ts  = getGMTTimestamp(lastSync);
		int idx = 1;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			return getCalendarChangedCollectionsFromResultSet(rs, lastSync);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	@Override
	public ChangedCollections getContactChangedCollections(Date lastSync) throws DaoException {
		String query = "select "
		+ "	distinct userobm_login, domain_name, now()"
		+ "	from SyncedAddressbook sa "
		+ " inner join UserObm on userobm_id=sa.user_id "
		+ " inner join Domain on userobm_domain_id=domain_id "
		+ "	inner join AddressBook ab on ab.id=sa.addressbook_id "
		+ "	where ab.timeupdate >= ? or ab.timecreate >= ? or sa.timestamp >= ? ";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Timestamp ts  = getGMTTimestamp(lastSync);
		
		int idx = 1;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			ps.setTimestamp(idx++, ts);
			rs = ps.executeQuery();
			return getContactChangedCollectionsFromResultSet(rs, lastSync);
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			OpushJDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	private ChangedCollections getCalendarChangedCollectionsFromResultSet(ResultSet rs,
			Date lastSync) throws SQLException {
		Set<String> changed = Sets.newHashSet();
		Date dbDate = lastSync;
		while (rs.next()) {
			final String email = getEmail(rs);
			dbDate = OpushJDBCUtils.getDate(rs, rs.getMetaData().getColumnName(3));
			EventType type = EventType.valueOf(rs.getString(4));
			
			StringBuilder colPath = getBaseCollectionPath(email);
			if (EventType.VTODO.equals(type)) {
				colPath.append("tasks\\");
			} else {
				colPath.append("calendar\\");
			}
			colPath.append(email);
			
			changed.add(colPath.toString());
		}
		return new ChangedCollections(dbDate, changed);
	}
	
	private Timestamp getGMTTimestamp(Date lastSync) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(lastSync.getTime());
		return new Timestamp(cal.getTimeInMillis());
	}
	
	private ChangedCollections getContactChangedCollectionsFromResultSet(ResultSet rs,
			Date lastSync) throws SQLException {
		Set<String> changed = Sets.newHashSet();
		Date dbDate = lastSync;
		while (rs.next()) {
			final String email = getEmail(rs);
			dbDate = OpushJDBCUtils.getDate(rs, rs.getMetaData().getColumnName(3));
			
			StringBuilder colPath = getBaseCollectionPath(email);
			colPath.append("contacts");
			
			changed.add(colPath.toString());
		}
		return new ChangedCollections(dbDate, changed);
	}
	
	private String getEmail(ResultSet rs) throws SQLException {
		String login = rs.getString("userobm_login");
		String domain = rs.getString("domain_name");
		return login + "@" + domain;
	}
	
	private StringBuilder getBaseCollectionPath(String email){
		StringBuilder colName = new StringBuilder();
		colName.append("obm:\\\\");
		colName.append(email);
		colName.append("\\");
		return colName;
	}	
}
