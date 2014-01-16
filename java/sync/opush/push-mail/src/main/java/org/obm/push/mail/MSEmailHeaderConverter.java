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
package org.obm.push.mail;

import java.util.ArrayList;
import java.util.List;

import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.mail.bean.Address;
import org.obm.push.mail.bean.Envelope;
import org.obm.push.utils.UserEmailParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MSEmailHeaderConverter {
	
	private final static Logger logger = LoggerFactory.getLogger(MSEmailHeaderConverter.class);
	
	private final UserEmailParserUtils emailParserUtils;

	@Inject
	@VisibleForTesting MSEmailHeaderConverter(UserEmailParserUtils emailParserUtils) {
		this.emailParserUtils = emailParserUtils;
	}
	
	public MSEmailHeader convertToMSEmailHeader(Envelope envelope) {
		Preconditions.checkNotNull(envelope);
		return MSEmailHeader.builder()
		.from(toMSAddresses(envelope.getFrom()))
		.replyTo(toMSAddresses(envelope.getReplyTo()))
		.to(toMSAddresses(envelope.getTo()))
		.cc(toMSAddresses(envelope.getCc()))
		.subject(envelope.getSubject())
		.date(envelope.getDate())
		.build();
	}

	private List<MSAddress> toMSAddresses(List<Address> addresses) {
		List<MSAddress> msAddresses = new ArrayList<MSAddress>();
		if (addresses != null) {
			for (Address address: addresses) {
				MSAddress msAddress = toMSAddress(address);
				if (msAddress != null) {
					msAddresses.add(msAddress);
				}
			}
		}
		return msAddresses;
	}

	private MSAddress toMSAddress(Address address) {
		if (address != null) {
			try {
				if (emailParserUtils.isAddress(address.getMail())) {
					return new MSAddress(address.getDisplayName(), address.getMail());
				}
			} catch (IllegalArgumentException e) {
				logger.warn(
						String.format("[%s] is an invalid email format.", address.getMail()));
			}
		}
		return null;
	}
}
