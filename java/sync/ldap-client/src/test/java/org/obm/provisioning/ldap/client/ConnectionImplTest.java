/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.provisioning.ldap.client.bean.LdapUser.Uid;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership;
import org.obm.provisioning.ldap.client.exception.ConnectionException;
import org.obm.provisioning.ldap.client.exception.LdapException;
import org.opends.messages.Message;
import org.opends.server.backends.MemoryBackend;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DirectoryServer;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.types.DN;
import org.opends.server.types.Entry;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.types.ResultCode;
import org.opends.server.util.LDIFReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

@GuiceModule(EmbeddedLdapModule.class)
@RunWith(GuiceRunner.class)
public class ConnectionImplTest {

	@Inject ConnectionImpl.Factory connectionFactory;
	@Inject DirectoryServer directoryServer;
	@Inject Provider<LdapGroup.Builder> groupBuilderProvider;
	@Inject Provider<LdapUser.Builder> userBuilderProvider;
	@Inject Provider<LdapUser.Builder> userBuilderProvider2;
	@Inject Provider<LdapUserMembership.Builder> groupMemberShipProvider;
	private ConnectionImpl connection;
	private LdapDomain ldapDomain;
	
	@Before
	public void setup() throws Exception {
		directoryServer.startServer();
		injectBootstrapData();
		
		connection = connectionFactory.create(getNetworkConfiguration());
		
		ldapDomain = LdapDomain.valueOf("test.obm.org");
	}

	private LdapConnectionConfig getNetworkConfiguration() {
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost("localhost");
		config.setLdapPort(33389);
		config.setUseSsl(false);
		return config;
	}
	
	private void injectBootstrapData()throws Exception  {
		initializeBackend();
		loadLdifBootstrap();
	}

	private void initializeBackend() throws Exception {
		MemoryBackend memoryBackend = new MemoryBackend();
		memoryBackend.setBackendID("test");
		memoryBackend.setBaseDNs(new DN[] {DN.decode("dc=local")});
		memoryBackend.initializeBackend();
		DirectoryServer.registerBackend(memoryBackend);
	}

	private void loadLdifBootstrap() throws Exception {
		LDIFImportConfig ldifImportConfig = new LDIFImportConfig(ClassLoader.getSystemClassLoader().getResource("bootstrap.ldif").getPath());
        LDIFReader reader = new LDIFReader(ldifImportConfig);
        Entry entry;
        while ((entry = reader.readEntry()) != null) {
        	addEntry(entry);
        }
	}
	
	private void addEntry(Entry entry) {
		InternalClientConnection conn = InternalClientConnection.getRootConnection();
		AddOperation addOperation = conn.processAdd(entry.getDN(), entry.getObjectClasses(), entry.getUserAttributes(),
				entry.getOperationalAttributes());
		
		Preconditions.checkState(addOperation.getResultCode() == ResultCode.SUCCESS, "Unable to add bootstrap entry " + entry.getDN());
	}

	@After
	public void tearDown() {
		DirectoryServer.shutDown(ConnectionImplTest.class.toString(), Message.EMPTY);
	}
	
	@Test
	public void testCreateGroup() throws Exception {
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		
		assertThat(connection.getGroupDnFromGroupCn(ldapGroup.getCn(), ldapDomain)
			.equals(new Dn("cn=group1,ou=groups,dc=test.obm.org,dc=local"))).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
	}
	
	@Test
	public void testDeleteGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		
		// In order to be sure that group is really inserted
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		assertThat(connection.getGroupDnFromGroupCn(groupCn, ldapDomain)
			.equals(new Dn("cn=group1,ou=groups,dc=test.obm.org,dc=local"))).isTrue();
		
