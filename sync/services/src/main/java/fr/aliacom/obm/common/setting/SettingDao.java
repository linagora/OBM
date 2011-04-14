/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

@Singleton
public class SettingDao {

	private static final String SETTING_TIME_ZONE = "set_timezone";
	
	private static final Log logger = LogFactory.getLog(SettingDao.class);
	private final ObmHelper obmHelper;

	@Inject
	private SettingDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public Locale getUserLanguage(AccessToken at) {
		Map<String, String> settings = getSettings(at);
		String localeAsString = Objects.firstNonNull(settings.get("set_lang"), "en");
		return new Locale(localeAsString);
	}
	
	public Map<String, String> getSettings(AccessToken at) {
		Map<String, String> data = new HashMap<String, String>();

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String domain = at.getDomain();
		String user = at.getUser();

		try {
			con = obmHelper.getConnection();

			String q = "select userobmpref_option, userobmpref_value from UserObmPref "
					+ "where userobmpref_user_id is null "
					+ "and userobmpref_option not in "
					+ "(select userobmpref_option from UserObmPref where userobmpref_user_id=(select userobm_id from UserObm "
					+ "where userobm_login=? ";
			if (domain != null) {
				q += "and userobm_domain_id=(select domain_id from Domain where domain_name=?) ";
			}
			q += ")) "
					+ "union "
					+ "select userobmpref_option, userobmpref_value from UserObmPref "
					+ "where userobmpref_user_id=(select userobm_id from UserObm "
					+ "where userobm_login=? ";
			if (domain != null) {
				q += "and userobm_domain_id=(select domain_id from Domain where domain_name=?) ";
			}
			q += " )";
			ps = con.prepareStatement(q);
			int idx = 1;
			ps.setString(idx++, user);
			if (domain != null) {
				ps.setString(idx++, domain);
			}
			ps.setString(idx++, user);
			if (domain != null) {
				ps.setString(idx++, domain);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				data.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException se) {
			logger.error("Error fetching preferences for " + user + " at "
					+ domain, se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

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
					+ token.getUser(), e);
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
					+ token.getUser(), e);
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
					+ token.getUser(), e);
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
			logger.error("Could load vacation settings for " + token.getUser(),
					e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return ret;
	}

	public TimeZone getUserTimeZone(AccessToken token) {
		Map<String, String> settings = getSettings(token);
		String timezoneAsString = Objects.firstNonNull(settings.get(SETTING_TIME_ZONE), "GMT");
		return TimeZone.getTimeZone(timezoneAsString);
	}

}
