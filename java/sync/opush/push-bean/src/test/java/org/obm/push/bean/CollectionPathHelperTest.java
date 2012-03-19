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
package org.obm.push.bean;

import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.exception.CollectionPathException;

public class CollectionPathHelperTest {

	private BackendSession bs;
	private CollectionPathHelper collectionPathHelper;
	
	@Before
	public void setUp() {
		collectionPathHelper = new CollectionPathHelper();
		String mailbox = "user@domain";
		String password = "password";
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	}
	
	@Test
	public void testParseImapFolderEmailINBOX() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("INBOX");
	}
	
	@Test
	public void testParseImapFolderEmailSent()  throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\Sent";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("Sent");
	}
	
	@Test
	public void testParseImapFolderCalendar()  throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\calendar\\user@domain";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.CALENDAR);
		Assertions.assertThat(parsedFolder).isEqualTo("user@domain");
	}

	@Test
	public void testParseImapFolderTasks()  throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\tasks\\test";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.TASKS);
		Assertions.assertThat(parsedFolder).isEqualTo("test");
	}
	
	@Test
	public void testParseImapFolderContacts()  throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\contacts";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.CONTACTS);
		Assertions.assertThat(parsedFolder).isEqualTo("contacts");
	}

	@Test
	public void testParseImapFolderTwoLevel()  throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX\\test";
		String parsedFolder = collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.EMAIL);
		Assertions.assertThat(parsedFolder).isEqualTo("INBOX");
	}

	@Test(expected=CollectionPathException.class)
	public void testParseImapFolderWhenBadUserId() throws CollectionPathException {
		String collectionPath = "obm:\\\\user\\email\\INBOX";
		collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.EMAIL);
	}
	
	@Test(expected=CollectionPathException.class)
	public void testParseImapFolderWhenBadType() throws CollectionPathException {
		String collectionPath = "obm:\\user@domain\\email\\INBOX";
		collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.TASKS);
	}

	@Test(expected=CollectionPathException.class)
	public void testParseImapFolderWhenBadProtocol() throws CollectionPathException {
		String collectionPath = "obm:\\user@domain\\email\\INBOX";
		collectionPathHelper.extractImapFolder(bs, collectionPath, PIMDataType.EMAIL);
	}

	@Test(expected=NullPointerException.class)
	public void testParseImapFolderWhenNullSession() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX";
		collectionPathHelper.extractImapFolder(null, collectionPath, PIMDataType.EMAIL);
	}

	@Test(expected=NullPointerException.class)
	public void testParseImapFolderWhenNullPath() throws CollectionPathException {
		collectionPathHelper.extractImapFolder(bs, null, PIMDataType.EMAIL);
	}

	@Test(expected=NullPointerException.class)
	public void testParseImapFolderWhenEmptyPath() throws CollectionPathException {
		collectionPathHelper.extractImapFolder(bs, "", PIMDataType.EMAIL);
	}
	
	@Test(expected=NullPointerException.class)
	public void testParseImapFolderWhenNullDataType() throws CollectionPathException {
		String collectionPath = "obm:\\\\user@domain\\email\\INBOX";
		collectionPathHelper.extractImapFolder(bs, collectionPath, null);
	}
	
	@Test
	public void testBuildCollectionPathINBOX() {
		String collectionPathExpected = "obm:\\\\user@domain\\email\\INBOX";
		String parsedFolder = collectionPathHelper.buildCollectionPath(
				bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		Assertions.assertThat(parsedFolder).isEqualTo(collectionPathExpected);
	}

	@Test
	public void testBuildCollectionPathSent() {
		String collectionPathExpected = "obm:\\\\user@domain\\email\\Sent";
		String parsedFolder = collectionPathHelper.buildCollectionPath(
				bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
		Assertions.assertThat(parsedFolder).isEqualTo(collectionPathExpected);
	}

	@Test
	public void testBuildCollectionPathWithSubFolder() {
		String collectionPathExpected = "obm:\\\\user@domain\\email\\INBOX\\Shared";
		String parsedFolder = collectionPathHelper.buildCollectionPath(
				bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME + "\\Shared");
		Assertions.assertThat(parsedFolder).isEqualTo(collectionPathExpected);
	}

	@Test
	public void testBuildDefaultCollectionPath() {
		String collectionPathExpected = "obm:\\\\user@domain\\contacts";
		String parsedFolder = collectionPathHelper.buildDefaultCollectionPath(bs, PIMDataType.CONTACTS);
		Assertions.assertThat(parsedFolder).isEqualTo(collectionPathExpected);
	}

	@Test(expected=NullPointerException.class)
	public void testBuildDefaultCollectionPathWhenNullSession() {
		collectionPathHelper.buildDefaultCollectionPath(null, PIMDataType.CONTACTS);
	}
	
	@Test(expected=NullPointerException.class)
	public void testBuildDefaultCollectionPathWhenNullDataType() {
		collectionPathHelper.buildDefaultCollectionPath(bs, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void testBuildCollectionPathWhenNullSession() {
		collectionPathHelper.buildCollectionPath(null, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test(expected=NullPointerException.class)
	public void testBuildCollectionPathWhenNullDataType() {
		collectionPathHelper.buildCollectionPath(bs, null, EmailConfiguration.IMAP_INBOX_NAME);
	}

	@Test
	public void testRecognizePIMDataTypeSubFolder() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\email\\anydata");
		Assertions.assertThat(type).isEqualTo(PIMDataType.EMAIL);
	}

	@Test
	public void testRecognizePIMDataTypeCalendar() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\calendar\\user@domain");
		Assertions.assertThat(type).isEqualTo(PIMDataType.CALENDAR);
	}

	@Test
	public void testRecognizePIMDataTypeEmail() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\email\\INBOX");
		Assertions.assertThat(type).isEqualTo(PIMDataType.EMAIL);
	}

	@Test
	public void testRecognizePIMDataTypeContacts() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\contacts");
		Assertions.assertThat(type).isEqualTo(PIMDataType.CONTACTS);
	}
	
	@Test
	public void testRecognizePIMDataTypeCollectedContacts() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\contacts\\collected_contacts");
		Assertions.assertThat(type).isEqualTo(PIMDataType.CONTACTS);
	}

	@Test
	public void testRecognizePIMDataTypeTasks() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user@domain\\tasks\\user@domain");
		Assertions.assertThat(type).isEqualTo(PIMDataType.TASKS);
	}

	@Test(expected=CollectionPathException.class)
	public void testRecognizePIMDataTypeWhenNoDomain() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\\\user\\mydata");
		Assertions.assertThat(type).isEqualTo(PIMDataType.EMAIL);
	}

	@Test(expected=CollectionPathException.class)
	public void testRecognizePIMDataTypeWhenBadProtocol() throws CollectionPathException {
		PIMDataType type = collectionPathHelper.recognizePIMDataType(bs, "obm:\\user@domain\\email");
		Assertions.assertThat(type).isEqualTo(PIMDataType.EMAIL);
	}
}
