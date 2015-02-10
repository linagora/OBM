/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2015 Linagora
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
package org.obm.provisioning.ldap.client.bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.Group;
import org.obm.provisioning.ldap.client.Configuration;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership.Builder;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

@RunWith(GuiceRunner.class)
@GuiceModule(LdapUserMembershipTest.Env.class)
public class LdapUserMembershipTest {

	public static class Env extends AbstractModule {

		private IMocksControl control = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);
			bind(Configuration.class).toInstance(control.createMock(Configuration.class));
		}
		
	}

	@Inject
	private IMocksControl control;
	@Inject
	private Configuration configuration;
	@Inject
	private Provider<Builder> builderProvider;

	@After
	public void tearDown() {
		control.verify();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildShouldEnsureMemberUidIsNotNull() {
		control.replay();

		builderProvider.get().build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildShouldEnsureDomainIsNotNull() {
		control.replay();

		builderProvider.get().memberUid("memberUid").build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildShouldEnsureMailboxIsNotNull() {
		control.replay();

		builderProvider.get().memberUid("memberUid").domain(LdapDomain.valueOf("domain")).build();
	}

	@Test
	public void testBuildShouldSucceed() throws Exception {
		LdapDomain domain = LdapDomain.valueOf("domain");

		expect(configuration.getUserBaseDn(domain)).andReturn(new org.apache.directory.api.ldap.model.name.Dn("dc=local"));
		control.replay();

		LdapUserMembership membership = builderProvider
				.get()
				.memberUid("memberUid")
				.domain(domain)
				.mailBox("mailbox")
				.targetGroupHasEmail(true)
				.build();

		assertThat(membership.getMemberUid()).isEqualTo("memberUid");
		assertThat(membership.getMailBox()).isEqualTo("mailbox");
		assertThat(membership.getMember()).isEqualTo("uid=memberUid,dc=local");
		assertThat(membership.isTargetGroupHasEmail()).isTrue();
	}

	@Test
	public void testBuildShouldConsiderTargetGroupHasEmailAsFalseByDefault() throws Exception {
		LdapDomain domain = LdapDomain.valueOf("domain");

		expect(configuration.getUserBaseDn(domain)).andReturn(new org.apache.directory.api.ldap.model.name.Dn("dc=local"));
		control.replay();

		LdapUserMembership membership = builderProvider
				.get()
				.memberUid("memberUid")
				.domain(domain)
				.mailBox("mailbox")
				.build();

		assertThat(membership.getMemberUid()).isEqualTo("memberUid");
		assertThat(membership.getMailBox()).isEqualTo("mailbox");
		assertThat(membership.getMember()).isEqualTo("uid=memberUid,dc=local");
		assertThat(membership.isTargetGroupHasEmail()).isFalse();
	}

	@Test
	public void testBuildForGroupShouldEnableGroupEmailIfGroupHasEmailAddress() throws Exception {
		LdapDomain domain = LdapDomain.valueOf("domain");
		Group group = Group
				.builder()
				.uid(Group.Id.valueOf(1))
				.email("groupEmail@domain")
				.build();

		expect(configuration.getUserBaseDn(domain)).andReturn(new org.apache.directory.api.ldap.model.name.Dn("dc=local"));
		control.replay();

		LdapUserMembership membership = builderProvider
				.get()
				.memberUid("memberUid")
				.domain(domain)
				.mailBox("mailbox")
				.forGroup(group)
				.build();

		assertThat(membership.getMemberUid()).isEqualTo("memberUid");
		assertThat(membership.getMailBox()).isEqualTo("mailbox");
		assertThat(membership.getMember()).isEqualTo("uid=memberUid,dc=local");
		assertThat(membership.isTargetGroupHasEmail()).isTrue();
	}

	@Test
	public void testBuildForGroupShouldDisableGroupEmailIfGroupHasNoEmailAddress() throws Exception {
		LdapDomain domain = LdapDomain.valueOf("domain");
		Group group = Group
				.builder()
				.uid(Group.Id.valueOf(1))
				.build();

		expect(configuration.getUserBaseDn(domain)).andReturn(new org.apache.directory.api.ldap.model.name.Dn("dc=local"));
		control.replay();

		LdapUserMembership membership = builderProvider
				.get()
				.memberUid("memberUid")
				.domain(domain)
				.mailBox("mailbox")
				.forGroup(group)
				.build();

		assertThat(membership.getMemberUid()).isEqualTo("memberUid");
		assertThat(membership.getMailBox()).isEqualTo("mailbox");
		assertThat(membership.getMember()).isEqualTo("uid=memberUid,dc=local");
		assertThat(membership.isTargetGroupHasEmail()).isFalse();
	}

}
