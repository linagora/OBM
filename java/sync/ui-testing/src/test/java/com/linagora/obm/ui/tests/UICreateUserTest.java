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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.bean.UIUserKind;
import com.linagora.obm.ui.bean.UIUserProfile;
import com.linagora.obm.ui.ioc.Module;
import com.linagora.obm.ui.page.CreateUserPage;
import com.linagora.obm.ui.page.CreateUserSummaryPage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;

@GuiceModule(Module.class)
@RunWith(SlowGuiceRunner.class)
public class UICreateUserTest {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;
	
	private UIUser uiUser;
	private UIDomain uiDomain;

	@Before
	public void setUp() {
		uiUser = UIUser.admin0();
		uiDomain = UIDomain.globalDomain();

		LoginPage loginPage = pageFactory.create(driver, LoginPage.class);
		loginPage.open();
		loginPage.login(uiUser, uiDomain);
	}
	
	@After
	public void tearDown() {
		// TODO Pastille jaune ou suppression de ce qu'on a créé ?
		driver.quit();
	}

	@Test
	public void createUserFailsNoName() {
		CreateUserPage createPage = pageFactory.create(driver, CreateUserPage.class);
		createPage.open();
		
		CreateUserPage failedCreateUserPage = createPage.createUserAsExpectingError(UIUser.builder()
				.login("testAdmin")
				.password("admin")
				.commonName("admin")
				.profile(UIUserProfile.ADMIN)
				.address1("add1")
				.address2("add2")
				.phone("0606060606")
				.build());
		
		List<WebElement> errorMessages = failedCreateUserPage.elMessagesError();
		assertThat(failedCreateUserPage.elMessagesInfo()).isEmpty();
		assertThat(failedCreateUserPage.elMessagesOk()).isEmpty();
		assertThat(failedCreateUserPage.elMessagesWarning()).isEmpty();
		assertThat(errorMessages).hasSize(1);
		assertThat(errorMessages.get(0).getText()).isEqualTo("Données invalides : Le nom doit être correctement renseigné ! :");
	}
	
	@Test
	public void createUserFailsNoEmail() {
		CreateUserPage createPage = pageFactory.create(driver, CreateUserPage.class);
		createPage.open();
		
		CreateUserSummaryPage creationSummaryPage = createPage.createUser(UIUser.builder()
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
		
		List<WebElement> errorMessages = creationSummaryPage.elMessagesError();
		assertThat(creationSummaryPage.elMessagesInfo()).isEmpty();
		assertThat(creationSummaryPage.elMessagesOk()).isEmpty();
		assertThat(creationSummaryPage.elMessagesWarning()).isEmpty();
		assertThat(errorMessages).hasSize(1);
		assertThat(errorMessages.get(0).getText()).isEqualTo("Données invalides : Vous devez saisir une adresse E-mail afin d'activer la messagerie !");
	}
	
	
	@Test
	public void createUserAdmin() {
		CreateUserPage createPage = pageFactory.create(driver, CreateUserPage.class);
		createPage.open();
		
		CreateUserSummaryPage creationSummaryPage = createPage.createUser(UIUser.builder()
				.login("testAdmin")
				.lastName("admin lastname")
				.password("admin")
				.commonName("admin")
				.profile(UIUserProfile.ADMIN)
				.address1("add1")
				.address2("add2")
				.phone("0606060606")
				.build());

		List<WebElement> okMessages = creationSummaryPage.elMessagesOk();
		assertThat(creationSummaryPage.elMessagesInfo()).isEmpty();
		assertThat(creationSummaryPage.elMessagesError()).isEmpty();
		assertThat(creationSummaryPage.elMessagesWarning()).isEmpty();
		assertThat(okMessages).hasSize(2);
		assertThat(okMessages.get(0).getText()).isEqualTo("Utilisateur : Insertion réussie");
		WebElement findElement = okMessages.get(1).findElement(By.tagName("input"));
		assertThat(findElement.getAttribute("value")).isEqualTo("Télécharger la fiche utilisateur");
    }
	
	@Test
	public void createUser() {
		CreateUserPage createPage = pageFactory.create(driver, CreateUserPage.class);
		createPage.open();
		
		CreateUserSummaryPage creationSummaryPage = createPage.createUser(UIUser.builder()
				.kind(UIUserKind.MADAME)
				.login("testUser")
				.lastName("testUser lastname")
				.firstName("testUser Firstname")
				.password("testUser")
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
				.emailAddress("testuser")
				.build());

		List<WebElement> okMessages = creationSummaryPage.elMessagesOk();
		assertThat(creationSummaryPage.elMessagesInfo()).isEmpty();
		assertThat(creationSummaryPage.elMessagesError()).isEmpty();
		assertThat(creationSummaryPage.elMessagesWarning()).isEmpty();
		assertThat(okMessages).hasSize(2);
		assertThat(okMessages.get(0).getText()).isEqualTo("Utilisateur : Insertion réussie");
		WebElement findElement = okMessages.get(1).findElement(By.tagName("input"));
		assertThat(findElement.getAttribute("value")).isEqualTo("Télécharger la fiche utilisateur");
    }
	
	@Test
	public void createUserFailsAlreadyExists() {
		CreateUserPage createPage = pageFactory.create(driver, CreateUserPage.class);
		createPage.open();
		
		CreateUserPage failedCreateUserPage = createPage.createUserAsExpectingError(UIUser.builder()
				.login("testUser")
				.password("testUser")
				.commonName("commonname testUser")
				.profile(UIUserProfile.USER)
				.build());
		
		List<WebElement> errorMessages = failedCreateUserPage.elMessagesError();
		assertThat(failedCreateUserPage.elMessagesInfo()).isEmpty();
		assertThat(failedCreateUserPage.elMessagesOk()).isEmpty();
		assertThat(failedCreateUserPage.elMessagesWarning()).isEmpty();
		assertThat(errorMessages).hasSize(1);
		assertThat(errorMessages.get(0).getText()).isEqualTo("Données invalides : testuser : Le login est déjà attribué à un autre utilisateur !");
	}
}
	
