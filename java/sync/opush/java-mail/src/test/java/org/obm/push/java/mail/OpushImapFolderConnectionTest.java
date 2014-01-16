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
package org.obm.push.java.mail;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.sun.mail.imap.IMAPFolder;

public class OpushImapFolderConnectionTest {
	
	@Test
	public void testCloseAndUnsetImapFolderNullParameter() throws MessagingException {
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection();
		opushImapFolderConnection.closeAndUnsetImapFolder();
	}
	
	@Test
	public void testCloseAndUnsetImapFolder() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.getFullName())
			.andReturn(null).once();
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		imapFolder.close(false);
		expectLastCall().once();
		
		replay(imapFolder);
		
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder); 
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.closeAndUnsetImapFolder();
		
		verify(imapFolder);
	}
	
	@Test(expected=MessagingException.class)
	public void testCloseAndUnsetImapFolderException() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.getFullName())
			.andReturn(null).once();
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		imapFolder.close(false);
		expectLastCall().andThrow(new MessagingException()).once();
		
		replay(imapFolder);
		
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder); 
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.closeAndUnsetImapFolder();
	}
	
	@Test
	public void testCloseImapFolderWhenChangedNullFolderName() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.getFullName())
			.andReturn("folder").once();
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		imapFolder.close(false);
		expectLastCall().once();
		
		replay(imapFolder);
		
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder); 
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.closeImapFolderWhenChanged(null);
		
		verify(imapFolder);
	}
	
	@Test
	public void testCloseImapFolderWhenChanged() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.getFullName())
			.andReturn("folder").times(2);
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		imapFolder.close(false);
		expectLastCall().once();
		
		replay(imapFolder);
		
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder);
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.closeImapFolderWhenChanged("otherFolder");
		
		verify(imapFolder);
	}
	
	@Test
	public void testOpenImapFolderIfNeededNullParameters() throws MessagingException {
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection();
		opushImapFolderConnection.openImapFolderIfNeeded(null, null);
	}
	
	@Test
	public void testOpenImapFolderIfNeededOpenImapFolder() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		
		replay(imapFolder);
		
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder);
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.openImapFolderIfNeeded(null, null);
		
		verify(imapFolder);
	}
	
	@Test
	public void testOpenImapFolderIfNeededClosedImapFolder() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.isOpen())
			.andReturn(false).once();
		ImapStore imapStore = createStrictMock(ImapStore.class);
		String folderName = "folder";
		OpushImapFolderImpl opushImapFolder = new OpushImapFolderImpl(null, null, imapFolder);
		expect(imapStore.openFolder(folderName , Folder.READ_WRITE))
			.andReturn(opushImapFolder).once();
		
		replay(imapFolder, imapStore);
		
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection(opushImapFolder);
		opushImapFolderConnection.openImapFolderIfNeeded(folderName, imapStore);
		
		verify(imapFolder, imapStore);
	}
	
	@Test
	public void testSetImapFolderToFolderNameAndOpenIt() throws MessagingException {
		IMAPFolder imapFolder = createStrictMock(IMAPFolder.class);
		expect(imapFolder.isOpen())
			.andReturn(true).once();
		ImapStore imapStore = createStrictMock(ImapStore.class);
		String folderName = "folder";
		OpushImapFolderImpl expectedOpushImapFolder = new OpushImapFolderImpl(null, null, imapFolder);
		expect(imapStore.openFolder(folderName , Folder.READ_WRITE))
			.andReturn(expectedOpushImapFolder).once();
		
		replay(imapFolder, imapStore);
		
		OpushImapFolderConnection opushImapFolderConnection = new OpushImapFolderConnection();
		opushImapFolderConnection.setImapFolderToFolderNameAndOpenIt(folderName, imapStore);
		OpushImapFolder opushImapFolder = opushImapFolderConnection.getOpushImapFolder();
		
		verify(imapFolder, imapStore);
		Assertions.assertThat(opushImapFolder).isEqualTo(expectedOpushImapFolder);
	}
}
