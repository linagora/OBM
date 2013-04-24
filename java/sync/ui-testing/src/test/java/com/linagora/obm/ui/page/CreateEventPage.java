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

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Predicate;
import com.linagora.obm.ui.bean.UIContact;
import com.linagora.obm.ui.bean.UIEvent;

public class CreateEventPage extends CalendarPage {
	
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
	private WebElement new_event_form;

	
	public CreateEventPage(WebDriver driver) {
		super(driver);
	}
	
	@Override
	public void open() {
		driver.get(mapping.lookup(CreateEventPage.class).toExternalForm());
	}

	public CalendarPage createEvent(UIEvent eventToCreate) {
		doCreateEvent(eventToCreate);
		
		new FluentWait<WebDriver>(driver).until(new Predicate<WebDriver>() {
			@Override
			public boolean apply(WebDriver input) {
				return !new_event_form.isDisplayed() 
					;
			}
		});
		
		return pageFactory.create(driver, CalendarPage.class);
	}
	

	private void doCreateEvent(UIEvent eventToCreate) {
		tf_title.sendKeys(eventToCreate.getTitle());
		tf_location.sendKeys(eventToCreate.getLocation());
		clickCheckbox(cba_force, eventToCreate.isForce());
		clickCheckbox(cba_privacy, eventToCreate.isPrivacy());
		clickCheckbox(cba_all_day, eventToCreate.isAllday());
		tf_date_begin.sendKeys(eventToCreate.getDateBegin().toString());
		sel_time_begin.sendKeys(eventToCreate.getHourBegin().toString());
		sel_min_begin.sendKeys(eventToCreate.getMinBegin().toString());
		tf_date_end.sendKeys(eventToCreate.getDateEnd().toString());
		sel_time_end.sendKeys(eventToCreate.getHourEnd().toString());
		sel_min_end.sendKeys(eventToCreate.getMinEnd().toString());
		
		// @TODO: the 2 following lines are linked
		clickCheckbox(rd_opacity_busy, eventToCreate.isBusy());
		clickCheckbox(rd_opacity_free, eventToCreate.isFree());
		
		tf_tag_label.sendKeys(eventToCreate.getTagLabel());
		userSearch.sendKeys(eventToCreate.getUserSearch());
		clickCheckbox(cba_show_user_calendar, eventToCreate.isShowUserCalendar());
		ta_description.sendKeys(eventToCreate.getDescription());
		clickCheckbox(cba_attendees_notification, eventToCreate.isAttendeesNotification());
		
		
		new_event_form.submit();
	}

}
