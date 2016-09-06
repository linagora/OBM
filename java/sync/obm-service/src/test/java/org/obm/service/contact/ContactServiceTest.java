/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Date;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.domain.dao.CommitedOperationDao;
import org.obm.domain.dao.ContactDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.service.solr.SolrHelper;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.CommitedOperation;
import org.obm.sync.addition.Kind;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.dao.EntityId;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;

@RunWith(GuiceRunner.class)
@GuiceModule(ContactServiceTest.Env.class)
public class ContactServiceTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(SolrHelper.Factory.class);
			bindWithMock(ContactDao.class);
			bindWithMock(CommitedOperationDao.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject private IMocksControl mocksControl;
	@Inject private ContactDao contactDao;
	@Inject private CommitedOperationDao commitedOperationDao;
	@Inject private ContactService testee;

	private AccessToken token;

	@Before
	public void setUp() {
		token = ToolBox.mockAccessToken(mocksControl);
	}

	@After
	public void tearDown() {
		mocksControl.verify();
	}

	@Test
	public void testCreateContactWithCommitedOperation() throws Exception {
		Integer addressBookId = 1;
		EntityId entityId = EntityId.valueOf(984);
		Contact contact = new Contact(), expectedContact = new Contact();
		String clientId = "6547";

		expectedContact.setEntityId(entityId);

		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(expectedContact).once();
		expect(commitedOperationDao.findAsContact(token, clientId)).andReturn(null).once();
		commitedOperationDao.store(token,
				CommitedElement.builder()
					.clientId(clientId)
					.entityId(entityId)
					.kind(Kind.VCONTACT)
					.build());
		expectLastCall().once();
		mocksControl.replay();
		
		Contact createdContact = testee.createContact(token, addressBookId, contact, clientId);
		
		assertThat(createdContact).isEqualTo(expectedContact);
	}

	@Test
	public void testCreateContactAlreadyCommited() throws Exception {
		Integer addressBookId = 1;
		Contact contact = new Contact();
		String clientId = "6547";

		CommitedOperation<Contact> commited = new CommitedOperation<Contact>(contact, Optional.<Date>absent());
		expect(commitedOperationDao.findAsContact(token, clientId)).andReturn(commited).once();
		mocksControl.replay();
		
		Contact createdContact = testee.createContact(token, addressBookId, contact, clientId);
		
		assertThat(createdContact).isEqualTo(contact);
	}

	@Test
	public void testCreateContactWhenNullClientId() throws Exception {
		Integer addressBookId = 1, entityId = 984;
		Contact contact = new Contact(), expectedContact = new Contact();

		expectedContact.setEntityId(EntityId.valueOf(entityId));
		
		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(expectedContact).once();
		expect(commitedOperationDao.findAsContact(token, null)).andReturn(null).once();
		mocksControl.replay();
		
		Contact createdContact = testee.createContact(token, addressBookId, contact, null);
		
		assertThat(createdContact).isEqualTo(expectedContact);
	}

	@Test
	public void testCreateContact() throws Exception {
		Contact contact = new Contact();
		int addressBookId = 1;

		expect(contactDao.createContactInAddressBook(token, contact, addressBookId)).andReturn(contact).once();
		expect(commitedOperationDao.findAsContact(token, null)).andReturn(null).once();
		mocksControl.replay();

		Contact createdContact = testee.createContact(token, addressBookId, contact, null);
		
		assertThat(createdContact).isNotNull();
	}
}
