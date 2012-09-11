/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common.trust;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

/**
 * A Dao {@link Class} used to manipulate the {@code Trust} token in the database.
 */
@Singleton
public class TrustTokenDao {
	private static final Logger logger = LoggerFactory.getLogger(TrustTokenDao.class);
	private final ObmHelper obmHelper;

	/**
	 * Builds a new {@link TrustTokenDao}.
	 * 
	 * @param obmHelper The {@link ObmHelper} instance to access the DB.
	 */
	@Inject
	private TrustTokenDao(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}

	/**
	 * Retrieves the trust {@code token} from the database.
	 * 
	 * @return The {@link TrustToken} if it exists or {@code null} if no token is found.
	 */
	public TrustToken getTrustToken() {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "SELECT token, time_created FROM TrustToken LIMIT 1";

		try {
			con = obmHelper.getConnection();
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();

			if (rs.next()) {
				return new TrustToken(rs.getString("token"), rs.getTimestamp("time_created").getTime());
			}
		}
		catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			obmHelper.cleanup(con, ps, rs);
		}

		return null;
	}

	/**
	 * Updates the trust {@code token} in the database.
	 */
	public void updateTrustToken() {
		Connection con = null;
		PreparedStatement ps = null;
		String deleteQuery = "DELETE FROM TrustToken";
		String insertQuery = "INSERT INTO TrustToken (token) VALUES (?)";

		if (logger.isDebugEnabled())
			logger.debug("Updating trust token in database.");

		try {
			String newToken = newToken();

			con = obmHelper.getConnection();
			ps = con.prepareStatement(deleteQuery);
			ps.executeUpdate();
			obmHelper.cleanup(null, ps, null);

			ps = con.prepareStatement(insertQuery);
			ps.setString(1, newToken);
			ps.executeUpdate();
		}
		catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
		finally {
			obmHelper.cleanup(con, ps, null);
		}
	}

	/**
	 * Builds and returns a new trust {@code token}, as a {@link String}.
	 * 
	 * @return The created trust token.
	 */
	private String newToken() {
		return UUID.randomUUID().toString();
	}
}
