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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.impl.ClientSupport;

@RunWith(SlowFilterRunner.class)
public class StoreClientImplTest {

	private StoreClientImpl storeClientImpl;
	private IMocksControl mocks;
	private ClientSupport clientSupport;

	@Before
	public void setUp() {
		int port = 12651;
		mocks = createControl();
		clientSupport = mocks.createMock(ClientSupport.class);
		storeClientImpl = new StoreClientImpl(null, port, null, null, clientSupport);
	}

	@Test
	public void testFirstSelect() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}
	
	@Test
	public void testSelectWhenServiceReturnsFalseWhenNullActive() {
		expect(clientSupport.select("INBOX")).andReturn(false);
		storeClientImpl.activeMailbox = null;
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isNull();
		assertThat(selected).isFalse();
	}
	
	@Test
	public void testSelectWhenServiceReturnsFalseWhenOtherActive() {
		expect(clientSupport.select("INBOX")).andReturn(false);
		storeClientImpl.activeMailbox = "Trash";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("Trash");
		assertThat(selected).isFalse();
	}
	
	@Test
	public void testSelectWhenServiceReturnsTrue() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsNull() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = null;
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsEmpty() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsDifferent() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "Trash";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsShorterThanAsked() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBO";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsLongerThanAsked() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOXX";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsASub() {
		expect(clientSupport.select("INBOX")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOX/sub";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsAParent() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOX/sub", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		expect(clientSupport.listAll()).andReturn(listResult);
		expect(clientSupport.select("INBOX/sub")).andReturn(true);
		storeClientImpl.activeMailbox = "INBOX";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX/sub");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX/sub");
		assertThat(selected).isTrue();
	}

	@Test
	public void testSelectWhenActiveMailboxIsSame() {
		storeClientImpl.activeMailbox = "INBOX";
		
		mocks.replay();
		boolean selected = storeClientImpl.select("INBOX");
		mocks.verify();

		assertThat(storeClientImpl.activeMailbox).isEqualTo("INBOX");
		assertThat(selected).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForNullWhenNullActive() {
		storeClientImpl.activeMailbox = null;
		assertThat(storeClientImpl.hasToSelectMailbox(null)).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForEmptyWhenNullActive() {
		storeClientImpl.activeMailbox = null;
		assertThat(storeClientImpl.hasToSelectMailbox("")).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForInboxWhenNullActive() {
		storeClientImpl.activeMailbox = null;
		assertThat(storeClientImpl.hasToSelectMailbox("INBOX")).isTrue();
	}

	@Test
	public void testHasToSelectMailboxForNullWhenEmptyActive() {
		storeClientImpl.activeMailbox = "";
		assertThat(storeClientImpl.hasToSelectMailbox(null)).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForEmptyWhenEmptyActive() {
		storeClientImpl.activeMailbox = "";
		assertThat(storeClientImpl.hasToSelectMailbox("")).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForInboxWhenEmptyActive() {
		storeClientImpl.activeMailbox = "";
		assertThat(storeClientImpl.hasToSelectMailbox("INBOX")).isTrue();
	}

	@Test
	public void testHasToSelectMailboxForNullWhenInboxActive() {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.hasToSelectMailbox(null)).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForEmptyWhenInboxActive() {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.hasToSelectMailbox("")).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForInboxWhenInboxActive() {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.hasToSelectMailbox("INBOX")).isFalse();
	}

	@Test
	public void testHasToSelectMailboxForSubInboxWhenInboxActive() {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.hasToSelectMailbox("INBOX/subfolder")).isTrue();
	}

	@Test
	public void testHasToSelectMailboxForTrashWhenInboxActive() {
		storeClientImpl.activeMailbox = "INBOX";
		assertThat(storeClientImpl.hasToSelectMailbox("Trash")).isTrue();
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForInbox() {
		String found = storeClientImpl.findMailboxNameWithServerCase("INBOX");
		assertThat(found).isEqualTo("INBOX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForInboxCaseSensitive() {
		String found = storeClientImpl.findMailboxNameWithServerCase("inBox");
		assertThat(found).isEqualTo("INBOX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForLongerInbox() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOXX", true, false));
		EasyMock.expect(clientSupport.listAll()).andReturn(listResult);
		
		mocks.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("INBOXX");
		mocks.verify();
		
		assertThat(found).isEqualTo("INBOXX");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForShorterInbox() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("INBOXX", true, false));
		EasyMock.expect(clientSupport.listAll()).andReturn(listResult);
		
		mocks.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("INBO");
		mocks.verify();
		
		assertThat(found).isEqualTo("INBO");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForTrash() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("Trash", true, false));
		EasyMock.expect(clientSupport.listAll()).andReturn(listResult);
		
		mocks.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("Trash");
		mocks.verify();
		
		assertThat(found).isEqualTo("Trash");
	}
	
	@Test
	public void testFindMailboxNameWithServerCaseForTrashOtherCase() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		EasyMock.expect(clientSupport.listAll()).andReturn(listResult);
		
		mocks.replay();
		String found = storeClientImpl.findMailboxNameWithServerCase("Trash");
		mocks.verify();
		
		assertThat(found).isEqualTo("TRASH");
	}
	
	@Test(expected=CollectionNotFoundException.class)
	public void testFindMailboxNameWithServerCaseForNotInResult() {
		ListResult listResult = new ListResult(3);
		listResult.add(new ListInfo("INBO", true, false));
		listResult.add(new ListInfo("INBOX", true, false));
		listResult.add(new ListInfo("TRASH", true, false));
		EasyMock.expect(clientSupport.listAll()).andReturn(listResult);
		
		mocks.replay();
		storeClientImpl.findMailboxNameWithServerCase("Youpi");
	}
}
