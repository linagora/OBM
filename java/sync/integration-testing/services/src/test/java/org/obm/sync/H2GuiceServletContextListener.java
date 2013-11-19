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
package org.obm.sync;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContextEvent;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.utils.DBUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

public class H2GuiceServletContextListener extends GuiceServletContextListener {

	public static final String INITIAL_DB_SCRIPT = "dbInitialScript.sql";
	public static final String ADDITIONAL_DB_SCRIPTS_FILE = "additionalDbScripts";

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);

		initializeH2Database();
	}

	private void initializeH2Database() {
		Connection connection = null;
		PreparedStatement ps = null;

		try {
			connection = getH2Connection();
			ps = connection.prepareStatement(readResourceAsString(INITIAL_DB_SCRIPT));

			ps.executeUpdate();
			ps.close();

			for (String script : getAdditionalDBScripts()) {
				ps = connection.prepareStatement(readResourceAsString(script));

				ps.executeUpdate();
				ps.close();
			}
		} catch (Exception e) {
			Throwables.propagate(e);
		} finally {
			DBUtils.cleanup(null, ps, null); // Cannot close the connection as this will kill the DB
		}
	}

	private List<String> getAdditionalDBScripts() throws Exception {
		try {
			return Resources
					.asCharSource(Resources.getResource(ADDITIONAL_DB_SCRIPTS_FILE), Charsets.UTF_8)
					.readLines();
		}
		catch (IllegalArgumentException e) {
			return ImmutableList.of();
		}
	}

	private String readResourceAsString(String resource) throws IOException {
		return Resources.toString(Resources.getResource(resource), Charsets.UTF_8);
	}

	private Connection getH2Connection() throws SQLException {
		return injector.getInstance(DatabaseConnectionProvider.class).getConnection();
	}

}
