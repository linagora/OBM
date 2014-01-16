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
package com.linagora.obm.ui.scenario.event;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.inject.Inject;
import com.linagora.obm.ui.bean.UIDomain;
import com.linagora.obm.ui.bean.UIEvent;
import com.linagora.obm.ui.bean.UIUser;
import com.linagora.obm.ui.page.CalendarPage;
import com.linagora.obm.ui.page.CreateCalendarPage;
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
	
	private CalendarPage processedCalendarPage;
	private CreateCalendarPage createCalendarPage;
	
	private CalendarPage calendarPage;
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
		calendarPage = pageFactory.create(driver, CalendarPage.class);
		calendarPage.open();
		
		createCalendarPage = calendarPage.createCalendarPage();
	}
	
	@When("I create a meeting \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void createMeeting(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.build());
	}
	
	@When("I create a meeting without title at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void createMeetingWithoutTitle(int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEventThrowingAlert(UIEvent.builder()
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.build());
	}
	
	@When("I create a meeting \"([^\"]*)\" with wrong date at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void createMeetingWithWrongDate(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEventFails(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.build());
	}

	@When("^I create a meeting \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+) every day$")
	public void createDailyMeeting(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.daily(true)
				.build());
	}

	@When("^I create a meeting \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+) every week$")
	public void createWeeklyMeeting(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.weekly(true)
				.build());
	}

	@When("^I create a meeting \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+) every month$")
	public void createMonthlyMeeting(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.monthlybydate(true)
				.build());
	}
	
	@When("^I create a meeting \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+) every year$")
	public void createYearlyMeeting(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.hourBegin(beginHour)
				.minBegin(beginMin)
				.dateEnd(date)
				.hourEnd(endHour)
				.minEnd(endMin)
				.yearly(true)
				.build());
	}
	
	@When("^I create an allday event \"([^\"]*)\" at (\\d+)/(\\d+)/(\\d+)$")
	public void createAlldayMeeting(String title, int day, int month, int year) {
		Date date = new DateTime().withDate(year, month, day).toDate();
		processedCalendarPage = createCalendarPage.createEvent(UIEvent.builder()
				.title(title)
				.dateBegin(date)
				.dateEnd(date)
				.allday(true)
				.build());
	}

	@Then("I am informed that the event has been inserted")
	public void eventHasBeenInserted() {
		List<WebElement> okMessages = processedCalendarPage.elMessagesOk();
		assertThat(okMessages).hasSize(1);
		assertThat(okMessages.get(0).getText()).isEqualTo("Evénement: Insertion réussie -\nConsulter");
	}
	
	@And("^event \"([^\"]*)\" is inserted in the calendar from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void eventIsInCalendar(String title, int beginHour, int beginMin, int endHour, int endMin) {
		DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
		String startTime = timeFormatter.print(new DateTime().withTime(beginHour, beginMin, 0, 0));
		String endTime = timeFormatter.print(new DateTime().withTime(endHour, endMin, 0, 0));
		
		WebElement divElement = assertEventByTitleIsInCalendar(title);
		
		WebElement hrefElement = divElement.findElement(new ByCssSelector("a"));
		assertThat(hrefElement.getText()).isEqualTo(startTime + " - " + endTime);
	}
	
	@Then("^a popup with the message \"([^\"]*)\" is displayed$")
	public void popupIsFired(String message) {
		Alert alert = driver.switchTo().alert();
		assertThat(alert.getText()).isEqualTo(message);
		alert.accept();
	}
	
	@And("^the red message \"([^\"]*)\" is displayed$")
	public void redMessageDisplayed(String message) {
		List<WebElement> errorMessages = processedCalendarPage.elMessagesError();
		assertThat(errorMessages).hasSize(1);
		assertThat(errorMessages.get(0).getText()).isEqualTo(message);
	}

	@And("^event \"([^\"]*)\" is inserted in the calendar$")
	public void eventIsInCalendar(String title) {
		assertEventByTitleIsInCalendar(title);
	}

	private WebElement assertEventByTitleIsInCalendar(String title) {
		WebElement divElement = processedCalendarPage.getDivByTitle(title);
		assertThat(divElement).isNotNull();
		return divElement;
	}
	
	@And("^event \"([^\"]*)\" appears every day, first is (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void eventAppearsEveryDay(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		
		DateTime dateTime = dateTime(day, month, year, beginHour, beginMin);
		String expectedEventDatesTitle = expectedEventDatesTitle(beginHour, beginMin, endHour, endMin);
		
		List<WebElement> divElements = sortedEvents(title);
		assertThat(divElements).isNotEmpty();
		for (WebElement divElement : divElements) {
			assertThat(divElement.getAttribute("id")).endsWith(String.valueOf(dateTime.getMillis() / 1000));
			
			WebElement hrefElement = divElement.findElement(new ByCssSelector("a"));
			assertThat(hrefElement.getText()).isEqualTo(expectedEventDatesTitle);
			
			dateTime = dateTime.plusDays(1);
		}
	}

	private List<WebElement> sortedEvents(String title) {
		List<WebElement> divElements = driver.findElements(new ByCssSelector("div[title='" + title + " ']"));

		Collections.sort(divElements, new Comparator<WebElement> () {

			@Override
			public int compare(WebElement we1, WebElement we2) {
				return we1.getAttribute("id").compareTo(we2.getAttribute("id"));
			}
			
		});
		return divElements;
	}

	@Then("^event \"([^\"]*)\" appears every week on saturday, first is (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void eventAppearsEveryWeek(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		
		DateTime dateTime = dateTime(day, month, year, beginHour, beginMin);
		String expectedEventDatesTitle = expectedEventDatesTitle(beginHour, beginMin, endHour, endMin);

		for (int numberOfChecks = 0; numberOfChecks < 5; numberOfChecks++) {
			WebElement divElement = processedCalendarPage.getDivByTitle(title);
			assertThat(divElement.getAttribute("id")).endsWith(String.valueOf(dateTime.getMillis() / 1000));
			
			WebElement hrefElement = divElement.findElement(new ByCssSelector("a"));
			assertThat(hrefElement.getText()).isEqualTo(expectedEventDatesTitle);
			
			dateTime = dateTime.plusWeeks(1);
			processedCalendarPage.calendarNavBarWidget().nextPage();
		}
	}

	@Then("^event \"([^\"]*)\" appears every month at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void eventAppearsEveryMonth(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		
		DateTime dateTimeDayOfMonth = dateTime(day, month, year, beginHour, beginMin);
		DateTime timelessDayOfMonth = timelessDateTime(day, month, year);
		String expectedEventDatesTitle = expectedEventDatesTitle(beginHour, beginMin, endHour, endMin);

		for (int numberOfChecksi = 0; numberOfChecksi < 5; numberOfChecksi++) {
			WebElement divElement = processedCalendarPage.getDivByTitle(title);
			assertThat(divElement.getAttribute("id")).endsWith(String.valueOf(dateTimeDayOfMonth.getMillis() / 1000));
			
			WebElement hrefElement = divElement.findElement(new ByCssSelector("a"));
			assertThat(hrefElement.getText()).isEqualTo(expectedEventDatesTitle);
			
			boolean found = false;
			dateTimeDayOfMonth = dateTimeDayOfMonth.plusMonths(1);
			timelessDayOfMonth = timelessDayOfMonth.plusMonths(1);
			for (int weekIterator = 0; weekIterator < 5; weekIterator++) {
				processedCalendarPage.calendarNavBarWidget().nextPage();
				found = isDayOfMonthInPrintedWeek(timelessDayOfMonth);
				if (found) {
					break;
				}
			}
			
			assertThat(found).isTrue();
		}
	}

	private DateTime dateTime(int day, int month, int year, int hour, int min) {
		return new DateTime().withDate(year, month, day)
				.withTime(hour, min, 0, 0);
	}
	
	private boolean isDayOfMonthInPrintedWeek(DateTime dayOfMonth) {
		List<WebElement> dayElements = driver.findElements(By.className("dayLabel"));
		for (WebElement dayElement : dayElements) {
			WebElement href = dayElement.findElement(By.tagName("a"));
			if (href.getAttribute("onclick").contains(String.valueOf(dayOfMonth.getMillis() / 1000))) {
				return true;
			}
		}
		return false;
	}

	private String expectedEventDatesTitle(int beginHour, int beginMin, int endHour, int endMin) {
		DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
		return timeFormatter.print(new DateTime().withTime(beginHour, beginMin, 0, 0)) 
				+ " - " + timeFormatter.print(new DateTime().withTime(endHour, endMin, 0, 0));
	}



	@And("^event \"([^\"]*)\" appears every year at (\\d+)/(\\d+)/(\\d+) from (\\d+):(\\d+) to (\\d+):(\\d+)$")
	public void eventAppearsEveryYear(String title, int day, int month, int year, int beginHour, int beginMin,
			int endHour, int endMin) {
		
		processedCalendarPage.calendarViewWidget().listView();
		processedCalendarPage.calendarCalRangeWidget().monthlyEvents();
		
		DateTime dateTime = new DateTime().withDate(year, month, day);
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		String expectedEventDatesTitle = expectedEventDatesTitle(beginHour, beginMin, endHour, endMin);
		
		for (int numberOfChecks = 0; numberOfChecks < 3; numberOfChecks++) {
			boolean found = false;
			
			dateTime = dateTime.plusYears(1);
			for (int monthIterator = 0; monthIterator < 12; monthIterator++) {
				processedCalendarPage.calendarNavBarWidget().nextPage();
				found = isYearlyEventInPrintedMonth(title, dateTimeFormatter.print(dateTime), expectedEventDatesTitle);
				if (found) {
					break;
				}
			}
			
			assertThat(found).isTrue();
		}
	}

	private boolean isYearlyEventInPrintedMonth(String title, String expectedDate, String expectedEventDatesTitle) {
		boolean found = false;
		WebElement eventsElement = driver.findElement(new ByCssSelector(".spreadSheet.eventList"));
		List<WebElement> divElements = eventsElement.findElements(new ByCssSelector("div"));
		for (WebElement divElement : divElements) {
			if (divElement.getAttribute("id").contains(expectedDate)) {
				List<WebElement> spanElements = divElement.findElements(new ByCssSelector("span"));
				for (WebElement spanElement : spanElements) {
					if (spanElement.getText().startsWith(expectedEventDatesTitle)) {
						WebElement imageElement = divElement.findElement(new ByCssSelector("strong"));
						assertThat(imageElement.getText()).isEqualTo(title);
						found = true;
						break;
					}
				}
			}
			if (found) {
				break;
			}
		}
		return found;
	}


	@And("^allday event \"([^\"]*)\" is inserted in the calendar at (\\d+)/(\\d+)/(\\d+)$")
	public void alldayEventIsInCalendar(String title, int day, int month, int year) {
		DateTime dateTime = timelessDateTime(day, month, year);
		String expectedPartialEventId = String.valueOf(dateTime.getMillis() / 1000);
		
		WebElement divElement = processedCalendarPage.getDivByTitle(title);
		assertThat(divElement.getAttribute("id")).endsWith(expectedPartialEventId);
	}

	private DateTime timelessDateTime(int day, int month, int year) {
		return new DateTime().withDate(year, month, day)
				.withTime(0, 0, 0, 0);
	}
}
