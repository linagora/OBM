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
package com.linagora.obm.ui.bean;

import lombok.Data;

@Data
public class UIEvent {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private String title;
		private String location;
		private boolean force;
		private boolean privacy;
		private boolean allday;
		private String dateBegin;
		private Integer hourBegin;
		private Integer minBegin;
		private String dateEnd;
		private Integer hourEnd;
		private Integer minEnd;
		private boolean isBusy;
		private boolean isFree;
		private String tagLabel;
		private String userSearch;
		private boolean showUserCalendar;
		private String description;
		private boolean attendeesNotification;
		
		
		private Builder() {
			super();
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder location(String location) {
			this.location = location;
			return this;
		}
		
		public Builder force(boolean force) {
			this.force = force;
			return this;
		}
				
		public Builder privacy(boolean privacy) {
			this.privacy = privacy;
			return this;
		}
		
		public Builder allday(boolean allday) {
			this.allday = allday;
			return this;
		}
		
		public Builder dateBegin(String dateBegin) {
			this.dateBegin = dateBegin;
			return this;
		}
		
		public Builder hourBegin(Integer hourBegin) {
			this.hourBegin = hourBegin;
			return this;
		}
		
		public Builder minBegin(Integer minBegin) {
			this.minBegin = minBegin;
			return this;
		}
		
		public Builder dateEnd(String dateEnd) {
			this.dateEnd = dateEnd;
			return this;
		}
		
		public Builder hourEnd(Integer hourEnd) {
			this.hourEnd = hourEnd;
			return this;
		}
				
		public Builder minEnd(Integer minEnd) {
			this.minEnd = minEnd;
			return this;
		}
		
		public Builder isBusy(boolean isBusy) {
			this.isBusy = isBusy;
			return this;
		}
		
		public Builder isFree(boolean isFree) {
			this.isFree = isFree;
			return this;
		}
				
		public Builder tagLabel(String tagLabel) {
			this.tagLabel = tagLabel;
			return this;
		}
		
		public Builder userSearch(String userSearch) {
			this.userSearch = userSearch;
			return this;
		}
		
		public Builder showUserCalendar(boolean showUserCalendar) {
			this.showUserCalendar = showUserCalendar;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder attendeesNotification(boolean attendeesNotification) {
			this.attendeesNotification=attendeesNotification;
			return this;
		}
		
		
		public UIEvent build() {
			return new UIEvent(
					title, location, force, privacy, allday,
					dateBegin, hourBegin, minBegin, dateEnd,
					hourEnd, minEnd, isBusy, isFree,
					tagLabel, userSearch,  showUserCalendar,
					description, attendeesNotification);
		}
	}

	private final String title;
	private final String location;
	private final boolean force;
	private final boolean privacy;
	private final boolean allday;
	private final String dateBegin;
	private final Integer hourBegin;
	private final Integer minBegin;
	private final String dateEnd;
	private final Integer hourEnd;
	private final Integer minEnd;
	private final boolean isBusy;
	private final boolean isFree;
	private final String tagLabel;
	private final String userSearch;
	private final boolean showUserCalendar;
	private final String description;
	private final boolean attendeesNotification;
	
		
}
