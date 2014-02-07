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

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


public class UserPhonesTest {

	@Test(expected = IllegalStateException.class)
	public void testThreeFaxesBuilder() {
		UserPhones
		.builder()
		.faxes(Sets.newHashSet("1", "2", "3"))
		.build();
	}

	@Test
	public void testTwoFaxesBuilder() {
		UserPhones phones = UserPhones
				.builder()
				.faxes(ImmutableSet.of("1", "2"))
				.build();

		assertThat(phones.getFax1()).isEqualTo("1");
		assertThat(phones.getFax2()).isEqualTo("2");
		assertThat(phones.getFaxes()).containsExactly("1", "2");
	}

	@Test
	public void testOneFaxBuilder() {
		UserPhones phones = UserPhones
				.builder()
				.faxes(ImmutableSet.of("1"))
				.build();

		assertThat(phones.getFax1()).isEqualTo("1");
		assertThat(phones.getFax2()).isNull();
		assertThat(phones.getFaxes()).containsExactly("1", null);
	}

	@Test
	public void testNoFaxBuilder() {
		UserPhones phones = UserPhones
				.builder()
				.faxes(ImmutableSet.<String>of())
				.build();

		assertThat(phones.getFax1()).isNull();
		assertThat(phones.getFax2()).isNull();
		assertThat(phones.getFaxes()).containsExactly(null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testBuilderWithNullFaxes() {
		UserPhones
		.builder()
		.faxes(null)
		.build();
	}
	
	@Test
	public void testAddNullFax() {
		UserPhones phones = UserPhones.builder()
			.addFax(null)
			.addFax("0810251251")
			.build();
		assertThat(phones.getFaxes()).containsExactly(null, "0810251251");
	}

}
