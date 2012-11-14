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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.FolderBackend;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.HierarchyItemsChanges.Builder;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.mail.MailBackend;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class HierarchyExporterTest {

	private User user;
	private Device device;
	private UserDataRequest userDataRequest;
	
	@Before
	public void setUp() {
		this.user = Factory.create().createUser("test@test", "test@domain", "displayName");
		this.device = new Device.Factory().create(null, "iPhone", "iOs 5", "my phone");
		this.userDataRequest = new UserDataRequest(new Credentials(user, "password"), "noCommand", device, null);
	}
	
	@Test
	public void testHierarchyItemsChangesBuilder() {
		HierarchyItemsChanges itemsChanges = HierarchyItemsChanges.builder().build();
		assertThat(itemsChanges.getChangedItems()).isEmpty();
		assertThat(itemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test(expected=NullPointerException.class)
	public void testHierarchyItemsChangesBuilderChangesNPE() {
		HierarchyItemsChanges.builder().changes(null).build();
	}

	@Test(expected=NullPointerException.class)
	public void testHierarchyItemsChangesBuilderDeletionsNPE() {
		HierarchyItemsChanges.builder().deletions(null).build();
	}
	
	@Test
	public void testHierarchyItemsChangesBuilderMergeItems() {
		ItemChange item1 = new ItemChange("1-ADD");
		ItemChange item2 = new ItemChange("1.1-ADD");
		ItemChange item3 = new ItemChange("2-ADD");
		ItemChange item4 = new ItemChange("2-REMOVE");

		HierarchyItemsChanges hierarchyItemsChanges1 = HierarchyItemsChanges.builder()
			.changes(Lists.newArrayList(item1, item2)).build();
		
		HierarchyItemsChanges hierarchyItemsChanges2 = HierarchyItemsChanges.builder()
		.changes(Lists.newArrayList(item3)).deletions(Lists.newArrayList(item4)).build();
		
		HierarchyItemsChanges hierarchyItemsChanges = HierarchyItemsChanges.builder()
				.mergeItems(hierarchyItemsChanges1)
				.mergeItems(hierarchyItemsChanges2).build();
		
		assertThat(hierarchyItemsChanges.getChangedItems())
			.containsOnly(item1, item2, item3);
		
		assertThat(hierarchyItemsChanges.getDeletedItems()).containsOnly(item4);
	}
	
	@Test
	public void testNothingChanges() throws Exception {
		Date currentDate = DateUtils.getCurrentDate();
		FolderSyncState incomingSyncState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncKey = new FolderSyncState("1234567890b");
		outgoingSyncKey.setLastSync(currentDate);
		
		FolderBackend folderExporter = createStrictMock(FolderBackend.class);

		ContactsBackend contactsBackend = createMock(ContactsBackend.class);
		CalendarBackend calendarBackend = createMock(CalendarBackend.class);
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

		HierarchyItemsChanges hierarchyItemsChanges =
				hierarchyExporter.getChanged(userDataRequest, incomingSyncState, outgoingSyncKey);
		
		verify(mailBackend, calendarBackend, contactsBackend, mappingService);
		
		assertThat(hierarchyItemsChanges.getChangedItems()).isEmpty();
		assertThat(hierarchyItemsChanges.getDeletedItems()).isEmpty();
	}
	
	@Test
	public void testFolderChanges() throws Exception {
		String contactCollectionId = "5";
		String mailCollectionId = "2";
		Date currentDate = DateUtils.getCurrentDate();
		FolderSyncState incomingSyncState = new FolderSyncState("1234567890a");
		FolderSyncState outgoingSyncState = new FolderSyncState("1234567890b");
		outgoingSyncState.setLastSync(currentDate);
		
		FolderBackend folderExporter = createStrictMock(FolderBackend.class);

		ContactsBackend contactsBackend = createMock(ContactsBackend.class);
		CalendarBackend calendarBackend = createMock(CalendarBackend.class);
		MailBackend mailBackend = createMock(MailBackend.class);

		expectGetPIMDataType(contactsBackend, calendarBackend, mailBackend);
		
		HierarchyItemsChanges contactHierarchyItemsChanges = buildHierarchyItemsChanges(contactCollectionId);
		expectHierarchyChangesForBackend(contactsBackend, incomingSyncState, outgoingSyncState, contactHierarchyItemsChanges);
		expectHierarchyChangesForBackend(calendarBackend, incomingSyncState, outgoingSyncState, buildEmptyHierarchyItemsChanges());
		HierarchyItemsChanges mailHierarchyItemsChanges = buildHierarchyItemsChanges(mailCollectionId);
		expectHierarchyChangesForBackend(mailBackend, incomingSyncState, outgoingSyncState, mailHierarchyItemsChanges);

		MappingService mappingService = createMock(MappingService.class);
		
		expectCreateBackendMappingForBackends(mappingService, outgoingSyncState,
				PIMDataType.CONTACTS, PIMDataType.CALENDAR, PIMDataType.EMAIL);
		
		replay(folderExporter, mailBackend, calendarBackend, contactsBackend, mappingService);
		
		IHierarchyExporter hierarchyExporter = buildHierarchyExporter(
				folderExporter, mappingService, contactsBackend, calendarBackend, mailBackend);
		
		HierarchyItemsChanges hierarchyItemsChanges =
				hierarchyExporter.getChanged(userDataRequest, incomingSyncState, outgoingSyncState);
		
		verify(mailBackend, calendarBackend, contactsBackend, mappingService);
		
		Builder builder = HierarchyItemsChanges.builder()
			.mergeItems(mailHierarchyItemsChanges);
		
		assertThat(hierarchyItemsChanges).equals(builder.build());
	}

	private void expectCreateBackendMappingForBackends(MappingService mappingService,
		FolderSyncState outgoingSyncKey, PIMDataType...backendsType) throws DaoException {

		for (PIMDataType backendType : backendsType) {
			mappingService.createBackendMapping(backendType, outgoingSyncKey);
			expectLastCall();
		}
	}

	private void expectGetPIMDataType(ContactsBackend contactsBackend,
			CalendarBackend calendarBackend, MailBackend mailBackend) {
		
		expect(contactsBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS).anyTimes();
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR).anyTimes();
		expect(mailBackend.getPIMDataType()).andReturn(PIMDataType.EMAIL).anyTimes();
	}
	
	private IHierarchyExporter buildHierarchyExporter(FolderBackend folderExporter, MappingService mappingService,
			ContactsBackend contactsBackend, CalendarBackend calendarBackend, MailBackend mailBackend) {
		
		Backends backends = buildBackends(contactsBackend, calendarBackend, mailBackend);
		return new HierarchyExporter(folderExporter, backends, mappingService);
	}

	private Backends buildBackends(ContactsBackend contactsBackend,
			CalendarBackend calendarBackend, MailBackend mailBackend) {
		
		return new Backends(Sets.newHashSet(contactsBackend, calendarBackend, mailBackend));
	}
	
	private HierarchyItemsChanges buildEmptyHierarchyItemsChanges() {
		return HierarchyItemsChanges.builder().build();
	}
	
	private void expectHierarchyChangesForBackend(PIMBackend backend,
		FolderSyncState incomingSyncState, FolderSyncState outgoingSyncState, HierarchyItemsChanges hierarchyItemsChanges) 
				throws DaoException, InvalidSyncKeyException {
		
		expect(backend.getHierarchyChanges(userDataRequest, incomingSyncState, outgoingSyncState))
			.andReturn(hierarchyItemsChanges).once();
	}

	private HierarchyItemsChanges buildHierarchyItemsChanges(String collectionId) {
		return HierarchyItemsChanges.builder()
			.changes(buildItemChanged(collectionId))
			.deletions(buildItemDeleted(collectionId))
			.build();
	}
	
	private List<ItemChange> buildItemChanged(String collectionId) {
		return Lists.newArrayList(
				new ItemChange(collectionId, "1", "FOLDER ONE", FolderType.USER_CREATED_EMAIL_FOLDER, false),
				new ItemChange(collectionId, "2", "FOLDER TWO", FolderType.USER_CREATED_EMAIL_FOLDER, false));
	}
	
	private List<ItemChange> buildItemDeleted(String collectionId) {
		return Lists.newArrayList(
				new ItemChange(collectionId, "3", "FOLDER DELETED", FolderType.USER_CREATED_EMAIL_FOLDER, false));
	}
	
}
