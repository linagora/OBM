package org.obm.provisioning.ldap.client.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.ldap.client.EmbeddedLdapModule;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

@GuiceModule(EmbeddedLdapModule.class)
@RunWith(SlowGuiceRunner.class)
public class LdapUserTest {

	@Inject LdapUser.Builder ldapUserBuilder;

	private ObmUser buildObmUser() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.emailAndAliases("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password("secret password")
				.mailHost(
						ObmHost.builder()
						.ip("255.255.255.0")
						.build())
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoUidNumber() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.emailAndAliases("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password("secret password")
				.mailHost(
						ObmHost.builder()
						.ip("255.255.255.0")
						.build())
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoGidNumber() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.emailAndAliases("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.uidNumber(1895)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password("secret password")
				.mailHost(
						ObmHost.builder()
						.ip("255.255.255.0")
						.build())
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoEmail() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password("secret password")
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoDomainName() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.emailAndAliases("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.build())
				.password("secret password")
				.mailHost(
						ObmHost.builder()
						.ip("255.255.255.0")
						.build())
				.build();
		return obmUser;
	}

	private ObmUser buildObmUserNoMailHostIP() {
		ObmUser obmUser = ObmUser.builder()
				.uid(666)
				.login("Richard.Sorge")
				.emailAndAliases("Richard.Sorge")
				.firstName("Richard")
				.lastName("Sorge")
				.uidNumber(1895)
				.gidNumber(1066)
				.domain(
						ObmDomain.builder().host(
									ServiceProperty.builder().build(),
									ObmHost.builder().build())
								.name("gru.gov.ru")
								.build())
				.password("secret password")
				.mailHost(
						ObmHost.builder()
						.build())
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
				.userPassword("secret password")
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
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
				.userPassword("secret password")
				.webAccess("REJECT")
				.mailAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
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
				.userPassword("secret password")
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
				.userPassword("secret password")
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
				.userPassword("secret password")
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
				.userPassword("secret password")
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
				.userPassword("secret password")
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
				.userPassword("secret password")
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
				.userPassword("secret password")
				.webAccess("REJECT")
				.mailBox("richard.sorge@gru.gov.ru")
				.mailBoxServer("lmtp:255.255.255.0:24")
				.mailAccess("PERMIT")
				.hiddenUser(false)
				.domain(LdapDomain.valueOf("gru.gov.ru"))
				.build();
	}
}
