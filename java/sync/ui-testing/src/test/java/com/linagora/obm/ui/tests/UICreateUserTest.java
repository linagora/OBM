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
		driver.quit();
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
		
		List<WebElement> messages = creationSummaryPage.messages();
		assertThat(messages).hasSize(2);
		assertThat(messages.get(0).getText()).isEqualTo("Utilisateur : Insertion réussie");
		WebElement findElement = messages.get(1).findElement(By.tagName("input"));
		assertThat(findElement.getAttribute("value")).isEqualTo("Télécharger la fiche utilisateur");
    }
}
