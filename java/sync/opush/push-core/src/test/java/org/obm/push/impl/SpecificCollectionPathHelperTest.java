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
package org.obm.push.impl;

import org.easymock.EasyMock;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;


public class SpecificCollectionPathHelperTest {

	private static final String SPECIFIC_MAILBOX_DRAFT = "specific\\mydraft";
	private static final String SPECIFIC_MAILBOX_SENT = "specific\\mysent";
	private static final String SPECIFIC_MAILBOX_TRASH = "specific\\bin";
	private UserDataRequest udr;
	private ICollectionPathHelper collectionPathHelper;
	
	@Before
	public void setUp() {
		EmailConfiguration emailConfiguration = mockEmailConfiguration();
		collectionPathHelper = new CollectionPathHelper(emailConfiguration);
		User user = Factory.create().createUser("user@domain", "user@domain", "user@domain");
		udr = new UserDataRequest(new Credentials(user, "test"), null, null);
	}
	
	private EmailConfiguration mockEmailConfiguration() {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		EasyMock.expect(emailConfiguration.imapMailboxDraft()).andReturn(SPECIFIC_MAILBOX_DRAFT);
		EasyMock.expect(emailConfiguration.imapMailboxSent()).andReturn(SPECIFIC_MAILBOX_SENT);
		EasyMock.expect(emailConfiguration.imapMailboxTrash()).andReturn(SPECIFIC_MAILBOX_TRASH);
		EasyMock.replay(emailConfiguration);
		return emailConfiguration;
	}
	
	@Test
	public void testParseSpecificFolderINBOX() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo(EmailConfiguration.IMAP_INBOX_NAME);
	}
	
	@Test
	public void testParseSpecificFolderDraft() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Drafts";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo(SPECIFIC_MAILBOX_DRAFT);
	}
	
	@Test
	public void testParseSpecificFolderSent() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Sent";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo(SPECIFIC_MAILBOX_SENT);
	}
	
	@Test
	public void testParseSpecificFolderTrash() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Trash";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo(SPECIFIC_MAILBOX_TRASH);
	}

	
	@Test
	public void testParseSpecificFolderINBOXNonMatchingCase() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INbOX";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("INbOX");
	}
	
	@Test
	public void testParseSpecificFolderDraftNonMatchingCase() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\DraftS";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("DraftS");
	}
	
	@Test
	public void testParseSpecificFolderSentNonMatchingCase() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\sent";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("sent");
	}
	
	@Test
	public void testParseSpecificFolderTrashNonMatchingCase() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\trash";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("trash");
	}
	
	@Test
	public void testParseSubSpecificFolderINBOXDoesntMatchAsSpecific() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX\\subfolder";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("INBOX\\subfolder");
	}
	
	@Test
	public void testParseSubSpecificFolderDraftDoesntMatchAsSpecific() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Drafts\\subfolder";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("Drafts\\subfolder");
	}
	
	@Test
	public void testParseSubSpecificFolderSentDoesntMatchAsSpecific() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Sent\\subfolder";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("Sent\\subfolder");
	}
	
	@Test
	public void testParseSubSpecificFolderTrashDoesntMatchAsSpecific() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Trash\\subfolder";
		String parsedFolder = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("Trash\\subfolder");
	}
}
