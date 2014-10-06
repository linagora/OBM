package org.obm.provisioning.ldap.client.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.ldap.client.EmbeddedLdapModule;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserIdentity;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserPassword;

@GuiceModule(EmbeddedLdapModule.class)
@RunWith(GuiceRunner.class)
public class LdapUserTest {

	private final UserLogin validLogin = UserLogin.valueOf("Richard.Sorge");
	private final UserIdentity richardSorgeIdentity = UserIdentity.builder()
			.firstName("Richard")
			.lastName("Sorge")
			.build();
	
	private final UserEmails.Builder richardSorgeEmailsBuilder = UserEmails.builder()
			.addAddress("Richard.Sorge")
			.server(ObmHost.builder()
						.ip("255.255.255.0")
						.build());
	
	@Inject LdapUser.Builder ldapUserBuilder;
	@Inject LdapUser.Builder ldapUserBuilder2;

	private ObmUser buildObmUser() {
		ObmDomain domain = ObmDomain.builder()
			.host(
				ServiceProperty.builder().build(),
				ObmHost.builder().build())
			.name("gru.gov.ru")
			.aliases(ImmutableSet.of("test1.org", "test2.org"))
			.build();
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.emails(UserEmails.builder()
						.addAddress("Richard.Sorge")
						.addAddress("alias1")
						.addAddress("alias2")
						.server(ObmHost.builder()
								.ip("255.255.255.0")
								.build())
						.domain(domain)
						.build())
				.identity(richardSorgeIdentity)
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(domain)
				.password(UserPassword.valueOf("secret password"))
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoUidNumber() {
		ObmDomain domain = ObmDomain.builder()
			.host(
				ServiceProperty.builder().build(),
				ObmHost.builder().build())
			.name("gru.gov.ru")
			.build();
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.emails(richardSorgeEmailsBuilder.domain(domain).build())
				.identity(richardSorgeIdentity)
				.gidNumber(1066)
				.domain(domain)
				.password(UserPassword.valueOf("secret password"))
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoGidNumber() {
		ObmDomain domain = ObmDomain.builder()
			.host(
				ServiceProperty.builder().build(),
				ObmHost.builder().build())
			.name("gru.gov.ru")
			.build();
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.emails(richardSorgeEmailsBuilder.domain(domain).build())
				.identity(richardSorgeIdentity)
				.uidNumber(1895)
				.domain(domain)
				.password(UserPassword.valueOf("secret password"))
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoEmail() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.identity(richardSorgeIdentity)
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password(UserPassword.valueOf("secret password"))
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoDomainName() {
		ObmDomain domain = ObmDomain.builder()
			.host(
				ServiceProperty.builder().build(),
				ObmHost.builder().build())
			.build();
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.emails(richardSorgeEmailsBuilder.domain(domain).build())
				.identity(richardSorgeIdentity)
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(domain)
				.password(UserPassword.valueOf("secret password"))
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoMailHostIP() {
		ObmDomain domain = ObmDomain.builder()
			.host(
				ServiceProperty.builder().build(),
				ObmHost.builder().build())
			.name("gru.gov.ru")
			.build();
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login(validLogin)
				.emails(UserEmails.builder()
					.addAddress("Richard.Sorge")
					.server(ObmHost.builder().build())
					.domain(domain)
					.build())
				.identity(richardSorgeIdentity)
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(domain)
				.password(UserPassword.valueOf("secret password"))
				
				.build();
		return obmUser;
	}

	@Test
	public void testFromObmUser() {
		LdapUser expectedLdapUser = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.loginShell("/bin/bash")
				.mail("Richard.Sorge@gru.gov.ru")
				.mailAlias(ImmutableSet.of(
						"Richard.Sorge@test1.org",
						"Richard.Sorge@test2.org",
						"alias1@gru.gov.ru",
						"alias1@test1.org",
						"alias1@test2.org",
						"alias2@gru.gov.ru",
						"alias2@test1.org",
						"alias2@test2.org"))
				.build();
		LdapUser ldapUser = ldapUserBuilder.fromObmUser(buildObmUser()).build();
		assertThat(ldapUser).isEqualTo(expectedLdapUser);
	}

	@Test
	public void testFromObmUserNoEmail() {
		LdapUser expectedLdapUser = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.loginShell("/bin/bash")
				.build();
		LdapUser ldapUser = ldapUserBuilder.fromObmUser(buildObmUserNoEmail()).build();
		assertThat(ldapUser).isEqualTo(expectedLdapUser);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromObmUserNoUidNumber() {
		ObmUser obmUser = buildObmUserNoUidNumber();
		ldapUserBuilder.fromObmUser(obmUser).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromObmUserNoGidNumber() {
		ObmUser obmUser = buildObmUserNoGidNumber();
		ldapUserBuilder.fromObmUser(obmUser).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromObmUserNoDomainName() {
		ObmUser obmUser = buildObmUserNoDomainName();
		ldapUserBuilder.fromObmUser(obmUser).build();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFromObmUserNoMailHostIp() {
		ObmUser obmUser = buildObmUserNoMailHostIP();
		ldapUserBuilder.fromObmUser(obmUser).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testNoUid() {
		ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testNoUidNumber() {
		ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testNoGidNumber() {
		ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testNoDomain() {
		ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testNoObjectClasses() {
		ldapUserBuilder
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testEmptyObjectClasses() {
		ldapUserBuilder
				.objectClasses(new String[]{})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test(expected=IllegalStateException.class)
	public void testNoCn() {
		ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}

	@Test
	public void testBuildDiffModifications() {
		LdapUser.Builder userBuilder = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"));
		LdapUser user = userBuilder.build();
		LdapUser newUser = userBuilder
				.sn("newSn")
				.mail("newMail@domain.com")
				.build();

		assertThat(newUser.buildDiffModifications(user)).isEqualTo(new Modification[] {
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "sn", "newSn"),
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "mail", "newMail@domain.com")
		});
	}

	@Test
	public void testBuildMailAliasDiffModifications() {
		LdapUser user = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.mailAlias(ImmutableSet.of("alias0", "alias2", "alias3"))
				.mail("mail")
				.build();
		LdapUser newUser = ldapUserBuilder2
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.mail("mail")
				.mailAlias(ImmutableSet.of("alias1", "alias2"))
				.build();

		assertThat(newUser.buildDiffModifications(user)).isEqualTo(new Modification[] {
				new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "MAILALIAS", "alias0"),
				new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "MAILALIAS", "alias2"),
				new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "MAILALIAS", "alias3"),
				new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "MAILALIAS", "alias1"),
				new DefaultModification(ModificationOperation.ADD_ATTRIBUTE, "MAILALIAS", "alias2"),
		});
	}
	
	@Test
	public void testBuildDiffModificationsWhenNoModifications() {
		LdapUser user = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru")).build();

		assertThat(user.buildDiffModifications(user)).isEqualTo(new Modification[0]);
	}

	@Test
	public void testBuildDiffModificationsWhenRemovingAnAttribute() {
		LdapUser.Builder userBuilder = ldapUserBuilder
				.objectClasses(new String[]{"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser"})
				.uid(LdapUser.Uid.valueOf("richard.sorge"))
				.uidNumber(1895)
				.gidNumber(1066)
				.cn("Richard Sorge")
				.displayName("Richard Sorge")
				.sn("Sorge")
				.givenName("Richard")
				.homeDirectory("/home/richard.sorge")
				.userPassword(UserPassword.valueOf("secret password"))
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"));
		LdapUser user = userBuilder.build();
		LdapUser newUser = userBuilder
				.sn(null)
				.cn("newCn")
				.build();

		assertThat(newUser.buildDiffModifications(user)).isEqualTo(new Modification[] {
				new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "cn", "newCn"),
				new DefaultModification(ModificationOperation.REMOVE_ATTRIBUTE, "sn"),
		});
	}

}
