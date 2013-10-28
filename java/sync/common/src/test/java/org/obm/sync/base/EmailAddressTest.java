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
package org.obm.sync.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class EmailAddressTest {

	@Test
	public void testEqualWhenSame() {
		EmailAddress first = EmailAddress.loginAtDomain("one@domain.org");
		EmailAddress second = EmailAddress.loginAtDomain("one@domain.org");
		assertThat(first.equals(second)).isTrue();
		assertThat(second.equals(first)).isTrue();
	}

	@Test
	public void testEqualWhenDifferentLogin() {
		EmailAddress first = EmailAddress.loginAtDomain("one@domain.org");
		EmailAddress second = EmailAddress.loginAtDomain("two@domain.org");
		assertThat(first.equals(second)).isFalse();
		assertThat(second.equals(first)).isFalse();
	}

	@Test
	public void testEqualWhenDifferentDomain() {
		EmailAddress first = EmailAddress.loginAtDomain("one@domain.org");
		EmailAddress second = EmailAddress.loginAtDomain("one@other.org");
		assertThat(first.equals(second)).isFalse();
		assertThat(second.equals(first)).isFalse();
	}

	@Test
	public void testEqualWhenDifferentLoginCase() {
		EmailAddress first = EmailAddress.loginAtDomain("oNe@domain.org");
		EmailAddress second = EmailAddress.loginAtDomain("one@domain.org");
		assertThat(first.equals(second)).isTrue();
		assertThat(second.equals(first)).isTrue();
	}

	@Test
	public void testEqualWhenDifferentDomainCase() {
		EmailAddress first = EmailAddress.loginAtDomain("one@domaiN.org");
		EmailAddress second = EmailAddress.loginAtDomain("one@domain.org");
		assertThat(first.equals(second)).isTrue();
		assertThat(second.equals(first)).isTrue();
	}
	
	@Test
	public void testAddressToLowerCase() {
		EmailAddress loginAtDomain = EmailAddress.loginAtDomain("loGin@domain.Org");
		assertThat(loginAtDomain.get()).isEqualTo("login@domain.org");
		assertThat(loginAtDomain.getLogin()).isEqualTo(new EmailLogin("login"));
		assertThat(loginAtDomain.getDomain()).isEqualTo(new DomainName("domain.org"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testLoginAtDomainNull() {
		EmailAddress.loginAtDomain(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoginAtDomainEmpty() {
		EmailAddress.loginAtDomain("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoginAtDomainNotAddress() {
		EmailAddress.loginAtDomain("loginatdomain.org");
	}

	@Test
	public void testLoginAtDomain() {
		EmailAddress emailAddress = EmailAddress.loginAtDomain("login@domain.org");
		assertThat(emailAddress.get()).isEqualTo("login@domain.org");
		assertThat(emailAddress.getLogin()).isEqualTo(new EmailLogin("login"));
		assertThat(emailAddress.getDomain()).isEqualTo(new DomainName("domain.org"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoginAndDomainNullDomainAndLogin() {
		EmailAddress.loginAndDomain(null, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoginAndDomainNullLogin() {
		EmailAddress.loginAndDomain(null, new DomainName("domain.org"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testLoginAndDomainNullDomain() {
		EmailAddress.loginAndDomain(new EmailLogin("login"), null);
	}

	@Test
	public void testLoginAndDomain() {
		EmailAddress emailAddress = EmailAddress.loginAndDomain(new EmailLogin("login"), new DomainName("domain.org"));
		assertThat(emailAddress.get()).isEqualTo("login@domain.org");
		assertThat(emailAddress.getLogin()).isEqualTo(new EmailLogin("login"));
		assertThat(emailAddress.getDomain()).isEqualTo(new DomainName("domain.org"));
	}
}
