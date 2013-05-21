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
package com.linagora.obm.ui.scenario.event;

import static org.fest.assertions.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIContact;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIEvent;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.page.CalendarPage;
import com.linagora.obm.ui.page.ContactPage;
import com.linagora.obm.ui.page.CreateContactPage;
import com.linagora.obm.ui.page.CreateEventPage;
import com.linagora.obm.ui.page.LoginPage;
import com.linagora.obm.ui.page.PageFactory;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class EventStepdefs {

	@Inject PageFactory pageFactory;
	@Inject WebDriver driver;
	
	private UIUser uiUser;
	private UIDomain uiDomain;
	
	private CalendarPage calendarPage;
	private CreateEventPage createCalendarPage;
	
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

	@Given("on calendar page")
	public void openCalendarPage() {
		CalendarPage calendarPage = pageFactory.create(driver, CalendarPage.class);
		calendarPage.open();
		
		createCalendarPage = calendarPage.createNewEventPage();
	}
	
	@When("I create a meeting $title at $day from $beginHour:$beginMin to $endHour:$endMin")
	public void createMeeting(String title, String day, Integer beginHour, Integer beginMin,
			Integer endHour, Integer endMin) {
		createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(day)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(day)
				.hourEnd(endHour)
				.minEnd(endMin)
				.build());
	}
	
	@Then("I am redirected to my calendar")
	public void redirectedToCalendar() {
		CalendarPage calendarPage = pageFactory.create(driver, CalendarPage.class);
		//assertThat(calendarPage.
	}
	
	@And("I am informed that the event has been inserted")
	public void eventHasBeenInserted() {
		
	}
	
	@And ("event $title is inserted in the calendar")
	public void eventIsInCalendar(String title) {
		
	}
}
