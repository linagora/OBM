/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HashTest {

	@Test
	public void testNONEHashShouldNotHash() {
		assertThat(Hash.NONE.hashString("password")).isEqualTo("password");
	}

	@Test
	public void testMD5HashShouldHashInMD5() {
		assertThat(Hash.MD5.hashString("password")).isEqualTo("5f4dcc3b5aa765d61d8327deb882cf99");
	}

	@Test
	public void testSHA1HashShouldHashInSHA1() {
		assertThat(Hash.SHA1.hashString("password")).isEqualTo("5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8");
	}

	@Test
	public void testSHA256HashShouldHashInSHA256() {
		assertThat(Hash.SHA256.hashString("password")).isEqualTo("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8");
	}

	@Test
	public void testSHA512HashShouldHashInSHA512() {
		assertThat(Hash.SHA512.hashString("password")).isEqualTo("b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86");
	}

	@Test
	public void testNONEHashShouldReturnNullWhenNullGiven() {
		assertThat(Hash.NONE.hashString(null)).isNull();
	}

	@Test
	public void testMD5HashShouldReturnNullWhenNullGiven() {
		assertThat(Hash.MD5.hashString(null)).isNull();
	}

	@Test
	public void testSHA1HashShouldReturnNullWhenNullGiven() {
		assertThat(Hash.SHA1.hashString(null)).isNull();
	}

	@Test
	public void test256HashShouldReturnNullWhenNullGiven() {
		assertThat(Hash.SHA256.hashString(null)).isNull();
	}

	@Test
	public void testSHA512HashShouldReturnNullWhenNullGiven() {
		assertThat(Hash.SHA512.hashString(null)).isNull();
	}
}