		connection.deleteGroup(groupCn, ldapDomain);
		try {
			connection.getGroupDnFromGroupCn(groupCn, ldapDomain);
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testCreateUser() throws Exception {
		LdapUser ldapUser = userBuilderProvider.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid(LdapUser.Uid.valueOf("test"))
				.uidNumber(1008)
				.gidNumber(1000)
				.loginShell("/bin/bash")
				.cn("prenom nom")
				.displayName("prenom nom")
				.sn("nom")
				.givenName("prenom")
				.homeDirectory("/home/test")
				.userPassword("password")
				.webAccess("REJECT")
				.mailBox("test@test.obm.org")
				.mailBoxServer("lmtp:127.0.0.1:24")
				.mailAccess("PERMIT")
				.mail("test@test.obm.org")
				.mailAlias(ImmutableSet.of("alias1", "alias2"))
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		
		connection.createUser(ldapUser);
		
		assertThat(connection.getUserDnFromUserId(ldapUser.getUid(), LdapDomain.valueOf("test.obm.org"))
			.equals(new Dn("uid=test,ou=users,dc=test.obm.org,dc=local"))).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getUserEntry(LdapUser.Uid.valueOf("test"), ldapDomain);

		assertThat(entry.get(new AttributeType("uid")).getString()).isEqualTo("test");
		assertThat(entry.get(new AttributeType("uidnumber")).getString()).isEqualTo("1008");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1000");
		assertThat(entry.get(new AttributeType("loginshell")).getString()).isEqualTo("/bin/bash");
		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("prenom nom");
		assertThat(entry.get(new AttributeType("displayname")).getString()).isEqualTo("prenom nom");
		assertThat(entry.get(new AttributeType("sn")).getString()).isEqualTo("nom");
		assertThat(entry.get(new AttributeType("givenname")).getString()).isEqualTo("prenom");
		assertThat(entry.get(new AttributeType("homedirectory")).getString()).isEqualTo("/home/test");
		assertThat(entry.get(new AttributeType("userpassword")).getBytes()).isNotNull();
		assertThat(entry.get(new AttributeType("webaccess")).getString()).isEqualTo("REJECT");
		assertThat(entry.get(new AttributeType("mailbox")).getString()).isEqualTo("test@test.obm.org");
		assertThat(entry.get(new AttributeType("mailboxserver")).getString()).isEqualTo("lmtp:127.0.0.1:24");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("test@test.obm.org");
		assertThat(entry.get(new AttributeType("mailalias")).contains("alias1", "alias2")).isTrue();
		assertThat(entry.get(new AttributeType("hiddenuser")).getString()).isEqualTo("FALSE");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
	}

	@Test
	public void testModifyUser() throws Exception {
		LdapUser ldapUser = userBuilderProvider.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid(LdapUser.Uid.valueOf("test"))
				.uidNumber(1008)
				.gidNumber(1000)
				.loginShell("/bin/bash")
				.cn("prenom nom")
				.displayName("prenom nom")
				.sn("nom")
				.givenName("prenom")
				.homeDirectory("/home/test")
				.userPassword("password")
				.webAccess("REJECT")
				.mailBox("test@test.obm.org")
				.mailBoxServer("lmtp:127.0.0.1:24")
				.mailAccess("PERMIT")
				.mail("test@test.obm.org")
				.mailAlias(ImmutableSet.of("alias1", "alias2"))
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		
		connection.createUser(ldapUser);
		
		LdapUser newLdapUser = userBuilderProvider2.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid(LdapUser.Uid.valueOf("test"))
				.uidNumber(10082)
				.gidNumber(10002)
				.loginShell("/bin/bash")
				.cn("prenom2 nom2")
				.displayName("prenom2 nom2")
				.sn("nom2")
				.givenName("prenom2")
				.homeDirectory("/home/test2")
				.userPassword("password2")
				.webAccess("REJECT")
				.mailBox("test2@test.obm.org")
				.mailBoxServer("lmtp:127.0.0.1:24")
				.mailAccess("PERMIT")
				.mail("test2@test.obm.org")
				.mailAlias(ImmutableSet.of("alias0", "alias2", "alias3"))
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		
		connection.modifyUser(ldapUser.getUid(), LdapDomain.valueOf("test.obm.org"), newLdapUser.buildDiffModifications(ldapUser));
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getUserEntry(LdapUser.Uid.valueOf("test"), ldapDomain);

		assertThat(entry.get(new AttributeType("uid")).getString()).isEqualTo("test");
		assertThat(entry.get(new AttributeType("uidnumber")).getString()).isEqualTo("10082");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("10002");
		assertThat(entry.get(new AttributeType("loginshell")).getString()).isEqualTo("/bin/bash");
		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("prenom2 nom2");
		assertThat(entry.get(new AttributeType("displayname")).getString()).isEqualTo("prenom2 nom2");
		assertThat(entry.get(new AttributeType("sn")).getString()).isEqualTo("nom2");
		assertThat(entry.get(new AttributeType("givenname")).getString()).isEqualTo("prenom2");
		assertThat(entry.get(new AttributeType("homedirectory")).getString()).isEqualTo("/home/test2");
		assertThat(entry.get(new AttributeType("userpassword")).getBytes()).isNotNull();
		assertThat(entry.get(new AttributeType("webaccess")).getString()).isEqualTo("REJECT");
		assertThat(entry.get(new AttributeType("mailbox")).getString()).isEqualTo("test2@test.obm.org");
		assertThat(entry.get(new AttributeType("mailboxserver")).getString()).isEqualTo("lmtp:127.0.0.1:24");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("test2@test.obm.org");
		assertThat(entry.get(new AttributeType("mailalias")).contains("alias0", "alias2", "alias3")).isTrue();
		assertThat(entry.get(new AttributeType("mailalias")).contains("alias1")).isFalse();
		assertThat(entry.get(new AttributeType("hiddenuser")).getString()).isEqualTo("FALSE");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
	}
	
	@Test
	public void testDeleteUser() throws Exception {
		LdapUser ldapUser = userBuilderProvider.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid(LdapUser.Uid.valueOf("test"))
				.uidNumber(1008)
				.gidNumber(1000)
				.loginShell("/bin/bash")
				.cn("prenom nom")
				.displayName("prenom nom")
				.sn("nom")
				.givenName("prenom")
				.homeDirectory("/home/test")
				.userPassword("password")
				.webAccess("REJECT")
				.mailBox("test@test.obm.org")
				.mailBoxServer("lmtp:127.0.0.1:24")
				.mailAccess("PERMIT")
				.mail("test@test.obm.org")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		
		connection.createUser(ldapUser);
		
		// In order to be sure that user is really inserted
		Uid userId = ldapUser.getUid();
		assertThat(connection.getUserDnFromUserId(userId, LdapDomain.valueOf("test.obm.org"))
			.equals(new Dn("uid=test,ou=users,dc=test.obm.org,dc=local"))).isTrue();
		
		connection.deleteUser(userId, LdapDomain.valueOf("test.obm.org"));
		try {
			connection.getUserDnFromUserId(userId, LdapDomain.valueOf("test.obm.org"));
		} catch (IllegalStateException e) {
		}
	}
	
	@Test
	public void testAddUserToGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUserToGroup(userMembership, groupCn, ldapDomain);
		
		Attribute attribute = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain)
			.get(new AttributeType("member"));
		assertThat(attribute.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).getString()).isEqualTo("uid=test,ou=users,dc=test.obm.org,dc=local");
	}
	
	@Test
	public void testAddUsersToEmptyGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test,ou=users,dc=test.obm.org,dc=local",
				"uid=test2,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testAddUsersToGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUserToGroup(userMembership, groupCn, ldapDomain);
		
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership3 = groupMemberShipProvider.get()
				.memberUid("test3")
				.mailBox("test3@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership2, userMembership3), groupCn, ldapDomain);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test,ou=users,dc=test.obm.org,dc=local",
				"uid=test2,ou=users,dc=test.obm.org,dc=local",
				"uid=test3,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testRemoveUserToGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = 	connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUserFromGroup(userMembership, groupCn, ldapDomain);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test2,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testRemoveUsersToGroup() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = 	connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUsersFromGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member"))).isNull();
	}
	
	@Test
	public void testRemoveUsersToGroupOnlyOneRemaining() throws Exception {
		
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Cn groupCn = ldapGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership3 = groupMemberShipProvider.get()
				.memberUid("test3")
				.mailBox("test3@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2, userMembership3), groupCn, ldapDomain);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = 	connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUsersFromGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);

		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test3,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testAddGroupWithNoMembersToGroup() throws LdapInvalidDnException, org.apache.directory.api.ldap.model.exception.LdapException {
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("toGroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("toGroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Cn toGroupCn = toGroup.getCn();
		
		connection.addGroupToGroup(groupCn, toGroupCn, ldapDomain);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member"))).isNull();		
	}
	
	@Test
	public void testAddGroupToGroup() throws Exception {
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("toGroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("toGroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Cn toGroupCn = toGroup.getCn();
		
		connection.addGroupToGroup(groupCn, toGroupCn, ldapDomain);

		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test,ou=users,dc=test.obm.org,dc=local",
				"uid=test2,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testRemoveGroupFromGroup() throws Exception {
		
		LdapGroup fromGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(fromGroup);
		LdapGroup.Cn fromGroupCn = fromGroup.getCn();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), fromGroupCn, ldapDomain);
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("subgroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupCn, ldapDomain);
		
		connection.removeGroupFromGroup(groupCn, fromGroupCn, ldapDomain);

		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member"))).isNull();
	}
	
	@Test
	public void testAddGroupsToGroup() throws Exception {
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership), groupCn, ldapDomain);
		
		LdapGroup group2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group2"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group2);
		LdapGroup.Cn groupCn2 = group2.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership2), groupCn2, ldapDomain);
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("subgroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Cn toGroupCn = toGroup.getCn();
		
		connection.addGroupsToGroup(ImmutableList.of(groupCn, groupCn2), toGroupCn, ldapDomain);

		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("subgroup"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("subgroup");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("subgroup@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test,ou=users,dc=test.obm.org,dc=local",
				"uid=test2,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testAddGroupsWithoutMembersToGroup() throws Exception {
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		
		LdapGroup group2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group2"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group2);
		LdapGroup.Cn groupCn2 = group2.getCn();
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("subgroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Cn toGroupCn = toGroup.getCn();
		
		connection.addGroupsToGroup(ImmutableList.of(groupCn, groupCn2), toGroupCn, ldapDomain);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("subgroup"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("subgroup");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("subgroup@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
	}
	
	@Test
	public void testRemoveGroupsFromGroup() throws Exception {
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.domain(ldapDomain)
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.domain(ldapDomain)
				.build();
		
		LdapGroup fromGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(fromGroup);
		LdapGroup.Cn fromGroupCn = fromGroup.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), fromGroupCn, ldapDomain);
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("subgroup"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group);
		LdapGroup.Cn groupCn = group.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership), groupCn, ldapDomain);

		LdapGroup group2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("subgroup2"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup2@test.obm.org")
				.domain(ldapDomain)
				.build();
		connection.createGroup(group2);
		LdapGroup.Cn groupCn2 = group2.getCn();
		connection.addUsersToGroup(ImmutableList.of(userMembership2), groupCn2, ldapDomain);

		connection.removeGroupsFromGroup(ImmutableList.of(groupCn, groupCn2), fromGroupCn, ldapDomain);

		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getGroupEntry(LdapGroup.Cn.valueOf("group1"), ldapDomain);

		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member"))).isNull();
	}
	
	@Test
	public void testRestartOnRequestCounterReached() {
		MyConnection myConnection = new MyConnection(new OneRequestCounterConfiguration(), getNetworkConfiguration());

		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group1"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		LdapGroup ldapGroup2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn(LdapGroup.Cn.valueOf("group2"))
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group2@test.obm.org")
				.domain(LdapDomain.valueOf("test.obm.org"))
				.build();
		
		myConnection.createGroup(ldapGroup);
		myConnection.createGroup(ldapGroup2);
		assertThat(myConnection.getShutdownCounter()).isEqualTo(2);
	}
	
	protected class MyConnection extends ConnectionImpl {

		private int shutdownCounter;
		
		public MyConnection(Configuration configuration, LdapConnectionConfig connectionConfig) throws LdapException, ConnectionException {
			super(configuration, connectionConfig);
		}
		
		@Override
		public void shutdown() throws ConnectionException {
			super.shutdown();
			shutdownCounter++;
		}

		public int getShutdownCounter() {
			return shutdownCounter;
		}
	}
	
	private class OneRequestCounterConfiguration extends StaticConfiguration {

		@Override
		public int maxRequests() {
			return 1;
		}
		
	}
}
