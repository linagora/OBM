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
package com.linagora.obm.ui.tests;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIContact;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.ioc.Module;
import com.linagora.obm.ui.page.ContactPage;
import com.linagora.obm.ui.page.CreateContactPage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;

@GuiceModule(Module.class)
@RunWith(SlowGuiceRunner.class)
public class UICreateContactTest {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;
	
	private UIUser uiUser;
	private UIDomain uiDomain;

	@Before
	public void setUp() {
		uiUser = UIUser.user();
		uiDomain = UIDomain.obmDomain();

		LoginPage loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		loginPage.login(uiUser, uiDomain);
	}
	
	@After
	public void tearDown() {
		driver.quit();
	}

	@Test
	public void createContactFailsNoName() {
		ContactPage contactPage = pageFactory.create(driver, ContactPage.class);
		contactPage.open();
		
		CreateContactPage createContactPage = contactPage.openCreateContactPage();
		CreateContactPage failedCreationPage = createContactPage.createContactAsExpectingError(UIContact.emptyFields());
		
		WebElement lastNameField = failedCreationPage.elLastname();
		assertThat(lastNameField.getAttribute("class")).isEqualTo("error");
		assertThat(lastNameField.getAttribute("title")).isEqualTo("Vous devez renseigner le Nom avant de valider.");
	}
	
	@Test
	public void createContactSuccess() {
		ContactPage contactPage = pageFactory.create(driver, ContactPage.class);
		contactPage.open();
		
		CreateContactPage createContactPage = contactPage.openCreateContactPage();
		UIContact contactToCreate = UIContact
				.builder()
				.firstName("J'ohn")
				.lastName("D'oe")
				.companyField("Lina'gora")
				.build();
		
		ContactPage okCreationPage = createContactPage.createContact(contactToCreate);
		assertThat(okCreationPage.countContactsWithLastnameInList(contactToCreate)).isEqualTo(1);
	}
	
	@Test
	public void createContactFailsAlreadyExistsAccept() {
		ContactPage contactPage = pageFactory.create(driver, ContactPage.class);
		contactPage.open();
		
		CreateContactPage createContactPage = contactPage.openCreateContactPage();
		UIContact contactToCreate = UIContact
				.builder()
				.firstName("existing")
				.lastName("contact")
				.build();
		// A popup appears because contact already exists, but we accept to recreate it
		
		ContactPage okCreationPage = createContactPage.createContact(contactToCreate);
		assertThat(okCreationPage.countContactsWithLastnameInList(contactToCreate)).isEqualTo(2);
	}
	
	@Test
	public void	createContactFailsAlreadyExistsCancel() {
		ContactPage contactPage = pageFactory.create(driver, ContactPage.class);
		contactPage.open();
		
		CreateContactPage createContactPage = contactPage.openCreateContactPage();
		UIContact contactToCreate = UIContact
				.builder()
				.firstName("existing")
				.lastName("contact")
				.build();
			
		// A popup appears because contact already exists, we cancel it
		
		ContactPage canceledCreationPage = createContactPage.createContact(contactToCreate);
		assertThat(canceledCreationPage.countContactsWithLastnameInList(contactToCreate)).isEqualTo(2);
	}
	
	
	
	
}
	
