package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.obm.dbcp.DBCP;
import org.obm.push.bean.Email;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailNotFoundException;
import org.obm.push.store.EmailDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.JDBCUtils;
import org.obm.push.utils.jdbc.LongIndexedSQLCollectionHelper;
import org.obm.push.utils.jdbc.LongSQLCollectionHelper;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailDaoJdbcImpl extends AbstractJdbcImpl implements EmailDao {

	@Inject
	private EmailDaoJdbcImpl(DBCP dbcp) {
		super(dbcp);
	}

	@Override
	public void updatedSyncedEmails(Integer devId, Integer collectionId, Collection<Email> messages) throws DaoException {
		updatedSyncedEmails(devId, collectionId, DateUtils.getCurrentDate(), messages);
	}

	@Override
	public void updatedSyncedEmails(Integer devId, Integer collectionId, Date lastSync, Collection<Email> emails) throws DaoException {
		for (Email email: emails) {
			try {
				getSyncedEmail(devId, collectionId, email.getUid());
				update(devId, collectionId, email);
			} catch (EmailNotFoundException e) {
				insert(devId, collectionId, lastSync, email);
			}
		}
	}
	
	@Override
	public void update(Integer devId, Integer collectionId, Email email) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = dbcp.getConnection();
			
			ps = con.prepareStatement("UPDATE opush_sync_mail SET is_read = ? WHERE collection_id = ? AND device_id = ? AND mail_uid = ?");
			ps.setBoolean(1, email.isRead());
			ps.setInt(2, collectionId);
			ps.setInt(3, devId);
			ps.setLong(4, email.getUid());
			
			ps.execute();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}	
	}
	
	@Override
	public void insert(Integer devId, Integer collectionId, Date lastSync, Email email) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = dbcp.getConnection();
			
			ps = con.prepareStatement("INSERT INTO opush_sync_mail (collection_id, device_id, mail_uid, timestamp) VALUES (?, ?, ?, ?)");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setLong(3, email.getUid());
			ps.setTimestamp(4, new Timestamp(lastSync.getTime()));
			
			ps.execute();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}
	
	@Override
	public void deleteSyncEmails(Integer devId, Integer collectionId, Collection<Long> mailUids) throws DaoException {
		deleteSyncEmails(devId, collectionId, Calendar.getInstance().getTime(), mailUids);
	}

	@Override
	public void deleteSyncEmails(Integer devId, Integer collectionId, Date lastSync, Collection<Long> uids) throws DaoException {
		PreparedStatement del = null;
		PreparedStatement insert = null;
		Connection con = null;
		
		LongSQLCollectionHelper idList = new LongSQLCollectionHelper(uids);
		try {
			con = dbcp.getConnection();
			del = con.prepareStatement(
					"DELETE FROM opush_sync_mail " +
					"WHERE collection_id=? AND device_id=? AND mail_uid " +
					"IN (" + idList.asPlaceHolders() + ")");
			del.setInt(1, collectionId);
			del.setInt(2, devId);
			idList.insertValues(del, 3);
			del.executeUpdate();

			insert = con.prepareStatement("INSERT INTO opush_sync_deleted_mail (collection_id, device_id, mail_uid, timestamp) VALUES (?, ?, ?, ?)");
			for (Long uid: uids) {
				insert.setInt(1, collectionId);
				insert.setInt(2, devId);
				insert.setLong(3, uid);
				insert.setTimestamp(4, new Timestamp(lastSync.getTime()));
				insert.addBatch();
			}
			insert.executeBatch();

		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(null, insert, null);
			JDBCUtils.cleanup(con, del, null);
		}
	}
	
	@Override
	public Set<Email> filterSyncedEmails(int collectionId, int deviceId, Collection<Email> emails) throws DaoException {
		Set<Email> filteredEmails = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		LongIndexedSQLCollectionHelper idList = new LongIndexedSQLCollectionHelper(emails);
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read, timestamp FROM opush_sync_mail " +
					"WHERE collection_id = ? AND device_id = ? AND mail_uid IN (" + idList.asPlaceHolders() + ")");
			ps.setInt(1, collectionId);
			ps.setInt(2, deviceId);
			idList.insertValues(ps, 3);
			evrs = ps.executeQuery();
			if (evrs.next()) {
				long uidDB = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = evrs.getDate("timestamp");
				Email email = new Email(uidDB, read, date);
				filteredEmails.add(email);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		return filteredEmails;
	}
	
	@Override
	public Email getSyncedEmail(Integer devId, Integer collectionId, long uid) throws DaoException, EmailNotFoundException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		
		Email email = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read, timestamp FROM opush_sync_mail " +
					"WHERE collection_id = ? AND device_id = ? AND mail_uid = ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setLong(3, uid);
			evrs = ps.executeQuery();
			if (evrs.next()) {
				long uidDB = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = evrs.getDate("timestamp");
				email = new Email(uidDB, read, date);
				return email;
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		throw new EmailNotFoundException(uid);
	}	
	
	@Override
	public Set<Email> listSyncedEmails(Integer devId, Integer collectionId, SyncState syncState) throws DaoException {
		Set<Email> uids = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read, timestamp FROM opush_sync_mail WHERE collection_id = ? AND device_id = ? AND timestamp >= ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setTimestamp(3, new Timestamp(syncState.getLastSync().getTime()));
			evrs = ps.executeQuery();
			while (evrs.next()) {
				long uid = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = evrs.getDate("timestamp");
				uids.add(new Email(uid, read, date));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		return uids;
	}

	@Override
	public Set<Email> listDeletedEmails(Integer devId, Integer collectionId) throws DaoException {
		Set<Email> uids = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read, timestamp FROM opush_sync_deleted_mail WHERE collection_id = ? and device_id = ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			evrs = ps.executeQuery();
			while (evrs.next()) {
				long uid = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = evrs.getDate("timestamp");
				uids.add(new Email(uid, read, date));
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		return uids;
	}
	
	@Override
	public Set<Long> getDeletedMail(Integer devId, Integer collectionId, Date lastSync) throws DaoException {
		Builder<Long> uids = ImmutableSet.builder();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid "
					+ "FROM opush_sync_deleted_mail "
					+ "WHERE collection_id=? and device_id=? and timestamp >= ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			ps.setTimestamp(3, new Timestamp(lastSync.getTime()));
			evrs = ps.executeQuery();
			while (evrs.next()) {
				long uid = evrs.getLong("mail_uid");
				uids.add(uid);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		return uids.build();
	}

}
