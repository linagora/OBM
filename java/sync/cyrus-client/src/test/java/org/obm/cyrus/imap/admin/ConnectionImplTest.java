/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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
package org.obm.cyrus.imap.admin;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.SlowGuiceRunner;
import org.obm.push.minig.imap.StoreClient;

@RunWith(SlowGuiceRunner.class)
public class ConnectionImplTest {
	private IMocksControl control;

	@Before
	public void setUp() {
		control = createControl();
	}
	
	@Test
	public void testCreateUserMailboxes() {
		StoreClient mockClient = control.createMock(StoreClient.class);
		
				
		expect(mockClient.create("user/ident4@vm.obm.org", "partition")).andReturn(true);
		expect(mockClient.create("user/ident4@vm.obm.org/Trash", "partition")).andReturn(true);
		
		control.replay();
	
		Connection conn = new ConnectionImpl(mockClient);
		conn.createUserMailboxes(Partition.of("partition"),
				ImapPath.builder().user("ident4@vm.obm.org").build(),
				ImapPath.builder().user("ident4@vm.obm.org").pathFragment("Trash").build()
				);
		control.verify();
	}

}
