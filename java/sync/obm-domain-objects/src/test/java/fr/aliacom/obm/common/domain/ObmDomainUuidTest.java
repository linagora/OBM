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
package fr.aliacom.obm.common.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;


public class ObmDomainUuidTest {

	@Test(expected=NullPointerException.class)
	public void nullStringShouldThrow() {
		ObmDomainUuid.of((String)null);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullStringShouldThrowOnValueOf() {
		ObmDomainUuid.valueOf(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void nullUUIDShouldThrow() {
		ObmDomainUuid.of((UUID)null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void ofShouldNotBuildFromRandomString() {
		ObmDomainUuid.of("random");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void valueOfShouldNotBuildFromRandomString() {
		ObmDomainUuid.valueOf("random");
	}
	
	@Test
	public void ofShouldBuildFromString() {
		ObmDomainUuid actual = ObmDomainUuid.of("4e3fa926-bbba-48c4-84ab-272b7433c412");
		assertThat(actual.get()).isEqualTo("4e3fa926-bbba-48c4-84ab-272b7433c412");
	}
	
	@Test
	public void valueOfShouldBuildFromString() {
		ObmDomainUuid actual = ObmDomainUuid.valueOf("4e3fa926-bbba-48c4-84ab-272b7433c412");
		assertThat(actual.get()).isEqualTo("4e3fa926-bbba-48c4-84ab-272b7433c412");
	}
	
	@Test
	public void ofShouldBuildFromUUID() {
		ObmDomainUuid actual = ObmDomainUuid.of(UUID.fromString("4e3fa926-bbba-48c4-84ab-272b7433c412"));
		assertThat(actual.get()).isEqualTo("4e3fa926-bbba-48c4-84ab-272b7433c412");
	}
}
