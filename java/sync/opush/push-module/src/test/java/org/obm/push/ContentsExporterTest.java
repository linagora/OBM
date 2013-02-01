/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;

@RunWith(SlowFilterRunner.class)
public class ContentsExporterTest {

	private IMocksControl mocks;
	private Backends backends;

	private ContentsExporter testee;
	
	private User user;
	private Device device;
	private Credentials credentials;
	private UserDataRequest udr;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		credentials = new Credentials(user, "password");
		udr = new UserDataRequest(credentials, "noCommand", device);
		
		mocks = createControl();
		backends = mocks.createMock(Backends.class);
		
		testee = new ContentsExporter(backends);
	}

	@Test
	public void testGetChangedOnBackend() throws Exception {
		int collectionId = 15;
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setItemSyncState(ItemSyncState.builder()
				.syncDate(date("2012-05-04T11:22:53"))
				.syncKey(new SyncKey("123"))
				.id(5)
				.build());
		SyncClientCommands clientCommands = SyncClientCommands.empty();

		PIMBackend emailBackend = mocks.createMock(PIMBackend.class);
		expect(backends.getBackend(PIMDataType.EMAIL)).andReturn(emailBackend);

		SyncKey allocatedSyncKey = new SyncKey("456");
		DataDelta backendDataDelta = DataDelta.newEmptyDelta(date("2012-05-04T12:22:53"), allocatedSyncKey);
		expect(emailBackend.getChanged(udr, syncCollection, clientCommands, allocatedSyncKey))
			.andReturn(backendDataDelta);
		
		mocks.replay();
		DataDelta dataDelta = testee.getChanged(udr, syncCollection, clientCommands, allocatedSyncKey);
		mocks.verify();
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(date("2012-05-04T12:22:53"));
		assertThat(dataDelta.getSyncKey()).isEqualTo(allocatedSyncKey);
		assertThat(dataDelta.getChanges()).isEmpty();
		assertThat(dataDelta.getDeletions()).isEmpty();
	}
	
}
