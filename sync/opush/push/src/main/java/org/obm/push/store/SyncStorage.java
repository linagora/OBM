package org.obm.push.store;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.TransactionManager;

import org.obm.dbcp.DBCP;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.IniFile;
import org.obm.push.utils.JDBCUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Store device infos, id mappings & last sync dates into OBM database
 */
@Singleton
public class SyncStorage implements ISyncStorage {

	private static final Logger logger = LoggerFactory.getLogger(SyncStorage.class);

	private final DBCP dbcp;

	private final DeviceDao deviceDao;
	
	@Inject
	private SyncStorage(DBCP dbcp, DeviceDao dao) {
		this.dbcp = dbcp;
		this.deviceDao = dao;
	}
	

	@Override
	public SyncState findStateForKey(String syncKey) {

		SyncState ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getDataSource().getConnection();
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

	@Override
	public long findLastHearbeat(String loginAtDomain, String devId) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT last_heartbeat FROM opush_ping_heartbeat WHERE device_id=?");
			ps.setInt(1, id);

			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong("last_heartbeat");
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return 0L;
	}

	@Override
	public synchronized void updateLastHearbeat(String loginAtDomain, String devId, long hearbeat) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		Connection con = null;
		PreparedStatement ps = null;
		TransactionManager tm = dbcp.getDataSource().getTransactionManager();
		try {
			tm.begin();
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("DELETE FROM opush_ping_heartbeat WHERE device_id=? ");
			ps.setInt(1, id);
			ps.executeUpdate();

			ps.close();
			ps = con.prepareStatement("INSERT INTO opush_ping_heartbeat (device_id, last_heartbeat) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setLong(2, hearbeat);
			ps.executeUpdate();
			tm.commit();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			rollback(tm);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public boolean initDevice(String loginAtDomain, String deviceId, String deviceType) {

		boolean ret = true;
		TransactionManager ut = dbcp.getDataSource().getTransactionManager();
		try {
			ut.begin();
			Integer opushDeviceId = deviceDao.findDevice(loginAtDomain, deviceId);
			if (opushDeviceId == null) {
				boolean registered = deviceDao.registerNewDevice(loginAtDomain, deviceId, deviceType);
				if (!registered) {
					logger.warn("did not insert any row in device table for device "
							+ deviceType + " of " + loginAtDomain);
					ret = false;
				}
			}
			ut.commit();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			rollback(ut);
			ret = false;
		}
		return ret;
	}

	public boolean syncAuthorized(String loginAtDomain, String deviceId) {
		IniFile ini = new IniFile("/etc/opush/sync_perms.ini") {
			@Override
			public String getCategory() {
				return null;
			}
		};

		if (userIsBlacklisted(loginAtDomain, ini)) {
			return false;
		}

		String syncperm = ini.getData().get("allow.unknown.pda");

		if ("true".equals(syncperm)) {
			return true;
		}

		String[] parts = loginAtDomain.split("@");
		String login = parts[0].toLowerCase();
		String domain = parts[1].toLowerCase();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean hasSyncPerm = false;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT policy FROM opush_sync_perms "
					+ "INNER JOIN UserObm u ON owner=userobm_id "
					+ "INNER JOIN Domain d ON userobm_domain_id=domain_id "
					+ "INNER JOIN opush_device od ON device_id=id "
					+ "WHERE od.identifier=? AND lower(u.userobm_login)=? AND lower(d.domain_name)=?");
			ps.setString(1, deviceId);
			ps.setString(2, login);
			ps.setString(3, domain);

			rs = ps.executeQuery();
			if (rs.next()) {
				hasSyncPerm = true;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		if (!hasSyncPerm) {
			logger.info(loginAtDomain
					+ " isn't authorized to synchronize in OBM-UI");
		}
		return hasSyncPerm;
	}

	private boolean userIsBlacklisted(String loginAtDomain, IniFile ini) {
		String blacklist = Strings.nullToEmpty(ini.getData().get(
				"blacklist.users"));
		Iterable<String> users = Splitter.on(',').trimResults()
				.split(blacklist);
		for (String user : users) {
			if (user.equalsIgnoreCase(loginAtDomain)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void updateState(String loginAtDomain, String devId, Integer collectionId,
			SyncState state) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);
		Connection con = null;
		PreparedStatement ps = null;
		TransactionManager ut = dbcp.getDataSource().getTransactionManager();
		try {
			ut.begin();
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_state (sync_key, device_id, last_sync, collection_id) VALUES (?, ?, ?, ?)");
			ps.setString(1, state.getKey());
			ps.setInt(2, id);
			ps.setTimestamp(3, new Timestamp(state.getLastSync().getTime()));
			ps.setInt(4, collectionId);
			ps.executeUpdate();
			ut.commit();
			logger.info("UpdateState [ " + devId + ", " + collectionId + ", "
					+ state.getKey() + ", " + state.getLastSync().toString()
					+ " ]");
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			rollback(ut);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public int getCollectionMapping(String loginAtDomain, String deviceId, String collection)
			throws CollectionNotFoundException, SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
		Integer ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
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

	public Integer addCollectionMapping(String loginAtDomain, String deviceId, String collection) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, deviceId);
		Integer ret = null;
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("INSERT INTO opush_folder_mapping (device_id, collection) VALUES (?, ?)");
			ps.setInt(1, id);
			ps.setString(2, collection);
			ps.executeUpdate();
			ret = dbcp.lastInsertId(con);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
		return ret;
	}

	@Override
	public String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException {
		String ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = dbcp.getDataSource().getConnection();
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
	public PIMDataType getDataClass(String collectionPath) {
		if (collectionPath.contains("\\calendar\\")) {
			return PIMDataType.CALENDAR;
		} else if (collectionPath.contains("\\contacts")) {
			return PIMDataType.CONTACTS;
		} else if (collectionPath.contains("\\email\\")) {
			return PIMDataType.EMAIL;
		} else if (collectionPath.contains("\\tasks\\")) {
			return PIMDataType.TASKS;
		} else {
			return PIMDataType.FOLDER;
		}
	}

	@Override
	public synchronized void resetCollection(String loginAtDomain, String devId, Integer collectionId) throws SQLException {
		Integer id = deviceDao.findDevice(loginAtDomain, devId);

		Connection con = null;
		PreparedStatement ps = null;
		TransactionManager ut = dbcp.getDataSource().getTransactionManager();
		try {
			ut.begin();
			con = dbcp.getDataSource().getConnection();
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

			ut.commit();
			logger.warn("mappings & states cleared for sync of collection "
					+ collectionId + " of device " + devId);
		} catch (Throwable e) {
			rollback(ut);
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Boolean isMostRecentInvitation(Integer eventCollectionId,
			String eventUid, Date dtStamp) {

		Boolean ret = true;
		String calQuery = "SELECT * FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND event_uid=? AND dtstamp > ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setTimestamp(3, new Timestamp(dtStamp.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = false;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid) {
		return isInvitationExistsToStatus(eventCollectionId, eventUid,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	@Override
	public boolean haveEventToDeleted(Integer eventCollectionId, String eventUid) {
		return isInvitationExistsToStatus(eventCollectionId, eventUid,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private boolean isInvitationExistsToStatus(Integer eventCollectionId,
			String eventUid, InvitationStatus status) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		boolean isInvitationExists = false;
		String query = "SELECT status FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND event_uid=? AND status=?";

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(query);
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);
			ps.setString(i++, status.toString());

			rs = ps.executeQuery();
			if (rs.next()) {
				isInvitationExists = true;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return isInvitationExists;
	}

	@Override
	public int getCountEmailFilterChanges(Integer emailCollectionId,
			String syncKey) {
		int nb = 0;
		String calQuery = "SELECT event_uid "
				+ "FROM opush_invitation_mapping "
				+ "WHERE mail_collection_id=? AND event_collection_id=? "
				+ "AND ( status=? OR status=? OR ( ( status=? OR status=? ) AND sync_key=?) )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			int i = 1;
			ps.setInt(i++, emailCollectionId);
			ps.setNull(i++, Types.INTEGER);
			ps.setString(i++, InvitationStatus.EMAIL_TO_SYNCED.toString());
			ps.setString(i++, InvitationStatus.EMAIL_TO_DELETED.toString());
			ps.setString(i++, InvitationStatus.EMAIL_SYNCED.toString());
			ps.setString(i++, InvitationStatus.DELETED.toString());
			ps.setString(i++, syncKey);

			rs = ps.executeQuery();
			while (rs.next()) {
				nb++;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return nb;
	}

	@Override
	public int getCountEventFilterChanges(Integer eventCollectionId,
			String syncKey) {
		int nb = 0;
		String calQuery = "SELECT event_uid "
				+ "FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? "
				+ "AND ( status=? OR status=? OR status=? OR ( ( status=? OR status=? ) AND sync_key=?) )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, InvitationStatus.EVENT_TO_SYNCED.toString());
			ps.setString(i++, InvitationStatus.EVENT_MUST_SYNCED.toString());
			ps.setString(i++, InvitationStatus.EVENT_TO_DELETED.toString());
			ps.setString(i++, InvitationStatus.EVENT_SYNCED.toString());
			ps.setString(i++, InvitationStatus.DELETED.toString());
			ps.setString(i++, syncKey);

			rs = ps.executeQuery();
			while (rs.next()) {
				nb++;
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return nb;
	}

	@Override
	public void createOrUpdateInvitationEventAsMustSynced(
			Integer eventCollectionId, String eventUid, Date dtStamp) {
		createOrUpdateInvitationEvent(eventCollectionId, eventUid, dtStamp,
				InvitationStatus.EVENT_MUST_SYNCED);
	}

	@Override
	public void createOrUpdateInvitationEvent(Integer eventCollectionId,
			String uid, Date dtStamp, InvitationStatus status) {
		createOrUpdateInvitationEvent(eventCollectionId, uid, dtStamp, status,
				null);
	}

	@Override
	public void createOrUpdateInvitationEvent(Integer eventCollectionId,
			String eventUid, Date dtStamp, InvitationStatus status,
			String syncKey) {
		createOrUpdateInvitation(eventCollectionId, eventUid, null, null,
				dtStamp, status, syncKey);
	}

	@Override
	public void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey) {

		StringBuilder calQuery = new StringBuilder(
				"SELECT status"
						+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND event_uid=? AND ");
		if (emailCollectionId != null) {
			calQuery.append(" mail_collection_id=? ");
		} else {
			calQuery.append(" mail_collection_id IS NULL ");
		}
		calQuery.append(" AND ");
		if (emailUid != null) {
			calQuery.append(" mail_uid=? ");
		} else {
			calQuery.append(" mail_uid IS NULL ");
		}
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery.toString());
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);

			if (emailCollectionId != null) {
				ps.setInt(i++, emailCollectionId);
			}
			if (emailUid != null) {
				ps.setLong(i++, emailUid);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				updateToSyncedInvitation(con, eventCollectionId, eventUid,
						emailCollectionId, emailUid, status, syncKey, dtStamp);
			} else {
				createInvitation(con, eventCollectionId, eventUid,
						emailCollectionId, emailUid, dtStamp, status, syncKey);
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	private void updateToSyncedInvitation(Connection con,
			Integer eventCollectionId, String eventUid,
			Integer emailCollectionId, Long emailUid, InvitationStatus status,
			String synkKey, Date dtStamp) {

		PreparedStatement ps = null;
		TransactionManager ut = dbcp.getDataSource().getTransactionManager();
		try {
			ut.begin();
			StringBuilder query = new StringBuilder(
					"UPDATE opush_invitation_mapping SET status=?, sync_key=?, dtstamp= ? ");
			query.append("WHERE event_collection_id=? AND event_uid=? ");

			if (emailCollectionId != null) {
				query.append("AND mail_collection_id=? ");
			} else {
				query.append("AND mail_collection_id IS NULL ");
			}
			if (emailUid != null) {
				query.append("AND mail_uid=? ");
			} else {
				query.append("AND mail_uid IS NULL ");
			}
			ps = con.prepareStatement(query.toString());
			int i = 1;
			ps.setString(i++, status.toString());
			ps.setString(i++, synkKey);

			if (dtStamp != null) {
				ps.setTimestamp(i++, new Timestamp(dtStamp.getTime()));
			} else {
				ps.setNull(i++, Types.TIMESTAMP);
			}

			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);
			if (emailCollectionId != null) {
				ps.setInt(i++, emailCollectionId);
			}
			if (emailUid != null) {
				ps.setLong(i++, emailUid);
			}

			ps.execute();
			ut.commit();
		} catch (Throwable se) {
			try {
				rollback(ut);
			} catch (Exception e) {
				logger.error("Error while rolling-back", e);
			}
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(null, ps, null);
		}
	}

	private void createInvitation(Connection con, Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey)
			throws SQLException {

		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT into opush_invitation_mapping "
					+ "(mail_collection_id, mail_uid, event_collection_id, event_uid, dtstamp, status, sync_key)"
					+ " VALUES (?,?,?,?,?,?,?)");
			int i = 1;
			if (emailCollectionId != null) {
				ps.setInt(i++, emailCollectionId);
			} else {
				ps.setNull(i++, Types.INTEGER);
			}
			if (emailUid != null) {
				ps.setLong(i++, emailUid);
			} else {
				ps.setNull(i++, Types.INTEGER);
			}

			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);

			if (dtStamp != null) {
				ps.setTimestamp(i++, new Timestamp(dtStamp.getTime()));
			} else {
				ps.setNull(i++, Types.TIMESTAMP);
			}

			ps.setString(i++, status.toString());
			ps.setString(i++, syncKey);
			ps.execute();
		} finally {
			JDBCUtils.cleanup(null, ps, null);
		}
	}

	@Override
	public void updateInvitationStatus(InvitationStatus status,
			Integer emailCollectionId, Collection<Long> emailUids) {
		updateInvitationStatus(status, null, emailCollectionId, emailUids);
	}

	@Override
	public void updateInvitationStatus(InvitationStatus status, String syncKey,
			Integer emailCollectionId, Collection<Long> emailUids) {
		if (emailUids != null && emailUids.size() > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			TransactionManager ut = dbcp.getDataSource().getTransactionManager();
			String uids = buildEmailUid(emailUids);
			try {
				ut.begin();
				con = dbcp.getDataSource().getConnection();
				ps = con.prepareStatement("UPDATE opush_invitation_mapping SET status=?, sync_key=?, dtstamp=dtstamp WHERE mail_collection_id=? AND mail_uid IN ("
						+ uids + ")");
				int idx = 1;
				ps.setString(idx++, status.toString());
				if (syncKey == null) {
					ps.setNull(idx++, Types.VARCHAR);
				} else {
					ps.setString(idx++, syncKey);
				}
				ps.setInt(idx++, emailCollectionId);
				ps.execute();
				ut.commit();
			} catch (Throwable se) {
				try {
					rollback(ut);
				} catch (Exception e) {
					logger.error("Error while rolling-back", e);
				}
				logger.error(se.getMessage(), se);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public void updateInvitationEventStatus(InvitationStatus status,
			Integer eventCollectionId, Collection<String> eventUids) {
		updateInvitationEventStatus(status, null, eventCollectionId, eventUids);
	}

	@Override
	public void updateInvitationEventStatus(InvitationStatus status,
			String syncKey, Integer eventCollectionId,
			Collection<String> eventUids) {
		if (eventUids.size() > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			TransactionManager ut = dbcp.getDataSource().getTransactionManager();
			String uids = buildEventUid(eventUids);
			try {
				ut.begin();
				con = dbcp.getDataSource().getConnection();
				ps = con.prepareStatement("UPDATE opush_invitation_mapping SET status=?, sync_key=?, dtstamp=dtstamp WHERE mail_collection_id IS NULL AND mail_uid IS NULL AND event_collection_id=? AND event_uid IN ("
						+ uids + ")");
				ps.setString(1, status.toString());
				if (syncKey == null) {
					ps.setNull(2, Types.VARCHAR);
				} else {
					ps.setString(2, syncKey);
				}
				ps.setInt(3, eventCollectionId);
				ps.execute();
				ut.commit();
			} catch (Throwable se) {
				try {
					rollback(ut);
				} catch (Exception e) {
					logger.error("Error while rolling-back", e);
				}
				logger.error(se.getMessage(), se);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public List<String> getInvitationEventMustSynced(Integer eventCollectionId) {
		List<String> ret = new ArrayList<String>();
		String calQuery = "SELECT event_uid FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND status=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, InvitationStatus.EVENT_MUST_SYNCED.toString());

			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("event_uid"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey) {
		return getEmail(emailCollectionId, syncKey,
				InvitationStatus.EMAIL_SYNCED, InvitationStatus.EMAIL_TO_SYNCED);
	}

	@Override
	public List<Long> getEmailToDeleted(Integer emailCollectionId,
			String syncKey) {
		return getEmail(emailCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	private List<Long> getEmail(Integer emailCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) {
		List<Long> ret = new ArrayList<Long>();
		String calQuery = "SELECT mail_uid FROM opush_invitation_mapping "
				+ "WHERE mail_collection_id=? AND status=? OR ( sync_key=? AND status=? )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, emailCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getLong("mail_uid"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public List<String> getEventToSynced(Integer eventCollectionId,
			String syncKey) {
		return getEvent(eventCollectionId, syncKey,
				InvitationStatus.EVENT_SYNCED, InvitationStatus.EVENT_TO_SYNCED);
	}

	@Override
	public List<String> getEventToDeleted(Integer eventCollectionId,
			String syncKey) {
		return getEvent(eventCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private List<String> getEvent(Integer eventCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) {
		List<String> ret = new ArrayList<String>();
		String calQuery = "SELECT event_uid"
				+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND ( status=? OR ( sync_key=? AND status=? ))";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("event_uid"));
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	private String buildEmailUid(Collection<Long> emailUids) {
		StringBuilder sb = new StringBuilder();
		sb.append("0");
		for (Long l : emailUids) {
			sb.append(",");
			sb.append(l);
		}
		return sb.toString();
	}

	private String buildEventUid(Collection<String> eventUids) {
		StringBuilder sb = new StringBuilder();
		sb.append("'0'");
		for (String s : eventUids) {
			sb.append(",");
			sb.append("'");
			sb.append(s);
			sb.append("'");
		}
		return sb.toString();
	}

	public Object getJdbcObject(String value, String type) {
		if ("PGSQL".equals(type)) {
			try {
				Object o = Class.forName("org.postgresql.util.PGobject")
						.newInstance();
				Method setType = o.getClass()
						.getMethod("setType", String.class);
				Method setValue = o.getClass().getMethod("setValue",
						String.class);

				setType.invoke(o, "vpartstat");
				setValue.invoke(o, toString());
				return o;
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		} else {
			return value;
		}
	}

	@Override
	public void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long emailUid) {
		Connection con = null;
		PreparedStatement ps = null;
		TransactionManager ut = dbcp.getDataSource().getTransactionManager();
		try {
			ut.begin();
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("DELETE FROM opush_invitation_mapping "
					+ "WHERE event_collection_id=? AND mail_collection_id=? AND mail_uid=?");
			ps.setInt(1, eventCollectionId);
			ps.setInt(2, emailCollectionId);
			ps.setLong(3, emailUid);
			ps.execute();
			ut.commit();
		} catch (Throwable se) {
			try {
				rollback(ut);
			} catch (Exception e) {
				logger.error("Error while rolling-back", e);
			}
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public Set<Email> getSyncedMail(Integer devId, Integer collectionId) {
		long time = System.currentTimeMillis();
		Set<Email> uids = new HashSet<Email>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read FROM opush_sync_mail WHERE collection_id=? and device_id=?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);

			evrs = ps.executeQuery();
			while (evrs.next()) {
				Long uid = evrs.getLong("mail_uid");
				Boolean read = evrs.getBoolean("is_read");
				uids.add(new Email(uid, read));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(" loadMailCache() in " + time + "ms.");
		}
		return uids;
	}

	@Override
	public Set<Email> getUpdatedMail(Integer devId, Integer collectionId,
			Date updatedFrom) {
		long time = System.currentTimeMillis();
		Set<Email> uids = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read "
					+ "FROM opush_sync_mail "
					+ "WHERE collection_id=? and device_id=? and timestamp >= ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setTimestamp(3, new Timestamp(updatedFrom.getTime()));
			evrs = ps.executeQuery();
			while (evrs.next()) {
				Long uid = evrs.getLong("mail_uid");
				Boolean read = evrs.getBoolean("is_read");
				uids.add(new Email(uid, read));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}

		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(" getUpdatedMail() in " + time + "ms.");
		}
		return uids;
	}

	@Override
	public Set<Long> getDeletedMail(Integer devId, Integer collectionId,
			Date lastSync) {
		long time = System.currentTimeMillis();
		Builder<Long> uids = ImmutableSet.builder();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement("SELECT mail_uid "
					+ "FROM opush_sync_deleted_mail "
					+ "WHERE collection_id=? and device_id=? and timestamp >= ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setTimestamp(3, new Timestamp(lastSync.getTime()));

			evrs = ps.executeQuery();
			while (evrs.next()) {
				Long uid = evrs.getLong("mail_uid");
				uids.add(uid);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(" loadMailCache() in " + time + "ms.");
		}
		return uids.build();
	}

	@Override
	public void removeMessages(Integer devId, Integer collectionId,
			Collection<Long> mailUids) throws SQLException {
		removeMessages(devId, collectionId, Calendar.getInstance().getTime(),
				mailUids);
	}

	@Override
	public void removeMessages(Integer devId, Integer collectionId,
			Date lastSync, Collection<Long> uids) throws SQLException {
		PreparedStatement del = null;
		PreparedStatement insert = null;
		Connection con = null;
		String ids = buildListId(uids);
		try {
			con = dbcp.getDataSource().getConnection();
			del = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE collection_id=? AND device_id=? AND mail_uid IN ("
							+ ids + ")");
			del.setInt(1, collectionId);
			del.setInt(2, devId);
			del.executeUpdate();

			insert = con
					.prepareStatement("INSERT INTO opush_sync_deleted_mail (collection_id, device_id, mail_uid, timestamp) VALUES (?, ?, ?, ?)");
			for (Long uid : uids) {
				insert.setInt(1, collectionId);
				insert.setInt(2, devId);
				insert.setLong(3, uid);
				insert.setTimestamp(4, new Timestamp(lastSync.getTime()));
				insert.addBatch();
			}
			insert.executeBatch();

		} finally {
			JDBCUtils.cleanup(null, insert, null);
			JDBCUtils.cleanup(con, del, null);
		}
	}

	@Override
	public void addMessages(Integer devId, Integer collectionId,
			Collection<Email> messages) throws SQLException {
		addMessages(devId, collectionId, null, messages);
	}

	@Override
	public void addMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Email> messages) throws SQLException {
		if (lastSync == null) {
			lastSync = DateUtils.getCurrentGMTCalendar().getTime();
		}
		Connection con = null;
		PreparedStatement insert = null;
		PreparedStatement del = null;
		String ids = buildListIdFromEmailCache(messages);
		try {
			con = dbcp.getDataSource().getConnection();
			del = con
					.prepareStatement("DELETE FROM opush_sync_mail WHERE collection_id=? AND device_id=? AND mail_uid IN ("
							+ ids + ")");
			del.setInt(1, collectionId);
			del.setInt(2, devId);
			del.executeUpdate();

			insert = con
					.prepareStatement("INSERT INTO opush_sync_mail (collection_id, device_id, mail_uid, is_read, timestamp) VALUES (?, ?, ?, ?, ?)");
			for (Email uid : messages) {
				insert.setInt(1, collectionId);
				insert.setInt(2, devId);
				insert.setLong(3, uid.getUid());
				insert.setBoolean(4, uid.isRead());
				insert.setTimestamp(5, new Timestamp(lastSync.getTime()));
				insert.addBatch();
			}
			insert.executeBatch();
		} finally {
			JDBCUtils.cleanup(con, insert, null);
		}
	}

	private String buildListIdFromEmailCache(Collection<Email> messages) {
		Collection<Long> uids = Collections2.transform(messages,
				new Function<Email, Long>() {
					@Override
					public Long apply(Email input) {
						return input.getUid();
					}
				});
		return buildListId(uids);
	}

	private String buildListId(Collection<Long> uids) {
		if (uids.isEmpty()) {
			return "0";
		}
		return Joiner.on(",").skipNulls().join(uids);
	}

	@Override
	public boolean eventIsAlreadySynced(Integer eventCollectionId,
			String eventUid) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String query = "SELECT * FROM opush_invitation_mapping "
				+ "JOIN event ON event_ext_id = event_uid "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND status = ? AND dtstamp > event_timeupdate";

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setString(3, InvitationStatus.EVENT_SYNCED.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}

		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return false;
	}

	@Override
	public void setEventStatusAtToDelete(Integer eventCollectionId,
			String eventUid) {
		Connection con = null;
		PreparedStatement ps = null;

		String query = "UPDATE opush_invitation_mapping "
				+ "SET status = ? "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND (status = ? OR status = ?)";

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, InvitationStatus.EVENT_TO_DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EVENT_SYNCED.toString());
			ps.setString(5, InvitationStatus.EVENT_TO_SYNCED.toString());
			ps.execute();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public void setInvitationStatusAtToDelete(Integer eventCollectionId,
			String eventUid) {
		Connection con = null;
		PreparedStatement ps = null;

		String query = "UPDATE opush_invitation_mapping "
				+ "SET status = ? "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND (status = ? OR status = ?)";

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, InvitationStatus.DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EMAIL_SYNCED.toString());
			ps.setString(5, InvitationStatus.EMAIL_TO_SYNCED.toString());
			ps.execute();
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public boolean invitationIsAlreadySynced(Integer eventCollectionId,
			String eventUid) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String query = "SELECT * FROM opush_invitation_mapping "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND status = ? AND dtstamp > "
				+ "( "
				+ "SELECT oim.dtstamp FROM opush_invitation_mapping as oim "
				+ "WHERE oim.event_collection_id = ? AND oim.event_uid = ? AND "
				+ "(oim.status LIKE '%EVENT%' OR oim.status = 'DELETED' AND oim.mail_uid IS NULL) "
				+ ")";

		try {
			con = dbcp.getDataSource().getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setString(3, InvitationStatus.EMAIL_SYNCED.toString());
			ps.setInt(4, eventCollectionId);
			ps.setString(5, eventUid);
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}

		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return false;
	}

	private void rollback(TransactionManager ut) {
		try {
			ut.rollback();
		} catch (Throwable e) {
			logger.error("Error while rollbacking transaction", e);
		}
	}
	
}
