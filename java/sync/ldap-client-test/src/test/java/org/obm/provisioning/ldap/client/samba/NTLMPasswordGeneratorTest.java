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
package org.obm.provisioning.ldap.client.samba;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.provisioning.ldap.client.bean.NTLMPassword;

public class NTLMPasswordGeneratorTest {

	@Test
	public void testComputeNTLMPasswordWithusera() throws Exception {
		verifyHashes("usera", "7A6353920EF1C89FAAD3B435B51404EE", "7563D2F3CFF16BA4BFC0A22B014F3DFF");
	}

	@Test
	public void testComputeNTLMPasswordWithUSERA() throws Exception {
		verifyHashes("USERA", "7A6353920EF1C89FAAD3B435B51404EE", "D9E7D63B06D47A2CB95596F6A3A5B9E2");
	}

	@Test
	public void testComputeNTLMPasswordWithPassword() throws Exception {
		verifyHashes("Password", "E52CAC67419A9A224A3B108F3FA6CB6D", "A4F49C406510BDCAB6824EE7C30FD852");
	}

	@Test
	public void testComputeNTLMPasswordWithPASSWORD() throws Exception {
		verifyHashes("PASSWORD", "E52CAC67419A9A224A3B108F3FA6CB6D", "7B592E4F8178B4C75788531B2E747687");
	}

	@Test
	public void testComputeNTLMPasswordWithsamba() throws Exception {
		verifyHashes("samba", "DF7D4C80BE72A070AAD3B435B51404EE", "B3A3496D3F61D8CDA3B865A2B4B29A37");
	}

	private void verifyHashes(String password, String lmHash, String ntHash) throws Exception {
		assertThat(NTLMPasswordGenerator.computeNTLMPassword(password)).isEqualTo(NTLMPassword
				.builder()
				.lmHash(lmHash)
				.ntHash(ntHash)
				.build());
	}

}
