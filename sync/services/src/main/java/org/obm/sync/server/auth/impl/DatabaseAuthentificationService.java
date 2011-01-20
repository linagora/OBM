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
package org.obm.sync.server.auth.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.server.auth.IAuthentificationService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.ldap.UnixCrypt;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * Authentification against the OBM database
 */
@Singleton
public class DatabaseAuthentificationService implements
		IAuthentificationService {

	private static final Log logger = LogFactory.getLog(DatabaseAuthentificationService.class);
	private final ObmHelper obmHelper;
	private final Helper helper;

	@Inject
	private DatabaseAuthentificationService(ObmHelper obmHelper, Helper helper) {
		this.obmHelper = obmHelper;
		this.helper = helper;
	}
	
	public boolean doAuth(String userLogin, ObmDomain obmDomain, String password) {
		boolean valid = false;
		try {
			valid = isValidPassword(userLogin, password, obmDomain.getId());
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return valid;
	}

	/**
	 * Checks the validity of the password for the given login in the given
	 * domain
	 * 
	 * @param login
	 *            user login without the domain part
	 * @param password
	 *            clear text password
	 * @param domainId
	 *            database id of the domain
	 * @return true if the credential matches
	 */
	private boolean isValidPassword(String login, String password, int domainId) {
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		boolean ret = false;
		try {
			con = obmHelper.getConnection();

			String query;
			query = "SELECT userobm_password_type, userobm_password"
					+ " FROM UserObm, Domain WHERE" + " userobm_login = ?";
			query += " AND userobm_domain_id = ?";

			ps = con.prepareStatement(query);

			ps.setString(1, login);
			ps.setInt(2, domainId);
			rs = ps.executeQuery();

			if (rs.next()) {
				String passType = rs.getString(1).toLowerCase();
				String pass = rs.getString(2);
				if (passType.equals("crypt")) {
					ret = UnixCrypt.matches(pass, password);
				} else if (passType.equals("md5sum")) {
					ret = pass.equals(helper.getMD5Diggest(password));
				} else {
					ret = pass.equals(password);
				}
			}
		} catch (SQLException e) {
			logger.error("Could not authentificate against OBM", e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return ret;
	}

	public String getObmDomain(String userLogin) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String dn = null;
		try {
			con = obmHelper.getConnection();
			ps = con
					.prepareStatement("SELECT domain_name FROM UserObm, Domain "
							+ "WHERE userobm_domain_id=domain_id AND userobm_login=?");
			ps.setString(1, userLogin);
			rs = ps.executeQuery();
			if (rs.next()) {
				dn = rs.getString(1);
			}
			if (rs.next()) {
				logger.error("the login " + userLogin
						+ " is in several domain (at least " + dn + " and "
						+ rs.getString(1)
						+ ") and you did @domain was not provided in login");
				dn = null;
			}
		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}
		return dn;
	}

	public String getType() {
		return "OBM DB";
	}

}
