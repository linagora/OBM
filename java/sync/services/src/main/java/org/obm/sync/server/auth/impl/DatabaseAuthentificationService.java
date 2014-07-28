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
package org.obm.sync.server.auth.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.Credentials;
import org.obm.sync.server.auth.IAuthentificationService;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.ldap.UnixCrypt;
import fr.aliacom.obm.utils.HelperService;
import fr.aliacom.obm.utils.ObmHelper;

/**
 * Authentification against the OBM database
 */
@Singleton
public class DatabaseAuthentificationService implements IAuthentificationService {

	private final ObmHelper obmHelper;
	private final HelperService helperService;
	private final DomainService domainService;

	@Inject
	private DatabaseAuthentificationService(ObmHelper obmHelper, HelperService helperService, DomainService domainService) {
		this.obmHelper = obmHelper;
		this.helperService = helperService;
		this.domainService = domainService;
	}
	
	@Override
	public boolean doAuth(Credentials credentials) throws AuthFault {
		ObmDomain obmDomain = domainService.findDomainByName(credentials.getLogin().getDomain());

		if (obmDomain == null) {
			throw new RuntimeException("domain not found");
		}

		checkPassword(credentials.getLogin().getLogin(), credentials.getPassword(), obmDomain.getId(), credentials.isPasswordHashed());
		return true;
	}

	private void checkPassword(String login, String password, int domainId, boolean isPasswordHashed) throws AuthFault {
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
				} else if (passType.equals("md5sum") && !isPasswordHashed) {
					ret = pass.equals(helperService.getMD5Diggest(password));
				} else {
					ret = pass.equals(password);
				}
			}
		} catch (SQLException e) {
			Throwables.propagate(e);
		} finally {
			obmHelper.cleanup(con, ps, rs);
		}

		if (!ret) {
			throw new AuthFault("Bad credentials for user '" + login + "'.");
		}
	}

	@Override
	public String getType() {
		return "OBM DB";
	}

}
