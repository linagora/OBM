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
package com.linagora.obm.ui.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIUser;

public class LoginPage extends RootPage {

	private WebElement loginForm;
	private WebElement loginField;
	private WebElement passwordField;
	@FindBy(id = "sel_domain_id")
	private WebElement selectDropdown;
	@FindBy(css = "#loginForm fieldset > legend[class=\"error\"]")
	private WebElement errorLegend;

	public LoginPage(WebDriver driver) {
		super(driver);
	}

	@Override
	public void open() {
		driver.get(mapping.lookup(LoginPage.class).toExternalForm());
	}

	public LoginPage loginAsExpectingError(UIUser user, UIDomain domain) {
		doLogin(user, domain);
		return this;
	}

	public HomePage login(UIUser user, UIDomain domain) {
		doLogin(user, domain);
		return pageFactory.create(driver, HomePage.class);
	}

	private void doLogin(UIUser user, UIDomain domain) {
		loginField.sendKeys(user.getLogin());
		passwordField.sendKeys(user.getPassword());
		for (WebElement domainOption : selectDropdown.findElements(By.tagName("option"))) {
			if (domainOption.getText().startsWith(domain.getName() + " ")) {
				domainOption.click();
			}
		}
		loginForm.submit();
	}

	public WebElement elLoginForm() {
		return loginForm;
	}

	public WebElement elErrorLegend() {
		return errorLegend;
	}
}
