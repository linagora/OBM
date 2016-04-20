/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2016  Linagora
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
package fr.aliacom.obm.common.mailshare;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.sync.host.ObmHost;

import com.google.common.base.Optional;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.mailshare.SharedMailbox;

public class SharedMailboxTest {

	@Test(expected=IllegalStateException.class)
	public void buildShouldThrowWhenIdIsNotProvided() {
		SharedMailbox
			.builder()
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void idShouldThrowWhenIdIsNull() {
		SharedMailbox
			.builder()
			.id(null);
	}

	@Test(expected=IllegalStateException.class)
	public void buildShouldThrowWhenDomainIsNotProvided() {
		SharedMailbox
			.builder()
			.id(1)
			.build();
	}

	@Test(expected=NullPointerException.class)
	public void idShouldThrowWhenDomainIsNull() {
		SharedMailbox
			.builder()
			.domain(null);
	}

	@Test
	public void buildShouldSetArchiveToFalseWhenNotProvided() {
		SharedMailbox sharedMailbox = SharedMailbox
			.builder()
			.id(1)
			.domain(ObmDomain.builder()
					.build())
			.build();
		assertThat(sharedMailbox.getArchive()).isFalse();
	}

	@Test
	public void buildShouldSetQuotaToZeroWhenNotProvided() {
		SharedMailbox sharedMailbox = SharedMailbox
			.builder()
			.id(1)
			.domain(ObmDomain.builder()
					.build())
			.build();
		assertThat(sharedMailbox.getQuota()).isEqualTo(0);
	}

	@Test
	public void buildShouldSetDelegationToEmptyWhenNotProvided() {
		SharedMailbox sharedMailbox = SharedMailbox
			.builder()
			.id(1)
			.domain(ObmDomain.builder()
					.build())
			.build();
		assertThat(sharedMailbox.getDelegation()).isEmpty();
	}

	@Test
	public void buildShouldWork() {
		int id = 1;
		ObmDomain domain = ObmDomain.builder()
				.build();
		String name = "name";
		boolean archive = true;
		int quota = 2;
		ObmHost obmHost = ObmHost.builder()
				.build();
		String delegation = "delegation";
		String description = "description";
		String email = "email";

		SharedMailbox expectedSharedMailbox = new SharedMailbox(id, domain, Optional.of(name), archive, quota, Optional.of(obmHost),
				delegation, Optional.of(description), Optional.of(email));

		SharedMailbox sharedMailbox = SharedMailbox
			.builder()
			.id(id)
			.domain(domain)
			.name(name)
			.archive(archive)
			.quota(quota)
			.server(obmHost)
			.delegation(delegation)
			.description(description)
			.email(email)
			.build();
		assertThat(sharedMailbox).isEqualToComparingFieldByField(expectedSharedMailbox);
	}
}
