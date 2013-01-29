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
package org.obm.push.mail;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.push.mail.MSMailTestsUtils.loadEmail;
import static org.obm.push.mail.MSMailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.CollectionPath;
import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.backend.OpushCollection;
import org.obm.push.bean.Address;
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
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MailboxFolders;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.service.impl.MappingService;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.inject.Provider;

@RunWith(SlowFilterRunner.class)
public class MailBackendTest {

	private static final String COLLECTION_MAIL_PREFIX = "obm:\\\\test@test\\email\\";
	
	private User user;
	private Device device;
	private UserDataRequest udr;
	private MailboxService mailboxService;
	private MappingService mappingService;
	private WindowingService windowingService;
	private Provider<Builder> collectionPathBuilderProvider;
	private CollectionPath.Builder collectionPathBuilder;
	private MailBackend testee;


	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		udr = new UserDataRequest(new Credentials(user, "password"), "noCommand", device);
		collectionPathBuilder = createMock(Builder.class);
		expect(collectionPathBuilder.userDataRequest(udr)).andReturn(collectionPathBuilder).anyTimes();
		expect(collectionPathBuilder.pimType(PIMDataType.EMAIL)).andReturn(collectionPathBuilder).anyTimes();
		collectionPathBuilderProvider = createMock(Provider.class);
		expect(collectionPathBuilderProvider.get()).andReturn(collectionPathBuilder).anyTimes();
		mailboxService = createMock(MailboxService.class);
		mappingService = createMock(MappingService.class);
		windowingService = createMock(WindowingService.class);
		
