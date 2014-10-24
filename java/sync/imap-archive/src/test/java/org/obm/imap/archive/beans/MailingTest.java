/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.sync.base.EmailAddress;

import com.google.common.collect.ImmutableList;


public class MailingTest {

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenEmailAddressesIsNull() {
		Mailing.builder().addAll(null);
	}

	@Test(expected=NullPointerException.class)
	public void builderShouldThrowWhenEmailAddressIsNull() {
		Mailing.builder().add(null);
	}

	@Test
	public void builderShouldBuild() {
		EmailAddress emailAddress = EmailAddress.loginAtDomain("usera@mydomain.org");
		
		ImmutableList<EmailAddress> emailAddresses = ImmutableList.of(emailAddress);
		EmailAddress emailAddress2 = EmailAddress.loginAtDomain("userb@mydomain.org");
		
		Mailing mailing = Mailing.builder()
				.addAll(emailAddresses)
				.add(emailAddress2)
				.build();
		assertThat(mailing.getEmailAddresses()).containsOnly(emailAddress, emailAddress2);
	}

	@Test
	public void fromShouldBuild() {
		ImmutableList<EmailAddress> emailAddresses = ImmutableList.of(EmailAddress.loginAtDomain("usera@mydomain.org"), EmailAddress.loginAtDomain("userb@mydomain.org"));
		Mailing mailing = Mailing.from(emailAddresses);
		assertThat(mailing.getEmailAddresses()).isEqualTo(emailAddresses);
	}

	@Test
	public void emptyShouldBuild() {
		Mailing mailing = Mailing.empty();
		assertThat(mailing.getEmailAddresses()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void fromStringsShouldThrowWhenNull() {
		Mailing.fromStrings(null);
	}
	
	@Test
	public void fromStringsShouldReturnEmptyWhenEmpty() {
		Mailing mailing = Mailing.fromStrings(ImmutableList.<String> of());
		assertThat(mailing.getEmailAddresses()).isEmpty();
	}
	
	@Test
	public void fromStringsShouldBuild() {
		Mailing mailing = Mailing.fromStrings(ImmutableList.<String> of("usera@mydomain.org", "userb@mydomain.org"));
		assertThat(mailing.getEmailAddresses()).containsExactly(EmailAddress.loginAtDomain("usera@mydomain.org"), EmailAddress.loginAtDomain("userb@mydomain.org"));
	}
}
