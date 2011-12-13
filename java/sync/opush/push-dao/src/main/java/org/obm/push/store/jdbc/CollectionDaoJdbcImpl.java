package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.calendar.EventType;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CollectionDaoJdbcImpl extends AbstractJdbcImpl implements CollectionDao {
	
	@Inject
	protected CollectionDaoJdbcImpl(IDBCP dbcp) {
		super(dbcp);
	}

	@Override
	public Integer addCollectionMapping(Device device, String collection) throws DaoException {
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
			JDBCUtils.cleanup(con, ps, null);
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
			ps.executeUpdate();

			ps = con.prepareStatement("DELETE FROM opush_sync_mail WHERE device_id=? AND collection_id=?");
			ps.setInt(1, devDbId);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ps = con.prepareStatement("DELETE FROM opush_sync_deleted_mail WHERE device_id=? AND collection_id=?");
			ps.setInt(1, devDbId);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			logger.warn("mappings & states cleared for sync of collection {} of device {}",
					new Object[]{collectionId, device.getDevId()});
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
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
			JDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException("Collection with id["
					+ collectionId + "] can not be found.");
		}
		return ret;
	}

	private String getCollectionPath(Integer collectionId, Connection con)
			throws CollectionNotFoundException, DaoException {
		String ret = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement("SELECT collection FROM opush_folder_mapping WHERE id=?");
			ps.setInt(1, collectionId);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getString(1);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
		}
		return ret;
	}
	
	@Override
	public int updateState(Device device, Integer collectionId, SyncState state) throws DaoException {
		final Integer devDbId = device.getDatabaseId();
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, devDbId);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setInt(4, collectionId);
			if (ps.executeUpdate() == 0) {
				throw new DaoException("No SyncState inserted");
			} else {
				return dbcp.lastInsertId(con);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public SyncState findStateForKey(String syncKey) throws DaoException, CollectionNotFoundException {
		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(
					"SELECT id, device_id, last_sync, collection_id " +
					"FROM opush_sync_state WHERE sync_key=?");
			ps.setString(1, syncKey);

			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp lastSync = rs.getTimestamp("last_sync");
				ret = new SyncState(getCollectionPath(
						rs.getInt("collection_id"), con));
				ret.setId(rs.getInt("id"));
				ret.setKey(syncKey);
				ret.setLastSync(lastSync);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}
	
	public int getCollectionMapping(Device device,
			String collection) throws CollectionNotFoundException, DaoException {
		final Integer devDbId = device.getDatabaseId();
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT id FROM opush_folder_mapping WHERE device_id=? AND collection=?");
			ps.setInt(1, devDbId);
			ps.setString(2, collection);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
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
			JDBCUtils.cleanup(con, ps, rs);
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
			JDBCUtils.cleanup(con, ps, rs);
		}
	}
	
	private ChangedCollections getCalendarChangedCollectionsFromResultSet(ResultSet rs,
			Date lastSync) throws SQLException {
		Set<SyncCollection> changed = Sets.newHashSet();
		Date dbDate = lastSync;
		while (rs.next()) {
			final String email = getEmail(rs);
			dbDate = new Date(rs.getTimestamp(3).getTime());
			EventType type = EventType.valueOf(rs.getString(4));
			
			StringBuilder colPath = getBaseCollectionPath(email);
			if (EventType.VTODO.equals(type)) {
				colPath.append("tasks\\");
			} else {
				colPath.append("calendar\\");
			}
			colPath.append(email);
			
			SyncCollection path = getSyncCollection(colPath.toString());
			changed.add(path);
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
		Set<SyncCollection> changed = Sets.newHashSet();
		Date dbDate = lastSync;
		while (rs.next()) {
			final String email = getEmail(rs);
			dbDate = new Date(rs.getTimestamp(3).getTime());
			
			StringBuilder colPath = getBaseCollectionPath(email);
			colPath.append("contacts");
			
			SyncCollection path = getSyncCollection(colPath.toString());
			changed.add(path);
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
	
	private SyncCollection getSyncCollection(String collectionPath){
		return new SyncCollection(0, collectionPath);
	}

}
