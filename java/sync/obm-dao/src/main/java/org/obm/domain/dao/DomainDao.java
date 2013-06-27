/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.utils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class DomainDao {

	private static final String DOMAIN_FIELDS = "domain_name, domain_id, domain_uuid, domain_label, domain_alias";
	
	private static final Logger logger = LoggerFactory.getLogger(DomainDao.class);
	private final DatabaseConnectionProvider dbcp;

	@Inject
	private DomainDao(DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
	}
	
	public ObmDomain findDomainByName(String domainName) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String uq = "SELECT " + DOMAIN_FIELDS + " FROM Domain WHERE domain_name = ? "
				+ " OR domain_alias = ? OR domain_alias LIKE ? OR domain_alias LIKE ? OR domain_alias LIKE ? ";
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(uq);
			ps.setString(1, domainName);
			ps.setString(2, domainName);
			ps.setString(3, domainName + "\r\n%");
			ps.setString(4, "%\r\n" + domainName + "\r\n%");
			ps.setString(5, "%\r\n" + domainName);
			rs = ps.executeQuery();

			if (rs.next()) {
				return domainFromCursor(rs);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DBUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	public ObmDomain findDomainByUuid(ObmDomainUuid uuid) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String uq = "SELECT " + DOMAIN_FIELDS + " FROM Domain WHERE domain_uuid = ?";
		try {
			con = dbcp.getConnection();
			ps = con.prepareStatement(uq);

			ps.setString(1, uuid.get());
			rs = ps.executeQuery();

			if (rs.next()) {
				return domainFromCursor(rs);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DBUtils.cleanup(con, ps, rs);
		}

		return null;
	}

	public ObmDomain create(ObmDomain domain) {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String query = "INSERT INTO Domain (domain_uuid, domain_name, domain_label, domain_alias)"
				+ " VALUES (?, ?, ?, ?)";
			con = dbcp.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, domain.getUuid().get());
			ps.setString(2, domain.getName());
			ps.setString(3, domain.getLabel());
			if (!domain.getAliases().isEmpty()) {
				ps.setString(4, Joiner.on("\r\n").join(domain.getAliases()));
			} else {
				ps.setNull(4, Types.LONGVARCHAR);
			}
			ps.executeUpdate();
			int domainId = dbcp.lastInsertId(con);
			return ObmDomain.builder().from(domain).id(domainId).build();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DBUtils.cleanup(con, ps, rs);
		}
		return null;
	}
	
	public List<ObmDomain> list() {
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			String query = "SELECT " + DOMAIN_FIELDS + " FROM Domain";
			con = dbcp.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(query);
			List<ObmDomain> domains = Lists.newArrayList();

			while (rs.next()) {
				domains.add(domainFromCursor(rs));
			}

			return domains;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			DBUtils.cleanup(con, statement, rs);
		}
		return null;
	}
	
	private Iterable<String> aliasToIterable(String aliases) {
		return aliases == null ? ImmutableSet.<String>of() : Splitter.on("\r\n").split(aliases);
	}
	
	private ObmDomain domainFromCursor(ResultSet rs) throws SQLException {
		return ObmDomain
				.builder()
				.id(rs.getInt("domain_id"))
				.uuid(ObmDomainUuid.of(rs.getString("domain_uuid")))
				.name(rs.getString("domain_name"))
				.label(rs.getString("domain_label"))
				.aliases(aliasToIterable(rs.getString("domain_alias")))
				.build();
	}
	
}
