/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync;

import org.obm.sync.calendar.EventPrivacy;


public enum ServerCapability {
	/**
	 * Used to indicate whether the calendar API supports {@link NotAllowedException}
	 * when there's a rights problem. 
	 */
	CALENDAR_HANDLER_SUPPORTS_NOTALLOWEDEXCEPTION,
	/**
	 * Used to indicate if the calendar API supports {@link EventPrivacy}
	 * new CONFIDENTIAL value.
	 */
	CONFIDENTIAL_EVENTS,
	/**
	 * Used to advertise support for the storeEvent endpoint.
	 */
	CALENDAR_HANDLER_SUPPORTS_STOREEVENT,
	/**
	 * Used to advertise support for pagination in list* endpoints.
	 */
	CALENDAR_HANDLER_SUPPORTS_PAGINATION,
	/**
	 * Advertise support for anonymized events
	 */
	SERVER_SIDE_ANONYMIZATION,
  /**
	 * Used to advertise support for the storeContact endpoint.
	 */
	ADDRESS_BOOK_HANDLER_SUPPORTS_STORECONTACT
}
