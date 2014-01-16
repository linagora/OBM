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
package com.linagora.obm.ui.scenario.login;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UILogin;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.page.HomePage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;
import com.linagora.obm.ui.bean.UIDomain;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class LoginStepdefs {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;

	LoginPage loginPage;
	private UIUser userUser;
	private UIDomain userDomain;
	private HomePage homePage;
	@Before
	public void setUp() {
			
	}
	
	@After
	public void tearDown() {
		driver.quit();
	}

	@Given("on login page")
	public void openLoginPage() {
		loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
	}
	
	@Then("title contains \"([^\"]*)\"$")
	public void titleContains(String title) {
		assertThat(loginPage.currentTitle()).contains(title);
	}
	
	@When("user logs as:$")
	public void userLogsAs(List<UILogin> logins) {
		assertThat(logins).hasSize(1);
		UILogin login = logins.get(0);
		userUser = UIUser.builder()
				.login(login.getLogin())
				.password(login.getPassword())
				.build();
		domainIs(login.getDomain());
	}
	
	private void domainIs(String domain) {
		if (domain.matches("Global Domain")) {
			userDomain = UIDomain.globalDomain();
		}
		else {
			userDomain = UIDomain.obmDomain();
		}
	}

	@And("user logout")
	public void logout() {
		HomePage homePage = loginPage.login(userUser, userDomain);
		homePage.logout();
	}
	
	@And("user logout by url")
	public void logoutByUrl() {
		HomePage homePage = loginPage.login(userUser, userDomain);
		homePage.logoutByUrl();
	}
	
	@Then("invalid login page displayed")
	public void invalidLoginPage() {
		LoginPage newPage = loginPage.loginAsExpectingError(
				userUser,
				userDomain);
		
		WebElement deniedLoginForm = newPage.elLoginForm();
		assertThat(deniedLoginForm).isNotNull();

		WebElement errorLegend = newPage.elErrorLegend();
		assertThat(errorLegend).isNotNull();
		assertThat(errorLegend.getText()).isEqualTo("Login ou mot de passe invalide.");
	}
	
	@Then("user is logged")
	public void userIsLoggedAs() {
		homePage = loginPage.login(userUser, userDomain);

		assertThat(homePage.currentTitle()).contains("OBM");
		assertThat(homePage.elInformationUser().getText()).startsWith(userUser.getLogin()).contains(userDomain.getName());
	}
	
	@Then("empty login page is displayed")
	public void emptyLoginPageIsDisplayed() {
		LoginPage newLoginPage = pageFactory.create(driver, LoginPage.class);
		assertThat(newLoginPage.elLoginForm()).isNotNull();
	}
	
	@Then("empty login page is not displayed")
	public void emptyLoginPageIsNotDisplayed() {
    	LoginPage newPage = pageFactory.create(driver, LoginPage.class);
    	assertThat(newPage.elLoginForm().isDisplayed());
	}
	
}
