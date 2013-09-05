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
package org.obm.provisioning.bean;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;


public enum UserJsonFields {
	ID("id"), LOGIN("login"), LASTNAME("lastname"), PROFILE("profile"), FIRSTNAME("firstname"), COMMONNAME("commonname"),
	PASSWORD("password"), KIND("kind"), TITLE("title"), DESCRIPTION("description"), COMPANY("company"), SERVICE("service"),
	DIRECTION("direction"), ADDRESSES("addresses"), TOWN("town"), ZIPCODE("zipcode"), BUSINESS_ZIPCODE("business_zipcode"),
	COUNTRY("country"), PHONES("phones"), MOBILE("mobile"), FAXES("faxes"), MAIL_QUOTA("mail_quota"), MAIL_SERVER("mail_server"),
	ARCHIVED("archived"), HIDDEN("hidden"), MAILS("mails"), TIMECREATE("timecreate"), TIMEUPDATE("timeupdate"), GROUPS("groups");
	
	private String value;
	
	private UserJsonFields(String right) {
		this.value = right;
	}
	
	public String asSpecificationValue() {
		return value;
	}
	
	public static UserJsonFields fromSpecificationValue(String value) {
		for (UserJsonFields right: values()) {
			if (right.asSpecificationValue().equals(value)) {
				return right;
			}
		}
		throw new IllegalArgumentException();
	}
	
	public static Set<UserJsonFields> fields;
	
	static {
		Builder<UserJsonFields> builder = ImmutableSet.builder();
		for (UserJsonFields field : values()) {
			builder.add(field);
		}
		fields = builder.build();
	}
}
