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
package org.obm.push.mail.imap;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

import java.io.InputStream;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;

import com.google.inject.Provider;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public class ManagedLifecycleImapStoreMockTest {

	private ImapStoreManagerImpl imapStoreManagerMock;

	@Test
	public void testLogoutMustCallCloseWhenDone() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToCallCloseWhenDone();
		
		imapStore.logout();
		
		EasyMock.verify(imapStoreManagerMock);
	}

	@Test
	public void testLogoutMustCallParentClose() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToCallParentClose();
		
		imapStore.logout();
		
		EasyMock.verify(imapStoreManagerMock);
	}

	@Test
	public void testGetMessageStreamMustCallBindTo() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToCallBindTo();
		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);
		
		long anyMessageUID = 1l;
		imapFolder.getMessageInputStream(anyMessageUID);
		
		EasyMock.verify(imapStoreManagerMock);
	}

	@Test
	public void testFetchPartMustCallBindTo() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToCallBindTo();
		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);

		long anyMessageUID = 1l;
		String anyMimePartAddress = "1.2.1";
		imapFolder.uidFetchPart(anyMessageUID, anyMimePartAddress);
		
		EasyMock.verify(imapStoreManagerMock);
	}

	@Test
	public void testFetchEmailDoesntCallBindTo() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToNeverCallBindTo();
		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);

		long anyMessageUID = 1l;
		imapFolder.fetch(anyMessageUID, new FetchProfile());
		
		EasyMock.verify(imapStoreManagerMock);
	}

	@Test
	public void testFetchEnvelopeDoesntCallBindTo() throws Exception {
		ImapStore imapStore = mockManagedImapStoreToNeverCallBindTo();
		OpushImapFolder imapFolder = imapStore.select(EmailConfiguration.IMAP_INBOX_NAME);

		long anyMessageUID = 1l;
		imapFolder.fetchEnvelope(anyMessageUID);
		
		EasyMock.verify(imapStoreManagerMock);
	}
	
	private ImapStore mockManagedImapStoreToCallParentClose() throws MessagingException {
		Provider<ImapStoreManager> imapStoreManagerFactory = mockImapStoreManagerProvider();

		IMAPStore imapStoreMock = mockIMAPStore();
		expectCallToCloseWhenDone();
		expectCallToParentClose(imapStoreMock);
		
		return createManagedLifecycleImapStore(imapStoreManagerFactory, imapStoreMock);
	}
	
	private ImapStore mockManagedImapStoreToCallCloseWhenDone() throws MessagingException {
		Provider<ImapStoreManager> imapStoreManagerFactory = mockImapStoreManagerProvider();

		expectCallToCloseWhenDone();
		
		return createManagedLifecycleImapStore(imapStoreManagerFactory, mockIMAPStore());
	}

	private ImapStore mockManagedImapStoreToCallBindTo() throws MessagingException {
		Provider<ImapStoreManager> imapStoreManagerFactory = mockImapStoreManagerProvider();

		expectCallToBindTo();
		
		return createManagedLifecycleImapStore(imapStoreManagerFactory, mockIMAPStore());
	}

	private ImapStore mockManagedImapStoreToNeverCallBindTo() throws MessagingException {
		Provider<ImapStoreManager> imapStoreManagerFactory = mockImapStoreManagerProvider();

		return createManagedLifecycleImapStore(imapStoreManagerFactory, mockIMAPStore());
	}

	private ImapStore createManagedLifecycleImapStore(Provider<ImapStoreManager> imapStoreManagerProvider, IMAPStore store) {
		
		ManagedLifecycleImapStore.Factory factory = new ManagedLifecycleImapStore.Factory(
				imapStoreManagerProvider, new MessageInputStreamProviderImpl());

		replay(imapStoreManagerProvider, imapStoreManagerMock, store);
		
		return factory.create(null, store, "user@domain", "password", "localhost", ServerSetupTest.IMAP.getPort());
	}

	private Provider<ImapStoreManager> mockImapStoreManagerProvider() {
		Provider<ImapStoreManager> imapStoreManagerProvider = createMock(Provider.class);
		imapStoreManagerMock = createStrictMock(ImapStoreManagerImpl.class);
		expectProviderGiveMock(imapStoreManagerProvider);
		return imapStoreManagerProvider;
	}
	
	private IMAPStore mockIMAPStore() throws MessagingException {
		IMAPStore store = EasyMock.createMock(IMAPStore.class);
		expect(store.getDefaultFolder()).andReturn(mockDefaultFolder()).once();
		return store;
	}

	private IMAPFolder mockDefaultFolder() throws MessagingException {
		IMAPFolder defaultFolder = createMock(IMAPFolder.class);
		expectFolderGetSubFolder(defaultFolder, mockFolderThatFurnishIMAPStream());
		replay(defaultFolder);
		return defaultFolder;
	}
	private IMAPFolder mockFolderThatFurnishIMAPStream() throws MessagingException {
		IMAPFolder folder = createMock(IMAPFolder.class);
		expectFolderOpenning(folder);
		expectFolderGetMessage(folder);
		expectFolderFetch(folder);
		replay(folder);
		return folder;
	}

	private void expectCallToCloseWhenDone() {
		imapStoreManagerMock.closeWhenDone();
		expectLastCall().once();
	}

	private void expectCallToParentClose(IMAPStore imapStoreMock) throws MessagingException {
		imapStoreMock.close();
		expectLastCall().once();
	}

	private void expectProviderGiveMock(Provider<ImapStoreManager> provider) {
		expect(provider.get()).andReturn(imapStoreManagerMock).once();
		imapStoreManagerMock.setImapStore(anyObject(ManagedLifecycleImapStore.class));
		expectLastCall().once();
	}

	private void expectCallToBindTo() {
		expect(imapStoreManagerMock.bindTo(anyObject(InputStream.class)))
			.andReturn(createStrictMock(InputStream.class)).once();
	}

	private void expectFolderGetSubFolder(IMAPFolder defaultFolder, Folder subFolder) throws MessagingException {
		expect(defaultFolder.getFolder(anyObject(String.class))).andReturn(subFolder).once();
	}

	private void expectFolderGetMessage(IMAPFolder folder) throws MessagingException {
		expect(folder.getMessageByUID(anyInt())).andReturn(createStrictMock(IMAPMessage.class)).once();
	}

	private void expectFolderOpenning(IMAPFolder folder) throws MessagingException {
		folder.open(anyInt());
		expectLastCall().once();
	}

	private void expectFolderFetch(IMAPFolder folder) throws MessagingException {
		folder.fetch(anyObject(Message[].class), anyObject(FetchProfile.class));
		expectLastCall().once();
	}
}
