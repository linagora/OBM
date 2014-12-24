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
package org.obm.push.minig.imap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration.MailboxNameCheckPolicy;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.ClientSupport;


public class StoreClientImplTest {

	private static final int port = 12651;

	private StoreClientImpl storeClientImpl;
	private IMocksControl control;
	private ClientSupport clientSupport;

	@Before
	public void setUp() {
		control = createControl();
		clientSupport = control.createMock(ClientSupport.class);
		storeClientImpl = new StoreClientImpl(null, port, null, null, MailboxNameCheckPolicy.ALWAYS, clientSupport);
	}

	@Test
	public void testFirstSelect() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}
	
	@Test
	public void testSelectWhenServiceReturnsFalseWhenNullActive() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(false);
		storeClientImpl.activeMailbox = null;
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isNull();
		assertThat(selected).isFalse();
	}
	
	@Test
	public void testSelectWhenServiceReturnsFalseWhenOtherActive() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(false);
		storeClientImpl.activeMailbox = "Trash";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("Trash");
		assertThat(selected).isFalse();
	}
	
	@Test
	public void testSelectWhenServiceReturnsTrue() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsNull() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = null;
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsEmpty() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsDifferent() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "Trash";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsShorterThanAsked() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBO";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsLongerThanAsked() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOXX";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsASub() throws Exception {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOX/sub";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsAParent() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOX/sub", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		expect(clientSupport.select("INBOX/sub")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOX";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX/sub");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX/sub");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsSame() throws Exception {
		storeClientImpl.activeMailbox = "INBOX";
		
		control.replay();
		boolean selected = storeClientImpl.select("INBOX");
		control.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void selectForNullWhenNullActive() throws Exception {
		storeClientImpl.activeMailbox = null;
		assertThat(storeClientImpl.select(null)).isFalse();
	}

	@Test
	public void selectForEmptyWhenNullActive() throws Exception {
		storeClientImpl.activeMailbox = null;
		assertThat(storeClientImpl.select("")).isFalse();
	}

	@Test
	public void selectForInboxWhenNullActive() throws Exception {
		String mailbox = "INBOX";
		storeClientImpl.activeMailbox = null;
		expect(clientSupport.select(mailbox))
			.andReturn(true);
		
		control.replay();
		assertThat(storeClientImpl.select(mailbox)).isTrue();
		control.verify();
	}

	@Test
	public void selectForNullWhenEmptyActive() throws Exception {
		storeClientImpl.activeMailbox = "";
		assertThat(storeClientImpl.select(null)).isFalse();
	}

	@Test
	public void selectForEmptyWhenEmptyActive() throws Exception {
		storeClientImpl.activeMailbox = "";
		assertThat(storeClientImpl.select("")).isFalse();
	}

	@Test
	public void selectForInboxWhenEmptyActive() throws Exception {
		storeClientImpl.activeMailbox = "";
		String mailbox = "INBOX";
		expect(clientSupport.select(mailbox))
			.andReturn(true);
	
		control.replay();
		assertThat(storeClientImpl.select("INBOX")).isTrue();
		control.verify();
	}

	@Test
	public void selectForNullWhenInboxActive() throws Exception {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.select(null)).isFalse();
	}

	@Test
	public void selectForEmptyWhenInboxActive() throws Exception {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.select("")).isFalse();
	}

	@Test
	public void selectForInboxWhenInboxActive() throws Exception {
		String mailbox = "INBOX";
		storeClientImpl.activeMailbox = mailbox;
		assertThat(storeClientImpl.select(mailbox)).isTrue();
	}

	@Test
	public void selectForSubInboxWhenInboxActive() throws Exception {
		storeClientImpl.activeMailbox = "INBOX";
		String mailbox = "INBOX/subfolder";
		expect(clientSupport.select(mailbox))
			.andReturn(true);
		ListResult listResult = new ListResult(2);
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo(mailbox, true, false));
		expect(clientSupport.listAll(null))
			.andReturn(listResult);
	
		control.replay();
		assertThat(storeClientImpl.select(mailbox)).isTrue();
		control.verify();
	}

	@Test
	public void selectForTrashWhenInboxActive() throws Exception {
		storeClientImpl.activeMailbox = "INBOX";
		String mailbox = "Trash";
		expect(clientSupport.select(mailbox))
			.andReturn(true);
		ListResult listResult = new ListResult(2);
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo(mailbox, true, false));
		expect(clientSupport.listAll(null))
			.andReturn(listResult);

		control.replay();
		assertThat(storeClientImpl.select(mailbox)).isTrue();
		control.verify();
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForInbox() throws Exception {
		String found = storeClientImpl.findMailboxNameWithServerCase("INBOX");
		assertThat(found).isEqualTo("INBOX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForInboxCaseSensitive() throws Exception {
		String found = storeClientImpl.findMailboxNameWithServerCase("inBox");
		assertThat(found).isEqualTo("INBOX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForLongerInbox() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOXX", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		control.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("INBOXX");
		control.verify();
		
		assertThat(found).isEqualTo("INBOXX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForShorterInbox() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOXX", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		control.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("INBO");
		control.verify();
		
		assertThat(found).isEqualTo("INBO");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForTrash() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("Trash", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		control.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("Trash");
		control.verify();
		
		assertThat(found).isEqualTo("Trash");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForTrashOtherCase() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		control.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("Trash");
		control.verify();
		
		assertThat(found).isEqualTo("TRASH");
	}
	
	@Test(expected=MailboxNotFoundException.class)
	public void testFindMailboxNameWithServerCaseForNotInResult() throws Exception {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		control.replay();
		storeClientImpl.findMailboxNameWithServerCase("Youpi");
	}

	@Test
	public void testFindMailboxNameWithServerCaseWithNeverPolicy() throws Exception {
		storeClientImpl = new StoreClientImpl(null, port, null, null, MailboxNameCheckPolicy.NEVER, clientSupport);
		control.replay();

		assertThat(storeClientImpl.findMailboxNameWithServerCase("mailBOXname")).isEqualTo("mailBOXname");

		control.verify();
	}

	@Test
	public void testFindMailboxNameWithServerCaseForInboxWithNeverPolicy() throws Exception {
		storeClientImpl = new StoreClientImpl(null, port, null, null, MailboxNameCheckPolicy.NEVER, clientSupport);
		control.replay();

		assertThat(storeClientImpl.findMailboxNameWithServerCase("INbOx")).isEqualTo("INBOX");

		control.verify();
	}

	@Test
	public void storeClientShouldBeAutoCloseable() {
		clientSupport.logout();
		expectLastCall();
		
		storeClientImpl = new StoreClientImpl(null, port, null, null, MailboxNameCheckPolicy.NEVER, clientSupport);
		try (StoreClient checkedStoreClient = storeClientImpl) {
			storeClientImpl.login(false);
			assertThat(storeClientImpl.isConnected()).isTrue();
		} catch (Exception e) {
		}
		assertThat(storeClientImpl.isConnected()).isFalse();
	}
	
	@Test(expected=NullPointerException.class)
	public void uidCopyShouldThrowWhenMessageSetIsNull() throws Exception {
		try {
			control.replay();
			storeClientImpl.uidCopy(null, null);
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void uidCopyShouldThrowWhenDestMailboxIsNull() throws Exception {
		try {
			control.replay();
			storeClientImpl.uidCopy(MessageSet.empty(), null);
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void uidCopyShouldWorkWhenMessageSetIsEmpty() throws Exception {
		MessageSet expectedMessageSet = MessageSet.empty();
		String destMailbox = "dest";
		
		ListResult listResult = new ListResult(1);
		listResult.add(new ListInfo(destMailbox, true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		MessageSet messageSet = MessageSet.empty();
		expect(clientSupport.uidCopy(messageSet, destMailbox))
			.andReturn(expectedMessageSet);
		
		control.replay();
		MessageSet returnedMessageSet = storeClientImpl.uidCopy(messageSet, destMailbox);
		control.verify();
		assertThat(returnedMessageSet).isEqualTo(expectedMessageSet);
	}
	
	@Test
	public void uidCopyShouldWork() throws Exception {
		MessageSet expectedMessageSet = MessageSet.builder().add(1l).add(2l).build();
		String destMailbox = "dest";
		
		ListResult listResult = new ListResult(1);
		listResult.add(new ListInfo(destMailbox, true, false));
		expect(clientSupport.listAll(null)).andReturn(listResult);
		
		MessageSet messageSet = MessageSet.builder().add(1l).add(2l).build();
		expect(clientSupport.uidCopy(messageSet, destMailbox))
			.andReturn(expectedMessageSet);
		
		control.replay();
		MessageSet returnedMessageSet = storeClientImpl.uidCopy(messageSet, destMailbox);
		control.verify();
		assertThat(returnedMessageSet).isEqualTo(expectedMessageSet);
	}
}
