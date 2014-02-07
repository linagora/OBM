/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.domain.ObmDomain;


public class UserEmailsTest {

	private static final ObmDomain OBM_ORG_DOMAIN = ObmDomain.builder().name("obm.org").build();

	@Test(expected=NullPointerException.class)
	public void testSetEmailsWhenNull() {
		UserEmails.builder()
			.addAddress(null);
	}

	@Test(expected=NullPointerException.class)
	public void testNullEmailAlias() {
		UserEmails.builder()
			.addAddress("valid")
			.addAddress(null);
	}

	@Test
	public void testSetEmailsWhenOne() {
		UserEmails emails = UserEmails.builder()
			.addAddress("one")
			.domain(OBM_ORG_DOMAIN)
			.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.org");
		assertThat(emails.getAliases()).isEmpty();
	}

	@Test
	public void testSetEmailsWhenOneWithDomain() {
		UserEmails emails = UserEmails.builder()
			.addAddress("one@obm.coop")
			.domain(OBM_ORG_DOMAIN)
			.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one@obm.coop");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.coop");
		assertThat(emails.getAliases()).isEmpty();
	}

	@Test
	public void testSetEmailsWhenTwo() {
		UserEmails emails = UserEmails.builder()
				.addAddress("one")
				.addAddress("two")
				.domain(OBM_ORG_DOMAIN)
				.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.org");
		assertThat(emails.getAliases()).contains("two");
	}

	@Test
	public void testSetEmailsWhenThree() {
		UserEmails emails = UserEmails.builder()
				.addAddress("one")
				.addAddress("two")
				.addAddress("three")
				.domain(OBM_ORG_DOMAIN)
				.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.org");
		assertThat(emails.getAliases()).contains("two", "three");
	}

	@Test
	public void testSetEmailsWhenThreeWithAddressesMethod() {
		UserEmails emails = UserEmails.builder()
				.addresses(Arrays.asList("one", "two", "three"))
				.domain(OBM_ORG_DOMAIN)
				.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.org");
		assertThat(emails.getAliases()).contains("two", "three");
	}
	
	@Test
	public void testSetEmailsWhenTwoWithDomain() {
		UserEmails emails = UserEmails.builder()
				.addAddress("one@obm.coop")
				.addAddress("two@obm.coop")
				.domain(OBM_ORG_DOMAIN)
				.build();

		assertThat(emails.getPrimaryAddress()).isEqualTo("one@obm.coop");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.coop");
		assertThat(emails.getAliases()).contains("two@obm.coop");
	}

	@Test
	public void testSetEmailsWhenThreeOnlyOneWithDomain() {
		UserEmails emails = UserEmails.builder()
				.addAddress("one")
				.addAddress("two@obm.coop")
				.addAddress("three")
				.domain(OBM_ORG_DOMAIN)
				.build();
		
		assertThat(emails.getPrimaryAddress()).isEqualTo("one");
		assertThat(emails.getFullyQualifiedPrimaryAddress()).isEqualTo("one@obm.org");
		assertThat(emails.getAliases()).contains("two@obm.coop", "three");
	}


	@Test
	public void testMainThenAliasEmails() {
		UserEmails emails = UserEmails.builder()
				.addAddress("user1@obmsync.test")
				.addAddress("user2")
				.addAddress("user3")
				.domain(OBM_ORG_DOMAIN)
				.build();

		assertThat(emails.expandAllEmailDomainTuples()).containsOnly(
				"user1@obmsync.test", "user2@obm.org", "user3@obm.org");
	}

	@Test
	public void testMainThenAliasEmailsWhenSimpleMainEmail() {
		UserEmails emails = UserEmails.builder()
				.addAddress("user1")
				.domain(OBM_ORG_DOMAIN)
				.build();

		assertThat(emails.expandAllEmailDomainTuples()).containsExactly("user1@obm.org");
	}

	@Test
	public void testMainThenAliasEmailsWhenNoAlias() {
		UserEmails emails = UserEmails.builder()
				.addAddress("user1@obm.coop")
				.domain(OBM_ORG_DOMAIN)
				.build();

		assertThat(emails.expandAllEmailDomainTuples()).containsExactly("user1@obm.coop");
	}

	@Test
	public void testMainThenAliasEmailsAliasWithDomain() {
		UserEmails emails = UserEmails.builder()
				.addAddress("user1@obmsync.test")
				.addAddress("user2@obmsync.test")
				.addAddress("user3")
				.domain(OBM_ORG_DOMAIN)
				.build();

		assertThat(emails.expandAllEmailDomainTuples()).containsOnly(
				"user1@obmsync.test", "user2@obmsync.test", "user3@obm.org");
	}
	
	@Test
	public void testMainThenAliasEmailsWhenOneEmailButDomainAlias() {
		ObmDomain domainWithAliases = ObmDomain.builder()
			.id(5)
			.name("ibm.com")
			.aliases(ImmutableSet.of("microsoft.com"))
			.build();

		UserEmails emails = UserEmails.builder()
				.addAddress("devil")
				.domain(domainWithAliases)
				.build();

		assertThat(emails.expandAllEmailDomainTuples())
			.containsOnly("devil@ibm.com", "devil@microsoft.com");
	}
	
	@Test
	public void testMainThenAliasEmailsWhenOneEmailButTwoDomainAlias() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name("ibm.com")
				.aliases(ImmutableSet.of("microsoft.com", "google.com"))
				.build();


		UserEmails emails = UserEmails.builder()
				.addAddress("devil")
				.domain(domainWithAliases)
				.build();
		
		assertThat(emails.expandAllEmailDomainTuples())
			.containsOnly("devil@ibm.com", "devil@microsoft.com", "devil@google.com");
	}

	@Test
	public void testMainThenAliasEmailsWhenThreeEmailAndThreeDomain() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name("ibm.com")
				.aliases(ImmutableSet.of("microsoft.com", "google.com"))
				.build();
		
		UserEmails emails = UserEmails.builder()
				.addAddress("user1")
				.addAddress("user2")
				.addAddress("user3")
				.domain(domainWithAliases)
				.build();

		
		assertThat(emails.expandAllEmailDomainTuples()).containsOnly(
				"user1@ibm.com",
				"user2@ibm.com",
				"user3@ibm.com",
				"user1@microsoft.com",
				"user2@microsoft.com",
				"user3@microsoft.com",
				"user1@google.com",
				"user2@google.com",
				"user3@google.com");
	}

	
	@Test
	public void testMainThenAliasEmailsWhenThreeEmailWithDomainAndThreeDomain() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name("ibm.com")
				.aliases(ImmutableSet.of("microsoft.com", "google.com"))
				.build();
		
		UserEmails emails = UserEmails.builder()
				.addAddress("user1")
				.addAddress("user2@one.org")
				.addAddress("user3@two.org")
				.domain(domainWithAliases)
				.build();
		
		assertThat(emails.expandAllEmailDomainTuples()).containsOnly(
				"user1@ibm.com",
				"user1@microsoft.com",
				"user1@google.com",
				"user2@one.org",
				"user3@two.org");
	}
}
