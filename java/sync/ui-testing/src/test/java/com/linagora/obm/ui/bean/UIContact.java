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
public class UIContact {
	
	public static Builder builder() {
		return new Builder();
	}

	public static UIContact contact() {
		return builder().firstName("firstnamea").lastName("lastnamea").companyField("linagora").mailokField(true).newsletterField(false).build();		
	}

	public static UIContact emptyFields() {
		return builder().build();		
	}
	
	public static class Builder {
		
		private String firstName;
		private String lastName;
		private String companyField;
		private boolean mailokField;
		private boolean newsletterField;

		private Builder() {
			super();
		}

		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		
		public Builder companyField(String companyField) {
			this.companyField = companyField;
			return this;
		}
		
		public Builder mailokField(boolean mailokField) {
			this.mailokField = mailokField;
			return this;
		}
		
		public Builder newsletterField(boolean newsletterField) {
			this.newsletterField = newsletterField;
			return this;
		}
		
		public UIContact build() {
			return new UIContact(firstName, lastName, companyField, mailokField, newsletterField);
		}
	}

	private final String firstName;
	private final String lastName;
	private final String companyField;
	private final boolean mailokField;
	private final boolean newsletterField;
	
}
