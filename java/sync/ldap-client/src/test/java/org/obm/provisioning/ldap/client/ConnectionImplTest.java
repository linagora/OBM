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

import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ldap.client.Configuration;
import org.obm.provisioning.ldap.client.ConnectionImpl;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership;
import org.obm.provisioning.ldap.client.bean.LdapUser.Id;
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
import com.google.inject.Inject;
import com.google.inject.Provider;

@Slow
@GuiceModule(EmbeddedLdapModule.class)
@RunWith(SlowGuiceRunner.class)
public class ConnectionImplTest {

	@Inject ConnectionImpl.Factory connectionFactory;
	@Inject DirectoryServer directoryServer;
	@Inject Provider<LdapGroup.Builder> groupBuilderProvider;
	@Inject Provider<LdapUser.Builder> userBuilderProvider;
	@Inject Provider<LdapUserMembership.Builder> groupMemberShipProvider;
	private ConnectionImpl connection;
	
	@Before
	public void setup() throws Exception {
		directoryServer.startServer();
		injectBootstrapData();
		
		connection = connectionFactory.create();
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		
		assertThat(connection.getGroupDnFromGroupId(ldapGroup.getId())
			.equals(new Dn("cn=group1,ou=groups,dc=test.obm.org,dc=local"))).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		
		// In order to be sure that group is really inserted
		LdapGroup.Id groupId = ldapGroup.getId();
		assertThat(connection.getGroupDnFromGroupId(groupId)
			.equals(new Dn("cn=group1,ou=groups,dc=test.obm.org,dc=local"))).isTrue();
		
		connection.deleteGroup(groupId);
		try {
			connection.getGroupDnFromGroupId(groupId);
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testCreateUser() throws Exception {
		LdapUser ldapUser = userBuilderProvider.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid("test")
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
				.obmDomain("test.obm.org")
				.build();
		
		connection.createUser(ldapUser);
		
		assertThat(connection.getUserDnFromUserId(ldapUser.getId())
			.equals(new Dn("uid=test,ou=users,dc=test.obm.org,dc=local"))).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=users,dc=test.obm.org,dc=local"), 
				"(uid=test)", SearchScope.ONELEVEL);
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
		assertThat(entry.get(new AttributeType("hiddenuser")).getString()).isEqualTo("false");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
	}

	@Test
	public void testDeleteUser() throws Exception {
		LdapUser ldapUser = userBuilderProvider.get()
				.objectClasses(new String[] {"shadowAccount", "obmUser", "posixAccount", "inetOrgPerson"})
				.uid("test")
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
				.obmDomain("test.obm.org")
				.build();
		
		connection.createUser(ldapUser);
		
		// In order to be sure that user is really inserted
		Id userId = ldapUser.getId();
		assertThat(connection.getUserDnFromUserId(userId)
			.equals(new Dn("uid=test,ou=users,dc=test.obm.org,dc=local"))).isTrue();
		
		connection.deleteUser(userId);
		try {
			connection.getUserDnFromUserId(userId);
		} catch (IllegalStateException e) {
		}
	}
	
	@Test
	public void testAddUserToGroup() throws Exception {
		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		connection.addUserToGroup(userMembership, groupId);
		
		Attribute attribute = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL)
			.get(new AttributeType("member"));
		assertThat(attribute.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		connection.addUserToGroup(userMembership, groupId);
		
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		LdapUserMembership userMembership3 = groupMemberShipProvider.get()
				.memberUid("test3")
				.mailBox("test3@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership2, userMembership3), groupId);
		
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUserFromGroup(userMembership, groupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUsersFromGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(ldapGroup);
		LdapGroup.Id groupId = ldapGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		LdapUserMembership userMembership3 = groupMemberShipProvider.get()
				.memberUid("test3")
				.mailBox("test3@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2, userMembership3), groupId);
		
		// In order to be sure that user is really inserted
		Attribute attributeInserted = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL)
			.get(new AttributeType("member"));
		assertThat(attributeInserted.contains("uid=test,ou=users,dc=test.obm.org,dc=local")).isTrue();
		
		connection.removeUsersFromGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test3,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testAddGroupToGroup() throws Exception {
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group);
		LdapGroup.Id groupId = group.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("toGroup")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("toGroup@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Id toGroupId = toGroup.getId();
		
		connection.addGroupToGroup(groupId, toGroupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(fromGroup);
		LdapGroup.Id fromGroupId = fromGroup.getId();
		
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), fromGroupId);
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("subgroup")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group);
		LdapGroup.Id groupId = group.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), groupId);
		
		connection.removeGroupFromGroup(groupId, fromGroupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
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
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group);
		LdapGroup.Id groupId = group.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership), groupId);
		
		LdapGroup group2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group2")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group2@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group2);
		LdapGroup.Id groupId2 = group2.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership2), groupId2);
		
		LdapGroup toGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("subgroup")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(toGroup);
		LdapGroup.Id toGroupId = toGroup.getId();
		
		connection.addGroupsToGroup(ImmutableList.of(groupId, groupId2), toGroupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=subgroup)", SearchScope.ONELEVEL);
		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("subgroup");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("subgroup@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member")).contains("uid=test,ou=users,dc=test.obm.org,dc=local",
				"uid=test2,ou=users,dc=test.obm.org,dc=local")).isTrue();
	}
	
	@Test
	public void testRemoveGroupsFromGroup() throws Exception {
		LdapUserMembership userMembership = groupMemberShipProvider.get()
				.memberUid("test")
				.mailBox("test@test.obm.org")
				.build();
		LdapUserMembership userMembership2 = groupMemberShipProvider.get()
				.memberUid("test2")
				.mailBox("test2@test.obm.org")
				.build();
		
		LdapGroup fromGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(fromGroup);
		LdapGroup.Id fromGroupId = fromGroup.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership, userMembership2), fromGroupId);
		
		LdapGroup group = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("subgroup")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group);
		LdapGroup.Id groupId = group.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership), groupId);

		LdapGroup group2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("subgroup2")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("subgroup2@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		connection.createGroup(group2);
		LdapGroup.Id groupId2 = group2.getId();
		connection.addUsersToGroup(ImmutableList.of(userMembership2), groupId2);

		connection.removeGroupsFromGroup(ImmutableList.of(groupId, groupId2), fromGroupId);
		org.apache.directory.api.ldap.model.entry.Entry entry = connection.getEntry(new Dn("ou=groups,dc=test.obm.org,dc=local"), 
				"(cn=group1)", SearchScope.ONELEVEL);
		assertThat(entry.get(new AttributeType("cn")).getString()).isEqualTo("group1");
		assertThat(entry.get(new AttributeType("gidnumber")).getString()).isEqualTo("1001");
		assertThat(entry.get(new AttributeType("mailaccess")).getString()).isEqualTo("PERMIT");
		assertThat(entry.get(new AttributeType("mail")).getString()).isEqualTo("group1@test.obm.org");
		assertThat(entry.get(new AttributeType("obmdomain")).getString()).isEqualTo("test.obm.org");
		assertThat(entry.get(new AttributeType("member"))).isNull();
	}
	
	@Test
	public void testRestartOnRequestCounterReached() {
		MyConnection myConnection = new MyConnection(new OneRequestCounterConfiguration());

		LdapGroup ldapGroup = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group1")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group1@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		LdapGroup ldapGroup2 = groupBuilderProvider.get()
				.objectClasses(new String[] {"posixGroup", "obmGroup"})
				.cn("group2")
				.gidNumber(1001)
				.mailAccess("PERMIT")
				.mail("group2@test.obm.org")
				.obmDomain("test.obm.org")
				.build();
		
		myConnection.createGroup(ldapGroup);
		myConnection.createGroup(ldapGroup2);
		assertThat(myConnection.getShutdownCounter()).isEqualTo(2);
	}
	
	protected class MyConnection extends ConnectionImpl {

		private int shutdownCounter;
		
		public MyConnection(Configuration configuration) throws LdapException, ConnectionException {
			super(configuration);
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
