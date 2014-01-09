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
package org.obm.push;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.FolderBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.mail.MailBackend;
import org.obm.push.service.impl.MappingService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class HierarchyExporterTest {

	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	
	@Before
	public void setUp() {
		this.user = Factory.create().createUser("test@test", "test@domain", "displayName");
		this.device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		this.userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);
	}
	
	@Test
	public void testHierarchyItemsChangesBuilder() {
		HierarchyCollectionChanges itemsChanges = HierarchyCollectionChanges.builder().build();
		assertThat(itemsChanges.getCollectionChanges()).isEmpty();
		assertThat(itemsChanges.getCollectionDeletions()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testHierarchyItemsChangesBuilderChangesNPE() {
		HierarchyCollectionChanges.builder().changes(null).build();
	}

	@Test(expected=NullPointerException.class)
	public void testHierarchyItemsChangesBuilderDeletionsNPE() {
		HierarchyCollectionChanges.builder().deletions(null).build();
	}
	
	@Test
	public void testHierarchyItemsChangesBuilderMergeItems() {
		CollectionChange item1 = CollectionChange.builder()
				.collectionId("1-ADD")
				.parentCollectionId("0")
				.displayName("1")
				.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
				.isNew(true)
				.build();
		CollectionChange item2 = CollectionChange.builder()
				.collectionId("1.1-ADD")
				.parentCollectionId("0")
				.displayName("2")
				.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
				.isNew(true)
				.build();
		CollectionChange item3 = CollectionChange.builder()
				.collectionId("2-ADD")
				.parentCollectionId("0")
				.displayName("3")
				.folderType(FolderType.DEFAULT_CALENDAR_FOLDER)
				.isNew(true)
				.build();
		CollectionDeletion item4 = CollectionDeletion.builder()
				.collectionId("2-REMOVE")
				.build();

		HierarchyCollectionChanges hierarchyItemsChanges1 = HierarchyCollectionChanges.builder()
			.changes(Lists.newArrayList(item1, item2)).build();
		
		HierarchyCollectionChanges hierarchyItemsChanges2 = HierarchyCollectionChanges.builder()
		.changes(Lists.newArrayList(item3)).deletions(Lists.newArrayList(item4)).build();
		
		HierarchyCollectionChanges hierarchyItemsChanges = HierarchyCollectionChanges.builder()
				.mergeItems(hierarchyItemsChanges1)
				.mergeItems(hierarchyItemsChanges2).build();
		
		assertThat(hierarchyItemsChanges.getCollectionChanges())
			.containsOnly(item1, item2, item3);
		
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).containsOnly(item4);
	}
	
	@Test
	public void testNothingChanges() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234567890a"));
		FolderSyncState outgoingSyncKey = buildFolderSyncState(new SyncKey("1234567890b"));
		
		FolderBackend folderExporter = createStrictMock(FolderBackend.class);

		PIMBackend contactsBackend = createMock(PIMBackend.class);
		PIMBackend calendarBackend = createMock(PIMBackend.class);
		MailBackend mailBackend = createMock(MailBackend.class);

		expectGetPIMDataType(contactsBackend, calendarBackend, mailBackend);

		expectHierarchyChangesForBackend(contactsBackend, incomingSyncState, outgoingSyncKey, buildEmptyHierarchyItemsChanges());
		expectHierarchyChangesForBackend(calendarBackend, incomingSyncState, outgoingSyncKey, buildEmptyHierarchyItemsChanges());
		expectHierarchyChangesForBackend(mailBackend, incomingSyncState, outgoingSyncKey, buildEmptyHierarchyItemsChanges());

		MappingService mappingService = createMock(MappingService.class);
		
		expectCreateBackendMappingForBackends(mappingService, outgoingSyncKey,
				PIMDataType.CONTACTS, PIMDataType.CALENDAR, PIMDataType.EMAIL);
		
		replay(folderExporter, mailBackend, calendarBackend, contactsBackend, mappingService);

		IHierarchyExporter hierarchyExporter = buildHierarchyExporter(
				folderExporter, mappingService, contactsBackend, calendarBackend, mailBackend);

		HierarchyCollectionChanges hierarchyItemsChanges =
				hierarchyExporter.getChanged(userDataRequest, incomingSyncState, outgoingSyncKey);
		
		verify(mailBackend, calendarBackend, contactsBackend, mappingService);
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).isEmpty();
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).isEmpty();
	}
	
	@Test
	public void testFolderChanges() throws Exception {
		String contactParentCollectionId = "5";
		String mailParentCollectionId = "2";
		
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234567890a"));
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234567890b"));
		
		FolderBackend folderExporter = createStrictMock(FolderBackend.class);

		PIMBackend contactsBackend = createMock(PIMBackend.class);
		PIMBackend calendarBackend = createMock(PIMBackend.class);
		MailBackend mailBackend = createMock(MailBackend.class);

		expectGetPIMDataType(contactsBackend, calendarBackend, mailBackend);
		
		CollectionChange contact1 = CollectionChange.builder()
				.collectionId("1")
				.parentCollectionId(contactParentCollectionId)
				.displayName("ONE")
				.folderType(FolderType.USER_CREATED_CONTACTS_FOLDER)
				.isNew(true).build();
		CollectionChange contact2 = CollectionChange.builder()
				.collectionId("2")
				.parentCollectionId(contactParentCollectionId)
				.displayName("TWO")
				.folderType(FolderType.USER_CREATED_CONTACTS_FOLDER)
				.isNew(true).build(); 
		CollectionDeletion contactDeleted = CollectionDeletion.builder()
				.collectionId("3")
				.build();
		expectHierarchyChangesForBackend(contactsBackend, incomingSyncState, outgoingSyncState,
				HierarchyCollectionChanges.builder()
					.changes(ImmutableList.of(contact1, contact2))
					.deletions(ImmutableList.of(contactDeleted)).build());

		CollectionChange mail1 = CollectionChange.builder()
				.collectionId("1")
				.parentCollectionId(mailParentCollectionId)
				.displayName("ONE")
				.folderType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.isNew(true).build();  
		CollectionChange mail2 = CollectionChange.builder()
				.collectionId("2")
				.parentCollectionId(mailParentCollectionId)
				.displayName("TWO")
				.folderType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.isNew(true).build();
		CollectionDeletion mailDeleted = CollectionDeletion.builder()
				.collectionId("3")
				.build();
		expectHierarchyChangesForBackend(mailBackend, incomingSyncState, outgoingSyncState, 
				HierarchyCollectionChanges.builder()
				.changes(ImmutableList.of(mail1, mail2))
				.deletions(ImmutableList.of(mailDeleted)).build());
		
		expectHierarchyChangesForBackend(calendarBackend, incomingSyncState, outgoingSyncState,
				buildEmptyHierarchyItemsChanges());

		MappingService mappingService = createMock(MappingService.class);
		
		expectCreateBackendMappingForBackends(mappingService, outgoingSyncState,
				PIMDataType.CONTACTS, PIMDataType.CALENDAR, PIMDataType.EMAIL);
		
		replay(folderExporter, mailBackend, calendarBackend, contactsBackend, mappingService);
		
		IHierarchyExporter hierarchyExporter = buildHierarchyExporter(
				folderExporter, mappingService, contactsBackend, calendarBackend, mailBackend);
		
		HierarchyCollectionChanges hierarchyItemsChanges =
				hierarchyExporter.getChanged(userDataRequest, incomingSyncState, outgoingSyncState);
		
		verify(mailBackend, calendarBackend, contactsBackend, mappingService);
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).containsOnly(contact1, contact2, mail1, mail2);
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).containsOnly(contactDeleted, mailDeleted);
	}

	private void expectCreateBackendMappingForBackends(MappingService mappingService,
		FolderSyncState outgoingSyncKey, PIMDataType...backendsType) throws DaoException {

		for (PIMDataType backendType : backendsType) {
			mappingService.createBackendMapping(backendType, outgoingSyncKey);
			expectLastCall();
		}
	}

	private void expectGetPIMDataType(PIMBackend contactsBackend,
                                      PIMBackend calendarBackend, MailBackend mailBackend) {
		
		expect(contactsBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS).anyTimes();
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(mailBackend.getPIMDataType()).andReturn(PIMDataType.EMAIL).anyTimes();
	}
	
	private IHierarchyExporter buildHierarchyExporter(FolderBackend folderExporter, MappingService mappingService,
			PIMBackend contactsBackend, PIMBackend calendarBackend, MailBackend mailBackend) {
		
		Backends backends = buildBackends(contactsBackend, calendarBackend, mailBackend);
		return new HierarchyExporter(folderExporter, backends, mappingService);
	}

	private Backends buildBackends(PIMBackend contactsBackend,
			PIMBackend calendarBackend, MailBackend mailBackend) {
		
		return new Backends(Sets.newHashSet(contactsBackend, calendarBackend, mailBackend));
	}
	
	private HierarchyCollectionChanges buildEmptyHierarchyItemsChanges() {
		return HierarchyCollectionChanges.builder().build();
	}
	
	private void expectHierarchyChangesForBackend(PIMBackend backend,
		FolderSyncState incomingSyncState, FolderSyncState outgoingSyncState, HierarchyCollectionChanges hierarchyItemsChanges) 
				throws DaoException, InvalidSyncKeyException {
		
		expect(backend.getHierarchyChanges(userDataRequest, incomingSyncState, outgoingSyncState))
			.andReturn(hierarchyItemsChanges).once();
	}

	private FolderSyncState buildFolderSyncState(SyncKey syncKey) {
		return FolderSyncState.builder()
				.syncKey(syncKey)
				.build();
	}
}
