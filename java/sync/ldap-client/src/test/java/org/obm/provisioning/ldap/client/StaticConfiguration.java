/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.provisioning.ldap.client;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;

import com.google.common.base.Throwables;

public class StaticConfiguration implements Configuration {

	@Override
	public int maxRequests() {
		return 0;
	}

	@Override
	public Dn getBindDn() {
		try {
			return new Dn("cn=directory manager");
		} catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getBindPassword() {
		return "secret";
	}

	@Override
	public Dn getUserBaseDn(LdapDomain domain) {
		try {
			return new Dn(String.format("ou=users,dc=%s,dc=local", domain.get()));
		} catch (LdapInvalidDnException e) {
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
			return new Dn("ou=groups,dc=test.obm.org,dc=local");
		} catch (LdapInvalidDnException e) {
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
