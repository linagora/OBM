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
package fr.aliacom.obm.common.setting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class SettingDao {
	
	private static final Logger logger = LoggerFactory
			.getLogger(SettingDao.class);
	private final ObmHelper obmHelper;

	@Inject
	private SettingDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public Map<String, String> getSettings(ObmUser user) {
		Builder<String, String> mapBuilder = new Builder<String, String>();

		String domain = user.getDomain().getName();
		String userLogin = user.getLogin();
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			con = obmHelper.getConnection();

			String q = "select userobmpref_option, userobmpref_value from UserObmPref "
					+ "where userobmpref_user_id is null "
					+ "and userobmpref_option not in "
					+ "(select userobmpref_option from UserObmPref where userobmpref_user_id=(select userobm_id from UserObm "
					+ "where userobm_login=? "
					+ "and userobm_domain_id=(select domain_id from Domain where domain_name=?) "
					+ ")) "
					+ "union "
					+ "select userobmpref_option, userobmpref_value from UserObmPref "
					+ "where userobmpref_user_id=(select userobm_id from UserObm "
					+ "where userobm_login=? "
					+ "and userobm_domain_id=(select domain_id from Domain where domain_name=?) "
					+ " )";
			
			ps = con.prepareStatement(q);
			int idx = 1;
			ps.setString(idx++, userLogin);
			ps.setString(idx++, domain);
			ps.setString(idx++, userLogin);
			ps.setString(idx++, domain);
			rs = ps.executeQuery();
			while (rs.next()) {
				mapBuilder.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException se) {
			logger.error("Error fetching preferences for " + user + " at "
					+ domain, se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		
		ImmutableMap<String, String> data = mapBuilder.build();
		
		logger.info("return " + data.size() + " settings for userid: " + user
				+ " at " + domain);
		return data;
	}

	public void setVacationSettings(AccessToken token, VacationSettings vs) {
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = obmHelper.getConnection();

			String query = "UPDATE UserObm SET "
					+ "userobm_vacation_enable=?, "
					+ "userobm_vacation_datebegin=?, "
					+ "userobm_vacation_dateend=?, "
					+ "userobm_vacation_message=? " + "WHERE userobm_id=?";

			ps = con.prepareStatement(query);

			int i = 1;
			ps.setInt(i++, vs.isEnabled() ? 1 : 0);
			if (vs.getStart() != null) {
				ps.setTimestamp(i++, new Timestamp(vs.getStart().getTime()));
			} else {
				ps.setNull(i++, Types.TIMESTAMP);
			}
			if (vs.getEnd() != null) {
				ps.setTimestamp(i++, new Timestamp(vs.getEnd().getTime()));
			} else {
				ps.setNull(i++, Types.TIMESTAMP);
			}
			ps.setString(i++, vs.getText());
			ps.setInt(i++, token.getObmId());

			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Could not store vacation settings for "
					+ token.getUserLogin(), e);
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	public void setEmailForwarding(AccessToken token, ForwardingSettings fs) {
		Connection con = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			String query = "UPDATE UserObm SET " + "userobm_nomade_enable=?, "
					+ "userobm_nomade_local_copy=?, "
					+ "userobm_email_nomade=? " + "WHERE userobm_id=?";

			ps = con.prepareStatement(query);

			int i = 1;
			ps.setInt(i++, fs.isEnabled() ? 1 : 0);
			ps.setInt(i++, fs.isLocalCopy() ? 1 : 0);
			ps.setString(i++, fs.getEmail());
			ps.setInt(i++, token.getObmId());

			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("Could not store forwarding settings for "
					+ token.getUserLogin(), e);
		} finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	public ForwardingSettings getEmailForwarding(AccessToken token) {
		ForwardingSettings ret = new ForwardingSettings();

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			String query = "SELECT userobm_nomade_perms, userobm_nomade_enable, userobm_nomade_local_copy, "
					+ "userobm_email_nomade FROM UserObm "
					+ "WHERE userobm_id=?";

			ps = con.prepareStatement(query);

			ps.setInt(1, token.getObmId());
			rs = ps.executeQuery();

			if (rs.next()) {
				ret.setAllowed(rs.getBoolean(1));
				ret.setEnabled(rs.getBoolean(2));
				ret.setLocalCopy(rs.getBoolean(3));
				String readMail = rs.getString(4);
				if (readMail != null) {
					String m = readMail;
					if (m.contains("\r\n")) {
						m = readMail.split("\r\n")[0];
					}
					ret.setEmail(m);
				}
			}

		} catch (SQLException e) {
			logger.error("Could not load forwarding settings for "
					+ token.getUserLogin(), e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

	public VacationSettings getVacationSettings(AccessToken token) {
		VacationSettings ret = new VacationSettings();

		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			con = obmHelper.getConnection();

			String query = "SELECT userobm_vacation_enable, userobm_vacation_datebegin, "
					+ "userobm_vacation_dateend, userobm_vacation_message FROM UserObm "
					+ "WHERE userobm_id=?";

			ps = con.prepareStatement(query);

			ps.setInt(1, token.getObmId());
			rs = ps.executeQuery();

			if (rs.next()) {
				ret.setEnabled(rs.getBoolean(1));
				Timestamp ts = rs.getTimestamp(2);
				if (ts != null) {
					ret.setStart(new Date(ts.getTime()));
				}
				ts = rs.getTimestamp(3);
				if (ts != null) {
					ret.setEnd(new Date(ts.getTime()));
				}
				ret.setText(rs.getString(4));
			}

		} catch (SQLException e) {
			logger.error("Could load vacation settings for " + token.getUserLogin(),
					e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

}