		testee = new MailBackendImpl(mailboxService, null, null, null, null, null, null,
				mappingService, null, null, collectionPathBuilderProvider, null, windowingService);
	}
	
	private void replayCommonMocks() {
		replay(mailboxService, mappingService, collectionPathBuilderProvider, collectionPathBuilder);
	}
	
	private void verifyCommonMocks() {
		verify(mailboxService, mappingService, collectionPathBuilderProvider, collectionPathBuilder);
	}
	
	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException, AuthFault {
		final String password = "pass";
		final AccessToken at = new AccessToken(1, "o-push");
		
		MailboxService mailboxService = createMock(MailboxService.class);
		ICalendar calendarClient = createMock(ICalendar.class);
		UserDataRequest userDataRequest = createMock(UserDataRequest.class);
		LoginService login = createMock(LoginService.class);
		
		expect(userDataRequest.getUser()).andReturn(user).once();
		expect(userDataRequest.getPassword()).andReturn(password).once();

		expect(login.login(user.getLoginAtDomain(), password)).andReturn(at).once();
		expect(calendarClient.getUserEmail(at)).andReturn(user.getLoginAtDomain()).once();
		login.logout(at);
		expectLastCall().once();
		Set<Address> addrs = Sets.newHashSet();
		mailboxService.sendEmail(anyObject(UserDataRequest.class), anyObject(Address.class), 
				anyObject(addrs.getClass()), anyObject(addrs.getClass()), anyObject(addrs.getClass()), 
				anyObject(InputStream.class), anyBoolean());
		
		expectLastCall().once();
				
		MailBackend mailBackend = new MailBackendImpl(mailboxService, calendarClient, login, new Mime4jUtils(),
				mockOpushConfigurationService(), null, null, mappingService, null, null,
				collectionPathBuilderProvider, null, windowingService);

		replay(mailboxService, calendarClient, userDataRequest, login);
		replayCommonMocks();
		
		InputStream emailStream = loadEmail("bigEml.eml");
		mailBackend.sendEmail(userDataRequest, ByteStreams.toByteArray(emailStream), true);
		
		verify(mailboxService, calendarClient, userDataRequest, login);
		verifyCommonMocks();
	}
	
	private void expectBuildMailboxesCollectionPaths(Map<String, Integer> mailboxesIds) {
		
		for(Entry<String, Integer> mailbox : mailboxesIds.entrySet()) {
			expect(collectionPathBuilder.backendName(mailbox.getKey())).andReturn(collectionPathBuilder).anyTimes();
			expect(collectionPathBuilder.build()).andReturn(new MailCollectionPath(mailbox.getKey())).once();
		}
	}
	
	@Test
	public void initialHierarchyContainsBaseFolders() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(SyncKey.INITIAL_FOLDER_SYNC_KEY);
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234"));

		Map<String, Integer> mailboxesIds = ImmutableMap.of(
			"INBOX", 1,
			"Drafts", 2,
			"Sent", 3,
			"Trash", 4);
		
		expectBuildMailboxesCollectionPaths(mailboxesIds);
		
		expectMappingServiceSearchThenCreateCollection(mailboxesIds);
		expectMappingServiceSnapshot(outgoingSyncState, mailboxesIds.values());
		expectMappingServiceLookupCollection(mailboxesIds);
		expectMappingServiceListLastKnowCollection(incomingSyncState, ImmutableList.<CollectionPath>of());
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replayCommonMocks();
		
		HierarchyCollectionChanges hierarchyItemsChanges = testee.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();
		
		CollectionChange inboxItemChange = CollectionChange.builder().collectionId("1")
			.parentCollectionId("0").folderType(FolderType.DEFAULT_INBOX_FOLDER)
			.displayName("INBOX").isNew(true).build();
		
		CollectionChange draftsItemChange = CollectionChange.builder().collectionId("2")
			.parentCollectionId("0").folderType(FolderType.DEFAULT_DRAFTS_FOLDER)
			.displayName("Drafts").isNew(true).build();
		
		CollectionChange sentItemChange = CollectionChange.builder().collectionId("3")
			.parentCollectionId("0").folderType(FolderType.DEFAULT_SENT_EMAIL_FOLDER)
			.displayName("Sent").isNew(true).build();
		
		CollectionChange trashItemChange = CollectionChange.builder().collectionId("4")
			.parentCollectionId("0").folderType(FolderType.DEFAULT_DELETED_ITEMS_FOLDER)
			.displayName("Trash").isNew(true).build();
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).contains(
				inboxItemChange, draftsItemChange, sentItemChange, trashItemChange);

		assertThat(hierarchyItemsChanges.getCollectionDeletions()).isEmpty();
	}

	@Test
	public void emptyHierarchyChanges() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234a"));
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234b"));

		Map<String, Integer> mailboxesIds = ImmutableMap.of(
			"INBOX", 1,
			"Drafts", 2,
			"Sent", 3,
			"Trash", 4);
		
		expectBuildMailboxesCollectionPaths(mailboxesIds);
		expectMappingServiceFindCollection(mailboxesIds);
		expectMappingServiceSnapshot(outgoingSyncState, mailboxesIds.values());
		expectMappingServiceListLastKnowCollection(incomingSyncState, ImmutableList.<CollectionPath>of(
				new MailCollectionPath("INBOX"), new MailCollectionPath("Drafts"),
				new MailCollectionPath("Sent"), new MailCollectionPath("Trash")));
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replayCommonMocks();
		
		HierarchyCollectionChanges hierarchyItemsChanges = testee.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).isEmpty();
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).isEmpty();
	}
	
	@Test
	public void newImapFolder() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234a"));
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234b"));

		Map<String, Integer> mailboxesIds = ImmutableMap.of(
			"INBOX", 1,
			"Drafts", 2,
			"Sent", 3,
			"Trash", 4);

		Map<String, Integer> changeMailboxes = ImmutableMap.of("NewFolder", 5);
		
		expectBuildMailboxesCollectionPaths(mailboxesIds);
		expectBuildMailboxesCollectionPaths(changeMailboxes);
		expectMappingServiceFindCollection(mailboxesIds);
		expectMappingServiceSearchThenCreateCollection(changeMailboxes);
		expectMappingServiceSnapshot(outgoingSyncState, Iterables.concat(mailboxesIds.values(), Sets.newHashSet(5)));
		expectMappingServiceLookupCollection(changeMailboxes);
		expectMappingServiceListLastKnowCollection(incomingSyncState, ImmutableList.<CollectionPath>of(
				new MailCollectionPath("INBOX"), new MailCollectionPath("Drafts"),
				new MailCollectionPath("Sent"), new MailCollectionPath("Trash")));
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("NewFolder"));
		
		replayCommonMocks();
		
		HierarchyCollectionChanges hierarchyItemsChanges = testee.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);
		
		verifyCommonMocks();

		CollectionChange newFolderItemChange = CollectionChange.builder().collectionId("5")
				.parentCollectionId("0").folderType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.displayName("NewFolder").isNew(true).build();
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).containsOnly(newFolderItemChange);
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).isEmpty();
	}

	
	@Test
	public void deletedImapFolder() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234a"));
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234b"));

		Map<String, Integer> mailboxesIds = ImmutableMap.of(
			"INBOX", 1,
			"Drafts", 2,
			"Sent", 3,
			"Trash", 4);
		Map<String, Integer> deletedMailboxes = ImmutableMap.of("deletedFolder", 5);
		
		expectBuildMailboxesCollectionPaths(mailboxesIds);
		expectMappingServiceFindCollection(mailboxesIds);
		expectMappingServiceSnapshot(outgoingSyncState, mailboxesIds.values());
		expectMappingServiceLookupCollection(deletedMailboxes);
		expectMappingServiceListLastKnowCollection(incomingSyncState, ImmutableList.<CollectionPath>of(
				new MailCollectionPath("INBOX"), new MailCollectionPath("Drafts"),
				new MailCollectionPath("Sent"), new MailCollectionPath("Trash"), new MailCollectionPath("deletedFolder")));
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders());
		
		replayCommonMocks();
		
		HierarchyCollectionChanges hierarchyItemsChanges = testee.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);

		verifyCommonMocks();
		
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).containsOnly(
				CollectionDeletion.builder().collectionId("5").build());
		assertThat(hierarchyItemsChanges.getCollectionChanges()).isEmpty();
	}
	
	@Test
	public void deletedAndAddedImapFolders() throws Exception {
		FolderSyncState incomingSyncState = buildFolderSyncState(new SyncKey("1234a"));
		FolderSyncState outgoingSyncState = buildFolderSyncState(new SyncKey("1234b"));

		Map<String, Integer> mailboxesIds = ImmutableMap.of(
			"INBOX", 1,
			"Drafts", 2,
			"Sent", 3,
			"Trash", 4);
		Map<String, Integer> changedMailboxes = ImmutableMap.of("changedFolder", 5);
		Map<String, Integer> deletedMailboxes = ImmutableMap.of("deletedFolder", 6);

		expectBuildMailboxesCollectionPaths(mailboxesIds);
		expectBuildMailboxesCollectionPaths(changedMailboxes);
		expectMappingServiceSearchThenCreateCollection(changedMailboxes);
		expectMappingServiceFindCollection(mailboxesIds);
		expectMappingServiceSnapshot(outgoingSyncState, Iterables.concat(mailboxesIds.values(), changedMailboxes.values()));
		expectMappingServiceLookupCollection(changedMailboxes);
		expectMappingServiceLookupCollection(deletedMailboxes);
		expectMappingServiceListLastKnowCollection(incomingSyncState, ImmutableList.<CollectionPath>of(
				new MailCollectionPath("INBOX"), new MailCollectionPath("Drafts"),
				new MailCollectionPath("Sent"), new MailCollectionPath("Trash"), new MailCollectionPath("deletedFolder")));
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("changedFolder"));
		
		replayCommonMocks();
		
		HierarchyCollectionChanges hierarchyItemsChanges = testee.getHierarchyChanges(udr, incomingSyncState, outgoingSyncState);

		verifyCommonMocks();
		
		CollectionChange newFolderItemChange = CollectionChange.builder()
				.collectionId("5")
				.parentCollectionId("0")
				.folderType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.displayName("changedFolder")
				.isNew(true)
				.build();
		
		CollectionDeletion oldFolderItemDeleted = CollectionDeletion.builder().collectionId("6").build();
		
		assertThat(hierarchyItemsChanges.getCollectionChanges()).containsOnly(newFolderItemChange);
		assertThat(hierarchyItemsChanges.getCollectionDeletions()).containsOnly(oldFolderItemDeleted);
	}

	@Test
	public void collectionDisplayNameForSpecialMailboxes() {
		Map<String, Integer> changedMailboxes = ImmutableMap.of(
				EmailConfiguration.IMAP_INBOX_NAME, 1,
				EmailConfiguration.IMAP_DRAFTS_NAME, 2,
				EmailConfiguration.IMAP_SENT_NAME, 3,
				EmailConfiguration.IMAP_TRASH_NAME, 4);
		
		expectBuildMailboxesCollectionPaths(changedMailboxes);
		
		replayCommonMocks();
		MailBackendImpl mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null,
				mappingService, null, null, collectionPathBuilderProvider, null, windowingService);
		Collection<OpushCollection> specialFolders = mailBackend.listSpecialFolders(udr).collections();
		verifyCommonMocks();

		assertThat(specialFolders).hasSize(4);
		assertThat(Iterables.transform(specialFolders, toDisplayNameFunction()))
			.containsOnly(
				EmailConfiguration.IMAP_INBOX_NAME,
				EmailConfiguration.IMAP_DRAFTS_NAME, 
				EmailConfiguration.IMAP_SENT_NAME,
				EmailConfiguration.IMAP_TRASH_NAME);
	}

	@Test
	public void collectionDisplayNameForSubscribedMailboxes() {
		Map<String, Integer> changedMailboxes = ImmutableMap.of(
				"display name", 1,
				"another display name", 2);
		
		expectBuildMailboxesCollectionPaths(changedMailboxes);
		expect(mailboxService.listSubscribedFolders(udr)).andReturn(mailboxFolders("display name", "another display name"));
		
		replayCommonMocks();
		MailBackendImpl mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null,
				mappingService, null, null, collectionPathBuilderProvider, null, windowingService);
		Collection<OpushCollection> subscribedFolders = mailBackend.listSubscribedFolders(udr).collections();
		verifyCommonMocks();

		
		assertThat(subscribedFolders).hasSize(2);
		assertThat(Iterables.transform(subscribedFolders, toDisplayNameFunction()))
			.containsOnly("display name", "another display name");
	}
	
	@Test
	public void createItemChangeGetsDisplayNameFromOpushCollection() throws Exception {
		CollectionPath collectionPath = createMock(CollectionPath.class);
		expect(collectionPath.collectionPath()).andReturn(COLLECTION_MAIL_PREFIX + "technicalName");
		
		expect(mappingService.collectionIdToString(3)).andReturn("3").anyTimes();
		expect(mappingService.getCollectionIdFor(udr.getDevice(), COLLECTION_MAIL_PREFIX + "technicalName"))
			.andReturn(3).anyTimes();

		OpushCollection collection = OpushCollection.builder()
				.collectionPath(collectionPath)
				.displayName("great display name!")
				.build();
		
		replayCommonMocks(); replay(collectionPath);
		MailBackendImpl mailBackend = new MailBackendImpl(mailboxService, null, null, null, null, null, null,
				mappingService, null, null, collectionPathBuilderProvider, null, windowingService);
		CollectionChange itemChange = mailBackend.createCollectionChange(udr, collection);
		verifyCommonMocks(); verify(collectionPath);
		
		assertThat(itemChange).isEqualTo(CollectionChange.builder()
				.displayName("great display name!")
				.parentCollectionId("0")
				.collectionId("3")
				.folderType(FolderType.USER_CREATED_EMAIL_FOLDER)
				.isNew(true)
				.build());
	}

	@Test
	public void testDeleteItemInTrash() throws Exception {
		int collectionId = 1;
		int itemId = 2;
		String serverId = collectionId + ":" + itemId;
		
		MailCollectionPath trashCollectionPath = new MailCollectionPath(EmailConfiguration.IMAP_TRASH_NAME);
		expect(mappingService.getItemIdFromServerId(serverId))
			.andReturn(itemId).once();
		expect(mappingService.getCollectionPathFor(collectionId))
			.andReturn(trashCollectionPath.collectionPath()).once();
		
		expect(collectionPathBuilder.backendName(EmailConfiguration.IMAP_TRASH_NAME))
			.andReturn(collectionPathBuilder).once();
		expect(collectionPathBuilder.build())
			.andReturn(trashCollectionPath).once();
		
		mailboxService.delete(udr, trashCollectionPath.collectionPath(), MessageSet.singleton(itemId));
		expectLastCall();
		
		replayCommonMocks();
		 
		testee.delete(udr, collectionId, serverId, true);
		verifyCommonMocks();
	}

	private void expectMappingServiceSearchThenCreateCollection(Map<String, Integer> mailboxesIds)
			throws DaoException, CollectionNotFoundException {
		
		for (Entry<String, Integer> mailbox : mailboxesIds.entrySet()) {

			expect(mappingService.getCollectionIdFor(device, COLLECTION_MAIL_PREFIX + mailbox.getKey()))
				.andThrow(new CollectionNotFoundException()).once();
			
			expect(mappingService.createCollectionMapping(device, COLLECTION_MAIL_PREFIX + mailbox.getKey()))
				.andReturn(mailbox.getValue()).once();
		}
	}

	private MailboxFolders mailboxFolders(String... folders) {
		return new MailboxFolders(
				FluentIterable.from(ImmutableList.copyOf(folders))
					.transform(new Function<String, MailboxFolder>() {
							@Override
							public MailboxFolder apply(String input) {
								return new MailboxFolder(input);
							}
						})
					.toList());
	}

	private void expectMappingServiceSnapshot(FolderSyncState outgoingSyncState, Iterable<Integer> collectionIds)
			throws DaoException {

		mappingService.snapshotCollections(outgoingSyncState, Sets.newHashSet(collectionIds));
		expectLastCall();
	}

	private void expectMappingServiceListLastKnowCollection(FolderSyncState incomingSyncState,
			List<CollectionPath> collectionPaths) throws DaoException {
		
		expect(mappingService.listCollections(udr, incomingSyncState))
			.andReturn(collectionPaths).once();
	}
	
	private void expectMappingServiceFindCollection(Map<String, Integer> mailboxesIds)
		throws CollectionNotFoundException, DaoException {

		for (Entry<String, Integer> mailbox : mailboxesIds.entrySet()) {
			expectMappingServiceFindCollection(mailbox.getKey(), mailbox.getValue());
		}
	}
	
	private void expectMappingServiceFindCollection(String collectionPath, Integer collectionId)
		throws CollectionNotFoundException, DaoException {
		
		expect(mappingService.getCollectionIdFor(device, COLLECTION_MAIL_PREFIX + collectionPath))
			.andReturn(collectionId).once();
	}
	
	private void expectMappingServiceLookupCollection(Map<String, Integer> mailboxesIds)
		throws CollectionNotFoundException, DaoException {

		for (Entry<String, Integer> mailbox : mailboxesIds.entrySet()) {
			expectMappingServiceFindCollection(mailbox.getKey(), mailbox.getValue());
			expect(mappingService.collectionIdToString(mailbox.getValue()))
				.andReturn(String.valueOf(mailbox.getValue())).once();
		}
	}
	
	private static class MailCollectionPath extends CollectionPath {

		public MailCollectionPath(String displayName) {
			super(COLLECTION_MAIL_PREFIX + displayName, PIMDataType.EMAIL, displayName);
		}
	}

	private Function<OpushCollection, String> toDisplayNameFunction() {
		return new Function<OpushCollection, String>() {
			
			@Override
			public String apply(OpushCollection opushCollection) {
				return opushCollection.displayName();
			}
		};
	}
	
	private FolderSyncState buildFolderSyncState(SyncKey syncKey) {
		return FolderSyncState.builder()
				.syncKey(syncKey)
				.build();
	}
}
