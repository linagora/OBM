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
package org.obm.provisioning.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;


public class UserIdentifierTest {

	private static final ObmDomainUuid domainUuid = ObmDomainUuid.of("ecdea37f-038e-46d3-9e43-82ec5f6e9bfd");

	@Test
	public void buildUserIdentifier() {
		UserExtId extId = UserExtId.builder().extId("1").build();
		UserIdentifier userIdentifier = UserIdentifier.builder().id(extId).domainUuid(domainUuid).build();

		assertThat(userIdentifier.getId()).isEqualTo(extId);
		assertThat(userIdentifier.getUrl()).isEqualTo("/ecdea37f-038e-46d3-9e43-82ec5f6e9bfd/users/1");
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildUserIdentifierWithNullId() {
		UserIdentifier.builder().domainUuid(domainUuid).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildUserIdentifierWithNullUrl() {
		UserIdentifier.builder().id(UserExtId.builder().extId("1").build()).build();
	}
}
