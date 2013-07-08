/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.contact;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;
import javax.naming.NoPermissionException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;


//@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class AddressBookHandlerIntegrationTest extends ObmSyncIntegrationTest {

	@Test
	@RunAsClient
	public void testModifyContact() throws Exception {
		int user1ContactsBook = 4;
		String email = "user1@domain.org";
		Contact contact = buildSampleContact();

		AccessToken token = loginClient.login(email, "user1");
		Contact createdContact = createContactOnServer(user1ContactsBook, token, contact);

		createdContact.setFirstname("John");
		createdContact.setLastname("Doe");
		
		bookClient.modifyContact(token, user1ContactsBook, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, user1ContactsBook, createdContact.getUid());

		assertThat(modifiedContact).isEqualTo(createdContact);
	}

	@Test
	@RunAsClient
	public void testModifyContactWithNoPhones() throws Exception {
		int user1ContactsBook = 4;
		String email = "user1@domain.org";
		Contact contact = buildSampleContact();

		AccessToken token = loginClient.login(email, "user1");
		Contact createdContact = createContactOnServer(user1ContactsBook, token, contact);

		createdContact.getPhones().clear();

		bookClient.modifyContact(token, user1ContactsBook, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, user1ContactsBook, createdContact.getUid());

		assertThat(modifiedContact).isEqualTo(createdContact);
	}
	
	@Test
	@RunAsClient
	public void testModifyContactWithNoAddresses() throws Exception {
		int user1ContactsBook = 4;
		String email = "user1@domain.org";
		Contact contact = buildSampleContact();

		AccessToken token = loginClient.login(email, "user1");
		Contact createdContact = createContactOnServer(user1ContactsBook, token, contact);

		createdContact.getAddresses().clear();

		bookClient.modifyContact(token, user1ContactsBook, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, user1ContactsBook, createdContact.getUid());

		assertThat(modifiedContact).isEqualTo(createdContact);
	}
	
	@Test
	@RunAsClient
	public void testModifyContactWithNoImIdentifers() throws Exception {
		int user1ContactsBook = 4;
		String email = "user1@domain.org";
		Contact contact = buildSampleContact();

		AccessToken token = loginClient.login(email, "user1");
		Contact createdContact = createContactOnServer(user1ContactsBook, token, contact);

		createdContact.getImIdentifiers().clear();

		bookClient.modifyContact(token, user1ContactsBook, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, user1ContactsBook, createdContact.getUid());

		assertThat(modifiedContact).isEqualTo(createdContact);
	}

	@Test
	@RunAsClient
	public void testModifyContactWithNoWebsites() throws Exception {
		int user1ContactsBook = 4;
		String email = "user1@domain.org";
		Contact contact = buildSampleContact();

		AccessToken token = loginClient.login(email, "user1");
		Contact createdContact = createContactOnServer(user1ContactsBook, token, contact);

		createdContact.setCalUri("");
		createdContact.getWebsites().clear();

		bookClient.modifyContact(token, user1ContactsBook, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, user1ContactsBook, createdContact.getUid());

		assertThat(modifiedContact).isEqualTo(createdContact);
	}

	/**
	 * We need this because obm-sync doesn't return the folder id on creation.
	 * This will be fixed as part of another ticket.
	 */
	private Contact createContactOnServer(int bookId, AccessToken token, Contact contact) throws ServerFault, NoPermissionException {
		Contact createdContact = bookClient.createContact(token, bookId, contact, null);

		createdContact.setFolderId(bookId);

		return createdContact;
	}

	private Contact buildSampleContact() {
		Contact contact = new Contact();

		contact.setCommonname("CommonName");
		contact.setFirstname("Firstname");
		contact.setLastname("Lastname");
		contact.setMiddlename("Middlename");
		contact.setSuffix("Suffix");

		contact.setTitle("Title");
		contact.setService("Service");
		contact.setAka("Aka");
		contact.setComment("Comment");
		contact.setCompany("Company");
		contact.setAssistant("Assistant");
		contact.setManager("Manager");
		contact.setSpouse("Spouse");

		contact.setCollected(false);
		contact.setCalUri("calUrui-obm.com");

		Phone phoneHome = new Phone("01");
		Phone phoneWork = new Phone("02");
		Phone phoneMobile = new Phone("03");
		Phone phoneHomeFax = new Phone("04");
		Phone phoneWorkFax = new Phone("05");
		Phone phonePager = new Phone("06");
		Phone phoneOther = new Phone("07"); 

		contact.addPhone("WORK_VOICE", phoneWork);
		contact.addPhone("HOME_VOICE", phoneHome);
		contact.addPhone("CELL_VOICE", phoneMobile);
		contact.addPhone("HOME_FAX", phoneHomeFax);
		contact.addPhone("WORK_FAX", phoneWorkFax);
		contact.addPhone("PAGER", phonePager);
		contact.addPhone("OTHER", phoneOther);

		Website websiteBlog = new Website("BLOG", "blog.com");
		Website websiteCalUri = new Website("CALURI;X-OBM-Ref1", contact.getCalUri());
		Website websiteOther = new Website("OTHER", "other.com");

		contact.addWebsite(websiteBlog);
		contact.addWebsite(websiteCalUri);
		contact.addWebsite(websiteOther);

		contact.addEmail("INTERNET", EmailAddress.loginAtDomain("first.last@obm.com"));
		contact.addEmail("OTHER", EmailAddress.loginAtDomain("first.last@other.com"));

		InstantMessagingId jabber 	= new InstantMessagingId("XMPP", "first.last@jabber.com");
		InstantMessagingId gtalk = new InstantMessagingId("X_GTALK", "first.last@gtalk.com");
		InstantMessagingId aim 	= new InstantMessagingId("AIM", "first.last@aim.com");
		InstantMessagingId yahoo = new InstantMessagingId("YMSGR", "first.last@yahoo.com");
		InstantMessagingId msn	 = new InstantMessagingId("MSN", "first.last@msn.com");
		InstantMessagingId icq  = new InstantMessagingId("ICQ", "first.last@icq.com");
		InstantMessagingId otherIM = new InstantMessagingId("OTHER", "@first_last");

		contact.addIMIdentifier("jabber", jabber);
		contact.addIMIdentifier("gtalk", gtalk);
		contact.addIMIdentifier("aim", aim);
		contact.addIMIdentifier("yahoo", yahoo);
		contact.addIMIdentifier("msn", msn);
		contact.addIMIdentifier("icq", icq);
		contact.addIMIdentifier("other", otherIM);

		Address homeAddress = new Address("01 Champs Élysées", "75000", "", "Paris", "France", "");
		Address workAddress = new Address("80 rue Rocque de Fillol", "92800", "", "Puteaux", "France", "");
		Address otherAddress = new Address("02 Champs Élysées", "75000", "", "Paris", "France", "");

		contact.addAddress("HOME", homeAddress);
		contact.addAddress("WORK", workAddress);
		contact.addAddress("OTHER", otherAddress);

		return contact;
	}

}
