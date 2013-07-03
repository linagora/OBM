/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import fr.aliacom.obm.common.domain.ObmDomain;

@RunWith(SlowFilterRunner.class)
public class ObmUserTest {
	
	private static final UserExtId userExtId = new UserExtId("extId");

	@Test(expected=IllegalStateException.class)
	public void testLoginPrecondition() {
		ObmUser
			.builder()
			.uid(1)
			.extId(userExtId)
			.domain(ObmDomain.builder().build())
			.build();
	}
	
	@Test
	public void testPreconditionWithNullExtId() {
		ObmUser user = ObmUser
			.builder()
			.uid(1)
			.login("login")
			.domain(ObmDomain.builder().build())
			.build();
		
		assertThat(user.getExtId()).isNull();
		assertThat(user.getUid()).isEqualTo(1);
	}
	
	@Test
	public void testPreconditionWithNullUid() {
		ObmUser user = ObmUser
			.builder()
			.extId(userExtId)
			.login("login")
			.domain(ObmDomain.builder().build())
			.build();
		
		assertThat(user.getExtId()).isEqualTo(userExtId);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testDomainPrecondition() {
		ObmUser
			.builder()
			.uid(1)
			.extId(userExtId)
			.login("login")
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testUidAndExItPrecondition() {
		ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.login("login")
			.build();
	}
	
	@Test
	public void testOneAddressBuilder() {
		ObmUser user = ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.addresses(Sets.newHashSet("1"))
			.build();
		
		assertThat(user.getAddresses()).containsOnly("1", null, null);
	}
	
	@Test
	public void testTwoAddressBuilder() {
		ObmUser user = ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.addresses(Sets.newHashSet("1", "2"))
			.build();
		
		assertThat(user.getAddresses()).containsOnly("1", "2", null);
	}
	
	@Test
	public void testThreeAddressBuilder() {
		ObmUser user = ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.addresses(Sets.newHashSet("1", "2", "3"))
			.build();
		
		assertThat(user.getAddresses()).containsOnly("1", "2", "3");
	}
	
	@Test(expected=IllegalStateException.class)
	public void testOutOfRangeAddressBuilder() {
		ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.addresses(Sets.newHashSet("1", "2", "3", "4"))
			.build();
	}
	
	@Test
	public void testEmptyAddressBuilder() {
		ObmUser user = ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.build();
		
		assertThat(user.getAddress1()).isEqualTo(null);
		assertThat(user.getAddress2()).isEqualTo(null);
		assertThat(user.getAddress3()).isEqualTo(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullAddressBuilder() {
		ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login("login")
			.addresses(null)
			.build();
	}
	
	@Test
	public void testOneMailBuilder() {
		ObmUser user = ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login("login")
				.mails(Sets.newHashSet("1"))
				.build();
		
		assertThat(user.getEmail()).isEqualTo("1");
		assertThat(user.getEmailAlias()).isEmpty();
	}
	
	@Test
	public void testTwoMailBuilder() {
		ObmUser user = ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login("login")
				.mails(Lists.newArrayList("1", "2"))
				.build();
		
		assertThat(user.getEmail()).isEqualTo("1");
		assertThat(user.getEmailAlias()).containsOnly("2");
	}
	
	@Test
	public void testEmptyMailBuilder() {
		ObmUser user = ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login("login")
				.build();
		
		assertThat(user.getEmail()).isNull();
		assertThat(user.getEmailAlias()).isNull();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullMailBuilder() {
		ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login("login")
				.mails(null)
				.build();
	}
}
