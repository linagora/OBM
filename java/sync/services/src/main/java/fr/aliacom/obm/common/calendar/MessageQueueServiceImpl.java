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
package fr.aliacom.obm.common.calendar;

import javax.jms.JMSException;

import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;

@Singleton
public class MessageQueueServiceImpl implements MessageQueueService {

	private final Ical4jHelper ical4jHelper;
	private final UserService userService;
	private final ICalendarFactory calendarFactory;
	private final Producer producer;

	@Inject
	private MessageQueueServiceImpl(UserService userService, Ical4jHelper ical4jHelper, ICalendarFactory calendarFactory,
			Producer producer) {
		
		this.userService = userService;
		this.ical4jHelper = ical4jHelper;
		this.calendarFactory = calendarFactory;
		this.producer = producer;
	}
	
	@Override
	public void writeIcsInvitationRequest(AccessToken token, Event current) {
		Ical4jUser buildIcal4jUser = getIcal4jUser(token);
		String ics = ical4jHelper.buildIcsInvitationRequest(buildIcal4jUser, current, token);		
		writeIcs(ics);
	}

	@Override
	public void writeIcsInvitationCancel(AccessToken token, Event event) {
		Ical4jUser buildIcal4jUser = getIcal4jUser(token);
		String ics = ical4jHelper.buildIcsInvitationCancel(buildIcal4jUser, event, token);
		writeIcs(ics);
	}
	
	@Override
	public void writeIcsInvitationReply(AccessToken token, Event event, ObmUser calendarOwner) {
		Ical4jUser replyIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(calendarOwner);
		String ics = ical4jHelper.buildIcsInvitationReply(event, replyIcal4jUser, token);
		writeIcs(ics);
	}

	private Ical4jUser getIcal4jUser(AccessToken token) {
		ObmUser user = userService.getUserFromAccessToken(token);
		Ical4jUser buildIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(user);
		return buildIcal4jUser;
	}

	private void writeIcs(String... ics)  {
		try {
			for (String s: ics) {
				producer.write(s);	
			}
		} catch (JMSException e) {
			throw new NotificationException(e);
		}
	}

}
