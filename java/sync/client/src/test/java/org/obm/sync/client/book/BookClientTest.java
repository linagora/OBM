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
package org.obm.sync.client.book;

import static fr.aliacom.obm.ToolBox.mockAccessToken;
import static fr.aliacom.obm.ToolBox.mockErrorDocument;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import javax.naming.NoPermissionException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.exception.InvalidContactException;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;

@RunWith(SlowFilterRunner.class)
public class BookClientTest {

	private IMocksControl control;
	private BookClient bookClient;
	private Responder responder;

	private static int ADDRESSBOOKID = 1;
	private AccessToken token;

	@Before
	public void setUp() {
		control = createControl();
		responder = control.createMock(Responder.class);
		token = mockAccessToken(control);
		bookClient = new BookClient(new SyncClientException(), null, null) {
			@Override
			protected Document execute(AccessToken token, String action,
					Multimap<String, String> parameters) {
				return responder.execute(token, action, parameters);
			}
		};
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test(expected=InvalidContactException.class)
	public void testCreateContactInvalidItem() throws Exception {
		testCreateContact(InvalidContactException.class);
	}

	@Test(expected=NoPermissionException.class)
	public void testCreateContactNoPermission() throws Exception {
		testCreateContact(NoPermissionException.class);
	}

	private void testCreateContact(Class<? extends Exception> exceptionClass) throws Exception {
		Contact contact = new Contact();
		Document document = mockErrorDocument(exceptionClass, null, control);

		expect(responder.execute(eq(token), eq("/book/createContact"),
				isA(Multimap.class))).andReturn(document).once();

		control.replay();

		bookClient.createContact(token, ADDRESSBOOKID, contact, "6545");
	}

	@Test(expected=InvalidContactException.class)
	public void testModifyContactInvalidItem() throws Exception {
		testModifyContact(InvalidContactException.class);
	}

	@Test(expected=ContactNotFoundException.class)
	public void testModifyContactContactNotFound() throws Exception {
		testModifyContact(ContactNotFoundException.class);
	}

	@Test(expected=NoPermissionException.class)
	public void testModifyContactNoPermission() throws Exception {
		testModifyContact(NoPermissionException.class);
	}

	private void testModifyContact(Class<? extends Exception> exceptionClass) throws Exception {
		Contact contact = new Contact();
		Document document = mockErrorDocument(exceptionClass, null, control);

		expect(responder.execute(eq(token), eq("/book/modifyContact"),
				isA(Multimap.class))).andReturn(document).once();

		control.replay();

		bookClient.modifyContact(token, ADDRESSBOOKID, contact);
	}

	@Test(expected=ContactNotFoundException.class)
	public void testGetContactFromIdContactNotFound() throws Exception {
		testGetContactFromId(ContactNotFoundException.class);
	}

	private void testGetContactFromId(Class<? extends Exception> exceptionClass) throws Exception {
		int contactId = 1;
		Document document = mockErrorDocument(exceptionClass, null, control);

		expect(responder.execute(eq(token), eq("/book/getContactFromId"),
				isA(Multimap.class))).andReturn(document).once();

		control.replay();

		bookClient.getContactFromId(token, ADDRESSBOOKID, contactId);
	}

	@Test(expected=ContactNotFoundException.class)
	public void testRemoveContactNotFound() throws Exception {
		testRemoveContact(ContactNotFoundException.class);
	}

	@Test(expected=NoPermissionException.class)
	public void testRemoveContactNoPermission() throws Exception {
		testRemoveContact(NoPermissionException.class);
	}

	private void testRemoveContact(Class<? extends Exception> exceptionClass) throws Exception {
		int contactId = 1;
		Document document = mockErrorDocument(exceptionClass, null, control);

		expect(responder.execute(eq(token), eq("/book/removeContact"),
				isA(Multimap.class))).andReturn(document).once();

		control.replay();

		bookClient.removeContact(token, ADDRESSBOOKID, contactId);
	}

	private static interface Responder {
		Document execute(AccessToken token, String action, Multimap<String, String> parameters);
	}
}