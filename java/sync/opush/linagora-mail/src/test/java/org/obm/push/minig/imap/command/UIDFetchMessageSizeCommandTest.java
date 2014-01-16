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
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.MailException;
import org.obm.push.minig.imap.command.UIDFetchMessageSizeCommand;
import org.obm.push.minig.imap.impl.IMAPResponse;

@RunWith(SlowFilterRunner.class)
public class UIDFetchMessageSizeCommandTest {

	@Test
	public void testMatchResponseWhenIsInternalDate() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 INTERNALDATE \"14-Dec-2012 10:56:18 +0100\")");
		assertThat(new UIDFetchMessageSizeCommand(12l).isMatching(response)).isFalse();
	}

	@Test
	public void testMatchResponseWithInternalDateAfter() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 123 INTERNALDATE \"14-Dec-2012 10:56:18 +0100\")");
		assertThat(new UIDFetchMessageSizeCommand(12l).isMatching(response)).isTrue();
	}

	@Test
	public void testMatchResponseWithInternalDateBefore() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 INTERNALDATE \"14-Dec-2012 10:56:18 +0100\" RFC822.SIZE 123)");
		assertThat(new UIDFetchMessageSizeCommand(12l).isMatching(response)).isTrue();
	}
	
	@Test
	public void testMatchResponseAlone() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 123)");
		assertThat(new UIDFetchMessageSizeCommand(12l).isMatching(response)).isTrue();
	}
	
	@Test(expected=MailException.class)
	public void testHandleReponseWhenNoSizeInResponse() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE )");
		
		new UIDFetchMessageSizeCommand(12l).handleResponse(response);
	}
	
	@Test
	public void testHandleReponseWithInternalDateBefore() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 INTERNALDATE \"14-Dec-2012 10:56:18 +0100\" RFC822.SIZE 123)");
		
		UIDFetchMessageSizeCommand command = new UIDFetchMessageSizeCommand(12l);
		command.setDataInitialValue();
		command.handleResponse(response);
		
		assertThat(command.getReceivedData()).containsOnly(123);
	}
	
	@Test
	public void testHandleReponseWithInternalDateAfter() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 123 INTERNALDATE \"14-Dec-2012 10:56:18 +0100\")");

		UIDFetchMessageSizeCommand command = new UIDFetchMessageSizeCommand(12l);
		command.setDataInitialValue();
		command.handleResponse(response);
		
		assertThat(command.getReceivedData()).containsOnly(123);
	}
	
	@Test
	public void testHandleRightReponse() {
		IMAPResponse response = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 123)");

		UIDFetchMessageSizeCommand command = new UIDFetchMessageSizeCommand(12l);
		command.setDataInitialValue();
		command.handleResponse(response);
		
		assertThat(command.getReceivedData()).containsOnly(123);
	}

	@Test
	public void testHandleReponseWhenActualBuildingResponseHasData() {
		IMAPResponse response1 = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 123)");
		IMAPResponse response2 = new IMAPResponse("OK", "* 11 FETCH (UID 12 RFC822.SIZE 546)");

		UIDFetchMessageSizeCommand command = new UIDFetchMessageSizeCommand(12l);
		command.setDataInitialValue();
		command.handleResponse(response1);
		command.handleResponse(response2);
		
		assertThat(command.getReceivedData()).containsOnly(546, 123);
	}
}
