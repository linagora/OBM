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
package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.command.ListCommand;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.Lists;


public class ListCommandTest {

	private ListCommand listCommand;

	@Before
	public void setup() {
		listCommand = new ListCommand();
	}
	
	@Test
	public void folderNameList() {
		listCommand.handleResponses(responses(
				"* LIST (\\Noinferiors) \"/\" INBOX",
				"* LIST (\\HasNoChildren) \"/\" Drafts",
				"* LIST (\\HasNoChildren) \"/\" SPAM",
				"* LIST (\\HasNoChildren) \"/\" Sent",
				"* LIST (\\HasNoChildren) \"/\" Templates",
				"* LIST (\\HasNoChildren) \"/\" Trash",
				"A OK Completed (0.000 secs 7 calls)"));
		ListResult result = listCommand.data;
		assertThat(result).containsOnly(
				inbox(),
				folder("Drafts"),
				folder("SPAM"),
				folder("Sent"),
				folder("Templates"),
				folder("Trash"));
	}
	
	@Test
	public void folderNameTrimQuotes() {
		listCommand.handleResponses(responses(
				"* LIST (\\Noinferiors) \"/\" \"INBOX\"",
				"* LIST (\\HasNoChildren) \"/\" \"Drafts\"",
				"* LIST (\\HasNoChildren) \"/\" \"SPAM\"",
				"* LIST (\\HasNoChildren) \"/\" \"Sent\"",
				"* LIST (\\HasNoChildren) \"/\" \"Templates\"",
				"* LIST (\\HasNoChildren) \"/\" \"Trash\"",
				"A OK Completed (0.000 secs 7 calls)"));
		ListResult result = listCommand.data;
		assertThat(result).containsOnly(
				inbox(),
				folder("Drafts"),
				folder("SPAM"),
				folder("Sent"),
				folder("Templates"),
				folder("Trash"));
	}

	private List<IMAPResponse> responses(String... lines) {
		List<IMAPResponse> responses = Lists.newArrayList();
		for (String line: lines) {
			responses.add(new IMAPResponse("OK", line));
		}
		return responses;
	}
	

	
	private ListInfo folder(String name) {
		return new ListInfo(name, true, true);
	}
	
	private ListInfo inbox() {
		return new ListInfo("INBOX", true, false);
	}
	
}
