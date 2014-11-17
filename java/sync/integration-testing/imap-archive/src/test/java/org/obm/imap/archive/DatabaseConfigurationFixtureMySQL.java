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

package org.obm.imap.archive;

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;

public class DatabaseConfigurationFixtureMySQL implements DatabaseConfiguration {


	@Override
	public Integer getDatabaseMaxConnectionPoolSize() {
		return 10;
	}

	@Override
	public DatabaseFlavour getDatabaseSystem() {
		return DatabaseFlavour.MYSQL;
	}

	@Override
	public String getDatabaseName() {
		return "obm";
	}

	@Override
	public String getDatabaseHost() {
		return "localhost.localdomain";
	}

	@Override
	public String getDatabaseLogin() {
		return "obm";
	}

	@Override
	public String getDatabasePassword() {
		return "obm";
	}

    @Override
    public boolean isPostgresSSLEnabled() {
        return true;
    }

    @Override
    public boolean isPostgresSSLNonValidating() {
        return true;
    }
    
    @Override
    public String getJdbcOptions() {
    	return NO_JDBC_OPTION;
    }
    
    @Override
    public Integer getDatabaseMinConnectionPoolSize() {
    	return null;
    }
}
