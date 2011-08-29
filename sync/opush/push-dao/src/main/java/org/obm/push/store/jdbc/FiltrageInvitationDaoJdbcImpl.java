package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.dbcp.IDBCP;
import org.obm.push.bean.InvitationStatus;
import org.obm.push.exception.DaoException;
import org.obm.push.store.FiltrageInvitationDao;
import org.obm.push.utils.JDBCUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FiltrageInvitationDaoJdbcImpl extends AbstractJdbcImpl implements FiltrageInvitationDao{

	@Inject
	private FiltrageInvitationDaoJdbcImpl(IDBCP dbcp) {
		super(dbcp);
	}
	
	@Override
	public boolean eventIsAlreadySynced(Integer eventCollectionId,
			String eventUid) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String query = "SELECT * FROM opush_invitation_mapping "
				+ "JOIN event ON event_ext_id = event_uid "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND status = ? AND ( dtstamp > event_timeupdate OR event_timeupdate IS NULL)";

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setString(3, InvitationStatus.EVENT_SYNCED.toString());
			rs = ps.executeQuery();
			if (rs.next()) {
				return true;
			}

		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return false;
	}

	@Override
	public void setEventStatusAtToDelete(Integer eventCollectionId,
			String eventUid) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;

		String query = "UPDATE opush_invitation_mapping "
				+ "SET status = ? "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND (status = ? OR status = ?)";

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, InvitationStatus.EVENT_TO_DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EVENT_SYNCED.toString());
			ps.setString(5, InvitationStatus.EVENT_TO_SYNCED.toString());
			ps.execute();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public void setInvitationStatusAtToDelete(Integer eventCollectionId,
			String eventUid) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;

		String query = "UPDATE opush_invitation_mapping "
				+ "SET status = ? "
				+ "WHERE event_collection_id = ? AND event_uid = ? AND (status = ? OR status = ?)";

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, InvitationStatus.DELETED.toString());
			ps.setInt(2, eventCollectionId);
			ps.setString(3, eventUid);
			ps.setString(4, InvitationStatus.EMAIL_SYNCED.toString());
			ps.setString(5, InvitationStatus.EMAIL_TO_SYNCED.toString());
			ps.execute();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public boolean invitationIsAlreadySynced(Integer eventCollectionId,
			String eventUid) throws DaoException {
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
			con = dbcp.getConnection();
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

		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return false;
	}
	
	@Override
	public void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long emailUid) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("DELETE FROM opush_invitation_mapping "
					+ "WHERE event_collection_id=? AND mail_collection_id=? AND mail_uid=?");
			ps.setInt(1, eventCollectionId);
			ps.setInt(2, emailCollectionId);
			ps.setLong(3, emailUid);
			ps.execute();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public Boolean isMostRecentInvitation(Integer eventCollectionId,
			String eventUid, Date dtStamp) throws DaoException {

		Boolean ret = true;
		String calQuery = "SELECT * FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND event_uid=? AND dtstamp > ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, eventUid);
			ps.setTimestamp(3, new Timestamp(dtStamp.getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = false;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid) throws DaoException {
		return isInvitationExistsToStatus(eventCollectionId, eventUid,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	@Override
	public boolean haveEventToDeleted(Integer eventCollectionId, String eventUid) throws DaoException {
		return isInvitationExistsToStatus(eventCollectionId, eventUid,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private boolean isInvitationExistsToStatus(Integer eventCollectionId,
			String eventUid, InvitationStatus status) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		boolean isInvitationExists = false;
		String query = "SELECT status FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND event_uid=? AND status=?";

		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			int i = 1;
			ps.setInt(i++, eventCollectionId);
			ps.setString(i++, eventUid);
			ps.setString(i++, status.toString());

			rs = ps.executeQuery();
			if (rs.next()) {
				isInvitationExists = true;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return isInvitationExists;
	}

	@Override
	public int getCountEmailFilterChanges(Integer emailCollectionId,
			String syncKey) throws DaoException {
		int nb = 0;
		String calQuery = "SELECT event_uid "
				+ "FROM opush_invitation_mapping "
				+ "WHERE mail_collection_id=? AND event_collection_id=? "
				+ "AND ( status=? OR status=? OR ( ( status=? OR status=? ) AND sync_key=?) )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
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
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return nb;
	}

	@Override
	public int getCountEventFilterChanges(Integer eventCollectionId,
			String syncKey) throws DaoException {
		int nb = 0;
		String calQuery = "SELECT event_uid "
				+ "FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? "
				+ "AND ( status=? OR status=? OR status=? OR ( ( status=? OR status=? ) AND sync_key=?) )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
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
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return nb;
	}

	@Override
	public void createOrUpdateInvitationEventAsMustSynced(
			Integer eventCollectionId, String eventUid, Date dtStamp) throws DaoException {
		createOrUpdateInvitationEvent(eventCollectionId, eventUid, dtStamp,
				InvitationStatus.EVENT_MUST_SYNCED);
	}

	@Override
	public void createOrUpdateInvitationEvent(Integer eventCollectionId,
			String uid, Date dtStamp, InvitationStatus status) throws DaoException {
		createOrUpdateInvitationEvent(eventCollectionId, uid, dtStamp, status,
				null);
	}

	@Override
	public void createOrUpdateInvitationEvent(Integer eventCollectionId,
			String eventUid, Date dtStamp, InvitationStatus status,
			String syncKey) throws DaoException {
		createOrUpdateInvitation(eventCollectionId, eventUid, null, null,
				dtStamp, status, syncKey);
	}

	@Override
	public void createOrUpdateInvitation(Integer eventCollectionId,
			String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey) throws DaoException {

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
			con = dbcp.getConnection();
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
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
	}

	private void updateToSyncedInvitation(Connection con,
			Integer eventCollectionId, String eventUid,
			Integer emailCollectionId, Long emailUid, InvitationStatus status,
			String synkKey, Date dtStamp) throws DaoException {

		PreparedStatement ps = null;
		try {
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
		} catch (SQLException e) {
			throw new DaoException(e);
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
			Integer emailCollectionId, Collection<Long> emailUids) throws DaoException {
		updateInvitationStatus(status, null, emailCollectionId, emailUids);
	}

	@Override
	public void updateInvitationStatus(InvitationStatus status, String syncKey,
			Integer emailCollectionId, Collection<Long> emailUids) throws DaoException {
		if (emailUids != null && emailUids.size() > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			String uids = buildEmailUid(emailUids);
			try {
				con = dbcp.getConnection();
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
			} catch (SQLException e) {
				throw new DaoException(e);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public void updateInvitationEventStatus(InvitationStatus status,
			Integer eventCollectionId, Collection<String> eventUids) throws DaoException {
		updateInvitationEventStatus(status, null, eventCollectionId, eventUids);
	}

	@Override
	public void updateInvitationEventStatus(InvitationStatus status,
			String syncKey, Integer eventCollectionId,
			Collection<String> eventUids) throws DaoException {
		if (eventUids.size() > 0) {
			Connection con = null;
			PreparedStatement ps = null;
			String uids = buildEventUid(eventUids);
			try {
				con = dbcp.getConnection();
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
			} catch (SQLException e) {
				throw new DaoException(e);
			} finally {
				JDBCUtils.cleanup(con, ps, null);
			}
		}
	}

	@Override
	public List<String> getInvitationEventMustSynced(Integer eventCollectionId) throws DaoException {
		List<String> ret = new ArrayList<String>();
		String calQuery = "SELECT event_uid FROM opush_invitation_mapping "
				+ "WHERE event_collection_id=? AND status=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, InvitationStatus.EVENT_MUST_SYNCED.toString());

			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("event_uid"));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey) throws DaoException {
		return getEmail(emailCollectionId, syncKey,
				InvitationStatus.EMAIL_SYNCED, InvitationStatus.EMAIL_TO_SYNCED);
	}

	@Override
	public List<Long> getEmailToDeleted(Integer emailCollectionId,
			String syncKey) throws DaoException {
		return getEmail(emailCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EMAIL_TO_DELETED);
	}

	private List<Long> getEmail(Integer emailCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) throws DaoException {
		List<Long> ret = new ArrayList<Long>();
		String calQuery = "SELECT mail_uid FROM opush_invitation_mapping "
				+ "WHERE mail_collection_id=? AND status=? OR ( sync_key=? AND status=? )";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, emailCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getLong("mail_uid"));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}
		return ret;
	}

	@Override
	public List<String> getEventToSynced(Integer eventCollectionId,
			String syncKey) throws DaoException {
		return getEvent(eventCollectionId, syncKey,
				InvitationStatus.EVENT_SYNCED, InvitationStatus.EVENT_TO_SYNCED);
	}

	@Override
	public List<String> getEventToDeleted(Integer eventCollectionId,
			String syncKey) throws DaoException {
		return getEvent(eventCollectionId, syncKey, InvitationStatus.DELETED,
				InvitationStatus.EVENT_TO_DELETED);
	}

	private List<String> getEvent(Integer eventCollectionId, String syncKey,
			InvitationStatus status, InvitationStatus statusAction) throws DaoException {
		List<String> ret = new ArrayList<String>();
		String calQuery = "SELECT event_uid"
				+ " FROM opush_invitation_mapping WHERE event_collection_id=? AND ( status=? OR ( sync_key=? AND status=? ))";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(calQuery);
			ps.setInt(1, eventCollectionId);
			ps.setString(2, statusAction.toString());
			ps.setString(3, syncKey);
			ps.setString(4, status.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("event_uid"));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
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

}
