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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ldap.client.Connection;
import org.obm.provisioning.ldap.client.LdapManagerImpl;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.inject.Provider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

@RunWith(SlowGuiceRunner.class)
public class LdapManagerImplTest {

	IMocksControl control;

	@Before
	public void setup() {
		control = createControl();
	}

	@Test
	public void testCreate() {
		Connection mockConnection = control.createMock(Connection.class);
		Provider<LdapUser.Builder> mockUserBuilderProvider = control.createMock(Provider.class);
		LdapUser.Builder mockLdapUserBuilder = control.createMock(LdapUser.Builder.class);
		LdapUser mockLdapUser = control.createMock(LdapUser.class);
		
		ObmUser obmUser = ObmUser.builder()
				.uid(1895)
				.login("richard.sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.domain(ObmDomain.builder().host(ServiceProperty.builder().build(), ObmHost.builder().build()).build())
				.build();

		expect(mockUserBuilderProvider.get()).andReturn(mockLdapUserBuilder);
		expect(mockLdapUserBuilder.fromObmUser(obmUser)).andReturn(mockLdapUserBuilder);
		expect(mockLdapUserBuilder.build()).andReturn(mockLdapUser);
		mockConnection.createUser(mockLdapUser);
		expectLastCall().once();

		control.replay();
		LdapManagerImpl ldapManager = new LdapManagerImpl(mockConnection, mockUserBuilderProvider);
		ldapManager.createUser(obmUser);
		control.verify();
	}
}
