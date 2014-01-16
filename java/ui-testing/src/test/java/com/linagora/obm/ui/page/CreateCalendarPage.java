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

import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

import com.google.common.base.Predicate;
import com.linagora.obm.ui.bean.UIEvent;

public class CreateCalendarPage extends CalendarPage {
	
	private WebElement tf_title;
	private WebElement tf_location;
	private WebElement cba_force;
	private WebElement cba_privacy;
	private WebElement cba_all_day;
	private WebElement tf_date_begin;
	private WebElement sel_time_begin;
	private WebElement sel_min_begin;
	private WebElement tf_date_end; 
	private WebElement sel_time_end;
	private WebElement sel_min_end;
	private WebElement rd_opacity_busy;
	private WebElement rd_opacity_free;
	private WebElement tf_tag_label;
	private WebElement userSearch;
	private WebElement cba_show_user_calendar;
	private WebElement ta_description;
	private WebElement cba_attendees_notification;
	private WebElement sel_repeat_kind;
	private WebElement cba_repeatday_1;
	private WebElement cba_repeatday_2;
	private WebElement cba_repeatday_3;
	private WebElement cba_repeatday_4;
	private WebElement cba_repeatday_5;
	private WebElement cba_repeatday_6;
	private WebElement cba_repeatday_7;
	private WebElement new_event_form;

	
	public CreateCalendarPage(WebDriver driver) {
		super(driver);
	}
	
	@Override
	public void open() {
		driver.get(mapping.lookup(CreateCalendarPage.class).toExternalForm());
	}

	public CalendarPage createEvent(UIEvent eventToCreate) {
		doCreateEvent(eventToCreate);
		
		new FluentWait<WebDriver>(driver).until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return calendarHeader.isDisplayed();
			}
		});
		
		return pageFactory.create(driver, CalendarPage.class);
	}

	public CalendarPage createEventFails(UIEvent eventToCreate) {
		doCreateEvent(eventToCreate);
		
		new FluentWait<WebDriver>(driver).until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return !elMessagesError().isEmpty();
			}
		});
		
		return pageFactory.create(driver, CalendarPage.class);
	}

	public CalendarPage createEventThrowingAlert(UIEvent eventToCreate) {
		doCreateEvent(eventToCreate);
		
		new FluentWait<WebDriver>(driver).until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				Alert alert = driver.switchTo().alert();
				if (alert != null) {
					return true;
				}
				return false;
			}
		});
		
		return pageFactory.create(driver, CalendarPage.class);
	}

	private void doCreateEvent(UIEvent eventToCreate) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		
		tf_title.sendKeys(eventToCreate.getTitle());
		tf_location.sendKeys(eventToCreate.getLocation());
		clickCheckbox(cba_force, eventToCreate.isForce());
		clickCheckbox(cba_privacy, eventToCreate.isPrivacy());
		clickCheckbox(cba_all_day, eventToCreate.isAllday());
		tf_date_begin.clear();
		tf_date_begin.sendKeys(format.format(eventToCreate.getDateBegin()));
		if (eventToCreate.getHourBegin() != null) {
			sel_time_begin.sendKeys(formatNumberStartingWithZeros(eventToCreate.getHourBegin()));
		}
		if (eventToCreate.getMinBegin() != null) {
			sel_min_begin.sendKeys(formatNumberStartingWithZeros(eventToCreate.getMinBegin()));
		}
		tf_date_end.clear();
		tf_date_end.sendKeys(format.format(eventToCreate.getDateEnd()));
		if (eventToCreate.getHourEnd() != null) {
			sel_time_end.sendKeys(formatNumberStartingWithZeros(eventToCreate.getHourEnd()));
		}
		if (eventToCreate.getMinEnd() != null) {
			sel_min_end.sendKeys(formatNumberStartingWithZeros(eventToCreate.getMinEnd()));
		}
		
		clickCheckbox(rd_opacity_busy, eventToCreate.isBusy());
		clickCheckbox(rd_opacity_free, eventToCreate.isFree());
		
		tf_tag_label.sendKeys(eventToCreate.getTagLabel());
		userSearch.sendKeys(eventToCreate.getUserSearch());
		clickCheckbox(cba_show_user_calendar, eventToCreate.isShowUserCalendar());
		ta_description.sendKeys(eventToCreate.getDescription());
		clickCheckbox(cba_attendees_notification, eventToCreate.isAttendeesNotification());
		
		setRecurrence(eventToCreate);
		
		new_event_form.submit();
	}

	private String formatNumberStartingWithZeros(int number) {
		return String.format("%02d", number);
	}

	private void setRecurrence(UIEvent eventToCreate) {
		Select select = new Select(sel_repeat_kind);
		if (!eventToCreate.isRecurent()) {
			select.selectByValue("none");
		}
		if (eventToCreate.isDaily()) {
			select.selectByValue("daily");
		}
		if (eventToCreate.isWeekly()) {
			select.selectByValue("weekly");
			selectDay(eventToCreate);
		}
		if (eventToCreate.isMonthlybydate()) {
			select.selectByValue("monthlybydate");
		}
		if (eventToCreate.isMonthlybyday()) {
			select.selectByValue("monthlybyday");
		}
		if (eventToCreate.isYearly()) {
			select.selectByValue("yearly");
		}
	}

	private void selectDay(UIEvent eventToCreate) {
		DateTime dateTime = new DateTime(eventToCreate.getDateBegin());
		switch(dateTime.get(DateTimeFieldType.dayOfWeek())) {
			case 1:
				cba_repeatday_1.click();
				break;
			case 2:
				cba_repeatday_2.click();
				break;
			case 3:
				cba_repeatday_3.click();
				break;
			case 4:
				cba_repeatday_4.click();
				break;
			case 5:
				cba_repeatday_5.click();
				break;
			case 6:
				cba_repeatday_6.click();
				break;
			case 7:
				cba_repeatday_7.click();
				break;
		}
	}
}
