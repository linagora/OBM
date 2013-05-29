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
package com.linagora.obm.ui.page.widget;

import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebElement;

import com.linagora.obm.ui.page.CalendarPage;

public class CalendarNavBarWidget extends CalendarWidget {

	private final WebElement calendarNavBarElement;
	private WebElement nextPageElement;

	public CalendarNavBarWidget(CalendarPage calendarPage, WebElement calendarNavBarElement) {
		super(calendarPage);
		this.calendarNavBarElement = calendarNavBarElement;
	}
	
	public void previousPage() {
		// TODO: not in used, to implement when needed
	}

	public void nextPage() {
		clickAndWait(retrieveNextPageElement());
		retrieveNextPageElement();
	}

	private WebElement retrieveNextPageElement() {
		if (nextPageElement != null) {
			return nextPageElement;
		}
		
		if (parentPage.calendarViewWidget().isInAgendaView()) {
			return navigateToNextPageOnCalendarView();
		} else {
			return navigateToNextPageOnOtherViews();
		}
	}
	
	private WebElement navigateToNextPageOnCalendarView() {
		List<WebElement> buttonElements = calendarNavBarElement.findElements(new ByCssSelector("a"));
		for (WebElement webElement : buttonElements) {
			if (webElement.getAttribute("onclick").contains("showNext")) {
				return nextPageElement = webElement;
			}
		}
		throw new NoSuchElementException("nextPageElementOnCalendarView");
	}
	
	private WebElement navigateToNextPageOnOtherViews() {
		List<WebElement> buttonElements = calendarNavBarElement.findElements(new ByCssSelector("img"));
		for (WebElement webElement : buttonElements) {
			if (webElement.getAttribute("src").contains("next.png")) {
				return nextPageElement = webElement;
			}
		}
		throw new NoSuchElementException("nextPageElementOnOtherViews");
	}
	
	public void goToday() {
		// TODO: not in used, to implement when needed
	}
	
	public String getNavBarLabel() {
		// TODO: not in used, to implement when needed
		return null;
	}
	
	public void actualize() {
		// TODO: not in used, to implement when needed
	}
}
