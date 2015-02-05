/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
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
package org.obm.provisioning.mailchooser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class LeastDiskUsageImapBackendChooser implements ImapBackendChooser {

	public static final int ID = 98;

	@Inject
	private DatabaseConnectionProvider dbcp;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public int getIdentifier() {
		return ID;
	}

	@Override
	public ObmHost choose(ObmDomain domain) {
		try (Connection con = dbcp.getConnection();
				PreparedStatement ps = con.prepareStatement(
					"SELECT host_id, SUM(COALESCE(userobm_mail_quota_use, 0)) AS disk_usage " +
					"FROM ServiceProperty " +
					"INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id " +
					"INNER JOIN Host ON host_id = CAST(serviceproperty_value AS INTEGER) " +
					"LEFT JOIN UserObm ON userobm_mail_server_id = host_id " +
					"WHERE domainentity_domain_id = ? AND serviceproperty_service = ? AND serviceproperty_property = ? " +
					"GROUP BY host_id " +
					"ORDER BY disk_usage ASC " +
					"LIMIT 1")) {

			int idx = 1;

			ps.setInt(idx++, domain.getId());
			ps.setString(idx++, ServiceProperty.IMAP.getService());
			ps.setString(idx++, ServiceProperty.IMAP.getProperty());

			ResultSet rs = ps.executeQuery();
			rs.next();

			int chosenHostId = rs.getInt("host_id");
			int diskUsage = rs.getInt("disk_usage");
			ObmHost host = domain.getHostById(chosenHostId);

			if (host == null) {
				throw new IllegalStateException(String.format(
						"Host %d doesn't belong to domain %s.", chosenHostId, domain.getName()));
			}

			logger.info(String.format(
					"Host %s (%s) has %d total disk usage and was chosen as the least loaded one.", host.getFqdn(), host.getIp(), diskUsage));

			return host;
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

}
