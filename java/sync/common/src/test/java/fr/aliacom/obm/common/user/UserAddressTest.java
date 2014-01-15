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

import java.util.Arrays;

import org.junit.Test;


public class UserAddressTest {

	@Test(expected=IllegalStateException.class)
	public void testOutOfRangeAddressBuilder() {
		UserAddress
		.builder()
		.addressParts(Arrays.asList("1", "2", "3", "4"))
		.build();
	}

	@Test
	public void testThreeAddressBuilder() {
		UserAddress userAddress = UserAddress
				.builder()
				.addressParts(Arrays.asList("1", "2", "3"))
				.build();

		assertThat(userAddress.getAddressParts()).containsOnly("1", "2", "3");
		assertThat(userAddress.getAddress1()).isEqualTo("1");
		assertThat(userAddress.getAddress2()).isEqualTo("2");
		assertThat(userAddress.getAddress3()).isEqualTo("3");
	}

	@Test
	public void testOneAddressBuilder() {
		UserAddress userAddress = UserAddress
				.builder()
				.addressParts(Arrays.asList("1"))
				.build();

		assertThat(userAddress.getAddressParts()).containsOnly("1", null, null);
		assertThat(userAddress.getAddress1()).isEqualTo("1");
		assertThat(userAddress.getAddress2()).isNull();
		assertThat(userAddress.getAddress3()).isNull();
	}

	@Test
	public void testTwoAddressBuilder() {
		UserAddress userAddress = UserAddress
				.builder()
				.addressParts(Arrays.asList("1", "2"))
				.build();

		assertThat(userAddress.getAddressParts()).containsOnly("1", "2", null);
		assertThat(userAddress.getAddress1()).isEqualTo("1");
		assertThat(userAddress.getAddress2()).isEqualTo("2");
		assertThat(userAddress.getAddress3()).isNull();
	}

	@Test
	public void testEmptyAddressBuilder() {
		UserAddress userAddress = UserAddress.builder().build();

		assertThat(userAddress.getAddressParts()).containsOnly(null, null, null);
		assertThat(userAddress.getAddress1()).isEqualTo(null);
		assertThat(userAddress.getAddress2()).isEqualTo(null);
		assertThat(userAddress.getAddress3()).isEqualTo(null);
	}

}
