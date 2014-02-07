/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
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

import org.junit.Test;
import org.obm.provisioning.ProfileName;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;


public class ObmUserTest {
	
	private static final UserExtId userExtId = UserExtId.builder().extId("extId").build();
	private static final UserLogin validLogin = UserLogin.valueOf("login");
	
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
			.login(validLogin)
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
			.login(validLogin)
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
			.login(validLogin)
			.build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testUidAndExItPrecondition() {
		ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.login(validLogin)
			.build();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullAddressBuilder() {
		ObmUser
			.builder()
			.domain(ObmDomain.builder().build())
			.uid(1)
			.login(validLogin)
			.address(null)
			.build();
	}
	
	@Test(expected=NullPointerException.class)
	public void testNullGroups() {
		ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login(validLogin)
				.groups(null)
				.build();
	}
	
	@Test
	public void testOneMailBuilder() {
		ObmUser user = ObmUser
				.builder()
				.domain(ObmDomain.builder().build())
				.uid(1)
				.login(validLogin)
				.emails(UserEmails.builder()
					.addAddress("1")
					.domain(ObmDomain.builder().build())
					.build())
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
				.login(validLogin)
				.emails(UserEmails.builder()
					.addAddress("1")
					.addAddress("2")
					.domain(ObmDomain.builder().build())
					.build())
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
				.login(validLogin)
				.build();
		
		assertThat(user.getEmail()).isNull();
		assertThat(user.getEmailAlias()).isEmpty();
	}
	

	@Test
	public void testFrom() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.extId(UserExtId.valueOf("JohnDoeExtId"))
				.login(validLogin)
				.password("secure")
				.profileName(ProfileName.valueOf("user"))
				.identity(UserIdentity.builder()
					.kind("Mr")
					.lastName("Doe")
					.firstName("John")
					.commonName("J. Doe")
					.build())
				.address(UserAddress.builder()
					.addressPart("1 OBM Street")
					.addressPart("2 OBM Street")
					.addressPart("3 OBM Street")
					.town("OBMCity")
					.countryCode("OB")
					.zipCode("OBMZip")
					.expressPostal("OBMExpressPostal")
					.build())
				.phones(UserPhones.builder()
					.addPhone( "+OBM 123456")
					.addPhone("+OBM 789")
					.mobile("+OBMMobile 123")
					.addFax("+OBMFax 123456")
					.addFax("+OBMFax 789")
					.build())
				.work(UserWork.builder()
					.company("Linagora")
					.service("OBMDev")
					.direction("LGS")
					.title("Software Dev")
					.build())
				.emails(UserEmails.builder()
					.addAddress("jdoe")
					.addAddress("john.doe")
					.quota(500)
					.domain(ToolBox.getDefaultObmDomain())
					.build())
				.archived(true)
				.domain(ToolBox.getDefaultObmDomain())
				.hidden(true)
				.build();

		assertThat(ObmUser.builder().from(user).build()).isEqualTo(user);
	}

	@Test
	public void testQuotaNonNullQuota() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(validLogin)
				.domain(ToolBox.getDefaultObmDomain())
				.emails(UserEmails.builder()
					.quota(123)
					.domain(ToolBox.getDefaultObmDomain())
					.build())
				.build();

		assertThat(user.getMailQuota()).isEqualTo(123);
	}

	@Test
	public void testQuotaNoQuota() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(validLogin)
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		assertThat(user.getMailQuota()).isNull();
	}

	@Test
	public void testIsArchivedWhenDefaultValue() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(validLogin)
				.domain(ToolBox.getDefaultObmDomain())
				.build();

		assertThat(user.isArchived()).isFalse();
	}

	@Test
	public void testIsArchivedWhenTrue() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(validLogin)
				.domain(ToolBox.getDefaultObmDomain())
				.archived(true)
				.build();

		assertThat(user.isArchived()).isTrue();
	}

	@Test
	public void testIsArchivedWhenFalse() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(validLogin)
				.domain(ToolBox.getDefaultObmDomain())
				.archived(false)
				.build();

		assertThat(user.isArchived()).isFalse();
	}

	@Test
	public void testAdminDefaultValue() {
		ObmUser obmUser = ObmUser.builder()
				.uid(1)
				.login(validLogin)
				.domain(ObmDomain.builder().build())
				.build();
		assertThat(obmUser.isAdmin()).isFalse();
	}

	@Test
	public void testIsAdmin() {
		ObmUser obmUser = ObmUser.builder()
				.uid(1)
				.login(validLogin)
				.domain(ObmDomain.builder().build())
				.admin(true)
				.build();
		assertThat(obmUser.isAdmin()).isTrue();
	}

	@Test
	public void testIsNotAdmin() {
		ObmUser obmUser = ObmUser.builder()
				.uid(1)
				.login(validLogin)
				.domain(ObmDomain.builder().build())
				.admin(false)
				.build();
		assertThat(obmUser.isAdmin()).isFalse();
	}
}
