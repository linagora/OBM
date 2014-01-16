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
package fr.aliacom.obm.common.mailingList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obm.push.utils.jdbc.IntegerSQLCollectionHelper;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

/**
 * SQL queries for contact for sync
 */
@Singleton
public class MailingListHome {

	private static final Logger logger = LoggerFactory
			.getLogger(MailingListHome.class);

	private static final String ML_SELECT_FIELDS = "mailinglist_id, mailinglist_name, mailinglistemail_id, mailinglistemail_label, mailinglistemail_address";

	private final ObmHelper obmHelper;

	@Inject
	private MailingListHome(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	public MailingList getMailingListFromId(AccessToken at, Integer id) {
		String q = "SELECT  "
				+ ML_SELECT_FIELDS
				+ " FROM MailingList "
				+ " INNER JOIN MailingListEmail e ON mailinglist_id = mailinglistemail_mailinglist_id "
				+ " WHERE mailinglist_id=? AND mailinglist_owner=?";

		int idx = 1;
		MailingList ret = null;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(idx++, id);
			ps.setInt(idx++, at.getObmId());
			rs = ps.executeQuery();
			Map<Integer, MailingList> mls = mailingListFromCursor(rs);
			ret = mls.get(id);
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	private List<MLEmail> getMailingListFromIds(Connection con, AccessToken at,
			Integer mailingListId, List<Integer> ids) {
		List<MLEmail> ret = new ArrayList<MLEmail>(0);
		IntegerSQLCollectionHelper mlIds = new IntegerSQLCollectionHelper(ids);
		String q = "SELECT  "
				+ ML_SELECT_FIELDS
				+ " FROM MailingList "
				+ " INNER JOIN MailingListEmail e ON mailinglist_id = mailinglistemail_mailinglist_id "
				+ " WHERE mailinglist_id=? AND mailinglist_owner=? AND mailinglistemail_id IN ("
				+ mlIds.asPlaceHolders() + ")";

		int idx = 1;

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(q);
			ps.setInt(idx++, mailingListId);
			ps.setInt(idx++, at.getObmId());
			mlIds.insertValues(ps, idx);
			rs = ps.executeQuery();
			ret = mailingListEmailFromCursor(rs);
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(null, ps, rs);
		}
		return ret;
	}

	public List<MailingList> findMailingLists(AccessToken at) {
		String q = "SELECT  "
				+ ML_SELECT_FIELDS
				+ " FROM MailingList "
				+ " INNER JOIN MailingListEmail e ON mailinglist_id = mailinglistemail_mailinglist_id "
				+ " WHERE mailinglist_owner=? ";

		int idx = 1;

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);
			ps.setInt(idx++, at.getObmId());
			rs = ps.executeQuery();
			Map<Integer, MailingList> mls = mailingListFromCursor(rs);
			return new ArrayList<MailingList>(mls.values());
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return new ArrayList<MailingList>(0);
	}

	public MailingList createMailingList(AccessToken at, MailingList mailingList) throws SQLException {
		MailingList ret = null;
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			ret = createMailingList(con, at, mailingList);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
		return ret;
	}

	public MailingList modifyMailingList(AccessToken at, MailingList ml) throws SQLException {
		String q = "update MailingList SET " + "mailinglist_name=?, "
				+ "mailinglist_userupdate=?, "
				+ "mailinglist_timeupdate=now() "
				+ "WHERE mailinglist_id=? AND mailinglist_owner=? ";
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setString(idx++, ml.getName());
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, ml.getId());
			ps.setInt(idx++, at.getObmId());
			ps.executeUpdate();

