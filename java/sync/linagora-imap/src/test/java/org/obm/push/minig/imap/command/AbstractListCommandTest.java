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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.ImmutableList;


public class AbstractListCommandTest {

	@Test
	public void testParseMailboxName() {
		String responseLine = "* LIST () \".\" INBOX";
		
		String name = new AbstractListCommand(false, null).parseMailboxName(responseLine, responseLine.indexOf(')'));
		
		assertThat(name).isEqualTo("INBOX");
	}
	
	@Test
	public void testParseMailboxNameWhenFlags() {
		String responseLine = "* LIST (\\Noinferiors) \".\" INBOX";
		
		String name = new AbstractListCommand(false, null).parseMailboxName(responseLine, responseLine.indexOf(')'));
		
		assertThat(name).isEqualTo("INBOX");
	}
	
	@Test
	public void testParseMailboxNameWithLeadingSpace() {
		String responseLine = "* LIST (\\Noinferiors) \".\" INBOX  ";
		
		String name = new AbstractListCommand(false, null).parseMailboxName(responseLine, responseLine.indexOf(')'));
		
		assertThat(name).isEqualTo("INBOX");
	}

	@Test
	public void testParseMailboxNameWithSpaceInName() {
		String responseLine = "* LIST (\\Noinferiors) \".\" INB OX  ";
		
		String name = new AbstractListCommand(false, null).parseMailboxName(responseLine, responseLine.indexOf(')'));
		
		assertThat(name).isEqualTo("INB OX");
	}

	@Test
	public void testParseMailboxNameWhenSpecialUTF7Char() {
		String responseLine = "* LIST (\\Noinferiors) \".\" INB&-OX  ";
		
		String name = new AbstractListCommand(false, null).parseMailboxName(responseLine, responseLine.indexOf(')'));
		
		assertThat(name).isEqualTo("INB&OX");
	}
	
	@Test
	public void testHandleResponses() {
		IMAPResponse response = new IMAPResponse("OK", "* LSUB () \".\" #news.comp.mail.mime");
		IMAPResponse response2 = new IMAPResponse("OK", "* LSUB () \".\" #news.comp.mail.misc");
		IMAPResponse response3 = new IMAPResponse("OK", "* LSUB (\\Noselect) \".\" #news.comp.mail.others");
		IMAPResponse response4 = new IMAPResponse("OK", "A002 OK LSUB completed");
		
		ListInfo expectedListInfo = new ListInfo("#news.comp.mail.mime", true, true);
		ListInfo expectedListInfo2 = new ListInfo("#news.comp.mail.misc", true, true);
		ListInfo expectedListInfo3 = new ListInfo("#news.comp.mail.others", false, true);
		
		AbstractListCommand abstractListCommand = new AbstractListCommand(true, null);
		abstractListCommand.handleResponses(ImmutableList.of(response, response2, response3, response4));
		ListResult listResult = abstractListCommand.getReceivedData();
		assertThat(listResult).hasSize(3);
		assertThat(listResult).containsOnly(expectedListInfo, expectedListInfo2, expectedListInfo3);
	}

	@Test
	public void referenceNameShouldBeEmptyWhenNull() {
		AbstractListCommand abstractListCommand = new AbstractListCommand(false, null);
		assertThat(abstractListCommand.getImapCommand()).isEqualTo("LIST \"\" \"*\"");
	}

	@Test
	public void referenceNameShouldBeEmptyWhenEmpty() {
		AbstractListCommand abstractListCommand = new AbstractListCommand(false, "");
		assertThat(abstractListCommand.getImapCommand()).isEqualTo("LIST \"\" \"*\"");
	}

	@Test
	public void referenceNameShouldSetWhenGiven() {
		AbstractListCommand abstractListCommand = new AbstractListCommand(false, "*user");
		assertThat(abstractListCommand.getImapCommand()).isEqualTo("LIST \"*user\" \"*\"");
	}
}
