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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Predicate;
import com.linagora.obm.ui.bean.UIContact;

public class CreateContactPage extends ContactPage {
	
	private WebElement firstname;
	private WebElement lastname;
	private WebElement companyField;
	private WebElement mailokField;
	private WebElement newsletterField;
	private WebElement contactForm;

	
	public CreateContactPage(WebDriver driver) {
		super(driver);
	}
	
	@Override
	public void open() {
		driver.get(mapping.lookup(CreateContactPage.class).toExternalForm());
	}

	public ContactPage createContact(UIContact contactToCreate) {
		doCreateContact(contactToCreate);
		waitForAjaxRequestToComplete();
		
		return createContactPage();
	}
	
	public ContactPage createContactAndRespondOKTOConfirmCreation(UIContact contactToCreate) {
		doCreateContact(contactToCreate);
		
		managePopup(true);
		
		return createContactPage();
	}

	public CreateContactPage createContactAndRespondCancelTOConfirmCreation(UIContact contactToCreate) {
		doCreateContact(contactToCreate);
		
		managePopup(false);
		
		return this;
	}

	private ContactPage createContactPage() {
		new FluentWait<WebDriver>(driver)
			.withTimeout(2, TimeUnit.SECONDS)
			.until(new Predicate<WebDriver>() {
				@Override
				public boolean apply(WebDriver input) {
					return !informationGrid.isDisplayed() 
						&& "expanded".equals(dataGrid.getAttribute("class"));
				}
		});
		
		return pageFactory.create(driver, ContactPage.class);
	}
	
	public CreateContactPage createContactAsExpectingError(UIContact contactToCreate) {
		doCreateContact(contactToCreate);
		new FluentWait<WebDriver>(driver).until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return lastname.getAttribute("class").equalsIgnoreCase("error");
			}
		});
		return this;
	}

	private void doCreateContact(UIContact contactToCreate) {
		firstname.sendKeys(StringUtils.defaultIfBlank(contactToCreate.getFirstName(), ""));
		lastname.sendKeys(StringUtils.defaultIfBlank(contactToCreate.getLastName(), ""));
		companyField.sendKeys(StringUtils.defaultIfBlank(contactToCreate.getCompanyField(), ""));
		clickCheckbox(mailokField, contactToCreate.isMailokField());
		clickCheckbox(newsletterField, contactToCreate.isNewsletterField());

		contactForm.submit();
	}

	public WebElement elFirstname() {
		return firstname;
	}

	public WebElement elLastname() {
		return lastname;
	}

	public WebElement elCompanyField() {
		return companyField;
	}

	public WebElement elMailokField() {
		return mailokField;
	}

	public WebElement elNewsletterField() {
		return newsletterField;
	}

	public WebElement elContactForm() {
		return contactForm;
	}
}
