/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.locator.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.obm.configuration.DomainConfiguration;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.base.EmailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocatorDbHelper {

	private static final Logger logger = LoggerFactory
			.getLogger(LocatorDbHelper.class);

	private final DatabaseConnectionProvider dbcp;
	private final static int MAX_CHAR_FOR_CAST = 8;

	private final DomainConfiguration domainConfiguration;
	
	@Inject
	protected LocatorDbHelper(DatabaseConnectionProvider dbcp, DomainConfiguration configurationService) {
		this.dbcp = dbcp;
		this.domainConfiguration = configurationService;
	}

	public Set<String> findImapBackendHost(EmailAddress loginAtDomain) {
		String query = "SELECT host_ip "
				+ "FROM Host "
				+ "INNER JOIN UserObm ON userobm_mail_server_id = host_id "
				+ "INNER JOIN Domain ON domain_id = userobm_domain_id "
				+ "WHERE domain_name = ? AND userobm_login = ?";
		ImmutableSet.Builder<String> ips = ImmutableSet.builder();

		try (Connection con = dbcp.getConnection(); PreparedStatement ps = con.prepareStatement(query)) {
			ps.setString(1, loginAtDomain.getDomain().get());
			ps.setString(2, loginAtDomain.getLogin().get());

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					ips.add(rs.getString("host_ip"));
				}
			}
		} catch (SQLException e) {
			logger.error("Failed to query IMAP backend for user " + loginAtDomain, e);
		}

		return ips.build();
	}

	/**
	 * Returns the ips of the hosts with the given service/property in the users
	 * domain.
	 * 
	 * @param loginAtDomain
	 * @param service
	 * @param prop
	 * @return
	 */
	public Set<String> findDomainHost(String loginAtDomain, String service, String prop) {
		ImmutableSet.Builder<String> ret = ImmutableSet.builder();
		String q = "SELECT host_ip "
				+ "FROM Domain "
				+ "INNER JOIN DomainEntity ON domainentity_domain_id=domain_id "
				+ "INNER JOIN ServiceProperty ON serviceproperty_entity_id=domainentity_entity_id "
				+ "INNER JOIN Host ON CAST(host_id as CHAR(" + MAX_CHAR_FOR_CAST + ")) = serviceproperty_value "
				+ "WHERE domain_name LIKE ? "
				+ "AND serviceproperty_service=? "
				+ "AND serviceproperty_property=? "
				+ "ORDER BY host_ip ASC";

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String domain = getDomainAsJdbcString(loginAtDomain);
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(q);
			ps.setString(1, domain);
			ps.setString(2, service);
			ps.setString(3, prop);
			rs = ps.executeQuery();

			while (rs.next()) {
				ret.add(rs.getString(1));
			}

		} catch (SQLException se) {
			logger.error(se.getMessage(), se);
		} finally {
			JDBCUtils.cleanup(con, ps, rs);
		}

		return ret.build();
	}

	private String getDomainAsJdbcString(String loginAtDomain) {
		int idx = loginAtDomain.indexOf("@");
		String domain = loginAtDomain.substring(idx + 1);
		if (domain.equals(domainConfiguration.getGlobalDomain())) {
			return "%";
		}
		return domain;
	}
	
}
