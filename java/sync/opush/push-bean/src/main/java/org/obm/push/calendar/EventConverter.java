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

package org.obm.push.calendar;

import org.obm.push.MSEventToObmEventConverter;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Convert events between OBM-Sync object model & Microsoft object model
 */
@Singleton
public class EventConverter {
	
	private final MSEventToObmEventConverter msEventConverter;

	@Inject
	@VisibleForTesting EventConverter(MSEventToObmEventConverter msEventConverter) {
		this.msEventConverter = msEventConverter;
	}

	public static ParticipationState getParticipationState(ParticipationState oldParticipationState, AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
		case RESPONSE_UNKNOWN:
		case TENTATIVE:
			return ParticipationState.NEEDSACTION;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}
	
	public static boolean isInternalEvent(Event event, boolean defaultValue){
		return event != null ? event.isInternalEvent() : defaultValue;
	}

	public Event convert(BackendSession bs, Event oldEvent, MSEvent data, boolean isInternal) {
		return msEventConverter.convert(bs, oldEvent, data, isInternal);
	}
	
}
