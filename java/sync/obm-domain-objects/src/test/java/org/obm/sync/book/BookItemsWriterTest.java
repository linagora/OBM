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
package org.obm.sync.book;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.AddressBook.Id;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;


public class BookItemsWriterTest {

	private BookItemsWriter writer;

	@Before
	public void initBookItemsWriter(){
		writer = new BookItemsWriter();
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void testGetXMLDocumentFromContact() throws TransformerException, SAXException, IOException {
		String expectedXML = loadXmlFile("SimpleContact.xml");
		Document resultDocument = writer.getXMLDocumentFrom(mockContact());
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
    public void testGetXMLDocumentFromContacts() throws TransformerException, SAXException, IOException {
        String expectedXML = loadXmlFile("SimpleListOfContacts.xml");
        Document resultDocument = writer.getXMLDocumentFromContacts(ImmutableList.of(mockContact()));
        XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromAddressBook() throws TransformerException, SAXException, IOException {
		String expectedXML = loadXmlFile("SimpleAddressBook.xml");
		Document resultDocument = writer.getXMLDocumentFromAddressBooks(ImmutableList.of(mockAddressBook()));
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromContactChanges() throws TransformerException, SAXException, IOException {
		String expectedXML = loadXmlFile("SimpleContactChanges.xml");
		Document resultDocument = writer.getXMLDocumentFrom(mockContactChanges());
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromFolder() throws TransformerException, IOException, SAXException {
		String expectedXML = loadXmlFile("SimpleFolder.xml");
		Document resultDocument = writer.getXMLDocumentFrom(mockFolder());
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromAddressBookChanges() throws TransformerException, IOException, SAXException {
		String expectedXML = loadXmlFile("SimpleAddressBookChanges.xml");
		Document resultDocument = writer.getXMLDocumentFrom(mockAddressBookChanges());
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));
	}

	@Test
	public void testGetXMLDocumentFromOfListAddressBooksChanged() throws TransformerException, IOException, SAXException {
		String expectedXML = loadXmlFile("SimpleFolderChanges.xml");
		Document resultDocument = writer.getXMLDocumentFrom(mockFolderChanges());
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));		
	}

	@Test
	public void testGetXMLDocumentFromCountContacts() throws TransformerException, SAXException, IOException {
		String expectedXML = loadXmlFile("SimpleCountOfContacts.xml");
		Document resultDocument = writer.getXMLDocumentFrom(123);
		XMLAssert.assertXMLEqual(expectedXML, DOMUtils.serialize(resultDocument));		
	}

	private Contact mockContact() {
		Contact newContact = new Contact();

		newContact.setUid(1);
		newContact.setCommonname("commonname");
		newContact.setFirstname("firstname");
		newContact.setLastname("lastname");
		newContact.setMiddlename("middlename");
		newContact.setSuffix("suffix");
		newContact.setTitle("title");
		newContact.setService("service");
		newContact.setAka("aka");
		newContact.setComment("comment");
		newContact.setBirthday(DateUtils.date("2014-10-03T12:00:00Z"));
		newContact.setAnniversary(DateUtils.date("2014-10-03T12:00:00Z"));
		newContact.setAssistant("assistant");
		newContact.setManager("manager");
		newContact.setSpouse("spouse");
		newContact.setFolderId(1);
		newContact.setCollected(true);
		newContact.setCalUri("caluri");

		newContact.addPhone("phone", new Phone("123"));
		newContact.addWebsite(new Website("label", "url"));
		newContact.addEmail("email", EmailAddress.loginAtDomain("email@linagora"));
		newContact.addIMIdentifier("im", new InstantMessagingId("protocol", "address"));
		newContact.addAddress("address", new Address("street", "zipCode", "expressPostal",
			"town", "country", "state"));

		return newContact;
	}

	private AddressBook mockAddressBook() {
		return AddressBook.builder()
				.name("name")
				.uid(Id.valueOf(123))
				.readOnly(true)
				.build();
	}

	private ContactChanges mockContactChanges() {
		return new ContactChanges(
				ImmutableList.of(mockContact()),
				ImmutableSet.of(123),
				DateUtils.date("2014-10-03T12:00:00Z"));
	}

	private Folder mockFolder() {
		return Folder.builder()
					.uid(123)
					.name("name")
					.ownerDisplayName("ownerDisplayName")
					.ownerLoginAtDomain("ownerLoginAtDomain")
					.build();
	}

	private FolderChanges mockFolderChanges() {
		return FolderChanges.builder()
							.lastSync(DateUtils.date("2014-10-03T12:00:00Z"))
							.removed(mockFolder())
							.updated(mockFolder())
							.build();
	}

	private AddressBookChangesResponse mockAddressBookChanges() {
		AddressBookChangesResponse newAddressBookChangesResponse = new AddressBookChangesResponse();
		newAddressBookChangesResponse.setContactChanges(mockContactChanges());
		newAddressBookChangesResponse.setBooksChanges(mockFolderChanges());
		newAddressBookChangesResponse.setLastSync(DateUtils.date("2014-10-03T12:00:00Z"));

		return newAddressBookChangesResponse;
	}

	private String loadXmlFile(String filename) throws IOException {
		InputStream inputStream = ClassLoader.getSystemClassLoader()
				.getResourceAsStream(filename);

		String fileContent = CharStreams.toString(new InputStreamReader(inputStream));
		fileContent = fileContent.replaceAll("\n|\t", "");
		return fileContent;
	}
}
