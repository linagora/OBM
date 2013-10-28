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
package org.obm.sync.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class CredentialsTest {

	private static final String PASSWORD = "password";
	private static final String DOMAIN = "exemple.com";
	private static final String LOGIN = "test";
	private static final String FULL_LOGIN = LOGIN + Login.FULL_LOGIN_SEPARATOR + DOMAIN;
	
	@Test
	public void nullDomain() {
		Credentials credentials = Credentials.builder().login(LOGIN).hashedPassword(false).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isNull();
	}
	
	
	@Test(expected=IllegalStateException.class)
	public void buildNull() {
		Credentials.builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void nullLogin() {
		Credentials.builder().domain(DOMAIN).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildWithoutPassword() {
		Credentials.builder().login(LOGIN).domain(DOMAIN).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildWithoutHashedPassword() {
		Credentials.builder().login(LOGIN).domain(DOMAIN).password(PASSWORD).build();
	}
	
	@Test
	public void buildWithEverything() {
		Credentials credentials = Credentials.builder().login(LOGIN).domain(DOMAIN).hashedPassword(false).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isEqualTo(DOMAIN);
		assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
		assertThat(credentials.isPasswordHashed()).isFalse();
	}
	
	
	@Test
	public void buildWithFullLogin() {
		Credentials credentials = Credentials.builder().login(FULL_LOGIN).hashedPassword(false).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isEqualTo(DOMAIN);
		assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
		assertThat(credentials.isPasswordHashed()).isFalse();
	}
	
	@Test
	public void buildWithHashedPassword() {
		Credentials credentials = Credentials.builder().login(LOGIN).domain(DOMAIN).hashedPassword(true).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isEqualTo(DOMAIN);
		assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
		assertThat(credentials.isPasswordHashed()).isTrue();
	}
	
	@Test
	public void creationByFullLoginAndMatchingDomain() {
		Credentials credentials = Credentials.builder().login(FULL_LOGIN).domain(DOMAIN).hashedPassword(false).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isEqualTo(DOMAIN);
		assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
		assertThat(credentials.isPasswordHashed()).isFalse();
	}
	
	@Test(expected=IllegalStateException.class)
	public void creationByFullLoginAndDifferentDomain() {
		Credentials.builder().login(FULL_LOGIN).domain("differentdomain").hashedPassword(false).password(PASSWORD).build();
	}
	
	@Test
	public void creationByLoginObject() {
		Credentials credentials = Credentials.builder().login(Login.builder().login(LOGIN).domain(DOMAIN).build()).hashedPassword(false).password(PASSWORD).build();
		assertThat(credentials.getLogin().getLogin()).isEqualTo(LOGIN);
		assertThat(credentials.getLogin().getDomain()).isEqualTo(DOMAIN);
		assertThat(credentials.getPassword()).isEqualTo(PASSWORD);
		assertThat(credentials.isPasswordHashed()).isFalse();
	}
	
}
