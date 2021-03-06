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
package org.obm.domain.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.CommitedOperation;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.utils.ObmHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Calendar data access functions
 */
@Singleton
public class CommitedOperationDaoJdbcImpl implements CommitedOperationDao {

	private final ObmHelper obmHelper;
	private final CalendarDao calendarDao;
	private final ContactDao contactDao;

	@Inject
	@VisibleForTesting CommitedOperationDaoJdbcImpl(ObmHelper obmHelper, CalendarDao calendarDao, ContactDao contactDao) {
		this.obmHelper = obmHelper;
		this.calendarDao = calendarDao;
		this.contactDao = contactDao;
	}
	
	@Override
	public void store(AccessToken at, CommitedElement commitedElement) throws SQLException, ServerFault {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			if (operationIsAlreadyRecorded(con, commitedElement)) {
				ps = prepareUpdateStatement(con, commitedElement);
			} else {
				ps = prepareInsertStatement(con, commitedElement);
			}
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	private boolean operationIsAlreadyRecorded(Connection con, CommitedElement commitedElement) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(
					"SELECT commitedoperation_hash_client_id " +
					"FROM CommitedOperation " +
					"WHERE commitedoperation_hash_client_id = ? ");

			ps.setString(1, commitedElement.getClientId());
			
			rs = ps.executeQuery();
			return rs.next();
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
	}

	private PreparedStatement prepareInsertStatement(Connection con, CommitedElement commitedElement) throws SQLException {
		PreparedStatement ps = con.prepareStatement(
				"INSERT INTO CommitedOperation " +
				"(commitedoperation_hash_client_id, commitedoperation_entity_id, commitedoperation_kind, commitedoperation_client_date) " +
				"VALUES (?, ?, ?, ?) ");
		int idx = 1;
		try {
			ps.setString(idx++, commitedElement.getClientId());
			ps.setInt(idx++, commitedElement.getEntityId().getId());
			ps.setObject(idx++, getJdbcObject(commitedElement.getKind()));
			ps.setTimestamp(idx++, timestampOrNull(commitedElement.getClientDate()));
			return ps;
		} catch (SQLException e) {
			ps.close();
			throw e;
		} catch (RuntimeException e) {
			ps.close();
			throw e;
		}
	}

	private Timestamp timestampOrNull(Optional<Date> clientDate) {
		if (clientDate.isPresent()) {
			return new Timestamp(clientDate.get().getTime());
		}
		return null;
	}

	private PreparedStatement prepareUpdateStatement(Connection con, CommitedElement commitedElement) throws SQLException {
		PreparedStatement ps = con.prepareStatement(
				"UPDATE CommitedOperation " +
				"SET commitedoperation_entity_id=?, commitedoperation_client_date=? " +
				"WHERE commitedoperation_hash_client_id=? ");
		try {
			int idx = 1;
			ps.setInt(idx++, commitedElement.getEntityId().getId());
			ps.setTimestamp(idx++, timestampOrNull(commitedElement.getClientDate()));
			ps.setString(idx++, commitedElement.getClientId());
			return ps;
		} catch (SQLException e) {
			ps.close();
			throw e;
		} catch (RuntimeException e) {
			ps.close();
			throw e;
		}
	}

	@Override
	public CommitedOperation<Event> findAsEvent(AccessToken token, String clientId) throws SQLException, ServerFault {
		
		if (clientId == null) {
			return null;
		}
		
		checkClientIdFormat(clientId);
		
		String q = "SELECT e.evententity_event_id, commitedoperation_client_date FROM CommitedOperation a "
				+ "INNER JOIN EventEntity e ON a.commitedoperation_entity_id=evententity_entity_id "
				+ "WHERE commitedoperation_hash_client_id = ? AND commitedoperation_kind = ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setString(idx++, clientId);
			ps.setObject(idx++, getJdbcObject(Kind.VEVENT));
			rs = ps.executeQuery();

			CommitedOperation<Event> ret = null;
			if (rs.next()) {
				int id = rs.getInt(1);
				EventObmId eventObmId = new EventObmId(id);
				try {
					Event event = calendarDao.findEventById(token, eventObmId);
					ret = buildCommitedOperationFromResultSet(event, rs);
				} catch (EventNotFoundException e) {
				}
			}
			return ret;
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

	@Override
	public CommitedOperation<Contact> findAsContact(AccessToken token, String clientId) throws SQLException {
		
		if (clientId == null) {
			return null;
		}
		
		checkClientIdFormat(clientId);
		
		String q = "SELECT c.contactentity_contact_id, commitedoperation_client_date FROM CommitedOperation a "
				+ "INNER JOIN ContactEntity c ON a.commitedoperation_entity_id=contactentity_entity_id "
				+ "WHERE commitedoperation_hash_client_id = ? AND commitedoperation_kind = ?";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setString(idx++, clientId);
			ps.setObject(idx++, getJdbcObject(Kind.VCONTACT));
			rs = ps.executeQuery();

			CommitedOperation<Contact> ret = null;
			if (rs.next()) {
				int id = rs.getInt(1);
				try {
					Contact contact = contactDao.findContact(token, id);
					ret = buildCommitedOperationFromResultSet(contact, rs);
				} catch (ContactNotFoundException e) {
				}
			}
			return ret;
		
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
	}

	private <T> CommitedOperation<T> buildCommitedOperationFromResultSet(T entity, ResultSet rs) throws SQLException {
		Timestamp commitDate = rs.getTimestamp("commitedoperation_client_date");
		if (commitDate == null) {
			return new CommitedOperation<T>(entity, Optional.<Date>absent());
		}
		return new CommitedOperation<T>(entity, Optional.of(new Date(commitDate.getTime())));
	}

	private Object getJdbcObject(Kind kind) throws SQLException {
		return obmHelper.getDBCP()
				.getJdbcObject(ObmHelper.VKIND, kind.toString());
	}
	
	@VisibleForTesting void checkClientIdFormat(String clientId) {
		Preconditions.checkArgument(clientId.length() == 40, "clientId must have a length of 40 characters");
		Preconditions.checkArgument(Base64.isBase64(clientId), "clientId must be in base 64");
	}
}
