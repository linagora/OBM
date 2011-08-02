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
import org.obm.push.exception.DaoException;
import org.obm.push.store.EmailDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.JDBCUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
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
	public void addMessages(Integer devId, Integer collectionId,
			Collection<Email> messages) throws DaoException {
		addMessages(devId, collectionId, null, messages);
	}

	@Override
	public void addMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Email> messages) throws DaoException {
		if (lastSync == null) {
			lastSync = DateUtils.getCurrentGMTCalendar().getTime();
		}
		Connection con = null;
		PreparedStatement insert = null;
		PreparedStatement del = null;
		String ids = buildListIdFromEmailCache(messages);
		try {
			con = dbcp.getConnection();
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
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, insert, null);
		}
	}

	@Override
	public void removeMessages(Integer devId, Integer collectionId,
			Collection<Long> mailUids) throws DaoException {
		removeMessages(devId, collectionId, Calendar.getInstance().getTime(),
				mailUids);
	}

	@Override
	public void removeMessages(Integer devId, Integer collectionId,
			Date lastSync, Collection<Long> uids) throws DaoException {
		PreparedStatement del = null;
		PreparedStatement insert = null;
		Connection con = null;
		String ids = buildListId(uids);
		try {
			con = dbcp.getConnection();
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

		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(null, insert, null);
			JDBCUtils.cleanup(con, del, null);
		}
	}
	
	@Override
	public Set<Email> getSyncedMail(Integer devId, Integer collectionId) throws DaoException {
		long time = System.currentTimeMillis();
		Set<Email> uids = new HashSet<Email>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
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
			throw new DaoException(e);
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
			Date updatedFrom) throws DaoException {
		long time = System.currentTimeMillis();
		Set<Email> uids = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
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
			throw new DaoException(e);
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
			Date lastSync) throws DaoException {
		long time = System.currentTimeMillis();
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
				Long uid = evrs.getLong("mail_uid");
				uids.add(uid);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		if (logger.isDebugEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.debug(" loadMailCache() in " + time + "ms.");
		}
		return uids.build();
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

}
