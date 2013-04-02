package org.obm.sync.book;

import static fr.aliacom.obm.ToolBox.loadXmlFile;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Before;
import org.junit.Test;
import org.obm.sync.base.EmailAddress;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class BookItemsParserTest {
	BookItemsParser parser;
	BookItemsWriter writer;

	@Before
	public void setUp() {
		parser = new BookItemsParser();
		writer = new BookItemsWriter();
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
}
