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

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.ContactLabel;
import org.obm.sync.book.Phone;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class VCFtoContactConverterTest {

	private TimeZone originalDefaultTZ;

	@Before
	public void setUp() {
		originalDefaultTZ = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
	@After
	public void tearDown() {
		TimeZone.setDefault(originalDefaultTZ);
	}
	
	@Test
	public void convertShouldReturnEmptyListWhenNullString() {
		List<Contact> contacts = new VCFtoContactConverter().convert(null);
		
		assertThat(contacts).isEmpty();
	}
	
	@Test
	public void convertShouldReturnEmptyListWhenEmptyString() {
		List<Contact> contacts = new VCFtoContactConverter().convert("");
		
		assertThat(contacts).isEmpty();
	}
	
	@Test
	public void convertShouldReturnEmptyListWhenNoVCardData() {
		List<Contact> contacts = new VCFtoContactConverter().convert("not vcard data");
		
		assertThat(contacts).isEmpty();
	}
	
	@Test
	public void convertShouldReturnEmptyListWhenEmptyVCard() throws IOException {
		String vcfData = Resources.toString(Resources.getResource("vcf/empty.vcf"), Charsets.UTF_8);
		List<Contact> contacts = new VCFtoContactConverter().convert(vcfData);
		
		assertThat(contacts).isEmpty();
	}
	
	@Test
	public void convertShouldAcceptMinimalVCard() throws IOException {
		Contact expectedContact = new Contact();
		expectedContact.setAka("Forrest Gump");
		
		String vcfData = Resources.toString(Resources.getResource("vcf/minimal.vcf"), Charsets.UTF_8);
		List<Contact> contacts = new VCFtoContactConverter().convert(vcfData);
		
		assertThat(contacts).containsOnly(expectedContact);
	}
	
	@Test
	public void convertShouldNotConsiderPostalAddressIfWorkIsDefined() throws IOException {
		Contact expectedContact = new Contact();
		expectedContact.setAka("");
		expectedContact.setTitle("");
		expectedContact.setCompany("Org name");
		expectedContact.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("(01) 99 99 99 99"));
		expectedContact.addAddress(ContactLabel.ADDRESS.getContactLabel(), 
				new Address("9/11 rue du test et tout..\n07000  VILLAGE COOLING", null, null, "Francehor", "France", null));
		expectedContact.addAddress(ContactLabel.ADDRESS_HOME.getContactLabel(), 
				new Address(null, null, null, null, null, null));
		
		String vcfData = Resources.toString(Resources.getResource("vcf/with-multiple-addresses.vcf"), Charsets.UTF_8);
		List<Contact> contacts = new VCFtoContactConverter().convert(vcfData);
		
		assertThat(contacts).containsOnly(expectedContact);
	}
	
	@Test
	public void convertShouldBehaveSmartlyWhenNoTypeIsDefined() throws IOException {
		Contact expectedContact = new Contact();
		expectedContact.setTitle("Business Man");
		expectedContact.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("(111) 555-1313"));
		expectedContact.addAddress(ContactLabel.ADDRESS.getContactLabel(), 
				new Address("100 Waters Edge", "30314", null, "Baytown", "United States of America", "LA"));
		expectedContact.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain("email@example.com"));
		
		String vcfData = Resources.toString(Resources.getResource("vcf/no-type-defined.vcf"), Charsets.UTF_8);
		List<Contact> contacts = new VCFtoContactConverter().convert(vcfData);
		
		assertThat(contacts).containsOnly(expectedContact);
	}
	
	@Test
	public void convertShouldReturnsMultipleContactsWhenVCFContainsMultplieVCards() throws IOException {
		Contact expectedContact1 = new Contact();
		expectedContact1.setLastname("Gump");
		expectedContact1.setFirstname("Forrest");
		expectedContact1.setAka("Forrest Gump");
		expectedContact1.setCompany("Bubba Gump Shrimp Co.");
		expectedContact1.setTitle("Shrimp Man");
		expectedContact1.addPhone(ContactLabel.FAX.getContactLabel(), new Phone("09090909"));
		expectedContact1.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("(111) 555-1212"));
		expectedContact1.addAddress(ContactLabel.ADDRESS.getContactLabel(), 
				new Address("111 Waters Edge", "30314", null, "Baytown", "United States of America", "LA"));
		expectedContact1.setAnniversary(DateUtils.dateUTC("2016-07-10"));
		expectedContact1.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain("email1@example.com"));
		expectedContact1.addEmail(ContactLabel.EMAIL2.getContactLabel(), EmailAddress.loginAtDomain("email2@example.com"));
		expectedContact1.addEmail(ContactLabel.EMAIL3.getContactLabel(), EmailAddress.loginAtDomain("email3@example.com"));
		
		Contact expectedContact2 = new Contact();
		expectedContact2.setLastname("Gump2");
		expectedContact2.setFirstname("Forrest2");
		expectedContact2.setAka("Gump2 Forrest");
		expectedContact2.setTitle("Business Man");
		expectedContact2.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("(111) 555-1313"));
		expectedContact2.addPhone(ContactLabel.PHONE_HOME.getContactLabel(), new Phone("(404) 555-1313"));
		expectedContact2.addAddress(ContactLabel.ADDRESS.getContactLabel(), 
				new Address("999 Waters Edge", "30314", null, "Baytown", "United States of America", "LA"));
		expectedContact2.addAddress(ContactLabel.ADDRESS_HOME.getContactLabel(), 
				new Address("99 Plantation St.", "30314", null, "Baytown", "United States of America", "LA"));
		expectedContact2.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain("email4@example.com"));
		expectedContact2.setComment("Multiple lines\n\nwith email <mailto:contact@domain.org>");
		
		String vcfData = Resources.toString(Resources.getResource("vcf/sample.vcf"), Charsets.UTF_8);
		List<Contact> contacts = new VCFtoContactConverter().convert(vcfData);
		
		assertThat(contacts).containsOnly(expectedContact1, expectedContact2);
	}

}
