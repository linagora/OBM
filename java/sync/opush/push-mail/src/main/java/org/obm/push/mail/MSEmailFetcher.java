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

import java.util.Collection;
import java.util.List;

import org.obm.icalendar.ICalendar;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewBuildException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.mail.conversation.EmailView;
import org.obm.push.mail.transformer.Transformer.TransformersFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MSEmailFetcher {

	private static final Logger logger = LoggerFactory.getLogger(MSEmailFetcher.class);

	private final TransformersFactory transformersFactory;
	private final MailboxService mailboxService;
	private final MailViewToMSEmailConverter msEmailConverter;

	@Inject
	@VisibleForTesting MSEmailFetcher(MailboxService mailboxService, TransformersFactory transformersFactory, 
			MailViewToMSEmailConverter msEmailConverter) {
		this.mailboxService = mailboxService;
		this.transformersFactory = transformersFactory;
		this.msEmailConverter = msEmailConverter;
	}

	public List<UidMSEmail> fetch(UserDataRequest udr, Integer collectionId, String collectionPath,
			Collection<Long> uids, List<BodyPreference> bodyPreferences) throws EmailViewPartsFetcherException, DaoException {
		
		List<UidMSEmail> msEmails  = Lists.newLinkedList();
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = 
				new EmailViewPartsFetcherImpl(transformersFactory, mailboxService, bodyPreferences, udr, collectionPath, collectionId);
		
		for (Long uid: uids) {
			try {
				EmailView emailView = emailViewPartsFetcherImpl.fetch(uid, new AnyMatchBodyPreferencePolicy());
				msEmails.add(msEmailConverter.convert(emailView, udr));
			} catch (EmailViewBuildException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return msEmails;
	}

	public ICalendar fetchInvitation(UserDataRequest udr, Integer collectionId, String collectionPath, Long uid) throws EmailViewPartsFetcherException, DaoException {
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = 
				new EmailViewPartsFetcherImpl(transformersFactory, mailboxService, null, udr, collectionPath, collectionId);
		
		return emailViewPartsFetcherImpl.fetchInvitation(uid);
	}
}
