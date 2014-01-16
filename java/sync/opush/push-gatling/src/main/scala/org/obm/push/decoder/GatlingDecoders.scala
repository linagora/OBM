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
package org.obm.push.decoder

import org.obm.push.protocol.data.CalendarDecoder
import org.obm.push.protocol.data.ms.MSEmailDecoder
import org.obm.push.protocol.data.ms.MSMeetingRequestDecoder
import org.obm.push.protocol.data.DecoderFactory
import com.google.inject.Provider
import org.obm.push.protocol.data.ContactDecoder
import org.obm.push.protocol.data.TaskDecoder
import org.obm.push.protocol.data.SyncDecoder

object GatlingDecoders {

	lazy val timeZoneDecoder = GatlingTimeZoneDecoder
	lazy val timeZoneConverter = GatlingTimeZoneConverter
	
	lazy val meetingRequestDecoder = new MSMeetingRequestDecoder(timeZoneDecoder, timeZoneConverter) {}
	lazy val syncDecoder = new SyncDecoder(decoderFactory) {}

	lazy val calendarDecoder = new CalendarDecoder(timeZoneDecoder, timeZoneConverter) {}
	lazy val emailDecoder = new MSEmailDecoder(meetingRequestDecoder) {}
	lazy val contactsDecoder: ContactDecoder = null
	lazy val taskDecoder: TaskDecoder = null
	
	lazy val decoderFactory = new DecoderFactory(
			new Provider[CalendarDecoder]() {
				override def get = calendarDecoder
			},
			new Provider[ContactDecoder]() {
				override def get = contactsDecoder
			},
			new Provider[TaskDecoder]() {
				override def get = taskDecoder
			},
			new Provider[MSEmailDecoder]() {
				override def get = emailDecoder
			}
	) {}
	
}