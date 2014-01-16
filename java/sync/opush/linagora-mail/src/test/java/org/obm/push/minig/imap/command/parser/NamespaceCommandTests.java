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
package org.obm.push.minig.imap.command.parser;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.minig.imap.command.NamespaceCommand;
import org.obm.push.minig.imap.impl.IMAPResponse;

public class NamespaceCommandTests {

	private NamespaceCommand command;
	
	@Before
	public void setup() {
		command = new NamespaceCommand();
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot1() {
		IMAPResponse response = createImapResponseFromPayload("* NAMESPACE ((\"\" \"/\")) NIL NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
	}

	@Test
	public void testParsingRFC2342Ex5Dot2() {
		IMAPResponse response = createImapResponseFromPayload("* NAMESPACE NIL NIL ((\"\" \".\"))");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(0, namespaceInfo.getPersonal().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(1, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getMailShares().get(0));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot3() {
		IMAPResponse response = createImapResponseFromPayload("* NAMESPACE ((\"\" \"/\")) NIL ((\"Public Folders/\" \"/\"))");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(1, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("Public Folders/", namespaceInfo.getMailShares().get(0));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot4() {
		IMAPResponse response = createImapResponseFromPayload(
				"* NAMESPACE ((\"\" \"/\")) ((\"~\" \"/\")) " +
				"((\"#shared/\" \"/\") (\"#public/\" \"/\") " +
				"(\"#ftp/\" \"/\")(\"#news.\" \".\"))");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(1, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(4, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("~", namespaceInfo.getOtherUsers().get(0));
		Assert.assertEquals(Arrays.asList("#shared/", "#public/", "#ftp/", "#news."), 
				namespaceInfo.getMailShares());
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot5() {
		IMAPResponse response = createImapResponseFromPayload("* NAMESPACE ((\"INBOX.\" \".\")) NIL  NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("INBOX.", namespaceInfo.getPersonal().get(0));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot6() {
		IMAPResponse response = createImapResponseFromPayload(
				"* NAMESPACE ((\"\" \"/\")(\"#mh/\" \"/\" \"X-PARAM\" (\"FLAG1\" \"FLAG2\"))) NIL NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(2, namespaceInfo.getPersonal().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("#mh/", namespaceInfo.getPersonal().get(1));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot7() {
		IMAPResponse response = createImapResponseFromPayload(
				"* NAMESPACE ((\"\" \"/\")) ((\"Other Users/\" \"/\")) NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(1, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("Other Users/", namespaceInfo.getOtherUsers().get(0));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot8() {
		IMAPResponse response = createImapResponseFromPayload(
				"* NAMESPACE ((\"\" \"/\")) ((\"#Users/\" \"/\")) NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(1, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("#Users/", namespaceInfo.getOtherUsers().get(0));
	}
	
	@Test
	public void testParsingRFC2342Ex5Dot9() {
		IMAPResponse response = createImapResponseFromPayload(
				"* NAMESPACE ((\"\" \"/\")) ((\"~\" \"/\")) NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals(1, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals("", namespaceInfo.getPersonal().get(0));
		Assert.assertEquals("~", namespaceInfo.getOtherUsers().get(0));
	}

	@Test
	public void testParsingRFC2342UTF7() {
		IMAPResponse response = createImapResponseFromPayload("* NAMESPACE ((\"Bo&AO4-tes partag&AOk-es/\" \"/\")) NIL NIL");
		command.handleResponses(Arrays.asList(response));
		NameSpaceInfo namespaceInfo = command.getReceivedData();
		Assert.assertEquals(0, namespaceInfo.getMailShares().size());
		Assert.assertEquals(0, namespaceInfo.getOtherUsers().size());
		Assert.assertEquals(1, namespaceInfo.getPersonal().size());
		Assert.assertEquals("Boîtes partagées/", namespaceInfo.getPersonal().get(0));
	}
	
	
	private IMAPResponse createImapResponseFromPayload(String payload) {
		IMAPResponse imapResponse = new IMAPResponse();
		imapResponse.setStatus("OK");
		imapResponse.setPayload(payload);
		return imapResponse;
	}
	
}
