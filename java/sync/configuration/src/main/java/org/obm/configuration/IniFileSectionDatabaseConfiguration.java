/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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
package org.obm.configuration;

import org.obm.configuration.utils.IniFile;

import com.google.common.base.Objects;

public class IniFileSectionDatabaseConfiguration implements DatabaseConfiguration {

	private static final int DB_MAX_POOL_SIZE_DEFAULT = 10;

	private final IniFile iniFile;
	private final String section;

	public IniFileSectionDatabaseConfiguration(IniFile iniFile, String section) {
		this.iniFile = iniFile;
		this.section = section;
	}

	@Override
	public Integer getDatabaseMinConnectionPoolSize() {
		return null;
	}

	@Override
	public Integer getDatabaseMaxConnectionPoolSize() {
		return iniFile.getIniIntegerValue(section, DB_MAX_POOL_SIZE_KEY, DB_MAX_POOL_SIZE_DEFAULT);
	}

	@Override
	public DatabaseFlavour getDatabaseSystem() {
		return DatabaseFlavour.valueOf(iniFile.getIniStringValue(section, DB_TYPE_KEY).trim());
	}

	@Override
	public String getDatabaseName() {
		return iniFile.getIniStringValue(section, DB_NAME_KEY);
	}

	@Override
	public String getDatabaseHost() {
		return iniFile.getIniStringValue(section, DB_HOST_KEY);
	}

	@Override
	public Integer getDatabasePort() {
		return iniFile.getIniIntegerValue(section, DB_PORT_KEY, null);
	}

	@Override
	public String getDatabaseLogin() {
		return iniFile.getIniStringValue(section, DB_USER_KEY);
	}

	@Override
	public String getDatabasePassword() {
		return iniFile.getIniStringValue(section, DB_PASSWORD_KEY);
	}

	@Override
	public boolean isPostgresSSLEnabled() {
		return iniFile.getIniBooleanValue(section, DB_PG_SSL, false);
	}

	@Override
	public boolean isPostgresSSLNonValidating() {
		return iniFile.getIniBooleanValue(section, DB_PG_SSL_NON_VALIDATING, false);
	}

	@Override
	public String getJdbcOptions() {
		return NO_JDBC_OPTION;
	}

	@Override
	public boolean isReadOnly() {
		return iniFile.getIniBooleanValue(section, DB_READONLY_KEY, false);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getDatabaseHost(), getDatabasePort(), getDatabaseSystem(), getDatabaseName(),
				getDatabaseLogin(), isReadOnly(), getDatabaseMinConnectionPoolSize(), getDatabaseMaxConnectionPoolSize(), getJdbcOptions());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IniFileSectionDatabaseConfiguration) {
			IniFileSectionDatabaseConfiguration other = (IniFileSectionDatabaseConfiguration) obj;

			return Objects.equal(getDatabaseHost(), other.getDatabaseHost())
					&& Objects.equal(getDatabasePort(), other.getDatabasePort())
					&& Objects.equal(getDatabaseSystem(), other.getDatabaseSystem())
					&& Objects.equal(getDatabaseName(), other.getDatabaseName())
					&& Objects.equal(getDatabaseLogin(), other.getDatabaseLogin())
					&& Objects.equal(isReadOnly(), other.isReadOnly())
					&& Objects.equal(getDatabaseMinConnectionPoolSize(), other.getDatabaseMinConnectionPoolSize())
					&& Objects.equal(getDatabaseMaxConnectionPoolSize(), other.getDatabaseMaxConnectionPoolSize())
					&& Objects.equal(getJdbcOptions(), other.getJdbcOptions());
		}

		return false;
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(getClass())
				.add("host", getDatabaseHost())
				.add("port", getDatabasePort())
				.add("dbType", getDatabaseSystem())
				.add("dbName", getDatabaseName())
				.add("login", getDatabaseLogin())
				.add("readOnly", isReadOnly())
				.add("minPoolSize", getDatabaseMinConnectionPoolSize())
				.add("maxPoolSize", getDatabaseMaxConnectionPoolSize())
				.add("jdbcOptions", getJdbcOptions())
				.toString();
	}

}
