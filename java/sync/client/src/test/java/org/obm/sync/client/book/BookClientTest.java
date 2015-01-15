/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2015 Linagora
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

import static org.easymock.EasyMock.expect;

import javax.naming.NoPermissionException;

import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.IntegerParameter;
import org.obm.sync.Parameter;
import org.obm.sync.StringParameter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.BookItemsParser;
import org.obm.sync.book.BookItemsWriter;
import org.obm.sync.book.Contact;
import org.obm.sync.client.AbstractClientTest;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import fr.aliacom.obm.ToolBox;

public class BookClientTest extends AbstractClientTest {

	private BookClient testee;
	private AccessToken token;
	private BookItemsParser parser;
	private BookItemsWriter writer;

	@Before
	public void setUpClient() {
		Logger logger = LoggerFactory.getLogger(getClass());
		token = ToolBox.mockAccessToken(control);
		parser = new BookItemsParser();
		writer = new BookItemsWriter();
		
		HttpClient httpClient = null;
		Locator locator = null;
		
		testee = new BookClient(new SyncClientAssert(), locator, logger, parser, writer, httpClient) {
			
			@Override
			protected Document execute(AccessToken token, String action, Multimap<String, Parameter> parameters) {
				return responder.execute(token, action, parameters);
			}
		};
	}

	@Test
	public void storeContactShouldForwardAllParameters() throws Exception {
		int addressBookId = 5;
		String clientId = "clientId";
		Contact contact = createContact();
		Document document = DOMUtils.createDoc("namespace", "rootElement");
		
		ImmutableMultimap<String, Parameter> expectedParams = ImmutableMultimap.of(
				"sid", new StringParameter("sessionId"),
				"clientId", new StringParameter("clientId"),
				"bookId", new IntegerParameter(addressBookId),
				"contact", new StringParameter(writer.getContactAsString(contact)));
		
		expect(responder.execute(token, "/book/storeContact", expectedParams)).andReturn(document);
		
		control.replay();
		testee.storeContact(token, addressBookId, contact, clientId);
		control.verify();
	}

	@Test(expected=NoPermissionException.class)
	public void storeContactShouldPropagateDocumentNoPermissionException() throws Exception {
		storeContactThenServerReturnErrorDocument(NoPermissionException.class);
	}
	
	@Test(expected=ServerFault.class)
	public void storeContactShouldPropagateDocumentUnknownExceptionAsServerFault() throws Exception {
		storeContactThenServerReturnErrorDocument(AuthFault.class);
	}

	private void storeContactThenServerReturnErrorDocument(Class<? extends Exception> exception) throws Exception {
		int addressBookId = 5;
		String clientId = "clientId";
		Contact contact = createContact();
		Document document = mockErrorDocument(exception, "message");
		
		ImmutableMultimap<String, Parameter> expectedParams = ImmutableMultimap.of(
				"sid", new StringParameter("sessionId"),
				"clientId", new StringParameter("clientId"),
				"bookId", new IntegerParameter(addressBookId),
				"contact", new StringParameter(writer.getContactAsString(contact)));
		
		expect(responder.execute(token, "/book/storeContact", expectedParams)).andReturn(document);
		
		control.replay();
		try {
			testee.storeContact(token, addressBookId, contact, clientId);
		} finally {
			control.verify();
		}
	}
}
