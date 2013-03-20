/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.ImmutableSet;

import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(SlowFilterRunner.class)
public class ObmUserTest {

	private ObmDomain domain;
	private String domainName;

	@Before
	public void setUp() {
		domainName = "obmsync.test";
		domain = ObmDomain.builder()
				.id(2)
				.name(domainName)
				.build();
	}
	
	@Test
	public void testGetEmailWithoutDomain() {
		String email = "user1";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);

		assertThat(obmUser.getEmail()).isEqualTo(email + "@" +domainName);
	}
	
	@Test
	public void testGetEmailWithDomain() {
		String email = "user1@obmsync.test";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);

		assertThat(obmUser.getEmail()).isEqualTo(email);
	}

	@Test
	public void testMainThenAliasEmails() {
		String email = "user1@obmsync.test";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);
		obmUser.addAlias("user2");
		obmUser.addAlias("user3");

		assertThat(obmUser.buildAllEmails()).containsExactly(
				email, "user2@obmsync.test", "user3@obmsync.test");
	}

	@Test
	public void testMainThenAliasEmailsWhenSimpleMainEmail() {
		String email = "user1";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);

		assertThat(obmUser.buildAllEmails()).containsExactly(email + "@" + domainName);
	}

	@Test
	public void testMainThenAliasEmailsWhenNoAlias() {
		String email = "user1@obmsync.test";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);

		assertThat(obmUser.buildAllEmails()).containsExactly(email);
	}

	@Test
	public void testMainThenAliasEmailsAliasWithDomain() {
		String email = "user1@obmsync.test";
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);
		obmUser.addAlias("user2@obmsync.test");
		obmUser.addAlias("user3");

		assertThat(obmUser.buildAllEmails()).containsExactly(
				email, "user2@obmsync.test", "user3@obmsync.test");
	}

	@Test
	public void testMainThenAliasEmailsWhenOneLoginButDomainAlias() {
		ObmDomain domainWithAliases = ObmDomain.builder()
			.id(5)
			.name(domainName)
			.aliases(ImmutableSet.of("obm.org"))
			.build();

		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domainWithAliases);
		obmUser.setEmail("user1");

		assertThat(obmUser.buildAllEmails()).containsExactly(
				"user1@obmsync.test", "user1@obm.org");
	}

	@Test
	public void testMainThenAliasEmailsWhenOneLoginButTwoDomainAlias() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name(domainName)
				.aliases(ImmutableSet.of("obm.org", "rasta.rocket"))
				.build();

		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domainWithAliases);
		obmUser.setEmail("user1");

		assertThat(obmUser.buildAllEmails()).containsExactly(
				"user1@obmsync.test", "user1@obm.org", "user1@rasta.rocket");
	}

	@Test
	public void testMainThenAliasEmailsWhenThreeLoginAndThreeDomain() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name(domainName)
				.aliases(ImmutableSet.of("obm.org", "rasta.rocket"))
				.build();

		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domainWithAliases);
		obmUser.setEmail("user1");
		obmUser.addAlias("user2");
		obmUser.addAlias("user3");

		assertThat(obmUser.buildAllEmails()).containsExactly(
				"user1@obmsync.test", 	"user2@obmsync.test", 	"user3@obmsync.test",
				"user1@obm.org", 		"user2@obm.org", 		"user3@obm.org",
				"user1@rasta.rocket", 	"user2@rasta.rocket", 	"user3@rasta.rocket");
	}

	@Test
	public void testMainThenAliasEmailsWhenThreeLoginWithDomainAndThreeDomain() {
		ObmDomain domainWithAliases = ObmDomain.builder()
				.id(5)
				.name(domainName)
				.aliases(ImmutableSet.of("obm.org", "rasta.rocket"))
				.build();

		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domainWithAliases);
		obmUser.setEmail("user1");
		obmUser.addAlias("user2@one.org");
		obmUser.addAlias("user3@two.org");

		assertThat(obmUser.buildAllEmails()).containsOnly(
				"user1@obmsync.test", "user1@obm.org", "user1@rasta.rocket",
				"user2@one.org", "user3@two.org");
	}
}
