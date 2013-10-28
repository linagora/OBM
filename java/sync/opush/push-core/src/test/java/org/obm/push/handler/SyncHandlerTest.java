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
package org.obm.push.handler;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.ContentsImporter;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.client.SyncClientCommands;

@RunWith(SlowFilterRunner.class)
public class SyncHandlerTest {

	private User user;
	private Device device;
	private UserDataRequest udr;

	private IMocksControl mocks;
	private ContentsImporter contentsImporter;
	private SyncHandler testee;

	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);

		mocks = createControl();
		contentsImporter = mocks.createMock(ContentsImporter.class);
		
		testee = new SyncHandler(null, null, contentsImporter, null, null, null, null,
				null, null, null, null, null, null, false, null, null);
	}

	@Test
	public void testProcessModificationForFetch() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.commandType(SyncCommand.FETCH)
								.build())
						.build())
				.build();

		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).isEmpty();
	}

	@Test
	public void testProcessModificationForFetchWithClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId("1234")
								.commandType(SyncCommand.FETCH)
								.build())
						.build())
				.build();

		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).isEmpty();
	}
	
	@Test
	public void testProcessModificationForModify() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.commandType(SyncCommand.MODIFY)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", null, null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Update("15:3"));
	}
	
	@Test
	public void testProcessModificationForModifyWithClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId("1234")
								.commandType(SyncCommand.MODIFY)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", "1234", null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Update("15:3"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testProcessModificationForAddNoClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.commandType(SyncCommand.ADD)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", null, null)).andReturn("15:3");
		
		mocks.replay();
		testee.processClientModification(udr, collection);
	}
	
	@Test
	public void testProcessModificationForAddWithClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId("1234")
								.commandType(SyncCommand.ADD)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", "1234", null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).containsOnly(new SyncClientCommands.Add("1234", "15:3"));
		assertThat(clientCommands.getChanges()).isEmpty();
	}
	
	@Test
	public void testProcessModificationForAddWithOnlyClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId(null)
								.clientId("1234")
								.commandType(SyncCommand.ADD)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, null, "1234", null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).containsOnly(new SyncClientCommands.Add("1234", "15:3"));
		assertThat(clientCommands.getChanges()).isEmpty();
	}
	
	@Test
	public void testProcessModificationForDelete() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.dataType(PIMDataType.EMAIL)
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId(null)
								.commandType(SyncCommand.DELETE)
								.build())
						.build())
				.build();

		contentsImporter.importMessageDeletion(udr, PIMDataType.EMAIL, 15, "15:2", true);
		expectLastCall();
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();

		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Deletion("15:2"));
	}
	
	@Test
	public void testProcessModificationForDeleteWithClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.dataType(PIMDataType.EMAIL)
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId("1234")
								.commandType(SyncCommand.DELETE)
								.build())
						.build())
				.build();

		contentsImporter.importMessageDeletion(udr, PIMDataType.EMAIL, 15, "15:2", true);
		expectLastCall();
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();

		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Deletion("15:2"));
	}

	@Test
	public void testProcessModificationForChange() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId(null)
								.commandType(SyncCommand.CHANGE)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", null, null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();

		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Update("15:3"));
	}

	@Test
	public void testProcessModificationForChangeWithClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId("15:2")
								.clientId("1234")
								.commandType(SyncCommand.CHANGE)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, "15:2", "1234", null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();

		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Update("15:3"));
	}
	
	@Test
	public void testProcessModificationForChangeWithOnlyClientId() throws Exception {
		AnalysedSyncCollection collection = AnalysedSyncCollection.builder()
				.collectionId(15)
				.syncKey(new SyncKey("123"))
				.commands(SyncCollectionCommands.Response.builder()
						.addCommand(SyncCollectionCommand.Response.builder()
								.serverId(null)
								.clientId("1234")
								.commandType(SyncCommand.CHANGE)
								.build())
						.build())
				.build();

		expect(contentsImporter.importMessageChange(udr, 15, null, "1234", null)).andReturn("15:3");
		
		mocks.replay();
		SyncClientCommands clientCommands = testee.processClientModification(udr, collection);
		mocks.verify();
		
		assertThat(clientCommands.getAdds()).isEmpty();
		assertThat(clientCommands.getChanges()).containsOnly(new SyncClientCommands.Update("15:3"));
	}
}
