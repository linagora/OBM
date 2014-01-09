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

import org.assertj.core.api.Assertions;
import org.junit.Test;


import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;


import org.obm.push.java.mail.UIDCopyMessage;


public class UIDCopyMessageTest {

	@Test
	public void testParseUidResponse() throws ProtocolException {
		Response uidCopyResponse = new Response("A3 [COPYUID 1320399688 1 25] Completed");
		
		Long newParsedUid = uidCopyCommand().parseNewMessageUID(uidCopyResponse);
		
		Assertions.assertThat(newParsedUid).isEqualTo(25);
	}

	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenCopiedUidDoesntMatch() throws ProtocolException {
		long copiedMessageUid = 1l;
		UIDCopyMessage command = new UIDCopyMessage("FOLDER_TARGET", copiedMessageUid);
		command.findNewMessageUID("1320399688 2 25");
	}
	
	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenNoCopiedUid() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1320399688 1 ");
	}

	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenNoNewUid() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1320399688 25");
	}

	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenNoUidValidity() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1 25");
	}
	
	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenTooManyArgsReceived() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1320399688 1 25 1");
	}

	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenNewUIDIsText() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1320399688 1 num");
	}
	
	@Test(expected=ProtocolException.class)
	public void testFindNewUIDWhenCopiedUIDIsText() throws ProtocolException {
		uidCopyCommand().findNewMessageUID("1320399688 num 25");
	}
	
	@Test
	public void testFindNewUIDWhenManySpace() throws ProtocolException {
		long newMessageUID = uidCopyCommand().findNewMessageUID("   1320399688    1    25  ");

		Assertions.assertThat(newMessageUID).isEqualTo(25);
	}
	
	@Test
	public void testFindNewUIDWhenManySpaceAtTheEnd() throws ProtocolException {
		long newMessageUID = uidCopyCommand().findNewMessageUID("1320399688  1 25     ");
		Assertions.assertThat(newMessageUID).isEqualTo(25);
	}
	
	@Test(expected=ProtocolException.class)
	public void testParseUIDsWhenResponseHooksAreMissing() throws ProtocolException {
		uidCopyCommand().parseUIDsInResponse("A3 COPYUID 1320399688 1 25 Completed");
	}

	@Test
	public void testParseUIDsWhenResponseNameHasEndingSpace() throws ProtocolException {
		String uidsToParse = uidCopyCommand().parseUIDsInResponse("A3 [COPYUID 1320399688 1 25 ] Completed");
		
		Assertions.assertThat(uidsToParse).isEqualTo("1320399688 1 25 ");
	}
	
	@Test(expected=ProtocolException.class)
	public void testParseUIDsWhenResponseNameHasSpace() throws ProtocolException {
		uidCopyCommand().parseUIDsInResponse("A3 [ COPYUID 1320399688 1 25] Completed");
	}

	@Test(expected=ProtocolException.class)
	public void testParseUIDsWhenResponseNameIsMisspelled() throws ProtocolException {
		uidCopyCommand().parseUIDsInResponse("A3 [COPIUID 1320399688 1 25] Completed");
	}

	private UIDCopyMessage uidCopyCommand() {
		String folderDst = "TEST";
		long messageUid = 1l;
		return new UIDCopyMessage(folderDst, messageUid);
	}
}
