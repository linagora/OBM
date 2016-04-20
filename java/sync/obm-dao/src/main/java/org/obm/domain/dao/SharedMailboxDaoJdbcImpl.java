/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2016  Linagora
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

import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.sync.host.ObmHost;
import org.obm.utils.ObmHelper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.mailshare.SharedMailbox;

@Singleton
public class SharedMailboxDaoJdbcImpl implements SharedMailboxDao {

	private static final String MAILSHARE_FIELDS = 
		"mailshare_id," +
		"mailshare_domain_id," +
		"mailshare_name," +
		"mailshare_archive," +
		"mailshare_quota," +
		"mailshare_mail_server_id," +
		"mailshare_delegation," +
		"mailshare_description," +
		"mailshare_email, " +
		"host_name, " +
		"host_fqdn, " +
		"host_ip, " +
		"host_domain_id";

	private final ObmHelper obmHelper;

	@Inject
	public SharedMailboxDaoJdbcImpl(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	@Override
	public SharedMailbox findSharedMailboxById(int id, ObmDomain domain) throws DaoException {
		String uq = "SELECT " + MAILSHARE_FIELDS
				+ " FROM Mailshare "
				+ "LEFT JOIN Host ON host_id = mailshare_mail_server_id "
				+ "WHERE mailshare_id=? ";
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(uq)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return createSharedMailboxFromResultSet(domain, rs);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
		return null;
	}

	@Override
	public SharedMailbox findSharedMailboxByName(String name, ObmDomain domain) throws DaoException {
		String uq = "SELECT " + MAILSHARE_FIELDS
				+ " FROM Mailshare "
				+ "LEFT JOIN Host ON host_id = mailshare_mail_server_id "
				+ "WHERE mailshare_name=? ";
		try (Connection con = obmHelper.getConnection();
				PreparedStatement ps = con.prepareStatement(uq)) {
			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return createSharedMailboxFromResultSet(domain, rs);
			}
		} catch (SQLException e) {
			throw new DaoException(e);
		}
		return null;
	}
	
	private SharedMailbox createSharedMailboxFromResultSet(ObmDomain domain, ResultSet rs) throws SQLException {
		return SharedMailbox.builder()
				.id(rs.getInt("mailshare_id"))
				.domain(domain)
				.name(rs.getString("mailshare_name"))
				.archive(rs.getBoolean("mailshare_archive"))
				.quota(rs.getInt("mailshare_quota"))
				.server(hostFromCursor(rs))
				.delegation(rs.getString("mailshare_delegation"))
				.description(rs.getString("mailshare_description"))
				.email(rs.getString("mailshare_email"))
				.build();
	}

	private ObmHost hostFromCursor(ResultSet rs) throws SQLException {
		int id = rs.getInt("mailshare_mail_server_id");

		if (rs.wasNull()) {
			return null;
		}

		return ObmHost
				.builder()
				.id(id)
				.name(rs.getString("host_name"))
				.fqdn(rs.getString("host_fqdn"))
				.ip(rs.getString("host_ip"))
				.domainId(rs.getInt("host_domain_id"))
				.build();
	}
}
