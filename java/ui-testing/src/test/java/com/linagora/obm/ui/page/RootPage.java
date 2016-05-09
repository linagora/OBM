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
package com.linagora.obm.ui.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.inject.Inject;
import com.linagora.obm.ui.service.Services.Logout;
import com.linagora.obm.ui.url.ServiceUrlMapping;
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor;

public abstract class RootPage implements Page {

	@Inject protected ServiceUrlMapping mapping;
	@Inject protected PageFactory pageFactory;
	protected final WebDriver driver;
	protected int timeout = 30;
	
	@FindBy(name="displayMessageOk")
	private List<WebElement> messagesOk;
	@FindBy(name="displayMessageInfo")
	private List<WebElement> messagesInfo;
	@FindBy(name="displayMessageWarning")
	private List<WebElement> messagesWarning;
	@FindBy(name="displayMessageError")
	private List<WebElement> messagesError;
	@FindBy(id="logout")
	private WebElement logoutLink;
	@FindBy(id="tools_update_index")
	private WebElement updateYellowSystem;
	private WebDriverWait webDriverWait;

	public RootPage(WebDriver driver) {
		this.driver = driver;
		webDriverWait = new WebDriverWait(driver, timeout);
	}
	
	@Override
	public String currentTitle() {
		return driver.getTitle();
	}	

	@Override
	public void open() {
		driver.get(mapping.lookup(LoginPage.class).toExternalForm());
	}	

	public LoginPage logout() {
		elLogoutLink().click();
		return pageFactory.create(driver, LoginPage.class);
	}

	public LoginPage logoutByUrl() {
		driver.get(mapping.lookup(Logout.class).toExternalForm());
		return pageFactory.create(driver, LoginPage.class);
	}

	public List<WebElement> elMessagesOk() {
		return messagesOk;
	}

	public List<WebElement> elMessagesInfo() {
		return messagesInfo;
	}

	public List<WebElement> elMessagesWarning() {
		return messagesWarning;
	}

	public List<WebElement> elMessagesError() {
		return messagesError;
	}

	public WebElement elLogoutLink() {
		return logoutLink;
	}
	
	protected boolean clickCheckbox(WebElement field, boolean hasToBeClicked) {
		if (hasToBeClicked) {
			field.click();
		}
		return hasToBeClicked;
	}
	
	public WebElement getDivByTitle(String title) {
		return driver.findElement(new ByCssSelector("div[title*='" + title + "']"));
	}
	
	public void waitForPageToLoad() {
		WebDriverCommandProcessor webDriverCommandProcessor = new WebDriverCommandProcessor(driver.getCurrentUrl(), driver);
		webDriverCommandProcessor.doCommand("waitForPageToLoad", new String[] {Integer.toString(timeout)});
	}

	public void waitAndTestElementPresence(By by) {
		webDriverWait.until(ExpectedConditions.presenceOfElementLocated(by));
	}

	public void waitForElementAndClick(By by) {
		webDriverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
	}

	public void clickOnSubmit() {
		waitForElementAndClick(By.cssSelector("input[type=\"submit\"]"));
	}

	public RootPage gotoUpdateSystem() {
		updateYellowSystem.click();
		return this;

	}

}
