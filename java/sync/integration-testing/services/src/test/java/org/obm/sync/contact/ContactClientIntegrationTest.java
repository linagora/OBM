/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.sync.contact;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.IntegrationTestUtils;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Website;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.exception.ContactNotFoundException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.UserPassword;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientModule.class)
public class ContactClientIntegrationTest extends ObmSyncIntegrationTest {

	@Inject ArquillianLocatorService locatorService;
	@Inject BookClient bookClient;
	@Inject LoginClient loginClient;

	private String calendar;
	private int addressBookId;
	private Contact contact;

	@Before
	public void setUp() {
		calendar = "user1@domain.org";
		addressBookId = 1;
		contact = IntegrationTestUtils.newContact();
	}

	@Test
	@RunAsClient
	public void testModifyContact(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Contact createdContact = bookClient.createContact(token, addressBookId, contact, null);

		createdContact.setFirstname("John");
		createdContact.setLastname("Doe");

		bookClient.modifyContact(token, addressBookId, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, addressBookId, createdContact.getUid());

		assertThat(modifiedContact).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "firstname", "lastname");
		assertThat(modifiedContact.getFirstname()).isEqualTo("John");
		assertThat(modifiedContact.getLastname()).isEqualTo("Doe");
	}

	@Test
	@RunAsClient
	public void testModifyContactWithNoPhones(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Contact createdContact = bookClient.createContact(token, addressBookId, contact, null);

		createdContact.getPhones().clear();

		bookClient.modifyContact(token, addressBookId, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, addressBookId, createdContact.getUid());

		assertThat(modifiedContact).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "phones");
		assertThat(modifiedContact.getPhones()).isNotEmpty();
	}
	
	@Test
	@RunAsClient
	public void testModifyContactWithNoAddresses(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Contact createdContact = bookClient.createContact(token, addressBookId, contact, null);

		createdContact.getAddresses().clear();

		bookClient.modifyContact(token, addressBookId, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, addressBookId, createdContact.getUid());

		assertThat(modifiedContact).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "addresses");
		assertThat(modifiedContact.getAddresses()).isNotEmpty();
	}
	
	@Test
	@RunAsClient
	public void testModifyContactWithNoImIdentifers(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Contact createdContact = bookClient.createContact(token, addressBookId, contact, null);

		createdContact.getImIdentifiers().clear();

		bookClient.modifyContact(token, addressBookId, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, addressBookId, createdContact.getUid());

		assertThat(modifiedContact).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "imIdentifiers");
		assertThat(modifiedContact.getImIdentifiers()).isNotEmpty();
	}

	@Test
	@RunAsClient
	public void testModifyContactWithNoWebsites(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));
		Contact createdContact = bookClient.createContact(token, addressBookId, contact, null);

		createdContact.setCalUri("");
		createdContact.getWebsites().clear();

		bookClient.modifyContact(token, addressBookId, createdContact);

		Contact modifiedContact = bookClient.getContactFromId(token, addressBookId, createdContact.getUid());

		assertThat(modifiedContact).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "websites", "calUri");
		assertThat(modifiedContact.getWebsites()).isNotEmpty();
		assertThat(modifiedContact.getCalUri()).isEmpty();
	}

	@Test
	@RunAsClient
	public void testStoreContactCreatesContactIfNotExist(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl)
			throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));

		bookClient.storeContact(token, addressBookId, contact, null);

		Contact contactFromServer = bookClient.getContactFromId(token, addressBookId, 1);

		assertThat(contactFromServer).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId");
	}

	@Test
	@RunAsClient
	public void testStoreContactModifiesContactIfExists(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl)
			throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));

		bookClient.createContact(token, addressBookId, contact, null);
		contact.setUid(1);
		contact.setCommonname("newCommonName");
		bookClient.storeContact(token, addressBookId, contact, null);

		Contact contactFromServer = bookClient.getContactFromId(token, addressBookId, 1);
		assertThat(contactFromServer).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId");
	}

	@Test
	@RunAsClient
	public void testStoreContactClearPutInsteadOfPatch(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl)
			throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));

		bookClient.createContact(token, addressBookId, contact, null);
		contact.setUid(1);
		contact.getPhones().clear();
		contact.getAddresses().clear();
		contact.getImIdentifiers().clear();
		contact.getWebsites().clear();
		bookClient.storeContact(token, addressBookId, contact, null);

		Contact contactFromServer = bookClient.getContactFromId(token, addressBookId, 1);
		assertThat(contactFromServer).isEqualToIgnoringGivenFields(contact, "uid", "comment", "folderId", "websites");
		assertThat(contactFromServer.getWebsites()).containsOnly(new Website("CALURI;X-OBM-Ref1", "calUrui-obm.com"));
	}

	@Test(expected=ContactNotFoundException.class)
	@RunAsClient
	public void testStoreContactWithUnkownnId(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl)
			throws Exception {
		locatorService.configure(baseUrl);
		AccessToken token = loginClient.login(calendar, UserPassword.valueOf("user1"));

		bookClient.createContact(token, addressBookId, contact, null);
		contact.setUid(666);
		bookClient.storeContact(token, addressBookId, contact, null);
	}	

	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils.createDeployment();
	}
}
