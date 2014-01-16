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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailNotFoundException;
import org.obm.push.mail.bean.Email;
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
	private EmailDaoJdbcImpl(DatabaseConnectionProvider dbcp) {
		super(dbcp);
	}
	
	@Override
	public void createSyncEntries(Integer devId, Integer collectionId,	Set<Email> emailsToMarkAsSynced, Date lastSync) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("INSERT INTO opush_sync_mail (collection_id, device_id, mail_uid, timestamp) VALUES (?, ?, ?, ?)");
			for (Email email: emailsToMarkAsSynced) {
				ps.setInt(1, collectionId);
				ps.setInt(2, devId);
				ps.setLong(3, email.getUid());
				ps.setTimestamp(4, new Timestamp(email.getDate().getTime()));
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, null);
		}
	}

	@Override
	public void updateSyncEntriesStatus(Integer devId, Integer collectionId, Set<Email> alreadySyncedEmails) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("UPDATE opush_sync_mail SET is_read = ?, timestamp = ? WHERE collection_id = ? AND device_id = ? AND mail_uid = ?");
			for (Email email: alreadySyncedEmails) {
				int index = 1;
				ps.setBoolean(index++, email.isRead());
				ps.setTimestamp(index++, new Timestamp(email.getDate().getTime()));
				ps.setInt(index++, collectionId);
				ps.setInt(index++, devId);
				ps.setLong(index++, email.getUid());
				ps.addBatch();
			}
			ps.executeBatch();
			
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
	public Set<Email> alreadySyncedEmails(int collectionId, int deviceId, Collection<Email> emails) throws DaoException {
		Set<Email> alreadySyncedEmails = new HashSet<Email>();
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
			while (evrs.next()) {
				long uidDB = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = JDBCUtils.getDate(evrs, "timestamp");
				Email email = Email.builder()
						.uid(uidDB)
						.read(read)
						.date(date)
						.build();
				alreadySyncedEmails.add(email);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			JDBCUtils.cleanup(con, ps, evrs);
		}
		return alreadySyncedEmails;
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
				Date date = JDBCUtils.getDate(evrs, "timestamp");
				email = Email.builder()
						.uid(uidDB)
						.read(read)
						.date(date)
						.build();
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
	public Set<Email> listSyncedEmails(Integer devId, Integer collectionId, ItemSyncState syncState) throws DaoException {
		Set<Email> uids = new HashSet<Email>();
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet evrs = null;
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement("SELECT mail_uid, is_read, timestamp FROM opush_sync_mail WHERE collection_id = ? AND device_id = ? AND timestamp >= ?");
			ps.setInt(1, collectionId);
			ps.setInt(2, devId);
			Date lastSync = syncState.getSyncDate();
			ps.setDate(3, JDBCUtils.getDateWithoutTime(lastSync), DateUtils.getCurrentGMTCalendar());
			evrs = ps.executeQuery();
			while (evrs.next()) {
				long uid = evrs.getLong("mail_uid");
				boolean read = evrs.getBoolean("is_read");
				Date date = JDBCUtils.getDate(evrs, "timestamp");
				uids.add(Email.builder()
						.uid(uid)
						.read(read)
						.date(date)
						.build());
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
				Date date = JDBCUtils.getDate(evrs, "timestamp");
				uids.add(Email.builder()
						.uid(uid)
						.read(read)
						.date(date)
						.build());
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
