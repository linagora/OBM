/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ExcludedUser;
import org.obm.imap.archive.beans.Mailing;
import org.obm.sync.base.EmailAddress;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

public class DomainConfigurationDto {

	public static DomainConfigurationDto from(DomainConfiguration configuration) {
		DomainConfigurationDto dto = new DomainConfigurationDto();
		dto.domainId = configuration.getDomainId().getUUID();
		dto.enabled = configuration.isEnabled();
		dto.repeatKind = configuration.getRepeatKind() != null ? configuration.getRepeatKind().name() : null;
		dto.dayOfWeek = from(configuration.getDayOfWeek());
		dto.dayOfMonth = configuration.getDayOfMonth() != null ? configuration.getDayOfMonth().getDayIndex() : null;
		dto.dayOfYear = configuration.getDayOfYear() != null ? configuration.getDayOfYear().getDayOfYear() : null;
		dto.hour = configuration.getHour();
		dto.minute = configuration.getMinute();
		dto.excludedFolder = configuration.getExcludedFolder();
		dto.excludedUserIdToLoginMap = toMap(configuration.getExcludedUsers());
		dto.mailingEmails = toStrings(configuration.getMailing());
		return dto;
	}

	private static Integer from(DayOfWeek dayOfWeek) {
		if (dayOfWeek == null) {
			return null;
		}
		switch (dayOfWeek) {
		case MONDAY:
			return 1;
		case TUESDAY:
			return 2;
		case WEDNESDAY:
			return 3;
		case THURSDAY:
			return 4;
		case FRIDAY:
			return 5;
		case SATURDAY:
			return 6;
		case SUNDAY:
			return 7;
		}
		throw new IllegalArgumentException(dayOfWeek.name() + " can't be converted to Integer");
	}
	
	private static Map<String, String> toMap(List<ExcludedUser> excludedUsers) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (ExcludedUser excludedUser : excludedUsers) {
			builder.put(excludedUser.serializeId(), excludedUser.getLogin());
		}
		return builder.build();
	}
	
	private static List<String> toStrings(Mailing mailing) {
		return FluentIterable.from(mailing.getEmailAddresses())
				.transform(new Function<EmailAddress, String>() {

					@Override
					public String apply(EmailAddress emailAddress) {
						return emailAddress.get();
					}
				}).toList();
	}

	public UUID domainId;
	public Boolean enabled;
	public String repeatKind;
	public Integer dayOfWeek;
	public Integer dayOfMonth;
	public Integer dayOfYear;
	public Integer hour;
	public Integer minute;
	public String excludedFolder;
	public Map<String, String> excludedUserIdToLoginMap;
	public List<String> mailingEmails;
	
}
