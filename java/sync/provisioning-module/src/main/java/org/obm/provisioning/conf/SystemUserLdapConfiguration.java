/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.provisioning.conf;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.obm.provisioning.ldap.client.Configuration;
import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;

import com.google.common.base.Throwables;

import fr.aliacom.obm.common.system.ObmSystemUser;

public class SystemUserLdapConfiguration implements Configuration {

	private final ObmSystemUser ldapSystemUser;

	public SystemUserLdapConfiguration(ObmSystemUser systemUser) {
		this.ldapSystemUser = systemUser;
	}

	@Override
	public int maxRequests() {
		return 1;
	}

	@Override
	public LdapConnectionConfig getNetworkConfiguration() {
		LdapConnectionConfig config = new LdapConnectionConfig();

		config.setLdapHost("localhost"); // TODO Well... Fix this
		config.setLdapPort(389);

		return config;
	}

	@Override
	public Dn getBindDn() {
		try {
			return new Dn(String.format("uid=%s,ou=sysusers,dc=local", ldapSystemUser.getLogin()));
		}
		catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getBindPassword() {
		return ldapSystemUser.getPassword();
	}

	@Override
	public Dn getUserBaseDn(LdapDomain domain) {
		try {
			return new Dn(String.format("ou=users,dc=%s,dc=local", domain.get()));
		}
		catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String buildUserFilter(LdapUser.Uid userId) {
		return "(uid=" + userId.get() + ")";
	}

	@Override
	public SearchScope getUserSearchScope() {
		return SearchScope.ONELEVEL;
	}

	@Override
	public Dn getGroupBaseDn() {
		try {
			return new Dn("ou=groups,dc=test.obm.org,dc=local"); // TODO: This si dependant on the Domain
		}
		catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String buildGroupFilter(LdapGroup.Cn groupCn) {
		return "(cn=" + groupCn.get() + ")";
	}

	@Override
	public SearchScope getGroupSearchScope() {
		return SearchScope.ONELEVEL;
	}

}
