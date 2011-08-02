package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class CollectionDaoJdbcImpl extends AbstractJdbcImpl implements
		CollectionDao {
	
	private final DeviceDao deviceDao;

	@Inject
	protected CollectionDaoJdbcImpl(IDBCP dbcp, DeviceDao deviceDao) {
		super(dbcp);
		this.deviceDao = deviceDao;
	}

	public Integer addCollectionMapping(String loginAtDomain, String deviceId, String collection) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
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
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}
	
	@Override
	public void resetCollection(String loginAtDomain, String deviceId, Integer collectionId) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("DELETE FROM opush_sync_state WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ps = con.prepareStatement("DELETE FROM opush_sync_mail WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			ps = con.prepareStatement("DELETE FROM opush_sync_deleted_mail WHERE device_id=? AND collection_id=?");
			ps.setInt(1, id);
			ps.setInt(2, collectionId);
			ps.executeUpdate();

			logger.warn("mappings & states cleared for sync of collection "
					+ collectionId + " of device " + deviceId);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException {
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
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
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
			throws CollectionNotFoundException {
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
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
		}
		return ret;
	}
	
	@Override
	public void updateState(String loginAtDomain, String deviceId, Integer collectionId,
			SyncState state) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, id);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setInt(4, collectionId);
			ps.executeUpdate();
			logger.info("UpdateState [ " + collectionId + ", "
					+ state.getKey() + ", " + state.getLastSync().toString()
					+ " ]");
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public SyncState findStateForKey(String syncKey) {

		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT device_id, last_sync, collection_id FROM opush_sync_state WHERE sync_key=?");
			ps.setString(1, syncKey);

			rs = ps.executeQuery();
			if (rs.next()) {
				Timestamp lastSync = rs.getTimestamp("last_sync");
				ret = new SyncState(getCollectionPath(
						rs.getInt("collection_id"), con));
				ret.setKey(syncKey);
				ret.setLastSync(lastSync);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}
	
	public int getCollectionMapping(String loginAtDomain, String deviceId,
			String collection) throws CollectionNotFoundException, SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT id FROM opush_folder_mapping WHERE device_id=? AND collection=?");
			ps.setInt(1, id);
			ps.setString(2, collection);
			rs = ps.executeQuery();

			if (rs.next()) {
				ret = rs.getInt(1);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		if (ret == null) {
			throw new CollectionNotFoundException();
		}
		return ret;
	}



}
