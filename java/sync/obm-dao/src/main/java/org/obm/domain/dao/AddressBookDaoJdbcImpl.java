/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.domain.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.AddressBook.Id;
import org.obm.utils.ObmHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;

@Singleton
public class AddressBookDaoJdbcImpl implements AddressBookDao {

	private static final String FIELDS = "id, name, syncable, is_default, timecreate, timeupdate, origin";

	private final ObmHelper obmHelper;

	@Inject
	private AddressBookDaoJdbcImpl(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	@Override
	public AddressBook get(Id id) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT " + FIELDS + " FROM AddressBook WHERE id = ?";

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setInt(1, id.getId());
			rs = ps.executeQuery();

			if (rs.next()) {
				return addressBookFromCursor(rs);
			}
		}
		catch (SQLException e) {
			throw new DaoException(e);
		}
		finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return null;
	}

	@Override
	public AddressBook create(AddressBook book, ObmUser owner) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		String query = "INSERT INTO AddressBook (name, syncable, is_default, origin, owner, domain_id, usercreate) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?)";

		try {
			int idx = 1;
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setString(idx++, book.getName());
			ps.setBoolean(idx++, book.isSyncable());
			ps.setBoolean(idx++, book.isDefaultBook());
			ps.setString(idx++, book.getOrigin());
			ps.setInt(idx++, owner.getUid());
			ps.setInt(idx++, owner.getDomain().getId());
			ps.setInt(idx++, owner.getUid());

			ps.executeUpdate();

			int bookId = obmHelper.lastInsertId(con);

			obmHelper.linkEntity(con, "AddressBookEntity", "addressbook_id", bookId);

			return get(Id.valueOf(bookId));
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	@Override
	public void enableAddressBookSynchronization(Id id, ObmUser user) throws DaoException {
		Connection con = null;
		PreparedStatement ps = null;
		String query = "INSERT INTO SyncedAddressbook (user_id, addressbook_id) VALUES (?, ?)";

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);

			ps.setInt(1, user.getUid());
			ps.setInt(2, id.getId());

			ps.executeUpdate();
		} catch (SQLException e) {
			throw new DaoException(e);
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	private AddressBook addressBookFromCursor(ResultSet rs) throws SQLException {
		return AddressBook
				.builder()
				.uid(Id.valueOf(rs.getInt("id")))
				.name(rs.getString("name"))
				.origin(rs.getString("origin"))
				.syncable(rs.getBoolean("syncable"))
				.defaultBook(rs.getBoolean("is_default"))
				.timecreate(JDBCUtils.getDate(rs, "timecreate"))
				.timeupdate(JDBCUtils.getDate(rs, "timeupdate"))
				.build();
	}

}
