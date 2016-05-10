/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
 * 
 * ***** END LICENSE BLOCK ***** */
package com.linagora.obm.ui.scenario.user;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.bean.UIUserKind;
import com.linagora.obm.ui.bean.UIUserProfile;
import com.linagora.obm.ui.page.CreateUserPage;
import com.linagora.obm.ui.page.CreateUserSummaryPage;
import com.linagora.obm.ui.page.DeleteUserPage;
import com.linagora.obm.ui.page.FindUserPage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.LogoutPage;
import com.linagora.obm.ui.page.PageFactory;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class UserStepdefs {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;
	
	private UIUser uiUser;
	private UIDomain uiDomain;
	
	private LoginPage loginPage;
	private LogoutPage logoutPage;
	private CreateUserPage createUserPage;
	private FindUserPage findUserPage;
	private DeleteUserPage deleteUserPage;
	private CreateUserPage processedCreateUserPage;
	private CreateUserSummaryPage processedCreationSummaryPage;
	
	@After
	public void tearDown() {
		driver.quit();
	}

	@Given("connected as \"([^\"]*)\" with password \"([^\"]*)\" on domain \"([^\"]*)\"")
	public void connectAsUserToDomain(String userName, String password, String domainName) {
		connectAs(UIUser.builder().login(userName).password(password).build(), UIDomain.builder().name(domainName).build());
	}

	@Given("connected as admin0")
	public void connectAsAdmin0()
	{
		connectAs(UIUser.admin0(), UIDomain.globalDomain());
	}

	private void connectAs(UIUser user, UIDomain domain)
	{
		uiUser = user;
		uiDomain = domain;

		findUserPage = pageFactory.create(driver, FindUserPage.class);
		logoutPage = pageFactory.create(driver, LogoutPage.class);
		loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		loginPage.login(uiUser, uiDomain);
	}

	@Given("on create user page")
	public void createUserPage() {
		createUserPage = pageFactory.create(driver, CreateUserPage.class);
		createUserPage.open();
	}
	
	@Given("on delete user page")
	public void deleteUserPage() {
		deleteUserPage = pageFactory.create(driver, DeleteUserPage.class);
		deleteUserPage.open();
	}
	
	@When("user creates a user without name")
	public void createUserWithoutName() {
		processedCreateUserPage = createUserPage.createUserAsExpectingError(UIUser.builder()
				.login("testAdmin")
				.password("admin")
				.commonName("admin")
				.profile(UIUserProfile.ADMIN)
				.address1("add1")
				.address2("add2")
				.phone("0606060606")
				.build());
	}
	
	@When("user creates a user without email")
	public void createUserWithoutEmail() {
		processedCreateUserPage = createUserPage.createUserAsExpectingError(UIUser.builder()
				.login("testAdmin")
				.lastName("admin lastname")
				.password("admin")
				.commonName("admin")
				.profile(UIUserProfile.ADMIN)
				.address1("add1")
				.address2("add2")
				.emailInternalEnabled(true)
				.emailAddress("")
				.phone("0606060606")
				.build());
	}
	
	public void createUserWithAdminProfile(String login) {
		processedCreationSummaryPage = createUserPage.createUser(UIUser.builder()
				.login(login)
				.lastName("admin lastname")
				.password("admin")
				.commonName("admin")
				.profile(UIUserProfile.ADMIN)
				.address1("add1")
				.address2("add2")
				.phone("0606060606")
				.build());
	}

	public void createUserWithUserProfile(String login) {
		processedCreationSummaryPage = createUserPage.createUser(UIUser.builder()
				.kind(UIUserKind.MADAME)
				.login(login)
				.lastName("testUser lastname")
				.firstName("testUser Firstname")
				.password(login)
				.commonName("commonname testUser")
				.title("Chef d'usine")
				.noExpire(true)
				.profile(UIUserProfile.USER)
				.address1("Avenue de l'aviation")
				.address2("Impasse de l'immeuble")
				.addressZip("Z23456")
				.addressCedex("12 560")
				.addressTown("L'Isle d'Abeau")
				.phone("0606060606")
				.phone2("+33 4 72 56 98")
				.phoneMobile("+33 6-24-55-66")
				.phoneFax("0123654789")
				.phoneFax2("987564123")
				.company("Linagora de L'yon")
				.direction("Centre'Est")
				.service("d'utilité publique")
				.description("Here's a short description of this user : " +
						"This is a \"Lovely\" test with nearly all fields filled up !")
				.emailInternalEnabled(true)
				.emailAddress(login)
				.build());
	}

	@When("user creates a user \"([^\"]*)\" with admin profile")
	public void createUserWithAdminProfileWhen(String login) {
		createUserWithAdminProfile(login);
	}
	
	@When("user creates a user \"([^\"]*)\"")
	public void createUserWhen(String login) {
		createUserWithUserProfile(login);
	}
	
	@Given("\"([^\"]*)\" exists with admin profile")
	public void createUserWithAdminProfileGiven(String login) {
		createUserWithAdminProfile(login);
	}
	
	@Given("\"([^\"]*)\" exists with user profile")
	public void createUserGiven(String login) {
		createUserWithUserProfile(login);
	}
	
	@When("user creates a user already existing")
	public void createUserAlreadyExisting() {
		processedCreateUserPage = createUserPage.createUserAsExpectingError(UIUser.builder()
				.login("testUser")
				.password("testUser")
				.commonName("commonname testUser")
				.profile(UIUserProfile.USER)
				.build());
	}
	
	@Then("creation fails with \"([^\"]*)\" as message")
	public void creationFails(String message) {
		List<WebElement> errorMessages = processedCreateUserPage.elMessagesError();
		assertThat(processedCreateUserPage.elMessagesInfo()).isEmpty();
		assertThat(processedCreateUserPage.elMessagesOk()).isEmpty();
		assertThat(processedCreateUserPage.elMessagesWarning()).isEmpty();
		assertThat(errorMessages).hasSize(1);
		assertThat(errorMessages.get(0).getAttribute("class")).matches(".*\\binvalid_data\\b.*");
	}
	
	@Then("creation succeeds")
	public void creationSucceeds() {
		List<WebElement> okMessages = processedCreationSummaryPage.elMessagesOk();
		assertThat(processedCreationSummaryPage.elMessagesInfo()).isEmpty();
		assertThat(processedCreationSummaryPage.elMessagesError()).isEmpty();
		assertThat(processedCreationSummaryPage.elMessagesWarning()).isEmpty();
		assertThat(okMessages).hasSize(2);
		assertThat(okMessages.get(0).getAttribute("id")).isEqualTo("message_ok");
		WebElement findElement = okMessages.get(1).findElement(By.id("download_user_card"));
	}

	@When("user deletes \"([^\"]*)\"")
	public void deleteUser(String userLogin) {

		findUserPage.open();
		findUserPage.findUserByLogin(userLogin);

		deleteUserPage.deleteUserByLogin(userLogin);
	}
	
	@And("\"([^\"]*)\" is no longer in user list")
	public void isUserListed(String userLogin) {

		findUserPage.open();
		findUserPage.findUserByLogin(userLogin);

		assertThat(findUserPage.elMessagesWarning()).hasSize(1);
		assertThat(findUserPage.elMessagesWarning().get(0).getAttribute("class")).matches(".*\\bwarn_no_found\\b.*");
	}
	
	@And("\"([^\"]*)\" can t connect anymore with password \"([^\"]*)\" on domain \"([^\"]*)\"")
	public void connectionFailedTest(String userLogin, String password, String domainName)
	{
		logoutPage.logoutByUrl();
		connectAsUserToDomain(userLogin, password, domainName);
		WebElement errorLegend = loginPage.elErrorLegend();
		assertThat(errorLegend).isNotNull();
	}
	
	@Then("deletion succeeds")
	public void deletionSucceeded() {
		assertThat(deleteUserPage.deletionSucceeded()).isTrue();
	}
	
}
