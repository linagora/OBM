/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.mailbox;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;


public class DeletableMailboxTest {

	private IMocksControl control;
	
	@Before
	public void setup() {
		control = createControl();
	}
	
	@Test(expected=NullPointerException.class)
	public void fromShouldThrowWhenNameIsNull() {
		DeletableMailbox.from(null, null, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fromShouldThrowWhenNameIsEmpty() {
		DeletableMailbox.from("", null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void fromShouldThrowWhenLoggerIsNull() {
		DeletableMailbox.from("user/usera@mydomain.org", null, null);
	}
	
	@Test(expected=NullPointerException.class)
	public void fromShouldThrowWhenStoreClientIsNull() {
		Logger logger = control.createMock(Logger.class);
		
		try {
			control.replay();
			DeletableMailbox.from("user/usera@mydomain.org", logger, null);
		} finally {
			control.verify();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldThrowWhenNameIsNotProvided() {
		new DeletableMailbox.Builder().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldThrowWhenLoggerIsNotProvided() {
		new DeletableMailbox.Builder().name("user/usera@mydomain.org").build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void buildShouldThrowWhenStoreClientIsNotProvided() {
		Logger logger = control.createMock(Logger.class);
		
		try {
			control.replay();
			new DeletableMailbox.Builder().name("user/usera@mydomain.org").logger(logger).build();
		} finally {
			control.verify();
		}
	}
	
	@Test
	public void deleteShouldNotThrowWhenSuccess() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		DeletableMailbox deletableMailbox = DeletableMailbox.from("user/usera@mydomain.org", logger, storeClient);
		expect(storeClient.delete(deletableMailbox.getName()))
			.andReturn(true);
		logger.debug(anyObject(String.class), anyObject(String.class));
		expectLastCall().anyTimes();

		control.replay();
		deletableMailbox.delete();
		control.verify();
	}
	
	@Test(expected=ImapDeleteException.class)
	public void deleteShouldThrowWhenError() throws Exception {
		Logger logger = control.createMock(Logger.class);
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		DeletableMailbox deletableMailbox = DeletableMailbox.from("user/usera@mydomain.org", logger, storeClient);
		expect(storeClient.delete(deletableMailbox.getName()))
			.andReturn(false);

		try {
			control.replay();
			deletableMailbox.delete();
		} finally {
			control.verify();
		}
	}
}
