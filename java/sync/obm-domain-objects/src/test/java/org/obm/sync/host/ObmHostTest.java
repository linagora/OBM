/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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

package org.obm.sync.host;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.sync.host.ObmHost;

public class ObmHostTest {
	
	@Test(expected = NullPointerException.class)
	public void testBuildWhenNullDomainIdGiven() {
		ObmHost.builder().domainId(null).build();
	}
	
	@Test(expected = NullPointerException.class)
	public void testBuildWhenNullNameGiven() {
		ObmHost.builder().name(null).build();
	}

	@Test
	public void testBuildWhenNullFqdnGiven() {
		ObmHost.builder().fqdn(null).build();
	}
	
	@Test
	public void testBuildWhenNullIpGiven() {
		ObmHost.builder().ip(null).build();
	}
	
	@Test
	public void localhostIs127_0_0_1() {
		ObmHost expected = ObmHost.builder().ip("127.0.0.1").build();
		ObmHost host = ObmHost.builder().localhost().build();
		assertThat(host).isEqualToComparingFieldByField(expected).isEqualTo(expected);
	}
}
