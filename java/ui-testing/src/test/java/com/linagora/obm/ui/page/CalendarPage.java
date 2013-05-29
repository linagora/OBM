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
package com.linagora.obm.ui.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.linagora.obm.ui.page.widget.CalendarCalRangeWidget;
import com.linagora.obm.ui.page.widget.CalendarNavBarWidget;
import com.linagora.obm.ui.page.widget.CalendarViewWidget;

public class CalendarPage extends RootPage {
	
	@FindBy(tagName="p")
	private List<WebElement> messages;
	
	private WebElement newEventCalendar;
	protected WebElement calendarTitle;
	protected WebElement calendarHeader;
	private CalendarNavBarWidget calendarNavBarWidget;
	private CalendarViewWidget calendarViewWidget;
	private CalendarCalRangeWidget calendarCalRangeWidget;
	
	public CalendarPage(WebDriver driver) {
		super(driver);
	}
	
	@Override
	public void open() {
		driver.get(mapping.lookup(CalendarPage.class).toExternalForm());
	}
	
	@Override
	public void waitForPageToLoad() {
		resetCalendarPageWidgets();
		super.waitForPageToLoad();
	}

	private void resetCalendarPageWidgets() {
		calendarNavBarWidget = null;
		calendarViewWidget = null;
		calendarCalRangeWidget = null;
	}

	public CreateCalendarPage createCalendarPage() {
		newEventCalendar.click();
		
		new WebDriverWait(driver, 10).until(
				ExpectedConditions.presenceOfElementLocated(By.id("tf_title")));
		
		return pageFactory.create(driver, CreateCalendarPage.class);
	}

	@Override
	public List<WebElement> elMessagesOk() {
		return messagesByClass("message ok");
	}

	@Override
	public List<WebElement> elMessagesError() {
		return messagesByClass("message error");
	}
	
	private List<WebElement> messagesByClass(String clazz) {
		Builder<WebElement> builder = ImmutableList.builder();
		for (WebElement message : messages) {
			if (clazz.equals(message.getAttribute("class"))) {
				builder.add(message);
			}
		}
		return builder.build();
	}
	
	public CalendarNavBarWidget calendarNavBarWidget() {
		retrieveCalendarPageWidgets();
		return calendarNavBarWidget;
	}
	
	public CalendarViewWidget calendarViewWidget() {
		retrieveCalendarPageWidgets();
		return calendarViewWidget;
	}
	
	public CalendarCalRangeWidget calendarCalRangeWidget() {
		retrieveCalendarPageWidgets();
		return calendarCalRangeWidget;
	}

	private void retrieveCalendarPageWidgets() {
		if (calendarNavBarWidget == null) {
			calendarNavBarWidget = new CalendarNavBarWidget(this, driver.findElement(By.id("calendarNavBar")));
		}
		if (calendarViewWidget == null) {
			calendarViewWidget = new CalendarViewWidget(this, driver.findElement(new ByCssSelector(".LF.NM")));
		}
		if (calendarCalRangeWidget == null) {
			calendarCalRangeWidget = new CalendarCalRangeWidget(this, driver.findElement(By.id("calendarCalRange")));
		}
	}
}