			List<MLEmail> mles = createOrUpdateEmails(con, at, ml.getId(),
					ml.getEmails(), true);
			ml.getEmails().clear();
			ml.addEmails(mles);
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
		return ml;
	}

	public void removeMailingList(AccessToken at, Integer id) throws SQLException {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement("DELETE FROM MailingList WHERE mailinglist_id=?  AND mailinglist_owner=? ");
			ps.setInt(1, id);
			ps.setInt(2, at.getObmId());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	private MailingList createMailingList(Connection con, AccessToken at,
			MailingList ml) throws SQLException {
		int mlId = 0;
		PreparedStatement ps = null;
		try {
			ps = con.prepareStatement("INSERT INTO MailingList "
					+ " (mailinglist_usercreate, mailinglist_timecreate, mailinglist_userupdate, mailinglist_timeupdate, mailinglist_domain_id, mailinglist_owner, mailinglist_name) "
					+ " VALUES (?, now(), ?, now(), ?, ?, ?) ");
			int idx = 1;
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getObmId());
			ps.setInt(idx++, at.getDomain().getId());
			ps.setInt(idx++, at.getObmId());
			ps.setString(idx++, ml.getName());

			ps.executeUpdate();
			mlId = obmHelper.lastInsertId(con);
			ml.setId(mlId);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
		List<MLEmail> mles = createOrUpdateEmails(con, at, mlId,
				ml.getEmails(), true);
		ml.getEmails().clear();
		ml.addEmails(mles);
		return ml;
	}

	private List<MLEmail> createOrUpdateEmails(Connection con, AccessToken at,
			int mlId, List<MLEmail> emails, boolean delete) throws SQLException {
		PreparedStatement ps = null;
		try {
			if (delete) {
				ps = con.prepareStatement("DELETE FROM MailingListEmail WHERE mailinglistemail_mailinglist_id=?");
				ps.setInt(1, mlId);
				ps.executeUpdate();

				ps.close();
			}

			ps = con.prepareStatement("INSERT INTO MailingListEmail (mailinglistemail_mailinglist_id, mailinglistemail_label, mailinglistemail_address) "
					+ "VALUES (?, ?, ?)");
			
			List<Integer> ids = Lists.newArrayListWithExpectedSize(emails.size());
			for (MLEmail e : emails) {
				ps.setInt(1, mlId);
				ps.setString(2, e.getLabel());
				ps.setString(3, e.getAddress());
				ps.executeUpdate();
				ids.add(obmHelper.lastInsertId(con));
			}
			
			return getMailingListFromIds(con, at, mlId, ids);
		} finally {
			obmHelper.cleanup(null, ps, null);
		}
	}

	private Map<Integer, MailingList> mailingListFromCursor(ResultSet rs)
			throws SQLException {
		Map<Integer, MailingList> mls = new HashMap<Integer, MailingList>();
		while (rs.next()) {
			Integer id = rs.getInt("mailinglist_id");
			MailingList ml = mls.get(id);
			if (ml == null) {
				ml = new MailingList();
				ml.setId(id);
				ml.setName(rs.getString("mailinglist_name"));
				mls.put(id, ml);
			}
			MLEmail e = new MLEmail(rs.getString("mailinglistemail_label"),
					rs.getString("mailinglistemail_address"));
			e.setId(rs.getInt("mailinglistemail_id"));
			ml.addEmail(e);
		}
		return mls;
	}

	private List<MLEmail> mailingListEmailFromCursor(ResultSet rs)
			throws SQLException {
		List<MLEmail> mles = new ArrayList<MLEmail>();
		while (rs.next()) {
			MLEmail e = new MLEmail(rs.getString("mailinglistemail_label"),
					rs.getString("mailinglistemail_address"));
			e.setId(rs.getInt("mailinglistemail_id"));
			mles.add(e);
		}
		return mles;
	}

	public List<MLEmail> addEmails(AccessToken at, Integer mailingListId,
			List<MLEmail> emails) throws SQLException {
		Connection con = null;
		try {
			con = obmHelper.getConnection();
			return createOrUpdateEmails(con, at, mailingListId, emails, false);
		} finally {
			obmHelper.cleanup(con, null, null);
		}
	}

	public void removeEmail(AccessToken at, Integer mailingListId,
			Integer emailId) throws SQLException {
		String q = "DELETE e FROM MailingListEmail e "
				+ " INNER JOIN MailingList l ON l.mailinglist_id=e.mailinglistemail_mailinglist_id "
				+ "WHERE e.mailinglistemail_mailinglist_id=? AND e.mailinglistemail_id=? AND l.mailinglist_owner=?";
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(q);

			int idx = 1;
			ps.setInt(idx++, mailingListId);
			ps.setInt(idx++, emailId);
			ps.setInt(idx++, at.getObmId());
			ps.executeUpdate();
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

}
