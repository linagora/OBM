
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
package org.obm.sync.book;

import static fr.aliacom.obm.ToolBox.loadXmlFile;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.base.EmailAddress;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BookItemsParserTest {

	private BookItemsParser parser;

	@Before
	public void setUp() {
		parser = new BookItemsParser();
	}

	@Test
	public void testOBMFULL4544ParseValidContact()
			throws SAXException, IOException, FactoryConfigurationError{
		String xml = loadXmlFile("validContact.xml");
		Contact aContact = parser.parseContact(xml);

		HashMap<String, EmailAddress> emails = Maps.newHashMap();
		emails.put("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("test@foo.fr"));
		HashMap<String, Phone> phones = Maps.newHashMap();
		phones.put("WORK;VOICE;X-OBM-Ref1", new Phone("voice"));
		phones.put("WORK;FAX;X-OBM-Ref1", new Phone("work"));
		HashSet<Website> websites = Sets.newHashSet(
				new Website("URL;X-OBM-Ref1", "http://www.uneURL.fr"),
				new Website("URL;X-OBM-Ref2", ""));

		assertThat(aContact.getUid()).isEqualTo(8);
		assertThat(aContact.getCommonname()).isEqualTo("DOE John");
		assertThat(aContact.getFirstname()).isEqualTo("DOE");
		assertThat(aContact.getLastname()).isEqualTo("John");
		assertThat(aContact.getService()).isEqualTo("service");
		assertThat(aContact.getTitle()).isEqualTo("title");
		assertThat(aContact.getAka()).isEqualTo("aka");
		assertThat(aContact.getCompany()).isEqualTo("company");
		assertThat(aContact.getWebsites()).isEqualTo(websites);
		assertThat(aContact.getEmails()).isEqualTo(emails);
		assertThat(aContact.getPhones()).isEqualTo(phones);
	}
	
	@Test
	public void testParseMinimalContact() throws SAXException, IOException {
		String xml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<contact>" +
				"<first>firstname</first>" +
				"<last>lastname</last>" +
				"<commonname>test test</commonname>" +
				"<emails>" +
					"<mail label=\"INTERNET;X-OBM-Ref1\" value=\"test@test.fr\"/>" +
					"<mail label=\"INTERNET;X-OBM-Ref2\" value=\"\"/>" +
				"</emails>" +
				"<phones/>" +
				"<addresses>" +
					"<address label=\"HOME;X-OBM-Ref1\" country=\"\" town=\"\" state=\"\" zip=\"\"></address>" +
					"<address label=\"WORK;X-OBM-Ref1\" country=\"\" town=\"\" state=\"\" zip=\"\"></address>" +
				"</addresses>" +
				"<websites>" +
					"<site label=\"URL;X-OBM-Ref2\" url=\"\"/>" +
					"<site label=\"URL;X-OBM-Ref1\" url=\"\"/>" +
				"</websites>" +
				"<instantmessaging/>" +
			"</contact>";
		
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Contact contact = parser.parseContact(doc.getDocumentElement());
		
		Contact expectedContact = new Contact();
		expectedContact.setCollected(false);
		expectedContact.setFirstname("firstname");
		expectedContact.setLastname("lastname");
		expectedContact.setCommonname("test test");
		expectedContact.setTitle("");
		expectedContact.setMiddlename("");
		expectedContact.setSpouse("");
		expectedContact.setSuffix("");
		expectedContact.setAka("");
		expectedContact.setAssistant("");
		expectedContact.setCalUri("");
		expectedContact.setComment("");
		expectedContact.setCompany("");
		expectedContact.setManager("");
		expectedContact.setService("");
		expectedContact.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("test@test.fr"));
		expectedContact.addWebsite(new Website("URL;X-OBM-Ref1", ""));
		expectedContact.addWebsite(new Website("URL;X-OBM-Ref2", ""));
		expectedContact.addAddress("HOME;X-OBM-Ref1", new Address("", "", "", "", "", ""));
		expectedContact.addAddress("WORK;X-OBM-Ref1", new Address("", "", "", "", "", ""));
		assertThat(contact).isEqualTo(expectedContact);
	}
	
	@Test
	public void testParseContact() throws SAXException, IOException {
		String xml = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<contact uid=\"15\" collected=\"true\">" +
				"<first>firstname</first>" +
				"<last>lastname</last>" +
				"<middlename>middlename</middlename>" +
				"<commonname>test test</commonname>" +
				"<title>title</title>" +
				"<service>service</service>" +
				"<aka>aka</aka>" +
				"<company>company</company>" +
				"<service>service</service>" +
				"<spouse>spouse</spouse>" +
				"<suffix>suffix</suffix>" +
				"<assistant>assistant</assistant>" +
				"<comment>comment comment</comment>" +
				"<manager>manager</manager>" +
				"<caluri>caluri</caluri>" +
				"<addressbookid>8</addressbookid>" +
				"<birthday>994904956105</birthday>" +
				"<anniversary>994904956105</anniversary>" +
				"<emails>" +
					"<mail label=\"INTERNET;X-OBM-Ref1\" value=\"test@test.fr\"/>" +
					"<mail label=\"INTERNET;X-OBM-Ref2\" value=\"test2@test2.fr\"/>" +
				"</emails>" +
				"<phones/>" +
				"<addresses>" +
					"<address label=\"HOME;X-OBM-Ref1\" country=\"France\" town=\"Lyon\" state=\"State\" zip=\"69000\">street</address>" +
				"</addresses>" +
				"<websites>" +
					"<site label=\"URL;X-OBM-Ref2\" url=\"http://obm.org\"/>" +
				"</websites>" +
				"<instantmessaging/>" +
			"</contact>";
		
		Document doc = DOMUtils.parse(new ByteArrayInputStream(xml.getBytes()));
		Contact contact = parser.parseContact(doc.getDocumentElement());
		
		Contact expectedContact = new Contact();
		expectedContact.setUid(15);
		expectedContact.setCollected(true);
		expectedContact.setFirstname("firstname");
		expectedContact.setLastname("lastname");
		expectedContact.setCommonname("test test");
		expectedContact.setTitle("title");
		expectedContact.setMiddlename("middlename");
		expectedContact.setSpouse("spouse");
		expectedContact.setSuffix("suffix");
		expectedContact.setAka("aka");
		expectedContact.setAssistant("assistant");
		expectedContact.setCalUri("caluri");
		expectedContact.setComment("comment comment");
		expectedContact.setCompany("company");
		expectedContact.setManager("manager");
		expectedContact.setService("service");
		expectedContact.setFolderId(8);
		expectedContact.setBirthday(new Date(994904956105l));
		expectedContact.setAnniversary(new Date(994904956105l));
		expectedContact.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("test@test.fr"));
		expectedContact.addEmail("INTERNET;X-OBM-Ref2", EmailAddress.loginAtDomain("test2@test2.fr"));
		expectedContact.addAddress("HOME;X-OBM-Ref1", new Address("street", "69000", "", "Lyon", "France", "State"));
		expectedContact.addWebsite(new Website("URL;X-OBM-Ref2", "http://obm.org"));
		
		assertThat(contact).isEqualTo(expectedContact);
	}
}
