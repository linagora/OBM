/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package com.linagora.obm.ui.scenario.contact;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIContact;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.page.ContactPage;
import com.linagora.obm.ui.page.CreateContactPage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ContactStepdefs {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;
	
	private UIUser uiUser;
	private UIDomain uiDomain;
	
	private CreateContactPage createContactPage;
	private CreateContactPage processedCreateContactPage;
	private UIContact contactToCreate;
	private ContactPage okCreationPage;
	
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

	@Given("on create contact page")
	public void createContactPage() {
		ContactPage contactPage = pageFactory.create(driver, ContactPage.class);
		contactPage.open();
		
		createContactPage = contactPage.openCreateContactPage();
	}
	
	@When("user creates a contact without lastname")
	public void createContactWithoutLastname() {
		processedCreateContactPage = createContactPage.createContactAsExpectingError(new UIContact());
	}
	
	@Then("creation fails")
	public void creationFails() {
		WebElement lastNameField = processedCreateContactPage.elLastname();
		assertThat(lastNameField.getAttribute("class")).isEqualTo("error");
		assertThat(lastNameField.getAttribute("title")).isEqualTo("Vous devez renseigner le Nom avant de valider.");
	}
	
	@When("user creates a contact:$")
	public void userCreatesContact(List<UIContact> contacts) {
		assertThat(contacts).hasSize(1);
		contactToCreate = contacts.get(0);
	}

	@And("user validate")
	public void userValidate() {
		okCreationPage = createContactPage.createContact(contactToCreate);
	}
	
	@And("user validate accepting existing popup") 
	public void userValidateAcceptingExistingPopup() {
		okCreationPage = createContactPage.createContactAndRespondOKTOConfirmCreation(contactToCreate);
	}
	
	@And("user validate cancelling existing popup") 
	public void userValidateCancellingExistingPopup() {
		processedCreateContactPage = createContactPage.createContactAndRespondCancelTOConfirmCreation(contactToCreate);
	}
	
	@Then("creation page still active with \"([^\"]*)\" as lastname")
	public void creationPageStillActive(String lastname) {
		WebElement lastNameField = processedCreateContactPage.elLastname();
		assertThat(lastNameField.getAttribute("value")).isEqualTo(lastname);
	}
	
	@Then("^\"([^\"]*)\" is once in contact list$")
	public void isOnceInContactList(String name) {
		count(name, 1);
	}
	
	@Then("^\"([^\"]*)\" is twice in contact list$")
	public void isTwiceInContactList(String name) {
		count(name, 2);
	}
	
	private void count(String name, int times) {
		assertThat(okCreationPage.countNameInList(name)).isEqualTo(times);
	}
	
}
